<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.notice.*" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="pvg" scope="page" class="com.redmoon.oa.android.Privilege"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link rel="stylesheet" href="css/reset.css" />
<link rel="stylesheet" href="css/notice_detatil.css" />
<link rel="stylesheet"  href="css/my_dialog.css"/>
<script src="js/zepto.js"></script>
<script type="js/jq_mydialog.js"></script>
<title>查看通知</title>
</head>
<% 
String skey = ParamUtil.get(request, "skey");
JSONObject json = new JSONObject();
boolean re = pvg.Auth(skey);
if(re){
	try {
		json.put("res","-2");
		json.put("msg","时间过期");
		out.print(json.toString());
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return;
}
pvg.doLogin(request, skey);
long id= ParamUtil.getLong(request, "id",22001);
NoticeDb noticeDb = new NoticeDb(id);
if(noticeDb == null){
	noticeDb = new NoticeDb();
}
String userName = privilege.getUser(request);
String cUserRealName = new UserDb(userName).getRealName();

String title = StrUtil.getNullStr(noticeDb.getTitle());
String content = StrUtil.getNullStr(noticeDb.getContent());
String createDate = "";
try{
	createDate = DateUtil.parseDate(DateUtil.format(noticeDb.getCreateDate(), DateUtil.DATE_TIME_FORMAT));
}catch (Exception e) {
	
}
String nName = noticeDb.getUserName();
String realName = new UserDb(nName).getRealName();
int is_show = noticeDb.getIsShow();
int is_forced_res = noticeDb.getIs_forced_response();
int is_reply = noticeDb.getIs_reply();
boolean isShowUser = false;
int knowUserCount = 0;
int unknowUserCount = 0;
String knowUsers = "";
String unKnowUsers = "";
boolean isNoticeAll = privilege.isUserPrivValid(request, "notice");
if(is_show == 1){
	if (isNoticeAll || privilege.isUserPrivValid(request, "notice.dept")) {
		isShowUser = true;
		NoticeReplyDb npd = new NoticeReplyDb();
		Vector userKnowVec = npd.getNoticeReadOrNot(id,1);
		Vector userUnknowVec = npd.getNoticeReadOrNot(id,0);
		knowUserCount = userKnowVec.size();
		unknowUserCount = userUnknowVec.size();
		Iterator userKnowIt = userKnowVec.iterator(); //已查看用户
		Iterator userUnknowIt = userUnknowVec.iterator();//未查看用户
		while(userKnowIt.hasNext()){
			String un = (String)userKnowIt.next();
			String rName = new UserDb(un).getRealName();
			if(knowUsers.equals("")){
				knowUsers += rName;
			}else{
				knowUsers += ","+rName;				
			}
			
		}
		while(userUnknowIt.hasNext()){
			String un = (String)userUnknowIt.next();
			String rName = new UserDb(un).getRealName();
			if(unKnowUsers.equals("")){
				unKnowUsers += rName;
			}else{
				unKnowUsers +=","+rName;
			}
		}
		
	}
	
	
}
//回复
boolean isReplyExist = true;
NoticeReplyDb nrd = new NoticeReplyDb();
int notice_id = (int)id;
Iterator nrIt = nrd.getNoticeReply(notice_id).iterator();
//当前回复信息
nrd.setUsername(userName);
nrd.setNoticeid(id);
nrd = nrd.getReply();
if(nrd == null){
	nrd = new NoticeReplyDb();
	isReplyExist = false;
}
String CurReplyContent = StrUtil.getNullStr(nrd.getContent());
String CurReplyName = StrUtil.getNullStr(nrd.getUsername());
boolean isReaded = "1".equals(nrd.getIsReaded());

// 当前用户尚未回复内容  // 当前用户不是 通知发布者
boolean isNotReply = userName.equals(CurReplyName) && (CurReplyContent.equals(""));

if (isReplyExist && !isReaded && is_forced_res == 0){
	java.util.Date rDate = new java.util.Date();
	
	NoticeReplyDb nrdb = new NoticeReplyDb();
	nrdb.setIsReaded("1");
	nrdb.setReadTime(rDate);
	nrdb.setNoticeid((long) id);
	nrdb.setUsername(userName);
	nrdb.saveStatus();
	
}

%>

<body>
	<!--
        	作者：2634516637@qq.com
        	时间：2016-09-29
        	描述：通知公告详情
        -->
      	<%if(is_forced_res == 1 && isNotReply){ %>
			 <div class="tips">
		        	<span class="icon_close"></span>
		        	该通知需强制回复
		     </div>
        <%} %>
		<div class="notice_body">
			<h3 class="n_title"><%=title %></h3>
			<div class="n_user">
				<span><%=realName %></span>
				<span><%=createDate %></span>
			</div>
			<p class="n_content"><%=content %>
			<%
				if(isShowUser){
			%>
			</p>
			<%
				if(unknowUserCount>0){
			%>
			<div class="n_look_user" >
				<span>未查看用户<font class="green">(<%=unknowUserCount%>)</font>：</span>
				<p class="green"><%=unKnowUsers %></p>
			</div>
			<%}
			if(knowUserCount>0){
			%>
			
				<div class="n_look_user" >
					<span>已查看用户<font class="green">(<%=knowUserCount%>)</font>：</span>
					<p ><%=knowUsers %></p>
				</div>
			<%} %>
			
			<% }%>
			<%
				Vector attVec =	noticeDb.getAttachs();
				Iterator attIt = attVec.iterator();
				while(attIt.hasNext()){
					NoticeAttachmentDb nad = (NoticeAttachmentDb)attIt.next();
					String att_name = nad.getName();
					long att_id = nad.getId();
			%>
			<div class="n_att">
				<div><a class="a_att" href="notice_getfile.jsp?attId=<%=att_id%>&skey=<%=skey %>" ><%=att_name %></a></div>
			</div>
			<%} %>	
		</div>
		<% if(is_reply == 1){%>
				<ul class="reply_time_shaft">
					<%
						if(userName.equals(nName) || isNoticeAll){
							while(nrIt.hasNext()){
								NoticeReplyDb replyDb = (NoticeReplyDb)nrIt.next();
								String replyDate = "";
								String replyName = replyDb.getUsername();
								UserDb ud  = new UserDb(replyName);
								String replyRealName = ud.getRealName();
								String photo = StrUtil.getNullStr(ud.getPhoto());
								String defaultUrl = ud.getGender() ==1?"img/avatar_male.png":"img/avatar_female.png";
								String avater_url = photo.equals("")?defaultUrl:photo;
								String replyContent = StrUtil.getNullStr(replyDb.getContent());
								long reply_id = replyDb.getId();
								try{
									replyDate = DateUtil.parseDate(DateUtil.format(replyDb.getReplyTime(), DateUtil.DATE_TIME_FORMAT));
								}catch (Exception e) {
									
								}
								if(!replyContent.equals("")){
						%>
							
					
					
					<li class="clearfix" id="reply_<%=reply_id %>">
						<span class="time"><%=replyDate %></span>
						<div class="contain" >
							<div>
							<span class="userName"><%=replyRealName %></span>
							</div>
							<div class="content"><%=replyContent %></div>
						</div>
					</li>
					<%
								}
								}
					}else{
						if(nrd != null && !StrUtil.getNullStr(nrd.getContent()).equals("")){
							String cReplyDate = "";
							String cReplyName = nrd.getUsername();
							UserDb cUd  = new UserDb(cReplyName);
							String cReplyRealName = cUd.getRealName();
							String cPhoto = StrUtil.getNullStr(cUd.getPhoto());
							String cDefaultUrl = cUd.getGender() ==1?"img/avatar_male.png":"img/avatar_female.png";
							String cAvater_url = cPhoto.equals("")?cDefaultUrl:cPhoto;
							String cReplyContent = StrUtil.getNullStr(nrd.getContent());
							long cReply_id = nrd.getId();
							try{
								cReplyDate = DateUtil.parseDate(DateUtil.format(nrd.getReplyTime(), DateUtil.DATE_TIME_FORMAT));
							}catch (Exception e) {
								
							}
						
						%>
								
								<li class="clearfix" id="reply_<%=cReply_id %>">
									<span class="time"><%=cReplyDate %></span>
									<div class="contain" >
										<div><%--
											<div class="avaterDiv">
						       					 <img src="<%=cAvater_url %>" width="48" height="48" />
						   					 </div>
						   					 --%><span class="userName"><%=cReplyRealName %></span>
						   					
										</div>
										<div class="content"><%=cReplyContent %></div>
									</div>
								</li>
							
							
					<%	}
						
					}
					
					
					%>
				</ul>
		
			<% 
				if(!CurReplyName.equals("")){
			%>
				<div class="n_reply">
					<input type="text" class="input_reply" name="reply" />
					<input type="button" class="btn_reply" value="回复" />
				</div>
			<%} %>
		
		
		<%} %>
			

</body>
<script>
$(".tips").tap(function(){
		$(this).hide();
})
$(".btn_reply").click(function(){
	var content = $(".input_reply").val();
	var notice_id = '<%=notice_id%>';
	var data = {"op":"addReply","content":content,"noticeId":notice_id};
	$.ajax({
	  type: 'POST',
	  url: 'do/notice_do.jsp',
	  // data to be added to query string:
	  data: data,
	  // type of data we are expecting in return:
	  dataType: 'json',
	  success: function(data){
		  var res = data.res;
		  var replyId = data.replyId;
		  var liSelector = $("#reply_"+replyId);
		  var length = $("#reply_"+replyId).length;
		  var realName = '<%=cUserRealName%>';
		  $(".input_reply").val("");
		  $(".tips").hide();
		  if(length>0){
			  liSelector.find(".content").text(content);
			  liSelector.find(".time").text("今天");
	      }else{
		      var li ='<li class="clearfix" id="reply_'+replyId+'">';
			  li+= '<span class="time">今天</span>';
			  li+= '<div class="contain">';
			  li+= '<div>'
			  li+='<span class="userName">'+realName+'</span>';
			  li+=' <span class="comment"></span>'
			  li+='</div>'
			  li+= '<div class="content">'+content+'</div>'
			  li+='</div>'
			$(".reply_time_shaft").prepend(li);			 
	    	
		    

		  }
	   
	  },
	  error: function(xhr, type){
	    alert('Ajax error!')
	  }
	})
})






</script>
</html>
