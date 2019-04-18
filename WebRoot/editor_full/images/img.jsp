<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<LINK href="img_files/pop.css" type=text/css rel=stylesheet>
<STYLE type=text/css>
BODY {
	FONT: 9pt "<lt:Label res="res.label.editor_full.img" key="font_songti"/>", Verdana, Arial, Helvetica, sans-serif
}
A {
	FONT: 9pt "<lt:Label res="res.label.editor_full.img" key="font_songti"/>", Verdana, Arial, Helvetica, sans-serif
}
TABLE {
	FONT: 9pt "<lt:Label res="res.label.editor_full.img" key="font_songti"/>", Verdana, Arial, Helvetica, sans-serif
}
DIV {
	FONT: 9pt "<lt:Label res="res.label.editor_full.img" key="font_songti"/>", Verdana, Arial, Helvetica, sans-serif
}
SPAN {
	FONT: 9pt "<lt:Label res="res.label.editor_full.img" key="font_songti"/>", Verdana, Arial, Helvetica, sans-serif
}
TD {
	FONT: 9pt "<lt:Label res="res.label.editor_full.img" key="font_songti"/>", Verdana, Arial, Helvetica, sans-serif
}
TH {
	FONT: 9pt "<lt:Label res="res.label.editor_full.img" key="font_songti"/>", Verdana, Arial, Helvetica, sans-serif
}
INPUT {
	FONT: 9pt "<lt:Label res="res.label.editor_full.img" key="font_songti"/>", Verdana, Arial, Helvetica, sans-serif
}
SELECT {
	FONT: 9pt "<lt:Label res="res.label.editor_full.img" key="font_songti"/>", Verdana, Arial, Helvetica, sans-serif
}
BODY {
	PADDING-RIGHT: 5px; PADDING-LEFT: 5px; PADDING-BOTTOM: 5px; PADDING-TOP: 5px
}
</STYLE>

<SCRIPT language=JavaScript>
var sAction = "INSERT";
var sTitle = "<lt:Label res="res.label.editor_full.img" key="insert"/>";

var oControl;
var oSeletion;
var sRangeType;

var sFromUrl = "http://";
var sAlt = "";
var sBorder = "0";
var sBorderColor = "#000000";
var sFilter = "";
var sAlign = "";
var sWidth = "";
var sHeight = "";
var sVSpace = "";
var sHSpace = "";

var sCheckFlag = "file";

//oSelection = dialogArguments[1];
//sRangeType = dialogArguments[2];

//oSelection = dialogArguments.IframeID.document.selection.createRange();
//sRangeType = dialogArguments.IframeID.document.selection.type;

if (sRangeType == "Control") {
	if (oSelection.item(0).tagName == "IMG"){
		sAction = "MODI";
		sTitle = '<lt:Label res="res.label.editor_full.img" key="modify"/>';
		sCheckFlag = "url";
		oControl = oSelection.item(0);
		sFromUrl = oControl.src;
		sAlt = oControl.alt;
		sBorder = oControl.border;
		sBorderColor = oControl.style.borderColor;
		sFilter = oControl.style.filter;
		sAlign = oControl.align;
		sWidth = oControl.width;
		sHeight = oControl.height;
		sVSpace = oControl.vspace;
		sHSpace = oControl.hspace;
	}
}


document.write('<title><lt:Label res="res.label.editor_full.img" key="page_title"/></title>');


// 初始值
function InitDocument(){
	//SearchSelectValue(d_filter, sFilter);
	//SearchSelectValue(d_align, sAlign.toLowerCase());

	d_fromurl.value = sFromUrl;
	d_alt.value = sAlt;
	d_border.value = sBorder;
	d_bordercolor.value = sBorderColor;
	//s_bordercolor.style.backgroundColor = sBorderColor;
	d_width.value = sWidth;
	d_height.value = sHeight;
	d_vspace.value = sVSpace;
	d_hspace.value = sHSpace;
}


// 图片来源单选点击事件


function makearray(n) {
this.length = n;
for(var i = 1; i <= n; i++)
this[i] = 0;
return this;
}

