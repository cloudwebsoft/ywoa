<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import = "com.redmoon.oa.dept.*" %>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if(true){
	request.getRequestDispatcher("user_multi_sel.jsp?mode=single&parameterNum=4").forward(request, response);
	return;
}
String unitCode = ParamUtil.get(request, "unitCode");
if (unitCode.equals("")) {
	unitCode = privilege.getUserUnitCode(request);
}

int pagesize = 20;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<TITLE><lt:Label res="res.flow.Flow" key="selectUser"/></TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
  <script src='dwr/interface/DeptUserDb.js'></script>
  <script src='dwr/engine.js'></script>
  <script src='dwr/util.js'></script>
  <script src='inc/common.js'></script>
  <script>
  function updateResults(deptCode) {
    DWRUtil.removeAllRows("postsbody");	
	
	if(deptCode=="<%=DeptDb.ROOTCODE%>"){
		document.getElementById("deptUsers").style.display = "";
		document.getElementById("pagesize").style.display = "";
		document.getElementById("searchIt").style.display = "";
		ajaxExchange(1, <%=pagesize%>);
	}else{
		document.getElementById("deptUsers").style.display = "none";
		document.getElementById("pagesize").style.display = "none";
		document.getElementById("searchIt").style.display = "none";
		DeptUserDb.list2DWR(fillTable, deptCode);
	}
	
    o("resultTable").style.display = '';
  }
  
  var getCode = function(unit) { return unit.deptCode };
  var getName = function(unit) { return unit.deptName };
  var getUserRealName = function(unit) { return unit.userRealName };
  var getUserName = function(unit) { 
  	  var u = unit.userRealName;
	  if (u!=null && u!="")
		return "<a href='javascript:;' onClick=\"setPerson('" + unit.deptCode + "', '" + unit.deptName + "', '" + unit.userName + "', '" + unit.userRealName + "')\">" + u + "</a>" 
	  else
	  	return "无";
  };
  
  function fillTable(apartment) {
    DWRUtil.addRows("postsbody", apartment, [ getName, getUserName ]);
  }

  function setPerson(deptCode, deptName, userName, userRealName) {
  	var dlg = window.opener ? window.opener : dialogArguments;

	<%
	// 不能直接调用dlg.setPerson，因为有可能是从表单中UserSelectWinCtl调用
	String isForm = ParamUtil.get(request, "isForm");
	if (isForm.equals("true")) {
		%>
		dlg.setIntpuObjValue(userName, userRealName);
		<%
	}
	else {
	%>
		dlg.setPerson(deptCode, deptName, userName, userRealName);
	<%}%>

	window.close();
  }
  </script>
  <script>
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

function ShowChild(imgobj, name) {
	var tableobj = document.getElementById("childof"+name);
	if (tableobj==null) {
		window.frames["ifrmGetChildren"].location.href = "admin/dept_ajax_getchildren.jsp?op=func&target=_self&parentCode=" + name;
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1) {
			imgobj.src = "images/i_minus.gif";
		}
		else
			imgobj.src = "images/i_plus.gif";
		return;
	}
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "images/i_minus.gif";
		else
			imgobj.src = "images/i_plus.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "images/i_minus.gif";
		else
			imgobj.src = "images/i_plus.gif";
	}
}

function initPages() {
   var dlg = window.opener ? window.opener : dialogArguments;
   if (typeof(dlg.getDept)=="function") {
	   var depts = dlg.getDept().trim();
	   if (depts!="") {
		   depts = "," + depts + ",";
		   if (depts.indexOf(",root,")==-1)
			  return;
	   }
   }
	
   document.getElementById("deptUsers").style.display = "";
   document.getElementById("pagesize").style.display = "";
   document.getElementById("searchIt").style.display = "";
   ajaxExchange(1, <%=pagesize%>);
}

function window_onload() {
   try {
	   // var depts = dialogArguments.getDept();
	   var dlg = window.opener ? window.opener : dialogArguments;
	   var depts = dlg.getDept().trim();
	   
	   if (depts!="") {
		   var ary = depts.split(",");
		   var isFinded = true;
	   	   isFinded = false;
		   var len = document.getElementById('deptTree').getElementsByTagName('a').length;
		   for(var i=0; i<len; i++) {
		   		try {
					var aObj = document.getElementById('deptTree').getElementsByTagName('a')[i];
					var canSel = false;
					for (var j=0; j<ary.length; j++) {
						if (aObj.outerHTML.indexOf("'" + ary[j] + "'")!=-1) {
							canSel = true;
							// alert(canSel);
							break;
						}
					}
					if (!canSel) {
						aObj.innerHTML = "<font color='#888888'>" + aObj.innerText + "</font>";
						aObj.outerHTML = aObj.outerHTML.replace(/onClick/gi, "''");
					}
						
					isFinded = true;
				}
				catch (e) {}
		   }
	   }
   }
   catch (e) {}

   initPages();
	
}

