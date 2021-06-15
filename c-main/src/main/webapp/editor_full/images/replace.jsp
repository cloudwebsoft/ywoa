<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE><lt:Label res="res.label.editor_full.replace" key="page_title"/></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8"><LINK 
href="replace_files/pop.css" type=text/css rel=stylesheet>
<SCRIPT language=JavaScript event=onclick for=Ok>
  window.returnValue = a.value+"*"+b.value+"*"+(c.checked?"1":"0")+"*";
  window.close();
</SCRIPT>

<SCRIPT>
function IsDigit()
{
  return ((event.keyCode >= 48) && (event.keyCode <= 57));
}
</SCRIPT>

<META content="MSHTML 6.00.3790.373" name=GENERATOR></HEAD>
<BODY bgColor=menu>
<TABLE style="PADDING-LEFT: 10px" cellSpacing=10 cellPadding=0 align=center 
border=0>
  <TBODY>
  <TR>
    <TD align=middle colSpan=2><FONT class=title><lt:Label res="res.label.editor_full.replace" key="find_replace"/></FONT> 
      <HR width="100%">
      <lt:Label res="res.label.editor_full.replace" key="find"/> <INPUT id=a size=15> </TD></TR>
  <TR>
    <TD><lt:Label res="res.label.editor_full.replace" key="replace"/><INPUT id=b size=15> </TD></TR>
  <TR>
    <TD colSpan=2><INPUT id=c type=checkbox><lt:Label res="res.label.editor_full.replace" key="case"/></TD></TR>
  <TR>
    <TD align=middle colSpan=2><BUTTON id=Ok type=submit><lt:Label res="res.label.editor_full.replace" key="begin"/></BUTTON>&nbsp; 
      &nbsp;<BUTTON onclick=window.close();><lt:Label res="res.label.editor_full.replace" key="close"/></BUTTON> 
</TD></TR></TBODY></TABLE></BODY></HTML>