// 本窗口返回值
function ReturnValue(){
	sFromUrl = d_fromurl.value;
	
	if (sFromUrl=="http://")
		return null;
		
	sAlt = d_alt.value;
	sBorder = d_border.value;
	sBorderColor = d_bordercolor.value;
	sFilter = d_filter.value;
	sAlign = d_align.value;
	sWidth = d_width.value;
	sHeight = d_height.value;
	sVSpace = d_vspace.value;
	sHSpace = d_hspace.value;

	if (sAction == "MODI") {
		oControl.src = sFromUrl;
		oControl.alt = sAlt;
		oControl.border = sBorder;
		oControl.style.borderColor = sBorderColor;
		oControl.style.filter = sFilter;
		oControl.align = sAlign;
		oControl.width = sWidth;
		oControl.height = sHeight;
		oControl.vspace = sVSpace;
		oControl.hspace = sHSpace;
	}else{
		var sHTML = '';
		if (sFilter!=""){
			sHTML=sHTML+'filter:'+sFilter+';';
		}
		if (sBorderColor!=""){
			sHTML=sHTML+'border-color:'+sBorderColor+';';
		}
		if (sHTML!=""){
			sHTML=' style="'+sHTML+'"';
		}
		sHTML = '<img src="'+sFromUrl+'"'+sHTML;
		if (sBorder!=""){
			sHTML=sHTML+' border="'+sBorder+'"';
		}
		if (sAlt!=""){
			sHTML=sHTML+' alt="'+sAlt+'"';
		}
		if (sAlign!=""){
			sHTML=sHTML+' align="'+sAlign+'"';
		}
		if (sWidth!=""){
			sHTML=sHTML+' width="'+sWidth+'"';
		}
		if (sHeight!=""){
			sHTML=sHTML+' height="'+sHeight+'"';
		}
		if (sVSpace!=""){
			sHTML=sHTML+' vspace="'+sVSpace+'"';
		}
		if (sHSpace!=""){
			sHTML=sHTML+' hspace="'+sHSpace+'"';
		}
		sHTML=sHTML+'>';
		//IframeID.document.body.innerHTML=sHTML;
		//dialogArguments.insertHTML(sHTML);
	}
	re = new makearray(2);
	re[1]=sHTML;
	re[2]=d_upfilename.value;
	window.returnValue = re;
	window.close();
}

// 点确定时执行
function ok(){
	// 数字型输入的有效性
	d_border.value = ToInt(d_border.value);
	d_width.value = ToInt(d_width.value);
	d_height.value = ToInt(d_height.value);
	d_vspace.value = ToInt(d_vspace.value);
	d_hspace.value = ToInt(d_hspace.value);
	// 边框颜色的有效性
	
		// 返回值
		ReturnValue();
}

// 使所有输入框无效
function DisableItems(){
	d_checkfromfile.disabled=true;
	d_checkfromurl.disabled=true;
	d_fromurl.disabled=true;
	d_alt.disabled=true;
	d_border.disabled=true;
	d_bordercolor.disabled=true;
	d_filter.disabled=true;
	d_align.disabled=true;
	d_width.disabled=true;
	d_height.disabled=true;
	d_vspace.disabled=true;
	d_hspace.disabled=true;
	Ok.disabled=true;
}

// 使所有输入框有效
function AbleItems(){
	d_checkfromfile.disabled=false;
	d_checkfromurl.disabled=false;
	d_fromurl.disabled=false;
	d_alt.disabled=false;
	d_border.disabled=false;
	d_bordercolor.disabled=false;
	d_filter.disabled=false;
	d_align.disabled=false;
	d_width.disabled=false;
	d_height.disabled=false;
	d_vspace.disabled=false;
	d_hspace.disabled=false;
	Ok.disabled=false;
}

// 转为数字型，并无前导0，不能转则返回""
function ToInt(str){
	str=BaseTrim(str);
	if (str!=""){
		var sTemp=parseFloat(str);
		if (isNaN(sTemp)){
			str="";
		}else{
			str=sTemp;
		}
	}
	return str;
}
// 去空格，left,right,all可选
function BaseTrim(str){
	  lIdx=0;rIdx=str.length;
	  if (BaseTrim.arguments.length==2)
	    act=BaseTrim.arguments[1].toLowerCase()
	  else
	    act="all"
      for(var i=0;i<str.length;i++){
	  	thelStr=str.substring(lIdx,lIdx+1)
		therStr=str.substring(rIdx,rIdx-1)
        if ((act=="all" || act=="left") && thelStr==" "){
			lIdx++
        }
        if ((act=="all" || act=="right") && therStr==" "){
			rIdx--
        }
      }
	  str=str.slice(lIdx,rIdx)
      return str
}
function cws_foreColor()
{
	var cws_bIsIE5=document.all;
	if (cws_bIsIE5){
		var arr = showModalDialog("selcolor.html", "", "dialogWidth:18.5em; dialogHeight:17.5em; status:0; help:0");
		if (arr != null) d_bordercolor.value=arr;

	}
}
// 只允许输入数字
function IsDigit(){
  return ((event.keyCode >= 48) && (event.keyCode <= 57));
}
</SCRIPT>

