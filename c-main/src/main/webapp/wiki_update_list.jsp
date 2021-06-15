<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%
int id = ParamUtil.getInt(request, "id", -1);
String dirCode = ParamUtil.get(request, "dir_code");
boolean isDirArticle = false;
Leaf lf = new Leaf();

if (!dirCode.equals("")) {
	lf = lf.getLeaf(dirCode);
	if (lf!=null) {
		if (lf.getType()==1) {
			id = lf.getDocID();
			isDirArticle = true;
		}
	}
}

if (id==-1) {
	out.print(SkinUtil.makeErrMsg(request, "id格式错误！"));
	return;
}

Document doc = null;
DocumentMgr docmgr = new DocumentMgr();
doc = docmgr.getDocument(id);
if (doc==null || !doc.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该文章不存在！"));
	return;
}

if (!isDirArticle)
	lf = lf.getLeaf(doc.getDirCode());

String CPages = ParamUtil.get(request, "CPages");
int pageNum = 1;
if (StrUtil.isNumeric(CPages))
	pageNum = Integer.parseInt(CPages);

String op = ParamUtil.get(request, "op");
String view = ParamUtil.get(request, "view");
CommentMgr cm = new CommentMgr();
if (op.equals("addcomment")) {
	try {
		cm.insert(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=doc.getTitle()%> - <%=Global.AppName%></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<style type="text/css">
<!--
.wikiTable td {
	border-bottom:1px dashed #cccccc;
}
-->
</style>
</head>
<body>
<div class="content">
<div align="center" style="text-align:left">
  <table width="100%" height="401" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td class="tdStyle_1"><div style="font-family:'宋体'">
            <%
		String navstr = "";
		String parentcode = lf.getParentCode();
		com.redmoon.oa.fileark.Leaf plf = new com.redmoon.oa.fileark.Leaf();
		while (!parentcode.equals("root")) {
			plf = plf.getLeaf(parentcode);
			if (plf.getType()==com.redmoon.oa.fileark.Leaf.TYPE_LIST)
				navstr = "<a href='doc_list.jsp?op=list&dirCode=" + StrUtil.UrlEncode(plf.getCode()) + "'>" + plf.getName() + "</a>&nbsp;>>&nbsp;" + navstr;
			else
				navstr = plf.getName() + "</a>&nbsp;>>&nbsp;" + navstr;
			
			parentcode = plf.getParentCode();
			// System.out.println(parentcode + ":" + plf.getName() + " leaf name=" + lf.getName());
		}
		if (lf.getType()==com.redmoon.oa.fileark.Leaf.TYPE_LIST) {
			out.print(navstr + "<a href='doc_list.jsp?op=list&dirCode=" + StrUtil.UrlEncode(lf.getCode()) + "'>" + lf.getName() + "</a>");
		}
		else
			out.print(navstr + lf.getName());
		%>
      </div></td>
    </tr>
    <tr>
      <td height="317" colspan="41" valign="top" style="padding:5px"><table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr style="border-bottom:dashed #cccccc solid"><td width="7%" height="22" align="center"><input class="btn" type="button" value="版本对比" onClick="compare()" /></td>
          <td width="21%" align="center"><strong>更新时间</strong></td>
          <td width="6%" align="center"><strong>版本</strong></td>
          <td width="13%" align="center"><strong>贡献者</strong></td>
          <td width="53%" align="left"><strong>修改原因</strong></td>
        </tr>
      <%
	  UserMgr um = new UserMgr();
	  WikiDocUpdateDb wdud = new WikiDocUpdateDb();
	  // out.print(wdud.getTable().getSql("listUpdateHistory"));
	  Iterator ir = wdud.list(wdud.getTable().getSql("listUpdateHistory"), new Object[]{new Integer(id)}).iterator();
	  while (ir.hasNext()) {
	  	wdud = (WikiDocUpdateDb)ir.next();
		String editor = wdud.getString("user_name");
		UserDb user = um.getUserDb(editor);
		%>
          <tr class="wikiTable">
            <td height="25" align="center"><input id="<%=wdud.getLong("id")%>" name="ids" type="checkbox" value="<%=wdud.getLong("id")%>" /><%=wdud.getLong("id")%></td>
            <td align="center"><%=DateUtil.format(wdud.getDate("edit_date"), "yyyy-MM-dd HH:mm")%></td>
            <td align="center"><a href="wiki_show.jsp?id=<%=id%>&pageNum=<%=wdud.getInt("page_num")%>">查看</a></td>
            <td align="center"><a href="javascript:;" onclick="addTab('用户信息', '<%=request.getContextPath()%>/user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%></a></td>
            <td><%=StrUtil.toHtml(wdud.getString("reason"))%>&nbsp;</td>
          </tr>
        <%
	  }
	  %>
      </table>
	  </td>
    </tr>
  </table>
</div>
</div>
</body>
<script>
var c1;
var c2;
$(document).ready(function() { 
	$("input[name='ids']").bind("click", function() {
		if ($(this)[0].checked) {
			if (c1==null) {
				if (c2!=null) {
					if (c2.id!=$(this)[0].id)
						c1 = $(this)[0];
				}
				else
					c1 = $(this)[0];
			}
			else if (c2==null) {
				if (c1!=null) {
					if (c1.id!=$(this)[0].id)
						c2 = $(this)[0];
				}
				else
					c2 = $(this)[0];
			}
			else {
				c1.checked = false;
				c1 = c2;
				c2 = $(this)[0];
			}
		}
		else {
			// alert($(this)[0].id + " " + $(this)[0].checked);
			if (c1 && $(this)[0].id==c1.id)
				c1 = null;
			else if (c2 && $(this)[0].id==c2.id)
				c2 = null;
		}
		/*
		var s = "";
		if (c1) {
			s += c1.value + " " + c1.checked;
		}
		else
			s += "null";
		if (c2) {
			s += "," + c2.value + " " + c2.checked;
		}
		else
			s += ",null";
		window.status = s;
		*/
	});
}
);

function compare() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		alert("请选择需要对比的记录！");
		return;
	}
	var ary = ids.split(",");
	if (ary.length!=2) {
		alert("请选择两条记录进行对比！");
		return;
	}
	addTab("版本对比", "<%=request.getContextPath()%>/wiki_compare.jsp?id=<%=id%>&ids=" + ids);
}
</script>
</html>