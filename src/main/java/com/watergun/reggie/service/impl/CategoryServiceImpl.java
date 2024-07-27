package com.watergun.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.reggie.common.CustomException;
import com.watergun.reggie.entity.Category;
import com.watergun.reggie.entity.Dish;
import com.watergun.reggie.entity.Setmeal;
import com.watergun.reggie.mapper.CategoryMapper;
import com.watergun.reggie.service.CategoryService;
import com.watergun.reggie.service.DishService;
import com.watergun.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    DishService dishService;

    @Autowired
    SetmealService setmealService;

    @Override
    public void remove(@RequestParam("ids") Long id) {

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);

        //查询当前分类是否关联了菜品
        if (count1>0){
            throw new CustomException("当前分类下已经关联菜品，不能删除");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        //查询当前分类是否关联了套餐
        if (count2>0){
            throw new CustomException("当前分类下已经关联套餐，不能删除");
        }


        //正常删除
        super.removeById(id);

    }
}
