package cn.huangkanglin.simple.web.project.web.servlet.user;

import cn.huangkanglin.simple.web.project.web.entitys.User;
import cn.huangkanglin.simple.web.project.web.service.UserService;
import cn.huangkanglin.simple.web.project.web.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @ClassName UserList
 * @Description TODO
 * @Author hkl
 * @Date 2019/4/5 14:53
 **/
@WebServlet("/user/list")
public class UserList extends HttpServlet {

    private UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<User> users = userService.list();
        req.setAttribute("users", users);
        req.getRequestDispatcher("/user.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }
}
