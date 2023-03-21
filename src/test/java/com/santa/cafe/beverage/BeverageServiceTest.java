package com.santa.cafe.beverage;

import com.santa.cafe.beverage.model.Beverage;
import com.santa.cafe.beverage.model.BeverageSize;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BeverageServiceTest {
    @InjectMocks
    private BeverageService beverageService;

    @Mock
    private BeverageRepository mockBeverageRepository;

    @Test
    public void whenGetBeverages_thenReturnAllBeverages() {
        Beverage americano = new Beverage(1, "americano", 10, BeverageSize.SMALL);
        Beverage latte = new Beverage(2, "latte", 20, BeverageSize.REGULAR);

        given(mockBeverageRepository.findAll()).willReturn(Lists.newArrayList(
                americano, latte
        ));

        List<Beverage> result = beverageService.getBeverages();

        assertThat(result).contains(americano, latte);
        verify(mockBeverageRepository,times(1)).findAll();

    }

    @Test
    public void whenCreateBeverage_ThenReturnBeverageList() {
        //given
        Beverage americano = new Beverage(1, "americano", 10, BeverageSize.SMALL);
        Beverage latte = new Beverage(2, "latte", 20, BeverageSize.REGULAR);

        List<Beverage> beverages = Lists.newArrayList(americano,latte);

        given(mockBeverageRepository.saveAll(beverages)).willReturn(beverages);

        //when
        List<Beverage> result = beverageService.createBeverages(beverages);

        //then
        assertThat(result).contains(americano, latte);
        verify(mockBeverageRepository,times(1)).saveAll(beverages);

    }
}