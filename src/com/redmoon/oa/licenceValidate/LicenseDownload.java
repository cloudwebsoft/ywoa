package com.redmoon.oa.licenceValidate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.web.Global;


/**
 * @Description: 证书下载
 * @author: lichao	
 * @Date: 2015-8-28下午02:05:21
 */
public class LicenseDownload {

	public void download(String URL, String enterpriseNo, String type) throws Exception,IOException {
		FileOutputStream out = null;
		InputStream in = null;

		String localFileName = Global.getRealPath() + "WEB-INF\\license.dat";

		try {
			URL url = new URL(URL);
			URLConnection urlConnection = url.openConnection();
			HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

			httpURLConnection.setDoOutput(true);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("connection", "Keep-Alive");
			httpURLConnection.setRequestProperty("Charsert", "UTF-8");
			// 1 min
			httpURLConnection.setConnectTimeout(60000);
			// 1 min
			httpURLConnection.setReadTimeout(60000);

			// httpURLConnection.addRequestProperty("userName", userName);
			// httpURLConnection.addRequestProperty("passwd", passwd);
			httpURLConnection.addRequestProperty("enterpriseNo", enterpriseNo);
			httpURLConnection.addRequestProperty("type", type);

			httpURLConnection.connect();
			
			// send request to server
			in = httpURLConnection.getInputStream();

			File file = new File(localFileName);
			if (!file.exists()) {
				file.createNewFile();
			}

			out = new FileOutputStream(file);
			byte[] buffer = new byte[4096];
			int readLength = 0;
			while ((readLength = in.read(buffer)) > 0) {
				byte[] bytes = new byte[readLength];
				System.arraycopy(buffer, 0, bytes, 0, readLength);
				out.write(bytes);
			}

			out.flush();
		} catch (Exception e) {
			LogUtil.getLog(getClass()).error(e);
			throw new Exception(e.toString());
		}finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				LogUtil.getLog(getClass()).error(e);
				throw new IOException(e.toString());
			}

			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				LogUtil.getLog(getClass()).error(e);
				throw new IOException(e.toString());
			}
		}
	}

}
