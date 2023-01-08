package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    DishService dishService;
    CategoryService categoryService;
    DishFlavorService dishFlavorService;

    @Autowired
    public DishController(DishService dishService, CategoryService categoryService, DishFlavorService dishFlavorService) {
        this.dishFlavorService = dishFlavorService;
        this.dishService = dishService;
        this.categoryService = categoryService;
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> dtoPage = new Page(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        dishService.page(pageInfo, queryWrapper);

        // 进行一个拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        List<Dish> dishes = pageInfo.getRecords();
        List<DishDto> list = new ArrayList<>();
        dishes.forEach((item-> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            list.add(dishDto);
        }));
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    @PostMapping
    public R<String> addMeal(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("添加成功");
    }

    @GetMapping("/{id}")
    public R<DishDto> getDishDtoById(@PathVariable Long id) {
        DishDto dishDto = new DishDto();
        Dish dish = dishService.getById(id);
        BeanUtils.copyProperties(dish, dishDto);
        Long categoryId = dish.getCategoryId();
        Category category = categoryService.getById(categoryId);
        String categoryName = category.getName();
        dishDto.setCategoryName(categoryName);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);

        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(dishFlavors);

        return R.success(dishDto);
    }

    @PutMapping
    @Transactional
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateById(dishDto);
        List<DishFlavor> dishFlavors = dishDto.getFlavors();
        dishFlavors.forEach((item->{
            item.setDishId(dishDto.getId());
        }));

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        dishFlavorService.saveBatch(dishFlavors);
        return R.success("菜品修改成功");
    }

    @PostMapping("/status/{status}")
    public R<String> setStatus(@PathVariable int status, Long[] ids) {
        Dish dish = new Dish();
        for(int i = 0; i < ids.length; i++) {
            dish.setId(ids[i]);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("已停售");
    }

    @DeleteMapping
    @Transactional
    public R<String> delete(Long[] ids) {
        for(int i = 0; i < ids.length; i++) {
            Dish dish = dishService.getById(ids[i]);

            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, ids[i]);
            dishService.removeById(ids[i]);
            dishFlavorService.remove(queryWrapper);
        }
        return R.success("删除成功");
    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Long categoryId) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId, categoryId);

        List<Dish> dishes = dishService.list(queryWrapper);
        List<DishDto> list = new ArrayList<>();
        dishes.forEach((item->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long dishId = item.getId();

            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavors);

            Category category = categoryService.getById(categoryId);
            dishDto.setCategoryName(category.getName());
            list.add(dishDto);
        }));

        return R.success(list);
    }
}
