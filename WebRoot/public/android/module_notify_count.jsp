<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.map.*"%>
<%@ page import="com.redmoon.oa.android.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.android.Privilege"/>
<%
/*
- 功能描述：定位签到
- 访问规则：来自手机客户端
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：2013-5
==================
- 修改者：
- 修改时间：
- 修改原因：
- 修改点：
*/

// System.out.println(getClass() + " here");
String skey = ParamUtil.get(request, "skey");
JSONObject json = new JSONObject();
boolean re = privilege.Auth(skey);
if(re){
	try {
		json.put("res","-2");
		json.put("msg","时间过期");
		out.print(json.toString());
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return;
}

String userName = privilege.getUserName(skey);
String myUnitCode = privilege.getUserUnitCode(skey); // 获得当前登陆人的username
if (userName.equals("")) {
	json.put("res", "0");
	json.put("msg", "请先登录！");
	out.print(json);
	return;
}

re = true;

// 取得未读短消息的条数
MessageDb md = new MessageDb();
int msgNewCount = md.getNewMsgCount(userName);
int msgInnerCount = md.getNewInnerMsgCount(userName);
int sysNoticeCount = md.getNewSysMsgCount(userName);
// 取得待办流程的条数
int flowWaitCount = WorkflowDb.getWaitCount(userName);

if (re) {
	json.put("res", "0");
	json.put("msg", "操作成功！");
	json.put("msgNewCount",String.valueOf(msgNewCount));
	json.put("sysNoticeCount",String.valueOf(sysNoticeCount));
	json.put("flowWaitCount",String.valueOf(flowWaitCount));
	json.put("msgInnerCount",String.valueOf(msgInnerCount));
}
else {
	json.put("res", "-1");
	json.put("msg", "操作失败！");
}
out.print(json);
%>