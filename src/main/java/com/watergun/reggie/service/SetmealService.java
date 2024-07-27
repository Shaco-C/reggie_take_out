package com.watergun.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.reggie.dto.SetmealDto;
import com.watergun.reggie.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {

    public void saveWithDish(SetmealDto setmealDto);

    public void deleteWithDish(String ids);

    public void setStatus(int st,String ids);

    public SetmealDto getWithDishes(Long id);

    public void updateWithDishes(SetmealDto setmealDto);

}
