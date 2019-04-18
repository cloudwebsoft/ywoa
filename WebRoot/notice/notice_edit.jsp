<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.notice.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.pvg.Privilege"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%
String skincode = UserSet.getSkin(request);
String userName = ParamUtil.get(request, "userName");
long id = ParamUtil.getLong(request, "id");
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
Privilege privilege = new Privilege();
String uName = privilege.getUser(request);
NoticeMgr nMgr = new NoticeMgr();
String myUnitCode = privilege.getUserUnitCode(request);
NoticeDb nd = new NoticeDb();
nd = nd.getNoticeDb(id);
boolean isUserPrivValid = false;		

if (nMgr.canEditNotice(id,uName) || (privilege.isUserPrivValid(request, "notice") && myUnitCode.equals(DeptDb.ROOTCODE))) {
	isUserPrivValid = true;
} else {
	if ((nd.getIsDeptNotice() == 0 && privilege.isUserPrivValid(request, "notice")) 
			|| (nd.getIsDeptNotice() == 1 && privilege.isUserPrivValid(request, "notice.dept"))) {
		if (myUnitCode.equals(nd.getUnitCode())) {
			isUserPrivValid = true;
		}
	}
}
if (!isUserPrivValid) {
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

NoticeMgr am = new NoticeMgr();
//try {
	//if (!am.isNoticeManageable(request, id)){
		//out.print(SkinUtil.makeErrMsg(request, "权限非法！", true));
		//return;
	//}
//}
//catch (ErrMsgException e) {
	%>
	<!-- <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" /> -->		
	<%
	//out.print(SkinUtil.makeErrMsg(request, e.getMessage(), true));
	//return;
//}

boolean isDeptNotice = nd.isDeptNotice();
String depts = "";
String deptNames = "";
Iterator ir = nd.getDeptOfNotice(id).iterator();
DeptDb deptDb = new DeptDb();
while(ir.hasNext()) {
	deptDb = (DeptDb)ir.next();
    String deptCode = deptDb.getCode();
	String deptName = deptDb.getName();
	if(ir.hasNext()) {
		depts += deptCode + ",";
		deptNames += deptName + ",";
	} else {
	    depts += deptCode;
		deptNames += deptName;
	}
}
String op = ParamUtil.get(request, "op");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=8"/>
<title>编辑通知</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../inc/upload.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js?2023"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js?2023"> </script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js?2023"></script>

<script src="../inc/livevalidation_standalone.js"></script>

<%@ include file="../inc/nocache.jsp"%>
<script>
function getDepts() {
	return form1.depts.value;
}

function getDept() {
	return form1.depts.value;
}

function setUsers(users, userRealNames) {
	form1.receiver.value = users;
	form1.userRealNames.value = userRealNames;
}

function getSelUserNames() {
	return form1.receiver.value;
}

function getSelUserRealNames() {
	return form1.userRealNames.value;
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function openWinUsers() {
	showModalDialog('../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:600px;dialogHeight:480px;status:no;help:no;')
}

function openWinUserGroup() {
	openWin("../user_usergroup_multi_sel.jsp", 520, 400);
}

function openWinUserRole() {
	openWin("../user_role_multi_sel.jsp", 520, 400);
}
</script>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
.mybtn {
	background-color: #87c3f1 !important;
	font-weight: bold;
	text-align: center;
	line-height: 35px;
	height: 35px;
	width:120px;
	padding-right: 8px;
	padding-left: 8px;
	-moz-border-radius: 3px;
	-webkit-border-radius: 3px;
	border-radius: 3px;
	behavior: url(../skin/common/ie-css3.htc);
	cursor:pointer;
	color:#fff;
	border-top-width: 0px;
	border-right-width: 0px;
	border-bottom-width: 0px;
	border-left-width: 0px;
	border-top-style: none;
	border-right-style: none;
	border-bottom-style: none;
	border-left-style: none;
}
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<%
if (op.equals("edit")) {
	boolean re = false;
	try {
		  re = am.saveNotice(request);
	%>
<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
<%
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "notice_edit.jsp?id=" + id));
	} else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
	return;
}
if (op.equals("delAttach")) {
    long attachId = ParamUtil.getLong(request, "attachId");
	NoticeDb nda = new NoticeDb();
	NoticeAttachmentDb nad = new NoticeAttachmentDb(attachId);
	nda = nda.getNoticeDb(nad.getNoticeId());
	boolean re = false;
	%>
	<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
	<%
	re = nda.delAttach(attachId);
	%>
<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
	<%
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "notice_edit.jsp?id=" + id));
	} else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
	return;
}
 %>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1"><img src="../images/left/icon-notice.gif"><a href="notice_list.jsp">通知</a></td>
    </tr>
  </tbody>
