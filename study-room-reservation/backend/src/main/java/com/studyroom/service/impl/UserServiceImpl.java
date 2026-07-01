package com.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.common.BusinessException;
import com.studyroom.common.Md5Util;
import com.studyroom.entity.User;
import com.studyroom.mapper.UserMapper;
import com.studyroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(String username, String password, HttpSession session) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名不存在");
        }

        // 验证密码
        String encryptedPwd = Md5Util.md5(password);
        if (!encryptedPwd.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 存入 Session（脱敏：不返回密码）
        user.setPassword(null);
        session.setAttribute("loginUser", user);
        return user;
    }

    @Override
    public void register(User user) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已被注册");
        }

        // 密码加密
        user.setPassword(Md5Util.md5(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());

        // 默认角色为学生
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("STUDENT");
        }

        userMapper.insert(user);
    }

    @Override
    public User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("loginUser");
    }
}
