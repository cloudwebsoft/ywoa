package com.redmoon.oa.upgrade.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmoon.oa.upgrade.domain.StatusRequest;
import com.redmoon.oa.upgrade.domain.StatusResponse;
import com.redmoon.oa.upgrade.domain.VersionRequest;
import com.redmoon.oa.upgrade.domain.VersionResponse;
import com.redmoon.oa.upgrade.service.UpgradeException;
import com.redmoon.oa.upgrade.util.IMiscUtil;

public class RestClient {
	private static ObjectMapper objectMapper = new ObjectMapper();

	private IMiscUtil miscUtil;

	public VersionResponse fetchNewVersion(VersionRequest request) {
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,  30000);//连接时间30s
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,  60000);//数据传输时间60s
		HttpPost httpRequest = new HttpPost(miscUtil.getUpgradeVersionUrl());

		BufferedReader rd = null;
		StringBuffer result = new StringBuffer();
		try {
			httpRequest.setHeader("Content-Type", "application/json");
			httpRequest.setEntity(new StringEntity(objectMapper
					.writeValueAsString(request)));
			HttpResponse response = client.execute(httpRequest);
			if (response.getStatusLine().getStatusCode() >= 400) {
				return null;
			}
			rd = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));

			String line = null;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			return objectMapper.readValue(result.toString(),
					VersionResponse.class);
		} catch (IOException e) {
			throw new UpgradeException("Request new version failed.", e);
		} finally {
			if (rd != null) {
				try {
					rd.close();
				} catch (IOException e) {

				}
			}
		}

	}

	public Boolean reportStatus(StatusRequest request) {
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,  30000);//连接时间30s
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,  60000);//数据传输时间60s
		HttpPost httpRequest = new HttpPost(miscUtil.getUpgradeStatusUrl());

		BufferedReader rd = null;
		StringBuffer result = new StringBuffer();
		try {
			httpRequest.setHeader("Content-Type", "application/json");
			httpRequest.setEntity(new StringEntity(objectMapper
					.writeValueAsString(request)));
			HttpResponse response = client.execute(httpRequest);
			rd = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));

			String line = null;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			return objectMapper.readValue(result.toString(),
					StatusResponse.class).isResult();
		} catch (IOException e) {
			throw new UpgradeException("Report version failed.", e);
		} finally {
			if (rd != null) {
				try {
					rd.close();
				} catch (IOException e) {

				}
			}
		}
	}
	/**
	 * 第一次使用上报ip地址及类型
	 * @param request
	 * @return
	 */
	public Boolean firstUseInfo(VersionRequest request) {
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,  30000);//连接时间30s
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,  60000);//数据传输时间60s
		HttpPost httpRequest = new HttpPost(miscUtil.getFirstUseInfoUrl());

		BufferedReader rd = null;
		StringBuffer result = new StringBuffer();
		try {
			httpRequest.setHeader("Content-Type", "application/json");
			httpRequest.setEntity(new StringEntity(objectMapper
					.writeValueAsString(request)));
			HttpResponse response = client.execute(httpRequest);
			rd = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));

			String line = null;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			return objectMapper.readValue(result.toString(),
					StatusResponse.class).isResult();
		} catch (IOException e) {
			throw new UpgradeException("Report version failed.", e);
		} finally {
			if (rd != null) {
				try {
					rd.close();
				} catch (IOException e) {

				}
			}
		}
	}

	public IMiscUtil getMiscUtil() {
		return miscUtil;
	}

	public void setMiscUtil(IMiscUtil miscUtil) {
		this.miscUtil = miscUtil;
	}

}
