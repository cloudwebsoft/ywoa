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
- 功能描述：实时取得用户信息，已作废，改为基于手机客户端本地读取
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

String mobile = ParamUtil.get(request, "mobile");
// System.out.println(getClass() + " mobile=" + mobile);

AddressDb ad = new AddressDb();
String[] ary = ad.getAddressDbByMobile(mobile);
if (ary!=null) {
	json.put("res", "1");
	json.put("msg", "操作成功！");
	json.put("realName", ary[0]);
	json.put("deptName", ary[1]);
}
else {
	json.put("res", "0");
	json.put("msg", "号码对应的用户不存在！");
}
out.print(json);
%>