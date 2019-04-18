<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE><lt:Label res="res.label.editor_full.rm" key="page_title"/></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8"><LINK 
href="rm_files/pop.css" type=text/css rel=stylesheet>
<SCRIPT language=JavaScript event=onclick for=Ok>
	var s=path.value;
	if (s.length<10)
	{
		alert("<lt:Label res="res.label.editor_full.rm" key="input_url"/>");
	}else{
		var autostart
		autostart=document.getElementById("autostart").checked
		window.returnValue = path.value+"*"+selrow.value+"*"+selcol.value+"*"+ autostart
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
<TABLE style="PADDING-LEFT: 10px; WIDTH: 98%" cellSpacing=5 cellPadding=0 
align=center border=0>
  <TBODY>
  <TR>
    <TD colSpan=2><FONT class=title><lt:Label res="res.label.editor_full.rm" key="insert_rm_player"/></FONT> 
      <HR width="100%">
      <lt:Label res="res.label.editor_full.rm" key="ext"/> rm, ra, ram</TD></TR>
  <TR>
    <TD colSpan=2><lt:Label res="res.label.editor_full.rm" key="url"/> <INPUT id=path size=40 value=http://></TD></TR>
  <TR>
    <TD><lt:Label res="res.label.editor_full.rm" key="width"/> <INPUT onkeypress=event.returnValue=IsDigit(); id=selrow size=7 
      value=480></TD></TR>
  <TR>
    <TD><lt:Label res="res.label.editor_full.rm" key="height"/> <INPUT onkeypress=event.returnValue=IsDigit(); id=selcol size=7 
      value=360></TD></TR>
  <TR>
    <TD><lt:Label res="res.label.editor_full.rm" key="mode"/> <INPUT type=radio CHECKED value=1 name=autostart><lt:Label res="res.label.editor_full.rm" key="auto"/> <INPUT 
      type=radio value=0 name=autostart><lt:Label res="res.label.editor_full.rm" key="manual"/></TD></TR>
  <TR>
    <TD align=middle colSpan=2><BUTTON id=Ok 
      type=submit><lt:Label res="res.common" key="ok"/></BUTTON>&nbsp;<BUTTON onclick=window.close();><lt:Label res="res.common" key="cancel"/></BUTTON> 
    </TD></TR></TBODY></TABLE>
<SCRIPT>
</SCRIPT>
</BODY></HTML>
