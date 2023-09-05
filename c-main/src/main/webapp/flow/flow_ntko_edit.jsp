<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@ page import="com.redmoon.oa.base.IFormMacroCtl" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int flowId = ParamUtil.getInt(request, "flowId");
int file_id = ParamUtil.getInt(request,"file_id");
int isRevise = StrUtil.toInt(ParamUtil.get(request,"isRevise"), 0);
int doc_id = ParamUtil.getInt(request,"doc_id");
boolean isApply = ParamUtil.get(request, "isApply").equals("true");

WorkflowDb wf = new WorkflowDb();
wf = wf.getWorkflowDb(flowId);
String typeCode = wf.getTypeCode();
Leaf lf = new Leaf();
lf = lf.getLeaf(typeCode);
/*
int templateId = lf.getTemplateId();
*/

int templateId = ParamUtil.getInt(request, "templateId", -1);

int actionId = ParamUtil.getInt(request, "actionId");
WorkflowActionDb wad = new WorkflowActionDb();
wad = wad.getWorkflowActionDb(actionId);

UserDb user = new UserDb();
user = user.getUserDb(privilege.getUser(request));
String userRealName = user.getRealName();

Document doc = new Document();
doc = doc.getDocument(wf.getDocId());
Attachment att = doc.getAttachment(1, file_id);
// 上锁
att.setLockUser(user.getName());
att.save();

String ext = StrUtil.getFileExt(att.getName());
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<title>流程 - 在线编辑</title>
<script language="JScript" for=TANGER_OCX event="OnDocumentOpened(TANGER_OCX_str,TANGER_OCX_obj)">
TANGER_OCX_OnDocumentOpened(TANGER_OCX_str,TANGER_OCX_obj)
</script>

<script>
var TANGER_OCX;
function init(){
	//获取文档控件对象
	TANGER_OCX = document.getElementById('TANGER_OCX');
	TANGER_OCX.IsUseUTF8Data = true;
	//创建新文档
	//TANGER_OCX.CreateNew("Word.Document");
	
	var url = "../flow/download.do?attachId=<%=file_id%>&flowId=<%=flowId%>";

	if (<%=ext.equals("xls") || ext.equals("xlsx")%>) {
		TANGER_OCX.OpenFromURL(url, false, "excel.sheet");
	} else if (<%=ext.equals("doc") || ext.equals("docx")%>) {
		TANGER_OCX.OpenFromURL(url, false, "Word.Document");
	} else if (<%=ext.equals("wps")%>) {
		TANGER_OCX.OpenFromURL(url, false, "WPS.Document");
	} else {
		TANGER_OCX.OpenFromURL(url);
	}
}

<%
DocTemplateDb dtd = new DocTemplateDb();
dtd = dtd.getDocTemplateDb(templateId);
%>
var templateUrl = "<%=dtd.getFileUrl(request)%>";

function TANGER_OCX_OnDocumentOpened(TANGER_OCX_str,TANGER_OCX_obj) {
	TANGER_OCX_SetDocUser("<%=userRealName%>");

	<%if (isApply) {%>
		applyTempalte(templateUrl);
		// 置为只读后，将不能再次套红
		// TANGER_OCX.SetReadOnly(true);
	<%}%>
	// 套表单内容
	replaceText();

	// 接受修订
	// TANGER_OCX_AcceptAllRevisions();
	
	<%
	if(true || isRevise!=0){
	%>
	TANGER_OCX_SetMarkModify(true);
	TANGER_OCX_ShowRevisions(false); // 默认不显示修订
	<%} else {%>
	// TANGER_OCX_SetMarkModify(false);
	<%}%>
}

function TANGER_OCX_SetDocUser(cuser) {
	with(TANGER_OCX.ActiveDocument.Application) {
		UserName = cuser;
	}	
}

function TANGER_OCX_SetMarkModify(boolvalue) {
	TANGER_OCX_SetReviewMode(boolvalue);
	//TANGER_OCX_EnableReviewBar(!boolvalue);
}

function TANGER_OCX_SetReviewMode(boolvalue) {
	try {
		TANGER_OCX.ActiveDocument.TrackRevisions = boolvalue;
	}
	catch (e) {}
}

function TANGER_OCX_EnableReviewBar(boolvalue) {
	TANGER_OCX.ActiveDocument.CommandBars("Reviewing").Enabled = boolvalue;
	TANGER_OCX.ActiveDocument.CommandBars("Track Changes").Enabled = boolvalue;
	TANGER_OCX.IsShowToolMenu = boolvalue;	//关闭或打开工具菜单
}

