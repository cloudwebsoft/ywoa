<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.redmoon.oa.ui.LocalUtil"%>
<%@ page import="com.redmoon.oa.db.SequenceManager"%>
<%@ page import="java.util.Date"%>
<%@ page import="com.redmoon.oa.message.MessageDb"%>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@page import="com.redmoon.oa.ui.SkinMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<%
String priv = "read";
if (!privilege.isUserPrivValid(request,priv)) {
	%>
	<link href="common.css" rel="stylesheet" type="text/css">	
	<%
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

WorkflowMgr wfm = new WorkflowMgr();

String action = ParamUtil.get(request, "action");

if (!action.equals("addReply") && !action.equals("addReplyDispose")) {
	%>
<script type="text/javascript" src="js/jquery.js"></script>
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
	<%
}

if (action.equals("recall")) {
	int flowId = ParamUtil.getInt(request, "flow_id");
	long myActionId = ParamUtil.getLong(request, "action_id");
	try {
		boolean re = wfm.recallMyAction(request, myActionId);
		if (re) {
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb(flowId);			
			// 判断是否为@流程
			WorkflowPredefineDb wpd = new WorkflowPredefineDb();
			wpd = wpd.getPredefineFlowOfFree(wf.getTypeCode());
			String str = LocalUtil.LoadString(request, "res.common","info_op_success");
			if (wpd.isLight()) {
				
				out.print(StrUtil.jAlert_Redirect(str,"提示", "flow_dispose_light.jsp?myActionId=" + myActionId));
			}
			else {
				out.print(StrUtil.jAlert_Redirect(str,"提示", "flow_modify.jsp?flowId=" + flowId));
			}
		}
		else {
			String str = LocalUtil.LoadString(request, "res.flow.Flow","noNodeCanBeWithdrawn");
			out.print(StrUtil.jAlert_Back(str,"提示"));
		}
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
	}
	return;
}
else if (action.equals("deliver")) {
	boolean re = false;
	int flowId = ParamUtil.getInt(request, "flowId", -1);
	try {
		re = wfm.deliverFree(request, flowId);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
	}
	if (re) {
		WorkflowPredefineDb wpd = new WorkflowPredefineDb();
		WorkflowDb wf = new WorkflowDb();
		wf = wf.getWorkflowDb(flowId);
	  	wpd = wpd.getPredefineFlowOfFree(wf.getTypeCode());
	  	String str = LocalUtil.LoadString(request, "res.common","info_op_success");
		if (wpd.isLight()) {
			out.print(StrUtil.jAlert_Redirect(str,"提示", "flow_dispose_light_show.jsp?flowId=" + flowId));
			//out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_dispose_light_show.jsp?flowId=" + flowId));
		}
		else {
			out.print(StrUtil.jAlert_Redirect(str,"提示", "flow_modify.jsp?flowId=" + flowId));
			//out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "flow_modify.jsp?flowId=" + flowId));
		}
		
	}
	else{
		String str = LocalUtil.LoadString(request, "res.common","info_op_fail");
		out.print(StrUtil.jAlert_Back(str,"提示"));
	}
	out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");	
	return;
}
//提交“评论”
else if(action.equals("addReply")){
	// flow_dispose.jsp回复
	JSONObject json = new JSONObject();
	String myActionId = ParamUtil.get(request, "myActionId");//当前活跃的标志id
	String discussId = ParamUtil.get(request, "discussId");//每条“评论”的id
	//WorkflowAnnexDb wfaDb = new WorkflowAnnexDb(StrUtil.toInt(discussId));
	long flowId = ParamUtil.getLong(request, "flow_id");//当前流程id
	long actionId = ParamUtil.getLong(request, "action_id");//当前流程action的id
	String replyContent = request.getParameter("content");//“评论”的内容
	String userRealName = request.getParameter("userRealName");
	String userName = request.getParameter("user_name");
	String replyName = request.getParameter("reply_name");
	String flowName = request.getParameter("flow_name");
	int parentId = ParamUtil.getInt(request, "parent_id", -1);
	
	UserMgr um = new UserMgr();
	UserDb oldUser = um.getUserDb(userName);
	UserDb replyUser = um.getUserDb(replyName);
	
	String partakeUsers = "";
	int isSecret = ParamUtil.getInt(request,"isSecret",0);//此“评论”是否隐藏
	//将数据插入flow_annex附言表中
	long id = (long) SequenceManager.nextID(SequenceManager.OA_FLOW_ANNEX);
	String currentDate = DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss");
	String myDate = currentDate;

	int progress = ParamUtil.getInt(request, "cwsProgress", 0);	
	StringBuilder sql = new StringBuilder("insert into flow_annex (id,flow_id,content,user_name,reply_name,add_date,action_id,is_secret,parent_id,progress) values(");
	sql.append(id).append(",").append(flowId).append(",").append(StrUtil.sqlstr(replyContent))
	.append(",").append(StrUtil.sqlstr(userName)).append(",").append(StrUtil.sqlstr(replyName))
	.append(",").append(StrUtil.sqlstr(myDate)).append(",").append(actionId).append(",").append(isSecret).append(",").append(parentId).append(",").append(progress).append(")");
	JdbcTemplate rmconn = new JdbcTemplate();
	try {
		rmconn.executeUpdate(sql.toString());
		
		//不管来源于“代办流程”还是“我的流程”，跳转之后都进入“我的流程”。如果这条回复是私密的，只给交流双方发送消息提醒，不然就给这条流程的每个人都发送一条消息提醒
		WorkflowDb wf = new WorkflowDb((int)flowId);
		
		// 写入进度
		Leaf lf = new Leaf();
		lf = lf.getLeaf(wf.getTypeCode());
		String formCode = lf.getFormCode();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		// 进度为0的时候不更新
		if (fd.isProgress() && progress>0) {
			com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
			fdao = fdao.getFormDAO((int)flowId, fd);
			fdao.setCwsProgress(progress);
			fdao.save();
		}
		
		MessageDb md = new MessageDb();
	    String myAction = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + flowId;
	    MyActionDb mad = new MyActionDb();
	    if (!myActionId.equals("")) {
			mad = mad.getMyActionDb(Long.parseLong(myActionId));
	    }
	    if(isSecret != 0){//如果是隐藏“评论”，只提醒发起“意见”的人
	    	if(!replyName.equals(userName)){//如果发起“意见”的人不是自己，就提醒
	    		if (!myActionId.equals("")) {
	    			md.sendSysMsg(replyName, "请注意查看我的流程："+wf.getTitle(), userRealName+"对意见："+mad.getResult()+"发表了评论：<p>"+replyContent+"</p>", myAction);
	    		} else {
	    			md.sendSysMsg(replyName, "请注意查看我的流程："+wf.getTitle(), userRealName + "发表了评论：<p>"+replyContent+"</p>", myAction);
	    		}
	    	}
	    }else{//如果不是隐藏“评论”，提醒所有参与流程的人
	    	//解析得到参与流程的所有人
	       	String allUserListSql = "select distinct user_name from flow_my_action where flow_id="+ flowId + " order by receive_date asc";
	    	ResultIterator ri1 = rmconn.executeQuery(allUserListSql);
	    	ResultRecord rr1 = null;
	    	while (ri1.hasNext()) {
	    		rr1 = (ResultRecord)ri1.next();
	    		partakeUsers += rr1.getString(1)+",";
	    	}
	    	if(!partakeUsers.equals("")){
	    		partakeUsers = partakeUsers.substring(0,partakeUsers.length()-1);
	    	}
	    	String[] partakeUsersArr = StrUtil.split(partakeUsers, ",");
			for(String user : partakeUsersArr){
				//如果不是自己就提醒
				if(!user.equals(userName)){
					if (!myActionId.equals("")) {
						md.sendSysMsg(user, "请注意查看我的流程："+wf.getTitle(), userRealName+"对意见："+mad.getResult()+"发表了评论：<p>"+replyContent+"</p>", myAction);
					} else {
		    			md.sendSysMsg(user, "请注意查看我的流程："+wf.getTitle(), userRealName + "发表了评论：<p>"+replyContent+"</p>", myAction);
		    		}
				}
			}
	    }
		//if(myActionId != null && !myActionId.equals("")){//来自于“代办流程”
		//	out.print(StrUtil.Alert_Redirect("发表评论成功！", "flow_dispose_light.jsp?myActionId="+myActionId));
		//}else{//来自于“我的流程”
		//	out.print(StrUtil.Alert_Redirect("发表评论成功！", "flow_dispose_light_show.jsp?flowId="+flowId));
		//}
	    json.put("ret", "1");
	    json.put("myDate", currentDate);
	    
	    StringBuilder sr = new StringBuilder();
	    WorkflowPredefineDb wpd = new WorkflowPredefineDb();
	    wpd = wpd.getPredefineFlowOfFree(wf.getTypeCode());
	    
	    String othersHidden = SkinUtil.LoadStr(request, "res.flow.Flow", "othersHidden");
	    String needHidden = SkinUtil.LoadStr(request, "res.flow.Flow", "needHidden");
	    String replyTo = SkinUtil.LoadStr(request, "res.flow.Flow", "replyTo");
	    String sure = SkinUtil.LoadStr(request, "res.flow.Flow", "sure");
	    if (wpd.isLight()) {
		    if(parentId==-1){
			    sr.append("<tr><td width=\"50\" class=\"nameColor\" style=\"text-align:left;\">")
			    .append(replyUser.getRealName()).append(":</td>")
			    .append("<td width=\"70%\" style=\"text-align:left;word-break:break-all;\">").append(replyContent).append("</td>")
			    .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
			    .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
			    .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>")
			   // .append("<tr id=trline0 ><td colspan=3><hr/></td></tr>" );
			   .append("<tr id=trline").append(id).append(" ><td colspan=3><hr class=\"hrLine\"/></td></tr>" );
			    
			    sr.append("<tr><td colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
			    .append("<form id=flowForm").append(id).append(" name=flowForm").append(id).append(" method=post >")
			    .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
			    .append("<input type=hidden name=myActionId value= >")
			    .append("<input type=hidden name=discussId value=").append(id).append(" >")	    
			    .append("<input type=hidden name=flow_id value=").append(flowId).append(" >")
			    .append("<input type=hidden name=action_id value=").append(actionId).append(" >")
			    .append("<input type=hidden name=user_name value=").append(userName).append(" >")
			    .append("<input type=hidden name=userRealName value=").append(userRealName).append(" >")
			    .append("<input type=hidden name=reply_name value=").append(replyName).append(" >")
			    .append("<input type=hidden name=parent_id value=").append(id).append(" >")
			    .append("<input type=hidden name=discussId value=").append(parentId).append(" >")
			    .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
			    .append(id).append(",").append(id).append( ") />")
			    .append("</form></div></td></tr>");
		    }else{
			    sr.append("<tr><td width=\"180\" class=\"nameColor\" style=\"text-align:left;\">").append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
			    .append(replyUser.getRealName()).append("&nbsp;").append(replyTo).append("&nbsp;").append(oldUser.getRealName()).append(":</td>	")
			    .append("<td width=\"70%\" style=\"text-align:left;word-break:break-all;\">").append(replyContent).append("</td>")
			    .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
			    .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
			    .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>");
			    
			    sr.append("<tr><td colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
			    .append("<form id=flowForm").append(id).append(" name=flowForm").append(id).append(" method=post >")
			    .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
			    .append("<input type=hidden name=myActionId value= >")
			    .append("<input type=hidden name=discussId value=").append(id).append(" >")	    
			    .append("<input type=hidden name=flow_id value=").append(flowId).append(" >")
			    .append("<input type=hidden name=action_id value=").append(actionId).append(" >")
			    .append("<input type=hidden name=user_name value=").append(replyName).append(" >")
			    .append("<input type=hidden name=userRealName value=").append(userRealName).append(" >")
			    .append("<input type=hidden name=reply_name value=").append(replyName).append(" >")
			    .append("<input type=hidden name=parent_id value=").append(parentId).append(" >")
			    .append("<input type=hidden name=discussId value=").append(parentId).append(" >")
			    .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
			    .append(id).append(",").append(parentId).append( ") />")
			    .append("</form></div></td></tr>");
		    }
	    } else {
	    	if(parentId==-1){
			    sr.append("<tr><td width=\"50\" class=\"nameColor\" style=\"text-align:left;\">")
			    .append(replyUser.getRealName()).append(":</td>")
			    .append("<td width=\"70%\" style=\"text-align:left;word-break:break-all;\">").append(replyContent).append("</td>")
			    .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
			    .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
			    .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>")
			   // .append("<tr id=trline0 ><td colspan=3><hr/></td></tr>" );
			   .append("<tr id=trline").append(id).append(" ><td colspan=3><hr class=\"hrLine\"/></td></tr>" );
			    
			    sr.append("<tr><td align=\"left\" colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
			    .append("<form id=flowForm").append(id).append(" name=flowForm").append(id).append(" method=post >")
			    .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
			    .append("<span align=\"left\" title=\"").append(othersHidden).append("\" style=\"cursor:pointer;\" onclick=\"chooseHideComment(this);\"><img src=\"").append(SkinMgr.getSkinPath(request)).append("/images/admin/functionManage/checkbox_not.png\" />&nbsp;").append(needHidden).append("<input type=\"hidden\" id=\"isSecret0\" name=\"isSecret0\" value=\"0\"/></span>")
			    .append("<input type=hidden id=myActionId").append(id).append(" value= >")
			    .append("<input type=hidden id=discussId").append(id).append(" value=").append(id).append(" >")	    
			    .append("<input type=hidden id=flow_id").append(id).append(" value=").append(flowId).append(" >")
			    .append("<input type=hidden id=action_id").append(id).append(" value=").append(actionId).append(" >")
			    .append("<input type=hidden id=user_name").append(id).append(" value=").append(userName).append(" >")
			    .append("<input type=hidden id=userRealName").append(id).append(" value=").append(userRealName).append(" >")
			    .append("<input type=hidden id=reply_name").append(id).append(" value=").append(replyName).append(" >")
			    .append("<input type=hidden id=parent_id").append(id).append(" value=").append(id).append(" >")
			    .append("<input type=hidden id=discussId").append(id).append(" value=").append(parentId).append(" >")
			    .append("<input type=hidden id=isSecret").append(id).append(" value=").append(isSecret).append(" >")
			    .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
			    .append(id).append(",").append(id).append( ") />")
			    .append("</form></div></td></tr>");
		    }else{
			    sr.append("<tr><td width=\"180\" class=\"nameColor\" style=\"text-align:left;\">").append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
			    .append(oldUser.getRealName()).append("&nbsp;").append(replyTo).append("&nbsp;").append(replyUser.getRealName()).append(":</td>	")
			    .append("<td style=\"text-align:left;\">").append(replyContent).append("</td>")
			    .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
			    .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
			    .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>");
			    
			    sr.append("<tr><td align=\"left\" colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
			    .append("<form id=flowForm").append(id).append(" name=flowForm").append(id).append(" method=post >")
			    .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
			    .append("<span align=\"left\" title=\"").append(othersHidden).append("\" style=\"cursor:pointer;\" onclick=\"chooseHideComment(this);\"><img src=\"").append(SkinMgr.getSkinPath(request)).append("/images/admin/functionManage/checkbox_not.png\" />&nbsp;").append(needHidden).append("<input type=\"hidden\" id=\"isSecret").append(id).append("\" name=\"isSecret").append(id).append("\" value=\"0\"/></span>")
			    .append("<input type=hidden id=myActionId").append(id).append(" value= >")
			    .append("<input type=hidden id=discussId").append(id).append(" value=").append(id).append(" >")	    
			    .append("<input type=hidden id=flow_id").append(id).append(" value=").append(flowId).append(" >")
			    .append("<input type=hidden id=action_id").append(id).append(" value=").append(actionId).append(" >")
			    .append("<input type=hidden id=user_name").append(id).append(" value=").append(userName).append(" >")
			    .append("<input type=hidden id=userRealName").append(id).append(" value=").append(userRealName).append(" >")
			    .append("<input type=hidden id=reply_name").append(id).append(" value=").append(userName).append(" >")
			    .append("<input type=hidden id=parent_id").append(id).append(" value=").append(parentId).append(" >")
			    .append("<input type=hidden id=discussId").append(id).append(" value=").append(parentId).append(" >")
			    .append("<input type=hidden id=isSecret").append(id).append(" value=").append(isSecret).append(" >")
			    .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
			    .append(id).append(",").append(parentId).append( ") />")
			    .append("</form></div></td></tr>");
		    }
	    }
	    
	    json.put("result", sr.toString());

		out.print(json);
	} catch (Exception e) {e.printStackTrace();
		//if(myActionId != null && !myActionId.equals("")){
		//	out.print(StrUtil.Alert_Redirect("发表评论失败！", "flow_dispose_light.jsp?myActionId="+myActionId));
		//}else{
		//	out.print(StrUtil.Alert_Redirect("发表评论失败！", "flow_dispose_light_show.jsp?flowId="+flowId));
		//}
		//json.put("ret", "2");
		//json.put("result", "发表评论失败！");
		//out.print(json);
		//return;
	} finally {
		if (rmconn != null) {
			rmconn.close();
		}
	}
	return;
}else if(action.equals("addReplyDispose")){
	// flow_modify.jsp回复
	JSONObject json = new JSONObject();
	String myActionId = ParamUtil.get(request, "myActionId");//当前活跃的标志id
	String discussId = ParamUtil.get(request, "discussId");//每条“评论”的id
	long flowId = ParamUtil.getLong(request, "flow_id");//当前流程id
	long actionId = ParamUtil.getLong(request, "action_id");//当前流程action的id
	String replyContent = ParamUtil.get(request, "content");//“评论”的内容
 	String userRealName = ParamUtil.get(request, "userRealName");//发起“评论”人真实姓名
	String userName = ParamUtil.get(request, "user_name");
	String replyName = ParamUtil.get(request, "reply_name");
	String flowName = ParamUtil.get(request, "flow_name");
	int parentId = ParamUtil.getInt(request, "parent_id", -1);
	
	UserMgr um = new UserMgr();
	UserDb oldUser = um.getUserDb(userName);
	UserDb replyUser = um.getUserDb(replyName);
	
	String partakeUsers = "";
	int isSecret = ParamUtil.getInt(request,"isSecret",0);//此“评论”是否隐藏
	//将数据插入flow_annex附言表中
	long id = (long) SequenceManager.nextID(SequenceManager.OA_FLOW_ANNEX);
	String currentDate = DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss");
	String myDate = currentDate;
	
	int progress = ParamUtil.getInt(request, "cwsProgress", 0);	

	StringBuilder sql = new StringBuilder("insert into flow_annex (id,flow_id,content,user_name,reply_name,add_date,action_id,is_secret,parent_id,progress) values(");
	sql.append(id).append(",").append(flowId).append(",").append(StrUtil.sqlstr(replyContent))
	.append(",").append(StrUtil.sqlstr(userName)).append(",").append(StrUtil.sqlstr(replyName))
	.append(",").append(StrUtil.sqlstr(myDate)).append(",").append(actionId).append(",").append(isSecret).append(",").append(parentId).append(",").append(progress).append(")");
	JdbcTemplate rmconn = new JdbcTemplate();
	try {
		rmconn.executeUpdate(sql.toString());
		
		//不管来源于“代办流程”还是“我的流程”，跳转之后都进入“我的流程”。如果这条回复是私密的，只给交流双方发送消息提醒，不然就给这条流程的每个人都发送一条消息提醒
		WorkflowDb wf = new WorkflowDb((int)flowId);
		
		// 写入进度
		Leaf lf = new Leaf();
		lf = lf.getLeaf(wf.getTypeCode());
		String formCode = lf.getFormCode();
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
		// 进度为0的时候不更新
		if (fd.isProgress() && progress>0) {
			com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
			fdao = fdao.getFormDAO((int)flowId, fd);
			fdao.setCwsProgress(progress);
			fdao.save();
		}
		
		MessageDb md = new MessageDb();
	    String myAction = "action=" + MessageDb.ACTION_FLOW_SHOW + "|flowId=" + flowId;
	    MyActionDb mad = new MyActionDb();
	    if (!myActionId.equals("")) {
			mad = mad.getMyActionDb(Long.parseLong(myActionId));
	    }
	    if(isSecret != 0){//如果是隐藏“评论”，只提醒发起“意见”的人
	    	if(!replyName.equals(userName)){//如果发起“意见”的人不是自己，就提醒
	    		if (!myActionId.equals("")) {
	    			md.sendSysMsg(replyName, "请注意查看我的流程："+wf.getTitle(), userRealName+"对意见："+mad.getResult()+"发表了评论：<p>"+replyContent+"</p>", myAction);
	    		} else {
	    			md.sendSysMsg(replyName, "请注意查看我的流程："+wf.getTitle(), userRealName + "发表了评论：<p>"+replyContent+"</p>", myAction);
	    		}
	    	}
	    }else{//如果不是隐藏“评论”，提醒所有参与流程的人
	    	//解析得到参与流程的所有人
	       	String allUserListSql = "select distinct user_name from flow_my_action where flow_id="+ flowId + " order by receive_date asc";
	    	ResultIterator ri1 = rmconn.executeQuery(allUserListSql);
	    	ResultRecord rr1 = null;
	    	while (ri1.hasNext()) {
	    		rr1 = (ResultRecord)ri1.next();
	    		partakeUsers += rr1.getString(1)+",";
	    	}
	    	if(!partakeUsers.equals("")){
	    		partakeUsers = partakeUsers.substring(0,partakeUsers.length()-1);
	    	}
	    	String[] partakeUsersArr = StrUtil.split(partakeUsers, ",");
			for(String user : partakeUsersArr){
				//如果不是自己就提醒
				if(!user.equals(userName)){
					if (!myActionId.equals("")) {
						md.sendSysMsg(user, "请注意查看我的流程："+wf.getTitle(), userRealName+"对意见："+mad.getResult()+"发表了评论：<p>"+replyContent+"</p>", myAction);
					} else {
						md.sendSysMsg(user, "请注意查看我的流程："+wf.getTitle(), userRealName + "发表了评论：<p>"+replyContent+"</p>", myAction);
					}
				}
			}
	    }
		//if(myActionId != null && !myActionId.equals("")){//来自于“代办流程”
		//	out.print(StrUtil.Alert_Redirect("发表评论成功！", "flow_dispose_light.jsp?myActionId="+myActionId));
		//}else{//来自于“我的流程”
		//	out.print(StrUtil.Alert_Redirect("发表评论成功！", "flow_dispose_light_show.jsp?flowId="+flowId));
		//}
	    json.put("ret", "1");
	    json.put("myDate", currentDate);

	    String othersHidden = SkinUtil.LoadStr(request, "res.flow.Flow", "othersHidden");
	    String needHidden = SkinUtil.LoadStr(request, "res.flow.Flow", "needHidden");
	    String replyTo = SkinUtil.LoadStr(request, "res.flow.Flow", "replyTo");
	    String sure = SkinUtil.LoadStr(request, "res.flow.Flow", "sure");
	    StringBuilder sr = new StringBuilder();
	    if(parentId==-1){
		    sr.append("<tr><td width=\"50\" class=\"nameColor\" style=\"text-align:left;\">")
		    .append(replyUser.getRealName()).append(":</td>")
		    .append("<td width=\"70%\" style=\"text-align:left;word-break:break-all;\">").append(replyContent).append("</td>")
		    .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
		    .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
		    .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>")
		   // .append("<tr id=trline0 ><td colspan=3><hr/></td></tr>" );
		   .append("<tr id=trline").append(id).append(" ><td colspan=3><hr class=\"hrLine\"/></td></tr>" );
		    
		    sr.append("<tr><td align=\"left\" colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
		    .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
		    .append("<span align=\"left\" title=\"").append(othersHidden).append("\" style=\"cursor:pointer;\" onclick=\"chooseHideComment(this);\"><img src=\"").append(SkinMgr.getSkinPath(request)).append("/images/admin/functionManage/checkbox_not.png\" />&nbsp;").append(needHidden).append("<input type=\"hidden\" id=\"isSecret0\" name=\"isSecret0\" value=\"0\"/></span>")
		    .append("<input type=hidden id=myActionId").append(id).append(" value= >")
		    .append("<input type=hidden id=discussId").append(id).append(" value=").append(id).append(" >")	    
		    .append("<input type=hidden id=flow_id").append(id).append(" value=").append(flowId).append(" >")
		    .append("<input type=hidden id=action_id").append(id).append(" value=").append(actionId).append(" >")
		    .append("<input type=hidden id=user_name").append(id).append(" value=").append(userName).append(" >")
		    .append("<input type=hidden id=userRealName").append(id).append(" value=").append(userRealName).append(" >")
		    .append("<input type=hidden id=reply_name").append(id).append(" value=").append(replyName).append(" >")
		    .append("<input type=hidden id=parent_id").append(id).append(" value=").append(id).append(" >")
		    .append("<input type=hidden id=discussId").append(id).append(" value=").append(parentId).append(" >")
		    .append("<input type=hidden id=isSecret").append(id).append(" value=").append(isSecret).append(" >")
		    .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
		    .append(id).append(",").append(id).append( ") />")
		    .append("</div></td></tr>");
	    }else{
		    sr.append("<tr><td width=\"180\" class=\"nameColor\" style=\"text-align:left;\">").append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
		    .append(oldUser.getRealName()).append("&nbsp;").append(replyTo).append("&nbsp;").append(replyUser.getRealName()).append(":</td>	")
		    .append("<td width=\"70%\" style=\"text-align:left;word-break:break-all;\">").append(replyContent).append("</td>")
		    .append("<td style=\"text-align:right;\">").append(myDate).append("&nbsp;&nbsp;&nbsp;&nbsp;")
		    .append("<a align=\"right\" class=\"comment\" href=\"javascript:;\" onclick=\"addMyReply(").append(id).append(") \">")
		    .append("<img title=\"").append(replyTo).append("\" src=\"images/dateline/replyto.png\"/></a></td></tr>");
		    
		    sr.append("<tr><td align=\"left\" colspan=3>").append("<div id=myReplyTextarea").append(id).append(" style='display:none; clear:both;position:relative;margin-bottom:40px'>")
		    .append("<textarea name=content id=get").append(id).append(" class=myTextarea></textarea>")
		    .append("<span align=\"left\" title=\"").append(othersHidden).append("\" style=\"cursor:pointer;\" onclick=\"chooseHideComment(this);\"><img src=\"").append(SkinMgr.getSkinPath(request)).append("/images/admin/functionManage/checkbox_not.png\" />&nbsp;").append(needHidden).append("<input type=\"hidden\" id=\"isSecret").append(id).append("\" name=\"isSecret").append(id).append("\" value=\"0\"/></span>")
		    .append("<input type=hidden id=myActionId").append(id).append(" value= >")
		    .append("<input type=hidden id=discussId").append(id).append(" value=").append(id).append(" >")	    
		    .append("<input type=hidden id=flow_id").append(id).append(" value=").append(flowId).append(" >")
		    .append("<input type=hidden id=action_id").append(id).append(" value=").append(actionId).append(" >")
		    .append("<input type=hidden id=user_name").append(id).append(" value=").append(userName).append(" >")
		    .append("<input type=hidden id=userRealName").append(id).append(" value=").append(userRealName).append(" >")
		    .append("<input type=hidden id=reply_name").append(id).append(" value=").append(userName).append(" >")
		    .append("<input type=hidden id=parent_id").append(id).append(" value=").append(parentId).append(" >")
		    .append("<input type=hidden id=discussId").append(id).append(" value=").append(parentId).append(" >")
		    .append("<input type=hidden id=isSecret").append(id).append(" value=").append(isSecret).append(" >")
		    .append("<input class=mybtn value=").append(sure).append(" type=button onclick=submitPostscript(")
		    .append(id).append(",").append(parentId).append( ") />")
		    .append("</div></td></tr>");
	    }
	    
	    json.put("result", sr.toString());

		out.print(json);
	} catch (Exception e) {e.printStackTrace();
		//if(myActionId != null && !myActionId.equals("")){
		//	out.print(StrUtil.Alert_Redirect("发表评论失败！", "flow_dispose_light.jsp?myActionId="+myActionId));
		//}else{
		//	out.print(StrUtil.Alert_Redirect("发表评论失败！", "flow_dispose_light_show.jsp?flowId="+flowId));
		//}
		//json.put("ret", "2");
		//json.put("result", "发表评论失败！");
		//out.print(json);
		//return;
	} finally {
		if (rmconn != null) {
			rmconn.close();
		}
	}
	return;
}
try{
	wfm.doUpload(application, request);
}catch(ErrMsgException e){
	out.clear();
	out.print(e.getMessage());
	return;
}

String op = wfm.getFieldValue("op");
String strFlowId = wfm.getFieldValue("flowId");
int flowId = Integer.parseInt(strFlowId);
String strActionId = wfm.getFieldValue("actionId");
int actionId = Integer.parseInt(strActionId);
String strMyActionId = wfm.getFieldValue("myActionId");
long myActionId = Long.parseLong(strMyActionId);

WorkflowDb wf = wfm.getWorkflowDb(flowId);

String myname = privilege.getUser( request );
WorkflowActionDb wa = new WorkflowActionDb();
wa = wa.getWorkflowActionDb(actionId);
if (!wa.isLoaded()) {
	String str = LocalUtil.LoadString(request, "res.flow.Flow","notBeingHandle");
	out.print(SkinUtil.makeErrMsg(request, str));
	return;
}

MyActionDb myActionDb = new MyActionDb();
myActionDb = myActionDb.getMyActionDb(myActionId);
String result = wfm.getFieldValue("cwsWorkflowResult");
myActionDb.setResult(result);
if(op!= null && !op.trim().equals("saveformvalue")){
	 myActionDb.setChecked(true);
 }
myActionDb.save();

if (op.equals("return")) {
	try {
		boolean re = wfm.ReturnAction(request, wf, wa, myActionId);
		if (re) {
			JSONObject json = new JSONObject();
			json.put("ret", "1");
			json.put("op", op);
			String str = LocalUtil.LoadString(request, "res.common","info_op_success");
			json.put("msg", str);
			out.print(json);			
		}
		else {
			JSONObject json = new JSONObject();
			json.put("ret", "0");
			String str = LocalUtil.LoadString(request, "res.common","info_op_fail");
			json.put("msg", str);
			json.put("op", op);
			out.print(json);			
		}
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Redirect(e.getMessage(), "flow_dispose_free.jsp?myActionId=" + myActionId));
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);		
	}
	return;
}
else if (op.equals("finish")) {
	try {
		try {
			wfm.checkLock(request, wf);
		} catch (ErrMsgException e1) {
			myActionDb.setChecked(false);
			myActionDb.save();
			JSONObject json = new JSONObject();
			json.put("ret", "0");
			json.put("msg", e1.getMessage());
			json.put("op", op);
			out.print(json);
			return;
		}
		boolean re = wfm.FinishActionFree(request, wf, wa, myActionId);
		if (re) {
			// 如果后继节点中有一个节点是由本人继续处理，且已处于激活状态，则继续处理这个节点
			MyActionDb mad = wa.getNextActionDoingWillBeCheckedByUserSelf(privilege.getUser(request));
			if (mad!=null) {
				// out.print(StrUtil.Alert_Redirect("操作成功！请点击确定，继续处理下一节点！", "flow_dispose_free.jsp?myActionId=" + mad.getId()));
				JSONObject json = new JSONObject();
				json.put("ret", "1");
				json.put("op", op);
				json.put("nextMyActionId", "" + mad.getId());
				String str = LocalUtil.LoadString(request, "res.flow.Flow","clickOk");
				json.put("msg", str);
				out.print(json);				
			}
			else {
				JSONObject json = new JSONObject();
				json.put("ret", "1");
				json.put("op", op);
				json.put("nextMyActionId", "");
				String str = LocalUtil.LoadString(request, "res.common","info_op_success");
				json.put("msg", str);
				out.print(json);				
			}
			return;
		}
		else {
			// out.print(StrUtil.Alert_Redirect("操作失败！", "flow_dispose_free.jsp?myActionId=" + myActionId));
			JSONObject json = new JSONObject();
			json.put("ret", "0");
			String str = LocalUtil.LoadString(request, "res.common","info_op_fail");
			json.put("msg", str);
			json.put("op", op);
			out.print(json);
			return;
		}
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Redirect(e.getMessage(), "flow_dispose_free.jsp?myActionId=" + myActionId));
		e.printStackTrace();
		
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);
		
		return;
	}
}
else if (op.equals("manualFinish")) {
	boolean re = false;
	try {
		re = wfm.saveFormValue(request, wf, wa);
		re = wfm.ManualFinish(request, flowId, myActionId);
		if (re) {
			myActionDb.setResultValue(WorkflowActionDb.RESULT_VALUE_DISAGGREE);
			myActionDb.save();
		}		
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Back(e.getMessage()));
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);		
		return;
	}
	if (re) {
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("op", op);
		String str = LocalUtil.LoadString(request, "res.common","info_op_success");
		json.put("msg", str);
		out.print(json);		
	}
	else {
		// out.print(StrUtil.Alert_Back("操作失败！"));
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		String str = LocalUtil.LoadString(request, "res.common","info_op_fail");
		json.put("msg", str);
		json.put("op", op);
		out.print(json);		
	}
	return;
}
// 保存草稿
else if (op.equals("saveformvalue")) {
	boolean re = false;
	try {
		re = wfm.saveFormValue(request, wf, wa);
	}
	catch (ErrMsgException e) {
		// out.print(StrUtil.Alert_Back(e.getMessage()));
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		json.put("op", op);
		out.print(json);
		return;
	}
	// afterXorCondNodeCommit通知flow_dispose.jsp页面，已保存完毕，匹配条件后，自动重定向
	if (re) {
		/*
		if (op.equals("saveformvalue"))
			String str = LocalUtil.LoadString(request, "res.common","info_op_fail");
			out.print(StrUtil.Alert_Redirect(str, "flow_dispose_free.jsp?myActionId=" + myActionId));
		else if (op.equals("AutoSaveArchiveNodeCommit")) {
			response.sendRedirect("flow_dispose_free.jsp?action=afterAutoSaveArchiveNodeCommit&myActionId=" + myActionId);
			return;
		}
		*/
		
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("op", op);
		String str = LocalUtil.LoadString(request, "res.common","info_op_success");
		json.put("msg", str);
		out.print(json);

	}
}
%>