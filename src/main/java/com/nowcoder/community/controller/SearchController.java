package com.nowcoder.community.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;

@Controller
public class SearchController implements CommunityConstant{
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //使用get方法传递参数：search?keyword=xxx
    @RequestMapping(path="/search",method = RequestMethod.GET)
    public String search(String keyword,Page page,Model model){

        //分页信息
        page.setPath("/search?keyword="+keyword);
        //搜索帖子
        //自己写的page从1开始，而方法中是从0开始，所以要减1
        SearchHits<DiscussPost> hits= elasticsearchService.searchDiscussPost(keyword, page.getCurrent()-1, page.getLimit());
        List<Map<String,Object>> discussPosts=new ArrayList<>();
        if(hits!=null){
            hits.get().forEach(hit->{
                Map<String,Object> map=new HashMap<>();
                //帖子
                DiscussPost post=hit.getContent();
                //用带有高亮标识符的标题替换原标题
                List<String> titleField= hit.getHighlightFields().get("title");
                if(titleField!=null){
                    post.setTitle(titleField.get(0));
                }
                //用带有高亮表示符的内容替换原内容
                List<String> contentField= hit.getHighlightFields().get("content");
                if(contentField!=null){
                    post.setContent(contentField.get(0));
                }

                map.put("post",post);
                //帖子作者
                map.put("user",userService.findUserById(post.getUserId()));
                //点赞数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                
                discussPosts.add(map);
            });
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        //分页信息
        page.setPath("/search?keyword="+keyword);
        page.setRows(hits==null?0:(int)hits.getTotalHits());
        return "/site/search";
    }
}
