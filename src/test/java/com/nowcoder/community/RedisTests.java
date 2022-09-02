package com.nowcoder.community;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)//将配置类设置为和正式环境一样
public class RedisTests {
    
    @Autowired
    private RedisTemplate redisTemplate;

    //@Test
    public void testStrings(){
        String redisKey="test:count";

        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    //@Test
    public void testHashes(){
        String redisKey="test:user";

        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"username","jonathan");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    //@Test
    public void testLists(){
        String redisKey="test:ids";

        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    //@Test
    public void testSets(){
        String redisKey="test:teachers";

        redisTemplate.opsForSet().add(redisKey,"lily","sara","ben");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
        
    }

    //@Test
    public void testSortedSets(){
        String redisKey="test:students";

        redisTemplate.opsForZSet().add(redisKey, "chelsey", 100);
        redisTemplate.opsForZSet().add(redisKey, "kevin", 99);
        redisTemplate.opsForZSet().add(redisKey, "tiger", 95);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "chelsey"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "kevin"));
        System.out.println(redisTemplate.opsForZSet().range(redisKey, 0, 1));
    }

    //@Test
    public void testKeys(){
        redisTemplate.delete("test:user");

        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students",10,TimeUnit.SECONDS);
    }

    //多次访问同一个key
    //@Test
    public void testBoundOperations(){
        String redisKey="test:count";
        //绑定key(不只是key，还有别的方法可以绑定别的zset，list等)
        BoundValueOperations operations=redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    /* 
     * redis在处理的过程中不是执行一条提交一条，而是执行完之后统一提交
     * 所以在事务执行过程中无法查询，声明式事务在redis用的很少
     * 一般是用编程式事务
     */
    //编程式事务
    @Test
    public void testTransactional(){
        Object obj= redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey="test:tx";
                //启用事务
                operations.multi();

                operations.opsForSet().add(redisKey, "zhangsan");
                operations.opsForSet().add(redisKey, "lisi");
                operations.opsForSet().add(redisKey, "wangwu");

                //在事务内部进行一次查询，查到的一定是空的，只有事务结束之后才能查到结果
                System.out.println(operations.opsForSet().members(redisKey));
                
                return operations.exec();
            }
        });
        System.out.println(obj);
    }
}
