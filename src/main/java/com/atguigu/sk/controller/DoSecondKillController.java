package com.atguigu.sk.controller;

import com.atguigu.sk.utils.JedisPoolUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

@RestController //等价于  Controller+方法上的 ResponseBody
public class DoSecondKillController {
//    lua脚本  通过lua脚本解决争抢问题，实际上是redis 利用其单线程的特性，用任务队列的方式解决多任务并发问题。
    static String secKillScript = "local userid=KEYS[1];\r\n"
            + "local prodid=KEYS[2];\r\n"
            + "local qtkey='sk:'..prodid..\":qt\";\r\n"
            + "local usersKey='sk:'..prodid..\":usr\";\r\n"
            + "local userExists=redis.call(\"sismember\",usersKey,userid);\r\n"
            + "if tonumber(userExists)==1 then \r\n"
            + "   return 2;\r\n"
            + "end\r\n"
            + "local num= redis.call(\"get\" ,qtkey);\r\n"
            + "if tonumber(num)<=0 then \r\n"
            + "   return 0;\r\n"
            + "else \r\n"
            + "   redis.call(\"decr\",qtkey);\r\n"
            + "   redis.call(\"sadd\",usersKey,userid);\r\n"
            + "end\r\n"
            + "return 1";

    @PostMapping(value = "/sk/doSecondKill", produces = "text/html;charset=utf-8")
    public String LuaDoSecondKill(Integer id) {
        //随机生成用户
        Integer userId = (int) (Math.random() * 10000);
        JedisPool jedispool = JedisPoolUtil.getJedisPoolInstance();
        Jedis jedis = jedispool.getResource();
        //加载lua脚本
        String sha1 = jedis.scriptLoad(secKillScript);
        //将LUA脚本和LUA脚本需要的参数传给redis执行：keyCount：lua脚本需要的参数数量，params：参数列表
        Object obj = jedis.evalsha(sha1, 2, userId + "", id + "");
        // Long 强转为Integer会报错  ，Long和Integer没有父类和子类的关系
        int result = (int)((long)obj);
        //关闭连接
        JedisPoolUtil.release(jedispool, jedis);
        if(result==1){
            System.out.println("秒杀成功");
            return "ok";
        }else if(result==2){
            System.out.println("重复秒杀");
            return "重复秒杀";
        }else{
            System.out.println("库存不足");
            return "库存不足";
        }
    }

    public String doSecondKill(Integer id) {
        //随机生成用户
        Integer userId = (int)(10000*Math.random());
        //秒杀商品id
        Integer pid = id;
        //拼接商品库存的key和用户列表集合的key
        String qtKey = "sk:" + pid + ":qt";

        String userKey = "sk:" + pid + ":usr";
        Jedis jedis = new Jedis("192.168.15.128",6379);
        //判断该用户是否重复秒杀
        if (jedis.sismember(userKey, userId+"")) {
            System.err.println("重复秒杀" + userId);
            return "当前用户重复秒杀";
        }
        // 1.对库存进行watch监听，开启乐观锁
        jedis.watch(qtKey);
        String qtStr = jedis.get(qtKey);
        //判断库存是否为0
        if (StringUtils.isEmpty(qtStr)) {
            System.err.println("秒杀尚未开始");
            return "秒杀尚未开始";
        }
        Integer count = Integer.valueOf(qtStr);
        if (count < 1) {
            System.err.println("库存不足");
            return "库存不足";
        }
// ab测试
//        ab -n5000 -c100 -p postfile.txt -T application/x-www-form-urlencoded http://192.168.15.1:8080/secondKills/sk/doSecondKill
        //开启事务
        Transaction multi = jedis.multi();
        //减库存，并保存秒杀成功的用户
        multi.decr(qtKey);
        multi.sadd(userKey, userId + "");
        System.out.println("秒杀成功");
        multi.exec();
        jedis.close();
        return "ok";
    }
}
