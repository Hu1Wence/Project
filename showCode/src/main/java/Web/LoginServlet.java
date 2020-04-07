package Web;

import DBUtil.DBUtil;
import domain.User;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("utf-8");
        request.setCharacterEncoding("utf-8");

        //获取用户名和密码
        String name = request.getParameter("username");
        String pwd = request.getParameter("password");
        System.out.println(name + "|密码：" + pwd);

        //到数据库中查询有没有该用户
        QueryRunner qr = new QueryRunner(DBUtil.getDataSource());
        String sql = "select * from user where username=? and password=?";
        User u = null;
        try {
            u = qr.query(sql, new BeanHandler<>(User.class), name,pwd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=utf-8");
        //判断有没有值
        if (u != null) {
            response.getWriter().write("登录成功");
            //保存用户
            HttpSession session = request.getSession();
            session.setAttribute("user", u);
            //跳转到主页面
            response.setHeader("refresh", "3,url=http://120.27.192.235:8080/showCode/teacher.html");
        } else {
            response.getWriter().write("登录失败");
            //跳转回注册页面
            response.setHeader("refresh", "3,url=http://120.27.192.235:8080/showCode/login.html");
        }
    }
}
