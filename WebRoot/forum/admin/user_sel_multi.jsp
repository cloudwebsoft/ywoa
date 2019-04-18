<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.module.pvg.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
String mode = ParamUtil.get(request, "mode");
String what = ParamUtil.get(request, "what");
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "RegDate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
%>
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<title><lt:Label res="res.label.forum.admin.forum_user_sel" key="select_user"/></title>
<link href="default.css" rel="stylesheet" type="text/css">
<script src="../../inc/common.js"></script>
<script language="JavaScript">
<!--
function setPerson(userName, userRealName) {
	window.opener.setPerson(userName, userRealName);
	window.close();
}

function selAllCheckBox(checkboxname){
  var checkboxboxs = document.all.item(checkboxname);
  if (checkboxboxs!=null)
  {
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
  var checkboxboxs = document.all.item(checkboxname);
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

function sel() {
	var names = getCheckboxValue("names");
	if (names=="") {
		alert("请选择用户！");
		return;
	}
	if (!confirm("您确定要选择么？"))
		return;
	window.opener.selPerson(names);
	window.close();
}
//-->
</script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
body {
	margin-right: 0px;
	margin-bottom: 0px;
}
.STYLE2 {color: #000000}
-->
</style>
<body bgcolor="#FFFFFF" leftmargin='0' topmargin='5'>
<%
String groupCode = ParamUtil.get(request, "groupCode");
if (groupCode.equals(""))
	groupCode = "cms";
String name = ParamUtil.get(request, "name");
%>
<form name="form1" method="get" action="?">
<table width="90%" border="0" align="center">
    <tr>
      <td height="25" align="center"> 用户组：
        <select name="groupCode">
          <%
			UserGroupDb ugroup = new UserGroupDb();
			Vector result = ugroup.list();
			Iterator ir1 = result.iterator();
			String opts = "";
			while (ir1.hasNext()) {
				ugroup = (UserGroupDb) ir1.next();
				if (ugroup.getCode().equals(UserGroupDb.EVERYONE))
					continue;
				opts += "<option value=" + ugroup.getCode() + ">" + ugroup.getDesc() + "</option>";
			}
			%>
          <option value="cms">后台用户</option>
          <option value="all">
            <lt:Label res="res.label.forum.admin.user_m" key="all"/>
          </option>
          <%=opts%>
        </select>
        <script>
			form1.groupCode.value = "<%=groupCode%>";
			</script>
        <lt:Label res="res.label.forum.admin.forum_user_sel" key="input_nick"/>
        ：
        <input type="text" name="what" style="height:18px;width:100px" value="<%=what%>">
        <input name="op" value="search" type="hidden" />
        &nbsp;
        <input type="submit" name="Submit" value="<lt:Label res="res.label.forum.admin.forum_user_sel" key="search"/>"></td>
    </tr>
</table>
</form>
<TABLE style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="100%" align=center>
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%"><font size="-1"><b><lt:Label res="res.label.forum.admin.forum_user_sel" key="select_user"/></b></font> </TD>
    </TR>
    <TR>
      <TD height="175" align="center" bgcolor="#FFFFFF" style="PADDING-LEFT: 10px"><%
		String sql = "select name from sq_user";
	  	String op = ParamUtil.get(request, "op");
	  	if (op.equals("search")) {
			if(groupCode.equals("cms")){
				sql = "select name from sq_user where nick like "+StrUtil.sqlstr("%"+what+"%");
				sql += " order by name";
			}else{
				if (groupCode.equals("all"))
					sql = "select name from sq_user where nick like "+StrUtil.sqlstr("%"+what+"%");
				else
					sql = "select name from sq_user where nick like "+StrUtil.sqlstr("%"+what+"%")+" and group_code=" + StrUtil.sqlstr(groupCode);
				sql += " order by " + orderBy + " " + sort;
			}	
		}
		else {
			// 后台用户
			if(groupCode.equals("cms")){
				sql = "select name from sq_user where nick like "+StrUtil.sqlstr("%"+what+"%");
				sql += " order by name";
			}else{
				sql += " order by " + orderBy + " " + sort;	
			}	
		}
	//com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).info("sql=" + sql);
		 //out.print(sql);
		ListResult lr=null;
		int pagesize = 200;
		
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		User user = new User();
		UserDb ud = new UserDb();
		
		/*
		if(groupCode.equals("cms")){
			lr = ud.listResult(sql, curpage, pagesize);
		}else{
			lr = ud.listResult(sql, curpage, pagesize);
		}
		*/
		
		lr = ud.listResult(sql, curpage, pagesize);
		
	    int total = lr.getTotal();
		Iterator ir = lr.getResult().iterator();

		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}	
			
		int i = 1;
		
%>
        <table width="90%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td align="right"><span class="title1"><%=paginator.getPageStatics(request)%></span></td>
          </tr>
        </table>
        <TABLE class="frame_gray" cellPadding=3 width="98%" align=center>
          <TBODY>
            <TR><td>
        <ul style="float:left">
		  	<%
			while(ir.hasNext()){
				if(groupCode.equals("cms")){
					ud = (UserDb)ir.next();
					// ud = ud.getUserDbByNick(user.getName());
				}else{
					ud = (UserDb)ir.next();
				}
			%>
            <li style="float:left;width:150px;margin-right:20px"><input type="checkbox" name="names" value="<%=ud.getName()%>" />
            <a href="../../userinfo.jsp?username=<%=ud.getName()%>" target="_blank"><%=ud.getNick()%></a>
            <%}%>
            </li>
        </ul>
        </td>
        </TR>
        </TBODY>
        </TABLE>
        <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
          <tr>
            <td width="52%" height="23" align="left"><input type="button" value="全选" onClick="selAllCheckBox('names')" >
			<input type="button" value="全不选" onClick="deSelAllCheckBox('names')" >
			<input type="button" value="选择" onClick="sel()" ></td>
            <td width="48%" align="right"><%
	String querystr = "op=" + op + "&name=" + StrUtil.UrlEncode(name) + "&groupCode=" + StrUtil.UrlEncode(groupCode);
    out.print(paginator.getCurPageBlock("?"+querystr));
%></td>
          </tr>
        </table>
        <br>
      <p> </TD>
    </TR>
    <!-- Table Body End -->
    <!-- Table Foot -->
    <TR>
      <TD class=tfoot align=right><DIV align=right> </DIV></TD>
    </TR>
    <!-- Table Foot -->
  </TBODY>
</TABLE>
</body>
</html>                            
  