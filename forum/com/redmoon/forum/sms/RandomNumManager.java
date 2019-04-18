package com.redmoon.forum.sms;

import java.util.Random;
import java.util.Hashtable;
import cn.js.fan.util.StrUtil;
import org.apache.log4j.Logger;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RandomNumManager {
    static Logger logger = Logger.getLogger(RandomNumManager.class.getName());

    static Hashtable hash = new Hashtable();
    // 随机数字生成器
    static Random rand = new Random(System.currentTimeMillis());
    static long lastRandTime = System.currentTimeMillis();

    public RandomNumManager() {
    }

    /**
     * 生成唯一数字ID
     * @return 返回随机生成的数字，长度为len
     */
    public static String getRandNumStr(int len) {
        // 根据时间值，重置hash，否则hash会无限增大
        if (System.currentTimeMillis()-lastRandTime>20000)
            hash.clear();
        Integer id = new Integer(0);
        synchronized (hash) {
            // 生成一个唯一的随机数字
            id = new Integer(rand.nextInt());
            while (hash.containsKey(id)) {
                id = new Integer(rand.nextInt());
            }
            if (id.intValue()<0)
                id = new Integer(-id.intValue());
            // 为当前用户保留该ID
            String data = "";
            hash.put(id, data);
        }
        lastRandTime = System.currentTimeMillis();
        String str = "" + id.intValue();
        if (str.length()>=len)
            return str.substring(0, len);
        else
            return StrUtil.PadString(str, '0', len, true);
    }


}
