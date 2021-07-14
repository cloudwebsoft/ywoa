package com.redmoon.weixin.mgr;

import com.cloudweb.oa.entity.Department;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.weixin.config.Constant;
import com.redmoon.weixin.util.HttpUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Description:
 * @author:
 * @Date: 2016-7-21下午02:40:10
 */
public class WXDeptMgr extends WXBaseMgr {
	public static final int ROOT_ID = 1;

	/**
	 * 新增部门
	 * @param department
	 * @return
	 */
	public int createWxDept(Department department) {
		String accessToken = getTokenContacts();
		String url = Constant.CREATE_DEPT + accessToken;
		String deptStr = getDeptParam(department);
		LogUtil.getLog(getClass()).info("createWxDept:" + deptStr);
		return baseRequestWxAdd(url, deptStr, Constant.REQUEST_METHOD_POST);
	}

	/**
	 * 更新部门
	 * @param department
	 * @return
	 */
	public int updateWxDept( Department department) {
		String accessToken = getTokenContacts();
		String url = Constant.UPDATE_DEPT + accessToken;
		String deptStr = getDeptParam(department);
		return baseRequestWxAdd(url, deptStr, Constant.REQUEST_METHOD_POST);
	}

	/**
	 * 删除部门
	 * @param id
	 * @return
	 */
	public int deleteWxDept( int id) {
		String accessToken = getTokenContacts();
		// 企业微信规则不能删除根部门；不能删除含有子部门、成员的部门
		String url = Constant.DELETE_DEPT + accessToken + "&id=" + id;
		return baseRequestWxAdd(url, "", Constant.REQUEST_METHOD_GET);
	}

	/**
	 * 获取部门列表
	 * @param id
	 * @return
	 */
	public JSONArray wxDeptList( int id) {
		JSONArray _jsonArr = null;
		String url = Constant.DEPT_LIST + getTokenContacts() + "&id=" + id;
		try {
			String result = HttpUtil.MethodGet(url);
			if (result != null && !result.equals("")) {
				JSONObject json = new JSONObject(result);
				if (json != null && !json.isNull(Constant.ERRCODE)) {
					int errorCode = json.getInt(Constant.ERRCODE);
					if (errorCode == Constant.ERROR_CODE_SUCCESS) {
						_jsonArr = json.getJSONArray(Constant.DEPARTMENT);
					}
				}
			}
		} catch (JSONException e) {
			LogUtil.getLog(WXDeptMgr.class).info(e.getMessage());
		}
		return _jsonArr;
	}

	public JSONArray wxDeptList(){
		return  wxDeptList(ROOT_ID);
	}
}
