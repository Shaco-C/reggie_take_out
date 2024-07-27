package com.watergun.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.reggie.entity.Dish;
import com.watergun.reggie.dto.DishDto;

public interface DishService extends IService<Dish> {
    public void saveWithFlavor(DishDto dishDto);

    public DishDto getByIdWithFlavor(Long id);

    public void updateWithFlavor(DishDto dishDto);
}
