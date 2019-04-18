package com.redmoon.oa.android.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.json.*;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.flow.Directory;
import com.redmoon.oa.flow.DirectoryView;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.post.PostFlowMgr;

/**
 * @author lichao
 * 发起流程功能列表接口
 */
public class GetMobileFlowIconAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	private static int RES_EXPIRED = -2;                     //SKEY过期
	
	private static int RETURNCODE_SUCCESS = 0;               //获取成功
	private static int RETURNCODE_SUCCESS_NULL = -1;         //获取成功，但无数据
	
	private String skey = "";
	private String result = "";
	
	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String execute() {
		boolean flag = true;
		JSONArray jArray = new JSONArray();
		JSONObject jReturn = new JSONObject();
		JSONObject jResult = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(skey);
		PostFlowMgr pfMgr = new PostFlowMgr();
	
		
		if(re){
			try {
				jReturn.put("res",RES_EXPIRED);
				jResult.put("returnCode", "");
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		HttpServletRequest request = ServletActionContext.getRequest();
		privilege.doLogin(request, getSkey());
		String userName = privilege.getUserName(skey);
		ArrayList<String> list = pfMgr.listCanUserStartFlow(userName);
		
		//取得有权限发起的流程
		Directory dir = new Directory();
		Leaf rootlf = dir.getLeaf(Leaf.CODE_ROOT);
		DirectoryView dv = new DirectoryView(rootlf);
		Vector children;
		try {
			children = dir.getChildren(Leaf.CODE_ROOT);
			Iterator ri = children.iterator();

			while (ri.hasNext()) {
				Leaf childlf = (Leaf) ri.next();
				if (childlf.isOpen() && dv.canUserSeeWhenInitFlow(request, childlf)) {
					Iterator ir = dir.getChildren(childlf.getCode()).iterator();
					while (ir.hasNext()) {
						Leaf chlf = (Leaf) ir.next();
						if (chlf.isOpen()  && dv.canUserSeeWhenInitFlow(request, chlf)) {
							boolean mobileCanStart = false;
							if(chlf.getParentCode().equals("performance")){
								if (list.contains(chlf.getCode())) {
									if (chlf.getType() != Leaf.TYPE_NONE) {
										mobileCanStart = true;
									}
								}
							}else{
								if(chlf.isMobileStart()){
									if (chlf.getType() != Leaf.TYPE_NONE) {
										mobileCanStart = true;
									}
								}
							}
							if (mobileCanStart) {
								JSONObject jObject = new JSONObject();
								jObject.put("flowCode", chlf.getCode());
								jObject.put("flowName", chlf.getName());
								jObject.put("flowType", chlf.getType());
								
								MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
								String imgUrl = mr.getImgUrl(chlf.getCode(), 2);
								
								jObject.put("imgUrl", imgUrl);
								jArray.put(jObject);
							}
						}
					}
				}
			}
			
			if(jArray.length()==0){
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_SUCCESS_NULL);
			}else{
				jReturn.put("res", RES_SUCCESS);
				jResult.put("returnCode", RETURNCODE_SUCCESS);	
				jResult.put("datas", jArray);
			}

			jReturn.put("result", jResult);
		} catch (JSONException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (ErrMsgException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally{
			if(!flag){
				try {
					jReturn.put("res", RES_FAIL);
					jResult.put("returnCode", "");
					jReturn.put("result", jResult);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		setResult(jReturn.toString());
		return "SUCCESS";
	}
}
