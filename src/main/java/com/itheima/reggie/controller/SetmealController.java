package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    SetmealService setmealService;
    CategoryService categoryService;
    SetmealDishService setmealDishService;
    DishService dishService;

    @Autowired
    public SetmealController(SetmealService setmealService, CategoryService categoryService, SetmealDishService setmealDishService
    ,DishService dishService) {
        this.setmealService = setmealService;
        this.categoryService = categoryService;
        this.setmealDishService = setmealDishService;
        this.dishService = dishService;
    }

    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);

        setmealService.page(pageInfo, queryWrapper);
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = new ArrayList<>();
        records.forEach((item->{
            SetmealDto dto = new SetmealDto();
            BeanUtils.copyProperties(item, dto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            dto.setCategoryName(category.getName());
            list.add(dto);
        }));

        dtoPage.setRecords(list);

        return R.success(dtoPage);
    }

    /**
     * 添加套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> addSetmeal(@RequestBody SetmealDto setmealDto) {
        setmealService.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes.forEach((item->{
            item.setSetmealId(setmealDto.getId());
        }));

        setmealDishService.saveBatch(setmealDishes);
        return R.success("添加成功");
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus, 1);

        int count = setmealService.count(setmealLambdaQueryWrapper);
        if(count > 0) throw new CustomException("商品再售，无法删除");

        setmealService.removeByIds(ids);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper);
        return R.success("删除成功");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getSetmealDto(@PathVariable Long id) {
        Setmeal setmeal = setmealService.getById(id);
        SetmealDto setmealDto = new SetmealDto();

        BeanUtils.copyProperties(setmeal, setmealDto);
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);

        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishes);

        return R.success(setmealDto);
    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateById(setmealDto);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        // 先删除已有的套餐内菜品
        setmealDishService.remove(queryWrapper);

        // 插入修改后的菜品
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.forEach((item->{
            item.setSetmealId(setmealDto.getId());
        }));
        setmealDishService.saveBatch(setmealDishes);
        return R.success("套餐修改成功");
    }

    @PostMapping("/status/{status}")
    public R<String> setStatus(@PathVariable int status, Long[] ids) {
        for(int i = 0; i < ids.length; i++) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(ids[i]);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("状态修改成功");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Long categoryId, int status) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId, categoryId);
        queryWrapper.eq(Setmeal::getStatus, status);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    @GetMapping("/dish/{id}")
    public R<Dish> dish(@PathVariable Long id) {
        Dish dish = dishService.getById(id);
        return R.success(dish);
    }
}