//接受所有修订
function TANGER_OCX_AcceptAllRevisions() {
   if (typeof(TANGER_OCX.ActiveDocument.AcceptAllRevisions)=="unknown") {
	   TANGER_OCX.ActiveDocument.AcceptAllRevisions();
   }
   
   //if (TANGER_OCX.ActiveDocument.AcceptAllRevisions!=undefined)
   //   TANGER_OCX.ActiveDocument.AcceptAllRevisions();
}

function AddMyMenuItems() {
 	try	{
		//在自定义主菜单中增加菜单项目
		<%if (wad.canReceiveRevise()) {%>
		TANGER_OCX.AddCustomMenuItem('定稿保存',false,false,6);
		<%}else{%>
		TANGER_OCX.AddCustomMenuItem('保存文件',false,false,1);
		<%}%>
		//TANGER_OCX.AddCustomMenuItem('');
		TANGER_OCX.AddCustomMenuItem('图片签章',false,false,2);
		TANGER_OCX.AddCustomMenuItem('手写签名',false,false,3);
		
		TANGER_OCX.AddCustomMenuItem('显示修订',false,false,4);
		TANGER_OCX.AddCustomMenuItem('隐藏修订',false,false,5);
		
        //在文件菜单中增加菜单项目
		//TANGER_OCX.AddFileMenuItem('创建Word文档',false,false,1);
		//TANGER_OCX.AddFileMenuItem('创建Excel文档',false,false,2);
		//TANGER_OCX.AddFileMenuItem('创建PPT文档',false,false,3);
		//TANGER_OCX.AddFileMenuItem('关闭文档',false,true,4);
		//TANGER_OCX.AddFileMenuItem('');
	}
   	catch(err){
		alert("不能创建新对象："+ err.number +":" + err.description);
	}
	finally{
	}
}
</script>
</HEAD>
<BODY onload="init();AddMyMenuItems();" onunload="clearLocker();">
<object id="TANGER_OCX" classid="clsid:C9BC4DFF-4248-4a3c-8A49-63A7D317F404" codebase="../activex/OfficeControl.cab#version=5,0,2,1" width="100%" height="100%" >
<param name="CustomMenuCaption" value="操作">
<param name="Caption" value="文件 - 编辑">
<param name="MakerCaption" value="cloudweb">
<param name="MakerKey" value="0727BEFE0CCD576DFA15807DA058F1AC691E1904">
<%
if (com.redmoon.oa.kernel.License.getInstance().isOem()) {%>
<param name="ProductCaption" value="<%=License.getInstance().getCompany()%>">
<param name="ProductKey" value="<%=License.getInstance().getOfficeControlKey()%>">
<%}else{ %>
<param name="ProductCaption" value="YIMIOA">
<param name="ProductKey" value="D026585BDAFC28B18C8E01C0FC4C0AA29B6226B5">
<%} %>
<SPAN STYLE="color:red">该网页需要控件浏览.浏览器无法装载所需要的文档控件.请检查浏览器选项中的安全设置.</SPAN>
</object>
<form id="myForm" METHOD="post" ACTION="<%=Global.getFullRootPath(request)%>/flow/flow_ntko_dispose_do.jsp" ENCTYPE="multipart/form-data" NAME="myForm">
</FORM>
<br />

</BODY>
<script language="JScript" for="TANGER_OCX" event="OnCustomMenuCmd(menuIndex,menuCaption,menuID)">
switch(menuID)
{
case 1:
//TANGER_OCX_AcceptAllRevisions();
edit();
break;
case 2:
userStamp();
break;
case 3:
useHandSign();
break;
case 4:
TANGER_OCX_ShowRevisions(true);
TANGER_OCX_SetMarkModify(true);
break;
case 5:
TANGER_OCX_ShowRevisions(false);
TANGER_OCX_SetMarkModify(false);
break;
case 6:
TANGER_OCX_AcceptAllRevisions();
edit();
break;
}
</script>
<script>
function edit() {
	var msg = TANGER_OCX.SaveToURL("<%=Global.getFullRootPath(request)%>/flow_document_check.jsp","some.doc","flowId=<%=flowId%>&doc_id=<%=doc_id%>&file_id=<%=file_id%>&templateId=<%=templateId%>","some.doc","myForm");
	alert(msg);
}
</script>
<script>
function TANGER_OCX_ShowRevisions(boolvalue) {
	if (TANGER_OCX.ActiveDocument.ShowRevisions != undefined)
		TANGER_OCX.ActiveDocument.ShowRevisions = boolvalue;
}

