package Crawler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dao.Project;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Crawler {
    private OkHttpClient okHttpClient = new OkHttpClient();
    private HashSet<String> urlBlackList = new HashSet<>();
    private Gson gson = new GsonBuilder().create();

    {
        urlBlackList.add("https://github.com/events");
        urlBlackList.add("https://github.community");
        urlBlackList.add("https://github.com/security");
        urlBlackList.add("https://github.com/about");
        urlBlackList.add("https://github.com/pricing");
        urlBlackList.add("https://github.com/contact");

    }

//    public static void main(String[] args) throws IOException {
//
//        Crawler crawler = new Crawler();
//        //1.获取入口页面
//        String url = "https://github.com/akullpp/awesome-java/blob/master/README.md";
//        //2.解析入口页面,获取项目列表
//        String html = crawler.getPage(url);
//
//        //解析项目
//        List<Project> projects = crawler.parseProjectList(html);
//        ProjectDao projectDao = new ProjectDao();
////        System.out.println(projects);
//        //3.遍历项目列表,调用github API 获取项目信息
////        for (Project project : projects) {
////            try {
////                String repoName = crawler.getRepoName(project.getUrl());
////                String jsonString = crawler.getRepoInfo(repoName);
//////            System.out.println(jsonString);
//////            System.out.println("=============");
////                //4.解析JSON数据
////                crawler.parseRepoInfo(jsonString, project);
////                // 5.保存到数据库中
////                projectDao.save(project);
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////        }
//        for (Project project : projects) {
//            try {
//                String repoName = crawler.getRepoName(project.getUrl());
//                String jsonString = crawler.getRepoInfo(repoName);
//
//                //4.解析JSON数据
//                crawler.parseRepoInfo(jsonString, project);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        for (Project project : projects) {
//            try {
//                // 5.保存到数据库中
//                projectDao.save(project);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//    }

    public String getPage(String url) throws IOException {
        //1.创建一个 OkHttpClient 对象
        okHttpClient = new OkHttpClient();

        //2.创建一个 Request 对象
        //builder 这个类是一个辅助创造 Request 对象的类
        // Builder 中提供的 url 方法能够设定当前请求的 url
        Request request = new Request.Builder().url(url).build();

        //3.创建一个call对象（负责进行一次网络访问操作）
        Call call = okHttpClient.newCall(request);

        //4.发送请求给服务器,获取到 response 对象;
        Response response = call.execute();

        //5.判定响应是否成功
        if (!response.isSuccessful()) {
            System.out.printf("请求失败");
            return null;
        }
        return response.body().string();

    }

    public List<Project> parseProjectList(String html) {
        ArrayList<Project> result = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByTag("li");
        for (Element li : elements) {
            Elements allLink = li.getElementsByTag("a");
            if (allLink.size() == 0) {
                //当前 li 标签没有包含 a 标签
                //就直接忽略
                continue;
            }
            Element link = allLink.get(0);

            String url = link.attr("href");
            if (!url.startsWith("https://github.com")) {
                continue;
            }
            if (urlBlackList.contains(url)) {
                continue;
            }

            Project project = new Project();
            project.setName(link.text());
            project.setUrl(link.attr("href"));
            project.setDescription(li.text());
            result.add(project);
        }
        return result;
    }

    public String getRepoInfo(String respName) throws IOException {
        //Hu1Wence aijiamin0715
        String userName = "Hu1Wence";
        String passWord = "aijiamin0715";
        //进行身份认证,把用户名密码加密之后得到一个字符串, 把这个字符串放到http header中.
        String credential = Credentials.basic(userName, passWord);

        String url = "https://api.github.com/repos/" + respName;

        Request request = new Request.Builder().url(url).header("Authorization", credential).build();
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();

        if (!response.isSuccessful()) {
            System.out.println("访问Github API失败! URL + " + url);
            return null;
        }
        return response.body().string();
    }

    //这个方法的功能,就是把项目的 url 提取出其中的仓库名字和作者名字
    public String getRepoName(String url) {
        int lastOne = url.lastIndexOf("/");
        int lastTwo = url.lastIndexOf("/", lastOne - 1);

        if (lastOne == -1 || lastTwo == -1) {
            System.out.println("当前url不是一个项目的url! url :" + url);
            return null;
        }
        return url.substring(lastTwo + 1);
    }

    //通过这个方法获取到仓库相关信息
    //第一个参数 jsonString 表示获取到的Github API的结果
    //第二个参数 表示将解析的数据存到 project 对象里
    public void parseRepoInfo(String jsonString, Project project) {
        Type type = new TypeToken<HashMap<String, Object>>() {
        }.getType();

        HashMap<String, Object> hashMap = gson.fromJson(jsonString, type);
        Double starCount = (Double)hashMap.get("stargazers_count");
        project.setStarCount(starCount.intValue());
        Double forkCount = (Double)hashMap.get("forks_count");
        project.setForkCount(forkCount.intValue());
        Double issuesCount = (Double) hashMap.get("open_issues_count");
        project.setOpenedIssueCount(issuesCount.intValue());

    }
}
