<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.cloudwebsoft.framework.base.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.forum.*"%>
<%@ page import = "com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>日程列表</title>
<link href="default.css" rel="stylesheet" type="text/css">
<%@ include file="../inc/nocache.jsp"%>
<script src="../../inc/common.js"></script>
<script language=javascript>
<!--
function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

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
//-->
</script>
</head>
<body background="" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

TagDb td = new TagDb();
		
String op = ParamUtil.get(request, "op");
if (op.equals("add")) {
	QObjectMgr qom = new QObjectMgr();
	try {
		if (qom.create(request, td, "sq_tag_create")) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "tag_list.jsp"));
			return;
		}
		else {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage() + " 请检查标签是否已存在!"));
		return;
	}
}
else if (op.equals("edit")) {
	long id = ParamUtil.getLong(request, "id");
	td = (TagDb)td.getQObjectDb(new Long(id));
	QObjectMgr qom = new QObjectMgr();
	try {
		if (qom.save(request, td, "sq_tag_save"))
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "tag_list.jsp"));
		else
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));	
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
}
else if (op.equals("del")) {
	long id = ParamUtil.getLong(request, "id");
	td = (TagDb)td.getQObjectDb(new Long(id));
	boolean re = td.del();
	if (re)
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "tag_list.jsp"));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	return;
}
else if (op.equals("delBatch")) {
	String strIds = ParamUtil.get(request, "ids");
	String[] ids = StrUtil.split(strIds, ",");
	if (ids!=null) {
		for (int i=0; i<ids.length; i++) {
			long delId = StrUtil.toLong(ids[i]);
			td = (TagDb)td.getQObjectDb(new Long(delId));
			td.del();
		}
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "tag_list.jsp"));
		return;
	}
}
		String sql;
		sql = "select id from " + td.getTable().getName() + " order by is_system desc, orders desc, count desc";
		int pagesize = 20;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		
		ListResult lr = td.listResult(new JdbcTemplate(), sql, curpage, pagesize);
		long total = lr.getTotal();
		Vector v = lr.getResult();
	    Iterator ir = null;
		if (v!=null)
			ir = v.iterator();
		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}		
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="head">标签管理</td>
    </tr>
  </tbody>
