package com.watergun.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.watergun.reggie.common.R;
import com.watergun.reggie.entity.Category;
import com.watergun.reggie.entity.Dish;
import com.watergun.reggie.dto.DishDto;
import com.watergun.reggie.entity.DishFlavor;
import com.watergun.reggie.service.CategoryService;
import com.watergun.reggie.service.DishFlavorService;
import com.watergun.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;


    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("新增成功");
    }

    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name != null, Dish::getName, name);
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, lambdaQueryWrapper);

        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> dishRecords = pageInfo.getRecords();
        List<DishDto> DishDtoRecords = dishRecords.stream().map((item) -> {

            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long CategoryId = item.getCategoryId();

            Category category = categoryService.getById(CategoryId);
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;

        }).collect(Collectors.toList());

        dishDtoPage.setRecords(DishDtoRecords);


        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);

        //删除所有缓存
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);

        //删除对应菜品分类的缓存
        //String key = "dish_" + dishDto.getCategoryId() + "_1";
        //redisTemplate.delete(key);
        return R.success("更新成功!");

    }

//    @DeleteMapping
//    public R<String> delete(String ids){
//        String[] split = ids.split(","); //将每个id分开
//        //每个id还是字符串，转成Long
//        List<Long> idList = Arrays.stream(split).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
//        dishService.removeByIds(idList);//执行批量删除
//        log.info("删除的ids: {}",ids);
//        return R.success("删除成功"); //返回成功
//    }

    @DeleteMapping
    public R<String> delete(String ids) {

        String[] split = ids.split(",");
        List<Long> id = Arrays.stream(split).map(s -> {
            return Long.parseLong(s.trim());
        }).collect(Collectors.toList());

        dishService.removeByIds(id);
        log.info("删除的ids: {}", ids);

        return R.success("删除成功");
    }

    @PostMapping("/status/{st}")
    public R<String> setStatus(@PathVariable int st, String ids) {
        String[] id = ids.split(",");
        List<Long> DishId = Arrays.stream(id).map(s -> {
            return Long.parseLong(s.trim());
        }).collect(Collectors.toList());

        List<Dish> dishes = DishId.stream().map(item -> {
            Dish dish1 = new Dish();
            dish1.setId(item);
            dish1.setStatus(st);
            return dish1;
        }).collect(Collectors.toList());

        dishService.updateBatchById(dishes);

        return R.success("修改成功");
    }

    //    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        lambdaQueryWrapper.eq(Dish::getStatus,1);
//        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> dishList = dishService.list(lambdaQueryWrapper);
//
//        return  R.success(dishList);
//
//    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;
        //动态构造key
        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //从redis中获取缓存数据
        dishDtoList= (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList!=null){
            //存在就返回，不需要查询数据库
            return R.success(dishDtoList);
        }
        //不存在就查询数据库，将内容存入redis中
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        lambdaQueryWrapper.eq(Dish::getStatus, 1);
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(lambdaQueryWrapper);

        dishDtoList = dishList.stream().map((item)->{
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,item.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);

            Long categoryId = item.getCategoryId();
            String categoryName = categoryService.getById(categoryId).getName();

            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            dishDto.setFlavors(dishFlavors);
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());
        //存入redis
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);

    }


}
