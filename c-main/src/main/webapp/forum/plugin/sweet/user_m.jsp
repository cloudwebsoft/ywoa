<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.sweet.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=request.getContextPath()%>/forum/<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title>本贴用户管理</title>
</head>
<body>
<div id="wrapper">
<%@ include file="../../inc/header.jsp"%>
<div id="main">
<%@ include file="../../inc/position.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.plugin.sweet.SweetPrivilege"/>
<div id="newdiv" name="newdiv">
<%
// 安全验证
long msgRootId = ParamUtil.getLong(request, "msgRootId");
if (!privilege.isOwner(request, msgRootId)) {
	out.print(StrUtil.Alert_Back(SweetSkin.LoadString(request, "PRIVILGE_NOT_OWNER")));
	return;
}

String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	String name = ParamUtil.get(request, "name");
	int type = ParamUtil.getInt(request, "type");
	int state = ParamUtil.getInt(request, "state");	
	SweetUserDb su = new SweetUserDb();
	su = su.getSweetUserDb(msgRootId, name);
	su.setType(type);
	su.setState(state);
	if (su.save()) {
	}
	else {
		out.print(StrUtil.Alert("操作失败！"));
	}
}
%>
<div class="tableTitle">本贴会员列表&nbsp;(<a href="../../showtopic.jsp?rootid=<%=msgRootId%>">贴子编号:<%=msgRootId%></a>)</div>
<TABLE class="tableCommon" width="98%">
    <thead>
      <TR align=center> 
        <TD width=107>用户名</TD>
        <TD width=138>性别</TD>
        <TD width=101>OICQ</TD>
        <TD width=125>省份</TD>
        <TD width=125>注册时间</TD>
      	<TD width=125>状态</TD>
        <TD width=125>类型</TD>
      	<TD width=125>操作</TD>
      </TR>
	</thead>
<%		
String id="",name="",RegDate="",Gender="",OICQ="",State="",myface="";
int layer = 1;
int i = 0;
String RealPic = "";

SweetUserDb su = new SweetUserDb();

String typeoptions = "";
typeoptions += "<option value='" + su.TYPE_APPLIER + "'>" + su.getTypeDesc(request, su.TYPE_APPLIER) + "</option>";
typeoptions += "<option value='" + su.TYPE_PERSUATER + "'>" + su.getTypeDesc(request, su.TYPE_PERSUATER) + "</option>";
// typeoptions += "<option value='" + su.TYPE_SPOUSE + "'>" + su.getTypeDesc(request, su.TYPE_SPOUSE) + "</option>";

String stateoptions = "";
stateoptions += "<option value='" + su.STATE_NORMAL + "'>" + su.getStateDesc(request, su.STATE_NORMAL) + "</option>";
stateoptions += "<option value='" + su.STATE_SHIELD + "'>" + su.getStateDesc(request, su.STATE_SHIELD) + "</option>";

Vector v = su.getAllPersuater(msgRootId);
Iterator ir = v.iterator();
UserDb user = new UserDb();
while (ir.hasNext()) {
 	    su = (SweetUserDb)ir.next(); 
		user = user.getUser(su.getName());
	    i++;
		name = user.getName();
		RegDate = DateUtil.format(user.getRegDate(), "yyyy-MM-dd");
		Gender = StrUtil.getNullString(user.getGender());
		if (Gender.equals("M"))
			Gender = "男";
		else if (Gender.equals("F"))
			Gender = "女";
		else
			Gender = "不详";
		
		OICQ = StrUtil.getNullString(user.getOicq());
		State = StrUtil.getNullString(user.getState());
		if (State.equals("0"))
			State = "不详";
		RealPic = user.getRealPic();
		myface = user.getMyface();
%>
      <TR align=center bgColor=#f8f8f8> 
	  <form action="?op=modify" method="post" name="form<%=i%>">
        <TD width=107 align="left"> &nbsp;
		<input type=hidden name=msgRootId value="<%=su.getMsgRootId()%>">
		<input type=hidden name="name" value="<%=su.getName()%>">
			  <%if (myface.equals("")) {%>
			  <img src="../../images/face/<%=RealPic%>" width=16 height=16> 
			  <%}else{%>
			  <img src="<%=user.getMyfaceUrl(request)%>" width=16 height=16>
			  <%}%>		
          <a href="../../../userinfo.jsp?username=<%=StrUtil.UrlEncode(StrUtil.toHtml(name),"utf-8")%>"><%=name%></a> 
        </TD>
        <TD width=138><%=Gender%></TD>
        <TD width=101><%=OICQ%></TD>
        <TD width=125><%=State%></TD>
        <TD width=125><%=RegDate%></TD>
      <TD width=125>
	  <select name=state>
	  <%=stateoptions%>
	  </select>
	  <script>
	  form<%=i%>.state.value = "<%=su.getState()%>";
	  </script>
	  </TD>
        <TD width=125>
	  <select name=type>
	  <%=typeoptions%>
	  </select>
	  <script>
	  form<%=i%>.type.value = "<%=su.getType()%>";
	  </script>
		</TD>
      <TD width=125>
	  <input type="submit" value="确定">
	  </TD></form>
      </TR>
<%}%>
    </TBODY>
  </TABLE>
</div>
<%@ include file="../../inc/footer.jsp"%>
</div>
</body>
</html>
