<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.netdisk.*"%>
<%@page import="cn.js.fan.web.SkinUtil"%>
<jsp:useBean id="privilege" scope="page"
			class="com.redmoon.oa.pvg.Privilege" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
	  <link type="text/css" rel="stylesheet"
			href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	  <link type="text/css" rel="stylesheet" href="css/reset.css"/>
	  <script src="../inc/common.js"></script>
	  <script src="../js/jquery-1.9.1.min.js"></script>
	  <script src="../js/jquery-migrate-1.2.1.min.js"></script>
	  <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	  <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	  <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	  <script type="text/javascript" src="../js/jquery.toaster.netdisk.js"></script>
	  <style type="text/css">
		  span {
			  font-size: 14px
		  }

		  .picture {
			  float: left;
			  margin-right: 11px;
			  margin-top: 13px;
			  padding-left: 20px;
		  }

		  .fireFoxOpacity {
			  -moz-opacity: 0.4;
			  opacity: 0.4
		  }

		  .note {
			  width: 695px;
			  height: 25px;
			  background: #fffae0;
			  border: 2px solid #f2e9b7;
			  margin-top: 24px;
			  margin-left: 25px;
			  margin-bottom: 15px;
			  font-famliy: "微软雅黑";
			  color: red;
			  text-align: center;
			  font-size: 13px;
			  padding: 25px 10px;
		  }

		  .sidebarDiv {
			  height: 215px;
			  width: 215px;
			  background: #fff;
			  margin-left: 25px;
			  float: left;
			  border: 1px solid #e6e6e6;
			  background: #fff
		  }

		  .sidebarEdit {
			  display: block;
			  float: left;
			  background: #336699;
			  width: 500px;
			  height: 215px;
			  border: 1px solid #e6e6e6;
			  background: #fff
		  }

		  .isShow {
			  height: 20px;
			  padding-left: 15px;
			  height: 44px;
			  padding-top: 27px;
			  border-bottom: 1px solid #e6e6e6
		  }

		  .repTitle {
			  font-famliy: "微软雅黑";
			  font-size: 15px;
			  color: #333;
			  float: left;
			  padding-left: 10px;
			  width: 120px;
		  }

		  .title1 {
			  margin-left: 30px;
			  float: left;
			  width: 100px;
			  font-famliy: "微软雅黑";
		  }

		  .title2 {
			  margin-left: 30px;
			  float: left;
			  width: 200px;
			  font-famliy: "微软雅黑";
		  }

		  .appDivSub {
			  width: 70px;
			  height: 30px;
			  padding-top: 5px;
			  background: #4393f1;
			  color: #fff;
			  float: left;
			  margin-left: 25px;
			  font-famliy: "微软雅黑";
			  font-size: 15px;
			  padding-left: 30px;
		  }

		  .editDiv {
			  display: none;
			  float: left;
			  background: #eff4f5;
			  width: 500px;
			  height: 215px;
			  border: 1px solid #b4dbf5;
		  }

		  .editTitle {
			  font-famliy: "微软雅黑";
			  font-size: 18px;
			  color: #4392f2;
			  height: 35px;
			  padding-top: 5px;
			  border-bottom: 1px solid #b4dbf5;
			  text-align: center;
		  }

		  .sideTitle {
			  font-famliy: "微软雅黑";
			  font-size: 15px;
			  color: #333;
			  float: left;
			  padding-left: 25px;
			  width: 80px;
		  }

		  .editItem {
			  height: 30px;
			  padding-top: 5px;
			  float: left
		  }

		  .buttonEdit {
			  float: left;
			  background: #b4dbf5;
			  border: 1px solid #88caf7;
			  width: 40px;
			  height: 18px;
			  padding-left: 10px;
			  margin-left: 10px;
			  line-height: 1
		  }

		  .selectEdit {
			  float: left;
			  margin-left: 10px;
		  }

		  .editDivSub {
			  width: 45px;
			  height: 23px;
			  background: #5cace6;
			  border: 1px solid #4799d6;
			  color: #fff;
			  float: left;
			  margin-left: 25px;
			  font-famliy: "微软雅黑";
			  font-size: 13px;
			  padding-left: 20px;
			  margin-left: 135px;
		  }

		  .editDivCel {
			  width: 45px;
			  height: 23px;
			  background: #5cace6;
			  border: 1px solid #4799d6;
			  color: #fff;
			  float: left;
			  margin-left: 25px;
			  font-famliy: "微软雅黑";
			  font-size: 13px;
			  padding-left: 20px;
		  }

		  .cbox {
			  background: url('images/clouddisk/checkbox_1.png');
			  float: left;
			  width: 20px;
			  height: 20px;
			  margin-top: -2px;
			  margin-left: -3px;
		  }

		  .notice_topic, .showNotice, .upDiv {
			  display: none;
		  }

		  .is_show {
			  background: url('images/clouddisk/radio_1.png');
			  float: left;
			  width: 12px;
			  height: 12px;
			  margin-top: 4px;
			  margin-left: 2px;
		  }

		  .turn {
			  background: url('images/clouddisk/radio_1.png');
			  float: left;
			  width: 12px;
			  height: 12px;
			  margin-top: 4px;
			  margin-left: 2px;
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
     
     %>
    <script type="text/javascript">
    var imgAttId;
    var imgAttIdOld;
    var isOpen=0;
    var appImg="";
    var imgHref="";
    var imgTitle="";
    var appImgOld = "";
   	var imgHrefOld = "";
   	var imgTitleOld = "";
    var selectMsg='';
    var showNotice='';
    var upDiv='';
    var notice_topic = '';
    var isCustom = 0;
    var user_name="<%=userName%>";
    $(function(){
    	$.ajax({
    		type:"get",
    		url:"clouddisk_list_do.jsp",
    		//async = false;   //同步
    		data:{"op":"getSideBar","user_name":user_name},
    		success:function(data,status){
    			data = $.parseJSON(data);
    			if(data.ret == "1"){
	    			$.each(data.sideBarArray,function(index,data){
	    				if(data.position == "<%=SideBarDb.UP_NOTICE%>"){
	    					if(data.is_show == "<%=SideBarDb.IS_SHOW%>"){
	    						$("#showNotice").attr({"checked":"checked"});
	    						$("#cradio1").css("background","url('images/clouddisk/radio_2.png')");
		    					$("#hiddenNotice").removeAttr("checked");
		    					$("#cradio2").css("background","url('images/clouddisk/radio_1.png')");
		    					$("#upNotice").attr({"checked":"checked"});
		    					$("#cradio3").css("background","url('images/clouddisk/radio_2.png')");
		    					$("#upButton").removeAttr("checked");
		    					$("#cradio4").css("background","url('images/clouddisk/radio_1.png')");
	    					}
	    				}
	    				else if(data.position == "<%=SideBarDb.UP_BUTTON%>"){
	    					if(data.is_show == "<%=SideBarDb.IS_SHOW%>"){
	    						$("#showNotice").attr({"checked":"checked"});
	    						$("#cradio1").css("background","url('images/clouddisk/radio_2.png')");
		    					$("#hiddenNotice").removeAttr("checked");
		    					$("#cradio2").css("background","url('images/clouddisk/radio_1.png')");
		    					$("#upNotice").removeAttr("checked");
		    					$("#cradio3").css("background","url('images/clouddisk/radio_1.png')");
		    					$("#upButton").attr({"checked":"checked"});
		    					$("#cradio4").css("background","url('images/clouddisk/radio_2.png')");
		    				}
	    				}
	    				else if(data.position == "<%=SideBarDb.NOTICE_TOPIC%>"){
	    					if((data.title).indexOf("msgNotice") >=0){
	    						$("#cbox2").css("background","url('images/clouddisk/checkbox_3.png')");
	    						$("#msgNotice").attr({"checked":"checked"});
	    					}
	    					if((data.title).indexOf("flowNotice") >=0){
	    						$("#cbox1").css("background","url('images/clouddisk/checkbox_3.png')");
	    						$("#flowNotice").attr({"checked":"checked"});
	    					}
	    				}
	    				else{
		    				if((data.is_show)=="<%=SideBarDb.IS_SHOW%>"){
		    					$("#pic_"+data.position).attr({"class":"picture"});
		    				}else{
		    					$("#pic_"+data.position).attr({"class":"picture fireFoxOpacity"});
		    				}
		    				$("#pic_"+data.position).find("img").attr({"title":data.title,"src":"img_show.jsp?path=images/appImages/"+data.picture,"is_open":data.is_show,"value":data.href,"width":"36px","height":"36px"});
		    			}
					});
					
			    	selectMsg = $("#select_items").html();//把默认select列表保存下来
			    	$("input[name='message']:checked").each(function(){//给默认的选项赋值
			  			  showNotice =$(this).val();
					})
			    	
			    	$("input[name='whichUp']:checked").each(function(){//给默认的选项赋值
			  			  upDiv =$(this).val();
					})
					  $("input[name='notice_topic']").each(function(i){
						  if($(this).is(":checked")){
							  if(notice_topic == ''){
								  notice_topic += $(this).val()+",";
							   }else {
								   notice_topic += $(this).val();
							   }
						   }
					  });
				}
    		},
    		error:function(XMLHttpRequest, textStatus){
				alert(XMLHttpRequest.responseText);
			}
    	})
    	
		
    	
    	//给默认的选项赋值(根据数据库的值，选中notice_topic对应项)
    	//if(($("#topic1").attr("checked"))=="checked"){
    	//	notice_topic += $("#topic1").val();
    	//	alert(notice_topic);
    	//}
    	//--------------setupDiv方法
		function clickImg(){
	    		appImg = $("#"+imgAttId).find("img").attr("src");
	    		appImg = appImg.substr(appImg.lastIndexOf("appImages/")+10);   //根据文档名称更改后面的数字
	    		appImg = appImg.replace("_move","_default");  //减去点击的时候会出现“_move”的情况 
	    		imgHref = $("#"+imgAttId).find("img").attr("value");
	    		imgTitle = $("#"+imgAttId).find("img").attr("title");
	    		$("#app_img").attr({"value":appImg});//记录当前图片到编辑栏
	    		$(".a_href").attr({"value":imgHref}); //记录当前链接
	    		$("#title").attr({"value":imgTitle}); //记录当前图片名称
	    		var s1 = document.getElementById("select_items");//恢复默认select状态
	    		$("#select_items option[value='"+imgHref+"']").attr("selected",true); 
	    		if($("#"+imgAttId).hasClass("fireFoxOpacity")){
    				$(".is_open").parent().html("<option class='is_open' value='0' id='open_sel'>否</option><option class='is_open' value='1'>是</option>");
    				//alert($("#open_sel").html());
	    		}else{
	    			$(".is_open").parent().html("<option class='is_open' value='1' id='open_sel'>是</option><option class='is_open' value='0'>否</option>");
					//alert($("#open_sel").html());
	    		}
	    		isOpen = $("#open_sel").attr("value"); //记录是否启用
	    		imgAttIdOld = imgAttId;
	    		appImgOld = appImg;  //点击图片后 储存old值 以便区分是否修改，以便提示；
		  		imgHrefOld = imgHref;
		  		imgTitleOld = imgTitle;
		}
		
    	$(".picture").click(function(){ //点击图片事件
    		imgAttId = $(this).attr("id");
    		if($(".editDiv").css("display")== "block"){
	    		//alert(appImgOld);
	    		//alert(imgHrefOld);
	    		//alert(imgTitleOld);
	    		//var a = $("#app_img").val(); alert(a);
	    		//var b = $(".a_href").val(); alert(b);
	    		//var c = $("#title").val(); alert(c);
    			if(($(".editDiv").css("display"))== "block" && (appImgOld!=$("#app_img").val() || imgHrefOld !=$(".a_href").val() || imgTitleOld != $("#title").val())){
    				jConfirm("你还没有保存，确定离开吗？", "提示", function(r){
	    				if(!r){
	    					//$(".editDiv").css({"display":"none"});
		    				//$(".sidebarEdit").css({"display":"block"});
		    				imgAttId = imgAttIdOld;
		    				//imgHref = imgHrefOld;alert(imgHref);
		    				//imgTitle = imgTitleOld;
		    				//appImg = appImgOld;
							return;
						}else{
							clickImg();
						}
    				});
    			}else{
    				imgAttId = $(this).attr("id");
	    			appImg = $("#"+imgAttId).find("img").attr("src");
		    		appImg = appImg.substr(appImg.lastIndexOf("appImages/")+10);   //根据文档名称更改后面的数字
		    		appImg = appImg.replace("_move","_default");  //减去点击的时候会出现“_move”的情况 
		    		imgHref = $(this).find("img").attr("value");
		    		imgTitle = $(this).find("img").attr("title");
	    			imgAttIdOld = imgAttId;
	    			appImgOld = appImg;  //点击图片后 储存old值 以便区分是否修改，以便提示；
		  		imgHrefOld = imgHref;
		  		imgTitleOld = imgTitle;
		    		$(".editDiv").css({"display":"block"});
		    		$(".sidebarEdit").css({"display":"none"});
		    		$("#app_img").attr({"value":appImg});//记录当前图片到编辑栏
		    		$(".a_href").attr({"value":imgHref}); //记录当前链接
		    		$("#title").attr({"value":imgTitle}); //记录当前图片名称
		    		$("#select_items option[value='"+imgHref+"']").attr("selected",true);
		    		if($(this).hasClass("fireFoxOpacity")){
	    				$(".is_open").parent().html("<option class='is_open' value='0' id='open_sel'>否</option><option class='is_open' value='1'>是</option>");
	    				//alert($("#open_sel").html());
		    		}else{
		    			$(".is_open").parent().html("<option class='is_open' value='1' id='open_sel'>是</option><option class='is_open' value='0'>否</option>");
						//alert($("#open_sel").html());
		    		}
		    		isOpen = $("#open_sel").attr("value"); //记录是否启用
    			}
    			
    		}else{
    			imgAttId = $(this).attr("id");
    			appImg = $("#"+imgAttId).find("img").attr("src");
	    		appImg = appImg.substr(appImg.lastIndexOf("appImages/")+10);   //根据文档名称更改后面的数字
	    		appImg = appImg.replace("_move","_default");  //减去点击的时候会出现“_move”的情况 
	    		imgHref = $(this).find("img").attr("value");
	    		imgTitle = $(this).find("img").attr("title");
    			imgAttIdOld = imgAttId;
	    		$(".editDiv").css({"display":"block"});
	    		$(".sidebarEdit").css({"display":"none"});
	    		$("#app_img").attr({"value":appImg});//记录当前图片到编辑栏
	    		$(".a_href").attr({"value":imgHref}); //记录当前链接
	    		$("#title").attr({"value":imgTitle}); //记录当前图片名称
	    		$("#select_items option[value='"+imgHref+"']").attr("selected",true);
	    		if($(this).hasClass("fireFoxOpacity")){
    				$(".is_open").parent().html("<option class='is_open' value='0' id='open_sel'>否</option><option class='is_open' value='1'>是</option>");
    				//alert($("#open_sel").html());
	    		}else{
	    			$(".is_open").parent().html("<option class='is_open' value='1' id='open_sel'>是</option><option class='is_open' value='0'>否</option>");
					//alert($("#open_sel").html());
	    		}
	    		isOpen = $("#open_sel").attr("value"); //记录是否启用
    		}
	    		appImgOld = appImg;  //点击图片后 储存old值 以便区分是否修改，以便提示；
		  		imgHrefOld = imgHref;
		  		imgTitleOld = imgTitle;
    			$(".a_href").attr({"disabled":"true"});
		    	//把确定取消按钮灰化
		    	$(".buttonDiv").html("");
	    })
   	
    	$("#select_items").live("change",function(){  //自定义连接显示
    		imgHref = $(this).val();
    		isCustom = 0;
    		if(imgHref == "-1"){
    			return;
    		}else if(imgHref == "check"){
    			imgHref = "";
    			$(".a_href").attr({"value":imgHref});
    			$(".a_href").attr({"disabled":"true"});
    		}else if(imgHref == "custom"){
    			imgHref = "";
    			isCustom = 1;
    			$(".a_href").attr({"value":imgHref});
    			$(".a_href").removeAttr("disabled");
    		}else{
    			$(".a_href").attr({"value":imgHref});
    			//$(".a_href").removeAttr("disabled");
    			$(".a_href").attr({"disabled":"true"});
    		}
    	})
    	
    	$(".editDivSub").live("click",function(){  //setupDIV修改提交（小确定）
    		$(".sidebarEdit").css({"display":"block"});
    		if(isOpen == 1){
    			$("#"+imgAttId).attr({"class":"picture"});
    			$("#"+imgAttId).find("img").attr({"is_open":"1"});
    		}else{
    			$("#"+imgAttId).attr({"class":"picture fireFoxOpacity"});//半透明表示不启用
    			//alert($("#"+imgAttId).find("img").attr("is_open"));
    			$("#"+imgAttId).find("img").attr("is_open","0"); // 1表示启用，0表示禁用
    		}
    		
    		var iCon = $("#app_img").attr("value");
    		$("#"+imgAttId).find("img").attr({"src":"img_show.jsp?path=images/appImages/"+iCon});//图片变更 
    		imgHref = $(".a_href").val();
    		$("#"+imgAttId).find("img").attr({"value":imgHref});//链接更换
    		var title = $("#title").val();
    		$("#"+imgAttId).find("img").attr({"title":title});  //名称变更
    		
    		$(".editDiv").css({"display":"none"});//关闭editDiv
    		//把"确定" "取消"显示出来 
    		$(".buttonDiv").html("<div class='appDivSub' id='subDiv' onmouseover='this.style.cursor=\"pointer\"'>确&nbsp;&nbsp;定</div><div class='appDivSub' id='celDiv' onmouseover='this.style.cursor=\"pointer\"'>取&nbsp;&nbsp;消</div>	");
    		$.ajax({
				type:"get",
				url:"clouddisk_list_do.jsp",
				data:{"op":"sidebar_setup_item","user_name":user_name,"is_show":isOpen,"position":imgAttId,"title":title,"picture":iCon,"href":imgHref,"custom":isCustom},
				success:function(data,status){
					data = $.parseJSON(data);
					if(data.ret == "1"){
						$.toaster({ priority : 'info', message : data.msg });
					}
					else{
						$.toaster({ priority : 'info', message : data.msg });
					}
				},
				error:function(XMLHttpRequest, textStatus){
					alert(XMLHttpRequest.responseText);
				}
			});
    	})
    	$(".editDivCel").live("click",function(){	////setupDIV修改取消
    		$(".editDiv").css({"display":"none"});
    		$(".sidebarEdit").css({"display":"block"});
    		$(".buttonDiv").html("<div class='appDivSub' id='subDiv' onmouseover='this.style.cursor=\"pointer\"'>确&nbsp;&nbsp;定</div><div class='appDivSub' id='celDiv' onmouseover='this.style.cursor=\"pointer\"'>取&nbsp;&nbsp;消</div>	");
    	})
    	$("#sel_open").live("change",function(){  //是否启用
			isOpen = $(this).val();// 得到当前选中的值
		});
    	//------------侧边栏功能
   		var isShowCradio = function(){
			var $is_show = $(this).parent().find(".showNotice");
			//alert($is_show.attr("id"));
			showNotice = $is_show.val();
			$(this).css("background","url('images/clouddisk/radio_2.png')");
			if($is_show.attr("id")=="showNotice"){
				$("#cradio2").css("background","url('images/clouddisk/radio_1.png')");
			}else{
				$("#cradio1").css("background","url('images/clouddisk/radio_1.png')");
			}
			$("#"+$is_show.attr("id")).attr({"checked":"checked"});
		}
    	$('.is_show').click(isShowCradio);//选raido的js效果 
    	
    	$(".turn").click(function(){   //选raido的js效果 
    		var $upDiv = $(this).parent().find(".upDiv");
    		upDiv = $upDiv.val();
    		//alert(upDiv);
    		$(this).css("background","url('images/clouddisk/radio_2.png')");
			if($upDiv.attr("id")=="upNotice"){
				$("#cradio4").css("background","url('images/clouddisk/radio_1.png')");
			}else{
				$("#cradio3").css("background","url('images/clouddisk/radio_1.png')");
			}
			$("#"+$upDiv.attr("id")).attr({"checked":"checked"});
    	})
    	$("#subDiv").live("click",function(){   //Div确定按钮 （大确定）
    		$.ajax({
				type:"post",
				url:"clouddisk_list_do.jsp",
				data:{"op":"sidebar_setup", "showNotice":showNotice, "upDiv":upDiv, "user_name":user_name, "notice_topic":notice_topic},
				success:function(data,status){
					data = $.parseJSON(data);
					if(data.ret == "1"){
						jAlert("设置成功。","提示");
					}
					else{
					}
				},
				error:function(XMLHttpRequest, textStatus){
					alert(XMLHttpRequest.responseText);
				}
			});
    	})
    	
    	$("#celDiv").live("click",function(){  //Div取消按钮 
    		window.history.go(-1);
    	})
    	
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
   		
		var checkAllCbox = function(){
			var $notice_topic = $(this).parent().find(".notice_topic");
			if($notice_topic.is(":checked")){
				$(this).css("background","url('images/clouddisk/checkbox_1.png')");
				$notice_topic.removeAttr("checked");
			}else{
				$(this).css("background","url('images/clouddisk/checkbox_3.png')");
				$notice_topic.attr({"checked":"checked"});
			}
			var notice_topic1="";
    		var notice_topic2="";
    		if(($("#flowNotice").attr("checked")) == "checked"){
    			notice_topic1 = $("#flowNotice").val();
    		}
    		if(($("#msgNotice").attr("checked")) == "checked"){
    			notice_topic2 = $("#msgNotice").val();
    		}
    		notice_topic = notice_topic1 + ","+ notice_topic2;
   		}
   		
   		$('.cbox').click(checkAllCbox); //选通知栏内容
    })
    
    function recover(){
   			jConfirm("你要初始化所有的图片属性吗？", "提示", function(r){
				if(!r){
				}else{
					$.ajax({
						type:"post",
						url:"clouddisk_list_do.jsp",
						data:{"op":"sidebar_recover", "user_name":user_name},
						success:function(data,status){
							data = $.parseJSON(data);
							$.toaster({ priority : 'info', message : data.msg });
						},
						error:function(XMLHttpRequest, textStatus){
							alert(XMLHttpRequest.responseText);
						}
					});	
				}
			})
   		}
    function selIcon(icon) {
		o("app_img").value = icon;
	}
    </script>
  </head>
  
  <body>
  	<div class="note">
  		<b>此处设置客户端侧边栏属性</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:recover();">恢复默认</a>
  	</div>
  	<div style="height:220px;">
	    <div class="sidebarDiv">
	    		<%for( int i = 0; i<SideBarDb.PICTURE_TWELFTH; i++){ %>
	    			<div class="picture" id="pic_<%=i+1 %>"><img src="" title="" onmouseover="this.style.cursor='pointer'" is_open="1" value=""/></div>
	    		<%} %>
	    </div>
	    <div class="sidebarEdit" >
	    	<div class="isShow">
	    		<div class="repTitle"><b>通知栏是否显示：</b></div>
				    <div class="title1"><div class="is_show" id="cradio1" ></div><input class="showNotice" type="radio" id="showNotice" name="message" value="1">&nbsp;&nbsp;&nbsp;<span>显示</span></div>
				    <div class="title1"><div class="is_show" id="cradio2" style="background:url('images/clouddisk/radio_2.png')"></div><input class="showNotice" type="radio" id="hiddenNotice" name="message" value="0" checked>&nbsp;&nbsp;&nbsp;<span>隐藏</span></div>
				    <!-- <div style="float:right">
				    <img src="images/clouddisk/setup.png"/>
				    </div>
				     -->
	    	</div>
	    	<div class="isShow">
	    		<div class="repTitle"><b>排列顺序：</b></div>
	    		 <div class="title1"><div class="turn" id="cradio3" style="background:url('images/clouddisk/radio_2.png')"></div><input class="upDiv" type="radio" id="upNotice" name="whichUp" value="0" checked>&nbsp;&nbsp;&nbsp;<span>通知栏在上</span></div>
	    		 <div class="title1"><div class="turn" id="cradio4"></div><input class="upDiv" type="radio" id="upButton" name="whichUp" value="999">&nbsp;&nbsp;&nbsp;<span>通知栏在下</span></div>
	    	</div>
	    	<div class="isShow">
	    		<div class="repTitle"><b>通知栏内容：</b></div>
	    		<div class="title1"><div class="cbox" id="cbox1"></div><input class="notice_topic" type="checkbox" name="notice_topic" id="flowNotice" value="flowNotice" >&nbsp;&nbsp;&nbsp;待办流程 </div>
	    		<div class="title1"><div class="cbox" id="cbox2"></div><input class="notice_topic" type="checkbox" name="notice_topic" id="msgNotice" value="msgNotice" >&nbsp;&nbsp;&nbsp;消息通知</div>
	    	</div>
	    </div>
	    
	    <div class="editDiv" >
	    	<div class="editTitle">修改</div>
	    	<div class="editItem">
		    	<div class="sideTitle">是否启用： </div>
		    	<div class="title1">
		    		<select id="sel_open" style="border:1px solid #b4dbf5;width:70px">
		    			<option class="is_open" value="1" id='open_sel'>是</option>
		    			<option class="is_open" value="0">否</option>
		    		</select>
		    	</div>
		    </div>
		    <div class="editItem">
		    	<div class="sideTitle">名称：</div>
		    	<div class="title2">	
		    		<input id="title" value="" style="border:1px solid #b4dbf5;width:200px"/>
		    	</div>
		    </div>
		    <div class="editItem">
		    	<div class="sideTitle">图标：</div>
		    	<div class="title2">
		    		<input disabled="true" id="app_img" value="不可写" style="border:1px solid #b4dbf5;width:200px"/>&nbsp;&nbsp;&nbsp;&nbsp;
		    	</div>
		    	<div class="buttonEdit" onclick="openWin('sidebar_icon.jsp', 800, 600)" onmouseover="this.style.cursor='pointer'" />选择</div>
		    </div>
		   
	    	 <div class="editItem">
		    	<div class="sideTitle">链接：</div>
		    	<div class="title2">
		    		<input class="a_href" value="" disabled style="border:1px solid #b4dbf5;width:200px"/>
				</div>
		    	<div class="selectEdit">
		    		<select id="select_items" style="border:1px solid #b4dbf5">
		    			<option value="check" class="a_option_all" id="choose" custom="0">请选择</option>
						<option value="custom" class="a_option_custom" id="custom" custom="1" style="color:#2d69b0"><strong>自定义</strong></option>
						<%out.print(UtilTools.ShowMenuAsOption(request));%>
		    		</select>
		    	</div>
		    </div>
		    <div class="editItem">
		    		<div class="editDivSub" onmouseover="this.style.cursor='pointer'">	确定</div>
		    		<div class="editDivCel" onmouseover="this.style.cursor='pointer'">	取消</div>
    		</div>
	    </div>
    </div>
    <br/>
    <div class="buttonDiv">
    		<div class="appDivSub" id="subDiv" onmouseover="this.style.cursor='pointer'">确&nbsp;&nbsp;定</div>	
    		<div class="appDivSub" id="celDiv" onmouseover="this.style.cursor='pointer'">取&nbsp;&nbsp;消</div>	
    </div>
  </body>
</html>
