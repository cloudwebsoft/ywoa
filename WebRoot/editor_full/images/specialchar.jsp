<%@ page contentType="text/html;charset=utf-8" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE><lt:Label res="res.label.editor_full.specialchar" key="page_title"/></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<META http-equiv=Expires content=0><LINK href="specialchar_files/pop.css" 
type=text/css rel=stylesheet>
<STYLE type=text/css>.Disactive {
	BORDER-RIGHT: 1px solid; BORDER-TOP: 1px solid; BORDER-LEFT: 1px solid; WIDTH: 1%; CURSOR: hand; BORDER-BOTTOM: 1px solid; BACKGROUND-COLOR: #dedbd6; TEXT-ALIGN: center
}
.Active {
	CURSOR: hand; BACKGROUND-COLOR: #ffffcc; TEXT-ALIGN: center
}
.MainTable {
	BORDER-RIGHT: #e8e8e8 5px solid; BORDER-TOP: #e8e8e8 5px solid; BORDER-LEFT: #e8e8e8 5px solid; BORDER-BOTTOM: #e8e8e8 5px solid
}
.Sample {
	BORDER-RIGHT: 1px solid; BORDER-TOP: 1px solid; FONT-SIZE: 24px; BORDER-LEFT: 1px solid; BORDER-BOTTOM: 1px solid; BACKGROUND-COLOR: #dedbd6
}
.Empty {
	BORDER-RIGHT: 1px solid; BORDER-TOP: 1px solid; BORDER-LEFT: 1px solid; WIDTH: 1%; CURSOR: default; BORDER-BOTTOM: 1px solid; BACKGROUND-COLOR: #dedbd6
}
</STYLE>

<SCRIPT language=javascript>
<!--
var oSample ;
function insertChar(charValue)
{
	window.returnValue = charValue ;
	window.close();
}

function over(td)
{
	oSample.innerHTML = td.innerHTML ;
	td.className = 'Active' ;
}

function out(td)
{
	oSample.innerHTML = "&nbsp;" ;
	td.className = 'Disactive' ;
}

function CloseWindow()
{
	window.returnValue = null ;
	window.close() ;
}
//-->
</SCRIPT>

<META content="MSHTML 6.00.3790.373" name=GENERATOR></HEAD>
<BODY bottomMargin=0 leftMargin=0 topMargin=0 rightMargin=0>
<TABLE height="100%" cellSpacing=10 cellPadding=0 width="100%">
  <TBODY>
  <TR>
    <TD width="100%" rowSpan=2>
      <TABLE class=MainTable height="100%" cellSpacing=0 cellPadding=0 
      width="100%" align=center border=1>
        <SCRIPT language=javascript>
