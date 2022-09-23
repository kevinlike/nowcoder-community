package com.nowcoder.community.dao.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.nowcoder.community.entity.DiscussPost;

@Repository
public interface DiscussRepository extends ElasticsearchRepository<DiscussPost,Integer>{

}
