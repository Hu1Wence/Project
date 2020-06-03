//common包表示每个模块都可能用到的公共信息
package common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
//表示一个文档对象(HTML对象)
public class DocInfo {
    //docId 文档的唯一身份标识(不能重复)
    private int docId;

    //该文档的标题,同文件名表示
    private String title;

    //表示该文档对应的线上文档的url,根据本地路径构建出线上文档的url
    private String url;

    //该文档的正文,把html文件中的html标签去掉留下的内容
    private String content;
}
