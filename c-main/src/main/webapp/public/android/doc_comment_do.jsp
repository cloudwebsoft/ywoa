<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.cloudwebsoft.framework.util.*"%>
<%@page import="com.redmoon.oa.android.sales.LinkmanListAction"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.android.Privilege"/>
<%
	/*
	 - 功能描述：文章评论
	 - 访问规则：来自手机客户端
	 - 过程描述：
	 - 注意事项：
	 - 创建者：fgf 
	 - 创建时间：2013-9-8
	 ==================
	 - 修改者：
	 - 修改时间：
	 - 修改原因
	 - 修改点：
	 */
	String skey = ParamUtil.get(request, "skey");
	JSONObject json = new JSONObject();
	boolean re = privilege.Auth(skey);
	if (re) {
		try {
			json.put("res", "-2");
			json.put("msg", "时间过期");
			out.print(json.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}
	String userName = privilege.getUserName(skey);
	String op = ParamUtil.get(request, "op");
	if (op.equals("add")) {
		int docId = ParamUtil.getInt(request, "docId");
		CommentMgr cmt = new CommentMgr();
		int type = ParamUtil.getInt(request,"type",0);//0代表IOS
		String content = "";
		if(type == 1){
			content = ParamUtil.get(request, "content"); 
		}else{
			content = request.getParameter("content");
		}
		String link = "";
		String ip = IPUtil.getRemoteAddr(request);
        re = cmt.create(docId, userName, content, link, ip);
		if (re) {
			json.put("res", "0");
			json.put("msg", "操作成功");	
		}
		else {
			json.put("res", "-1");
			json.put("msg", "操作失败");	
		}
	}

	out.print(json);
%>