package com.santa.cafe.order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void OrderId로_GET_API를_요청하면_해당_ORDER를_리턴한다() throws Exception {
        String orderResult = "{\"id\":100,\"totalCost\":3000.0,\"mileagePoint\":100.0,\"payment\":1,\"status\":\"PREPARING\",\"orderItems\":[{\"id\":1,\"count\":3}],\"customer\":{\"id\":1,\"name\":\"customer1\"}}";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/orders/100"))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("PREPARING"))
                .andExpect(content().string(is(orderResult)))
                .andExpect(status().isOk());
    }

    @Test
    public void 주문정보로_POST_API를_요청하면_해당_주문을_생성한다() throws Exception {
        String orderParam = "{\"customerId\": 1,\"payment\": 3,\"orderItems\": []}";

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/api/v1/orders")
                        .content(orderParam)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}