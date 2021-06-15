<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE><lt:Label res="res.label.editor_full.swf" key="page_title"/></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<META http-equiv=Expires content=0><LINK href="swf_files/pop.css" type=text/css 
rel=stylesheet>
<SCRIPT language=JavaScript event=onclick for=Ok>
	var s=path.value;
	if (s.length<10)
	{
		alert('<lt:Label res="res.label.editor_full.swf" key="input_url"/>');
	}else{
		window.returnValue = path.value+"*"+selrow.value+"*"+selcol.value
		window.close();
	}
</SCRIPT>

<SCRIPT>
function IsDigit()
{
  return ((event.keyCode >= 48) && (event.keyCode <= 57));
}
</SCRIPT>

<META content="MSHTML 6.00.3790.373" name=GENERATOR></HEAD>
<BODY bgColor=menu>
<TABLE style="PADDING-LEFT: 10px; WIDTH: 98%" cellSpacing=3 cellPadding=0 
align=center border=0>
  <TBODY>
  <TR>
    <TD colSpan=2><FONT class=title><lt:Label res="res.label.editor_full.swf" key="input_flash"/></FONT> 
      <HR width="100%">
      <FONT class=title><lt:Label res="res.label.editor_full.swf" key="url"/> <INPUT id=path size=50 value=http://></TD></TR>
  <TR>
    <TD><lt:Label res="res.label.editor_full.swf" key="width"/> <INPUT onkeypress=event.returnValue=IsDigit(); id=selrow size=7 
      value=480></TD></TR>
  <TR>
    <TD><lt:Label res="res.label.editor_full.swf" key="height"/> <INPUT onkeypress=event.returnValue=IsDigit(); id=selcol size=7 
      value=360></TD></TR>
  <TR>
    <TD align=middle colSpan=2><BUTTON id=Ok type=submit><lt:Label res="res.common" key="ok"/></BUTTON>&nbsp; 
      &nbsp;<BUTTON 
onclick=window.close();><lt:Label res="res.common" key="close"/></BUTTON></TD></TR></TD></TR></TBODY></TABLE></BODY></HTML>
