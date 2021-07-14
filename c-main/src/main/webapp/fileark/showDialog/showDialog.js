/*!
 * jQuery showDialog
 * befen.net
 * Date: 2013.02.08
*/
var ImgId = "";
var ImgWidth = "";
var ImgHeight = "";
var isSearch = "";
$(function(){

	$("#showImg_left").live("mouseover",function(){
		showNext();
	});
	$("#showImg_right").live("mouseover",function(){
		showNext();
	});
	$("#showImg_left").live("mousedown",function(){
		$(this).find("img").attr({"src":"images/clouddisk/arrow_left_hover.png"});
	});
	$("#showImg_right").live("mousedown",function(){
		$(this).find("img").attr({"src":"images/clouddisk/arrow_right_hover.png"});
	});
	$("#showImg_left").live("mouseup",function(){
		$(this).find("img").attr({"src":"images/clouddisk/arrow_left.png"});
	});
	$("#showImg_right").live("mouseup",function(){
		$(this).find("img").attr({"src":"images/clouddisk/arrow_right.png"});
	});
})

function showImgNext(url,arrow,isImgSearch){
	$.ajax({
		type:"post",
		url:url,
		data:{"op":"showNextImg", "att_id":ImgId,"arrow":arrow,"isImgSearch":isImgSearch},
		success:function(data,status){
			data = $.parseJSON(data);
			if(data.ret == "1" ){
				showDialog("info",data.newId,"图片预览",data.width,data.height,"showNextImg",data.downloadUrl);
			}
			
		},
		error:function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});
	
}
	
function detectMacXFF() {
	var userAgent = navigator.userAgent.toLowerCase();
	if(userAgent.indexOf("mac") != -1 && userAgent.indexOf("firefox") != -1) {
		return true;
	}
}

function in_array(needle, haystack) {
	if(typeof needle == "string" || typeof needle == "number") {
		for(var i in haystack) {
			if(haystack[i] == needle) {
				return true;
			}
		}
	}
	return false;
}

function sd_load(sd_width) {
	if(sd_width) {
		$("#SD_window").css("width", (sd_width+400) + "px");
	}
	var sd_top = ($(window).height() - $("#SD_window").height()) / 2 + $(document).scrollTop();
	if(sd_top < 0) {
		sd_top = 0;
	}
	var sd_left = ($(window).width() - $("#SD_window").width()) / 2;
	if(sd_left < 0) {
		sd_left = 0;
	}
	$("#SD_window").css("top", sd_top);
	$("#SD_window").css("left", sd_left);
}

function sd_remove() {
	$("#SD_close,#SD_cancel,#SD_confirm").unbind("click");
	$("#SD_window,#SD_overlay,#SD_HideSelect").remove();
	if(typeof document.body.style.maxHeight == "undefined") {
		$("body","html").css({height: "auto", width: "auto"});
	}
}

function sd_closeWindow(){
	$("#SD_confirm,#SD_close").live("click",function(){
		window.close();
	});
	$("#SD_cancel").bind("click", function(){
		sd_remove();
	});
}

