package cn.huangkanglin.simple.web.project.web.service;

import cn.huangkanglin.simple.web.project.web.entitys.User;

import java.util.List;

public interface UserService {

    /**
     * 登录方法
     * @param user
     * @return
     */
    User login(User user);

    /**
     * 获取用户列表
     * @return
     */
    List<User> list();
}
