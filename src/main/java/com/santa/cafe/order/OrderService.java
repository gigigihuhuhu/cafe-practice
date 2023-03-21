package com.santa.cafe.order;

import com.santa.cafe.api.alarm.AlarmApiService;
import com.santa.cafe.api.mileage.Mileage;
import com.santa.cafe.api.mileage.MileageApiService;
import com.santa.cafe.beverage.BeverageRepository;
import com.santa.cafe.beverage.model.Beverage;
import com.santa.cafe.customer.CustomerService;
import com.santa.cafe.customer.model.Customer;
import com.santa.cafe.exception.BizException;
import com.santa.cafe.exception.NotFoundException;
import com.santa.cafe.order.model.Order;
import com.santa.cafe.order.model.OrderItem;
import com.santa.cafe.order.model.OrderStatus;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 2021.02.20 홍길동
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MileageApiService mileageApiService;
    private final CustomerService customerService;
    private final BeverageRepository beverageRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository,
                        MileageApiService mileageApiService,
                        CustomerService customerService,
                        BeverageRepository beverageRepository,
                        OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.mileageApiService = mileageApiService;
        this.customerService = customerService;
        this.beverageRepository = beverageRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Order getOrder(int orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order is not found."));
    }

    public List<OrderItem> getOrderItems(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order is not found."));

        return order.getOrderItems();
    }

    public OrderStatus getOrderStatus(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order is not found."));

        return order.getStatus();
    }

    public Customer getCustomer(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order is not found."));

        return order.getCustomer();
    }

    /**
     * 주문 생성
     * 1. orderItem 생성 및 주문 총 비용 계산
     * 2. 할인 프로모션 : 매월 마지막 날이면 10% 할인
     * 3. 결제 유형에 따라 마일리지 적립
     * 4. order, orderItem DB 저장
     * @param customerId
     * @param orderItemList
     * @return
     */
    @Transactional
    public Order create(int customerId, List<Map<String, Object>> orderItemList, int payment) {
        Customer customer = customerService.getCustomer(customerId);
        Order order = new Order();
        order.setStatus(OrderStatus.WAITING);
        order.setCustomer(customer);
        order.setPayment(payment);
        List<OrderItem> orderItems = new ArrayList<>();
        double totalCost = 0;

        // 1. 주문된 항목의 Total Cost계산
        for(Map<String, Object> orderItemMap: orderItemList) {
            Beverage beverage = beverageRepository.getOne((Integer) orderItemMap.get("beverageId"));
            if(beverage == null) {
                continue;
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setCount((Integer) orderItemMap.get("count"));
            orderItem.setBeverage(beverage);
            orderItem.setOrder(order);
            orderItems.add(orderItem);
            totalCost += orderItem.getCount() * beverage.getCost();
        }

        // 2. 매월 마지막 날이면 10% 할인
        totalCost = this.getDiscountedTotalCost(totalCost);
        order.setTotalCost(totalCost);

        double mileagePoint = 0;
        switch(payment) {
            case 1: // cash 10%
                //2021.1.1 현금 적립률 8% -> 10%로 변경
                //mileagePoint = totalCost * 0.05;
                mileagePoint = totalCost * 0.1;
                break;
            case 2: // credit card 5%
                mileagePoint = totalCost * 0.05;
                break;
            case 3: // mileage
                break;
        }

        if(payment == 3) { // pay mileage
            int customerMileage = mileageApiService.getMileages(customerId);
            if(customerMileage >= totalCost) {
                Mileage mileage = new Mileage(customerId, order.getId(), totalCost);
                mileageApiService.minusMileages(customerId, mileage);
            } else {
                throw new BizException("mileage is not enough");
            }
        } else {
            Mileage mileage = new Mileage(customerId, order.getId(), mileagePoint);
            mileageApiService.saveMileages(customerId, mileage);

            if(payment == 1) {
                payWithCash();
            } else if(payment == 2) {
                payWithCard();
            }
        }

        order.setMileagePoint(mileagePoint);
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        return order;
    }

    private double getDiscountedTotalCost(double totalCost) {
        if(isLastDayOfMonth()) {
            totalCost = totalCost * 0.9;
        }
        return totalCost;
    }

    protected boolean isLastDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        int lastDayOfMonth = cal.getActualMaximum(Calendar.DATE);
        int todayDate = cal.get(Calendar.DATE);

        return lastDayOfMonth == todayDate;
    }

    private void payWithCard() {
    }

    private void payWithCash() {
    }

    /**
     *
     * @param orderId
     */
    @Transactional
    public void cancel(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order is not found."));

        if (OrderStatus.WAITING.equals(order.getStatus())) {
            order.setStatus(OrderStatus.CANCEL);
            orderRepository.save(order);

            Mileage mileage = new Mileage(order.getCustomer().getId(), order.getId(), order.getMileagePoint());
            mileageApiService.minusMileages(order.getCustomer().getId(), mileage);

            payCancel(order.getPayment());
        }
    }

    private void payCancel(int payment) {
        switch (payment) {
            case 1:
                cancelCash();
            case 2:
                cancelCard();
            case 3:
                cancelMileage();
            default:
                break;
        }
    }

    private void cancelMileage() {
    }

    private void cancelCard() {
    }

    private void cancelCash() {
    }

}
