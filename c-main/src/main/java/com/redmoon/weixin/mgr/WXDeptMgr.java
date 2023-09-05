package com.redmoon.weixin.mgr;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.entity.Department;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.sys.DebugUtil;
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
	public JSONArray wxDeptList(int id) {
		JSONArray jsonArr = null;
		String url = Constant.DEPT_LIST + getTokenContacts() + "&id=" + id;
		try {
			String result = HttpUtil.MethodGet(url);
			if (result != null && !"".equals(result)) {
				JSONObject json = new JSONObject(result);
				if (!json.isNull(Constant.ERRCODE)) {
					int errorCode = json.getInt(Constant.ERRCODE);
					if (errorCode == Constant.ERROR_CODE_SUCCESS) {
						jsonArr = json.getJSONArray(Constant.DEPARTMENT);
					} else {
						throw new ErrMsgException(json.getString("errmsg"));
					}
				}
			}
		} catch (JSONException e) {
			LogUtil.getLog(WXDeptMgr.class).error(e);
		}
		return jsonArr;
	}

	public JSONArray wxDeptList(){
		return wxDeptList(ROOT_ID);
	}

	public JSONArray wxUserListId() {
		JSONArray jsonArr = new JSONArray();
		String url = Constant.GET_USER_LIST_ID + getTokenContacts();
		try {
			String result = HttpUtil.MethodPost(url, "{}");
			if (result != null && !"".equals(result)) {
				JSONObject json = new JSONObject(result);
				if (!json.isNull(Constant.ERRCODE)) {
					int errorCode = json.getInt(Constant.ERRCODE);
					if (errorCode == Constant.ERROR_CODE_SUCCESS) {
						jsonArr = json.getJSONArray("dept_user");
					} else {
						throw new ErrMsgException(json.getString("errmsg"));
					}
				}
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return jsonArr;
	}

	/**
	 * 获取部门ID
	 * "department_id": [
	 *        {
	 *            "id": 2,
	 *            "parentid": 1,
	 *            "order": 10
	 *        },
	 * @return
	 */
	public JSONArray wxDeptListId() {
		JSONArray jsonArr = new JSONArray();
		String url = Constant.GET_DEPT_LIST_ID + getTokenContacts();
		try {
			String result = HttpUtil.MethodPost(url, "{}");
			if (result != null && !"".equals(result)) {
				JSONObject json = new JSONObject(result);
				if (!json.isNull(Constant.ERRCODE)) {
					int errorCode = json.getInt(Constant.ERRCODE);
					if (errorCode == Constant.ERROR_CODE_SUCCESS) {
						jsonArr = json.getJSONArray("department_id");
					} else {
						throw new ErrMsgException(json.getString("errmsg"));
					}
				}
			}
		} catch (JSONException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		return jsonArr;
	}
}
