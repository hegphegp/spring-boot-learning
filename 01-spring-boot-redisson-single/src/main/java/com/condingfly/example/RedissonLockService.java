package com.condingfly.example;

import com.condingfly.utils.RedissonLockUtil;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author hgp
 * @date 20-7-16
 */
@Service
public class RedissonLockService {
    @Autowired
    private RedissonClient redissonClient;

    @PostConstruct
    public void test() {
        RedissonLockUtil.setRedissonClient(redissonClient);
        long goodsId = 10000;
        long userId = 111111;
        startRedissonDistributedLock(goodsId, userId);
    }

    public void startRedissonDistributedLock(Long goodsId, Long userId) {
        boolean res=false;
        try {
            // 释放锁的时间
            res = RedissonLockUtil.tryLock(goodsId+"", TimeUnit.SECONDS, 1, 5);
            if(res) {
                dbOperation(goodsId, userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            if(res) {//释放锁
                RedissonLockUtil.unlock(goodsId+"");
            }
        }
    }

    @Transactional
    public void dbOperation(long goodsId, long userId) {
        // 数据库操作
        System.out.println("==============================");
    }
}
