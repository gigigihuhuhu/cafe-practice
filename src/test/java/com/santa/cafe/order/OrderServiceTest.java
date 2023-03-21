package com.santa.cafe.order;

import com.santa.cafe.api.mileage.Mileage;
import com.santa.cafe.api.mileage.MileageApiService;
import com.santa.cafe.beverage.BeverageRepository;
import com.santa.cafe.beverage.model.Beverage;
import com.santa.cafe.beverage.model.BeverageSize;
import com.santa.cafe.customer.CustomerService;
import com.santa.cafe.exception.BizException;
import com.santa.cafe.order.model.Order;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    private static final int PAYMENT_CASH = 1;
    private static final int PAYMENT_CARD = 2;
    private static final int PAYMENT_MILEAGE = 3;
    private static final int CUSTOMER_ID = 1;
    private static final int BEVERAGE_ID_AMERICANO = 111;
    private static final int BEVERAGE_ID_LATTE = 222;

    @Mock
    private OrderRepository mockOrderRepository;
    @Mock
    private MileageApiService mockMileageApiService;
    @Mock
    private CustomerService mockCustomerService;
    @Mock
    private BeverageRepository mockBeverageRepository;
    @Mock
    private OrderItemRepository mockOrderItemRepository;

    private TestableOrderService subject;

    @Captor
    private ArgumentCaptor<Mileage> mileageArgumentCaptor;

    @Before
    public void setUp() {
        subject = new TestableOrderService(
                mockOrderRepository,
                mockMileageApiService,
                mockCustomerService,
                mockBeverageRepository,
                mockOrderItemRepository);
    }

    @Test
    public void 주문을하면_OrderItem들의_가격을_합한_TotalCost를_계산한다() {
        //given
        List<Map<String, Object>> orderItems = getDefaultOrderItems();

        double expectedTotalCost = 320.0; // 10 * 10 + 11 * 20

        //when
        Order order = subject.create(CUSTOMER_ID, orderItems, PAYMENT_CASH);

        //then
        assertThat(order.getTotalCost()).isEqualTo(expectedTotalCost);
    }

    @Test
    public void 등록되지않은_음료가_OrderItem에_포함되면_해당금액은_TotalCost에서_제외한다() {
        //given
        List<Map<String, Object>> orderItems = getDefaultOrderItems();

        Map<String, Object> orderItemThatDoesNotExist = new HashMap<>();
        orderItemThatDoesNotExist.put("beverageId", -1);
        orderItemThatDoesNotExist.put("count", 10);
        orderItems.add(orderItemThatDoesNotExist);

        double expectedTotalCost = 320.0; // 10 * 10 + 11 * 20

        //when
        Order order = subject.create(CUSTOMER_ID, orderItems, PAYMENT_CASH);

        //then
        assertThat(order.getTotalCost()).isEqualTo(expectedTotalCost);
    }

    @Test
    public void 매월_마지막날에_주문하면_TotalCost에서_10퍼센트가_할인된다() {
//        Calendar mockedCalendar = Mockito.mock(Calendar.class);
//        given(mockedCalendar.getActualMaximum(Calendar.DATE)).willReturn(0);
//        given(mockedCalendar.get(Calendar.DATE)).willReturn(0);
//        MockedStatic<Calendar> calendarMockedStatic = Mockito.mockStatic(Calendar.class);
//        calendarMockedStatic.when(Calendar::getInstance).thenReturn(mockedCalendar);

        //given
        subject.isLastDayOfMonth = true;
        List<Map<String, Object>> orderItems = getDefaultOrderItems();

        double expectedTotalCost = 288.0; // 10 * 10 + 11 * 20

        //when
        Order order = subject.create(CUSTOMER_ID, orderItems, PAYMENT_CASH);

        //then
        assertThat(order.getTotalCost()).isEqualTo(expectedTotalCost);

        subject.isLastDayOfMonth = false;
    }

    @Test
    public void 현금으로_결제시_TotalCost의_10퍼센트를_마일리지로_적립한다() {
        //given
        List<Map<String, Object>> orderItems = getDefaultOrderItems();

        double expectedMileagePoint = 32.0; // 320 * 0.1

        //when
        Order order = subject.create(CUSTOMER_ID, orderItems, PAYMENT_CASH);

        //then
        assertThat(order.getMileagePoint()).isEqualTo(expectedMileagePoint);
        verify(mockMileageApiService).saveMileages(eq(CUSTOMER_ID), mileageArgumentCaptor.capture());
        Mileage capturedMileage = mileageArgumentCaptor.getValue();
        assertThat(capturedMileage.getValue()).isEqualTo(expectedMileagePoint);
    }

    @Test
    public void 카드로_결제시_TotalCost의_5퍼센트를_마일리지로_적립한다() {
        //given
        List<Map<String, Object>> orderItems = getDefaultOrderItems();

        double expectedMileagePoint = 16.0; // 320 * 0.1

        //when
        Order order = subject.create(CUSTOMER_ID, orderItems, PAYMENT_CARD);

        //then
        assertThat(order.getMileagePoint()).isEqualTo(expectedMileagePoint);
        verify(mockMileageApiService).saveMileages(eq(CUSTOMER_ID), mileageArgumentCaptor.capture());
        Mileage capturedMileage = mileageArgumentCaptor.getValue();
        assertThat(capturedMileage.getValue()).isEqualTo(expectedMileagePoint);
    }

    @Test
    public void 마일리지로_결제하는경우_고객의마일리지가_TotalCost보다_적으면_예외를_발생한다() {
        //given
        List<Map<String, Object>> orderItems = getDefaultOrderItems();

        int mileagePointLeft = 310; // expected total cost = 320

        given(mockMileageApiService.getMileages(CUSTOMER_ID)).willReturn(mileagePointLeft);

        //when
        try {
            subject.create(CUSTOMER_ID, orderItems, PAYMENT_MILEAGE);
        }
        //then
        catch (BizException bizException) {
            assertThat(bizException.getMessage()).isEqualTo("mileage is not enough");
        }
    }

    @Test
    public void 마일리지로_결제하는경우_고객의마일리지가_TotalCost보다_크거나같으면_마일리지API를_호출하여_마일리지를_차감한다() {
        //given
        List<Map<String, Object>> orderItems = getDefaultOrderItems();

        int mileagePointLeft = 320;
        double expectedTotalCost = 320;

        given(mockMileageApiService.getMileages(CUSTOMER_ID)).willReturn(mileagePointLeft);

        //when
        subject.create(CUSTOMER_ID, orderItems, PAYMENT_MILEAGE);

        //then
        verify(mockMileageApiService).minusMileages(eq(CUSTOMER_ID), mileageArgumentCaptor.capture());
        Mileage capturedMileage = mileageArgumentCaptor.getValue();
        assertThat(capturedMileage.getValue()).isEqualTo(expectedTotalCost);
    }

    @Test
    public void 마일리지로_결제하는경우_마일리지API를_호출하여_마일리지를_적립하지_않는다() {
        //given
        List<Map<String, Object>> orderItems = getDefaultOrderItems();

        int mileagePointLeft = 1000;

        given(mockMileageApiService.getMileages(CUSTOMER_ID)).willReturn(mileagePointLeft);

        //when
        subject.create(CUSTOMER_ID, orderItems, PAYMENT_MILEAGE);

        //then
        verify(mockMileageApiService, times(0)).saveMileages(eq(CUSTOMER_ID), any());
    }

    @Test
    public void 마일리지로_결제하지않는경우_마일리지API를_호출하여_마일리지를_적립한다() {
        //given
        List<Map<String, Object>> orderItems = getDefaultOrderItems();

        //when
        subject.create(CUSTOMER_ID, orderItems, PAYMENT_CARD);

        //then
        verify(mockMileageApiService).saveMileages(eq(CUSTOMER_ID), any());
    }

    private Map<String, Object> setupOrderBeverage(int beverageId, String beverageName, int costPerOneBeverage, int count) {
        Beverage beverage = new Beverage(beverageId, beverageName, costPerOneBeverage, BeverageSize.SMALL);
        Map<String, Object> orderBeverage = new HashMap<>();
        orderBeverage.put("beverageId", beverage.getId());
        orderBeverage.put("count", count);
        given(mockBeverageRepository.getOne(beverage.getId())).willReturn(beverage);
        return orderBeverage;
    }

    private List<Map<String, Object>> getDefaultOrderItems() {
        Map<String, Object> orderItem1 = setupOrderBeverage(BEVERAGE_ID_AMERICANO, "americano", 10, 10);
        Map<String, Object> orderItem2 = setupOrderBeverage(BEVERAGE_ID_LATTE, "latte", 20, 11);
        List<Map<String, Object>> orderItems = Lists.newArrayList(orderItem1, orderItem2);
        return orderItems;
    }

    private class TestableOrderService extends OrderService {
        boolean isLastDayOfMonth = false;

        public TestableOrderService(OrderRepository orderRepository, MileageApiService mileageApiService, CustomerService customerService, BeverageRepository beverageRepository, OrderItemRepository orderItemRepository) {
            super(orderRepository, mileageApiService, customerService, beverageRepository, orderItemRepository);
        }

        @Override
        protected boolean isLastDayOfMonth() {
            return this.isLastDayOfMonth;
        }
    }
}
