package com.atguigu.sk.test;

import org.junit.Test;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.security.Key;
import java.util.HashSet;
import java.util.Set;

public class RedisTest {
    /**
     * JedisConnectionException: java.net.SocketTimeoutException:
     *      错误排查：
     *             1.1  检查reids是否启动
     *                  xhsell中： ps -aux|grep redis
     *             1.2 检查虚拟机防火墙是否关闭
     *             1.3 redis保护模式必须关闭
     *                  vim  /myredis/redis.conf
     *                     69行 ， 注释bind 127.0.0.1
     *                      88行，protected-mode yes改为no
     *                      重启redis服务
     */
    @Test
    public void test() {
        //java代码连接redis步骤
        //1、获取连接
        Jedis jedis = new Jedis("192.168.15.128", 6379);
        //2、使用连接对象发送命名操作redis
        String ping = jedis.ping();
        System.out.println("ping = " + ping);
        //3、关闭连接
        jedis.close();
    }

    @Test
    public void test2() {
        //使用redis集群获取redis对象
        Set<HostAndPort> set = new HashSet<>();
        set.add(new HostAndPort("192.168.15.128", 6379));
        set.add(new HostAndPort("192.168.15.128", 6380));
        JedisCluster jedisCluster = new JedisCluster(set);
        jedisCluster.set("cluster", "Key");
        System.out.println(jedisCluster.get("cluster"));
    }
}
