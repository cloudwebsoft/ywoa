<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="org.json.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv = "read";
	if (!privilege.isUserPrivValid(request, priv)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request,
				cn.js.fan.web.SkinUtil.LoadString(request,
						"pvg_invalid")));
		return;
	}

	int flowId = ParamUtil.getInt(request, "flowId");
	WorkflowDb wf = new WorkflowDb();
	wf = wf.getWorkflowDb(flowId);
	String typeCode = wf.getTypeCode();
	Leaf lf = new Leaf();
	lf = lf.getLeaf(typeCode);
	int templateId = lf.getTemplateId();

	UserDb user = new UserDb();
	user = user.getUserDb(privilege.getUser(request));
	String userRealName = user.getRealName();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<title>拟文 - 在线编辑</title>
<script>
var TANGER_OCX;
function init(){
//获取文档控件对象
// TANGER_OCX.Caption = "拟文";

TANGER_OCX = document.getElementById('TANGER_OCX');
TANGER_OCX.IsUseUTF8Data = true;
//创建新文档
TANGER_OCX.CreateNew("Word.Document");

ntko_NotifyCtrlReady();
}

function AddMyMenuItems()
{
 	try
	{
		//在自定义主菜单中增加菜单项目
		TANGER_OCX.AddCustomMenuItem('上传文件',false,false,1);
		TANGER_OCX.AddCustomMenuItem('图片签章',false,false,2);
		TANGER_OCX.AddCustomMenuItem('手写签名',false,false,3);
		TANGER_OCX.AddCustomMenuItem('套用表单字段',false,false,4);
		TANGER_OCX.AddCustomMenuItem('创建Word文档',false,false,5);
		TANGER_OCX.AddCustomMenuItem('创建WPS文档',false,false,6);
        // 在文件菜单中增加菜单项目
		// TANGER_OCX.AddFileMenuItem('创建Word文档',false,false,1);
		// TANGER_OCX.AddFileMenuItem('');
	}
   	catch(err){
		alert("不能创建新对象："+ err.number +":" + err.description);
	}
	finally{
	}
}

function ntko_NotifyCtrlReady() {
	applyTemplate();
	
	<%
	FormDb fd = new FormDb();
	fd = fd.getFormDb(lf.getFormCode());
	Iterator irff = fd.getFields().iterator();
	while (irff.hasNext()) {
		FormField ff = (FormField) irff.next();
		%>
		var obj = window.opener.o("<%=ff.getName()%>"); // document.getElementById(bname);
		if (obj) {
			var val = "";
			if (obj.value)
				val = obj.value;
			else
				val = obj.innerHTML;
				
			var bookmarkname = "<%=ff.getName()%>";
			if(TANGER_OCX.ActiveDocument.BookMarks.Exists(bookmarkname)) { 
				TANGER_OCX.SetBookmarkValue(bookmarkname, val);
			}
		}
	<%}%>	
	
	<%
	com.redmoon.oa.flow.FormDAO fdao = new com.redmoon.oa.flow.FormDAO();
	fdao = fdao.getFormDAO(flowId, fd);

	// 将数据赋予给流程中的主表单
	// setFieldsValue(fd.getCode(), resultRecord, fdao.getFields());

	// 获取子表单的数据
	int tableCount = 1;
	MacroCtlMgr mm = new MacroCtlMgr();			
	Iterator ir = fd.getFields().iterator();
	while (ir.hasNext()) {
		FormField ff = (FormField) ir.next();
				
		if (ff.getType().equals(FormField.TYPE_MACRO)) {
			MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());

			if (mu != null) {
				String nestFormCode = null;
				if (mu.getNestType() == MacroCtlUnit.NEST_TYPE_TABLE) {
							String nestFieldName = ff.getName();
					nestFormCode = ff.getDescription();
					try {
						// 20131123 fgf 添加
						String defaultVal = StrUtil.decodeJSON(ff.getDescription());
						JSONObject json = new JSONObject(defaultVal);
						nestFormCode = json.getString("destForm");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
					
				}
				else if (mu.getNestType() == MacroCtlUnit.NEST_TYPE_NORMAIL) {
					nestFormCode = ff.getDescription();
					try {
						// 20131123 fgf 添加
						String defaultVal = StrUtil.decodeJSON(ff.getDescription());
						JSONObject json = new JSONObject(defaultVal);
						nestFormCode = json.getString("destForm");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
						// LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123. ff.getDefaultValueRaw()=" + ff.getDefaultValueRaw());
					}		
				}
				
				if (nestFormCode!=null) {
					%>
					var table;
					try {
						table = TANGER_OCX.ActiveDocument.Tables(<%=tableCount%>);
					} catch(err) {
					};
					<%
					String sql = "select id from " + FormDb.getTableName(nestFormCode) + " where cws_id=" + fdao.getId() + " order by cws_order";
					ModuleSetupDb msd = new ModuleSetupDb();
					msd = msd.getModuleSetupDb(nestFormCode);
					// String listField = StrUtil.getNullStr(msd.getString("list_field"));
					String[] fields = msd.getColAry(false, "list_field");

					int row = 2;
					FormDb fdNest = new FormDb();
					fdNest = fdNest.getFormDb(nestFormCode);
					com.redmoon.oa.visual.FormDAO fdaoNest = new com.redmoon.oa.visual.FormDAO();
					Vector fdaoV = fdaoNest.list(nestFormCode, sql);
					Iterator irNest = fdaoV.iterator();
					while (irNest.hasNext()) {
						fdaoNest = (com.redmoon.oa.visual.FormDAO)irNest.next();
						
						int len = 0;
						if (fields!=null)
							len = fields.length;
						if (len==0)
							continue;
						%>
						table.rows.add();
						<%
						for (int i=0; i<len; i++) {
							String val = fdaoNest.getFieldValue(fields[i]);
							FormField ffNest = fdNest.getFormField(fields[i]);
							// 如果是宏控件，则转换为html显示值
							if (ffNest.getType().equals(FormField.TYPE_MACRO)) {
								MacroCtlUnit muNest = mm.getMacroCtlUnit(ffNest.getMacroType());
								val = muNest.getIFormMacroCtl().converToHtml(request, ffNest, val);
							}
							%>
							table.Cell(<%=row%>, <%=i+1%>).range.Text="<%=val%>";
							<%
						}
						row ++;
					}
					tableCount ++;
				}
			}
		}
	}%>
	
}