function showDialogDirTree(mode, sd_width){
	var mode = in_array(mode, ['confirm', 'window', 'info', 'loading']) ? mode : 'alert';
		
		if(detectMacXFF()) {
				$("#SD_overlay").addClass("SD_overlayMacFFBGHack2");
		} else {
				$("#SD_overlay").addClass("SD_overlayBG2");
		}
		
//		$("#SD_confirm,#SD_cancel,#SD_close").bind("click", function(){
//			sd_remove();
//		});
		
		if(mode == "window") {
			$("#SD_close").show();
			$("#SD_cancel").show();
			$("#SD_button").show();
			
		}
		var sd_move = false;
		var sd_x, sd_y;
		$("#SD_container > h3").click(function(){}).mousedown(function(e){
			sd_move = true;
			sd_x = e.pageX - parseInt($("#SD_window").css("left"));
			sd_y = e.pageY - parseInt($("#SD_window").css("top"));
		});
		$(document).mousemove(function(e){
			if(sd_move){
				var x = e.pageX - sd_x;
				var y = e.pageY - sd_y;
				$("#SD_window").css({left:x, top:y});
			}
		}).mouseup(function(){
			sd_move = false;
		});
		$("#SD_body").width(sd_width - 50);
		sd_load(sd_width);
		$("#SD_window").show();
		$("#SD_window").focus();
	
}
function showNext(){
	$(".showImg_Next").css({"display":"block"});
}
function hiddenNext(){
	$(".showImg_Next").css({"display":"none"});
}
function showDialog(mode, msg, t, sd_width, sd_height,dirCode,type,url,isImgSearch) {
	if(isImgSearch != null){
		isSearch = isImgSearch;
	}
	var window_height = document.documentElement.clientHeight;
	var window_width = document.documentElement.clientWidth;
	if(type == "showNextImg"){
		img_w = sd_width;
		img_h = sd_height;
		img_w = (img_w > ImgWidth )? ImgWidth : img_w;
		img_h = (img_h > ImgHeight)? ImgHeight : img_h;
		var left = (window_width - ImgWidth)/2 - 100;
		var top = (window_height - ImgHeight)/2 - 70;
	}else{
		var img_w = sd_width; 
		var img_h = sd_height;
		sd_width = (sd_width < 550) ? 550 : ((sd_width > 750) ? 750 : sd_width);
		sd_height = (sd_height < 380) ? 380 : ((sd_height > 450) ? 450 : sd_height);
		ImgWidth = sd_width;  
		ImgHeight = sd_height;
		img_w = (img_w > sd_width )? sd_width : img_w;
		img_h = (img_h > sd_height)? sd_height : img_h;
		var left = (window_width - sd_width)/2 - 100;
		var top = (window_height - sd_height)/2 - 70;
	}
	
	var mode = in_array(mode, ['confirm', 'window', 'info', 'loading']) ? mode : 'alert';
	var t = t ? t : "提示信息";
	var msg = msg ? msg : "";
	ImgId = msg;           //为左右切换图片id赋值
	var confirmtxt = confirmtxt ? confirmtxt : "确定";
	var canceltxt = canceltxt ? canceltxt : "关闭";
	sd_remove();
	try {
		if(typeof document.body.style.maxHeight === "undefined") {
			$("body","html").css({height: "100%", width: "100%"});
			if(document.getElementById("SD_HideSelect") === null) {
				$("body").append("<iframe id='SD_HideSelect'></iframe><div id='SD_overlay'></div>");
			}
		} else {
			if(document.getElementById("SD_overlay") === null) {
				$("body").append("<div id='SD_overlay'></div>");
			}
		}
		if(mode == "alert") {
			if(detectMacXFF()) {
				$("#SD_overlay").addClass("SD_overlayMacFFBGHack");
			} else {
				$("#SD_overlay").addClass("SD_overlayBG");
			}
		} else {
			if(detectMacXFF()) {
				$("#SD_overlay").addClass("SD_overlayMacFFBGHack2");
			} else {
				$("#SD_overlay").addClass("SD_overlayBG2");
			}
		}
		$("body").append("<div id='SD_window' style='height:600px;'></div>");
		var SD_html;
		SD_html = "";
		SD_html += "<table cellspacing='0' cellpadding='0'  style='position:fixed;  z-index:1911; top:"+top+"px; left:"+left+"px;'><tbody ><tr><td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td></tr>";
		SD_html += "<tr><td class='SD_bg'></td>";
		SD_html += "<td id='SD_container'>";
		SD_html += "<h3 id='SD_title'>" + t + "</h3>";
		if(type == "showImg"){
			SD_html += "<div id='SD_body' onmouseover='showNext()'  onmouseout = 'hiddenNext()' style='height:"+ (sd_height+10)+"px; width:"+ (50+sd_width)+"px !important;overflow:auto; border:#999 4px solid;text-align:center;'>";
			SD_html += "<div class='showImg_Next' id='showImg_left' value='left' isImgSearch='"+isSearch+"' style='display:none;position:absolute; top:"+((sd_height+40)/2)+"px; left:10px;'><img src='images/clouddisk/arrow_left.png'></div>";
			SD_html += "<img style='padding-top:"+ ((sd_height-img_h)/2-15) +"px;height:"+img_h+"px; width:"+img_w+"px;' src='"+ url+ "'/>";
			SD_html += "<div class='showImg_Next' id='showImg_right' value='right' isImgSearch='"+isSearch+"' style='display:none;position:absolute; top:"+((sd_height+40)/2)+"px; right:10px'><img src='images/clouddisk/arrow_right.png'></div>";
			SD_html += "</div>";
		}else if(type == "showNextImg"){
			SD_html += "<div id='SD_body' onmouseover='showNext()'  onmouseout = 'hiddenNext()' style='height:"+ (ImgHeight +10)+"px; width:"+ (50+ImgWidth)+"px;overflow:auto; border:#999 4px solid;text-align:center;'>";
			SD_html += "<div class='showImg_Next' id='showImg_left' value='left' isImgSearch='"+isSearch+"' style='display:none;position:absolute; top:"+((ImgHeight+40)/2)+"px; left:10px;'><img src='images/clouddisk/arrow_left.png'></div>";
			SD_html += "<img style='padding-top:"+ ((ImgHeight-img_h)/2-15) +"px;height:"+img_h+"px; width:"+img_w+"px;' src='"+url+ "'/>";
			SD_html += "<div class='showImg_Next' id='showImg_right' value='right' isImgSearch='"+isSearch+"' style='display:none;position:absolute; top:"+((ImgHeight+40)/2)+"px; right:10px'><img src='images/clouddisk/arrow_right.png'></div>";
			SD_html += "</div>";
		}
		else{
		SD_html += "<div id='SD_body' style='height:"+ sd_height+"px; width:"+ sd_width +"px;overflow:auto; border:#999 4px solid;'><div id='SD_content' >" + msg + "</div></div>";
		}
		SD_html += "<div id='SD_button'><div class='SD_button'>";
		if(type == "movefile"){
			SD_html += "<a id='SD_confirm1' >"+ confirmtxt + "</a>";
			SD_html += "<a id='SD_cancel' onclick='closeDialog();'>"+ canceltxt + "</a>";
		}else{
			SD_html += "<a id='SD_confirm' >"+ confirmtxt + "</a>";
			SD_html += "<a id='SD_cancel' >"+ canceltxt + "</a>";
		}
		SD_html += "</div></div>";
		if(type == "movefile"){
			SD_html += "<a href='javascript:void(0);' id='SD_close' onclick='closeBackGround();' title='关闭'></a>";
		}else{
			SD_html += "<a href='javascript:void(0);' id='SD_close' title='关闭'></a>";
		}
		SD_html += "</td>";
		SD_html += "<td class='SD_bg'></td></tr>";
		SD_html += "<tr><td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td></tr></tbody></table>";
		$("#SD_window").append(SD_html);
		$("#SD_confirm,#SD_cancel,#SD_close").live("click", function(){
			//sd_remove();
			//sd_closeWindow();
			
			$("#SD_window,#SD_overlay,#SD_HideSelect").remove();
			
			window.location.href="../fileark/document_list_m.jsp?dir_code="+dirCode;
			
		});
		if(mode == "info" || mode == "alert") {
			$("#SD_cancel").show();
			$("#SD_confirm").hide();
		}
		if(mode == "window") {
			$("#SD_close").show();
			$("#SD_cancel").show();
			$("#SD_button").show();
		}
		if(mode == "confirm") {
			$("#SD_button").show();
		}
		var sd_move = false;
		var sd_x, sd_y;
		$("#SD_container > h3").click(function(){}).mousedown(function(e){
			sd_move = true;
			sd_x = e.pageX - parseInt($("#SD_window").css("left"));
			sd_y = e.pageY - parseInt($("#SD_window").css("top"));
		});
		$(document).mousemove(function(e){
			if(sd_move){
				var x = e.pageX - sd_x;
				var y = e.pageY - sd_y;
				$("#SD_window").css({left:x, top:y});
			}
		}).mouseup(function(){
			sd_move = false;
		});
		//$("#SD_body").width(sd_width - 50);
		sd_load(sd_width);
		$("#SD_window").show();
		$("#SD_window").focus();
	} catch(e) {
		alert("System Error !");
	}
}