</table>
<form name="form1" action="?op=edit&id=<%=nd.getId()%>" method="post">
<table width="100%" class="tabStyle_1">
  <tr>
  	<td class="tabStyle_1_title" colspan="3">修改通知</td>
  </tr>
  <tr>
    <td width="100">标&nbsp;&nbsp;&nbsp;&nbsp;题</td>
    <td colspan="2">
	  <input name="title" id="title" size="80" maxlength="25" value="<%=nd.getTitle()%>" />
	  <input name="userName" value="<%=userName%>" type="hidden" />
	  <input name="id" id="id" size="40" type="hidden" value="<%=nd.getId()%>" />
	  <input name="isall" id="isall" size="40" type="hidden" value="<%=nd.getIsall()%>" />
	  <input name="unitCode" id="unitCode" size="40" type="hidden" value="<%=nd.getUnitCode()%>" />
	  <input type="hidden" name="isDeptNotice" value="<%=(isDeptNotice?"1":"0")%>" />
	</td>
  </tr>
  <tr>
    <td>颜&nbsp;&nbsp;&nbsp;&nbsp;色</td>
    <td colspan="2"><select id="color" name="color">
      <option value="" style="COLOR: black" selected="selected">标题颜色</option>
      <option style="BACKGROUND: #000088" value="#000088">标题颜色</option>
      <option style="BACKGROUND: #0000ff" value="#0000ff">标题颜色</option>
      <option style="BACKGROUND: #008800" value="#008800">标题颜色</option>
      <option style="BACKGROUND: #008888" value="#008888">标题颜色</option>
      <option style="BACKGROUND: #0088ff" value="#0088ff">标题颜色</option>
      <option style="BACKGROUND: #00a010" value="#00a010">标题颜色</option>
      <option style="BACKGROUND: #1100ff" value="#1100ff">标题颜色</option>
      <option style="BACKGROUND: #111111" value="#111111">标题颜色</option>
      <option style="BACKGROUND: #333333" value="#333333">标题颜色</option>
      <option style="BACKGROUND: #50b000" value="#50b000">标题颜色</option>
      <option style="BACKGROUND: #880000" value="#880000">标题颜色</option>
      <option style="BACKGROUND: #8800ff" value="#8800ff">标题颜色</option>
      <option style="BACKGROUND: #888800" value="#888800">标题颜色</option>
      <option style="BACKGROUND: #888888" value="#888888">标题颜色</option>
      <option style="BACKGROUND: #8888ff" value="#8888ff">标题颜色</option>
      <option style="BACKGROUND: #aa00cc" value="#aa00cc">标题颜色</option>
      <option style="BACKGROUND: #aaaa00" value="#aaaa00">标题颜色</option>
      <option style="BACKGROUND: #ccaa00" value="#ccaa00">标题颜色</option>
      <option style="BACKGROUND: #ff0000" value="#ff0000">标题颜色</option>
      <option style="BACKGROUND: #ff0088" value="#ff0088">标题颜色</option>
      <option style="BACKGROUND: #ff00ff" value="#ff00ff">标题颜色</option>
      <option style="BACKGROUND: #ff8800" value="#ff8800">标题颜色</option>
      <option style="BACKGROUND: #ff0005" value="#ff0005">标题颜色</option>
      <option style="BACKGROUND: #ff88ff" value="#ff88ff">标题颜色</option>
      <option style="BACKGROUND: #ee0005" value="#ee0005">标题颜色</option>
      <option style="BACKGROUND: #ee01ff" value="#ee01ff">标题颜色</option>
      <option style="BACKGROUND: #3388aa" value="#3388aa">标题颜色</option>
      <option style="BACKGROUND: #000000" value="#000000">标题颜色</option>
      </select>
      &nbsp;&nbsp;&nbsp;&nbsp;
