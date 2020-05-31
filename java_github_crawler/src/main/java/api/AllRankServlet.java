package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.Project;
import dao.ProjectDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AllRankServlet extends HttpServlet {

    private Gson gson = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.准备工作
        resp.setContentType("application/json; charset=utf-8");
        //2.解析请求,获取日期参数
        String date = req.getParameter("date");
        if (date == null || date.equals("")) {
            resp.setStatus(404);
            resp.getWriter().write("date 参数错误");
            return;
        }
        //3.从数据库查找数据
        ProjectDao projectDao = new ProjectDao();
        List<Project> projects = projectDao.selectProjectByDate(date);
        String respString = gson.toJson(projects);
        resp.getWriter().write(respString);
        return;
    }
}