function showTreeDialog(mode, msg, t, sd_width) {
	var sd_width = sd_width ? sd_width : 400;
	var mode = in_array(mode, ['confirm', 'window', 'info', 'loading']) ? mode : 'alert';
	var t = t ? t : "提示信息";
	var msg = msg ? msg : "";
	var confirmtxt = confirmtxt ? confirmtxt : "确定";
	var canceltxt = canceltxt ? canceltxt : "取消";
	sd_remove();
	try {
	
		if(mode == "alert") {
			if(detectMacXFF()) {
				$("#SD_overlay").addClass("SD_overlayMacFFBGHack");
			} else {
				$("#SD_overlay").addClass("SD_overlayBG");
			}
		} else {
			if(detectMacXFF()) {
				$("#SD_overlay").addClass("SD_overlayMacFFBGHack2");
			} else {
				$("#SD_overlay").addClass("SD_overlayBG2");
			}
		}
		$("body").append("<div id='SD_window' style='height:500px;'></div>");
		var SD_html;
		SD_html = "";
		SD_html += "<table cellspacing='0' cellpadding='0'  style='position:fixed; height:450px; z-index:1111; top:30px; left:350px;'><tbody ><tr><td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td></tr>";
		SD_html += "<tr><td class='SD_bg'></td>";
		SD_html += "<td id='SD_container'>";
		SD_html += "<h3 id='SD_title'>" + t + "</h3>";
		SD_html += "<div id='SD_body' style='height:390px;width:450px;overflow:auto; border:#999 4px solid;'><div id='SD_content' >" + msg + "</div></div>";
		SD_html += "<div id='SD_button'><div class='SD_button'>";
		SD_html += "<a id='SD_confirm' onclick='closeBackGround();'>"+ confirmtxt + "</a>";
		SD_html += "<a id='SD_cancel' onclick='closeDialog();'>"+ canceltxt + "</a>";
		SD_html += "</div></div>";
		SD_html += "<a href='javascript:closeDialog();' id='SD_close' title='关闭'></a>";
		SD_html += "</td>";
		SD_html += "<td class='SD_bg'></td></tr>";
		SD_html += "<tr><td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td></tr></tbody></table>";
		$("#SD_window").append(SD_html);
		$("#SD_cancel,#SD_close").bind("click", function(){
			sd_remove();
			//sd_closeWindow();
			//$("#SD_window,#SD_overlay,#SD_HideSelect").remove();
		});
		if(mode == "info" || mode == "alert") {
			$("#SD_cancel").hide();
			$("#SD_button").show();
		}
		if(mode == "window") {
			$("#SD_close").show();
			$("#SD_cancel").show();
			$("#SD_button").show();
		}
		if(mode == "confirm") {
			$("#SD_button").show();
		}
		var sd_move = false;
		var sd_x, sd_y;
		$("#SD_container > h3").click(function(){}).mousedown(function(e){
			sd_move = true;
			sd_x = e.pageX - parseInt($("#SD_window").css("left"));
			sd_y = e.pageY - parseInt($("#SD_window").css("top"));
		});
		$(document).mousemove(function(e){
			if(sd_move){
				var x = e.pageX - sd_x;
				var y = e.pageY - sd_y;
				$("#SD_window").css({left:x, top:y});
			}
		}).mouseup(function(){
			sd_move = false;
		});
		$("#SD_body").width(sd_width - 50);
		sd_load(sd_width);
		$("#SD_window").show();
		$("#SD_window").focus();
	} catch(e) {
		alert("System Error !");
	}
}

