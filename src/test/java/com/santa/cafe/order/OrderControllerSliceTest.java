package com.santa.cafe.order;

import com.santa.cafe.order.model.Order;
import com.santa.cafe.order.model.OrderStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(OrderController.class)
public class OrderControllerSliceTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrderService mockOrderService;

    @Test
    public void OrderId로_GET_API를_요청하면_해당_ORDER를_리턴한다() {
    }

    @Test
    public void 주문정보로_POST_API를_요청하면_해당_주문을_생성한다() {
    }
}
