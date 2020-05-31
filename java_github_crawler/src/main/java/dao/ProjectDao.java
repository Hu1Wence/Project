package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

//负责针对project这个对象进行数据操作
public class ProjectDao {

    public void save(Project project) {

        //通过save方法吧project对象保存到数据库
        //1.获取数据库连接
        Connection connection = DBUtil.getConnection();

        //2.构造 PrepareStatement 对象;
        PreparedStatement statement = null;

        String sql = "insert into project_table values(?, ?, ?, ?, ?, ?, ?)";

        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, project.getName());
            statement.setString(2, project.getUrl());
            statement.setString(3, project.getDescription());
            statement.setInt(4, project.getStarCount());
            statement.setInt(5, project.getForkCount());
            statement.setInt(6, project.getOpenedIssueCount());

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            statement.setString(7, simpleDateFormat.format(System.currentTimeMillis()));

            //3.执行sql语句,完成数据库插入
            int ret = statement.executeUpdate();
            if (ret != 1) {
                System.out.println("当前数据库执行插入数据出错");
                return;
            }
            System.out.println("数据插入成功");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, null);
        }
    }

    public List<Project> selectProjectByDate(String date) {
        List<Project> projects = new ArrayList<>();
        //获取数据库连接
        Connection connection = DBUtil.getConnection();
        String sql = "select name,url,starCount,forkCount,openedIssueCount " +
                "from project_table where date = ? order by startCount desc";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1, date);
            //执行sql语句
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Project project = new Project();
                project.setName(resultSet.getString("name"));
                project.setUrl(resultSet.getString("url"));
                project.setStarCount(resultSet.getInt("starCount"));
                project.setForkCount(resultSet.getInt("forkCount"));
                project.setOpenedIssueCount(resultSet.getInt("openedIssueCount"));
                projects.add(project);

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }

        return projects;
    }


}