</table>
<br>
<TABLE 
style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="95%" align=center>
  <!-- Table Head Start-->
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%">&nbsp;</TD>
    </TR>
    <TR class=row style="BACKGROUND-COLOR: #fafafa">
      <TD height="175" align="center" style="PADDING-LEFT: 10px">
	  <table width="100%" border="0" cellspacing="0" cellpadding="0">
	  <form name="formAdd" action="tag_list.jsp?op=add" method="post">
        <tr>
          <td align="center">系统标签
            <input name="name"><input type="submit" value="添加">
            <input name="is_system" value="1" type="hidden">
            <input name="user_name" value="<%=TagDb.USER_NAME_SYSTEM%>" type="hidden"></td>
        </tr></form>
      </table>
      <table width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
          <tr>
            <td align="right"> 找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %> </td>
          </tr>
        </table>
        <table width="98%" border="0" align="center" cellpadding="2" cellspacing="0" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
          <tr>
            <td width="2%" align="center" class="thead">
              <input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" />
            </td>
            <td width="15%" align="left" class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">名称</td>
            <td width="9%" align="left" class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">排序号</td>
            <td width="8%" align="left" class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">数量</td>
            <td width="8%" align="left" class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">颜色</td>
            <td width="6%" align="left" class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">加粗</td>
            <td width="6%" align="left" class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">字号</td>
            <td width="7%" align="left" class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">系统标签</td>
            <td width="11%" align="left" class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">创建者</td>
            <td width="11%" align="left" class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">创建日期</td>
            <td width="17%" align="left" class="thead"><img src="images/tl.gif" align="absMiddle" width="10" height="15">操 作</td>
          </tr>
        <%
		int k = 0;
		UserMgr um = new UserMgr();
		while (ir.hasNext()) {
			k++;
			td = (TagDb)ir.next();
		%>
		<form name="form<%=k%>" action="tag_list.jsp?op=edit" method="post">
          <tr>
            <td align="left">&nbsp;              <input type="checkbox" name="ids" value="<%=td.getLong("id")%>"></td>
            <td height="22" align="left">
			<input name="name" value="<%=td.get("name")%>">
			<input name="id" value="<%=td.getLong("id")%>" type="hidden"></td>
            <td align="left"><input name="orders" value="<%=td.get("orders")%>" size="3"></td>
            <td align="left">&nbsp;&nbsp;
            <input name="count" value="<%=td.getInt("count")%>" type="hidden"><%=td.getInt("count")%></td>
            <td align="left">
				<select name="color">
                  <option value="" style="COLOR: black" selected>&nbsp;无&nbsp;</option>
                  <option style="BACKGROUND: #000088" value="#000088"></option>
                  <option style="BACKGROUND: #0000ff" value="#0000ff"></option>
                  <option style="BACKGROUND: #008800" value="#008800"></option>
                  <option style="BACKGROUND: #008888" value="#008888"></option>
                  <option style="BACKGROUND: #0088ff" value="#0088ff"></option>
                  <option style="BACKGROUND: #00a010" value="#00a010"></option>
                  <option style="BACKGROUND: #1100ff" value="#1100ff"></option>
                  <option style="BACKGROUND: #111111" value="#111111"></option>
                  <option style="BACKGROUND: #333333" value="#333333"></option>
                  <option style="BACKGROUND: #50b000" value="#50b000"></option>
                  <option style="BACKGROUND: #880000" value="#880000"></option>
                  <option style="BACKGROUND: #8800ff" value="#8800ff"></option>
                  <option style="BACKGROUND: #888800" value="#888800"></option>
                  <option style="BACKGROUND: #888888" value="#888888"></option>
                  <option style="BACKGROUND: #8888ff" value="#8888ff"></option>
                  <option style="BACKGROUND: #aa00cc" value="#aa00cc"></option>
                  <option style="BACKGROUND: #aaaa00" value="#aaaa00"></option>
                  <option style="BACKGROUND: #ccaa00" value="#ccaa00"></option>
                  <option style="BACKGROUND: #ff0000" value="#ff0000"></option>
                  <option style="BACKGROUND: #ff0088" value="#ff0088"></option>
                  <option style="BACKGROUND: #ff00ff" value="#ff00ff"></option>
                  <option style="BACKGROUND: #ff8800" value="#ff8800"></option>
                  <option style="BACKGROUND: #ff0005" value="#ff0005"></option>
                  <option style="BACKGROUND: #ff88ff" value="#ff88ff"></option>
                  <option style="BACKGROUND: #ee0005" value="#ee0005"></option>
                  <option style="BACKGROUND: #ee01ff" value="#ee01ff"></option>
                  <option style="BACKGROUND: #3388aa" value="#3388aa"></option>
                  <option style="BACKGROUND: #000000" value="#000000"></option>
                </select>
			<script>
			form<%=k%>.color.value = "<%=StrUtil.getNullStr(td.getString("color"))%>";
			</script>            
            </td>
            <td align="left">
            <input name="is_bold" value="1" type="checkbox" <%=td.getInt("is_bold")==1?"checked":""%> />           
            </td>
            <td align="left">
            <select id="font_size" name="font_size">
            <option value="">无</option>
            <option value="14px">14px</option>
            <option value="15px">15px</option>
            <option value="16px">16px</option>
            <option value="17px">17px</option>
            <option value="18px">18px</option>
            </select>
            <script>
			form<%=k%>.font_size.value = "<%=StrUtil.getNullStr(td.getString("font_size"))%>";
			</script>
            </td>
            <td align="left">
            <input name="is_system" value="1" type="checkbox" <%=td.getInt("is_system")==1?"checked":""%> />           
			</td>
            <td align="left">
			<%
			String userName = td.getString("user_name");
			if (!userName.equals(TagDb.USER_NAME_SYSTEM)) {
				UserDb ud = um.getUser(userName);
				out.print("<a target=_blank href='../../userinfo.jsp?username=" + StrUtil.UrlEncode(ud.getName()) + "'>" + ud.getNick() + "</a>");
			}
			else
				out.print("系统");
			%>
			</td>
            <td align="left"><%=DateUtil.format(td.getDate("create_date"), "yyyy-MM-dd")%></td>
            <td align="center"><a href="javascript:form<%=k%>.submit()">修改</a>　<a href="javascript:if (confirm('您确定要删除吗？')) window.location.href='tag_list.jsp?op=del&id=<%=td.getLong("id")%>'">删除</a>&nbsp;&nbsp;&nbsp;<a href="../listtag.jsp?tagId=<%=td.getLong("id")%>" target="_blank">查看</a></td>
          </tr></form>
        <%}%>
        </table>		
        <br>
        <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
          <tr>
            <td width="51%" height="23" align="left">
            <input type="button" value="删除" onClick="delBatch()" /></td>
            <td width="49%" align="right"><%
				String querystr = "";
				out.print(paginator.getCurPageBlock("?"+querystr));
				%></td>
          </tr>
        </table>
      <br></TD>
    </TR>
    <!-- Table Body End -->
    <!-- Table Foot -->
    <TR>
      <TD class=tfoot align=right>&nbsp;</TD>
    </TR>
    <!-- Table Foot -->
  </TBODY>
</TABLE>
<br>
</body>
<script>
function delBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择标签！");
		return;
	}
	if (confirm("您确定要删除么？"))
		window.location.href = "tag_list.jsp?op=delBatch&ids=" + ids;
}
</script>
</html>
