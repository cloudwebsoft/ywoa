<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" isErrorPage="true"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.net.*"%>
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

int id = 0;
String dirCode = ParamUtil.get(request, "dir_code");
boolean isDirArticle = false;
Leaf lf = new Leaf();

Document doc = null;
DocumentMgr docmgr = new DocumentMgr();
UserMgr um = new UserMgr();

if (!dirCode.equals("")) {
	lf = lf.getLeaf(dirCode);
	if (lf!=null) {
		if (lf.getType()==1) {
			// id = lf.getDocID();
			doc = docmgr.getDocumentByCode(request, dirCode, privilege);
			id = doc.getID();
			isDirArticle = true;
		}
	}
}

if (id==0) {
	try {
		id = ParamUtil.getInt(request, "id");
		doc = docmgr.getDocument(id);
	}
	catch (ErrMsgException e) {
		out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
		return;
	}
}

if (!doc.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "该文章不存在！"));
	return;
}
if (!isDirArticle)
	lf = lf.getLeaf(doc.getDirCode());

String CPages = ParamUtil.get(request, "CPages");
int pageNum = 1;
if (StrUtil.isNumeric(CPages))
	pageNum = Integer.parseInt(CPages);
%>
<%
  int pid = ParamUtil.getInt(request, "id");
  int attId = ParamUtil.getInt(request, "attachId");
  int pageNums = 1;
  String pn = ParamUtil.get(request, "pageNum");
  if (StrUtil.isNumeric(pn))
  	pageNum = Integer.parseInt(pn);

  Document mmd = new Document();
  mmd = mmd.getDocument(pid);
  Attachment att = mmd.getAttachment(pageNums, attId);
  
  String s = Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();
  
  String previewfile=s;
  String htmlfile=s.substring(0,previewfile.lastIndexOf("."))+".html";
  String showpath=request.getContextPath()+"/"+att.getVisualPath() + "/"+htmlfile.substring(htmlfile.lastIndexOf("/")+1);
	
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=Global.AppName%> - <%=doc.getTitle()%></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="inc/common.js"></script>
<script src="js/jquery.js"></script>
<script type="text/javascript" src="ckeditor/ckeditor.js" mce_src="ckeditor/ckeditor.js"></script>
<script tyle="text/javascript" language="javascript" src="spwhitepad/createShapes.js"></script>
<script> 
var isLeftMenuShow = true;
function closeLeftMenu() {
	if (isLeftMenuShow) {
		window.parent.setCols("0,*");
		isLeftMenuShow = false;
		btnName.innerHTML = "打开菜单";
	}
	else {
		window.parent.setCols("200,*");
		isLeftMenuShow = true;
		btnName.innerHTML = "关闭菜单";		
	}
}
</script>
</head>
<body>
 
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td width="74%" class="tdStyle_1">
	<%
	String pageUrl = "fileark/fileark_main.jsp?";
	if (lf.getCode().indexOf("cws_prj_")==0) {
		String projectId = lf.getCode().substring(8);
		pageUrl = "project/project_doc_list.jsp?projectId=" + projectId;
	}
	%>
	<a href="<%=pageUrl%>&dir_code=<%=StrUtil.UrlEncode(lf.getCode())%>"><%=lf.getName()%></a>
      <script>
		if (typeof(window.parent.leftFileFrame)=="object"){
			var btnN = "关闭菜单";
			if (window.parent.getCols()!="200,*"){
				btnN = "打开菜单";
				isLeftMenuShow=false;
			}
			document.write("&nbsp;&nbsp;<a href=\"javascript:closeLeftMenu()\"><span id=\"btnName\">");
			document.write(btnN);
			document.write("</span></a>");
		}
		</script></td>
    <td width="26%" align="right" class="tdStyle_1">
      
	</td>
  </tr>
</table>
<table cellSpacing="0" class="percent98" cellPadding="5" width="100%" align="center" border="0">
  <tbody>
    <tr>
      <td height="39" align="center"><%if (doc.isLoaded()) {%>
          <b><font size="3"> <%=doc.getTitle()%></font></b>&nbsp; </td>
    </tr>
  </tbody>
</table>
<table width="100%" align="center" class="percent98">
    <tr>
      <td height="22" align="right" bgcolor="#e4e4e4"><%if (!doc.getAuthor().equals("")){%>
        作者：<%=doc.getAuthor()%>&nbsp;
        <%}%>
        &nbsp;&nbsp;日期：<%=doc.getModifiedDate()%>&nbsp;&nbsp;访问次数：<%=doc.getHit()%>
        <%}else{%>
        未找到该文章！
        <%}%>
      &nbsp;&nbsp;&nbsp;&nbsp;</td>
    </tr>
</table>

 

 
<br>
<br>
 
<table width="98%"  border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td align="left"></td>
  </tr>
</table>

 <div style="width:100%; height:550px; text-align: center;background-color:#fff; padding:5px 0px;overflow: hidden;">
  <iframe src="<%=showpath%>" width="95%" height="545" style=" border: 0px; text-align: center;"></iframe>
 </div>
 
</body>
</html>

