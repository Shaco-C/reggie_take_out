package com.watergun.reggie.dto;

import com.watergun.reggie.entity.Setmeal;
import com.watergun.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
