package parser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//遍历文档目录,读取所有的 html 文档内容,把结果解析成行文本文件
//每一行都对应一个文档,每一行都包含文档标题,文档的url,文档的正文
//Parser 是一个单独的可执行类
public class Parser {
    //输入目录表示下载好的 JavaAPI文档
    private static final String INPUT_PATH = "/apache-tomcat-8.5.51/webapps/docsAPI/api";
    //输出目录表示预处理模块输出文件存放的目录
    private static final String OUTPUT_PATH = "/apache-tomcat-8.5.51/webapps/raw_data.txt";

    public static void main(String[] args) throws IOException {
        FileWriter resultFileWriter = new FileWriter(new File(OUTPUT_PATH));
        //通过main完成整个预处理的过程
        //1.枚举出 INPUT_PATH 中所有的 html 文件(递归)
        ArrayList<File> fileList = new ArrayList<>();
        enumFile(INPUT_PATH, fileList);
        //2.针对枚举出来的 html 文件路径进行遍历,依次打开每个文件,并读取内容
        //  把内容转换成需要的结构化的数据(DocInfo对象)
        for (File f : fileList) {
            System.out.println("converting " + f.getAbsolutePath() + "...");
            //最终输出的是 raw_data 文件是一个行文本文件.每一行对应一个 html 文件
            //line 这个对象就对应到一个文件
            String line = convertLine(f);
            //3.把得到的结果写入到最终的输出文件中(OUTPUT_PATH),写成行文本的形式
            resultFileWriter.write(line);
        }
        resultFileWriter.close();
    }

    private static String convertLine(File f) throws IOException {
        //1.根据f 转换出标题
        String title = convertTitle(f);
        //2.根据f 转换出url
        String url = convertUrl(f);
        //3.根据f 转换出正文
        String content = convertContent(f);
        //4.把这三个部分拼成一行文本
        //  \3是分割3个部分的作用 \3是不可见的
        return title + "\3" + url + "\3" + content + "\n";
    }

    private static String convertContent(File f) throws IOException {
        //这个方法做两件事情:
        //1.把html中的标签去掉
        //2.把 \n 去掉
        //一个一个字符读取并判定
        FileReader fileReader = new FileReader(f);
        boolean isContent = true;
        StringBuilder output = new StringBuilder();
        while (true) {
            int ret = fileReader.read();
            if (ret == -1) {
                //读取完毕
                break;
            }
            char c = (char) ret;
            if (isContent) {
                //当前这部分是正文
                if (c == '<') {
                    isContent = false;
                    continue;
                }
                if (c == '\n' || c == '\r') {
                    c = ' ';
                }
                output.append(c);
            } else {
                //当前是标签
                //不去写 output
                if (c == '>') {
                    isContent = true;
                }
            }
        }
        fileReader.close();
        return output.toString();
    }

    private static String convertUrl(File f) {
        //url由两部分组成
        //第一部分https://docs.oracle.com/javase/8/docs/api
        //第二部分 /java/util/Collection.html
        String part1 = "https://docs.oracle.com/javase/8/docs/api";
        String part2 = f.getAbsolutePath().substring(INPUT_PATH.length());
        return part1 + part2;
    }

    private static String convertTitle(File f) {
        //把文件名作为标题
        String name = f.getName();
        return name.substring(0, name.length() - ".html".length());
    }

    //当这个方法递归完毕后,当前 inputPath 目录下所有的 html 的文件就在fileList中了
    private static void enumFile(String inputPath, ArrayList<File> fileList) {
        //递归的把 INPUT_PATH 对应的全部目录和文件遍历一遍
        File root = new File(inputPath);
        //listFiles P相当于 linux 的 ls 命令
        //把当前目录下的所有文件都罗列出来
        File[] files = root.listFiles();
        //遍历当前这些目录和文件路径,分别处理
        for (File f : files) {
            if (f.isDirectory()) {
                //如果是目录就递归调用 enumFile
                enumFile(f.getAbsolutePath(), fileList);
            } else if (f.getAbsolutePath().endsWith(".html")) {
                //如果当前 f 不是目录, 看文件后缀是不是.html
                //如果是就加入到 fileList 中
                fileList.add(f);
            }
        }
    }

}
