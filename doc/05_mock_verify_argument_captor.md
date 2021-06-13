# Service Layer 테스트 케이스 작성

## Service Unit 테스트 작성 – Argument Captor

* 마일리지가 차감 되는 것을 테스트하기 위해 mock object 를 활용하여 verify 한다.

* 마일리지 API의 getMileages를 Stubbing

```java
public class OrderServiceTest {
    @Test(expected = BizException.class)
    public void 마일리지로_결제하는경우_고객의마일리지가_TotalCost보다_적으면_예외를_발생한다() {
        //given
        when(mockMileageApiService.getMileages(CUSTOMER_ID)).thenReturn(1000);

        Map<String, Object> orderItem = getOrderItem(AMERICANO_ID, 2);

        List<Map<String, Object>> orderItemList = Arrays.asList(orderItem);

        //when
        Order result = subject.create(CUSTOMER_ID, orderItemList, PAYMENT_MILEAGE);

        //then
        verify(mockMileageApiService).minusMileages(anyInt(), any());
    }
}
```

* mock object 를 활용하여 마일리지 API의 minusMileages 를 verify 한다.

```java
public class OrderServiceTest {
    @Test
    public void 마일리지로_결제하는경우_고객의마일리지가_TotalCost보다_크거나같으면_마일리지API를_호출하여_마일리지를_차감한다() {
        //given
        when(mockMileageApiService.getMileages(CUSTOMER_ID)).thenReturn(3000);

        //when
        Map<String, Object> orderItem = new HashMap<>();
        orderItem.put("beverageId", AMERICANO_ID);
        orderItem.put("count", 2);

        Order result = subject.create(CUSTOMER_ID, Collections.singletonList(orderItem), PAYMENT_MILEAGE);

        //then
        verify(mockMileageApiService).minusMileages(anyInt(), any());
    }
}
```

* ArgumentCaptor를 이용하여 직접 접근이 불가능한 파라미터값 테스트

```java
public class OrderServiceTest {
    
    @Captor
    private ArgumentCaptor<Mileage> mileageCaptor;        
        
    @Test
    public void 마일리지로_결제하는경우_고객의마일리지가_TotalCost보다_크거나같으면_마일리지API를_호출하여_마일리지를_차감한다() {
        //given
        when(mockMileageApiService.getMileages(CUSTOMER_ID)).thenReturn(3000);

        Map<String, Object> orderItem = getOrderItem(AMERICANO_ID, 2);

        List<Map<String, Object>> orderItemList = Arrays.asList(orderItem);

        //when
        Order result = subject.create(CUSTOMER_ID, orderItemList, PAYMENT_MILEAGE);

        //then
        verify(mockMileageApiService).minusMileages(eq(CUSTOMER_ID), mileageCaptor.capture());

        Mileage appliedMileage = mileageCaptor.getValue();
        assertThat(appliedMileage.getValue(), is(2000.0));
        assertThat(result.getMileagePoint(), is(0.0));
    }
}
```

* test-step-02 branch


* mock object 를 활용하여 마일리지 적립하는 것을 verify 한다.

```java
public class OrderServiceTest {
    @Test
    public void 마일리지로_결제하는경우_마일리지API를_호출하여_마일리지를_적립하지_않는다() {
        //given
        when(mockMileageApiService.getMileages(CUSTOMER_ID)).thenReturn(3000);

        Map<String, Object> orderItem = getOrderItem(AMERICANO_ID, 2);

        List<Map<String, Object>> orderItemList = Arrays.asList(orderItem);

        //when
        Order result = subject.create(CUSTOMER_ID, orderItemList, PAYMENT_MILEAGE);

        //then
        verify(mockMileageApiService, never()).saveMileages(anyInt(), any());
    }

    @Test
    public void 마일리지로_결제하지않는경우_마일리지API를_호출하여_마일리지를_적립한다() {
        //given
        Map<String, Object> orderItem = getOrderItem(AMERICANO_ID, 2);

        List<Map<String, Object>> orderItemList = Arrays.asList(orderItem);

        //when
        Order result = subject.create(CUSTOMER_ID, orderItemList, PAYMENT_CASH);

        //then
        verify(mockMileageApiService).saveMileages(eq(CUSTOMER_ID), mileageCaptor.capture());
        Mileage appliedMileage = mileageCaptor.getValue();
        assertThat(appliedMileage.getValue(), is(200.0));
    }
}
```

* test-step-03 branch

[이전](04_depedency_breaking.md) [다음](06_write_tests_in_controller_layer.md)