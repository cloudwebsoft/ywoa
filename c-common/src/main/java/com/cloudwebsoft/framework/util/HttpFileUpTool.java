package com.cloudwebsoft.framework.util;

import cn.js.fan.util.file.FileUtil;
import com.alibaba.fastjson.JSONObject;
import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;

public class HttpFileUpTool {

	private final static String TAG = "file upload";

	/**
	 * 绕过验证
	 *
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		// SSLContext sc = SSLContext.getInstance("SSLv3");
		SSLContext sc = SSLContext.getInstance("TLS");

		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(
					X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(
					X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

	/**
	 * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
	 *
	 * @param actionUrl
	 * @param params
	 * @param files
	 * @return
	 * @throws IOException
	 */
	public static String postUploadFile(String actionUrl,
                                        Map<String, ArrayList<String>> params, Map<String, File> files, boolean isSSL) {
		// StringBuilder sb2 = null;
		StringBuffer sb2 = null;
		String BOUNDARY = java.util.UUID.randomUUID().toString();
		String PREFIX = "--", LINEND = "\r\n";
		String MULTIPART_FROM_DATA = "multipart/form-data";
		String CHARSET = "UTF-8";
		DataOutputStream outStream = null;
		HttpURLConnection conn = null;
		int res = 200;
		try {
			LogUtil.getLog(HttpFileUpTool.class).info(TAG + "actionUrl:" + actionUrl);

			if (isSSL) {
				//采用绕过验证的方式处理https请求
				SSLContext sslcontext = null;
				try {
					sslcontext = createIgnoreVerifySSL();
				} catch (NoSuchAlgorithmException e) {
					LogUtil.getLog(HttpFileUpTool.class).error(e);
				} catch (KeyManagementException e) {
					LogUtil.getLog(HttpFileUpTool.class).error(e);
				}

				HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
					@Override
					public boolean verify(String s, SSLSession sslsession) {
						LogUtil.getLog(HttpFileUpTool.class).warn("Hostname is not matched for cert.");
						return true;
					}
				};
				HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
				HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
			}

			URL uri = new URL(actionUrl);
			conn = (HttpURLConnection) uri.openConnection();
			conn.setReadTimeout(20 * 60 * 1000); // 缓存的最长时间
			conn.setDoInput(true);// 允许输入
			conn.setDoOutput(true);// 允许输出
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST");
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
					+ ";boundary=" + BOUNDARY);

			// 首先组拼文本类型的参数
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, ArrayList<String>> entry : params.entrySet()) {
				ArrayList<String> values = entry.getValue();
				String key = entry.getKey();
				for (String value : values) {
					LogUtil.getLog(HttpFileUpTool.class).info(TAG + "参数名:" + key + " 参数值:" + value);
					sb.append(PREFIX);
					sb.append(BOUNDARY);
					sb.append(LINEND);
					sb.append("Content-Disposition: form-data; name=\"" + key
							+ "\"" + LINEND);
					// sb.append("Content-Type: text/plain; charset=" + CHARSET
					//		+ LINEND);
					// sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
					sb.append(LINEND);
					sb.append(value);
					sb.append(LINEND);
				}
			}
			LogUtil.getLog(HttpFileUpTool.class).info(TAG + "post content:" + sb);
			outStream = new DataOutputStream(conn.getOutputStream());
			outStream.write(sb.toString().getBytes());
			InputStream in = null;

			// 发送文件数据
			if (files != null) {
				for (Map.Entry<String, File> file : files.entrySet()) {
					if (!file.getValue().exists()) {
						LogUtil.getLog(HttpFileUpTool.class).warn(file.getValue().getAbsoluteFile() + " 不存在！");
						continue;
					}
					LogUtil.getLog(HttpFileUpTool.class).info(TAG + "文件名 :" + file.getKey());
					LogUtil.getLog(HttpFileUpTool.class).info(TAG + "文件路径:" + file.getValue());
					StringBuilder sb1 = new StringBuilder();
					sb1.append(PREFIX);
					sb1.append(BOUNDARY);
					sb1.append(LINEND);
					sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""
							+ file.getKey() + "\"" + LINEND);
					sb1.append("Content-Type: application/octet-stream; charset="
							+ CHARSET + LINEND);
					sb1.append(LINEND);
					outStream.write(sb1.toString().getBytes());

					InputStream is;
					try {
						is = new FileInputStream(file.getValue());
					}
					catch(FileNotFoundException e) {
						LogUtil.getLog(HttpFileUpTool.class).error(e);
						continue;
					}
					byte[] buffer = new byte[1024];
					int len;
					while ((len = is.read(buffer)) != -1) {
						outStream.write(buffer, 0, len);
					}

					is.close();
					outStream.write(LINEND.getBytes());
				}
			}
			// 请求结束标志
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
			outStream.write(end_data);
			outStream.flush();
			// 得到响应码
			res = conn.getResponseCode();
			LogUtil.getLog(HttpFileUpTool.class).info(TAG + "code:" + res);
			if (res == 200) {
				in = conn.getInputStream();

				BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
				sb2 = new StringBuffer();

				/*String data = "";
				while ((data = br.readLine()) != null) {
					sb2.append(data + "\n");
				}*/
				/**
				 * 上面那段接收代码，可能会出现EOF错误
				 * java.io.IOException: Premature EOF
				 * 原因分析：因为您正在逐行读取内容，而对于最后一行，文件可能缺少返回值以表示行结束，即http请求访问的第三方接口没有发送http协议需要的结束行。
				 */
				int BUFFER_SIZE = 1024;
				char[] buffer = new char[BUFFER_SIZE]; // or some other size,
				int charsRead = 0;
				while ((charsRead = br.read(buffer, 0, BUFFER_SIZE)) != -1) {
					sb2.append(buffer, 0, charsRead);
				}
			}
			else {
				in = conn.getInputStream();

				BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
				sb2 = new StringBuffer();
				/*String data = "";
				while ((data = br.readLine()) != null) {
					sb2.append(data + "\n");
				}*/
				/**
				 * 上面那段接收代码，可能会出现EOF错误
				 * java.io.IOException: Premature EOF
				 * 原因分析：因为您正在逐行读取内容，而对于最后一行，文件可能缺少返回值以表示行结束，即http请求访问的第三方接口没有发送http协议需要的结束行。
				 */
				int BUFFER_SIZE = 1024;
				char[] buffer = new char[BUFFER_SIZE]; // or some other size,
				int charsRead = 0;
				while ((charsRead = br.read(buffer, 0, BUFFER_SIZE)) != -1) {
					sb2.append(buffer, 0, charsRead);
				}

				LogUtil.getLog(HttpFileUpTool.class).info(TAG + "返回结果：" + sb2.toString());

				// return null;
			}
		} catch (Exception e) {
			try {
				if (outStream!=null) {
					outStream.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (conn!=null) {
				conn.disconnect();
			}

			LogUtil.getLog(HttpFileUpTool.class).info(TAG + "IOException:" + e.getMessage());
			LogUtil.getLog(HttpFileUpTool.class ).error(e);
		}
		if (sb2 == null) {
			JSONObject json = new JSONObject();
			json.put("code", res);
			json.put("msg", "Response error.");
			return json.toString();
		}
		LogUtil.getLog(HttpFileUpTool.class).info(TAG + "返回结果：" + sb2.toString());
		return sb2.toString();
	}

	static class MyX509TrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
}
