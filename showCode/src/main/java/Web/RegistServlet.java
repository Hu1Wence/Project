package Web;

import DBUtil.DBUtil;
import domain.User;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.QueryRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


//注册模块
@WebServlet("/RegistServlet")
public class RegistServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("RegistServlet");
        /**
         * 接收所有参数
         * 把参数封装成user对象
         * 设置uid
         * 写入到数据库
         * */
        //解决乱码问题
        response.setCharacterEncoding("utf-8");
        request.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");

        //接收所有参数
        Map<String, String[]> parameterMap = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            System.out.println("---------");
            System.out.println();
            System.out.println(entry.getKey()+":"+Arrays.toString(entry.getValue()));
        }

        User u = new User();

        //把接收的参数封装成user对象
        try {
            BeanUtils.populate(u, parameterMap);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        System.out.println(u);

        //设置uid
        u.setId(UUID.randomUUID().toString());

        //写入数据库
        QueryRunner qr = new QueryRunner(DBUtil.getDataSource());
        String sql = "insert into user value(?,?,?)";

        try {
            qr.update(sql,u.getId(),u.getUsername(), u.getPassword());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //跳转到登录页面
        response.getWriter().write("注册成功");
        response.setHeader("refresh", "3,url=http://120.27.192.235:8080/showCode/login.html");
    }

}
