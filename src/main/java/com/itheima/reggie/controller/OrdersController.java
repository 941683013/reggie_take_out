package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    OrdersService ordersService;
    BaseContext baseContext;
    UserService userService;
    OrderDetailService orderDetailService;

    @Autowired
    public OrdersController(OrdersService ordersService, BaseContext baseContext, UserService userService
                            ,OrderDetailService orderDetailService) {
        this.ordersService = ordersService;
        this.baseContext = baseContext;
        this.userService = userService;
        this.orderDetailService = orderDetailService;
    }

    @GetMapping("/userPage")
    public R<Page<Orders>> userPage(int page, int pageSize) {

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Long userId = baseContext.getCurrentId();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, userId);

        ordersService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 提交订单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, Integer number) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        ordersService.page(pageInfo);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> put(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return R.success("派送成功");
    }

    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders) {
        orders = ordersService.getById(orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }
}
