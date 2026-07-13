package com.studyroom.service;

import com.studyroom.entity.User;

import javax.servlet.http.HttpSession;

public interface UserService {
    User login(String username, String password, HttpSession session);
    void register(User user);
    User getCurrentUser(HttpSession session);
}
