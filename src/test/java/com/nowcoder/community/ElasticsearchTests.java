package com.nowcoder.community;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.elasticsearch.index.query.QueryBuilders;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussRepository;
import com.nowcoder.community.entity.DiscussPost;



@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//将配置类设置为和正式环境一样
public class ElasticsearchTests {

    //数据需要从Mysql中取
    @Autowired
    private DiscussPostMapper discussPostMapper;

    //数据需要存入es
    @Autowired
    private DiscussRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    // @Autowired
    // private ElasticsearchTemplate elasticsearchTemplate;

    //@Test//向es中添加数据
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    //@Test//向es中添加一组数据
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(104,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(105,0,100,0));
        
    }

    //@Test//修改数据
    public void testUpdate(){
        DiscussPost post=discussPostMapper.selectDiscussPostById(231);
        post.setContent("使劲灌水啦");
        discussPostRepository.save(post);
    }

    //@Test//删除数据
    public void testDelete(){
        //删除一条数据
        discussPostRepository.deleteById(231);
        //删除索引中所有数据
        discussPostRepository.deleteAll();
    }



    //@Test
    public void testFindAll(){

        Iterable<DiscussPost> articles = discussPostRepository.findAll();
        articles.forEach(a-> System.out.println(a));
    }

    //@Test
    public void testFind(){

        Optional<DiscussPost> a= discussPostRepository.findById(109);
        System.out.println(a);
    }

    @Test
    public void testSearchByTemplate(){
        NativeSearchQuery searchQuery=new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("乐观", "title","content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                    new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                    new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        org.springframework.data.elasticsearch.core.SearchHits<DiscussPost> hits=elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        System.out.println("-----------------------------------");
        List<DiscussPost> list=new ArrayList<>();
        hits.get().forEach(hit->{
            //System.out.println(hit.toString());
            System.out.println(hit.getHighlightFields().get("content")); //获取标注数据
            DiscussPost post=hit.getContent();
            list.add(post);
        });
        System.out.println("-----------------------------------");
        for(DiscussPost post:list){
            System.out.println(post.toString());
        }
    }
}
