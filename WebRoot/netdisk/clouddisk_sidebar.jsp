<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@page import="cn.js.fan.web.Global"%>
<%@page import="cn.js.fan.web.SkinUtil"%>
<jsp:useBean id="privilege" scope="page"
			class="com.redmoon.oa.pvg.Privilege" />
		<!--	<!DOCTYPE html> -->
<html>
  <head>
  	<link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<link type="text/css" rel="stylesheet" href="css/reset.css" />
	<style type="text/css">
		body{background:#f3f3f3;float:left;};
		.upNotice{float:left;display:inline;padding-top:0px; width:210px;display:none}
		.downNotice{float:left;display:inline;height:75px; width:210px;display:none}
		.flowNotice{float:left;display:inline;display:none;padding-left:10px;overflow:hidden;}
		.flowNotice div{float:left;display:inline;text-align:left;width:200px;text-overflow:ellipsis;white-space:nowrap;color:#606060}
		.flowNotice div a{color:#606060}
		.flowNotice a:hover {color:#2cbb79;text-outline:#2cbb79}
		.msgNotice{float:left;display:inline;display:none;padding-left:10px;overflow:hidden;}
		.msgNotice div{float:left;display:inline;text-align:left;width:200px;text-overflow:ellipsis;white-space:nowrap;color:#606060}
		.msgNotice div a{color:#606060}
		.msgNotice a:hover {color:#2cbb79;text-outline:#2cbb79}
		.msgNotice div {float:left;font-size:13px;height:18px;overflow:hidden;}
		.flowNotice div {float:left;font-size:13px;height:18px;overflow:hidden;}
	 	.imgsDiv{float:left; height:200px;width:210px}
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
			left:37px;
			bottom: 18px;
		}
	 	.picture{
			float:left;margin-right:25px;margin-top:12px;padding-left:7px;
			position: relative;
		}
	 	
	</style>
	<script src="../js/jquery1.7.2.min.js"></script>
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
     	SideBarMgr sbm = new SideBarMgr();
     %>
    <script type="text/javascript">
     var user_name="<%=userName%>";
     var notice_topic;
    	$(function(){
    		var refresh = function(){
    		var msgNotice = "";
    		var flowNotice = "";
    		var imgsDiv = "";
	    	$.ajax({
	    		type:"get",
	    		url:"clouddisk_list_do.jsp",
	    		data:{"op":"getSideBar","user_name":user_name},
	    		success:function(data,status){
	    			data = $.parseJSON(data);
	    			if(data.ret == "1"){
		    			var innerMsgCount = data.innerMsgCount;
		    			var flowWaitCount = data.flowWaitCount;
		    			$.each(data.sideBarArray,function(index,data){
		    				var csstemp;
		    				var hreftemp;
			    			if((data.is_show)=="<%=SideBarDb.IS_SHOW%>"){
			    				csstemp = "class='picture'";
			    			}else{
			    				csstemp = "style='display:none'";
			    			}
			    			if(data.custom=="<%=SideBarDb.NOT_CUSTOM%>"){
			    				hreftemp = "../public/clouddisk_login.jsp?op=sidebarLink&title="+encodeURI(data.title)+"&authKey="+data.authKey+"&link="+data.href;
			    			} else{
			    				hreftemp = "../public/clouddisk_login.jsp?op=sidebarCustomer&authKey="+data.authKey+"&link="+data.href;
			    			}
		    				if(data.position == "<%=SideBarDb.UP_NOTICE%>"){
		    					if(data.is_show == "<%=SideBarDb.IS_SHOW%>"){
		    						$(".upNotice").css({"display":"block"});
		    					}else if(data.is_show == "<%=SideBarDb.NOT_SHOW%>"){
		    						$(".upNotice").css({"display":"none"});
		    					}
		    				}
		    				else if(data.position == "<%=SideBarDb.UP_BUTTON%>"){
		    					if(data.is_show == "<%=SideBarDb.IS_SHOW%>"){
		    						$(".downNotice").css({"display":"block"});
			    				}else if(data.is_show == "<%=SideBarDb.NOT_SHOW%>"){
		    						$(".downNotice").css({"display":"none"});
			    				}
		    				}
		    				else if(data.type == <%=SideBarDb.TYPE_PICTURE%>){//alert(data.href);
			    				if(data.href == 'message_oa/message_ext/message.jsp' || data.href=='message_oa/message_frame.jsp'){
									if(innerMsgCount > 0){
										imgsDiv += "<div "+csstemp+" id='pic_"+data.position+"' ><span class='msg_count'>"+innerMsgCount+"</span><a href='"+hreftemp+"' target='_blank'><img src='img_show.jsp?path=images/appImages/"+data.picture+"' title='"+data.title+"'  is_open='"+data.is_show+"' value='"+data.href+"' width='36px' height='36px'/></a></div>";
									}else{
										imgsDiv += "<div "+csstemp+" id='pic_"+data.position+"' ><a href='"+hreftemp+"' target='_blank'><img src='img_show.jsp?path=images/appImages/"+data.picture+"' title='"+data.title+"'  is_open='"+data.is_show+"' value='"+data.href+"' width='36px' height='36px'/></a></div>"
									}
									//$(".imgsDiv").html(imgsDiv);
			    				}else if(data.href == 'flow/flow_list.jsp?displayMode=1'){
									if(flowWaitCount > 0){
										imgsDiv += "<div "+csstemp+" id='pic_"+data.position+"' ><span class='msg_count'>"+flowWaitCount+"</span><a href='"+hreftemp+"' target='_blank'><img src='img_show.jsp?path=images/appImages/"+data.picture+"' title='"+data.title+"'  is_open='"+data.is_show+"' value='"+data.href+"' width='36px' height='36px'/></a></div>";
									}else{
										imgsDiv += "<div "+csstemp+" id='pic_"+data.position+"' ><a href='"+hreftemp+"' target='_blank'><img src='img_show.jsp?path=images/appImages/"+data.picture+"' title='"+data.title+"'  is_open='"+data.is_show+"' value='"+data.href+"' width='36px' height='36px'/></a></div>";
									}
			    					//$(".imgsDiv").html(imgsDiv);
				    			}else{
				    				imgsDiv += "<div "+csstemp+" id='pic_"+data.position+"' ><a href='"+hreftemp+"' target='_blank'><img src='img_show.jsp?path=images/appImages/"+data.picture+"' title='"+data.title+"'  is_open='"+data.is_show+"' value='"+data.href+"' width='36px' height='36px'/></a></div>";
					    		}
		    					//$("#pic_"+data.position).find("img").attr({"title":data.title,"src":"images/appImages/"+data.picture,"is_open":data.is_show,"value":data.href});
			    			}
			    			else if(data.type == <%=SideBarDb.TYPE_NOTICE_TOPIC%>){
			    				notice_topic = data.title;
			    				var s = notice_topic.split(",");
			    				if(s[0]!= "" && s[1]!= ""){
			    					$(".flowNotice").css({"display":"block"});
			    					$(".msgNotice").css({"display":"block"});
			    				}else if(s[0]!= "" && s[1]== ""){
			    					$(".flowNotice").css({"display":"block"});
			    					$(".msgNotice").css({"display":"none"});
			    				}else if(s[0]== "" && s[1]!= ""){
			    					$(".flowNotice").css({"display":"none"});
			    					$(".msgNotice").css({"display":"block"});
			    				}else{
			    					$(".flowNotice").css({"display":"none"});
			    					$(".msgNotice").css({"display":"none"});
			    				}
			    				if(flowWaitCount == 0){
			    					$(".flowNotice").css({"display":"none"});
			    				}
			    				if(innerMsgCount == 0){
			    					$(".msgNotice").css({"display":"none"});
			    				}
			    			}
						});
						if ($(".imgsDiv").html() != imgsDiv) {   //对比是否有差异，如果有差异就替换，此可以防止刷新页面
							$(".imgsDiv").html(imgsDiv);
						}
						var i = 1;
						$.each(data.sideBarMsgTopic,function(index,data){//系统信息的显示
							
							//$(".msgTd1").html("<b><a target='_blank' title = '"+data.title+"' href='../public/clouddisk_login.jsp?op=sidebarMsg&authKey="+data.authKey+"&id="+data.id+"' >"+data.title+"</a></b>");
							msgNotice += "<div class = 'msgTd"+i+" msgItem' style='margin-top:8px'><b><a target='_blank' title = '["+data.date+"]"+data.title+"' href='../public/clouddisk_login.jsp?op=sidebarMsg&authKey="+data.authKey+"&id="+data.id+"' >[消息]  "+data.title+"</a></b></div>";
							++i;
						});
						
						if ($(".msgNotice").html() != msgNotice) {
							$(".msgNotice").html(msgNotice);
						}
						
						i = 1;
						$.each(data.sideBarFlowTopic,function(index,data){//流程信息的显示
							//alert(data.title);alert(data.id);
							flowNotice += "<div class = 'flowTd"+i+"' style='margin-top:8px'><b><a target='_blank' title = '["+data.date+"]"+data.title+"' href='../public/clouddisk_login.jsp?op=sidebarFlow&authKey="+data.authKey+"&id="+data.id+"'>[流程]  "+data.title+" </a></b></div>";
							//$(".flowTd"+i).html("<b><a target='_blank' title = '"+data.title+"' href='../public/clouddisk_login.jsp?op=sidebarFlow&authKey="+data.authKey+"&id="+data.id+"'>"+data.title+" </a></b>");
							++i;
						});
						if ($(".flowNotice").html() != flowNotice) {
							$(".flowNotice").html(flowNotice);
						}
					}
	    		},
	    		error:function(XMLHttpRequest, textStatus){
					//alert(XMLHttpRequest.responseText);
				}
	    	})
    		}
    	//--------img图片的触碰点击JS效果   
    		$(".picture").live("mouseover",function(){
    			var img = $(this).find("img").attr("src");
				var a = img.lastIndexOf("/")+1;
				var b = img.lastIndexOf("_");
				var visualPath = img.substring(0,a);
				img = img.substring(a,b);  //img是不带效果的图片问题 如：longin
    			$(this).find("img").attr({"src":visualPath+img+"_move.png"});
    		})
    		$(".picture").live("mouseout",function(){
    			var img = $(this).find("img").attr("src");
				var a = img.lastIndexOf("/")+1;
				var b = img.lastIndexOf("_");
				var visualPath = img.substring(0,a);
				img = img.substring(a,b);  //img是不带效果的图片问题 如：longin
    			$(this).find("img").attr({"src":visualPath+img+"_default.png"});
    		})
    		$(".picture").live("mousedown",function(){
    			var img = $(this).find("img").attr("src");
				var a = img.lastIndexOf("/")+1;
				var b = img.lastIndexOf("_");
				var visualPath = img.substring(0,a);
				img = img.substring(a,b);  //img是不带效果的图片问题 如：longin
    			$(this).find("img").attr({"src":visualPath+img+"_click.png"});
    		})
    		$(".picture").live("mouseup",function(){
    			var img = $(this).find("img").attr("src");
				var a = img.lastIndexOf("/")+1;
				var b = img.lastIndexOf("_");
				var visualPath = img.substring(0,a);
				img = img.substring(a,b);  //img是不带效果的图片问题 如：longin
    			$(this).find("img").attr({"src":visualPath+img+"_move.png"});
    		})
    		
    		//var refreshSpace = function(){
    		//	$(window).load(refresh);
    		//}
    		setInterval(refresh, 10000);  //定时器刷新
    		$(window).load(refresh);  //打开加载ajax数据
    		$(".msgItem").live("click",function(){ //点击信息，视为已读，将信息提示消除
    			$(this).remove();
    		})
    	})
    </script>
  </head>
  
<body scroll="no" onload="" oncontextmenu="return false" style="height:270px;FONT-SIZE:10pt;border: 0px;overflow: auto;FONT-FAMILY:aria;l">
  
    <div style=" width:200px;background:#f3f3f3;height:270px;overflow:hidden">
    	<div class="notice upNotice" >
    		<div class="flowNotice">
    			<!-- <div style='color:blue;width:60px;float:left'>[待办流程]</div><div class = "flowTd1">尚未有新流程</div>
    			<div style='color:blue;width:60px;float:left'>[待办流程]</div><div class = "flowTd2">尚未有新流程</div>
    			 
    			<div class = "flowTd1"></div>
    			<div class = "flowTd2"></div>-->
			</div>
			<div class="msgNotice">
				<!-- <div style='color:blue;width:60px;float:left'>[内部消息]</div><div class = "msgTd1">尚未有新信息</div> 
				<div class = "msgTd1"></div>-->
			</div>	    		
    	</div>
    	<div class="imgsDiv">
    	</div>
    	
    	<div class="notice downNotice">
			<div class="flowNotice">
    			<!-- <div style='color:blue;width:60px;float:left'>[待办流程]</div><div class = "flowTd1">尚未有新流程</div>
    			<div style='color:blue;width:60px;float:left'>[待办流程]</div><div class = "flowTd2">尚未有新流程</div>
    			 
    			<div class = "flowTd1"></div>
    			<div class = "flowTd2"></div>-->
			</div>
			<div class="msgNotice">
				<!-- <div style='color:blue;width:60px;float:left'>[内部消息]</div><div class = "msgTd1">尚未有新信息</div> 
				<div class = "msgTd1"></div>-->
			</div>	  
    	</div>
    </div>
  </body>
</html>