function applyTemplate() {
	<%if (templateId==-1) {%>
		TANGER_OCX_DoTaoHong("<%=Global.getFullRootPath(request)%>/flow/empty_word.doc", true);
	<%}else{
		DocTemplateDb dtd = new DocTemplateDb();
		dtd = dtd.getDocTemplateDb(templateId);
		
		if (!dtd.isLoaded()) {
			%>
			alert('<lt:Label res="res.flow.Flow" key="templateNotExist"/>');
			// window.close();
			return;
			<%
		}
	%>
		TANGER_OCX_DoTaoHong("<%=dtd.getFileUrl(request)%>", false);
	<%}%>
}

// 简单套红
function TANGER_OCX_DoTaoHong(URL, isEmpty)
{
	try{
		TANGER_OCX.ActiveDocument.Application.Selection.HomeKey(6);
		if (isEmpty) {
			TANGER_OCX.OpenFromURL(URL);
		} else {
			TANGER_OCX.AddTemplateFromURL(URL);
		}
	}
	catch(err)
	{alert("<lt:Label res='res.flow.Flow' key='notCreateObject'/>"+ err.number +":" + err.description);};
}
</script>
</HEAD>
<BODY onLoad="init();AddMyMenuItems();">
<object id="TANGER_OCX" classid="clsid:C9BC4DFF-4248-4a3c-8A49-63A7D317F404" codebase="../activex/OfficeControl.cab#version=5,0,2,1" width="100%" height="100%" >
<param name="CustomMenuCaption" value="操作">
<param name="Caption" value="拟文">
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
</BODY>
<script language="JScript" for="TANGER_OCX" event="OnCustomMenuCmd(menuIndex,menuCaption,menuID)">
switch(menuID)
{
case 1:
upload();
break;
case 2:
useStamp();
break;
case 3:
useHandSign();
break;
case 4:
replaceText();
break;
case 5:
creatDOC();
ntko_NotifyCtrlReady();
break;
case 6:
createWPS();
ntko_NotifyCtrlReady();
break;
}
</script>
<script>
function createWPS(){
 TANGER_OCX.CreateNew("WPS.Document");
 document.getElementById("exts").value = "WPS";
}
function creatDOC(){
 TANGER_OCX.CreateNew("Word.Document");
 document.getElementById("exts").value = "DOC";
}

//上传
function upload() {
	var msg = TANGER_OCX.SaveToURL("<%=Global.getFullRootPath(request)%>/flow/flow_ntko_dispose_do.jsp","some.doc","flowId=<%=flowId%>&templateId=<%=templateId%>","some.doc","myForm");
	window.opener.refreshAttachments();
	if(msg=="success"){
		alert('<lt:Label res="res.common" key="info_op_success"/>');
		window.close();
	}else if(msg=="fail"){
		alert('<lt:Label res="res.common" key="info_op_fail"/>');
	}else{
		alert(msg);
		window.close();
	}
}
</script>
<script>
//图片印章
function useStamp() {
   openChooseStamp("getstamp");
   //alert(URL);
}
<%String rootpath = request.getContextPath();%>
function openWinForFlowAccess(url,width,height)
{
	var newwin = window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=400,left=550,width="+width+",height="+height);
}

function openWinStamp(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/flow/flow_ntko_stamp_win.jsp?stampId="+obj, 300, 150);
	
}

function openChooseStamp(obj) {
	inputObj = obj;
	openWinForFlowAccess("<%=rootpath%>/flow/flow_ntko_stamp_choose.jsp", 300, 150);
	
}

function AddSignFromURL(URL)
{
     // alert(URL);
     // alert(TANGER_OCX_key);
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

function AddPictureFromURL(URL)
{
    TANGER_OCX.AddPicFromURL(
		URL,//URL 注意；URL必须返回Word支持的图片类型。
		true,//是否浮动图片
		0, 
		0,
        1, //当前光标处
		100,//无缩放
		1 //文字上方
)
	};
</script>
<script>
function useHandSign()
{
   DoHandSign();
}
function DoHandSign()
{
//   alert(TANGER_OCX_key);
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
</script>
<script>
function replaceText()
{
    rangeWord = TANGER_OCX.ActiveDocument.Content; // 获取当前文档文字部分
	<%// FormDb fd = new FormDb();
			// fd = fd.getFormDb(lf.getFormCode());
			irff = fd.getFields().iterator();
			while (irff.hasNext()) {
				FormField ff = (FormField) irff.next();%>
		var obj = window.opener.o("<%=ff.getName()%>"); // document.getElementById(bname);
		if (obj) {
			var val = "";
			if (obj.value)
				val = obj.value;
			else
				val = obj.innerHTML;
			searchStr = "【<%=ff.getTitle()%>】";
			rangeWord.Find.Execute(searchStr,false,false,false,false,false,true,1,false,val,2); // 执行查找替换方法
		}
		<%}%>
}
</script>
</html>