function showDialogTemplate(mode ,msg, t, sd_width) {
	var sd_width = sd_width ? sd_width : 400;
	var mode = in_array(mode, ['confirm', 'window', 'info', 'loading']) ? mode : 'alert';
	var t = t ? t : "提示信息";
	var msg = msg ? msg : "";
	var confirmtxt = confirmtxt ? confirmtxt : "关闭";
	var canceltxt = canceltxt ? canceltxt : "取消";
	sd_remove();
	try {
		if(typeof document.body.style.maxHeight === "undefined") {
			$("body","html").css({height: "100%", width: "100%"});
			if(document.getElementById("SD_HideSelect") === null) {
				$("body").append("<iframe id='SD_HideSelect'></iframe><div id='SD_overlay'></div>");
			}
		} else {
			if(document.getElementById("SD_overlay") === null) {
				$("body").append("<div id='SD_overlay'></div>");
			}
		}
		if(mode == "alert") {
			if(detectMacXFF()) {
				$("#SD_overlay").addClass("SD_overlayMacFFBGHack");
			} else {
				$("#SD_overlay").addClass("SD_overlayBG");
			}
		} else {
			if(detectMacXFF()) {
				$("#SD_overlay").addClass("SD_overlayMacFFBGHack2");
			} else {
				$("#SD_overlay").addClass("SD_overlayBG2");
			}
		}
		$("body").append("<div id='SD_window' style='height:700px;'></div>");
		var SD_html;
		SD_html = "";
		SD_html += "<table cellspacing='0' cellpadding='0'  style='position:fixed; z-index:1111; top:100px; left:200px;'><tbody ><tr><td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td></tr>";
		SD_html += "<tr><td class='SD_bg'></td>";
		SD_html += "<td id='SD_container'>";
		SD_html += "<h3 id='SD_title'>" + t + "</h3>";
		SD_html += "<div id='SD_body' style='height:350px;width:200px;overflow:auto; border:#999 4px solid;'><div id='SD_content' >" + msg + "</div></div>";
		SD_html += "<div id='SD_button'><div class='SD_button'>";
		SD_html += "<a id='template_confirm'>确定</a>";
		SD_html += "<a id='template_cancel'>"+ canceltxt + "</a>";
		SD_html += "</div></div>";
		SD_html += "<a href='javascript:;' id='SD_close' title='关闭'></a>";
		SD_html += "</td>";
		SD_html += "<td class='SD_bg'></td></tr>";
		SD_html += "<tr><td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td></tr></tbody></table>";
		$("#SD_window").append(SD_html);
		$("#template_cancel,#SD_close").bind("click", function(){
			sd_remove();
		});
		if(mode == "info" || mode == "alert") {
			$("#template_cancel").show();
			$("#SD_button").show();
		}
		
		var sd_move = false;
		var sd_x, sd_y;
		$("#SD_container > h3").click(function(){}).mousedown(function(e){
			sd_move = true;
			sd_x = e.pageX - parseInt($("#SD_window").css("left"));
			sd_y = e.pageY - parseInt($("#SD_window").css("top"));
		});
		$(document).mousemove(function(e){
			if(sd_move){
				var x = e.pageX - sd_x;
				var y = e.pageY - sd_y;
				$("#SD_window").css({left:x, top:y});
			}
		}).mouseup(function(){
			sd_move = false;
		});
		$("#SD_body").width(sd_width - 50);
		sd_load(sd_width);
		$("#SD_window").show();
		$("#SD_window").focus();
	} catch(e) {
		alert("System Error !");
	}
}

