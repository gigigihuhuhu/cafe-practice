# Service Layer 테스트 케이스 작성

## OrderService 테스트 코드 작성

```java
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    @Test
    public void 주문을하면_OrderItem들의_가격을_합한_TotalCost를_계산한다() {
    }

    @Test
    public void 등록되지않은_음료가_OrderItem에_포함되면_해당금액은_TotalCost에서_제외한다() {
    }
    
    @Test
    public void 매월_마지막날에_주문하면_TotalCost에서_10퍼센트가_할인된다() {
    }

    @Test
    public void 현금으로_결제시_TotalCost의_10퍼센트를_마일리지로_적립한다() {
    }

    @Test
    public void 카드로_결제시_TotalCost의_5퍼센트를_마일리지로_적립한다() {
    }

    @Test
    public void 마일리지로_결제하는경우_고객의마일리지가_TotalCost보다_적으면_예외를_발생한다() {
    }

    @Test
    public void 마일리지로_결제하는경우_고객의마일리지가_TotalCost보다_크거나같으면_마일리지API를_호출하여_마일리지를_차감한다() {
    }

    @Test
    public void 마일리지로_결제하는경우_마일리지API를_호출하여_마일리지를_적립하지_않는다() {
    }

    @Test
    public void 마일리지로_결제하지않는경우_마일리지API를_호출하여_마일리지를_적립한다() {
    }
}
```

## Service Unit 테스트 작성

* OrderService와 의존성이 있는 클래스들을 @Mock Annotation 사용하여 mocking 

```java
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;           
import static org.mockito.Mockito.*;              
                                                  
@RunWith(MockitoJUnitRunner.class)                
public class OrderServiceTest {                   
                                                  
    @InjectMocks                                  
    OrderService subject;                         
    @Mock                                         
    CustomerService mockCustomerService;          
    @Mock                                         
    OrderRepository mockOrderRepository;          
    @Mock                                         
    MileageApiService mockMileageApiService;      
    @Mock                                         
    BeverageRepository mockBeverageRepository;    
    @Mock                                         
    OrderItemRepository mockOrderItemRepository;  
}
```

* BeverageRepository stubbing

    * Beverage (1, "americano", 1000, BeverageSize.SMALL) 

```java
public class OrderServiceTest {                                 
    private static final int CUSTOMER_ID = 12345;
    private static final int PAYMENT_CASH = 1;
    
    @Test
    public void 주문을하면_OrderItem들의_가격을_합한_TotalCost를_계산한다() {
        //given
        Beverage beverage = new Beverage(1, "americano", 1000, BeverageSize.SMALL);
        when(mockBeverageRepository.getOne(1)).thenReturn(beverage);

        Map<String, Object> orderItem = new HashMap<>();
        orderItem.put("beverageId", 1);
        orderItem.put("count", 2);

        List<Map<String, Object>> orderItemList = Arrays.asList(orderItem);

        //when
        Order result = subject.create(CUSTOMER_ID, orderItemList, PAYMENT_CASH);

        //then
        assertThat(result.getTotalCost(), is(2000.0));
    }
}
```

* 등록되지 않은 음료는 BeverageRepository 에서 null로 stubbing  

```java
public class OrderServiceTest {                                 
    private static final int CUSTOMER_ID = 12345;
    private static final int PAYMENT_CASH = 1;

    @Test
    public void 등록되지않은_음료가_OrderItem에_포함되면_해당금액은_TotalCost에서_제외한다() {
        //given
        Beverage beverage = new Beverage(1, "americano", 1000, BeverageSize.SMALL);
        when(mockBeverageRepository.getOne(1)).thenReturn(beverage);
        when(mockBeverageRepository.getOne(2)).thenReturn(null);

        Map<String, Object> orderItem = new HashMap<>();
        orderItem.put("beverageId", 1);
        orderItem.put("count", 2);

        Map<String, Object> notValidOrderItem = new HashMap<>();
        notValidOrderItem.put("beverageId", 2);
        notValidOrderItem.put("count", 3);

        List<Map<String, Object>> orderItemList = Arrays.asList(orderItem, notValidOrderItem);

        //when
        Order result = subject.create(CUSTOMER_ID, orderItemList, PAYMENT_CASH);

        //then
        assertThat(result.getTotalCost(), is(2000.0));
    }
}
```

[이전](02_domain_overview.md) [다음](04_depedency_breaking.md)