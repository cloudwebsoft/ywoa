package cn.js.fan.util;

import java.util.Random;
import java.util.Hashtable;
import com.cloudwebsoft.framework.util.ThreadUtil;
import com.cloudwebsoft.framework.console.ConsoleConfig;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class RandomSecquenceCreator {
    static Hashtable hash = new Hashtable();
    // 随机数字生成器
    static Random rand = new Random(System.currentTimeMillis());
    static long lastRandTime = System.currentTimeMillis();

    public RandomSecquenceCreator() {
    }

    public static Hashtable getHash() {
        return hash;
    }

    /**
     * 生成长度不限的随机数字序列号
     * @return String
     */
    public static String getId() {
        // 根据时间值，重置hash，否则hash会无限增大
        if (System.currentTimeMillis()-lastRandTime>20000) {
            hash.clear();
            lastRandTime = System.currentTimeMillis();
        }
        Integer id = new Integer(0);
        synchronized (hash) {
            // 生成一个唯一的随机数字
            id = new Integer(rand.nextInt());
            while (hash.containsKey(id)) {
                id = new Integer(rand.nextInt());
            }
            // 为当前用户保留该ID
            String data = "";
            if (ConsoleConfig.isDebug()) {
                data = ThreadUtil.getStackTraceString();
            }

            hash.put(id, data);
        }
        return System.currentTimeMillis() + "" + Math.abs(id.intValue());
    }

    /**
     * 生成长度在length之内的随机数字序列号
     * @param length int
     * @return String
     */
    public static String getId(int length) {
        // 如果有调度每隔5秒调度一次，如刷新在线用户列表，则会使得lastRandTime不断更新，而使得永远也不会clear
        // 根据时间值，重置hash，否则hash会无限增大 System.currentTimeMillis()有13位
        if (System.currentTimeMillis()-lastRandTime>20000) {
            hash.clear();
            lastRandTime = System.currentTimeMillis();
        }
        Integer id = new Integer(0);
        String strId = "";
        synchronized (hash) {
            // 生成一个唯一的随机数字
            id = new Integer(rand.nextInt());
            if (length > 15)
                strId = System.currentTimeMillis() + "" + Math.abs(id.intValue());
            else
                strId = "" + Math.abs(id.intValue());
            if (strId.length() > length)
                strId = strId.substring(0, length);
            while (hash.containsKey(strId)) {
                id = new Integer(rand.nextInt());
                if (length > 15)
                    strId = System.currentTimeMillis() + "" + Math.abs(id.intValue());
                else
                    strId = "" + Math.abs(id.intValue());
                if (strId.length() > length)
                    strId = strId.substring(0, length);
            }
            // 为当前用户保留该ID
            String data = "";
            if (ConsoleConfig.isDebug()) {
                data = ThreadUtil.getStackTraceString();
            }
            hash.put(strId, data);
        }
        return strId;
    }
}