<!--
//var aChars = ["!","&quot;","#","$","%","&","\\'","(",")","*","+","-",".","/","0","1","2","3","4","5","6","7","8","9",":",";","&lt;","=","&gt;","?","@","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","[","]","^","_","`","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","{","|","}","~","&euro;","?,"?,"?,"?,"?,"?,"\?,"?,"?,"?,"&lsquo;","&rsquo;","&rsquo;","&ldquo;","&rdquo;","?,"&ndash;","&mdash;","?,"?,"?,"?,"?,"?,"&iexcl;","&cent;","&pound;","&pound;","&curren;","&yen;","&brvbar;","&sect;","&uml;","&copy;","&ordf;","&laquo;","&not;","?,"&reg;","&macr;","&deg;","&plusmn;","&sup2;","&sup3;","&acute;","&micro;","&para;","&middot;","&cedil;","&sup1;","&ordm;","&raquo;","&frac14;","&frac12;","&frac34;","&iquest;","&Agrave;","&Aacute;","&Acirc;","&Atilde;","&Auml;","&Aring;","&AElig;","&Ccedil;","&Egrave;","&Eacute;","&Ecirc;","&Euml;","&Igrave;","&Iacute;","&Icirc;","&Iuml;","&ETH;","&Ntilde;","&Ograve;","&Oacute;","&Ocirc;","&Otilde;","&Ouml;","&times;","&Oslash;","&Ugrave;","&Uacute;","&Ucirc;","&Uuml;","&Yacute;","&THORN;","&szlig;","&agrave;","&aacute;","&acirc;","&atilde;","&auml;","&aring;","&aelig;","&ccedil;","&egrave;","&eacute;","&ecirc;","&euml;","&igrave;","&iacute;","&icirc;","&iuml;","&eth;","&ntilde;","&ograve;","&oacute;","&ocirc;","&otilde;","&ouml;","&divide;","&oslash;","&ugrave;","&uacute;","&ucirc;","&uuml;","&uuml;","&yacute;","&thorn;","&yuml;"] ;
var aChars = ["!","&quot;","#","$","%","&","\\'","(",")","*","+","-",".","/","0","1","2","3","4","5","6","7","8","9",":",";","&lt;","=","&gt;","?","@","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","[","]","^","_","`","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","{","|","}","~","&euro;","&lsquo;","&rsquo;","&rsquo;","&ldquo;","&rdquo;","&ndash;","&mdash;","&iexcl;","&cent;","&pound;","&curren;","&yen;","&brvbar;","&sect;","&uml;","&copy;","&ordf;","&laquo;","&not;","&reg;","&macr;","&deg;","&plusmn;","&sup2;","&sup3;","&acute;","&micro;","&para;","&middot;","&cedil;","&sup1;","&ordm;","&raquo;","&frac14;","&frac12;","&frac34;","&iquest;","&Agrave;","&Aacute;","&Acirc;","&Atilde;","&Auml;","&Aring;","&AElig;","&Ccedil;","&Egrave;","&Eacute;","&Ecirc;","&Euml;","&Igrave;","&Iacute;","&Icirc;","&Iuml;","&ETH;","&Ntilde;","&Ograve;","&Oacute;","&Ocirc;","&Otilde;","&Ouml;","&times;","&Oslash;","&Ugrave;","&Uacute;","&Ucirc;","&Uuml;","&Yacute;","&THORN;","&szlig;","&agrave;","&aacute;","&acirc;","&atilde;","&auml;","&aring;","&aelig;","&ccedil;","&egrave;","&eacute;","&ecirc;","&euml;","&igrave;","&iacute;","&icirc;","&iuml;","&eth;","&ntilde;","&ograve;","&oacute;","&ocirc;","&otilde;","&ouml;","&divide;","&oslash;","&ugrave;","&uacute;","&ucirc;","&uuml;","&uuml;","&yacute;","&thorn;","&yuml;"] ;

var cols = 20 ;

var i = 0 ;
while (i < aChars.length)
{
	document.write("<TR>") ;
	for(var j = 0 ; j < cols ; j++) 
	{
		if (aChars[i])
		{
			document.write('<TD class="Disactive" onclick="insertChar(\'' + aChars[i].replace(/&/g, "&amp;") + '\')" onmouseover="over(this)" onmouseout="out(this)">') ;
			document.write(aChars[i]) ;
		}
		else
			document.write("<TD class='Empty'>&nbsp;") ;
		document.write("</TD>") ;
		i++ ;
	}
	document.write("</TR>") ;
}
//-->
</SCRIPT>

        <TBODY></TBODY></TABLE></TD>
    <TD vAlign=top>
      <TABLE class=MainTable>
        <TBODY>
        <TR>
          <TD class=Sample id=SampleTD align=middle width=40 
          height=40>&nbsp;</TD></TR></TBODY></TABLE></TD></TR>
  <TR>
    <TD align=middle height=1><BUTTON 
  onclick=window.close();><lt:Label res="res.common" key="close"/></BUTTON></TD></TR></TBODY></TABLE>
<SCRIPT language=javascript>
<!--
oSample = document.getElementById("SampleTD") ;
//-->
</SCRIPT>
</BODY></HTML>
