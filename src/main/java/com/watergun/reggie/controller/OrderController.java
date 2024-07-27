package com.watergun.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.reggie.common.BaseContext;
import com.watergun.reggie.common.R;
import com.watergun.reggie.dto.OrdersDto;
import com.watergun.reggie.entity.OrderDetail;
import com.watergun.reggie.entity.Orders;
import com.watergun.reggie.entity.ShoppingCart;
import com.watergun.reggie.service.OrderDetailService;
import com.watergun.reggie.service.OrderService;
import com.watergun.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, Long number, String beginTime,String  endTime){
        log.info("page:{},pageSize:{},number:{},beginTime:{},endTime:{}",page,pageSize,number,beginTime,endTime);

        Page<Orders> ordersPage = new Page<>(page,pageSize);

        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(number!=null,Orders::getNumber,number)
                .gt(StringUtils.isNotEmpty(beginTime), Orders::getOrderTime,beginTime)
                .lt(StringUtils.isNotEmpty(endTime),Orders::getOrderTime,endTime);

        orderService.page(ordersPage,lambdaQueryWrapper);
        return R.success(ordersPage);

    }

    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize){
        log.info("page = {},pageSize = {}",page,pageSize);

        //构造分页构造器
        Page<Orders> pageInfo = new Page(page,pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getCheckoutTime);

        //执行查询
        orderService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> list = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(item,ordersDto);

            Long orderid = item.getId();//订单号

            //根据订单号查询订单详情
            //构造条件构造器
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            //添加过滤条件
            lambdaQueryWrapper.eq(OrderDetail::getOrderId, orderid);
            //执行查询
            List<OrderDetail> orderDetailList = orderDetailService.list(lambdaQueryWrapper);

            ordersDto.setOrderDetails(orderDetailList);
            ordersDto.setSumNum(orderDetailList.size());

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(list);

        return R.success(ordersDtoPage);
    }

    /**
     * 修改订单状态
     */
    @PutMapping
    public R<String> orderStatusChange(@RequestBody Map<String,String> map){
        orderService.changeStatus(map);
        return R.success("订单状态修改成功");

    }


    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){

        orderService.submit(orders);
        return R.success("成功");
    }

    /**
     * 再来一单
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders) {
        log.info("订单：{}",orders);

        //获取订单ID
        //SQL : select * from orders where id = ?
        Orders ordersData = orderService.getById(orders.getId());
        String id = ordersData.getNumber();

        //通过订单ID查询订单明细
        //SQL : select * from order_detail where order_id = ?
        LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OrderDetail::getOrderId,id);
        List<OrderDetail> orderDetailList = orderDetailService.list(lambdaQueryWrapper);
        log.info("该订单数据：{}",orderDetailList);

        //清空现在的购物车
        //SQL : delete * from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);

        //将订单明细里的数据添加到购物车
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item) -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setName(item.getName());
            shoppingCart.setImage(item.getImage());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setDishId(item.getDishId());
            shoppingCart.setSetmealId(item.getSetmealId());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("Success！");
    }

}
