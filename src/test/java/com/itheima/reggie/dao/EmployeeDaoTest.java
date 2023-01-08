package com.itheima.reggie.dao;

import com.itheima.reggie.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
public class EmployeeDaoTest {

    @Autowired
    EmployeeMapper employeeDao;

    @Test
    void selectAll() {
        List list = employeeDao.selectList(null);
        log.info(list.toString());
    }
}
