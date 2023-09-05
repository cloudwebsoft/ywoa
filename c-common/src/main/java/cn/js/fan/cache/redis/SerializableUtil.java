package cn.js.fan.cache.redis;

import com.cloudwebsoft.framework.util.LogUtil;

import java.io.*;

/**
 * @Author: qcg
 * @Description:
 * @Date: 2019/1/10 16:20
 */
public class SerializableUtil {

	/**
	 * 序列化对象
	 * @param obj
	 * @return
	 */
	public static byte[] serializableObj(Object obj){
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(obj);
			objectOutputStream.flush();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			LogUtil.getLog(SerializableUtil.class).error(e.getMessage());
		}
		return null;
	}

	/**
	 *  反序列化对象
	 * @param bytes
	 * @return
	 */
	public static Object unserializableObj(byte[] bytes){
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			return objectInputStream.readObject();
		} catch (IOException e) {
			LogUtil.getLog(SerializableUtil.class).error(e);
		} catch (ClassNotFoundException e) {
			LogUtil.getLog(SerializableUtil.class).error(e.getMessage());
		}
		return null;
	}
}
