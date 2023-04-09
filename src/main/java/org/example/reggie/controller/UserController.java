package org.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.example.reggie.common.R;
import org.example.reggie.entity.User;
import org.example.reggie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequestMapping("/user")
@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private RedisTemplate redisTemplate;

  /*  @PostMapping("/sendMsg")
    public R<String> sengMsg(@RequestBody User user, HttpSession session) {
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("976483414@qq.com");
            message.setTo("976483414@qq.com");
            message.setSubject("验证码");
            message.setText("123456");
            String code = message.getText();
            session.setAttribute(phone, code);
            javaMailSender.send(message);
            log.info("发送成功");
            return R.success("验证码发送成功");
        }
        return R.error("发送失败");
    }*/
  @PostMapping("/sendMsg")
  public R<String> sengMsg(@RequestBody User user, HttpSession session) {
      String phone = user.getPhone();
      if (StringUtils.isNotEmpty(phone)) {
          SimpleMailMessage message = new SimpleMailMessage();
          message.setFrom("976483414@qq.com");
          message.setTo("976483414@qq.com");
          message.setSubject("验证码");
          message.setText("123456");
          String code = message.getText();
          //session.setAttribute(phone, code);
          redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
          javaMailSender.send(message);
          log.info("发送成功");
          return R.success("验证码发送成功");
      }
      return R.error("发送失败");
  }
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从Session中获取验证码
        Object codeInSession = session.getAttribute(phone);
        //验证码比对
        if (codeInSession != null && codeInSession.equals(code)) {
            //登录成功
            //当前手机号是否再用户表中，如果是新用户，自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                //新用户，自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }
}
