<%@ page contentType="text/html; charset=utf-8" language="java"
	import="java.sql.*" errorPage=""%>
<%@ page import="com.redmoon.oa.notice.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="cn.js.fan.db.ResultIterator"%>
<%@page import="cn.js.fan.db.ResultRecord"%>
<%@ page import="org.json.*"%>
<%@page import="com.redmoon.oa.message.MessageDb"%>
<%
	com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

	if (!privilege.isUserPrivValid(request, "read")) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(
				request, "pvg_invalid")));
		return;
	}

	String op = ParamUtil.get(request, "op");
	JSONObject json = new JSONObject();

	String strId = ParamUtil.get(request, "id");
	String strIsShow = ParamUtil.get(request, "isShow");
	int id = Integer.valueOf(strId).intValue();
	int flowId = ParamUtil.getInt(request, "flowId", 0);
	
	NoticeDb nd = new NoticeDb();
	nd = nd.getNoticeDb(id);

	if (!nd.isLoaded()) {
		out.print(SkinUtil.makeErrMsg(request, "通知不存在！"));
		return;		
	}

	/*
	String usersKnow = nd.getUsersKnow();
	if (usersKnow.equals("")) {
		usersKnow = privilege.getUser(request);
	} else {
		// 检查用户是否已被记录
		String[] ary = usersKnow.split(",");
		boolean isFound = false;
		int len = ary.length;
		String userName = privilege.getUser(request);
		for (int i = 0; i < len; i++) {
			if (userName.equals(ary[i])) {
				isFound = true;
				break;
			}
		}
		if (!isFound) {
			usersKnow += "," + userName;
		}
	}
	nd.setUsersKnow(usersKnow);
	nd.save();
	*/

	String uName = privilege.getUser(request);
	//lzm添加  根据 通知ID 用户名查看  查看回复用户
	int is_forced_res = nd.getIs_forced_response();
	boolean isReplyExist = true;
	NoticeReplyDb nnrd = new NoticeReplyDb();
	nnrd.setUsername(uName);
	nnrd.setNoticeid((long)id);
	nnrd = nnrd.getReply();
	if(nnrd == null) {
		nnrd = new NoticeReplyDb();
		isReplyExist = false;
	}
	String content = StrUtil.getNullStr(nnrd.getContent());
	String name = StrUtil.getNullStr(nnrd.getUsername());
	boolean isReaded = "1".equals(nnrd.getIsReaded());
	// 当前用户尚未回复内容  // 当前用户不是 通知发布者
	boolean isNotReply = uName.equals(name) && (content.equals("")); // && (!uName.equals(nd.getUserName()));
	if (isReplyExist && !isReaded && is_forced_res == 0){
		java.util.Date rDate = new java.util.Date();
		NoticeReplyDb nrdb = new NoticeReplyDb();
		nrdb.setIsReaded("1");
		nrdb.setReadTime(rDate);
		nrdb.setNoticeid((long) id);
		nrdb.setUsername(uName);
		nrdb.saveStatus();
	}

	UserMgr um = new UserMgr();
	UserDb user = um.getUserDb(nd.getUserName());
	// lzm新增  更新oa_message表中   isReaded的 状态 
	MessageDb messageDb = new MessageDb();
	messageDb.setCommonUserReaded(uName, (long) id,
			MessageDb.MESSAGE_SYSTEM_NOTICE_TYPE);

	Iterator ir = nd.getAttachs().iterator();
	NoticeReplyDb nrd = new NoticeReplyDb();
	Iterator irnrd = nrd.getNoticeReply(id).iterator();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css/common/common.css" />
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css/message/message.css" />
		<!-- <script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script> -->

<style>
.reply_div {
	margin: 0 auto;
	width: 96%;
	font-family: "microsoft yahei";
	font-size: 14px;
	border: 10px solid #efefef;
}

.reply_title_div {
	margin: 10px;
}

.con_btn {
	color: white;
	background-color: #85c4f0;
	text-align: center;
	font-weight: bold;
	line-height: 20px;
	padding-right: 10px;
	padding-left: 10px;
	height: 24px;
	border: 1px solid #85c4f0;
	cursor: pointer;
	-moz-border-radius: 3px;
	-webkit-border-radius: 3px;
	border-radius: 3px;
}

.reply_title {
	font-weight: bold;
}

.reply_btn {
	float: right;
}

