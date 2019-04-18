<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import = "java.io.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.kernel.License"%>
<%@ taglib uri="/WEB-INF/tlds/HelpDocTag.tld" prefix="help" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="noticeDb" scope="page" class="com.redmoon.oa.notice.NoticeDb"/>
<jsp:useBean id="deptUserDb" scope="page" class="com.redmoon.oa.dept.DeptUserDb"/>
<%
int paperId = ParamUtil.getInt(request, "paperId", -1);
PaperDistributeDb pdd = new PaperDistributeDb();
pdd = pdd.getPaperDistributeDb(paperId);
if (pdd==null) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
	return;
}

com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
if (!privilege.isUserPrivValid(request, "paper.receive")) {
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();

String userName = privilege.getUser(request);
int curpage = ParamUtil.getInt(request, "CPages", 1);
String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>
<%
String kind = License.getInstance().getKind();
if (kind.equalsIgnoreCase(License.KIND_COM)) {
%>
	<lt:Label res="res.flow.Flow" key="notify"/>
<%
}
else {
%>
	<lt:Label res="res.flow.Flow" key="distribute"/>
<%
}
%>
</title>
<%@ include file="../inc/nocache.jsp"%>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
</head>
<body>
<%
// String unitCode = privilege.getUserUnitCode(request);
boolean canSee = false;
if (pdd.getInt("kind")==PaperDistributeDb.KIND_UNIT) {
	DeptUserDb dud = new DeptUserDb();
	String[] ary = dud.getUnitsOfUser(userName);
	for (int i=0; i<ary.length; i++) {
		if (pdd.getString("to_unit").equals(ary[i])) {
			canSee = true;
			break;
		}
	}
}
else {
	if (pdd.getString("to_unit").equals(userName)) {
		canSee = true;
	}
}

if (!canSee) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

// 置公文为已读状态
if (pdd.getInt("is_readed")==0) {
	pdd.set("is_readed", new Integer(1));
	pdd.set("read_date", new java.util.Date());
	pdd.save();

    // 置日程为关闭状态
	PlanDb pd = new PlanDb();
	pd = pd.getPlanDb(userName, PlanDb.ACTION_TYPE_PAPER_DISTRIBUTE, String.valueOf(paperId));
	if (pd!=null) {
		pd.setClosed(true);
		pd.save();
	}	
}

int flowId = pdd.getInt("flow");
WorkflowDb wf = new WorkflowDb();
wf = wf.getWorkflowDb(flowId);
int doc_id = wf.getDocId();
DocumentMgr dm = new DocumentMgr();
Document doc = dm.getDocument(doc_id);
if (doc==null) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "文件不存在！"));
	return;
}
%>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">查看文件</td>
    </tr>
  </tbody>
</table>
<table width="332" border="0" align="center" class="tabStyle_1 percent98">
  <tr>
    <td class="tabStyle_1_title"><%=pdd.getString("title")%></td>
  </tr>
  <tr>
    <td>
    <%
	java.util.Vector attachments = doc.getAttachments(1);
	java.util.Iterator ir = attachments.iterator();
	if (ir.hasNext()) {
		Attachment att = (Attachment) ir.next();
		String fileName = att.getDiskName();
		String fName = fileName.substring(0, fileName.lastIndexOf("."));
		fileName = fName + ".pdf";
		
		// 在flow_dispose_to.jsp中已生成，这儿为了保险起见，再检测一下
		String fileDiskPath = cn.js.fan.web.Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();
		String pdfPath = cn.js.fan.web.Global.getRealPath() + att.getVisualPath() + "/" + fileName;
		File f = new File(pdfPath);
		if (!f.exists()) {
			com.redmoon.oa.util.PDFConverter.convert2PDF(fileDiskPath, pdfPath);
		}
	%>
    <object classid="clsid:CA8A9780-280D-11CF-A24D-444553540000" width="100%" height="768" border="0">  
      <param name="_Version" value="65539"> 
      <param name="_ExtentX" value="20108"> 
      <param name="_ExtentY" value="10866">
      <param name="_StockProps" value="0"> 
      <param name="SRC" value="<%=request.getContextPath()%>/<%=att.getVisualPath() + "/" + fileName%>">
	</object>
    <%}%>
    
    </td>
  </tr>
</table>

<table class="tabStyle_1 percent98" width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td height="31" align="right" class="tabStyle_1_title">&nbsp;</td>
    <td class="tabStyle_1_title">文件名</td>
    <td align="center" class="tabStyle_1_title">大小</td>
    <td align="center" class="tabStyle_1_title">操作</td>
  </tr>
  <%
  com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();  
  ir = attachments.iterator();  
  while (ir.hasNext()) {
    Attachment att = (Attachment) ir.next();
	String attName = att.getName();
    if ("true".equals(cfg.get("canConvertToPDF")) && (StrUtil.getFileExt(att.getName()).equals("doc") || StrUtil.getFileExt(att.getName()).equals("docx"))) {
		int p = attName.lastIndexOf(".");
		attName = attName.substring(0, p) + ".pdf";
	}
  %>
  <tr>
    <td width="2%" height="31" align="center"><img src="../images/attach.gif" /></td>
    <td width="51%" align="left">
    &nbsp;<span id="spanAttLink<%=att.getId()%>"><span id="spanAttName<%=att.getId()%>"><%=attName%></span></span> <span id="spanAttNameInput<%=att.getId()%>" style="display:none">
    <input id="attName<%=att.getId()%>" value="<%=StrUtil.HtmlEncode(att.getName())%>" />
    <input class="btn" type="button" value="确定" onclick="renameAtt('<%=att.getId()%>')" />
    </span> &nbsp;&nbsp;<span id="spanRename<%=att.getId()%>" style="display:none;color:#aaaaaa;cursor:pointer" onclick="changeName('<%=att.getId()%>')">改名</span></td>
    <td width="11%" align="center"><%=NumberUtil.round((double)att.getSize()/1024000, 2)%>M</td>
    <td width="11%" align="center">
	<%
    if ("true".equals(cfg.get("canConvertToPDF")) && (StrUtil.getFileExt(att.getName()).equals("doc") || StrUtil.getFileExt(att.getName()).equals("docx"))) {
        %>
        &nbsp;&nbsp;<a href="../flow_getfile.jsp?op=toPDF&flowId=<%=wf.getId()%>&attachId=<%=att.getId()%>" target="_blank">下载</a>
        <%
    }
	else {
		%>
    <a href="../flow_getfile.jsp?attachId=<%=att.getId()%>&amp;flowId=<%=wf.getId()%>" target="_blank">下载</span></a>
		<%
	}
    %>    
	</td>
  </tr>
  <%}%>
</table>
</body>
</html>