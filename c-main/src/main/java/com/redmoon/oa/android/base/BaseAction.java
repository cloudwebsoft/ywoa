package com.redmoon.oa.android.base;

import org.json.JSONObject;

public class BaseAction implements IbeanUtil {
	public static int RETURNCODE_SUCCESS = 0;       //登录成功
	public static int RESULT_TIME_OUT = -2;//时间过期
	public static int RESULT_SUCCESS = 0;//请求成功
	public static int RESULT_NO_DATA = -1;//列表无数据 
	public static int RESULT_FORMCODE_ERROR = -3;//未传formcode
	public static int RESULT_INSERT_FAIL = -4;//插入失败
	public static int RESULT_ID_NULL = -5;//详细信息ID为空
	public static int RESULT_DATE_ISEXISTS = -7;//已新增
	public static int RESULT_MODULE_ERROR = -8;//未传formcode\
	public static int RESULT_SKEY_ERROR = -9;//skey不存在
	public static int RESULT_SERVER_ERROR = -10;//服务器异常
	
	public static int DEPT = 1;//已新增
	public static int USER = 0;//已新增
	public static int BACK = 1;
	public static int NO_BACK = 0;
	
	//附件类型 应用于common_getfile.jsp中 通用的附件流下载
	public static int WORK_LOG_ATT_FILE_TYPE = 0;

	public JSONObject jSend = new JSONObject();
	public JSONObject jReturn = new JSONObject(); 
	public JSONObject jResult = new JSONObject();
	
	public static final String SUCCESS = "SUCCESS";
	public static final String RES = "res";
	public static final String RETURNCODE = "returnCode";
	public static final String RESULT = "result";
	public static final String DATAS = "datas";
	public static final String SEARCH = "search";
	public static final String DATA = "data";

	public static final String MACRO_CURRENT_USER = "macro_current_user";//当前用户
	public static final String MACRO_USER_SELECT = "macro_user_select";//用户列表
	public static final String MACRO_USER_SELECT_WIN= "macro_user_select_win";//用户选择框
	public static final String MACRO_DEPT_SELECT = "macro_dept_select"; //部门选择框
	public static final String MACRO_POST_LIST = "macro_post_list"; //职位列表宏控件
	public static final String MACRO_POST = "macro_post"; //当前用户职位宏控件

	public static final int WOKR_lOG_PRJ_TYPE = 1;
	public static final int WOKR_lOG_TASK_TYPE = 0;

	private String result;
	
	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public Object get(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void set(String name, Object value) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String execute() {
		// TODO Auto-generated method stub
		executeAction(); //具体的业务逻辑
		setResult(jReturn.toString());
		return SUCCESS;
	}
	
	public void executeAction(){
		
	}
}