* {
	margin: 0;
	padding: 0;
}

.myTextarea {
	display: block;
	margin: 8px auto;
	overflow: hidden;
	width: 100%;
	font-size: 14px;
	line-height: 24px;
	text-indent: 1em;
	height: 48px;
	border: solid 1px #ffa200;
	-moz-border-radius: 5px;
	-webkit-border-radius: 5px;
	border-radius: 5px;
	margin: 10px 0px;
	height: 48px;
}

ul li {
	list-style: none;
}

.org_btn {
	background: #ffc24d;
	border: 0px;
}

.right {
	float: right;
}

.reply_date {
	margin-left:20px
}

.clearfix {
	zoom: 1;
}

.clearfix:before,.clearfix:after {
	content: '';
	display: table;
}

.clearfix:after {
	clear: both;
}

.reply_ul li {
	font-size: 14px;
	margin: 10px;
	margin-bottom: 0px;
	padding: 10px 0px;
	border-bottom: 1px solid #EEEEEE;
}

.reply_name {
	color: #85c4f0;
}
.tips{
	background-color:#FFE081;
	margin: 0 auto;
	font-size: 14px;
	color: #ab701b;
	padding-top: 10px;
	position: absolute;
	top: 0px;
	margin-top: 10px;
	margin-right: 10px;
	width: 100%;
	padding-bottom:10px;
	text-align: center;

}
.tips .icon_close{
	display: inline-block;
	width: 16px;
	height: 16px;
	position: absolute;
	right: 0px;
	top: 0px;
	
	background: url(close.png) no-repeat;
}

.li_re_content {
	padding: 5px;
	word-break: break-all;
}
</style>
		<script src="../spwhitepad/createShapes.js"></script>
		<script src="../inc/common.js"></script>
		<script src="../inc/upload.js"></script>
		<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
		<script src="../js/jquery.js"></script>
		<script src="../js/jquery.form.js"></script>
		<script src="../js/jquery-ui/jquery-ui.js"></script>
		<script src="../js/jquery-alerts/jquery.alerts.js"
			type="text/javascript"></script>
		<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
		<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"
			type="text/css" media="screen" />

		<script type="text/javascript" charset="utf-8"
			src="../ueditor/js/ueditor/ueditor.config.js"></script>
		<script type="text/javascript" charset="utf-8"
			src="../ueditor/js/ueditor/ueditor.all.js"> </script>
		<script type="text/javascript" charset="utf-8"
			src="../ueditor/js/ueditor/lang/zh-cn/zh-cn.js"></script>

		<link type="text/css" rel="stylesheet" href="../ueditor/js/ueditor/third-party/video-js/video-js.css" />
		<script language="javascript" type="text/javascript" src="../ueditor/js/ueditor/third-party/video-js/video.js"></script>
		<script language="javascript" type="text/javascript" src="../ueditor/js/ueditor/third-party/video-js/html5media.min.js"></script>
			
		<script>		
var uEditor;
function loadMenu(){
	if (parent.leftFrame != null){
		parent.leftFrame.location.href="left_menu.jsp";
	}
	
	uEditor = UE.getEditor('myEditor',{
		initialContent : '<span style="color:gray;font-size:14px;">快速回复</span>',//初始化编辑器的内容  
		toolleipi:true,//是否显示，设计器的 toolbars
		textarea: 'content',
		enableAutoSave: false,  
		//选择自己需要的工具按钮名称,此处仅选择如下五个
		toolbars:[[
		'fullscreen','undo', 'redo', '|',
	           'bold', 'italic', 'underline','|','forecolor',
	           'rowspacingtop', 'rowspacingbottom', 'lineheight', '|',
	           'customstyle', 'paragraph', 'fontfamily', 'fontsize', '|',
	           'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify'
		]],
		//focus时自动清空初始化时的内容
		//autoClearinitialContent:true,
		//关闭字数统计
		wordCount:false,
		//关闭elementPath
		elementPathEnabled:false,
		//默认的编辑区域高度
		initialFrameHeight:150
		///,iframeCssUrl:"css/bootstrap/css/bootstrap.css" //引入自身 css使编辑器兼容你网站css
		//更多其他参数，请参考ueditor.config.js中的配置项
	});
	
	 uEditor.addListener('focus', function(){
     	var content = uEditor.getContentTxt();
     	if(content=="快速回复"){
     		 uEditor.setContent("");
     	}
     }); 
     uEditor.addListener('blur', function(){
     	var content = uEditor.getContent();
     	if(content==""){
     		 uEditor.setContent("<span style='color:gray;font-size:14px;'>快速回复</span>");
     	}
     }); 
	
}

