<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE>多功能编辑器--插入 MediaPlayer 播放器</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8"><LINK 
href="wmv_files/pop.css" type=text/css rel=stylesheet>
<SCRIPT language=JavaScript event=onclick for=Ok>
	var s=path.value
	if (s.length<10 )
	{
		alert('<lt:Label res="res.label.editor_full.wmv" key="input_url"/>');
	}else{
	var autostart
		autostart=document.getElementById("autostart").checked
		window.returnValue = path.value+"*"+ autostart +"*"+width.value+"*"+height.value
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
<TABLE style="PADDING-LEFT: 5px; WIDTH: 98%" cellSpacing=5 cellPadding=0 
align=center border=0>
  <TBODY>
  <TR>
    <TD colSpan=2><FONT class=title><lt:Label res="res.label.editor_full.wmv" key="input_wmv"/></FONT> 
      <HR width="100%"><lt:Label res="res.label.editor_full.wmv" key="ext"/>
       avi, wmv, asf, mov</TD></TR>
  <TR>
    <TD colSpan=2><lt:Label res="res.label.editor_full.wmv" key="url"/> <INPUT id=path size=40 value=http://></TD></TR>
  <TR>
    <TD><lt:Label res="res.label.editor_full.wmv" key="width"/> <INPUT onkeypress=event.returnValue=IsDigit(); id=width size=7 
      value=480></TD></TR>
  <TR>
    <TD><lt:Label res="res.label.editor_full.wmv" key="height"/> <INPUT onkeypress=event.returnValue=IsDigit(); id=height size=7 
      value=360></TD></TR>
  <TR>
    <TD><lt:Label res="res.label.editor_full.wmv" key="mode"/> <INPUT type=radio CHECKED value=1 name=autostart><lt:Label res="res.label.editor_full.wmv" key="auto"/> <INPUT 
      type=radio value=0 name=autostart><lt:Label res="res.label.editor_full.wmv" key="manual"/></TD></TR>
  <TR>
    <TD align=middle colSpan=2><BUTTON id=Ok type=submit><lt:Label res="res.common" key="ok"/></BUTTON>&nbsp; 
      &nbsp;<BUTTON 
onclick=window.close();><lt:Label res="res.common" key="close"/></BUTTON></TD></TR></TBODY></TABLE></BODY></HTML>
