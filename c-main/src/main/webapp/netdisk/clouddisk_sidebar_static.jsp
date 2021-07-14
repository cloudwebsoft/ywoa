<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@page import="cn.js.fan.web.Global"%>
<%@page import="cn.js.fan.web.SkinUtil"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="com.redmoon.oa.message.MessageDb"%>
<%@page import="com.redmoon.oa.flow.WorkflowDb"%>
<jsp:useBean id="privilege" scope="page"
			class="com.redmoon.oa.pvg.Privilege" />
<html>
  <head>
  	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<style type="text/css">
		body{background:#f3f3f3;float:left;font-family:"微软雅黑"};
		:link, :visited , ins {text-decoration: none;}
		a {text-decoration:none;}
		fieldset, img { border: 0;}
		.upNotice{float:left;display:inline; width:180px;display:block}
		.downNotice{float:left;display:inline;height:75px; width:180px;display:block}
		.flowNotice{float:left;display:inline;display:block;overflow:hidden;}
		.flowNotice div{float:left;display:inline;text-align:left;width:180px;text-overflow:ellipsis;white-space:nowrap;color:#606060}
		.flowNotice div a{color:#606060}
		.flowNotice a:hover {color:#2cbb79;text-outline:#2cbb79}
		.msgNotice{float:left;display:inline;display:block;overflow:hidden;}
		.msgNotice div{float:left;display:inline;text-align:left;width:180px;text-overflow:ellipsis;white-space:nowrap;color:#606060}
		.msgNotice div a{color:#606060}
		.msgNotice a:hover {color:#2cbb79;text-outline:#2cbb79}
		.msgNotice div {float:left;font-size:13px;height:18px;overflow:hidden;}
		.flowNotice div {float:left;font-size:13px;height:18px;overflow:hidden;}
	 	.imgsDiv{float:left; height:240px;width:180px;margin-left:-3px;}
	 	.msg_count{
			width:24px;
			height:24px;
			background:url(images/msg_count.png) no-repeat;
			color:#fff;
			position: absolute;
			z-index: 999;
			font-size:12px;
			text-align:center;
			vertical-align:middle; 
			line-height:24px;
			left:28px;
			bottom: 18px;
		}
	 	.picture{
			float:left;margin-right:32px;margin-top:20px;
			position: relative;
		}
		.picture_side{
			float:left;margin-top:20px;
			position: relative;
		}
	 	
	</style>
	<%	
		if (!privilege.isUserLogin(request)) {
			out.print("对不起，请先登录！");
			 return;
		}
		String priv="read";
		if (!privilege.isUserPrivValid(request,priv)) {
			out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
     	String userName = ParamUtil.get(request,"userName"); 
     	if("".equals(userName)){
			userName = privilege.getUser(request);
		}
		String authKey = userName + "|" + DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
		authKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(com.redmoon.clouddisk.socketServer.CloudDiskThread.OA_KEY,authKey);
     %>
    <script type="text/javascript">
     var user_name="<%=userName%>";
     //--------img图片的触碰点击JS效果   
     function pic_over(pic){
     	var imgs = document.getElementById(pic).getElementsByTagName("img") ;
	   	var src = imgs[0].src;
	   	var a = src.lastIndexOf("/")+1;
		var b = src.lastIndexOf("_");
		var visualPath = src.substring(0,a);
		img = src.substring(a,b);  //img是不带效果的图片问题 如：longin
     	imgs[0].src = visualPath+img+"_move.png";
     }
     function pic_out(pic){
     	var imgs = document.getElementById(pic).getElementsByTagName("img") ;
	   	var src = imgs[0].src;
	   	var a = src.lastIndexOf("/")+1;
		var b = src.lastIndexOf("_");
		var visualPath = src.substring(0,a);
		img = src.substring(a,b);  //img是不带效果的图片问题 如：longin
     	imgs[0].src = visualPath+img+"_default.png";
     }
     function pic_down(pic){
     	var imgs = document.getElementById(pic).getElementsByTagName("img") ;
	   	var src = imgs[0].src;
	   	var a = src.lastIndexOf("/")+1;
		var b = src.lastIndexOf("_");
		var visualPath = src.substring(0,a);
		img = src.substring(a,b);  //img是不带效果的图片问题 如：longin
     	imgs[0].src = visualPath+img+"_click.png";
     }
     function pic_up(pic){
     	var imgs = document.getElementById(pic).getElementsByTagName("img") ;
	   	var src = imgs[0].src;
	   	var a = src.lastIndexOf("/")+1;
		var b = src.lastIndexOf("_");
		var visualPath = src.substring(0,a);
		img = src.substring(a,b);  //img是不带效果的图片问题 如：longin
     	imgs[0].src = visualPath+img+"_move.png";
     }
     
    </script>
  </head>
  
<body scrolling="no" onload="" oncontextmenu="return false" style="height:270px;FONT-SIZE:10pt;border: 0px;overflow: auto;FONT-FAMILY:aria;l">
  <%
  	SideBarMgr sbMgr = new SideBarMgr();
	List<SideBarBean> list = sbMgr.querySideBar(userName);
	String topic = sbMgr.getTopicString(userName);

	String[] topics = null;

	if (topic.equals(",")) {
		topics = null;
	} else if (topic.startsWith(",")) {
		topics = new String[] { topic.substring(1)};
	} else if (topic.endsWith(",")) {
		topics = new String[] { topic.substring(0, topic.length() - 1) };
	} else {
		topics = StrUtil.split(topic, ",");
	}
	com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config.getInstance();
	int flowCount = cfg.getIntProperty("sidebar_flow_count");
	int msgCount = cfg.getIntProperty("sidebar_msg_count");
	int maxCount = flowCount + msgCount;
	
	// 取得待办流程的条数
	int flowWaitCount = WorkflowDb.getWaitCount(userName);
	// 内部邮件的数目
	MessageDb md = new MessageDb();
	int msgNewCount = md.getNewInnerMsgCount(userName);
	
	NetDiskSideBar ndsb = new NetDiskSideBar();
	Iterator<Integer> irch = ndsb.getFlowNoticeTopicByStatic(userName,flowCount).iterator();
	WorkflowDb wf = new WorkflowDb();
	SideBarMgr sbm = new SideBarMgr();
	String upHidden = "";
	String downHidden = "display:none";
	if(sbm.getWhichHidden(userName) == 999){
		upHidden = "";
		downHidden = "display:none";
	}else{
		upHidden = "display:none";
		downHidden = "";
	}
	if(msgNewCount + flowWaitCount == 0){
		upHidden = "display:none";
		downHidden = "display:none";
	}
   %>
   
    <div style="height:270px; width:180px;background:#f3f3f3">
    	<div class="notice upNotice" style="<%=upHidden %>" >
    		<div class="flowNotice">
    			<%
	                irch = ndsb.getFlowNoticeTopicByStatic(userName,flowCount).iterator();
	                int i = 1;
					while (irch.hasNext()) {
						int id = irch.next();
						wf = new WorkflowDb(id);
    			 %>
    			<div class = 'flowTd<%=i %>' style='margin-top:8px'><b><a target='_blank' title = '[<%=DateUtil.format(wf.getMydate(), "yyyy-MM-dd HH:mm") %>]<%=wf.getTitle() %>' href='<%=Global.getFullRootPath(request) %>/public/clouddisk_login.jsp?op=sidebarFlow&authKey=<%=authKey %>&id=<%=wf.getId() %>'>[流程]  <%=wf.getTitle() %> </a></b></div>
				<%
					++i;}
				 %>
			</div>
			<div class="msgNotice">
				<%
	                irch = ndsb.getMsgNoticeTopicByStatic(userName,msgCount).iterator();
	                i = 1;
					while (irch.hasNext()) {
						int id = irch.next();
						md = new MessageDb(id);
    			 %>
    			 <div class = 'msgTd<%=i %> msgItem' style='margin-top:8px' onclick="this.style.display='none'"><b><a target='_blank' title = '[<%=md.getSendTime() %>]<%=md.getTitle() %>' href='<%=Global.getFullRootPath(request) %>/public/clouddisk_login.jsp?op=sidebarMsg&authKey=<%=authKey %>&id=<%=md.getId() %> '>[消息]  <%=md.getTitle() %></a></b></div>
    			 <%
    			 	++i;}
    			  %>
			</div>	    		
    	</div>
    	<div class="imgsDiv">
    		<% 
    			irch = sbm.getTheId(userName).iterator();
    			int zInt = 1;
    			while(irch.hasNext()){
    				int id = irch.next();
    				SideBarDb sbd = new SideBarDb();
    				sbd = sbd.getSideBarDb(id);
    				String cssTemp = "";
    				int picHidden = sbd.getIs_show();
    				if(sbd.getIs_show() == 1){
    					if(zInt != 3 && zInt !=6 && zInt !=9 && zInt !=12){
    						cssTemp = "class='picture' onmouseover='pic_over(this.id)' onmouseout='pic_out(this.id)' onmousedown='pic_down(this.id)' onmouseup='pic_up(this.id)'";
    					}else{
    						cssTemp = "class='picture_side' onmouseover='pic_over(this.id)' onmouseout='pic_out(this.id)' onmousedown='pic_down(this.id)' onmouseup='pic_up(this.id)'";
    					}
    				}else{
    					cssTemp = "style='display:none'";
    				}
	    		 	if(sbd.getHref().equals("message_oa/message_ext/message.jsp") || sbd.getHref().equals("message_oa/message_frame.jsp")){
	    		 		if(msgNewCount > 0){
    		 			%>
    		 			 <div <%=cssTemp %> id='pic_<%=sbd.getPosition() %>' ><span class='msg_count'><%=msgNewCount %></span><a href='<%=Global.getFullRootPath(request) %>/public/clouddisk_login.jsp?op=sidebarLink&title=<%=StrUtil.UrlEncode(sbd.getTitle()) %>&authKey=<%=authKey %>&link=<%=sbd.getHref() %>' target='_blank'><img src='images/appImages/<%=sbd.getPicture() %>' title='<%=sbd.getTitle() %>'  is_open='<%=sbd.getIs_show() %>' value='<%=sbd.getHref() %>' width='36px' height='36px'/></a></div>
    		 			<%
	    		 		}else{
	    		 		  if(sbd.getCustom() == 0){%>
	    		 			<div <%=cssTemp %> id='pic_<%=sbd.getPosition()%>'  ><a href='<%=Global.getFullRootPath(request) %>/public/clouddisk_login.jsp?op=sidebarLink&title=<%=StrUtil.UrlEncode(sbd.getTitle()) %>&authKey=<%=authKey %>&link=<%=sbd.getHref() %>' target='_blank'><img src='images/appImages/<%=sbd.getPicture() %>' title='<%=sbd.getTitle() %>'  is_open='<%=sbd.getIs_show() %>' value='<%=sbd.getHref() %>' width='36px' height='36px'/></a></div> 
	    		 		<%}else{%>
	    		 			<div <%=cssTemp %> id='pic_<%=sbd.getPosition()%>'  ><a href='<%=Global.getFullRootPath(request) %>/public/clouddisk_login.jsp?op=sidebarCustomer&authKey=<%=authKey %>&link=<%=sbd.getHref() %>' target='_blank'><img src='images/appImages/<%=sbd.getPicture() %>' title='<%=sbd.getTitle() %>'  is_open='<%=sbd.getIs_show() %>' value='<%=sbd.getHref() %>' width='36px' height='36px'/></a></div> 
	    		 		<%}
	    		 		}
	    		 	 }else if (sbd.getHref().equals("flow/flow_list.jsp?displayMode=1")){
	    		 	 	if(flowWaitCount > 0){
	    		 	 	%>
	    		 	 	<div <%=cssTemp %> id='pic_<%=sbd.getPosition() %>' ><span class='msg_count'><%=flowWaitCount %></span><a href='<%=Global.getFullRootPath(request) %>/public/clouddisk_login.jsp?op=sidebarLink&title=<%=StrUtil.UrlEncode(sbd.getTitle()) %>&authKey=<%=authKey %>&link=<%=sbd.getHref() %>' target='_blank'><img src='images/appImages/<%=sbd.getPicture() %>' title='<%=sbd.getTitle() %>'  is_open='<%=sbd.getIs_show() %>' value='<%=sbd.getHref() %>' width='36px' height='36px'/></a></div>
	    		 	 	<%}else{
		    		 	 	if(sbd.getCustom() == 0){%>
		    		 			<div <%=cssTemp %> id='pic_<%=sbd.getPosition()%>'  ><a href='<%=Global.getFullRootPath(request) %>/public/clouddisk_login.jsp?op=sidebarLink&title=<%=StrUtil.UrlEncode(sbd.getTitle()) %>&authKey=<%=authKey %>&link=<%=sbd.getHref() %>' target='_blank'><img src='images/appImages/<%=sbd.getPicture() %>' title='<%=sbd.getTitle() %>'  is_open='<%=sbd.getIs_show() %>' value='<%=sbd.getHref() %>' width='36px' height='36px'/></a></div> 
		    		 		<%}else{%>
		    		 			<div <%=cssTemp %> id='pic_<%=sbd.getPosition()%>'  ><a href='<%=Global.getFullRootPath(request) %>/public/clouddisk_login.jsp?op=sidebarCustomer&authKey=<%=authKey %>&link=<%=sbd.getHref() %>' target='_blank'><img src='images/appImages/<%=sbd.getPicture() %>' title='<%=sbd.getTitle() %>'  is_open='<%=sbd.getIs_show() %>' value='<%=sbd.getHref() %>' width='36px' height='36px'/></a></div> 
		    		 		<%}
	    		 		}
	    		 	 }else{
	    		 	 	if(sbd.getCustom() == 0){%>
	    		 			<div <%=cssTemp %> id='pic_<%=sbd.getPosition()%>'  ><a href='<%=Global.getFullRootPath(request) %>/public/clouddisk_login.jsp?op=sidebarLink&title=<%=StrUtil.UrlEncode(sbd.getTitle()) %>&authKey=<%=authKey %>&link=<%=sbd.getHref() %>' target='_blank'><img src='images/appImages/<%=sbd.getPicture() %>' title='<%=sbd.getTitle() %>'  is_open='<%=sbd.getIs_show() %>' value='<%=sbd.getHref() %>' width='36px' height='36px'/></a></div> 
	    		 		<%}else{%>
	    		 			<div <%=cssTemp %> id='pic_<%=sbd.getPosition()%>'  ><a href='<%=Global.getFullRootPath(request) %>/public/clouddisk_login.jsp?op=sidebarCustomer&authKey=<%=authKey %>&link=<%=sbd.getHref() %>' target='_blank'><img src='images/appImages/<%=sbd.getPicture() %>' title='<%=sbd.getTitle() %>'  is_open='<%=sbd.getIs_show() %>' value='<%=sbd.getHref() %>' width='36px' height='36px'/></a></div> 
	    		 		<%}
	    			}
	    			zInt++;
    			}
    		%>
    	</div>
    	<div class="notice downNotice" style="<%=downHidden %>">
			<div class="flowNotice">
				<%
	                irch = ndsb.getFlowNoticeTopicByStatic(userName,flowCount).iterator();
	                int z = 1;
					while (irch.hasNext()) {
						int id = irch.next();
						wf = new WorkflowDb(id);
    			 %>
    			<div class = 'flowTd<%=z %>' style='margin-top:8px'><b><a target='_blank' title = '[<%=wf.getMydate() %>]<%=wf.getTitle() %>' href='<%=Global.getFullRootPath(request) %>/public/clouddisk_login.jsp?op=sidebarFlow&authKey=<%=authKey %>&id=<%=wf.getId()%>'>[流程]  <%=wf.getTitle() %> </a></b></div>
				<%
					++z;}
				 %>
			</div>
			<div class="msgNotice">
				<%
	                irch = ndsb.getMsgNoticeTopicByStatic(userName,msgCount).iterator();
	                z = 1;
					while (irch.hasNext()) {
						int id = irch.next();
						md = new MessageDb(id);
    			 %>
    			 <div class = 'msgTd<%=z %> msgItem' style='margin-top:8px' onclick="this.style.display='none'"><b><a target='_blank' title = '[<%=md.getSendTime() %>]<%=md.getTitle() %>' href='/public/clouddisk_login.jsp?op=sidebarMsg&authKey=<%=authKey %>&id=<%=md.getId() %> '>[消息]  <%=md.getTitle() %></a></b></div>
    			 <%
    			 	++z;}
    			  %>
			</div>	  
    	</div>
    </div>
  </body>
</html>