function showDialogUpload(mode ,msg, t, sd_width) {
	var sd_width = sd_width ? sd_width : 400;
	var mode = in_array(mode, ['confirm', 'window', 'info', 'loading']) ? mode : 'alert';
	var t = t ? t : "提示信息";
	var msg = msg ? msg : "";
	var confirmtxt = confirmtxt ? confirmtxt : "关闭";
	var canceltxt = canceltxt ? canceltxt : "取消";
	sd_remove();
	try {
		if(typeof document.body.style.maxHeight === "undefined") {
			$("body","html").css({height: "100%", width: "100%"});
			if(document.getElementById("SD_HideSelect") === null) {
				$("body").append("<iframe id='SD_HideSelect'></iframe><div id='SD_overlay'></div>");
			}
		} else {
			if(document.getElementById("SD_overlay") === null) {
				$("body").append("<div id='SD_overlay'></div>");
			}
		}
		if(mode == "alert") {
			if(detectMacXFF()) {
				$("#SD_overlay").addClass("SD_overlayMacFFBGHack");
			} else {
				$("#SD_overlay").addClass("SD_overlayBG");
			}
		} else {
			if(detectMacXFF()) {
				$("#SD_overlay").addClass("SD_overlayMacFFBGHack2");
			} else {
				$("#SD_overlay").addClass("SD_overlayBG2");
			}
		}
		$("body").append("<div id='SD_window' style='height:100px;'></div>");
		var SD_html;
		SD_html = "";
		SD_html += "<table cellspacing='0' cellpadding='0'  style='position:fixed; z-index:1111; top:200px; left:370px;'><tbody ><tr><td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td></tr>";
		SD_html += "<tr><td class='SD_bg'></td>";
		SD_html += "<td id='SD_container'>";
		SD_html += "<h3 id='SD_title'>" + t + "</h3>";
		SD_html += "<div id='SD_body' style='height:75px;width:250px;overflow:auto; border:#999 4px solid;'><div id='SD_content' >" + msg + "</div></div>";
		SD_html += "<div id='SD_button'><div class='SD_button'>";
		SD_html += "<a id='template_cancel'>"+ canceltxt + "</a>";
		SD_html += "</div></div>";
		SD_html += "<a href='javascript:;' id='SD_close' title='关闭'></a>";
		SD_html += "</td>";
		SD_html += "<td class='SD_bg'></td></tr>";
		SD_html += "<tr><td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td></tr></tbody></table>";
		$("#SD_window").append(SD_html);
		$("#template_cancel,#SD_close").bind("click", function(){
			sd_remove();
		});
		if(mode == "info" || mode == "alert") {
			$("#template_cancel").show();
			$("#SD_button").show();
		}
		
		var sd_move = false;
		var sd_x, sd_y;
		$("#SD_container > h3").click(function(){}).mousedown(function(e){
			sd_move = true;
			sd_x = e.pageX - parseInt($("#SD_window").css("left"));
			sd_y = e.pageY - parseInt($("#SD_window").css("top"));
		});
		$(document).mousemove(function(e){
			if(sd_move){
				var x = e.pageX - sd_x;
				var y = e.pageY - sd_y;
				$("#SD_window").css({left:x, top:y});
			}
		}).mouseup(function(){
			sd_move = false;
		});
		$("#SD_body").width(sd_width - 50);
		sd_load(sd_width);
		$("#SD_window").show();
		$("#SD_window").focus();
	} catch(e) {
		alert("System Error !");
	}
}

