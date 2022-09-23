package com.nowcoder.community.service;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import com.nowcoder.community.dao.elasticsearch.DiscussRepository;
import com.nowcoder.community.entity.DiscussPost;

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussRepository discussRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    //向es服务器提交新产生的帖子
    public void saveDiscussPost(DiscussPost post){
        discussRepository.save(post);
    }

    //根据id删除帖子
    public void deleteDiscussPost(int id){
        discussRepository.deleteById(id);
    }

    //搜索方法
    /* 
     * keyword 搜索关键词
     * current 当前页码
     * limit 每页的内容数量
     */
    public SearchHits<DiscussPost> searchDiscussPost(String keyword,int current,int limit){
        NativeSearchQuery searchQuery=new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.multiMatchQuery(keyword, "title","content"))
        .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC))
        .withSorts(SortBuilders.fieldSort("score").order(SortOrder.DESC))
        .withPageable(PageRequest.of(current,limit))
        .withHighlightFields(
            new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
            new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
        ).build();
        SearchHits<DiscussPost> hits=elasticsearchRestTemplate.search(searchQuery,DiscussPost.class);
        List<DiscussPost> list=new ArrayList<>();
        hits.get().forEach(hit->{
            //System.out.println(hit.toString());
            System.out.println(hit.getHighlightFields().get("content")); //获取标注数据
            DiscussPost post=hit.getContent();
            list.add(post);
        });
        return hits;
    }
}
