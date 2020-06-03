package searcher;

import common.DocInfo;
import index.Index;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Searcher {

    private Index index = new Index();

    public Searcher() throws IOException {
        index.build("/apache-tomcat-8.5.51/webapps/raw_data.txt");
    }

    public List<Result> search(String query) {
        //1.分词:针对输入的查询词进行分词.
        List<Term> terms = ToAnalysis.parse(query).getTerms();
        //2.触发:遍历分词结果,去索引中找到所有和这个分词结果相关的记录(一大堆 docld)
        ArrayList<Index.Weight> allTokenResult = new ArrayList<>();
        for (Term term : terms) {
            String word = term.getName();
            List<Index.Weight> invertedList = index.getInverted(word);
            if (invertedList == null) {
                //如果没查到跳过
                continue;
            }
            allTokenResult.addAll(invertedList);
        }
        //3.排序:针对相关性高低，进行降序排序
        allTokenResult.sort(new Comparator<Index.Weight>() {
            @Override
            public int compare(Index.Weight o1, Index.Weight o2) {
                return o2.weight - o1.weight;
            }
        });
        //4.包装结果:把刚才的这些docld所对应的DocInfo信息查找到,组装成一个响应数据
        ArrayList<Result> results = new ArrayList<>();
        for (Index.Weight weight : allTokenResult) {
            //根据 weight 中包含的 docId 找到对应的 DocInfo 对象
            DocInfo docInfo = index.getDocInfo(weight.docId);
            Result result = new Result();
            result.setTitle(docInfo.getTitle());
            result.setShowUrl(docInfo.getUrl());
            result.setClickUrl(docInfo.getUrl());
            //GenDesc 是从我们正文中摘取一段信息
            result.setDesc(GenDesc(docInfo.getContent(), weight.word));
            results.add(result);
        }
        return results;
    }

    private String GenDesc(String content, String word) {
        //查找 word 在 contend 中出现的位置
        int firstPos = content.toLowerCase().indexOf(word);
        if (firstPos == -1) {
            //只在标题中出现,没有在正文中出现
            return "";
        }
        //从 firstPos 开始往前开始找 60 个字符作为开始,如果不足60个就从正文头部开始
        int descBeg = firstPos < 60 ? 0 : firstPos - 60;
        //从 firstPos 开始往后找 160 个字符
        if (descBeg + 160 > content.length()) {
            return content.substring(descBeg);
        }
        return content.substring(descBeg, descBeg + 160) + "...";
    }
}
