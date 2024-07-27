package com.watergun.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.reggie.entity.Category;
import org.springframework.web.bind.annotation.RequestParam;

public interface CategoryService extends IService<Category> {
    public void remove(@RequestParam("ids") Long id);

}
