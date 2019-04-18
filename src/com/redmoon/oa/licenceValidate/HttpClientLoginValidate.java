package com.redmoon.oa.licenceValidate;


import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.licenceValidate.diskno.*;
import com.redmoon.oa.util.HttpClientConnectUtil;


/**
 * @Description: 注册企业号及激活企业号页面验证数据正确性
 * @author: lichao
 * @Date: 2015-8-28下午01:59:07
 */
public class HttpClientLoginValidate {
	private static String msg = "";
	
	//企业号登录验证用
	public static boolean HttpClientValEntEmail(String URL, JSONArray jarray) throws JSONException, IOException {
		boolean flag = false;

		try {
			StringBuilder sb = new StringBuilder("");
			sb = HttpClientConnectUtil.getHttpConnect(URL, jarray.toString());

			JSONObject jobj = new JSONObject(sb.toString());
			if (jobj.getInt("ret") == 1) {
				flag = true;
			}
		} catch (JSONException e) {
			LogUtil.getLog("HttpClientValEntEmail").error(StrUtil.trace(e));
			e.printStackTrace();
		} catch (IOException e) {
			LogUtil.getLog("HttpClientValEntEmail").error(StrUtil.trace(e));
			e.printStackTrace();
		}

		return flag;
	}

	// 验证企业号是否存在、审核情况
	public static int HttpClientValEntNo(String URL, String str) throws JSONException, IOException {
		int re = -1;

		try {
			StringBuilder sb = new StringBuilder("");
			sb = HttpClientConnectUtil.getHttpConnect(URL, str);

			JSONObject jobj = new JSONObject(sb.toString());
			re = jobj.getInt("ret");
		} catch (JSONException e) {
			LogUtil.getLog("HttpClientValEntNo").error(StrUtil.trace(e));
			e.printStackTrace();
		} catch (IOException e) {
			LogUtil.getLog("HttpClientValEntNo").error(StrUtil.trace(e));
			e.printStackTrace();
		}

		return re;
	}

	/**
	 * 企业号激活用
	 */
	public static boolean HttpClientEntLogin(String URL, JSONObject jobject) throws JSONException, IOException {
		boolean flag = false;
		jobject = DiskNo.GetDiskNoOrMotherboardNo(jobject); // 获取系统所在机器的硬盘号或主板号

		JSONArray jarray = new JSONArray();
		jarray.put(jobject);

		try {
			StringBuilder sb = new StringBuilder("");
			sb = HttpClientConnectUtil.getHttpConnect(URL, jarray.toString());

			JSONObject jobj = new JSONObject(sb.toString());
			if (jobj.getInt("ret") == 1) {
				flag = true;
			}
			msg = jobj.getString("msg"); 
		} catch (JSONException e) {
			LogUtil.getLog("HttpClientEntLogin").error(StrUtil.trace(e));
			e.printStackTrace();
		} catch (IOException e) {
			LogUtil.getLog("HttpClientEntLogin").error(StrUtil.trace(e));
			e.printStackTrace();
		}

		return flag;
	}
	
   //注册验证用	
   public static boolean HttpClientVal(String URL,String str) throws JSONException, IOException{
	   boolean flag = false;
	   
		try {
			StringBuilder sb = new StringBuilder("");
			sb = HttpClientConnectUtil.getHttpConnect(URL, str);

			JSONObject jobj = new JSONObject(sb.toString());
			if (jobj.getInt("ret") == 1) {
				flag = true;
			}
		} catch (JSONException e) {
			LogUtil.getLog("HttpClientVal").error(StrUtil.trace(e));
			e.printStackTrace();
		} catch (IOException e) {
			LogUtil.getLog("HttpClientVal").error(StrUtil.trace(e));
			e.printStackTrace();
		}
		
		return flag;
   }
   
	// 注册post数据
	public static JSONObject HttpClientRegist(String URL, JSONObject jobject) throws JSONException, IOException {
		JSONObject jReturn = null;

		try {
			StringBuilder sb = new StringBuilder("");
			sb = HttpClientConnectUtil.getHttpConnect(URL, jobject.toString());

			jReturn = new JSONObject(sb.toString());
		} catch (JSONException e) {
			LogUtil.getLog("HttpClientRegist").error(StrUtil.trace(e));
			e.printStackTrace();
		} catch (IOException e) {
			LogUtil.getLog("HttpClientRegist").error(StrUtil.trace(e));
			e.printStackTrace();
		}

		return jReturn;
	}
	
	/**
	 * @Description: License 校验
	 * @param URL
	 *            服务端接口地址
	 * @param jobject
	 *            包含客户端信息的json对象 enterpriseNum、userCount、diskNo
	 * @return json对象,包含ret、expire
	 */
	public static JSONObject HttpClientLM(String URL, JSONObject jobject) throws JSONException, IOException {
		JSONObject jReturn = null;

		try {
			StringBuilder sb = new StringBuilder("");
			sb = HttpClientConnectUtil.getHttpConnect(URL, jobject.toString());

			jReturn = new JSONObject(sb.toString());
		} catch (JSONException e) {
			LogUtil.getLog("HttpClientLM").error(e);
			e.printStackTrace();
		} catch (IOException e) {
			LogUtil.getLog("HttpClientLM").error(e);
			e.printStackTrace();
		}

		return jReturn;
	}
	
	/**
	 * popup_box.jsp页面检测企业号审核情况
	 */
	public static JSONObject checkEnterpriseNoStatus(String URL, JSONObject jobject) throws JSONException, IOException {
		JSONObject jReturn = null;

		try {
			StringBuilder sb = new StringBuilder("");
			sb = HttpClientConnectUtil.getHttpConnect(URL, jobject.toString());

			jReturn = new JSONObject(sb.toString());
		} catch (JSONException e) {
			LogUtil.getLog("checkEnterpriseNoStatus").error(StrUtil.trace(e));
			e.printStackTrace();
		} catch (IOException e) {
			LogUtil.getLog("checkEnterpriseNoStatus").error(StrUtil.trace(e));
			e.printStackTrace();
		}

		return jReturn;
	}

	public static String getMsg() {
		return msg;
	}

}
