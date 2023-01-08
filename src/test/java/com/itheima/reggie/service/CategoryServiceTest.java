package com.itheima.reggie.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class CategoryServiceTest {

    @Autowired
    CategoryService service;

    @Test
    void getCount() {
        int count = service.count();
        log.info("count = {}", count);
    }

}
