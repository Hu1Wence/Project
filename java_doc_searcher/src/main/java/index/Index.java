package index;

import common.DocInfo;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Index {
    //word 这个词在 docId 文档中对应的权重是多少
    static public class Weight {
        public String word;
        public int docId;
        //weight 生成公式: weight = 标题中出现的次数 * 10 + 正文中出现的次数
        public int weight;
    }


    //索引类需要包含两方面内容,一个是正排索引,一个是倒排索引
    //正排索引是 docId -> DocInfo 直接把 docId 作为数组下标就行了
    private ArrayList<DocInfo> forwardIndex = new ArrayList<>();

    //倒排索引 根据 词 -> 一组 docId
    //不光能得到每个词在哪些文档中出现过,还想知道这个词在该文档中的权重是多少
    private HashMap<String, ArrayList<Weight>> invertedIndex = new HashMap<>();


    //Index 类提供的方法
    //查正派
    public DocInfo getDocInfo(int docId) {
        return forwardIndex.get(docId);
    }

    //查倒排
    public ArrayList<Weight> getInverted(String term) {
        return invertedIndex.get(term);
    }

    //构建索引,把row_data.txt 文件读取出来,加载到数据结构中
    public void build(String inputPath) throws IOException {
        System.out.println("build start");

        //1.打开文件按行读取
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(inputPath)));
        //2.读取到每一行就是文档,按照 \3 来切分
        while (true) {
            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            }
            //3.构建正排的过程:按照\3来切分,切分结果构造成一个DocInfo对象,并加入到正排索引中
            DocInfo docInfo = buildForward(line);

            //4.构造倒排的过程:把DocInfo对象里面的内容进一步处理,构造倒排
            buildInverted(docInfo);
//            System.out.println("Build " + docInfo.getTitle() + "done!");
        }

        bufferedReader.close();

        System.out.println("build finish");
    }

    private void buildInverted(DocInfo docInfo) {
        class  WordCnt {
            public int titleCount;
            public int contentCount;

            public WordCnt(int titleCount, int contentCount) {
                this.titleCount = titleCount;
                this.contentCount = contentCount;
            }
        }
        HashMap<String, WordCnt> wordCntHashMap = new HashMap<>();

        //针对DocInfo中的 title 和 content进行分词,在建立weight对象,构建倒排
        //1.先针对标题分词
        List<Term> titleTerms = ToAnalysis.parse(docInfo.getTitle()).getTerms();
        //2.遍历分词结果,统计标题中的每个词的出现次数
        for (Term term : titleTerms) {
            //此处word已经是小写了
            String word = term.getName();
            WordCnt wordCnt = wordCntHashMap.get(word);
            if (wordCnt == null) {
                //说明在哈希表中不存在
                wordCntHashMap.put(word, new WordCnt(1, 0));
            } else {
                //当前这个词已经存在了,修改WordCnt就可以了
                wordCnt.titleCount++;
            }
        }
        //3.在针对正文分词
        List<Term> contentTerms = ToAnalysis.parse(docInfo.getContent()).getTerms();
        //4.遍历分词结果,统计正文中的每个词的出现次数
        for (Term term : contentTerms) {
            //此处word已经是小写了
            String word = term.getName();
            WordCnt wordCnt = wordCntHashMap.get(word);
            if (wordCnt == null) {
                //说明在哈希表中不存在
                wordCntHashMap.put(word, new WordCnt(0, 0));
            } else {
                //当前这个词已经存在了,修改WordCnt就可以了
                wordCnt.contentCount++;
            }
        }

        //5.遍历hashmap,依次构建 weight 对象并更新倒排索引的映射关系
        for (Map.Entry<String, WordCnt> entry : wordCntHashMap.entrySet()) {
            Weight weight = new Weight();
            weight.word = entry.getKey();
            weight.docId = docInfo.getDocId();
            weight.weight = entry.getValue().titleCount* 10 + entry.getValue().contentCount;
            //weight 加入到倒排索引中
            //现根据这个词,找到 HashMap中对应的 Arraylist
            ArrayList<Weight> invertedList = invertedIndex.get(entry.getKey());
            if (invertedList == null) {
                invertedList = new ArrayList<>();
                invertedList.add(weight);
                invertedIndex.put(entry.getKey(), invertedList);
            }
            // 可以加入weight了
            invertedList.add(weight);
        }
    }

    private DocInfo buildForward(String line) {
        //把这一行按照 \3 切分
        //分出来的三个部分就是标题,url,正文
        String[] tokens = line.split("\3");
        if (tokens.length != 3) {
            //发现文件格式有问题
            System.out.println("文件格式存在问题" + line);
            return null;
        }
        DocInfo docInfo = new DocInfo();
        //id 就是正排索引下标
        docInfo.setDocId(forwardIndex.size());
        docInfo.setTitle(tokens[0]);
        docInfo.setUrl(tokens[1]);
        docInfo.setContent(tokens[2]);
        forwardIndex.add(docInfo);
        return docInfo;
    }


}