//图片印章
function userStamp() {
	openChooseStamp("getstamp");
	//alert(URL);
}
<% String rootpath = request.getContextPath(); %>

function openWinForFlowAccess(url, width, height) {
	var newwin = window.open(url, "_blank", "toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=400,left=550,width=" + width + ",height=" + height);
}

function openWinStamp(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/flow/flow_ntko_stamp_win.jsp?stampId="+obj, 300, 150);
}

function openChooseStamp(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/flow/flow_ntko_stamp_choose.jsp", 300, 150);
}

function test() {
	AddSignFromURL(URL);
}

function AddSignFromURL(URL) {
	TANGER_OCX.AddSignFromURL(
			'<%=userRealName%>',//当前登陆用户
			URL,//URL
			50,//left
			50,
			"1",
			1,
			100,
			0)
}

function AddPictureFromURL(URL) {
	try {
		URL = '<%=request.getContextPath()%>/showImg.do?path=' + URL;
		TANGER_OCX.AddPicFromURL(
				URL,//URL 注意；URL必须返回Word支持的图片类型。
				true,//是否浮动图片
				0,
				0,
				1, //当前光标处
				100,//无缩放
				1 //文字上方
		)
	} catch (e) {

	}
};
</script>
<script>
function useHandSign() {
   DoHandSign();
}
function DoHandSign() {
	TANGER_OCX.DoHandSign(
	'<%=userRealName%>',//当前登陆用户 必须
	0,//笔型0－实线 0－4 //可选参数
	0x000000ff, //颜色 0x00RRGGBB//可选参数
	2,//笔宽//可选参数
	100,//left//可选参数
	50,//top//可选参数
	false,//可选参数
	1
	); 
}

// 应用模板，模板中需要有zhengwen标签
function applyTempalte(templateUrl) {
	try {
		// 选择对象当前文档的所有书签内容
		var bk = TANGER_OCX.ActiveDocument.Bookmarks;
		// 如果页面中已经含有zw，则说明是二次套红，取当前文件的正文部分置于剪切板中
		if(bk.Exists("zw") || bk.Exists("正文")) {
			var mark;
			if (bk.Exists("zw")) {
				TANGER_OCX.ActiveDocument.Application.Selection.GoTo(-1,0,0,"zw");
				mark = TANGER_OCX.ActiveDocument.Bookmarks("zw");
			}
			else {
				TANGER_OCX.ActiveDocument.Application.Selection.GoTo(-1,0,0,"正文");
				mark = TANGER_OCX.ActiveDocument.Bookmarks("正文");
			}

			var range = mark.Range;
			if (range == null || range == "") {
				alert("模板中没有名称为zw或正文的标签");
				return;
			}
			// 如果存在则剪切
			range.Cut();

			// 删除全部内容，以便于重新应用模板
			TANGER_OCX.ActiveDocument.Application.Selection.HomeKey(6);//光标定位到文件头
			var curSel = TANGER_OCX.ActiveDocument.Application.Selection;
			curSel.WholeStory();
			curSel.Delete();

			// 删除书签，因清除全部内容并不能同时删除书签，会使得全部书签移至被清空文档的首部位置
			// 致AddTemplateFromURL后模板中的书签可能因同名的原因而无效，最后书签都在文档首部位置
			var count = TANGER_OCX.ActiveDocument.BookMarks.Count;
			while (count > 0) {
				// 注意不能在循环中用Item(i).Delete的方式删除，因为这样会使得长度发生实时变化，使得在删除时报“集合所要求的成员不存在”
				var markName = TANGER_OCX.ActiveDocument.BookMarks.Item(1).Name;
				TANGER_OCX.ActiveDocument.BookMarks(markName).Delete();
				count = TANGER_OCX.ActiveDocument.BookMarks.Count;
			}

			// 应用模板
			TANGER_OCX.AddTemplateFromURL(templateUrl);
		}
		else {
			var curSel = TANGER_OCX.ActiveDocument.Application.Selection;
			curSel.WholeStory();
			curSel.Cut();
			// 插入模板
			TANGER_OCX.AddTemplateFromURL(templateUrl);
		}

		var bkmk = TANGER_OCX.ActiveDocument.BookMarks;
		if (bkmk.Exists("zw"))	{
			var bkmkObj = TANGER_OCX.ActiveDocument.BookMarks("zw");
			var saverange = bkmkObj.Range;
			// 粘贴原来的文件内容
			saverange.Paste();
			TANGER_OCX.ActiveDocument.Bookmarks.Add("zw",saverange);
			// 接受修订
			TANGER_OCX_AcceptAllRevisions();
		}
		else if (bkmk.Exists("正文")) {
			var bkmkObj = TANGER_OCX.ActiveDocument.BookMarks("正文");
			var saverange = bkmkObj.Range;
			// 粘贴原来的文件内容
			saverange.Paste();
			TANGER_OCX.ActiveDocument.Bookmarks.Add("正文",saverange);
			// 接受修订
			TANGER_OCX_AcceptAllRevisions();
		}
		else {
			alert("模板中没有名称为zw的标签（zw表示正文）！");
		}
	}
	catch(err) {
		alert("错误：" + err.number + ":" + err.description);
	}
}

