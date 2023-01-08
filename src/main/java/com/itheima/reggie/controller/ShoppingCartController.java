package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    ShoppingCartService service;
    BaseContext baseContext;
    SetmealDishService setmealDishService;
    DishFlavorService dishFlavorService;

    @Autowired
    public ShoppingCartController(ShoppingCartService service, BaseContext baseContext, SetmealDishService setmealDishService
    ,DishFlavorService dishFlavorService) {
        this.service = service;
        this.baseContext = baseContext;
        this.setmealDishService = setmealDishService;
        this.dishFlavorService = dishFlavorService;
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        Long userId = baseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        List<ShoppingCart> shoppingCarts = service.list(queryWrapper);
        return R.success(shoppingCarts);
    }

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("添加{}到购物车", shoppingCart.getName());

        Long userId = baseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        shoppingCart.setCreateTime(LocalDateTime.now());

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        Long dishId = shoppingCart.getDishId();
        if(dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        }
        else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart cart = service.getOne(queryWrapper);
        if(cart == null) {
            shoppingCart.setNumber(1);
            service.save(shoppingCart);
        }
        else {
            cart.setNumber(cart.getNumber() + 1);
            service.updateById(cart);
        }

        ShoppingCart one = service.getOne(queryWrapper);
        return R.success(one);
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        Long currentId = baseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);

        Long dishId = shoppingCart.getDishId();
        if(dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        }
        else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart cart = service.getOne(queryWrapper);
        if(cart.getNumber() == 1) {
            service.removeById(cart);
        }
        else {
            cart.setNumber(cart.getNumber() - 1);
            service.updateById(cart);
        }

        ShoppingCart one = service.getOne(queryWrapper);
        return R.success(one);
    }

    @DeleteMapping("/clean")
    public R<String> clear() {
        Long userId = baseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        service.remove(queryWrapper);
        return R.success("已清空");
    }
}
