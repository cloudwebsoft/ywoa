package com.redmoon.weixin.mgr;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jdom.Element;
import cn.js.fan.util.StrUtil;
import com.redmoon.weixin.Config;
import com.redmoon.weixin.config.Constant;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-8-8下午03:34:57
 */
public class WXMenuMgr extends WXBaseMgr {
	/**
	 * 创建菜单
	 * @Description: 
	 * @param isWork
	 * @param agentId
	 * @param body
	 * @param agentSecret
	 * @return
	 */
	public int createMenu(boolean isWork, int agentId, String body, String agentSecret){
		String accessToken = "";
		if (isWork) {
			accessToken = getToken(agentSecret);
		}
		else {
			accessToken = getToken();
		}
		
		String url = Constant.MENU_CREATE + accessToken+"&agentid="+agentId;
		return baseRequestWxAdd(url, body, Constant.REQUEST_METHOD_POST);
	}
	
	/**
	 * 删除菜单
	 * @Description: 
	 * @param isWork
	 * @param agentId
	 * @param agentSecret
	 * @return
	 */
	public int deleteMenu(boolean isWork, int agentId, String agentSecret){
		String accessToken = "";
		if (isWork) {
			accessToken = getToken(agentSecret);
		}
		else {
			accessToken = getToken();
		}

		String url = Constant.MENU_DELETE + accessToken+"&agentid="+agentId;
		return baseRequestWxAdd(url, null, Constant.REQUEST_METHOD_GET);
	}
	/**
	 * 批量删除
	 * @Description:
	 */
	public void batchDeleteMenu(){
		Config config = Config.getInstance();
		boolean isWork = config.getBooleanProperty("isWork");
		org.jdom.Element root = config.getRoot();
		Element agentMenu = root.getChild("agentMenu");
		List<Element> menus = agentMenu.getChildren("item");
		for(Element ele:menus){
			int agentId = StrUtil.toInt(ele.getChild("agentId").getText());
			String agentSecret = ele.getChild("secret").getText();
			deleteMenu(isWork, agentId, agentSecret);
		}
		
	}
	/**
	 * 批量新增菜单
	 * @Description: 
	 * @return
	 */
	public void batchCreateMenu(){
		Config config = Config.getInstance();
		boolean isWork = config.getBooleanProperty("isWork");
		org.jdom.Element root = config.getRoot();
		Element agentMenu = root.getChild("agentMenu");
		String corpId = config.getProperty("corpId");
		List<Element> menus = agentMenu.getChildren("item");
		
		if (isWork) {
			for(Element ele:menus){
				String agentId = ele.getChild("agentId").getText();
				String agentName = ele.getChild("agentName").getText();
				String agentSecret = ele.getChild("secret").getText();
				Element btnElement = ele.getChild("button");
				
				JSONArray btnArr = new JSONArray();
				
				JSONObject agentObj = new JSONObject();
				agentObj.put("name", agentName);
				
				List<Element> btns = btnElement.getChildren("menu");
				JSONArray subBtnArr = new JSONArray();
				for(Element btnEle:btns){
					String type = btnEle.getChild("type").getText();
					String name = btnEle.getChild("name").getText();
					String url = btnEle.getChild("url").getText();

					if (url.indexOf("?")==-1) {
						url += "?agentId=" + agentId;
					}
					else {
						url += "&agentId=" + agentId;							
					}

					// scope 为snsapi_privateinfo时，手动授权，可获取成员的详细信息，包含手机、邮箱等敏感信息，当点击菜单时会提示手工授权
					// url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+corpId+"&redirect_uri="+StrUtil.UrlEncode(url)+"&response_type=code&scope=snsapi_privateinfo#wechat_redirect";
					// 上行为老版的授权，20181207发现方式已改，且有时redirect_uri带的agentId传不过去，需要在页面中request.setAttribute("agentId", ...);
					// 判断是否为私有部署的微信
					String serverName = Config.getInstance().getProperty("serverName");
					if (!serverName.equals("qyapi.weixin.qq.com")) {
						url = "https://" + serverName + "/connect/oauth2/authorize?appid="+corpId+"&redirect_uri="+StrUtil.UrlEncode(url)+"&response_type=code&agentid=" + agentId + "&scope=snsapi_base#wechat_redirect";
					}
					else {
						url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+corpId+"&redirect_uri="+StrUtil.UrlEncode(url)+"&response_type=code&agentid=" + agentId + "&scope=snsapi_base#wechat_redirect";
					}

					JSONObject jsonObj = new JSONObject();
					jsonObj.put("type",type);
					jsonObj.put("name", name);
					jsonObj.put("url", url);
					
					subBtnArr.add(jsonObj);
				}
				
				agentObj.put("sub_button", subBtnArr);
				
				btnArr.add(agentObj);
				
				JSONObject resObj = new JSONObject();
				resObj.put("button", btnArr);

				// deleteMenu(StrUtil.toInt(agentId)); //删除菜单
				createMenu(isWork, StrUtil.toInt(agentId), resObj.toString(), agentSecret);//创建菜单
			}
		}
		else {
			for(Element ele:menus){
				String agentId = ele.getChild("agentId").getText();
				String agentSecret = ele.getChild("secret").getText();
				Element btnElement = ele.getChild("button");
				List<Element> btns = btnElement.getChildren("menu");
				JSONArray jsonArr = new JSONArray();
				for(Element btnEle:btns){
					String type = btnEle.getChild("type").getText();
					String name = btnEle.getChild("name").getText();
					String urls = btnEle.getChild("url").getText();
					String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+corpId+"&redirect_uri="+StrUtil.UrlEncode(urls)+"&response_type=codeagentid=" + agentId + "&scope=snsapi_base#wechat_redirect";
					String serverName = Config.getInstance().getProperty("serverName");
					if (!serverName.equals("qyapi.weixin.qq.com")) {
						url = "https://" + serverName + "/connect/oauth2/authorize?appid="+corpId+"&redirect_uri="+StrUtil.UrlEncode(urls)+"&response_type=code&agentid=" + agentId + "&scope=snsapi_base#wechat_redirect";
					}
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("type",type);
					jsonObj.put("name", name);
					jsonObj.put("url", url);
					jsonArr.add(jsonObj);
				}
				JSONObject resultObj = new JSONObject();
				resultObj.put("button", jsonArr);
				// deleteMenu(StrUtil.toInt(agentId)); //删除菜单
				createMenu(isWork, StrUtil.toInt(agentId), resultObj.toString(), agentSecret);//创建菜单
			}
		}
	}
	
	
	
	
}