<META content="MSHTML 6.00.3790.373" name=GENERATOR></HEAD>
<BODY bgColor=menu onload=InitDocument()>
<TABLE cellSpacing=0 cellPadding=0 align=center border=0>
  <TBODY>
  <TR>
    <TD>
      <FIELDSET><LEGEND><lt:Label res="res.label.editor_full.img" key="img_source"/></LEGEND>
      <TABLE cellSpacing=0 cellPadding=0 border=0>
        <TBODY>
        <TR>
          <TD colSpan=9 height=5></TD></TR>
        <TR>
          <TD width=7></TD>
          <TD align=right width=54><lt:Label res="res.label.editor_full.img" key="url"/></TD>
          <TD width=5></TD>
          <TD colSpan=5>
<%
boolean isWebedit = cn.js.fan.util.ParamUtil.get(request, "isWebedit").equals("true");
if (isWebedit) {
%>		  
		  <INPUT type=file id=d_fromurl style="WIDTH: 243px" size=30>
<%}else{%>		  
		  <INPUT id=d_fromurl style="WIDTH: 243px" size=30>
<%}%>
		  </TD>
          <TD width=7></TD></TR>
        <TR>
          <TD colSpan=9 height=5></TD></TR></TBODY></TABLE></FIELDSET> </TD></TR>
  <TR>
    <TD height=5></TD></TR>
  <TR>
    <TD>
      <FIELDSET><LEGEND><lt:Label res="res.label.editor_full.img" key="view"/></LEGEND>
      <TABLE cellSpacing=0 cellPadding=0 border=0>
        <TBODY>
        <TR>
          <TD colSpan=9 height=5></TD></TR>
        <TR>
          <TD width=7></TD>
          <TD><lt:Label res="res.label.editor_full.img" key="desc"/></TD>
          <TD width=5></TD>
          <TD colSpan=5><INPUT id=d_alt style="WIDTH: 243px" size=38></TD>
          <TD width=7></TD></TR>
        <TR>
          <TD colSpan=9 height=5></TD></TR>
        <TR>
          <TD width=7></TD>
          <TD noWrap><lt:Label res="res.label.editor_full.img" key="border"/></TD>
          <TD width=5></TD>
          <TD><INPUT onkeypress=event.returnValue=IsDigit(); id=d_border 
            size=10></TD>
          <TD width=40></TD>
          <TD noWrap><lt:Label res="res.label.editor_full.img" key="border_color"/></TD>
          <TD width=5></TD>
          <TD>
            <TABLE cellSpacing=0 cellPadding=0 border=0>
              <TBODY>
              <TR>
                <TD><INPUT id=d_bordercolor size=7></TD>
                <TD language=javascript onclick=cws_foreColor();><IMG 
                  height=17 src="<%=request.getContextPath()%>/editor_full/images/img_files/Rect.gif" 
            width=18></TD></TR></TBODY></TABLE></TD>
          <TD width=7></TD></TR>
        <TR>
          <TD colSpan=9 height=5></TD></TR>
        <TR>
          <TD width=7></TD>
          <TD><lt:Label res="res.label.editor_full.img" key="special_view"/></TD>
          <TD width=5></TD>
          <TD><SELECT id=d_filter style="WIDTH: 72px" size=1> <OPTION 
              value="" selected><lt:Label res="res.label.editor_full.img" key="none"/></OPTION> <OPTION 
              value=Alpha(Opacity=50)><lt:Label res="res.label.editor_full.img" key="transparent"/></OPTION> <OPTION 
              value="Alpha(Opacity=0, FinishOpacity=100, Style=1, StartX=0, StartY=0, FinishX=100, FinishY=140)"><lt:Label res="res.label.editor_full.img" key="line_tran"/></OPTION> 
              <OPTION 
              value="Alpha(Opacity=10, FinishOpacity=100, Style=2, StartX=30, StartY=30, FinishX=200, FinishY=200)"><lt:Label res="res.label.editor_full.img" key="radio_tran"/></OPTION> 
              <OPTION 
              value=blur(add=1,direction=14,strength=15)><lt:Label res="res.label.editor_full.img" key="blur"/></OPTION><OPTION 
              value=blur(add=true,direction=45,strength=30)><lt:Label res="res.label.editor_full.img" key="blur_blow"/></OPTION> 
              <OPTION 
              value="Wave(Add=0, Freq=60, LightStrength=1, Phase=0, Strength=3)"><lt:Label res="res.label.editor_full.img" key="wave_sin"/></OPTION> 
              <OPTION value=gray><lt:Label res="res.label.editor_full.img" key="black_white"/></OPTION><OPTION 
              value=Chroma(Color=#FFFFFF)><lt:Label res="res.label.editor_full.img" key="tran_white"/></OPTION> <OPTION 
              value="DropShadow(Color=#999999, OffX=7, OffY=4, Positive=1)"><lt:Label res="res.label.editor_full.img" key="drop_shadow"/></OPTION> 
              <OPTION value="Shadow(Color=#999999, Direction=45)"><lt:Label res="res.label.editor_full.img" key="shadow"/></OPTION> 
              <OPTION value="Glow(Color=#ff9900, Strength=5)"><lt:Label res="res.label.editor_full.img" key="glow"/></OPTION> 
              <OPTION value=flipv><lt:Label res="res.label.editor_full.img" key="v_convert"/></OPTION> <OPTION 
              value=fliph><lt:Label res="res.label.editor_full.img" key="h_convert"/></OPTION> <OPTION value=grays><lt:Label res="res.label.editor_full.img" key="lower_color"/></OPTION> 
              <OPTION value=xray><lt:Label res="res.label.editor_full.img" key="x_shine"/></OPTION> <OPTION 
            value=invert><lt:Label res="res.label.editor_full.img" key="film"/></OPTION></SELECT> </TD>
          <TD width=40></TD>
          <TD><lt:Label res="res.label.editor_full.img" key="align"/></TD>
          <TD width=5></TD>
          <TD><SELECT id=d_align style="WIDTH: 72px" size=1> <OPTION 
              value="" selected><lt:Label res="res.label.editor_full.img" key="default"/></OPTION> <OPTION value=left><lt:Label res="res.label.editor_full.img" key="align_left"/></OPTION> 
              <OPTION value=right><lt:Label res="res.label.editor_full.img" key="align_right"/></OPTION> <OPTION value=top><lt:Label res="res.label.editor_full.img" key="align_top"/></OPTION> 
              <OPTION value=middle><lt:Label res="res.label.editor_full.img" key="align_center"/></OPTION> <OPTION value=bottom><lt:Label res="res.label.editor_full.img" key="align_bottom"/></OPTION> 
              <OPTION value=absmiddle><lt:Label res="res.label.editor_full.img" key="align_middle"/></OPTION> <OPTION 
              value=absbottom><lt:Label res="res.label.editor_full.img" key="align_bottom"/></OPTION> <OPTION value=baseline><lt:Label res="res.label.editor_full.img" key="align_base"/></OPTION> 
              <OPTION value=texttop><lt:Label res="res.label.editor_full.img" key="align_text_top"/></OPTION></SELECT> </TD>
          <TD width=7></TD></TR>
        <TR>
          <TD colSpan=9 height=5></TD></TR>
        <TR>
          <TD width=7></TD>
          <TD><lt:Label res="res.label.editor_full.img" key="photo_width"/></TD>
          <TD width=5></TD>
          <TD><INPUT onkeypress=event.returnValue=IsDigit(); id=d_width 
            maxLength=4 size=10></TD>
          <TD width=40></TD>
          <TD><lt:Label res="res.label.editor_full.img" key="photo_height"/></TD>
          <TD width=5></TD>
          <TD><INPUT onkeypress=event.returnValue=IsDigit(); id=d_height 
            maxLength=4 size=10></TD>
          <TD width=7></TD></TR>
        <TR>
          <TD colSpan=9 height=5></TD></TR>
        <TR>
          <TD width=7></TD>
          <TD><lt:Label res="res.label.editor_full.img" key="space_up_down"/></TD>
          <TD width=5></TD>
          <TD><INPUT onkeypress=event.returnValue=IsDigit(); id=d_vspace 
            maxLength=2 size=10></TD>
          <TD width=40></TD>
          <TD><lt:Label res="res.label.editor_full.img" key="space_left_right"/></TD>
          <TD width=5></TD>
          <TD><INPUT onkeypress=event.returnValue=IsDigit(); id=d_hspace 
            maxLength=2 size=10></TD>
          <TD width=7></TD></TR>
        <TR>
          <TD colSpan=9 height=5></TD></TR></TBODY></TABLE></FIELDSET> </TD></TR>
  <TR>
    <TD height=5></TD></TR>
  <TR>
    <TD align=right><INPUT id=d_upfilename type=hidden> <INPUT id=Ok onclick=ok() type=submit value="<lt:Label res="res.label.editor_full.img" key="ok"/>"> &nbsp;&nbsp; <INPUT onclick=window.close(); type=button value="<lt:Label res="res.label.editor_full.img" key="cancel"/>"></TD></TR>
  <TR>
    <TD align=right>
		<%
		if (!isWebedit && privilege.canUserUpload(request, com.redmoon.forum.UserSession.getBoardCode(request))) {
		%>
		<BR><iframe src="<%=request.getContextPath()%>/forum/uploadimg.jsp" width=100% height="48" frameborder="0" scrolling="no"></iframe>
		<%}%></TD>
  </TR>
  </TBODY></TABLE>
</BODY></HTML>
