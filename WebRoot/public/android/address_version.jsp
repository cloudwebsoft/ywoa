<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.address.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.android.Privilege"/>
<%
/*
- 功能描述：通讯录同步
- 访问规则：来自手机客户端
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：2013-5
==================
- 修改者：
- 修改时间：
- 修改原因
- 修改点：
*/

// System.out.println(getClass() + " here");
String skey = ParamUtil.get(request, "skey");
String type = ParamUtil.get(request,"type");

JSONObject json = new JSONObject();
boolean re = privilege.Auth(skey);
String userName = privilege.getUserName(skey);
String unitCode = privilege.getUserUnitCode(skey); 
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

AddressVersionDb avd = new AddressVersionDb();
if(type != null){
	if(type.equals("-1")){
		userName = "organize";
		unitCode = "root";
	}else if(type.equals("1")){
		userName = "public";
		unitCode = "root";
	}
}else{
	userName = "public";
	unitCode = "root";
}

avd = avd.getAddressVersionDbOfUnit(userName,unitCode);
if (avd!=null) {
	int ver = avd.getInt("version");
	json.put("res", "0");
	json.put("msg", "操作成功！");
	json.put("version", "" + ver);
}
else {
	json.put("res", "-1");
	json.put("msg", "操作失败！");
}
out.print(json);
%>