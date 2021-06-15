package com.redmoon.clouddisk.tools;

import com.cloudwebsoft.framework.util.LogUtil;

/**
 * @author 古月圣
 * 
 */
public class ByteConvertUtil {

	public static final int SIZEOF_BYTE = 1;
	public static final int SIZEOF_SHORT = 2;
	public static final int SIZEOF_INT = 4;
	public static final int SIZEOF_LONG = 8;

	@SuppressWarnings("unused")
	/**
	 * 低字节在前，高字节在后的byte数组
	 * @param n
	 * @param length
	 * @return
	 */
	private static byte[] SILtoByteArray(long n, int length) {
		byte[] b = new byte[length];
		for (int i = 0; i < length; i++) {
			b[i] = (byte) (n >> (8 * i) & 0xff);
		}
		return b;
	}

	// 封包long算法
	/**
	 * 将long转为低字节在前，高字节在后的byte数组
	 */
	public static byte[] LongtoByteArray(long n) {
		byte[] b = new byte[SIZEOF_LONG];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
		b[2] = (byte) (n >> 16 & 0xff);
		b[3] = (byte) (n >> 24 & 0xff);
		b[4] = (byte) (n >> 32 & 0xff);
		b[5] = (byte) (n >> 40 & 0xff);
		b[6] = (byte) (n >> 48 & 0xff);
		b[7] = (byte) (n >> 56 & 0xff);
		return b;
		// return SILtoByteArray(n, SIZEOF_LONG);
	}

	// 封包int算法
	/**
	 * 将int转为低字节在前，高字节在后的byte数组
	 */
	public static byte[] InttoByteArray(int n) {
		byte[] b = new byte[SIZEOF_INT];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
		b[2] = (byte) (n >> 16 & 0xff);
		b[3] = (byte) (n >> 24 & 0xff);
		return b;
		// return SILtoByteArray(n, SIZEOF_INT);
	}

	// 封包short算法
	/**
	 * 将short转为低字节在前，高字节在后的byte数组(网络字节)
	 */
	public static byte[] ShorttoByteArray(short n) {
		byte[] b = new byte[SIZEOF_SHORT];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
		return b;
		// return SILtoByteArray(n, SIZEOF_SHORT);
	}

	// 解包为long算法
	/**
	 * @param b
	 * @return
	 */
	public static int ByteArraytoLong(byte[] b) {
		int iOutcome = 0;
		byte bLoop;
		for (int i = 0; i < SIZEOF_LONG; i++) {
			bLoop = b[i];
			iOutcome += (bLoop & 0xff) << (8 * i);
		}
		return iOutcome;
	}

	// 解包为int算法
	/**
	 * @param b
	 * @return
	 */
	public static int ByteArraytoInt(byte[] b) {
		int iOutcome = 0;
		byte bLoop;
		for (int i = 0; i < SIZEOF_INT; i++) {
			bLoop = b[i];
			iOutcome += (bLoop & 0xff) << (8 * i);
		}
		return iOutcome;
	}

	// 解包为short算法
	/**
	 * @param b
	 * @return
	 */
	public static short ByteArraytoShort(byte[] b) {
		short iOutcome = 0;
		byte bLoop;
		for (int i = 0; i < SIZEOF_SHORT; i++) {
			bLoop = b[i];
			iOutcome += (bLoop & 0xff) << (8 * i);
		}
		return iOutcome;
	}

	// 解包为String算法
	/**
	 * @param b
	 * @return
	 */
	public static String ByteArraytoString(byte[] b) {
		String retStr = "";
		try {
			// retStr = new String(b, "GBK");
			retStr = new String(b);
		} catch (Exception e) {
			LogUtil.getLog(e.getMessage());
		}
		return retStr.trim();
	}

	// Java中String字符串转换为ANSI字符数组[ 封包 ]
	/**
	 * @param str
	 * @return
	 */
	public byte[] StringtoByteArray(String str) {
		byte[] retBytes = null;
		try {
			// retBytes = str.getBytes("GBK");
			retBytes = str.getBytes();
		} catch (Exception e) {
			LogUtil.getLog(e.getMessage());
		}
		return retBytes;
	}
}
