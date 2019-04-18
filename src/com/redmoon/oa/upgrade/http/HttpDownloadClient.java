package com.redmoon.oa.upgrade.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redmoon.oa.upgrade.service.UpgradeException;

public class HttpDownloadClient {
	private static Logger logger = LoggerFactory
			.getLogger(HttpDownloadClient.class);

	public void download(String url, File target) {
		try {
			this.doDownload(url, target);
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(), e);
			throw new UpgradeException("Download failed with url: " + url, e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new UpgradeException("Download failed with url: " + url, e);
		} catch (UpgradeException e) {
			logger.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new UpgradeException("Download failed with url: " + url, e);
		}
	}

	private void doDownload(String url, File target)
			throws ClientProtocolException, IOException {
		if (url == null || target == null) {
			throw new IllegalArgumentException("Argument cannot be null.");
		}
		if (!target.getParentFile().exists()) {
			target.getParentFile().mkdirs();
		}
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		HttpResponse response = httpClient.execute(get);
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {
			this.saveFile(response, target);
		} else if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY
				|| statusCode == HttpStatus.SC_MOVED_PERMANENTLY) {
			if (!response.containsHeader("Location")) {
				throw new UpgradeException(
						"Cannot download update package with url: " + url);
			}
			this.doDownload(response.getHeaders("Location")[0].getValue(),
					target);
		} else {
			throw new UpgradeException(
					"Cannot download update package with url: " + url + " ("
							+ response.getStatusLine().toString() + ")");
		}
	}

	private void saveFile(HttpResponse response, File target)
			throws IllegalStateException, IOException {
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = response.getEntity().getContent();
			outputStream = new FileOutputStream(target);
			byte[] buffer = new byte[8192];
			int size = 0;
			while ((size = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, size);
			}
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {

				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {

				}
			}
		}
	}
}
