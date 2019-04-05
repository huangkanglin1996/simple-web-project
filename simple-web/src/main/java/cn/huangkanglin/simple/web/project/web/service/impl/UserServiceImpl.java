package cn.huangkanglin.simple.web.project.web.service.impl;

import cn.huangkanglin.simple.web.project.datasource.DataSource;
import cn.huangkanglin.simple.web.project.util.MyJDBCUtils;
import cn.huangkanglin.simple.web.project.web.entitys.User;
import cn.huangkanglin.simple.web.project.web.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description TODO
 * @Author hkl
 * @Date 2019/4/5 12:58
 **/
public class UserServiceImpl implements UserService {

    @Override
    public User login(User user) {
        // 1.创建sql
        String sql = "SELECT * FROM `user` WHERE userName = ? AND userPassword = ?";
        List<Object> queryParams = new ArrayList<>();
        queryParams.add(user.getUserName());
        queryParams.add(user.getUserPassword());

        // 2.执行sql
        List<User> users = MyJDBCUtils.getBeans(DataSource.getConnection(), sql, queryParams.toArray(), User.class);

        // 3.判断结果集
        if (users == null || users.isEmpty()) {
            return null;
        } else {
            return users.get(0);
        }
    }

    @Override
    public List<User> list() {
        // 1.创建sql
        String sql = "SELECT * FROM `user`";

        // 2.执行查询
        List<User> users = MyJDBCUtils.getBeans(DataSource.getConnection(), sql, User.class);

        return users;
    }
}
