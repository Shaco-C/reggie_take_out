package com.watergun.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.watergun.reggie.common.R;
import com.watergun.reggie.entity.User;
import com.watergun.reggie.service.UserService;
import com.watergun.reggie.utils.SMSUtils;
import com.watergun.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequestMapping("/user")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取用户手机号
        String phone = user.getPhone();
        if (phone != null){
            //生成随机的4位验证码,使用工具类中的代码生成器
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("生成的验证码为：{}",code);//便于测试

            //调用阿里云的短信服务API完成发送短信,按照api的格式去发送
            //可以不删除，日后若是购买了短信服务，可以按照这个格式去发送即可
            //SMSUtils.sendMessage("瑞吉外卖","templateCode",phone,code);

            //将生成的验证码保存到session中，方便之后登陆校验使用
            //session.setAttribute(phone,code);

            //将验证码保存到redis中，并设置有效期为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.success("短信发送成功...");
        }
        return R.error("短信发送失败...");
//        return R.success("ok");
    }


    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession httpSession){

        log.info("map:{}",map);


        //获取手机号
        String phone = map.get("phone").toString();


        //获取验证码

        String code = map.get("code").toString();

        //从Session中获取保存的验证码

        //String rightCode = (String) httpSession.getAttribute(phone);
        String rightCode = (String) redisTemplate.opsForValue().get(phone);
        //进行验证码比对

        if (/*StringUtils.isNotEmpty(rightCode)&&rightCode.equals(code)*/true){
            //比对成功登陆成功
            //判断该手机号是否为新用户，是的话自动注册
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(lambdaQueryWrapper);
            if (user==null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            httpSession.setAttribute("user",user.getId());

            //如果用户登陆成功，删除验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }

        return   R.error("登陆失败");
    }

}