//历史版本弹窗
function showDialogHistory(mode ,msg, t, sd_width) {
	var sd_width = sd_width ? sd_width : 400;
	var mode = in_array(mode, ['confirm', 'window', 'info', 'loading']) ? mode : 'alert';
	var t = t ? t : "提示信息";
	var msg = msg ? msg : "";
	var confirmtxt = confirmtxt ? confirmtxt : "关闭";
	var canceltxt = canceltxt ? canceltxt : "取消";
	//sd_remove();
	try {
		if(typeof document.body.style.maxHeight === "undefined") {
			$("body","html").css({height: "100%", width: "100%"});
			if(document.getElementById("SD_HideSelect") === null) {
				$("body").append("<iframe id='SD_HideSelect'></iframe><div id='SD_overlay'></div>");
			}
		} else {
			if(document.getElementById("SD_overlay") === null) {
				$("body").append("<div id='SD_overlay'></div>");
			}
		}
		if(mode == "alert") {
			if(detectMacXFF()) {
				$("#SD_overlay").addClass("SD_overlayMacFFBGHack");
			} else {
				$("#SD_overlay").addClass("SD_overlayBG");
			}
		} else {
			if(detectMacXFF()) {
				$("#SD_overlay").addClass("SD_overlayMacFFBGHack2");
			} else {
				$("#SD_overlay").addClass("SD_overlayBG2");
			}
		}
		$("body").append("<div id='SD_window' style='height:100px;'></div>");
		var SD_html;
		SD_html = "";
		SD_html += "<table cellspacing='0' cellpadding='0'  style='position:fixed; z-index:1111; top:100px; left:220px;'><tbody ><tr><td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td></tr>";
		SD_html += "<tr><td class='SD_bg'></td>";
		SD_html += "<td id='SD_container'>";
		SD_html += "<h3 id='SD_title'>" + t + "</h3>";
		SD_html += "<div id='SD_body' style='height:200px;width:700px;overflow:auto; border:#999 4px solid;'><div id='SD_content' >" + msg + "</div></div>";
		SD_html += "<div id='SD_button'><div class='SD_button'>";
		SD_html += "<a id='template_cancel'>"+ canceltxt + "</a>";
		SD_html += "</div></div>";
		SD_html += "<a href='javascript:;' id='SD_close' title='关闭'></a>";
		SD_html += "</td>";
		SD_html += "<td class='SD_bg'></td></tr>";
		SD_html += "<tr><td class='SD_bg'></td><td class='SD_bg'></td><td class='SD_bg'></td></tr></tbody></table>";
		$("#SD_window").append(SD_html);
		$("#template_cancel,#SD_close").bind("click", function(){
			//sd_remove();
			$("#SD_window,#SD_overlay,#SD_HideSelect").remove();
		});
		if(mode == "info" || mode == "alert") {
			$("#template_cancel").show();
			$("#SD_button").show();
		}
		
		var sd_move = false;
		var sd_x, sd_y;
		$("#SD_container > h3").click(function(){}).mousedown(function(e){
			sd_move = true;
			sd_x = e.pageX - parseInt($("#SD_window").css("left"));
			sd_y = e.pageY - parseInt($("#SD_window").css("top"));
		});
		$(document).mousemove(function(e){
			if(sd_move){
				var x = e.pageX - sd_x;
				var y = e.pageY - sd_y;
				$("#SD_window").css({left:x, top:y});
			}
		}).mouseup(function(){
			sd_move = false;
		});
		$("#SD_body").width(sd_width - 50);
		sd_load(sd_width);
		$("#SD_window").show();
		$("#SD_window").focus();
	} catch(e) {
		alert("System Error !");
	}
}

function showInfo(msg, fn, timeout) {
	showDialog("info", msg);
	$("#SD_confirm").unbind("click");
	if(fn && timeout) {
		st = setTimeout(function(){
			sd_remove();
			fn();
		}, timeout * 1000);
	}
	$("#SD_confirm").bind("click", function(){
		if(timeout) {
			clearTimeout(st);
		}
		sd_remove();
		if(fn) {
			fn();
		}
	});
}

function showWindow(title, the_url, sd_width) {
	var sd_width = sd_width ? sd_width : 400;
	$.ajax({
		type		: "GET",
		dataType	: "html",
		cache		: false,
		timeout		: 10000,
		url			: the_url,
		data		: "isajax=1",
		success		: function(data){
			showDialog("window", data, title, sd_width);
		},
		error		: function(data){
			showDialog("alert", "读取数据失败");
		},
		beforeSend	: function(data){
			showDialog("loading", "正在读取数据...");
		}
	});
}

function showConfirm(msg, fn) {
	showDialog("confirm", msg);
	$("#SD_confirm").unbind("click");
	$("#SD_confirm").bind("click", function(){
		if(fn) {
			fn();
		}
	});
}