<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%
int id = ParamUtil.getInt(request, "id", -1);
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

String dirCode = doc.getDirCode();
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

String ids = ParamUtil.get(request, "ids");
String[] ary = StrUtil.split(ids, ",");
if (ary==null || ary.length<2) {
	out.print(SkinUtil.makeErrMsg(request, "请选择两条记录！"));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=doc.getTitle()%> - <%=Global.AppName%></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script src="inc/common.js"></script>
<script src="js/compare.js"></script>
<script type="text/javascript">
//滚动条同步
function DriveRightScroll() {
	var RightDivObj = document.getElementById('contrastt_right');
	var LeftDivObj = document.getElementById('contrastt_left');
	if (LeftDivObj.scrollTop < (RightDivObj.scrollHeight - RightDivObj.clientHeight))
	{
	  RightDivObj.scrollTop = LeftDivObj.scrollTop;
	  RightDivObj.scrollLeft = LeftDivObj.scrollLeft;
	}
}
function DriveLeftScroll() {
	var RightDivObj = document.getElementById('contrastt_right');
	var LeftDivObj = document.getElementById('contrastt_left');
	if (RightDivObj.scrollTop < (LeftDivObj.scrollHeight - LeftDivObj.clientHeight))
	{
	  LeftDivObj.scrollTop = RightDivObj.scrollTop;
	  LeftDivObj.scrollLeft = RightDivObj.scrollLeft;     
	}
}
</script>
</head>
<body>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
<TBODY>
<TR>
<TD class="tdStyle_1">
    <div style="font-family:'宋体'">
      <%
		String navstr = "";
		String parentcode = lf.getParentCode();
		com.redmoon.oa.fileark.Leaf plf = new com.redmoon.oa.fileark.Leaf();
		while (!parentcode.equals("root") && !parentcode.equals("-1")) {
			plf = plf.getLeaf(parentcode);
			if (plf==null) {
				break;
			}
			if (plf.getType()==com.redmoon.oa.fileark.Leaf.TYPE_LIST)
				navstr = "<a href='fileark/wiki_list.jsp?op=list&dir_code=" + StrUtil.UrlEncode(plf.getCode()) + "'>" + plf.getName() + "</a>&nbsp;>>&nbsp;" + navstr;
			else
				navstr = plf.getName() + "</a>&nbsp;>>&nbsp;" + navstr;
			
			parentcode = plf.getParentCode();
			// System.out.println(parentcode + ":" + plf.getName() + " leaf name=" + lf.getName());
		}
		if (lf.getType()==com.redmoon.oa.fileark.Leaf.TYPE_LIST) {
			out.print(navstr + "<a href='fileark/wiki_list.jsp?op=list&dir_code=" + StrUtil.UrlEncode(lf.getCode()) + "'>" + lf.getName() + "</a>");
		}
		else
			out.print(navstr + lf.getName());
		%>
    </div>
</TD>
</TR>
</TBODY>
</TABLE>
<div class="content">
  <div align="center" style="text-align:left; clear:both; padding:10px">
    <div id="contentDiv">
      <div id="contrast"></div>
      <%
WikiDocUpdateDb wdud = new WikiDocUpdateDb();
wdud = (WikiDocUpdateDb)wdud.getQObjectDb(new Long(StrUtil.toLong(ary[0])));
int pageNum = wdud.getInt("page_num");
String content = doc.getContent(pageNum);
wdud = (WikiDocUpdateDb)wdud.getQObjectDb(new Long(StrUtil.toLong(ary[1])));
pageNum = wdud.getInt("page_num");
String content2 = doc.getContent(pageNum);
%>
      <div id="contrastt_left" style="width:450px;height:400px; overflow:auto; float:left" onscroll="DriveRightScroll()"> <%=StrUtil.getNullStr(content)%> </div>
      <div id="contrastt_right" style="width:450px;height:400px; overflow:auto; float:left" onscroll="DriveLeftScroll()" > <%=StrUtil.getNullStr(content2)%> </div>
    </div>
    <div style="clear:both"></div>
    <div>
      <ul id="contrast_left">
        <li><strong>注:</strong></li>
        <li>1、浅绿色 表示一个范围</li>
        <li>2、浅紫色 不同点</li>
      </ul>
    </div>
  </div>
</div>
</div>
</body>
<script type="text/javascript">
CompareById('contrastt_left', 'contrastt_right');
</script>
</html>