function insertAdjacentHTML(objId,code,isStart){ 
	var obj = document.getElementById(objId);
	if(isIE()) 
		obj.insertAdjacentHTML(isStart ? "afterbegin" : "afterEnd",code); 
	else{ 
		var range=obj.ownerDocument.createRange(); 
		range.setStartBefore(obj); 
		var fragment = range.createContextualFragment(code); 
		if(isStart) 
			obj.insertBefore(fragment,obj.firstChild); 
		else 
			obj.appendChild(fragment); 
	}
}
  </script>
</HEAD>
<BODY onLoad="window_onload()">
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr> 
    <td height="24" colspan="2" class="tdStyle_1"><lt:Label res="res.flow.Flow" key="selectUser"/></td>
  </tr>
  <tr> 
    <td id="deptTree" width="423" height="87" valign="top"><%
DeptMgr dm = new DeptMgr();
DeptDb dd = dm.getDeptDb(unitCode);
DeptView tv = new DeptView(dd);
tv.ListFuncAjax(request, out, "_self", "updateResults", "", "",true );
%></td>
    <td width="491" align="center" valign="top">
	<div id="resultTable">
	  <table class="tabStyle_1" width="100%" border="0" cellpadding="4" cellspacing="0">
      <thead>
        <tr>
          <td class="tabStyle_1_title" width="50%"><lt:Label res="res.flow.Flow" key="depart"/></td>
          <td class="tabStyle_1_title"><lt:Label res="res.flow.Flow" key="staffMember"/></td>
        </tr>
      </thead>
      <tbody id="postsbody">

      </tbody>      
        <tr>
        <td id="deptUsers" colspan="3" style="display:none;padding:0px">  	 
        </td>
        </tr>      
        <tr>
        <td id="pagesize" colspan="3" align="right" style="display:none;">
		<%
        String strcurpage = "1";
        int curpage = Integer.parseInt(strcurpage);
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
		String orderField = showByDeptSort ? "du.orders" : "u.orders";
        String sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and u.unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request)) + " order by du.DEPT_CODE asc, " + orderField + " asc";
        DeptUserDb du = new DeptUserDb();
        ListResult lr = du.listResult(sql, curpage, pagesize);
        int total = lr.getTotal();
        Paginator paginator = new Paginator(request, total, pagesize);
        // 设置当前页数和总页数
        int totalpages = paginator.getTotalPages();
        int pageBlock = 3;
        int curPageMax = 10;
        if (totalpages < pageBlock)
            curPageMax = totalpages;
        else
            curPageMax = pageBlock;
        %>
        <a href="javascript:;" onclick="firstPage()"><lt:Label res="res.flow.Flow" key="homePage"/></a>&nbsp;
        <a href="javascript:;" onclick="pageUp()"><lt:Label res="res.flow.Flow" key="upPage"/></a>&nbsp;
        <span id="pageSpan">
        <%
        for(int i=1;i<=curPageMax;i++) {
        %>
            <a id="pageNum<%=i%>" href="javascript:;" onclick="ajaxExchange(<%=i%>,<%=pagesize%>)"><%=i%></a>&nbsp;
        <%}%>
        </span>
        &nbsp;<a href="javascript:;" onclick="pageDown()"><lt:Label res="res.flow.Flow" key="downPage"/></a>
        &nbsp;<a href="javascript:;" onclick="lastPage()"><lt:Label res="res.flow.Flow" key="lastPage"/></a>
        &nbsp;&nbsp;       
        </td>
        </tr>
        <tr>
        <td id="searchIt" colspan="3" align="right" style="display:none;">
        <div style="float:left;"><lt:Label res="res.flow.Flow" key="userName"/>&nbsp;<input type="text" id=search name=search>
        <input class="btn" type="button" onclick="searchByName()" value="<lt:Label res='res.flow.Flow' key='lastPage'/>" /></div> 
        </td>
        </tr>            
    </table>
	</div>
    </td>
  </tr>
  <tr align="center">
    <td height="28" colspan="2"><input type="button" class="btn" name="cancelbtn" value="<lt:Label res='res.flow.Flow' key='close'/>" onClick="window.close()">    </td>
  </tr>
