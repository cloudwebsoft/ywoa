<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%
	response.setContentType("text/javascript;charset=utf-8");
	// 防漏洞：1; mode=block 启用XSS保护，并在检查到XSS攻击时，停止渲染页面
	response.setHeader("X-XSS-Protection", "1; mode=block");
%>
var bustcachevar=1 //bust potential caching of external pages after initial request? (1=yes, 0=no)
var loadstatustext="<img src='<%=request.getContextPath()%>/inc/ajaxtabs/loading.gif' /> ..."

////NO NEED TO EDIT BELOW////////////////////////
var loadedobjects=""
var defaultcontentarray=new Object()
var bustcacheparameter=""

function ajaxpage(url, containerid){
	var page_request = false
	if (window.XMLHttpRequest) // if Mozilla, Safari etc
		page_request = new XMLHttpRequest()
	else if (window.ActiveXObject){ // if IE
		try {
			page_request = new ActiveXObject("Msxml2.XMLHTTP")
		}
		catch (e){
			try{
				page_request = new ActiveXObject("Microsoft.XMLHTTP")
			}
			catch (e){}
		}
	}
	else
		return false

	document.getElementById(containerid).innerHTML=loadstatustext
	page_request.onreadystatechange=function(){
		loadpage(page_request, containerid)
	}
	if (bustcachevar) //if bust caching of external page
		bustcacheparameter=(url.indexOf("?")!=-1)? "&"+new Date().getTime() : "?"+new Date().getTime()
	page_request.open('GET', url+bustcacheparameter, true)
	page_request.send(null)
}

function loadpage(page_request, containerid){
	if (page_request.readyState == 4 && (page_request.status==200 || window.location.href.indexOf("http")==-1))
	{
		var obj = document.getElementById(containerid);
		filterJS(obj, page_request.responseText, true);
	}
}

// AJAX加载的javascript无效，需作处理
function filterJS(obj, html, loadScripts) {
	obj.innerHTML = html;
	if(!loadScripts) return;   

	var _parseScripts = function() {
		var s = obj.getElementsByTagName("script");
		var docHead = document.getElementsByTagName("head")[0];

		//   For browsers which discard scripts when inserting innerHTML, extract the scripts using a RegExp   
		if(s.length == 0){   
			var re = /(?:<script.*(?:src=[\"\'](.*)[\"\']).*>.*<\/script>)|(?:<script.*>([\S\s]*?)<\/script>)/ig; // assumes HTML well formed and then loop through it.   
			var match;   
			while(match = re.exec(html)){
				 var s0 = document.createElement("script");
				 if (match[1])
					s0.src = match[1];   
				 else if (match[2])
					s0.text = match[2];   
				 else  
					continue;   
				 docHead.appendChild(s0);   
			}   
		} else {
		  for(var i = 0; i < s.length; i++){
			 var s0 = document.createElement("script");
			 s0.type = s[i].type;
			 if (s[i].text) {
				s0.text = s[i].text;
			 } else {   
				s0.src = s[i].src;   
			 }
			 docHead.appendChild(s0);
		  }   
		}   
	}   
	// set timeout to give obj opportunity to catch up   
	setTimeout(_parseScripts, 10);   
}