package searcher;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
//表示一条搜索结果,根据DocInfo得到的
public class Result {

    private String title;

    //这两个场景中,这两个URL就填成一样的
    private String showUrl;
    private String clickUrl;

    //描述这个页面
    private String desc;
}
