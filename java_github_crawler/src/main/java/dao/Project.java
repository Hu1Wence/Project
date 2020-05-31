package dao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Project {
    //项目名称,对应 a 标签中的内容
    private String name;

    //项目主页链接,对应 a 标签中的 href 属性
    private String url;

    //项目的描述信息,对应到 li 标签里面的内容
    private String description;

    //以下属性都是要统计到的数据
    //需要根据该项目的 url 进入到对应页面,进而统计数据
    private int starCount;
    private int forkCount;
    private int openedIssueCount;
}
