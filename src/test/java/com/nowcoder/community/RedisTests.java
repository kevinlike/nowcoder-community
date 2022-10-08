package com.nowcoder.community;

import static org.mockito.Mockito.ignoreStubs;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisCommand;
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

        System.out.println("--------------------------------------");
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
    //@Test
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

    //HyperLogLog
    //统计20w个重复数据的独立总数
    //@Test
    public void testHyperLogLog(){
        String redisKey="test:hll:01";
        for (int i = 0; i <=100000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }
        for (int i = 0; i <=100000; i++) {
            int random=(int)(Math.random()*100000+1);
            redisTemplate.opsForHyperLogLog().add(redisKey,random);
        }

        long size=redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println("------------------------------------------");
        System.out.println(size);//大约会有0.81%的误差
    }

    /* 
     * HyperLogLog合并重复数据的性能很强
     * eg：原本的数据是每天登录的用户账号，现在要统计一周中有哪些账号登录过
     * 这其中就有大量的重复数据，在这样的任务上，hll的性能表现就很好
     */
    //将3组数据合并，再统计合并后的重复数据的独立总数
    //@Test
    public void testHyperLogLogUnion(){
        String redisKey2="test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }
        String redisKey3="test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }
        String redisKey4="test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }
        String unionKey="test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2,redisKey3,redisKey4);
        long size=redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println("------------------------------------------");
        System.out.println(size);

    }

    //BitMap
    //统计一组数据的bool值
    //@Test
    public void testBitMap(){
        String redisKey="test:bm:01";
        //记录,false的值不用存,默认为false
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        //查询
        System.out.println("------------------------------------------");
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));//f
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));//t
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 20));//f

        //统计，没有自带的统计方法，所以需要按下方式使用底层的方法
        Object obj= redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // TODO Auto-generated method stub
                //bitCount能统计对应redisKey.getBytes()中“1”的个数
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);//3

    }
    //统计三组数据的bool值，并对这3组数据做or运算
    //@Test
    public void testBitMapOperation(){
        String redisKey2="test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3="test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);
        redisTemplate.opsForValue().setBit(redisKey2, 3, true);
        redisTemplate.opsForValue().setBit(redisKey2, 4, true);

        String redisKey4="test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey2, 4, true);
        redisTemplate.opsForValue().setBit(redisKey2, 5, true);
        redisTemplate.opsForValue().setBit(redisKey2, 6, true);

        String redisKey="test:bm:or";
        Object obj=redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // TODO Auto-generated method stub
                //声明运算符,位运算，需要二进制数据，所以传入redisKey.getBytes()
                connection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(), redisKey2.getBytes(),redisKey3.getBytes(),redisKey4.getBytes());

                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);

    }
}
