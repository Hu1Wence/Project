package Crawler;

import dao.Project;
import dao.ProjectDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadCrawler extends Crawler{

    public static void main(String[] args) throws IOException {

        //使用多线程的方式重新组织核心逻辑,访问Github API 并行式访问
        ThreadCrawler crawler = new ThreadCrawler();
        //1.获取页面入口
        String url = "https://github.com/akullpp/awesome-java/blob/master/README.md";

        String html = crawler.getPage(url);

        //2.解析入口页面,获取项目列表
        List<Project> projects = crawler.parseProjectList(html);

        //3.遍历项目列表,使用线程池
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        //ExecutorService有两种提交任务的操作
        //execute:不关注任务提交的结果
        //submit:管住任务提交的结果
        //使用 submit 最主要的目的就是为了能知道任务啥时候全部完成
        List<Future<?>> taskResults = new ArrayList<>();

        for (Project project : projects) {
            Future<?> taskResult = executorService.submit(new CrawlerTask(project, crawler));
            taskResults.add(taskResult);
        }
        //等待所有线程池中的任务执行结束,在进行下一步操作

        for (Future<?> taskResult : taskResults) {
            //调用 get 方法就会阻塞,阻塞到该任务完毕,get才会返回
            try {
                taskResult.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        //代码执行到这一步,说明所有任务都执行完毕了,销毁线程池.
        executorService.shutdown();

        //4.保存到数据库中
        ProjectDao projectDao = new ProjectDao();
        for (Project project : projects) {
            projectDao.save(project);
        }
    }


    static class CrawlerTask implements Runnable {
        private Project project;
        private ThreadCrawler threadCrawler;

        public CrawlerTask(Project project, ThreadCrawler threadCrawler) {
            this.project = project;
            this.threadCrawler = threadCrawler;
        }

        @Override
        public void run() {
            try {
                String repoName = threadCrawler.getRepoName(project.getUrl());
                String jsonString = threadCrawler.getRepoInfo(repoName);

                threadCrawler.parseRepoInfo(jsonString, project);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
