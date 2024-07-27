package com.watergun.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.reggie.common.R;
import com.watergun.reggie.dto.SetmealDto;
import com.watergun.reggie.entity.Category;
import com.watergun.reggie.entity.Setmeal;
import com.watergun.reggie.service.CategoryService;
import com.watergun.reggie.service.SetmealDishService;
import com.watergun.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息:{}", setmealDto);

        setmealService.saveWithDish(setmealDto);


        return R.success("添加套餐成功");
    }

    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.like(name != null, Setmeal::getName, name);
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage, setmealLambdaQueryWrapper);

        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        List<Setmeal> setmealList = setmealPage.getRecords();
        List<SetmealDto> setmealDtoList = setmealList.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }


            return setmealDto;


        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);


        return R.success(setmealDtoPage);

    }


    @DeleteMapping
    public R<String> delete(String ids) {
        log.info("要删除的id为:{}", ids);
        setmealService.deleteWithDish(ids);
        return R.success("删除成功");
    }


    @PostMapping("/status/{st}")
    public R<String> setStatus(@PathVariable int st, String ids) {
        log.info("要修改的套餐id为{},将状态修改为{}", ids, st);

        setmealService.setStatus(st, ids);

        return R.success("修改成功");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id) {
        log.info("要修改的id为:{}", id);
        SetmealDto withDishes = setmealService.getWithDishes(id);
        return R.success(withDishes);
    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info("要更新的套餐为:{}", setmealDto);

        setmealService.updateWithDishes(setmealDto);


        return R.success("更新套餐成功");
    }

    @GetMapping("/list")  // 在消费者端 展示套餐信息
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        Long categoryId = setmeal.getCategoryId();
        Integer status = setmeal.getStatus();
        //种类不为空才查
        queryWrapper.eq(categoryId != null, Setmeal::getCategoryId, categoryId);
        //在售状态才查
        queryWrapper.eq(status != null, Setmeal::getStatus, status);

        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmeals = setmealService.list(queryWrapper);

        return R.success(setmeals);
    }
}

