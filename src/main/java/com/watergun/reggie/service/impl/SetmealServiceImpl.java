package com.watergun.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.watergun.reggie.common.CustomException;
import com.watergun.reggie.dto.SetmealDto;
import com.watergun.reggie.entity.Setmeal;
import com.watergun.reggie.entity.SetmealDish;
import com.watergun.reggie.mapper.SetmealMapper;
import com.watergun.reggie.service.SetmealDishService;
import com.watergun.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);


    }

    @Override
    @Transactional
    public void deleteWithDish(String ids) {
        String[] setmealIdsStr = ids.split(",");

        List<Long> setmealIds = Arrays.stream(setmealIdsStr).map((item)->{
            return Long.parseLong(item);
        }).collect(Collectors.toList());

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,setmealIds);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(setmealLambdaQueryWrapper);
        if(count>0){
            throw new CustomException("套餐正在售卖中，不能删除，请先停售套餐");
        }

        this.removeByIds(setmealIds);

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,setmealIds);

        setmealDishService.remove(lambdaQueryWrapper);

    }

    @Override
    public void setStatus(int st, String ids) {
        String[] id = ids.split(",");
        List<Long> setmealIds = Arrays.stream(id).map((item)->{
            return Long.parseLong(item);
        }).collect(Collectors.toList());

        List<Setmeal> setmealList = setmealIds.stream().map((item)->{
            Setmeal setmeal = new Setmeal();
            setmeal.setId(item);
            setmeal.setStatus(st);
            return setmeal;
        }).collect(Collectors.toList());

        this.updateBatchById(setmealList);
    }

    @Override
    public SetmealDto getWithDishes(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();

        BeanUtils.copyProperties(setmeal,setmealDto);

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);

        List<SetmealDish> list = setmealDishService.list(lambdaQueryWrapper);

        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    @Override
    @Transactional
    public void updateWithDishes(SetmealDto setmealDto) {
        this.updateById(setmealDto);

        Long setmealId = setmealDto.getId();
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        setmealDishService.remove(lambdaQueryWrapper);

        List<SetmealDish> list = setmealDto.getSetmealDishes();
        list = list.stream().map((item)->{
            item.setSetmealId(setmealId);
            return item;

        }).collect(Collectors.toList());

        setmealDishService.saveBatch(list);
    }
}
