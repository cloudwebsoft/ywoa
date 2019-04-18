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
<%@ page import="com.redmoon.oa.fileark.*"%>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
Privilege privilege = new Privilege();
boolean isUserPrivValid = privilege.isUserPrivValid(request, "notice") || privilege.isUserPrivValid(request, "notice.dept");
if (!isUserPrivValid) {
    out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

Document doc = null;
Document template = null;

boolean isDeptNotice = !privilege.isUserPrivValid(request, "notice") && privilege.isUserPrivValid(request, "notice.dept");
String userName = privilege.getUser(request);
UserDb ud = new UserDb();
ud = ud.getUserDb(userName);
String depts = "";
DeptUserDb du = new DeptUserDb();
java.util.Iterator ir = du.getDeptsOfUser(userName).iterator();
String deptNames = "";
depts = "";
while (ir.hasNext()) {
	DeptDb dd = (DeptDb)ir.next();
	
	if (depts.equals("")) {
		depts = dd.getCode();
		deptNames = dd.getName();
	}
	else {
		depts += "," + dd.getCode();
		deptNames += "," + dd.getName();
	}
	
	// 加入子部门
	Vector v = new Vector();
	dd.getAllChild(v, dd);
	Iterator ir2 = v.iterator();
	while (ir2.hasNext()) {
		DeptDb dd2 = (DeptDb)ir2.next();
		if (("," + depts + ",").indexOf("," + dd2.getCode() + ",")==-1) {
			depts += "," + dd2.getCode();
			deptNames += "," + dd2.getName();
		}
	}
}
String op = ParamUtil.get(request, "op");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=8"/>
<title>添加通知</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<%@ include file="../inc/nocache.jsp"%>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<script src="../inc/upload.js"></script>

<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.config.js?2023"></script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/ueditor.all.js?2023"> </script>
<script type="text/javascript" charset="utf-8" src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js?2023"></script>

<script src="../inc/livevalidation_standalone.js"></script>
<script type="text/javascript" src='../ckeditor/formpost.js'></script>
<script src="../inc/map.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
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
.notice_add_toolbar{
	width:100%;
	height:30px;
	background-color:#daeaf8;
	border-bottom:2px solid #92b4d2;
	padding-left:10px;
	margin-bottom:10px;
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
<script>
function getDepts() {
	return form1.depts.value;
}

function getDept() {
	return form1.depts.value;
}

function setUsers(users, userRealNames) {
	form1.receiver.value = users;
	form1.isall.value = '0';
	form1.deptNames.value = userRealNames;
}

function getSelUserNames() {
	return form1.receiver.value;
}

function getSelUserRealNames() {
	return form1.deptNames.value;
}

function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}

function openWinUsers() {
	openWin("../user_multi_sel.jsp?unitCode=<%=privilege.getUserUnitCode(request)%>&isIncludeChildren=true", 800, 480);
}

function openWinUserGroup() {
	openWin("../user_usergroup_multi_sel.jsp", 520, 400);
}

function openWinUserRole() {
	openWin("../user_role_multi_sel.jsp", 520, 400);
}
</script>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif' /></div>
<%
if (op.equals("add")) {
	NoticeMgr am = new NoticeMgr();
	boolean re = false;
	try {
		  re = am.create(application, request);
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
		return;
	}
	if (re) {
		out.print(StrUtil.jAlert_Redirect("发布成功！","提示", "notice_list.jsp"));
	} else {
		out.print(StrUtil.jAlert_Back("发布失败！","提示"));
	}
	return;
}
 %>
<table cellSpacing="0" cellPadding="0" width="100%" class="notice_add_toolbar">
  <tbody>
    <tr>
      <td ><a href="notice_list.jsp"><img src="../images/left/icon-notice.gif" />&nbsp;<%=(isDeptNotice?"部门":"公共")%>通知</a></td>
    </tr>
  </tbody>
</table>
<form name="form1" action="notice_add.jsp?op=add" method="post" enctype="multipart/form-data">
<table width="100%" class="tabStyle_1">
<tbody>
  <tr>
    <td colspan="3" class="tabStyle_1_title">发布通知</td>
    </tr>
  <tr>
	<td >标&nbsp;&nbsp;&nbsp;&nbsp;题：</td>
	<td colspan="2">
	  <input type="text" name="title" id="title" size="80" maxlength="25" />
	</td>
  </tr>
  
  <tr>
    <td id="tdColor">颜&nbsp;&nbsp;&nbsp;&nbsp;色：</td>
    <td colspan="2">
        <select id="color" name="color">
          <option value="" style="COLOR: black" selected>标题颜色</option>
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
        &nbsp;&nbsp;&nbsp;&nbsp;<input type="checkbox" id="level" name="level" value="1" />
        <span title="重要通知将在桌面弹窗显示">重要通知</span>
        <!-- <input type="checkbox" id="isBold" name="isBold" value="true" />
        标题加粗 -->
      <input type="checkbox" name="isShow" checked="checked" value="1" />
	  显示已查看通知人员
	  <input type="checkbox" name="is_reply"  value="1"  id="is_reply" checked="checked" title="是否可以回复" />
	  回复
	  <span class="responseDiv" >
	   <input type="checkbox" name="is_forced_response" id="is_forced_response" value="1" />
		 强制回复
	 </span>
	 <%if (com.redmoon.oa.sms.SMSFactory.isUseSMS()) {%>
     <input type="checkbox" id="isToMobile" name="isToMobile" checked="checked" value="true" />
     短信提醒
     <%}%>
  </td>
  </tr>
   <tr>
        <td colspan="3" valign="top">
        <div id="divTmpAttachId" style="display:none"></div>        
        <div style="clear:both">              
			<textarea id="htmlcode" name="htmlcode"></textarea>
        </div>

<script>
var uEditor;
$(function() {
	uEditor = UE.getEditor('htmlcode',{
				//allowDivTransToP: false,//阻止转换div 为p
				toolleipi:true,//是否显示，设计器的 toolbars
				textarea: 'htmlcode',
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
	        return '<%=request.getContextPath()%>/ueditor/UploadFile?op=notice';
	    } else {
	        return this._bkGetActionUrl.call(this, action);
	    }
	}
});
</script>


		</td>
      </tr>
  <tr>
    <td><span class="TableContent">有效期：</span></td>
    <td colspan="2"><span class="TableData">开始日期：
        <input type="text" id="beginDate" name="beginDate" size="10" onblur="beginDateCheck()" value="<%=DateUtil.format(new java.util.Date(), "yyyy-MM-dd")%>" />
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
结束日期：
<input type="text" id="endDate" name="endDate" size="10" title="结束日期不填表示永不过期" />
    </span></td>
  </tr>

  <tr id="trToDept" >
    <td>
      <input type="hidden" name="isDeptNotice" value="<%=(isDeptNotice?"1":"0")%>" />
      <%
	  String myUnitCode = privilege.getUserUnitCode(request);
	  
	  %>
      <input type="hidden" id="depts" name="depts" value="<%=(isDeptNotice ? depts : myUnitCode)%>" />
      <input id="isall" name="isall" value="<%=privilege.isUserPrivValid(request,"notice")==true?2:1 %>" type="hidden" />
      <input id="userName" name="userName" value="<%=userName%>" type="hidden" />
      <input id="unitCode" name="unitCode" value="<%=myUnitCode %>" type="hidden" />
      发布人员：</td>
    <td colspan="2" style="line-height:1.5">
    	<input id="radioall" type="radio" name="radio" value="全部用户" onclick="setAllUsers()" checked /> <a href="javascript:setAllUsers();setRadioALLSelected();">全部用户</a>
		<input id="radioselect" type="radio" name="radio" value="选择用户" onclick="setIsAll();openWinUsers();desDepts()" /><a href="javascript:setRadioSelected();setIsAll();openWinUsers();desDepts()">选择用户</a>
      <br/>
      <textarea name="deptNames" cols="90" rows="5" readOnly wrap="yes" id="deptNames" disabled>全部用户</textarea>
      <input type="hidden" name="receiver" id="receiver"/>
      </td>
  </tr>
  <tr>
    <td align="left" colspan="3">
    <script>initUpload();</script>
	</td>
  </tr>
  <tr>
	<td align="center" colspan="3">
	  <input class="mybtn" name="button" type="button" value="确定" onclick='setNew()'/>
	  &nbsp;&nbsp;&nbsp;&nbsp;
	  <input type="button" class="mybtn" value="返回" onclick="window.history.back()" />	</td>
  </tr>
</tbody>
</table>
</form>
<br />
<script language="javascript">

function openWinPersonUserGroup() {
	openWin("../user/persongroup_user_multi_sel.jsp", 520, 400);
}

function setAllUsers() {
	<%if(privilege.isUserPrivValid(request,"notice")){ %>
		form1.isall.value = "2";
    o("unitCode").value = "root";
  <%} else if(privilege.isUserPrivValid(request,"notice.dept")){ %>
  	form1.isall.value = "1";
    o("unitCode").value = "<%=myUnitCode%>";
  <%}%>
	$("#receiver").val("");
	form1.deptNames.value = "全部用户";
}
function setDeptUsers() {
	if($("#ckd").attr("checked")=="checked"){
		o("button22").disabled = true;
		//o("deptNames").style.disabled = "disabled";
	}else{
		o("button22").disabled = false;
		//o("deptNames").style.disabled = "";
	}
	$("#receiver").val("");
    form1.isall.value = "1";
    o("unitCode").value = "<%=myUnitCode%>";
	form1.deptNames.value = "全部用户";
}
function clearUsers(){
	$("#deptNames").val("");
	$("#receiver").val("");
}

function desDepts(){
	o("deptNames").disabled = true;
}
function setIsAll(){
	$("#receiver").val("");
	o("isall").value = "0";
	$("#deptNames").val("");
}
function setRadioALLSelected(){
	$("#radioall").attr("checked","checked");
	$("#radioselect").removeAttr("checked");
}
function setRadioSelected(){
	$("#radioall").removeAttr("checked");
	$("#radioselect").attr("checked","checked");
}

</script>
</body>
<script language="JavaScript">

<%
if (doc!=null) {
	out.println("var id=" + doc.getID() + ";");
}
%>
var op = "<%=op%>";
var title;

$(function(){
	//is_forced_response
	$("#is_reply").change(function(){
	
		if($("#is_reply").is(":checked")){
			$(".responseDiv").show();
			$("#is_forced_response").removeAttr("checked");
		}else{
			$(".responseDiv").hide();
			//$("#is_forced_response").removeAttr("checked");
		}

	});
	
	$('#beginDate').datetimepicker({
	  	lang:'ch',
	  	timepicker:false,
	  	format:'Y-m-d',
	  	formatDate:'Y/m/d'
	  });
	$('#endDate').datetimepicker({
		lang:'ch',
		timepicker:false,
		format:'Y-m-d',
		formatDate:'Y/m/d'
	});
    $('#color').change(function() {
    	$('#color').css("color", $(this).val());
    });
  $("#level").click(function() {
	  if ($("#level").attr("checked")) {
	  	//$("#isBold").attr("checked","checked");
		$("#color").val("#ff0000");
	  }
	  else {
	  	//$("#isBold").removeAttr("checked");
		$("#color").val("");
	  }
	  $("#color").change();
  });
  title = new LiveValidation('title');
  title.add(Validate.Presence);	
});

function setNew(){
	
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
	if(form1.deptNames.value==""){
	<%if(privilege.isUserPrivValid(request,"notice")){%>
	$("#receiver").val("");
    form1.isall.value = "2";
    o("unitCode").value = "root";
	form1.deptNames.value = "全部用户";
	<%}%>
	<%if(privilege.isUserPrivValid(request,"notice.dept")&&!privilege.isUserPrivValid(request,"admin")){ %>
	$("#receiver").val("");
    form1.isall.value = "1";
    o("unitCode").value = "<%=myUnitCode%>";
	form1.deptNames.value = "全部用户";
	<%}%>
	}
	$(".treeBackground").addClass("SD_overlayBG2");
	$(".treeBackground").css({"display":"block"});
	$(".loading").css({"display":"block"});
	form1.submit();
}

function showvote(isshow)
{
	if (addform.isvote.checked)
	{
		divVote.style.display = "";
	}
	else
	{
		divVote.style.display = "none";		
	}
}

function insertHTMLToEditor(value) {
	var oEditor = CKEDITOR.instances.htmlcode;
	if ( oEditor.mode == 'wysiwyg' ) {
		oEditor.insertHtml( value );
	}
	else
		jAlert( '请切换编辑器至设计视图!','提示' );
}

function addImg(attachId, imgPath, uploadSerialNo) {
	var ext = imgPath.substring(imgPath.length-3, imgPath.length);
	if (ext=="swf") {
		 var str = '<object classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" codebase="http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0" width="200" height="150">';
		 str += '<param name="movie" value="<%=request.getContextPath()%>/img_show.jsp?path=' + imgPath + '"><param name="quality" value="high">';
		 str += '</object>';
		insertHTMLToEditor("<BR>" + str + "<BR>");
	}
	else if (ext=="gif" || ext=="jpg" || ext=="png" || ext=="bmp") {
		var img = "<img alt='点击在新窗口中打开' style='cursor:hand' onclick=\"window.open('/img_show.jsp?path=" + imgPath + "')\" onload=\"if(this.width>screen.width-333)this.width=screen.width-333\" src='<%=request.getContextPath()%>/img_show.jsp?path=" + imgPath + "'>";
		insertHTMLToEditor("<BR>" + img + "<BR>");
	}
	else if (ext=="flv") {
	　　// insertHTMLToEditor("<embed width=320 height=260 flashvars=\"file="　+　imgPath　+　"\" allowfullscreen=\"true\" allowscriptaccess=\"always\" bgcolor=\"#ffffff\" src=\"<%=request.getContextPath()%>/ckeditor/plugins/cwvideo/jwplayer.swf\"></embed>");		

		// var str = '<object width="640" height="520"><param name="movie" value="ckeditor/plugins/cwvideo/jwplayer.swf?file=' + imgPath + '" /><param name="wmode" value="transparent" /></object>';
		// insertHTMLToEditor(str);
		
var str = '<object type="application/x-shockwave-flash" width="640" height="520" wmode="transparent" data="ckeditor/plugins/cwvideo/jwplayer.swf?file=' + imgPath + '">';

str += '<param name="movie" value="ckeditor/plugins/cwvideo/jwplayer.swf?file=' + imgPath + '" />';

str += '<param name="wmode" value="transparent" />';

str += '</object>';
insertHTMLToEditor(str);
		
		// insertHTMLToEditor("<embed height=520 width=640 autostart=false flashvars=\"file="　+　imgPath　+　"\" allowfullscreen=\"true\" allowscriptaccess=\"always\" bgcolor=\"#ffffff\" src=\"ckeditor/plugins/cwvideo/jwplayer.swf\"></embed>");   
	}
	else {
		// var img = "<img alt='点击在新窗口中打开' style='cursor:hand' onclick=\"window.open('" + imgPath + "')\" onload=\"if(this.width>screen.width-333)this.width=screen.width-333\" src='" + imgPath + "'>";
		str += "<param name=\"menu\" value=\"true\"/>";
		str += "<param name=\"url\" value=\"" + imgPath + "\"/>";
		str += "<param name=\"autostart\" value=\"true\"/>";
		str += "<param name=\"loop\" value=\"true\"/>";
		str += "</object>";
		insertHTMLToEditor("<BR>" + str + "<BR>");
	}
	divTmpAttachId.innerHTML += "<input type=hidden name=tmpAttachId value='" + attachId + "'>";
		
	uploadMap.put(uploadSerialNo + "_attId", attachId);
	//$("uploadStatus_" + uploadSerialNo).innerHTML = "&nbsp;<a href='javascript:;' onclick=\"delUpload('" + uploadSerialNo + "')\">删除</a>";
}

var attachCount = 1;

function AddAttach() {
	updiv.insertAdjacentHTML("BeforeEnd", "<div><input type='file' name='attachment" + attachCount + "'></div>");
	attachCount += 1;
}

function selectNode(code, name) {
	addform.dir_code.value = code;
	$("dirNameSpan").innerHTML = name;
}

function beginDateCheck(){
	if($("#beginDate").val() == ""){
		$("#beginDate").val("<%=DateUtil.format(new java.util.Date(), "yyyy-MM-dd") %>") ;
	}
}

function showProgress(serialNo, fileSize) {
	uploadMap.put(serialNo+"_size", fileSize);
	
	refreshProgress(serialNo, fileSize);
}

var i = 0;
function refreshProgress(serialNo, fileSize) {
	getProgress(serialNo);

	// window.status = i;
	i++;
	
	var timeoutid = window.setTimeout("refreshProgress('" + serialNo + "'," + fileSize + ")", "2000");
	if (!uploadMap.containsKey(serialNo)) {
		uploadMap.put(serialNo, timeoutid);
	}
	// alert(uploadMap.get(serialNo).value);
}

</script>
</html>
