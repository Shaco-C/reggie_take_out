package com.watergun.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.watergun.reggie.entity.Orders;
import org.springframework.stereotype.Service;

import java.util.Map;


public interface OrderService extends IService<Orders> {
    public void changeStatus(Map<String,String> map);

    public void submit(Orders orders);
}
