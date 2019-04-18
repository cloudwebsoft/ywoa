<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.fileark.*"%>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
if (!privilege.isUserPrivValid(request, "read")) {
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

//String userName = privilege.getUser(request);
int curpage = ParamUtil.getInt(request, "CPages", 1);
String op = ParamUtil.get(request, "op");

String type = ParamUtil.get(request, "type");
int doc_id = ParamUtil.getInt(request, "id", -1);

// ID标识非法
if (doc_id==-1) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id") + " doc_id=" + doc_id));
	return;
}

Document doc = new Document();
doc = doc.getDocument(doc_id);
if (doc==null || !doc.isLoaded()) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id") + " doc_id=" + doc_id));
	return;
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "name";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
String sql = "SELECT distinct user_name FROM doc_log where doc_id=" + doc_id + " and user_name<>" + StrUtil.sqlstr(UserDb.SYSTEM);

int pagesize = ParamUtil.getInt(request, "pageSize", 20);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>签读</title>
<%@ include file="../inc/nocache.jsp"%>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />

<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>

<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<body>

<%@ include file="doc_log_menu_top.jsp"%>
<script>
o("menu4").className="current";
</script>
<div class="spacerH"></div>
<%
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = jt.executeQuery(sql);
StringBuffer sb = new StringBuffer();
while (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	sb.append(",");
	sb.append(rr.getString(1));
}
String usersReaded = sb.toString();
if (!"".equals(usersReaded))
	usersReaded += ",";
	
Vector v = DocPriv.getUsersCanSee(doc_id);	
Iterator ir = v.iterator();	
%>
      <table width="93%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98">
      <thead>
        <tr>
          <td class="tabStyle_1_title" width="5%" align="center" name="name" ><span class="right-title">
            <input name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" />
          </span></td>
          <td class="tabStyle_1_title"  width="61%" name="name" >用户</td> 
          <td class="tabStyle_1_title"  width="34%" name="log_date" >操作</td> 
        </tr>
      </thead>
      <tbody>
<%
UserDb user = new UserDb();
int count = 0;
while(ir.hasNext()) {
	user = (UserDb)ir.next();
	if (user == null || !user.isLoaded()) {
		continue;
	}
	if (user.getName().equals(UserDb.SYSTEM)) {
		continue;
	}
	String userName = user.getName();
	if (usersReaded.indexOf("," + userName + ",")!=-1) {
		continue;
	}
	count++;
	String realName = user.getRealName();
%>
        <tr >
          <td align="center"><input type="checkbox" id="ids" name="ids" value="<%=user.getName()%>" /></td>
		  <td><%=user.getRealName()%></td> 
          <td align="center"><a href="javascript:;" onclick="send('<%=user.getName()%>')">提醒用户</a></td>
        </tr>
<%}%>
        <tr >
          <td colspan="3"><input class="btn" style="margin-left:3px" name="button2" type="button" onclick="sendBatch()" value="提醒用户" />
          &nbsp;&nbsp;共<%=count%>人未读</td>
        </tr>
	</tbody>
</table>  
<%
	String querystr = "op=" + op;
	// out.print(paginator.getPageBlock(request,"notice_list.jsp?"+querystr));
%>
<form name="hidForm" action="../message_oa/message_frame.jsp" method="post">
<%
Leaf lf = new Leaf();
lf = lf.getLeaf(doc.getDirCode());
%>
<input name="op" type="hidden" value="send" />
<input name="title" type="hidden" value="请查看文档：<%=doc.getTitle()%>" />
<input name="content" type="hidden" value="该文档所在目录：<a href=<%=request.getContextPath()%>/fileark/document_list_m.jsp?dirCode=<%=StrUtil.UrlEncode(doc.getDirCode())%>><%=lf.getName()%></a>，点击此处<a href=<%=request.getContextPath()%>/doc_show.jsp?id=<%=doc_id%>>浏览文档</a>" />
<input id="receiver" name="receiver" type="hidden" />
</form>

</body>
<script>
function selAllCheckBox(checkboxname){
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function deSelAllCheckBox(checkboxname) {
  var checkboxboxs = document.getElementsByName(checkboxname);
  if (checkboxboxs!=null)
  {
	  if (checkboxboxs.length==null) {
	  checkboxboxs.checked = false;
	  }
	  for (i=0; i<checkboxboxs.length; i++)
	  {
		  checkboxboxs[i].checked = false;
	  }
  }
}

function send(userName) {
	hidForm.receiver.value = userName;
	hidForm.submit();
}

function sendBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请先选择用户！","提示");
		return;
	}

	hidForm.receiver.value = ids;
	hidForm.submit();
}
</script>
</html>