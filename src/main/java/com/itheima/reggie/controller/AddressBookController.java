package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    AddressBookService addressBookService;

    @Autowired
    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;
    }

    @GetMapping("/default")
    public R<AddressBook> _default(HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("employee");

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        return R.success(addressBook);
    }

    @PutMapping("/default")
    public R<String> _default(@RequestBody AddressBook addressBook, HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("employee");

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        AddressBook addressBook1 = new AddressBook();
        addressBook1.setIsDefault(0);
        addressBookService.update(addressBook1, queryWrapper);

        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success("设为默认成功");
    }

    @PostMapping
    public R<String> addAddress(@RequestBody AddressBook addressBook, HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("employee");
        addressBook.setUserId(userId);

        addressBookService.save(addressBook);
        return R.success("保存成功");
    }

    @GetMapping("/list")
    public R<List<AddressBook>> list(HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute("employee");

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);

        List<AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }

    @GetMapping("/{id}")
    public R<AddressBook> getAddressBook(@PathVariable Long id) {

        AddressBook addressBook = addressBookService.getById(id);
        return R.success(addressBook);
    }

    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook) {

        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }
}