</table>
<iframe id="ifrmGetChildren" name="ifrmGetChildren" style="display:none" width="300" height="300" src=""></iframe>
</BODY>
<script src='js/jquery.js'></script>
<script>
var j$ = jQuery.noConflict();
var curPage = 1;
function ajaxExchange(cPages, pagesize) {
	jQuery("a[id^='pageNum']").removeClass("activePageNum");
	jQuery("#pageNum" + cPages).addClass("activePageNum");
	
	curPage = cPages;
	j$.ajax({
		type: "post",
		url: "user_sel_ajax.jsp",
		data : {
		    op: "getResult",
        	CPages : cPages,
			pagesize : pagesize
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			var re = j$.parseJSON(data);
			if (re.ret=="1") {
				document.getElementById("deptUsers").innerHTML = re.result;
				allUserOfDept = re.allUserOfDept;
				allUserRealNameOfDept = re.allUserRealNameOfDept;
				//alert(re.result);
			}		
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(){
			//请求出错处理
		}
	});
}

var curPageMax = <%=curPageMax%>;
function pageDown() {
	if (curPage == <%=totalpages%>)
		return;
	if (curPage == curPageMax) {
		var oldCurPageMax = curPageMax;
		curPageMax += <%=pageBlock%>;
		if (curPageMax > <%=totalpages%>) {
			curPageMax = <%=totalpages%>;
		}
		initPageSpan(oldCurPageMax+1, curPageMax);
		ajaxExchange(curPage + 1, <%=pagesize%>);
	}
	else
		ajaxExchange(curPage + 1, <%=pagesize%>);
}

function initPageSpan(fromPage, toPage) {
	var str = "";
	for (i=fromPage; i<=toPage; i++) {
		str += "<a id=\"pageNum" + i + "\" href=\"javascript:;\" onclick=\"ajaxExchange(" + i + ",<%=pagesize%>)\">" + i + "</a>&nbsp;";
	}
	o("pageSpan").innerHTML = str;
}

function pageUp() {
	if (curPage==1)
		return;
	if (curPage == Math.floor(curPageMax/<%=pageBlock%>) * <%=pageBlock%> + 1) {
		// alert(curPage);
		curPageMax = Math.floor(curPageMax/<%=pageBlock%>) * <%=pageBlock%>;
		curPageStart = curPageMax - <%=pageBlock%> + 1;
		// alert(curPageMax + "--" + <%=pageBlock%>);
		initPageSpan(curPageStart, curPageMax);
		ajaxExchange(curPage - 1, <%=pagesize%>);
	}
	else
		ajaxExchange(curPage - 1, <%=pagesize%>);
}

function firstPage() {
	if (<%=pageBlock%> < <%=totalpages%>) {
		initPageSpan(1, <%=pageBlock%>);
	}
	else
		initPageSpan(1, <%=totalpages%>);
	ajaxExchange(1, <%=pagesize%>);		
}

function lastPage() {
	var p = Math.floor(<%=totalpages%> / <%=pageBlock%>);
	var lastPage = p + <%=pageBlock%> + 1;
	if (lastPage > <%=totalpages%>) {
		lastPage = <%=totalpages%>;
	}
	initPageSpan(p * <%=pageBlock%> + 1, lastPage);
	ajaxExchange(<%=totalpages%>, <%=pagesize%>);		
}

function searchByName(){
  var name = j$("#search").val();
  searchByNameAjax(name,<%=pagesize%>);
}

function searchByNameAjax(name,pagesize) {
	document.getElementById("deptUsers").style.display = "none";
	document.getElementById("pagesize").style.display = "none";
	document.getElementById("searchIt").style.display = "none";
	
	j$.ajax({
		type: "post",
		url: "user_sel_ajax.jsp",
		data : {
		    op: "getResult",
        	name : name,
			pagesize : pagesize
        },
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			//ShowLoading();
		},
		success: function(data, status){
			var re = j$.parseJSON(data);
						
			if (re.ret=="1") {
				document.getElementById("deptUsers").innerHTML = re.result;
				o("deptUsers").style.display = "";
			}		
		},
		complete: function(XMLHttpRequest, status){
			//HideLoading();
		},
		error: function(){
			//请求出错处理
		}
	});	
}

if(!isIE()){ // firefox innerText define
   HTMLElement.prototype.__defineGetter__("innerText", 
    function(){
     var anyString = "";
     var childS = this.childNodes;
     for(var i=0; i<childS.length; i++) {
      if(childS[i].nodeType==1)
       anyString += childS[i].tagName=="BR" ? '\n' : childS[i].textContent;
      else if(childS[i].nodeType==3)
       anyString += childS[i].nodeValue;
     }
     return anyString;
    } 
   ); 
   HTMLElement.prototype.__defineSetter__("innerText", 
    function(sText){
     this.textContent=sText;
    }
   ); 
}
</script>
</HTML>