<input type="checkbox" id="level" name="level" value="1" <%=nd.getLevel()==1?"checked":""%> disabled/>
      <span title="重要通知将在桌面弹窗显示">重要通知</span>
      <!-- <input type="checkbox" id="isBold" name="isBold" value="true" <%=nd.isBold()?"checked":""%> disabled/>
      标题加粗  -->
      <input type="checkbox" name="is_reply"  value="1"  id="is_reply" <%=nd.getIs_reply()==1?"checked":"" %> disabled="disabled" title="是否可以回复" />
      回复
      <%
	  	if((nd.getIs_reply() == 1)&&(nd.getIs_forced_response() == 1)){
	  %>
      <span class="responseDiv" >
        <input type="checkbox" name="is_forced_response" id="is_forced_response" value="1"  <%=nd.getIs_forced_response() == 1?"checked":""%> disabled="disabled"/>
        强制回复
        </span>
      <%} %>
      <input type="checkbox" name="isShow" <%=(nd.getIsShow()==1?"checked":"")%> value="1"  disabled/>
      显示已查看通知人员
      
      </td>
  </tr>
   <tr>
     <td colspan="3">
       <div style="clear:both">     
         <textarea id="content" name="content"><%=nd.getContent()%></textarea>
       </div>
  <script>
var uEditor;
$(function() {
	uEditor = UE.getEditor('content',{
				//allowDivTransToP: false,//阻止转换div 为p
				toolleipi:true,//是否显示，设计器的 toolbars
				textarea: 'content',
				enableAutoSave: false,  
				toolbars: [[
		            'fullscreen', 'source', '|', 'undo', 'redo', '|',
		            'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'superscript', 'subscript', 'removeformat', 'formatmatch', 'autotypeset', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'selectall', 'cleardoc', '|',
		            'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
		            'paragraph', 'fontfamily', 'fontsize', '|',
		            'directionalityltr', 'directionalityrtl', 'indent', '|',
		            'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', '|', 'touppercase', 'tolowercase', '|',
		            'link', 'unlink', 'anchor', '|', 'imagenone', 'imageleft', 'imageright', 'imagecenter', '|',
		            'simpleupload', 'insertimage', 'insertvideo', 'emotion', 'map', 'insertframe', 'insertcode', 'pagebreak', 'template', '|',
		            'horizontal', 'date', /*'time'*/, 'spechars', '|',
		            'inserttable', 'deletetable', 'insertparagraphbeforetable', 'insertrow', 'deleterow', 'insertcol', 'deletecol', 'mergecells', 'mergeright', 'mergedown', 'splittocells', 'splittorows', 'splittocols', '|',
		            'print', 'preview', 'searchreplace', 'help'
		        ]],				
				//focus时自动清空初始化时的内容
				//autoClearinitialContent:true,
				//关闭字数统计
				wordCount:false,
				//关闭elementPath
				elementPathEnabled:false,
				//默认的编辑区域高度
				initialFrameHeight:300,
				disabledTableInTable:false 
				///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
				//更多其他参数，请参考ueditor.config.js中的配置项
			});
			
	UE.Editor.prototype._bkGetActionUrl = UE.Editor.prototype.getActionUrl;
	UE.Editor.prototype.getActionUrl = function(action) {
	    if (action == 'uploadimage' || action == 'uploadscrawl') {
	        return '<%=request.getContextPath()%>/ueditor/UploadFile?op=notice';
	    } else if (action == 'uploadvideo') {
	        return '<%=request.getContextPath()%>/ueditor/UploadFile';
	    } else {
	        return this._bkGetActionUrl.call(this, action);
	    }
	}
});
</script>     </td>
    </tr>
  <tr>
    <td><span class="TableContent">有效期</span></td>
    <td colspan="2"><span class="TableData">开始日期：
        <input type="text" id="beginDate" name="beginDate" size="10" value="<%=DateUtil.format(nd.getBeginDate(), "yyyy-MM-dd")%>" disabled/>
