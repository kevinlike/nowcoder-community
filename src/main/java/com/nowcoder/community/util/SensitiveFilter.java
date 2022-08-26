package com.nowcoder.community.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;



@Component
public class SensitiveFilter {


    private static final Logger logger=LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT="***";

    //根节点
    private TrieNode rootNode=new TrieNode();

    @PostConstruct//容器调用此方法实例化后就调用此方法
    public void init(){
        //编译完成后所有文件都在target的classes下，访问路径也是相对于classes的
        try(
            InputStream is= this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            //转为缓冲流效率高
            BufferedReader reader=new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while((keyword=reader.readLine())!=null){
                this.addKeyword(keyword);
            }
        }catch(IOException e){
            logger.error("加载敏感词文件失败："+e.getMessage());
        }
        
    }

    //将一个敏感词添加到前缀树中
    private void addKeyword(String keyword){
        TrieNode temNode=rootNode;
        for(int i=0;i<keyword.length();i++){
            char c=keyword.charAt(i);
            TrieNode subNode=temNode.getSubNode(c);
            //若当前字符不在子节点中，则新建一个子节点
            if(subNode==null){
                subNode=new TrieNode();
                temNode.addSubNode(c, subNode);
            }
            //指向下一个节点，开始下一轮循环
            temNode=subNode;
            //若到达最后一个字符，则打上结束标志
            if(i==keyword.length()-1){
                temNode.setKeywordEnd(true);
            }
        }
    }


    /* 
     * 过滤敏感词
     * 
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        //指针1
        TrieNode tempNode=rootNode;
        //指针2
        int begin=0;
        //指针3
        int position=0;
        //结果
        StringBuilder sb=new StringBuilder();

        while(position<text.length()){
            char c=text.charAt(position);

            //跳过符号
            if(isSymbol(c)){
                //若指针指向根节点，将此符号计入结果，让指针2向下走一步
                if(tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头还是中间，指针3都向下走一步
                position++;
                continue;
            }
            tempNode=tempNode.getSubNode(c);
            if(tempNode==null)
            {
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                position=++begin;
                //重新指向根节点
                tempNode=rootNode;
            }
            else if(tempNode.isKeywordEnd()){
                //发现敏感词，将begin～position替换
                sb.append(REPLACEMENT);
                begin=++position;
                tempNode=rootNode;

            }
            else{
                position++;
            }
        }
        //将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    //判断是否为符号
    private boolean isSymbol(Character c){
        //0x2E80~0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }

    //前缀树
    private class TrieNode{
        //关键词结束标志
        private boolean isKeywordEnd=false;

        //子节点(key是下级字符，value是下级节点)
        private Map<Character,TrieNode> subNodes=new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean isKeywordEnd) {
            this.isKeywordEnd = isKeywordEnd;
        }

        //添加子节点的方法
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
        
    }
}