function replaceText() {
    rangeWord = TANGER_OCX.ActiveDocument.Content; // 获取当前文档文字部分
	<%
	MacroCtlMgr mm = new MacroCtlMgr();
	FormDb fd = new FormDb();
	fd = fd.getFormDb(lf.getFormCode());
	java.util.Iterator irff = fd.getFields().iterator();
	while (irff.hasNext()) {
		FormField ff = (FormField)irff.next();
		%>
		// 取得表单域的值
		var obj = window.opener.o("<%=ff.getName()%>"); // document.getElementById(bname);
		if (obj) {
			var isRadio = false;
			var val = "";
			// 如果有value属性
			if (obj.value) { // 如果obj.value为空字符串，则为false
				val = obj.value;
				if (obj.tagName=="INPUT" && obj.type=="checkbox") {
					// 字体应设为MS Gothic 11号字体大小，以达到最佳显示效果
					if (obj.checked) {
						val = "☑";
					}
					else {
						val = "□";
					}
				}
				else if (obj.tagName=="INPUT" && obj.type=="radio") {
					isRadio = true;
					var objs = window.opener.document.getElementsByName(obj.name);
					for (var k=0; k < objs.length; k++) {
						val = objs[k].value;
						searchStr = "【<%=ff.getTitle()%>-" + val + "】";
						if (objs[k].checked) {
							val = "☑";
						}
						else {
							val = "□";
						}
						if (rangeWord != undefined) {
							rangeWord.Find.Execute(searchStr, false, false, false, false, false, true, 1, false, val, 2); // 执行查找替换方法
						}
					}
				}
			}
			else {
				if (obj.tagName=="SELECT") {
					val = obj.options[obj.selectedIndex].text;
				}
				else {
					val = obj.innerHTML;
				}
			}

			if (!isRadio) {
				<%
				boolean isImg = false;
				if(ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					IFormMacroCtl imc = mu.getIFormMacroCtl();
					if (imc!=null) {
						if (imc.getControlType().equals("writePad")
							// || imc.getControlType().equals("img")
						) {
							isImg = true;
				%>
					val = window.opener.o("pad_<%=ff.getName()%>").getAttribute("img");
					if (val!=null && val!="") {
						var bkmks = TANGER_OCX.ActiveDocument.BookMarks;
						if (bkmks.Exists("<%=ff.getTitle()%>")) {
							var markWritePad = TANGER_OCX.ActiveDocument.Bookmarks("<%=ff.getTitle()%>");
							markWritePad.Select();
							var picUrl = val + "?time=" + new Date().getTime(); // 防止缓存
							TANGER_OCX.AddPicFromURL(picUrl,
									false,//是否浮动图片
									0, //如果是浮动图片，相对于左边的Left 单位磅
									5, //如果是浮动图片，相对于当前段落Top
									1, //当前光标处
									30, //缩放百分比
									1 //文字上方
							);
						}
					}
				<%
						}
					}
				}

				if (!isImg) {
				%>
				searchStr = "【<%=ff.getTitle()%>】";
				if (rangeWord != undefined) {
					rangeWord.Find.Execute(searchStr, false, false, false, false, false, true, 1, false, val, 2); // 执行查找替换方法
				}
				<%
				}
				%>
			}
		}
		<%
	}
	%>
}

function clearLocker(){
	$.ajax({
		async: false,
		type: "post",
		url: "<%=rootpath%>/flow/clearLocker.do",
		data : {
			fileId: <%=file_id%>
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){

		},
		success: function(data, status){
			//data = $.parseJSON(data);
			//alert(data.ret);
		},
		complete: function(XMLHttpRequest, status){

		},
		error: function(XMLHttpRequest, textStatus){
			//jAlert(XMLHttpRequest.responseText,'<lt:Label res="res.flow.Flow" key="prompt"/>');
		}
	});
}
</script>
</html>