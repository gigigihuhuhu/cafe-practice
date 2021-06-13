# Repository Layer 테스트 케이스 작성

## Repository Slice Test 작성

* JPA 쿼리 검증을 위한 실제 데이터 조회 테스트

* @DataJpaTest를 이용한 slice test


```java
@RunWith(SpringRunner.class)
@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void 초기데이터에_저장된_ID가_100인_ORDER를_조회한다() {
        Order order = orderRepository.findById(100).orElse(new Order());

        assertThat(order.getId(), is(100));
        assertThat(order.getStatus(), is(OrderStatus.PREPARING));
    }
}
```

* setUp 메서드에 테스트를 위한 초기 데이터 준비

```java

@RunWith(SpringRunner.class)
@DataJpaTest
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Before
    public void setUp() {
        Order waitingOrder = new Order();
        waitingOrder.setStatus(OrderStatus.WAITING);
        Order canceledOrder = new Order();
        canceledOrder.setStatus(OrderStatus.CANCEL);

        orderRepository.saveAll(Arrays.asList(waitingOrder, canceledOrder));
    }

    @Test
    public void SetUp한_데이터가_조회된다() {
        List<Order> order = orderRepository.findAllByStatus(OrderStatus.CANCEL);

        assertThat(order.size(), is(1));
        assertThat(order.get(0).getStatus(), is(OrderStatus.CANCEL));
    }
}
```

[이전](06_write_tests_in_controller_layer.md)