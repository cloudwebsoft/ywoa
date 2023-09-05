package com.redmoon.weixin.config;

import com.redmoon.weixin.Config;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-7-22上午11:55:06
 */
public class Constant {
	public static String serverName = "";
	
	static {
		serverName = Config.getInstance().getProperty("serverName");
	}
	
	public static final int REQUEST_METHOD_GET = 1;//GET请求
	public static final int REQUEST_METHOD_POST = 0;//POST请求
	public static final String GET_TOKEN = "https://" + serverName + "/cgi-bin/gettoken?";//微信获得access_token

	public static final String GET_CODE2SESSION = "https://" + serverName + "/cgi-bin/miniprogram/jscode2session?access_token=%s&js_code=%s&grant_type=authorization_code";

	//通讯录  部门
	public static final String CREATE_DEPT = "https://" + serverName + "/cgi-bin/department/create?access_token=";//创建部门
	public static final String UPDATE_DEPT = "https://" + serverName + "/cgi-bin/department/update?access_token=";//更新部门
	public static final String DELETE_DEPT = "https://" + serverName + "/cgi-bin/department/delete?access_token=";//删除部门
	public static final String DEPT_LIST = "https://" + serverName + "/cgi-bin/department/list?access_token=";//获得部门列表
	//通讯录  用户
	public static final String USER_BY_CODE = "https://" + serverName + "/cgi-bin/user/getuserinfo?access_token=";//根据用户code获取用户名
	public static final String CREATE_USER = "https://" + serverName + "/cgi-bin/user/create?access_token=";//新增用户
	public static final String UPDATE_USER = "https://" + serverName + "/cgi-bin/user/update?access_token=";//更新用户
	public static final String BATCH_DELETE_USER = "https://" + serverName + "/cgi-bin/user/batchdelete?access_token=";//删除用户
	public static final String GET_USER_INFO = "https://" + serverName + "/cgi-bin/user/get?access_token=";//获得用户详情
	public static final String DEPT_USER_LIST = "https://" + serverName + "/cgi-bin/user/list?access_token=";//部门下用户
	public static final String DELETE_USER = "https://" + serverName + "/cgi-bin/user/delete?access_token=";//删除用户
	//agent
	public static final String AGENT_LIST = "https://" + serverName + "/cgi-bin/agent/list?access_token=";
	//菜单
	public static final String MENU_CREATE = "https://" + serverName + "/cgi-bin/menu/create?access_token=";//创建菜单
	public static final String MENU_DELETE = "https://" + serverName + "/cgi-bin/menu/delete?access_token=";//删除菜单
	//消息类型
	public static final String MESSAGE_SEND = "https://" + serverName + "/cgi-bin/message/send?access_token=";//消息类型
	public static final String AGENTLIST = "agentlist";
	public static final String ERRCODE = "errcode";//请求ERRCODE
	public static final String DEPARTMENT="department";//部门
	public static final String USERLIST="userlist";//用户列表

	// 获取企业成员的userid与对应的部门ID列表
	public static final String GET_USER_LIST_ID = "https://qyapi.weixin.qq.com/cgi-bin/user/list_id?access_token=";

	// 部门id。获取指定部门及其下的子部门（以及子部门的子部门等等，递归）。 如果不填id，默认获取全量组织架构，如需指定id，后面加 &id=
	public static final String GET_DEPT_LIST_ID = "https://qyapi.weixin.qq.com/cgi-bin/department/simplelist?access_token=";

	public static final int ERROR_CODE_NO_RETURN = -1;//请求结果未返回
	public static final int ERROR_CODE_SUCCESS = 0; //请求 成功
	public static final int ERROR_CODE_CREATE_EXISTED = 6008;//部门ID或者部门名称已存在 
	public static final int ERROR_CODE_ILLEGAL_ACCESS_TOKEN = 40014;//不合法的Access_token
	public static final int ERROR_CODE_ILLEGAL_CORPID = 40013 ;//不合法的公司企业ID

}
