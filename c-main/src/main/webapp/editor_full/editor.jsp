<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String editorRootPath = request.getContextPath();
%>
<link rel="STYLESHEET" type="text/css" href="<%=editorRootPath%>/editor_full/edit.css">
<script src="<%=editorRootPath%>/inc/common.js"></script>
<script src="<%=editorRootPath%>/js/jquery-1.9.1.min.js"></script>
<script src="<%=editorRootPath%>/js/jquery-migrate-1.2.1.min.js"></script>
<script src="<%=editorRootPath%>/editor_full/editor_js.jsp"></script>
<input type="hidden" id="edit" name="edit" value="" />
<div id="cws_edit">
	<ul id="ExtToolbar0">
	<li >
	<select id="cws_formatSelect"  onchange="cws_doSelectClick('FormatBlock',this)">
	<option><lt:Label res="res.label.editor_full.editor" key="p_format"/></option>
	<option value="&lt;P&gt;"><lt:Label res="res.label.editor_full.editor" key="normal_format"/> 
	<option value="&lt;H1&gt;"><lt:Label res="res.label.editor_full.editor" key="title1"/>
	<option value="&lt;H2&gt;"><lt:Label res="res.label.editor_full.editor" key="title2"/>
	<option value="&lt;H3&gt;"><lt:Label res="res.label.editor_full.editor" key="title3"/>
	<option value="&lt;H4&gt;"><lt:Label res="res.label.editor_full.editor" key="title4"/>
	<option value="&lt;H5&gt;"><lt:Label res="res.label.editor_full.editor" key="title5"/>
	<option value="&lt;H6&gt;"><lt:Label res="res.label.editor_full.editor" key="title6"/>
	<option value="&lt;H7&gt;"><lt:Label res="res.label.editor_full.editor" key="title7"/>
	<option value="&lt;PRE&gt;"><lt:Label res="res.label.editor_full.editor" key="format_already"/> 
	<option value="&lt;ADDRESS&gt;"><lt:Label res="res.label.editor_full.editor" key="address"/>  
	</select>
	<select language="javascript" class="cws_TBGen" id="FontName" onChange="FormatText ('fontname',this[this.selectedIndex].value);">
	<option class="heading" selected><lt:Label res="res.label.editor_full.editor" key="font"/> 
	<option value="宋体"><lt:Label res="res.label.editor_full.editor" key="songti"/> 
	<option value="黑体"><lt:Label res="res.label.editor_full.editor" key="heiti"/>  
	<option value="楷体_GB2312"><lt:Label res="res.label.editor_full.editor" key="kaiti"/>  
	<option value="仿宋_GB2312"><lt:Label res="res.label.editor_full.editor" key="fangsong"/> 
	<option value="隶书"><lt:Label res="res.label.editor_full.editor" key="lishu"/>
	<option value="幼圆"><lt:Label res="res.label.editor_full.editor" key="youyuan"/> 
	<option value="新宋体"><lt:Label res="res.label.editor_full.editor" key="xinsongti"/> 
	<option value="细明体"><lt:Label res="res.label.editor_full.editor" key="ximinti"/> 
	<option value="Arial">Arial 
	<option value="Arial Black">Arial Black 
	<option value="Arial Narrow">Arial Narrow 
	<option value="Bradley Hand ITC">Bradley Hand ITC 
	<option value="Brush Script	MT">Brush Script MT 
	<option value="Century Gothic">Century Gothic 
	<option value="Comic Sans MS">Comic Sans MS 
	<option value="Courier">Courier 
	<option value="Courier New">Courier New 
	<option value="MS Sans Serif">MS Sans Serif 
	<option value="Script">Script 
	<option value="System">System 
	<option value="Times New Roman">Times New Roman 
	<option value="Viner Hand ITC">Viner Hand ITC 
	<option value="Verdana">Verdana 
	<option value="Wide Latin">Wide Latin 
	<option value="Wingdings">Wingdings</option>
	</select>
	<select language="javascript" class="cws_TBGen" id="FontSize" onChange="FormatText('fontsize',this[this.selectedIndex].value);">                                   
	<option class="heading" selected><lt:Label res="res.label.editor_full.editor" key="font_size"/> 
	<option value="1">1 
	<option value="2">2 
	<option value="3">3 
	<option value="4">4 
	<option value="5">5 
	<option value="6">6 
	<option value="7">7</option>
	</select>
	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="font_color"/>' language="javascript" onclick="cws_foreColor();" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/fgcolor.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="font_bg_color"/>' language="javascript" onclick="cws_backColor();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'";> 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/fbcolor.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="symbole"/>' language="javascript" onclick="insertSpecialChar();" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'";> 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/specialchar.gif" /></li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="replace"/>' language="javascript" onclick="cws_replace();" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'";> 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/replace.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="clear_code"/>' language="javascript" onclick="cws_CleanCode();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'";> 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/cleancode.gif" /></li>
	<li> 
	<select ID="Zoom" class="cws_TBGen" onChange="doZoom(this)" >
	<option value="100">100% 
	<option value="50">50% 
	<option value="75">75% 
	<option value="100">100% 
	<option value="125">125% 
	<option value="150">150% 
	<option value="175">175% 
	<option value="200">200%</option>
	</select>
	</li>
	<!--
     <li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="help"/>' language="javascript" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'";> <a href="#"><img src="<%=editorRootPath%>/editor_full/images/help.gif" class="cws_Ico" border="0"></a>	</li>     
	-->
	</ul>
	<ul id="ExtToolbar1"> 
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="select_all"/>' language="javascript" onclick="FormatText('selectAll');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'" onmouseout="this.className='cws_Btn'" > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/selectAll.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="cut"/>' language="javascript" onclick="FormatText('cut');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/cut.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="copy"/>' language="javascript" onclick="FormatText('copy');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/copy.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="paste"/>' language="javascript" onclick="FormatText('paste');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/paste.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="undo"/>' language="javascript" onclick="FormatText('undo');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/undo.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="redo"/>' language="javascript" onclick="FormatText('redo');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/redo.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="link"/>' language="javascript" onclick="cws_forlink();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/wlink.gif" >	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="clear_link"/>' language="javascript" onclick="FormatText('Unlink');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/unlink.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="image"/>' language="javascript" onclick="cws_forimg();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/img.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="hr"/>' language="javascript" onclick="FormatText('InsertHorizontalRule', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/hr.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="table"/>' language="javascript" onclick="cws_fortable();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/table.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="row"/>' language="javascript" onclick="cws_InsertRow();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/insertrow.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="del_row"/>' language="javascript" onclick="cws_DeleteRow();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/deleterow.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="col"/>' language="javascript" onclick="cws_InsertColumn();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/insertcolumn.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="del_col"/>' language="javascript" onclick="cws_DeleteColumn();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/deletecolumn.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="flash"/>' language="javascript" onclick="cws_forswf();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/swf.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="media"/>' language="javascript" onclick="cws_forwmv();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/wmv.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="realplay"/>' language="javascript" onclick="cws_forrm();ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/rm.gif" />	</li>
	</ul>
	<ul id="ExtToolbar2"> 
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="bold"/>' language="javascript" onclick="FormatText('bold', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/bold.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="italic"/>' language="javascript" onclick="FormatText('italic', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/italic.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="underline"/>' language="javascript" onclick="FormatText('underline', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/underline.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="superscript"/>' language="javascript" onclick="FormatText('superscript', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/superscript.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="subscript"/>' language="javascript" onclick="FormatText('subscript', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/subscript.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="del_line"/>' language="javascript" onclick="FormatText('strikethrough', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/strikethrough.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="cancel_format"/>' language="javascript" onclick="FormatText('RemoveFormat', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/removeformat.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="align_left"/>' NAME="Justify" language="javascript" onclick="FormatText('justifyleft', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/aleft.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="align_center"/>' NAME="Justify" language="javascript" onclick="FormatText('justifycenter', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/center.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="align_right"/>' NAME="Justify" language="javascript" onclick="FormatText('justifyright', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/aright.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="code"/>' language="javascript" onclick="FormatText('insertorderedlist', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/numlist.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="item_icon"/>' language="javascript" onclick="FormatText('insertunorderedlist', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/bullist.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="little_brings"/>' language="javascript" onclick="FormatText('outdent', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/outdent.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="more_brings"/>' language="javascript" onclick="FormatText('indent', '');ondrag='return false;'" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/indent.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="emote"/>' language="javascript" onclick="cws_foremot()" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'"; > 
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/smiley.gif" / >	</li>
	<!--
	<li class="cws_Btn" title="上传文件" language="javascript" onclick="cws_forfile()" onmouseover="this.className='cws_BtnMouseOverUp'"; onmouseout="this.className='cws_Btn'";>
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/file.gif" />
	</li>
	-->
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="script"/>' language="javascript" onclick="cws_code()" onmouseover="this.className='cws_BtnMouseOverUp'" onmouseout="this.className='cws_Btn'">
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/code.gif" />	</li>
	<li class="cws_Btn" title='<lt:Label res="res.label.editor_full.editor" key="quote"/>' language="javascript" onclick="cws_quote()" onmouseover="this.className='cws_BtnMouseOverUp'" onmouseout="this.className='cws_Btn'">
	<img class="cws_Ico" src="<%=editorRootPath%>/editor_full/images/quote.gif" />	</li>
	</ul>
	<ul style="height:100%" id="PostiFrame">
    <iframe class="cws_Composition" ID="cws_Composition" MARGINHEIGHT="5" MARGINWIDTH="5" width="100%" height="100%"></iframe> 
	</ul>
	<ul>
	<li style="width:10px"></li>
	<li class="cws_TabOn" id="cws_TabDesign" onclick="if (cws_bTextMode!=1) {cws_setMode(1);}"> 
	<img src="<%=editorRootPath%>/editor_full/images/mode.design.gif" ALIGN="absmiddle" width="20" height="20">&nbsp;<lt:Label res="res.label.editor_full.editor" key="design"/></li>
	<li style="width:10px"></li>
	<li class="cws_TabOff" id="cws_TabView" onclick="cws_View();" > 
	<img unselectable="on" src="<%=editorRootPath%>/editor_full/images/mode.view.gif" ALIGN="absmiddle" width="20" height="20" />&nbsp;<lt:Label res="res.label.editor_full.editor" key="preview"/>	</li>
	<li style="width:10px"></li>
	<li class="cws_TabOff" id="cws_TabHtml" onclick="if (cws_bTextMode!=2) {cws_setMode(2);}" style="cursor: pointer;"><img unselectable="on" src="<%=editorRootPath%>/editor_full/images/mode.html.gif" ALIGN="absmiddle" width=21 height=20 />&nbsp;<lt:Label res="res.label.editor_full.editor" key="source_code"/></li>
	<li style="width:300;text-align:right; display:none">
	<a href="javascript:cws_Size(-560)"><img src="<%=editorRootPath%>/editor_full/images/minus.gif" border="0" /></a> 
	<a href="javascript:cws_Size(320)"><img src="<%=editorRootPath%>/editor_full/images/plus.gif" border="0" /></a></li>
	<li style="width:10px"></li>
	</ul>
</div>
<script language="JavaScript">
var cws_bIsIE5 = isIE(); // document.all;
var canusehtml='1';
var PostType=1;
if (cws_bIsIE5){
	var IframeID=frames["cws_Composition"];
}
else{
	var IframeID=document.getElementById("cws_Composition").contentWindow;
	var cws_bIsNC=true;
}

$(function() {
	if (cws_bLoad==false) {
		cws_InitDocument("Body","GB2312");
	}
});

/*
function submits(){
	var html;
	html =cws_getText();
	html=cws_rCode(html,"<a>　</a>","");
 	document.oblogform.edit.value=html;
}
*/
function initx(){
//IframeID.document.body.innerHTML=document.oblogform.edit.value;
}
function initt(){
//IframeID.document.body.innerHTML="<a>　</a>"+document.oblogform.edit.value;
}

if (0==1) {
	initt();
}
else{
	initx();
}
function part()
{
	cws_InsertSymbol('<lt:Label res="res.label.editor_full.editor" key="part"/>');
}
function pastestr()
{
	var tmpstr=window.clipboardData.getData("Text");
	if (tmpstr!=null)
	{
		if (IframeID.document.body.innerHTML!="") {
			if (confirm('<lt:Label res="res.label.editor_full.editor" key="confirm_coverage"/>') == false)
			return false;
		}
		IframeID.document.body.innerHTML=window.clipboardData.getData("Text");
	}
}

var editorRootPath = "<%=editorRootPath%>";
</script>
<script src="<%=editorRootPath%>/editor_full/editor.js"></script>