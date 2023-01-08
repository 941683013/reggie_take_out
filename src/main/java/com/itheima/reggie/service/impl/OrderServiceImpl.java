package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    BaseContext baseContext;

    ShoppingCartService shoppingCartService;
    AddressBookService addressBookService;
    UserService userService;
    OrderDetailService orderDetailService;

    @Autowired
    public OrderServiceImpl(BaseContext baseContext, ShoppingCartService shoppingCartService
    ,AddressBookService addressBookService, UserService userService
    ,OrderDetailService orderDetailService) {
        this.baseContext = baseContext;
        this.shoppingCartService = shoppingCartService;
        this.addressBookService = addressBookService;
        this.userService = userService;
        this.orderDetailService = orderDetailService;
    }

    @Override
    @Transactional
    public void submit(Orders orders) {
        // 获取用户ID
        Long userId = baseContext.getCurrentId();

        // 获取用户购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if(shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }
        AtomicInteger amount = new AtomicInteger(0);
        shoppingCarts.forEach((item->{
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
        }));

        // 向Orders表插入一条数据
        User user = userService.getById(userId);
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if(addressBook == null) {
            throw new CustomException("没有默认地址，无法下单");
        }

        long orderId = IdWorker.getId();
        orders.setNumber(String.valueOf(orderId));
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());

        log.info(addressBook.getDetail());
        String address = (addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail());
        orders.setAddress(address);

        save(orders);
        // 向OrdersDetail插入多条数据
        List<OrderDetail> list = new ArrayList<>();
        shoppingCarts.forEach((item->{
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderId);
            detail.setNumber(item.getNumber());
            detail.setDishFlavor(item.getDishFlavor());
            detail.setDishId(item.getDishId());
            detail.setSetmealId(item.getSetmealId());
            detail.setName(item.getName());
            detail.setImage(item.getImage());
            detail.setAmount(item.getAmount());
            list.add(detail);
        }));
        orderDetailService.saveBatch(list);
        // 清空购物车
        LambdaQueryWrapper<ShoppingCart> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(ShoppingCart::getUserId, userId);

        shoppingCartService.remove(queryWrapper);
    }
}
