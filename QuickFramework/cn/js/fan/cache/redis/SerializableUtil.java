package cn.js.fan.cache.redis;

import org.apache.log4j.Logger;

import java.io.*;

/**
 * @Author: qcg
 * @Description:
 * @Date: 2019/1/10 16:20
 */
public class SerializableUtil {
	private static Logger logger = Logger.getLogger(SerializableUtil.class);

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
			logger.error(e.getMessage());
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
			Object obj = objectInputStream.readObject();
			return obj;
		} catch (IOException e) {
//			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
}
