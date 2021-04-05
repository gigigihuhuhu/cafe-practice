package com.santa.cafe.order;

import com.santa.cafe.order.model.Order;
import com.santa.cafe.order.model.OrderStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Before
    public void setUp() {
    }

    @Test
    public void 초기데이터에_저장된_ID가_100인_ORDER를_조회한다() {
    }

    @Test
    public void SetUp한_데이터가_조회된다() {
    }
}
