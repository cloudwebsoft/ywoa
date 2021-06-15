<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="java.util.Iterator"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作报告 - 编辑</title>
<link href="common.css" rel="stylesheet" type="text/css">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<script src="../inc/upload.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script language=javascript>
<!--
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
//-->
</script>
</head>
<body>
<%
String userName = ParamUtil.get(request, "userName");
%>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%@ include file="mywork_nav.jsp"%>
<div class="spacerH"></div>
<%

int id = ParamUtil.getInt(request, "id");
String content="",mydate="";
String sql = "";
String itemType = "";

WorkLogMgr wlm = new WorkLogMgr();
WorkLogDb wld = wlm.getWorkLogDb(request, id);
if (wld!=null && wld.isLoaded()) {
	content = wld.getContent();
	itemType = wld.getItemType();
	mydate = DateUtil.format(wld.getMyDate(), "yyyy-MM-dd");
}

boolean canEditPreviousWorklog = com.redmoon.oa.worklog.Config.getInstance().getBooleanProperty("canEditPreviousWorklog");		
if (canEditPreviousWorklog) {
	int editPreviousWorklogLimit = com.redmoon.oa.worklog.Config.getInstance().getIntProperty("editPreviousWorklogLimit");
	java.util.Date d = DateUtil.addDate(wld.getMyDate(), editPreviousWorklogLimit);
	// System.out.println(DateUtil.format(wld.getMyDate(), "yyyy-MM-dd") + " editPreviousWorklogLimit=" + editPreviousWorklogLimit);
	if (DateUtil.compare(new java.util.Date(), d)==1) {
		out.print(StrUtil.jAlert_Back("报告发布" + editPreviousWorklogLimit + "天后不能被编辑!","提示"));
		return;
	}
}
else {
	out.print(StrUtil.jAlert_Back("以前写的报告不能被编辑!","提示"));
	return;
}

String op = request.getParameter("op");
if (op!=null) {
	boolean re = false;
	try {
		re = wlm.modify(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert(e.getMessage(),"提示"));
	}
	if (re)
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示","mywork_edit.jsp?id="+id));
}

%>
<br>
<form name="form1" action="?op=edit&id=<%=id%>" method="post" onSubmit="return form1_onsubmit()" enctype="multipart/form-data">
<table width="98%" align="center" class="tabStyle_1 percent80">
  
    <tr>
      <td align="center" class="tabStyle_1_title">编辑&nbsp;&nbsp;&nbsp;&nbsp;
      <%if (wld.getLogType()==WorkLogDb.TYPE_NORMAL) {%>
      日期：<%=mydate%>
      <%}else if (wld.getLogType()==WorkLogDb.TYPE_WEEK) {%>
      第<%=wld.getLogItem()%>周
      <%}else{%>
      <%=wld.getLogItem()%>月
      <%}%>
      </td>
    </tr>
    <tr>
      <td>
      	<% 
      		String[] itemTypeArr = itemType.split("a#a");
      		for(int i=0;i<itemTypeArr.length;i++){
      			String itemContent = itemTypeArr[i];
      			String title = itemContent.split(":#")[0];
      			content = itemContent.split(":#")[1];
      			String canNull = itemContent.split(":#")[2];
      			String wordCount = itemContent.split(":#")[3];
      			if(content.equals("NULL")){
      				content = "";
      			}
				      		
      	%>
      	  <div style="padding:5px"><%=title%>:<%=canNull.equals("true")?"":"<font color=red>*</font>"%><%=!wordCount.equals("-1")?"&nbsp;(" + wordCount+"字以上)":""%></div>
      	  <input type="hidden" name="title" id="title" value="<%=title %>"/>
      	  <input type="hidden" name="canNull" id="canNull" value="<%=canNull %>"/>
      	  <input type="hidden" name="wordCount" id="wordCount" value="<%=wordCount %>"/>
      	  <textarea id="content<%=i%>" name="content" style="display:none"><%=content%></textarea>
          <script>
			CKEDITOR.replace('content<%=i%>',
				{
					// skin : 'kama',
					toolbar : 'Middle'
				});
		  </script>
      	
        <%}%>
                      
      </td>
    </tr>
    <tr>
    	<td>
    		<script>initUpload()</script>
    	
    	<% 
				
			WorkLogDb workLogDb = new WorkLogDb();
			workLogDb = workLogDb.getWorkLogDb(id);
			
			Iterator ir1 = workLogDb.getAttachs().iterator();
			
			while (ir1.hasNext()) {
                   WorkLogAttachmentDb workLogAttachmentDb = (WorkLogAttachmentDb)ir1.next();
		%>
		<div><img src="../images/attach2.gif" width="17" height="17" />
	    	<a target="_blank" href="mywork_getfile.jsp?attachId=<%=workLogAttachmentDb.getId()%>"><%=workLogAttachmentDb.getName()%></a>&nbsp;&nbsp;<a href="javascript:delAttach(<%=workLogAttachmentDb.getId()%>,<%=id %>)">删除</a>
	    </div>
	    <%} %>
	  </td>
    </tr>
    <tr>
      <td align="center"><input class="btn" id="btn" name="submit" type="submit" value="确定"></td>
    </tr>
</table>
</form>
<br>
</body>
<script>

function delAttach(workLogAttachId,id){
	jConfirm("您确定要删除吗?","提示",function(r){
		if(!r){return;}
		else{
			window.location.href="myworkedit_do.jsp?op=delAttach&attachId="+workLogAttachId+"&id="+id;
		}
	})
}

function form1_onsubmit() {
	
	<%
		String[] itemTypeArr1 = itemType.split("a#a");
		for(int i=0;i<itemTypeArr1.length;i++){
			String itemContent = itemTypeArr1[i];
			String title = itemContent.split(":#")[0];
			//content = itemContent.split(":#")[1];
			String canNull = itemContent.split(":#")[2];
			String wordCount = itemContent.split(":#")[3];
			
			if(canNull.equals("false")){
	%>
			
			if (CKEDITOR.instances.content<%=i%>.getData().trim()=="") {
				jAlert("请填写<%=title%>！","提示");
				o("btn").disabled = false;
				return false;
			}
		<%}
			if(!wordCount.equals("-1")){
		%>
			
			var txt = CKEDITOR.instances.content<%=i%>.document.getBody().getText();	
			if (strlen(txt) < <%=wordCount%>) {
				jAlert("<%=title%>不能少于<%=wordCount%>字！","提示");
				o("btn").disabled = false;
				return false;
			}
		
	<%}
	}%>
	//if (CKEDITOR.instances.content.getData().trim()=="") {
		//alert("请填写内容！");
		//return false;
	//}
}
function strlen(str) {
	//<summary>获得字符串实际长度，中文2，英文1</summary>    
	//<param name="str">要获得长度的字符串</param>
	var regExp = new RegExp(" ","g");
	str = str.replace(regExp , "");
	str = str.replace(/\r\n/g,"");
	var realLength = 0, len = str.length, charCode = -1;
	for (var i = 0; i < len; i++) {
		charCode = str.charCodeAt(i);
		if (charCode >= 0 && charCode <= 128)
			realLength += 1;
		else realLength += 2;
	}
	return realLength;
}  
</script>
</html>