&nbsp;
结束日期：
<%String edstr = DateUtil.format(nd.getEndDate(), "yyyy-MM-dd"); %>
<input type="text" id="endDate" name="endDate" size="10" title="<%=edstr.equals("") ? "结束日期不填表示永不过期" : "" %>" value="<%=edstr%>" <%=edstr.equals("") ? "" : "disabled" %>/>
    </span></td>
  </tr>
   
  <tr id="trToDept" >
    <td>
      <input type="hidden" name="isDeptNotice" value="<%=(isDeptNotice?"1":"0")%>">
     
      发布人员</td>
    <td width="80%" style="line-height:1.5" colspan=2>
    <%
    	NoticeReplyMgr nrm = new NoticeReplyMgr();
    	NoticeDb ndb = new NoticeDb();
    	ndb = ndb.getNoticeDb(id);
    	String userStr = "";
    	if(ndb.getIsall()==0){
    		userStr = nrm.getUserStr(id);
    	}else{
    		userStr = "全部用户";
    	}
    %>
      <textarea name="deptNames" cols="80" rows="5" readOnly wrap="yes" id="deptNames" disabled><%=userStr %></textarea>
      <input type="hidden" name="receiver" id="receiver"/>
      
      </td>
    
  </tr>
  <tr>
	<td align="center" colspan="3"><input name="button2" type="button" class="mybtn" value="确定" onclick="dosub()">
	&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" class="mybtn" onClick="window.location.href='notice_list.jsp'" value="返回" ></td>
  </tr>
  <%
  ir = nd.getAttachs().iterator();
  if(ir.hasNext()){
  %>
  <tr>
	<td height="30" colspan="3" align="left" style="line-height:1.5">附&nbsp;&nbsp;&nbsp;&nbsp;件：<br>
<%

while (ir.hasNext()) {
NoticeAttachmentDb nad = (NoticeAttachmentDb)ir.next();
%>
	  <a target=_blank href="notice_getfile.jsp?noticeId=<%=nd.getId()%>&attachId=<%=nad.getId()%>"><%=nad.getName()%></a>&nbsp;&nbsp;[<a href="#" onClick="jConfirm('您确定要删除吗？','提示',function(r){ if(!r){return;} else{window.location.href='notice_edit.jsp?op=delAttach&id=<%=id%>&attachId=<%=nad.getId()%>'}}) ">删除</a>]<BR>
<%}%>	</td>
  </tr>
  <%} %>
</table>
</form>
<br />
<script language="javascript">
var title;

$(function(){
	$('#endDate').datetimepicker({
		lang:'ch',
		timepicker:false,
		format:'Y-m-d'
	});
	title = new LiveValidation('title');
	title.add(Validate.Presence);	
	o("color").value = "<%=nd.getColor()%>";
	$('#color').css("color", $('#color').val());	   
    $('#color').change(function() {
     	$('#color').css("color", $(this).val());
    });
	$("#level").click(function() {
		  if ($("#level").attr("checked")) {
		  	$("#isBold").attr("checked","checked");
			$("#color").val("#ff0000");
		  }
		  else {
		  	$("#isBold").removeAttr("checked");
			$("#color").val("");
		  }
		   $("#color").change();
	});
})
function openWinPersonUserGroup() {
	openWin("../user/persongroup_user_multi_sel.jsp", 520, 400);
}

function openWinDepts() {
	var ret = showModalDialog('../dept_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>',window.self,'dialogWidth:500px;dialogHeight:360px;status:no;help:no;')
	if (ret==null)
		return;
	form1.deptNames.value = "";
	form1.depts.value = "";
	for (var i=0; i<ret.length; i++) {
		if (form1.deptNames.value=="") {
			form1.depts.value += ret[i][0];
			form1.deptNames.value += ret[i][1];
		}
		else {
			form1.depts.value += "," + ret[i][0];
			form1.deptNames.value += "," + ret[i][1];
		}
	}
}

function dosub() {
	if (!LiveValidation.massValidate(title.formObj.fields)) {
		jAlert("标题不能为空！", "提示");
		return;
	}
	if ($('#title').val().trim() == '') {
		jAlert("标题不能为空！", "提示");
		return;
	}
	if (uEditor.getContent().trim() == "") {
		jAlert("正文不能为空！", "提示");
		return;
	}
	$(".treeBackground").addClass("SD_overlayBG2");
	$(".treeBackground").css({"display":"block"});
	$(".loading").css({"display":"block"});
	form1.submit();
}
</script>

</body>
</html>