package cn.huangkanglin.simple.web.project.web.servlet.user;

import cn.huangkanglin.simple.web.project.web.entitys.User;
import cn.huangkanglin.simple.web.project.web.service.UserService;
import cn.huangkanglin.simple.web.project.web.service.impl.UserServiceImpl;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;

/**
 * @ClassName LoginServlet
 * @Description TODO
 * @Author hkl
 * @Date 2019/4/5 13:12
 **/
@WebServlet("/user/login")
public class LoginServlet extends HttpServlet {

    private UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1.获取参数
        String userName = request.getParameter("userName");
        String userPassword = request.getParameter("userPassword");

        // 2.用对象的思想传参数
        User user = new User();
        user.setUserName(userName);
        user.setUserPassword(userPassword);

        // 3.执行业务逻辑
        User currentUser = userService.login(user);

        // 4.currentUser如果存在就放入session并重定向到user.jsp;
        // currentUser如果不存在就重定向到index.jsp
        if(currentUser == null) {
            response.sendRedirect(request.getContextPath()+"/index.jsp");
        } else {
            request.getSession().setAttribute("current",currentUser);
            response.sendRedirect(request.getContextPath()+"/user/list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }
}
