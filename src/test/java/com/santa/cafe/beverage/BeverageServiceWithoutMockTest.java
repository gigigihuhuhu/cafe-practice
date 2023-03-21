package com.santa.cafe.beverage;

import com.santa.cafe.beverage.model.Beverage;
import com.santa.cafe.beverage.model.BeverageSize;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = "spring.datasource.data=")
public class BeverageServiceWithoutMockTest {

    @Autowired
    private BeverageService subject;

    @Autowired
    private BeverageRepository beverageRepository;

    @Before
    public void setUp() {
        List<Beverage> beverageList = new ArrayList<>();
        Beverage coke = new Beverage(1, "coke", 1000, BeverageSize.SMALL);
        beverageList.add(coke);
        beverageRepository.saveAll(beverageList);
    }

    @After
    public void tearDown() throws Exception {
        beverageRepository.deleteAll();
    }

    @Test
    public void whenGetBeverages_thenReturnBeverages() {
        //given

        //when
        List<Beverage> result = subject.getBeverages();

        //then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getName()).isEqualTo("coke");
    }

}