function form_onsubmit()
{
	errmsg = "";
	if (uEditor.getContentTxt()=="" || uEditor.getContentTxt() == '快速回复')
		errmsg += "请填写内容！\n"
	
	if (errmsg!="")
	{
		jAlert(errmsg,"提示");
		return false;
	}else{
		return true;
	}
}
</script>
		<title>查看通知</title>
		<style>
.loading {
	display: none;
	position: fixed;
	z-index: 1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
}

.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity =   20);
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
	filter: alpha(opacity =   80);
}

.userItem {
	width: 70px;
	float: left;
	padding-top: 5px;
}

.attImg {
	border: none;
	max-width: 800px;
	cursor: pointer;
	width: expression ( 
		 function(img) { 
		 	img.onload = function() { 
			 this.style.width =   ( this.width >   800 ) ? '800px' : this.width + 'px'
			};
			return '800px';
		}
		(this)	
	);
}
.mybtn {
	background-color: #87c3f1 !important;
	font-weight: bold;
	text-align: center;
	line-height: 35px;
	height: 35px;
	width: 120px;
	padding-right: 8px;
	padding-left: 8px;
	-moz-border-radius: 3px;
	-webkit-border-radius: 3px;
	border-radius: 3px;
	behavior: url(../skin/common/ie-css3.htc);
	cursor: pointer;
	color: #fff;
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
	<body onload="loadMenu()">
	<%if(is_forced_res == 1 && isNotReply) {%>
	 <div class="tips">
        	<span class="icon_close"></span>
        	该通知须回复
        </div>
        <%} %>
		<div id="treeBackground" class="treeBackground"></div>
		<div id='loading' class='loading'>
			<img src='../images/loading.gif' />
		</div>
		<%
			if (op.equals("reply")) {
				NoticeReplyMgr am = new NoticeReplyMgr();
				boolean re = false;
				try {
		%>
		<script>
		$(".treeBackground").addClass("SD_overlayBG2");
		$(".treeBackground").css({"display":"block"});
		$(".loading").css({"display":"block"});
	</script>
		<%
			re = am.saveReply(application, request);
		%>
		<script>
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
	</script>
		<%
			if (re) {
						json.put("ret", "1");
						json.put("msg", "操作成功!");
						out.print(json);
						return;
					} else {
						json.put("ret", "0");
						json.put("msg", "操作失败!");
						out.print(json);
						return;
					}
				} catch (Exception e) {
					out.print(StrUtil.jAlert_Back(e.getMessage(), "提示"));
					return;
				}
				//if (re) {
				//out.print(StrUtil.Alert_Redirect("发布成功！", "notice_detail.jsp?id="+am.getNoticeId()+"&isShow="+am.getIsShow()));
				//} else {
				//out.print(StrUtil.Alert_Back("发布失败！"));
				//}
				//return;
			}
		%>
		<table cellspacing="0" cellpadding="0" width="100%">
			<tbody>
				<tr>
					<td class="tdStyle_1" style="border: none">
						<img src="../images/left/icon-notice.gif" />
						<a href="notice_list.jsp">通知公告</a>
					</td>
				</tr>
			</tbody>
		</table>

		<table width="100%" border="0" class="tabStyle_1 percent98"
			style="table-layout: fixed; word-wrap: break-word; word-break; break-all; margin-top: 15px;">
			<tr>
				<%
					String title1 = nd.getTitle();
					if (title1.length() > 30) {
						title1 = title1.substring(0, 30) + "......";
					}
				%>
				<td colspan=2 align="center" valign="middle"
					class="tabStyle_1_title"><%=title1%></td>
			</tr>
			<tr>
				<td colspan=2 valign="top">
					<div class="msgContent">
						<div style="margin: 10px 0px 15px 0px; text-align: center">
							发布者：<%=user.getRealName()%>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;发布日期：<%=DateUtil
					.format(nd.getCreateDate(), "yyyy-MM-dd HH:mm:ss")%></div>
						<div style="line-height: 1.5"><%=nd.getContent()%></div>
						<div style="margin-top: 10px">
							<%
								if (flowId > 0) {
							%>
							<a href="javascript:;"
								onclick="addTab('流程消息','flow_modify.jsp?flowId=<%=nd.getFlowId()%>')"><font
								color="blue">查看流程</font>
							</a>
							<%
								}
							%>
						</div>

						<%
							int i = 0;
							while (ir.hasNext()) {
								NoticeAttachmentDb nad = (NoticeAttachmentDb) ir.next();
								i++;
						%>
						<div>
							<img src="../images/attach2.gif" width="17" height="17" />
							<!--<a target="_blank" href="../<%=nad.getVisualPath()%><%=nad.getDiskName()%>"><%=nad.getName()%></a>-->
							<a target="_blank"
								href="notice_getfile.jsp?noticeId=<%=nd.getId()%>&attachId=<%=nad.getId()%>"><%=nad.getName()%></a>
						</div>
						<%
							String extName = StrUtil.getFileExt(nad.getName());
							if (StrUtil.isImage(extName)) {
						%>
						<div style="margin-top: 10px">
							<img class="attImg"
								src="<%=request.getContextPath()%>/img_show.jsp?path=<%=nad.getVisualPath()%><%=nad.getDiskName()%>"
								title="点击查看原图"
								onclick="window.open('<%=request.getContextPath()%>/img_show.jsp?path=<%=nad.getVisualPath()%>/<%=nad.getDiskName()%>')" />
						</div>
						<%
							}
						}
						if (strIsShow.equals("1") && (privilege.isUserPrivValid(request, "notice")
										|| privilege.isUserPrivValid(request, "notice.dept")) && uName.equals(nd.getUserName())) {
						%>
						<div style="margin-top: 15px">
							<%
								NoticeReplyDb npd = new NoticeReplyDb();
								Vector vu = npd.getNoticeReadOrNot((long) id, 1);
							%>
							已查看通知的用户：<%=vu.size()%>人
							<br />
							<%
								ir = vu.iterator();
										String users = "";
										String uNames = "";
										UserDb ud = new UserDb();
										while (ir.hasNext()) {
											uNames = (String) ir.next();
											ud = ud.getUserDb(uNames);
											users += "<div class='userItem'><a target='_blank' href='../user_info.jsp?userName="
													+ StrUtil.UrlEncode(ud.getName())
													+ "'>"
													+ ud.getRealName() + "</a></div>";
										}
							%>
							<%=users%>
						</div>
						<br />
						<div style="clear: both; padding-top: 5px">
							<%
								vu = npd.getNoticeReadOrNot((long) id, 0);
							%>
							未查看通知的用户：<%=vu.size()%>人
							<br />
							<%
								ir = vu.iterator();
										users = "";
										while (ir.hasNext()) {
											uNames = (String) ir.next();
											ud = ud.getUserDb(uNames);
											users += "<div class='userItem'><a href='javascript:;' onclick='addTab(\""
													+ StrUtil.getNullStr(ud.getRealName())
													+ "\",\""
													+ Global.getFullRootPath(request)
													+ "/user_info.jsp?userName="
													+ StrUtil.UrlEncode(ud.getName())
													+ "\")'>"
													+ StrUtil.getNullStr(ud.getRealName())
													+ "</a></div>";
										}
							%>
							<%=users%>
						</div>
						<%
						}
						%>
					</div>
				</td>
			</tr>
			<tr class="message_style_tr">
				<td align="center" colspan="2">
					<input type="button" class="mybtn"
						onclick="window.location.href='notice_list.jsp'" value="返回" />
				</td>
			</tr>
		</table>
		<%
			NoticeReplyMgr nrm = new NoticeReplyMgr();
			boolean res = nrm.canReplay(id, uName);
		%>
		<form name="form2" id="form2" action="" method="post"
			enctype="multipart/form-data">
			<div style="display: none; width: 100%">
				<table width="98%">
					<tr>
						<td colspan="2" class="showMsg_Table_td" style="width: 100%;"
							style="border:none">
							<div id="myEditor" style="height: 100px; width: 100%"></div>
							<input type="hidden" id="noticeid" name="noticeid"
								value="<%=id%>" />
							<input type="hidden" id="uName" name="uName" value="<%=uName%>" />
							<input type="hidden" id="isShow" name="isShow"
								value="<%=strIsShow%>" />
						</td>
					</tr>
					<tr class="message_style_tr">
						<td colspan="2" class="showMsg_Table_td" align="center"
							style="border: none">
							<input name="button" type="submit" value="确定"
								style="margin-top: 10px" class="blue_btn_90" />
						</td>
					</tr>
				</table>
			</div>
		</form>
		<%
			if(nd.getIs_reply() ==1) {
		%>
		<div class="reply_div">
			<div class="reply_title_div clearfix">
				<span class="reply_title">
				回复
				</span>
				<%
				if(content.equals("") && isReplyExist) {
				%>
				<textarea name="myReplyTextareaContent" id="myReplyTextareaContent" class="myTextarea" ></textarea>
				<input type="button" name="hf" class="con_btn org_btn right"  value="回复" noticeId="<%=id%>" />
				<div style="clear:both"></div>
				<%
				}
				// 如果内容不为空且不是发起人
				if(!content.equals("") && !nd.getUserName().equals(uName)) {
				%>
				<ul class="reply_ul" >
						<li>
							<div>
								<span class="reply_name"><%=name %></span>
								<span class="reply_date"><%=DateUtil.format(nnrd.getReplyTime(), "yyyy-MM-dd HH:mm:ss")%></span>
							</div>
							<div class="li_re_content"><%=content%></div>
						</li>
				</ul>
				<%}%>
			</div>
			<% 
				if((privilege.isUserPrivValid(request, "notice")
										|| privilege.isUserPrivValid(request, "notice.dept")) && nd.getUserName().equals(uName)){
			%>
			<ul class="reply_ul" >
				<% 
					while(irnrd.hasNext()){
						NoticeReplyDb nrd2 = (NoticeReplyDb)irnrd.next();
						//String isReaded = StrUtil.getNullStr(nrd2.getIsReaded());
						if(!StrUtil.getNullStr(nrd2.getContent()).equals("")){
							String rRname = new UserDb(nrd2.getUsername()).getRealName();
						%>
						<li>
							<div>
								<span class="reply_name"><%=rRname %></span>
								<span class="reply_date"><%=DateUtil.format(nrd2.getReplyTime(), "yyyy-MM-dd HH:mm:ss")%></span>
							</div>
							<div class="li_re_content"><%=StrUtil.getNullStr(nrd2.getContent()) %></div>
						</li>
				<% 		}
					}
				%>
			</ul>
			<% }%>
		</div>
		<%} %>
	</body>
	<script>
	//展开 回复
 $(document).ready(function(){
	 $(".icon_close").click(function(){
			$(".tips").hide();

		 })
	
	$(".org_btn").click(function(){
		var name = '<%=uName%>';
		var myTextarea = $(".myTextarea").val();
		var noticeId = $(this).attr("noticeId");
		var time2 = new Date().Format("yyyy-MM-dd hh:mm:ss");  
		var data = {"content":myTextarea,"noticeId":noticeId}
		$.ajax({
			type: "post",
			url: "notice_do.jsp?op=addReply",
			data:data,
			dataType:"json",
			beforeSend: function(XMLHttpRequest){
				$(".treeBackground").addClass("SD_overlayBG2");
				$(".treeBackground").css({"display":"block"});
				$(".loading").css({"display":"block"});
			},
			complete: function(XMLHttpRequest, status){
				$(".loading").css({"display":"none"});
				$(".treeBackground").css({"display":"none"});
				$(".treeBackground").removeClass("SD_overlayBG2");
			},
			success: function(data, status){
				var res = data.res;
				if(res == 0){
					jAlert_Redirect("操作成功！","提示", window.location.href);
				}else{
					jAlert("操作失败！","提示");
				}
				
			},
			error: function(){
				jAlert("操作失败！","提示");
			}
		});
	})
		


	  
	var options = { 
		success:showResponse,  // post-submit callback 
		beforeSubmit:    form_onsubmit,
		url:"notice_detail.jsp?op=reply&noticeid=<%=id%>20$43_&<%=uName%>&isShow=<%=strIsShow%>"
		}; 
		$('#form2').submit(function() { 
		   $(this).ajaxSubmit(options); 
		   return false; 
		});
	});
function showResponse(data){
	data = $.parseJSON(data);
	if(data.ret == "1"){
		jAlert(data.msg,"提示");
		window.location.href="notice_detail.jsp?isShow=<%=strIsShow%>&id="+$("#noticeid").val();
	}
}
 </script>
</html>
