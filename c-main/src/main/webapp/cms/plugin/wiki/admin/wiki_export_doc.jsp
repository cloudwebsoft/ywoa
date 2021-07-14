<%@ page contentType="text/html; charset=gb2312"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.util.file.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.fileark.*"%>
<%@ page import = "cn.js.fan.module.cms.plugin.wiki.*"%>
<%@ page import = "java.io.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	Directory dir = new Directory();
	String userName = ParamUtil.get(request,"userName");
	
	String dirCode = ParamUtil.get(request, "dirCode");
	Leaf showLeaf = dir.getLeaf(dirCode);
	
	response.reset();

	response.setContentType("application/msword;charset=gb2312");
	response.setHeader("Content-disposition","attachment; filename=wiki.doc");

	// response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(showLeaf.getName()) + ".doc");

%>
<html xmlns:v="urn:schemas-microsoft-com:vml"
xmlns:o="urn:schemas-microsoft-com:office:office"
xmlns:w="urn:schemas-microsoft-com:office:word"
xmlns="http://www.w3.org/TR/REC-html40">

<head>
<meta http-equiv=Content-Type content="text/html; charset=gb2312">
<meta name=ProgId content=Word.Document>
<meta name=Generator content="Microsoft Word 11">
<meta name=Originator content="Microsoft Word 11">
<link rel=File-List href="a.files/filelist.xml">
<link rel=Edit-Time-Data href="a.files/editdata.mso">
<link rel=OLE-Object-Data href="a.files/oledata.mso">
<!--[if !mso]>
<style>
v\:* {behavior:url(#default#VML);}
o\:* {behavior:url(#default#VML);}
w\:* {behavior:url(#default#VML);}
.shape {behavior:url(#default#VML);}
</style>
<![endif]-->
<title><%=showLeaf.getName()%></title>
<!--[if gte mso 9]><xml>
 <o:DocumentProperties>
  <o:Author>Blue Wind</o:Author>
  <o:LastAuthor>Blue Wind</o:LastAuthor>
  <o:Revision>5</o:Revision>
  <o:TotalTime>1</o:TotalTime>
  <o:Created>2011-02-11T11:52:00Z</o:Created>
  <o:LastSaved>2011-02-11T12:19:00Z</o:LastSaved>
  <o:Pages>1</o:Pages>
  <o:Words>499</o:Words>
  <o:Characters>2847</o:Characters>
  <o:Company>Microsoft</o:Company>
  <o:Lines>23</o:Lines>
  <o:Paragraphs>6</o:Paragraphs>
  <o:CharactersWithSpaces>3340</o:CharactersWithSpaces>
  <o:Version>11.6568</o:Version>
 </o:DocumentProperties>
</xml><![endif]--><!--[if gte mso 9]><xml>
 <w:WordDocument>
  <w:View>Print</w:View>
  <w:Zoom>90</w:Zoom>
  <w:SpellingState>Clean</w:SpellingState>
  <w:GrammarState>Clean</w:GrammarState>
  <w:PunctuationKerning/>
  <w:DrawingGridVerticalSpacing>7.8 磅</w:DrawingGridVerticalSpacing>
  <w:DisplayHorizontalDrawingGridEvery>0</w:DisplayHorizontalDrawingGridEvery>
  <w:DisplayVerticalDrawingGridEvery>2</w:DisplayVerticalDrawingGridEvery>
  <w:ValidateAgainstSchemas/>
  <w:SaveIfXMLInvalid>false</w:SaveIfXMLInvalid>
  <w:IgnoreMixedContent>false</w:IgnoreMixedContent>
  <w:AlwaysShowPlaceholderText>false</w:AlwaysShowPlaceholderText>
  <w:Compatibility>
   <w:SpaceForUL/>
   <w:BalanceSingleByteDoubleByteWidth/>
   <w:DoNotLeaveBackslashAlone/>
   <w:ULTrailSpace/>
   <w:DoNotExpandShiftReturn/>
   <w:AdjustLineHeightInTable/>
   <w:BreakWrappedTables/>
   <w:SnapToGridInCell/>
   <w:WrapTextWithPunct/>
   <w:UseAsianBreakRules/>
   <w:DontGrowAutofit/>
   <w:UseFELayout/>
  </w:Compatibility>
  <w:BrowserLevel>MicrosoftInternetExplorer4</w:BrowserLevel>
 </w:WordDocument>
</xml><![endif]--><!--[if gte mso 9]><xml>
 <w:LatentStyles DefLockedState="false" LatentStyleCount="156">
  <w:LsdException Locked="false" Name="Default Paragraph Font"/>
 </w:LatentStyles>
</xml><![endif]-->
<style>
<!--
 /* Font Definitions */
 @font-face
	{font-family:宋体;
	panose-1:2 1 6 0 3 1 1 1 1 1;
	mso-font-alt:SimSun;
	mso-font-charset:134;
	mso-generic-font-family:auto;
	mso-font-pitch:variable;
	mso-font-signature:3 135135232 16 0 262145 0;}
@font-face
	{font-family:PMingLiU;
	panose-1:2 2 3 0 0 0 0 0 0 0;
	mso-font-alt:新細明體;
	mso-font-charset:136;
	mso-generic-font-family:roman;
	mso-font-pitch:variable;
	mso-font-signature:3 137232384 22 0 1048577 0;}
@font-face
	{font-family:黑体;
	panose-1:2 1 6 0 3 1 1 1 1 1;
	mso-font-alt:SimHei;
	mso-font-charset:134;
	mso-generic-font-family:auto;
	mso-font-pitch:variable;
	mso-font-signature:1 135135232 16 0 262144 0;}
@font-face
	{font-family:仿宋_GB2312;
	panose-1:2 1 6 9 3 1 1 1 1 1;
	mso-font-charset:134;
	mso-generic-font-family:modern;
	mso-font-pitch:fixed;
	mso-font-signature:1 135135232 16 0 262144 0;}
@font-face
	{font-family:方正姚体;
	panose-1:2 1 6 1 3 1 1 1 1 1;
	mso-font-charset:134;
	mso-generic-font-family:auto;
	mso-font-pitch:variable;
	mso-font-signature:3 135135232 16 0 262144 0;}
@font-face
	{font-family:华文细黑;
	panose-1:2 1 6 0 4 1 1 1 1 1;
	mso-font-charset:134;
	mso-generic-font-family:auto;
	mso-font-pitch:variable;
	mso-font-signature:647 135200768 16 0 262303 0;}
@font-face
	{font-family:华文中宋;
	panose-1:2 1 6 0 4 1 1 1 1 1;
	mso-font-charset:134;
	mso-generic-font-family:auto;
	mso-font-pitch:variable;
	mso-font-signature:647 135200768 16 0 262303 0;}
@font-face
	{font-family:\02CE\0325;
	panose-1:0 0 0 0 0 0 0 0 0 0;
	mso-font-alt:"Times New Roman";
	mso-font-charset:0;
	mso-generic-font-family:roman;
	mso-font-format:other;
	mso-font-pitch:auto;
	mso-font-signature:0 0 0 0 0 0;}
@font-face
	{font-family:Tahoma;
	panose-1:2 11 6 4 3 5 4 4 2 4;
	mso-font-charset:0;
	mso-generic-font-family:swiss;
	mso-font-pitch:variable;
	mso-font-signature:1627421319 -2147483648 8 0 66047 0;}
@font-face
	{font-family:"\@宋体";
	panose-1:2 1 6 0 3 1 1 1 1 1;
	mso-font-charset:134;
	mso-generic-font-family:auto;
	mso-font-pitch:variable;
	mso-font-signature:3 135135232 16 0 262145 0;}
@font-face
	{font-family:"\@黑体";
	panose-1:2 1 6 0 3 1 1 1 1 1;
	mso-font-charset:134;
	mso-generic-font-family:auto;
	mso-font-pitch:variable;
	mso-font-signature:1 135135232 16 0 262144 0;}
@font-face
	{font-family:"\@仿宋_GB2312";
	panose-1:2 1 6 9 3 1 1 1 1 1;
	mso-font-charset:134;
	mso-generic-font-family:modern;
	mso-font-pitch:fixed;
	mso-font-signature:1 135135232 16 0 262144 0;}
@font-face
	{font-family:"\@PMingLiU";
	panose-1:2 2 3 0 0 0 0 0 0 0;
	mso-font-charset:136;
	mso-generic-font-family:roman;
	mso-font-pitch:variable;
	mso-font-signature:3 137232384 22 0 1048577 0;}
@font-face
	{font-family:"\@方正姚体";
	panose-1:2 1 6 1 3 1 1 1 1 1;
	mso-font-charset:134;
	mso-generic-font-family:auto;
	mso-font-pitch:variable;
	mso-font-signature:3 135135232 16 0 262144 0;}
@font-face
	{font-family:"\@华文细黑";
	panose-1:2 1 6 0 4 1 1 1 1 1;
	mso-font-charset:134;
	mso-generic-font-family:auto;
	mso-font-pitch:variable;
	mso-font-signature:647 135200768 16 0 262303 0;}
@font-face
	{font-family:"\@华文中宋";
	panose-1:2 1 6 0 4 1 1 1 1 1;
	mso-font-charset:134;
	mso-generic-font-family:auto;
	mso-font-pitch:variable;
	mso-font-signature:647 135200768 16 0 262303 0;}
 /* Style Definitions */
 p.MsoNormal, li.MsoNormal, div.MsoNormal
	{mso-style-parent:"";
	margin:0cm;
	margin-bottom:.0001pt;
	text-align:justify;
	text-justify:inter-ideograph;
	mso-pagination:none;
	font-size:10.5pt;
	mso-bidi-font-size:12.0pt;
	font-family:"Times New Roman";
	mso-fareast-font-family:宋体;
	mso-font-kerning:1.0pt;}
h1
	{mso-style-name:"标题 1\,YCL标题 1\,Heading 0\,H1\,H11\,H12\,H111\,H13\,H112\,h1\,Datasheet title\,Section Head\,1st level\,l1\,1\,H14\,H15\,H16\,H17\,L1 Heading 1\,h11\,1st level1\,heading 11\,h12\,1st level2\,heading 12\,h111\,1st level11\,heading 111\,h13\,1st level3\,heading 13\,h112\,1st level12\,h121";
	mso-style-next:正文;
	margin-top:17.0pt;
	margin-right:0cm;
	margin-bottom:16.5pt;
	margin-left:111.6pt;
	text-align:justify;
	text-justify:inter-ideograph;
	text-indent:-21.6pt;
	line-height:240%;
	mso-pagination:lines-together;
	page-break-after:avoid;
	mso-outline-level:1;
	mso-list:l4 level1 lfo2;
	tab-stops:list 180.0pt;
	background:#D9D9D9;
	font-size:24.0pt;
	mso-bidi-font-size:12.0pt;
	font-family:"Times New Roman";
	mso-fareast-font-family:黑体;
	mso-font-kerning:1.0pt;
	font-weight:normal;}
h2
	{mso-style-name:"标题 2\,YCL标题 2\,proj2\,proj21\,proj22\,proj23\,proj24\,proj25\,proj26\,proj27\,proj28\,proj29\,proj210\,proj211\,proj212\,proj221\,proj231\,proj241\,proj251\,proj261\,proj271\,proj281\,proj291\,proj2101\,proj2111\,proj213\,proj222\,proj232\,proj242\,proj252\,proj262\,proj272\,proj282\,2";
	mso-style-next:正文;
	margin-top:13.0pt;
	margin-right:0cm;
	margin-bottom:13.0pt;
	margin-left:0cm;
	text-align:justify;
	text-justify:inter-ideograph;
	text-indent:0cm;
	line-height:150%;
	mso-pagination:lines-together;
	page-break-after:avoid;
	mso-outline-level:2;
	mso-list:l4 level2 lfo2;
	border:none;
	mso-border-bottom-alt:three-d-emboss windowtext 6.0pt;
	padding:0cm;
	mso-padding-alt:0cm 0cm 1.0pt 0cm;
	font-size:16.0pt;
	mso-bidi-font-size:12.0pt;
	font-family:黑体;
	mso-hansi-font-family:宋体;
	mso-bidi-font-family:宋体;
	mso-font-kerning:1.0pt;
	font-weight:normal;
	mso-no-proof:yes;}
h3
	{mso-style-name:"标题 3\,YCL标题 3\,H3\,l3\,CT\,Bold Head\,bh\,sect1\.2\.3\,sect1\.2\.31\,sect1\.2\.32\,sect1\.2\.311\,sect1\.2\.33\,sect1\.2\.312\,Heading 3 - old\,Level 3 Head\,h3\,level_3\,PIM 3\,3rd level\,3\,BOD 0\,heading 3TOC\,1\.1\.1 Heading 3\,1\.1\.1\,heading 3 + Indent\: Left 0\.25 in\,一\,Heading 3 hidden";
	mso-style-next:正文;
	margin-top:13.0pt;
	margin-right:0cm;
	margin-bottom:13.0pt;
	margin-left:0cm;
	text-align:justify;
	text-justify:inter-ideograph;
	text-indent:0cm;
	line-height:150%;
	mso-pagination:lines-together;
	page-break-after:avoid;
	mso-outline-level:3;
	mso-list:l4 level3 lfo2;
	border:none;
	mso-border-bottom-alt:solid windowtext .5pt;
	padding:0cm;
	mso-padding-alt:0cm 0cm 1.0pt 0cm;
	font-size:14.0pt;
	mso-bidi-font-size:12.0pt;
	font-family:"Times New Roman";
	mso-font-kerning:1.0pt;
	font-weight:normal;}
p.MsoHeading7, li.MsoHeading7, div.MsoHeading7
	{mso-style-name:"标题 7\,表头";
	mso-style-next:正文;
	margin-top:12.0pt;
	margin-right:0cm;
	margin-bottom:3.2pt;
	margin-left:64.8pt;
	text-align:justify;
	text-justify:inter-ideograph;
	text-indent:-64.8pt;
	line-height:132%;
	mso-pagination:lines-together;
	page-break-after:avoid;
	mso-outline-level:7;
	mso-list:l4 level7 lfo2;
	tab-stops:list 64.8pt;
	font-size:12.0pt;
	font-family:宋体;
	mso-bidi-font-family:"Times New Roman";
	mso-font-kerning:1.0pt;
	font-weight:bold;}
p.MsoHeading8, li.MsoHeading8, div.MsoHeading8
	{mso-style-next:正文;
	margin-top:12.0pt;
	margin-right:0cm;
	margin-bottom:3.2pt;
	margin-left:72.0pt;
	text-align:justify;
	text-justify:inter-ideograph;
	text-indent:-72.0pt;
	line-height:132%;
	mso-pagination:lines-together;
	page-break-after:avoid;
	mso-outline-level:8;
	mso-list:l4 level8 lfo2;
	tab-stops:list 72.0pt;
	font-size:12.0pt;
	font-family:Arial;
	mso-fareast-font-family:黑体;
	mso-bidi-font-family:"Times New Roman";
	mso-font-kerning:1.0pt;}
p.MsoHeading9, li.MsoHeading9, div.MsoHeading9
	{mso-style-next:正文;
	margin-top:12.0pt;
	margin-right:0cm;
	margin-bottom:3.2pt;
	margin-left:79.2pt;
	text-align:justify;
	text-justify:inter-ideograph;
	text-indent:-79.2pt;
	line-height:132%;
	mso-pagination:lines-together;
	page-break-after:avoid;
	mso-outline-level:9;
	mso-list:l4 level9 lfo2;
	tab-stops:list 79.2pt;
	font-size:10.5pt;
	font-family:Arial;
	mso-fareast-font-family:黑体;
	mso-bidi-font-family:"Times New Roman";
	mso-font-kerning:1.0pt;}
p.MsoToc1, li.MsoToc1, div.MsoToc1
	{mso-style-update:auto;
	mso-style-noshow:yes;
	mso-style-next:正文;
	margin-top:6.0pt;
	margin-right:0cm;
	margin-bottom:6.0pt;
	margin-left:0cm;
	mso-pagination:none;
	font-size:10.0pt;
	font-family:"Times New Roman";
	mso-fareast-font-family:宋体;
	text-transform:uppercase;
	mso-font-kerning:1.0pt;
	font-weight:bold;}
p.MsoToc2, li.MsoToc2, div.MsoToc2
	{mso-style-update:auto;
	mso-style-noshow:yes;
	mso-style-next:正文;
	margin-top:0cm;
	margin-right:0cm;
	margin-bottom:0cm;
	margin-left:10.5pt;
	margin-bottom:.0001pt;
	mso-pagination:none;
	font-size:10.0pt;
	font-family:"Times New Roman";
	mso-fareast-font-family:宋体;
	font-variant:small-caps;
	mso-font-kerning:1.0pt;}
p.MsoToc3, li.MsoToc3, div.MsoToc3
	{mso-style-update:auto;
	mso-style-noshow:yes;
	mso-style-next:正文;
	margin-top:0cm;
	margin-right:0cm;
	margin-bottom:0cm;
	margin-left:21.0pt;
	margin-bottom:.0001pt;
	mso-pagination:none;
	font-size:10.0pt;
	font-family:"Times New Roman";
	mso-fareast-font-family:宋体;
	mso-font-kerning:1.0pt;
	font-style:italic;}
a:link, span.MsoHyperlink
	{color:blue;
	text-decoration:underline;
	text-underline:single;}
a:visited, span.MsoHyperlinkFollowed
	{color:purple;
	text-decoration:underline;
	text-underline:single;}
p.CharCharCharChar, li.CharCharCharChar, div.CharCharCharChar
	{mso-style-name:"Char Char Char Char";
	mso-style-link:默认段落字体;
	margin:0cm;
	margin-bottom:.0001pt;
	text-align:justify;
	text-justify:inter-ideograph;
	mso-pagination:none;
	font-size:12.0pt;
	mso-bidi-font-size:10.0pt;
	font-family:Tahoma;
	mso-fareast-font-family:宋体;
	mso-bidi-font-family:"Times New Roman";
	mso-font-kerning:1.0pt;}
span.GramE
	{mso-style-name:"";
	mso-gram-e:yes;}
 /* Page Definitions */
 @page
	{mso-page-border-surround-header:no;
	mso-page-border-surround-footer:no;}
@page Section1
	{size:595.3pt 841.9pt;
	margin:72.0pt 90.0pt 72.0pt 90.0pt;
	mso-header-margin:42.55pt;
	mso-footer-margin:49.6pt;
	mso-paper-source:0;
	layout-grid:15.6pt;}
div.Section1
	{page:Section1;}
@page Section2
	{size:595.3pt 841.9pt;
	margin:72.0pt 90.0pt 72.0pt 90.0pt;
	mso-header-margin:42.55pt;
	mso-footer-margin:49.6pt;
	mso-paper-source:0;
	layout-grid:15.6pt;}
div.Section2
	{page:Section2;}
@page Section3
	{size:595.3pt 841.9pt;
	margin:72.0pt 90.0pt 72.0pt 90.0pt;
	mso-header-margin:42.55pt;
	mso-footer-margin:49.6pt;
	mso-paper-source:0;
	layout-grid:15.6pt;}
div.Section3
	{page:Section3;}
 /* List Definitions */
 @list l0
	{mso-list-id:201286512;
	mso-list-template-ids:1353236202;}
@list l0:level1
	{mso-level-start-at:2;
	mso-level-tab-stop:21.25pt;
	mso-level-number-position:left;
	margin-left:21.25pt;
	text-indent:-21.25pt;}
@list l0:level2
	{mso-level-text:"%1\.%2";
	mso-level-tab-stop:1.0cm;
	mso-level-number-position:left;
	margin-left:1.0cm;
	text-indent:-1.0cm;}
@list l0:level3
	{mso-level-text:"%1\.%2\.%3\.";
	mso-level-tab-stop:35.45pt;
	mso-level-number-position:left;
	margin-left:35.45pt;
	text-indent:-35.45pt;}
@list l0:level4
	{mso-level-text:"%1\.%2\.%3\.%4\.";
	mso-level-tab-stop:42.55pt;
	mso-level-number-position:left;
	margin-left:42.55pt;
	text-indent:-42.55pt;}
@list l0:level5
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.";
	mso-level-tab-stop:49.6pt;
	mso-level-number-position:left;
	margin-left:49.6pt;
	text-indent:-49.6pt;}
@list l0:level6
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.";
	mso-level-tab-stop:2.0cm;
	mso-level-number-position:left;
	margin-left:2.0cm;
	text-indent:-2.0cm;}
@list l0:level7
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.";
	mso-level-tab-stop:63.8pt;
	mso-level-number-position:left;
	margin-left:63.8pt;
	text-indent:-63.8pt;}
@list l0:level8
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.%8\.";
	mso-level-tab-stop:70.9pt;
	mso-level-number-position:left;
	margin-left:70.9pt;
	text-indent:-70.9pt;}
@list l0:level9
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.%8\.%9\.";
	mso-level-tab-stop:77.95pt;
	mso-level-number-position:left;
	margin-left:77.95pt;
	text-indent:-77.95pt;}
@list l1
	{mso-list-id:354816274;
	mso-list-template-ids:-1212404408;}
@list l1:level1
	{mso-level-start-at:2;
	mso-level-tab-stop:21.25pt;
	mso-level-number-position:left;
	margin-left:21.25pt;
	text-indent:-21.25pt;}
@list l1:level2
	{mso-level-text:"%1\.%2";
	mso-level-tab-stop:1.0cm;
	mso-level-number-position:left;
	margin-left:1.0cm;
	text-indent:-1.0cm;}
@list l1:level3
	{mso-level-text:"%1\.%2\.%3";
	mso-level-tab-stop:35.45pt;
	mso-level-number-position:left;
	margin-left:35.45pt;
	text-indent:-35.45pt;}
@list l1:level4
	{mso-level-text:"%1\.%2\.%3\.%4\.";
	mso-level-tab-stop:42.55pt;
	mso-level-number-position:left;
	margin-left:42.55pt;
	text-indent:-42.55pt;}
@list l1:level5
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.";
	mso-level-tab-stop:49.6pt;
	mso-level-number-position:left;
	margin-left:49.6pt;
	text-indent:-49.6pt;}
@list l1:level6
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.";
	mso-level-tab-stop:2.0cm;
	mso-level-number-position:left;
	margin-left:2.0cm;
	text-indent:-2.0cm;}
@list l1:level7
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.";
	mso-level-tab-stop:63.8pt;
	mso-level-number-position:left;
	margin-left:63.8pt;
	text-indent:-63.8pt;}
@list l1:level8
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.%8\.";
	mso-level-tab-stop:70.9pt;
	mso-level-number-position:left;
	margin-left:70.9pt;
	text-indent:-70.9pt;}
@list l1:level9
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.%8\.%9\.";
	mso-level-tab-stop:77.95pt;
	mso-level-number-position:left;
	margin-left:77.95pt;

	text-indent:-77.95pt;}
@list l2
	{mso-list-id:468135289;
	mso-list-template-ids:-1712709202;}
@list l2:level1
	{mso-level-tab-stop:21.25pt;
	mso-level-number-position:left;
	margin-left:21.25pt;
	text-indent:-21.25pt;}
@list l2:level2
	{mso-level-start-at:2;
	mso-level-text:"%1\.%2";
	mso-level-tab-stop:1.0cm;
	mso-level-number-position:left;
	margin-left:1.0cm;
	text-indent:-1.0cm;}
@list l2:level3
	{mso-level-text:"%1\.%2\.%3";
	mso-level-tab-stop:35.45pt;
	mso-level-number-position:left;
	margin-left:35.45pt;
	text-indent:-35.45pt;}
@list l2:level4
	{mso-level-text:"%1\.%2\.%3\.%4\.";
	mso-level-tab-stop:42.55pt;
	mso-level-number-position:left;
	margin-left:42.55pt;
	text-indent:-42.55pt;}
@list l2:level5
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.";
	mso-level-tab-stop:49.6pt;
	mso-level-number-position:left;
	margin-left:49.6pt;
	text-indent:-49.6pt;}
@list l2:level6
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.";
	mso-level-tab-stop:2.0cm;
	mso-level-number-position:left;
	margin-left:2.0cm;
	text-indent:-2.0cm;}
@list l2:level7
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.";
	mso-level-tab-stop:63.8pt;
	mso-level-number-position:left;
	margin-left:63.8pt;
	text-indent:-63.8pt;}
@list l2:level8
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.%8\.";
	mso-level-tab-stop:70.9pt;
	mso-level-number-position:left;
	margin-left:70.9pt;
	text-indent:-70.9pt;}
@list l2:level9
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.%8\.%9\.";
	mso-level-tab-stop:77.95pt;
	mso-level-number-position:left;
	margin-left:77.95pt;
	text-indent:-77.95pt;}
@list l3
	{mso-list-id:1137605167;
	mso-list-template-ids:-224368436;}
@list l3:level1
	{mso-level-tab-stop:21.25pt;
	mso-level-number-position:left;
	margin-left:21.25pt;
	text-indent:-21.25pt;}
@list l3:level2
	{mso-level-text:"%1\.%2";
	mso-level-tab-stop:1.0cm;
	mso-level-number-position:left;
	margin-left:1.0cm;
	text-indent:-1.0cm;}
@list l3:level3
	{mso-level-text:"%1\.%2\.%3\.";
	mso-level-tab-stop:35.45pt;
	mso-level-number-position:left;
	margin-left:35.45pt;
	text-indent:-35.45pt;}
@list l3:level4
	{mso-level-text:"%1\.%2\.%3\.%4\.";
	mso-level-tab-stop:42.55pt;
	mso-level-number-position:left;
	margin-left:42.55pt;
	text-indent:-42.55pt;}
@list l3:level5
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.";
	mso-level-tab-stop:49.6pt;
	mso-level-number-position:left;
	margin-left:49.6pt;
	text-indent:-49.6pt;}
@list l3:level6
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.";
	mso-level-tab-stop:2.0cm;
	mso-level-number-position:left;
	margin-left:2.0cm;
	text-indent:-2.0cm;}
@list l3:level7
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.";
	mso-level-tab-stop:63.8pt;
	mso-level-number-position:left;
	margin-left:63.8pt;
	text-indent:-63.8pt;}
@list l3:level8
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.%8\.";
	mso-level-tab-stop:70.9pt;
	mso-level-number-position:left;
	margin-left:70.9pt;
	text-indent:-70.9pt;}
@list l3:level9
	{mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.%8\.%9\.";
	mso-level-tab-stop:77.95pt;
	mso-level-number-position:left;
	margin-left:77.95pt;
	text-indent:-77.95pt;}
@list l4
	{mso-list-id:1237322533;
	mso-list-template-ids:-858720634;}
@list l4:level1
	{mso-level-number-format:chinese-counting-thousand;
	mso-level-style-link:"标题 1";
	mso-level-text:"第%1章 ";
	mso-level-tab-stop:180.0pt;
	mso-level-number-position:left;
	margin-left:111.6pt;
	text-indent:-21.6pt;}
@list l4:level2
	{mso-level-style-link:"标题 2";
	mso-level-suffix:space;
	mso-level-tab-stop:none;
	mso-level-number-position:left;
	margin-left:0cm;
	text-indent:0cm;}
@list l4:level3
	{mso-level-style-link:"标题 3";
	mso-level-suffix:space;
	mso-level-text:"%2\.%3";
	mso-level-tab-stop:none;
	mso-level-number-position:left;
	margin-left:0cm;
	text-indent:0cm;}
@list l4:level4
	{mso-level-suffix:space;
	mso-level-text:%4）;
	mso-level-tab-stop:none;
	mso-level-number-position:left;
	margin-left:43.2pt;
	text-indent:-43.2pt;}
@list l4:level5
	{mso-level-text:"%5\)";
	mso-level-tab-stop:21.0pt;
	mso-level-number-position:left;
	margin-left:21.0pt;
	text-indent:-21.0pt;}
@list l4:level6
	{mso-level-number-format:alpha-lower;
	mso-level-suffix:space;
	mso-level-text:%6）;
	mso-level-tab-stop:none;
	mso-level-number-position:left;
	margin-left:57.6pt;
	text-indent:-57.6pt;}
@list l4:level7
	{mso-level-style-link:"标题 7";
	mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7";
	mso-level-tab-stop:64.8pt;
	mso-level-number-position:left;
	margin-left:64.8pt;
	text-indent:-64.8pt;}
@list l4:level8
	{mso-level-style-link:"标题 8";
	mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.%8";
	mso-level-tab-stop:72.0pt;
	mso-level-number-position:left;
	margin-left:72.0pt;
	text-indent:-72.0pt;}
@list l4:level9
	{mso-level-style-link:"标题 9";
	mso-level-text:"%1\.%2\.%3\.%4\.%5\.%6\.%7\.%8\.%9";
	mso-level-tab-stop:79.2pt;
	mso-level-number-position:left;
	margin-left:79.2pt;
	text-indent:-79.2pt;}
@list l5
	{mso-list-id:1819610573;
	mso-list-type:hybrid;
	mso-list-template-ids:-1306069802 1797422460 1178785956 67698715 67698703 67698713 67698715 67698703 67698713 67698715;}
@list l5:level1
	{mso-level-text:%1．;
	mso-level-tab-stop:39.0pt;
	mso-level-number-position:left;
	margin-left:39.0pt;
	text-indent:-18.0pt;}
@list l5:level2
	{mso-level-text:%2）;
	mso-level-tab-stop:60.0pt;
	mso-level-number-position:left;
	margin-left:60.0pt;
	text-indent:-18.0pt;}
@list l5:level3
	{mso-level-number-format:roman-lower;
	mso-level-tab-stop:84.0pt;
	mso-level-number-position:right;
	margin-left:84.0pt;
	text-indent:-21.0pt;}
@list l5:level4
	{mso-level-tab-stop:144.0pt;
	mso-level-number-position:left;
	text-indent:-18.0pt;}
@list l5:level5
	{mso-level-tab-stop:180.0pt;
	mso-level-number-position:left;
	text-indent:-18.0pt;}
@list l5:level6
	{mso-level-tab-stop:216.0pt;
	mso-level-number-position:left;
	text-indent:-18.0pt;}
@list l5:level7
	{mso-level-tab-stop:252.0pt;
	mso-level-number-position:left;
	text-indent:-18.0pt;}
@list l5:level8
	{mso-level-tab-stop:288.0pt;
	mso-level-number-position:left;
	text-indent:-18.0pt;}
@list l5:level9
	{mso-level-tab-stop:324.0pt;
	mso-level-number-position:left;
	text-indent:-18.0pt;}
ol
	{margin-bottom:0cm;}
ul
	{margin-bottom:0cm;}
-->
</style>
<!--[if gte mso 10]>
<style>
 /* Style Definitions */
 table.MsoNormalTable
	{mso-style-name:普通表格;
	mso-tstyle-rowband-size:0;
	mso-tstyle-colband-size:0;
	mso-style-noshow:yes;
	mso-style-parent:"";
	mso-padding-alt:0cm 5.4pt 0cm 5.4pt;
	mso-para-margin:0cm;
	mso-para-margin-bottom:.0001pt;
	mso-pagination:widow-orphan;
	font-size:10.0pt;
	font-family:"Times New Roman";
	mso-fareast-font-family:"Times New Roman";
	mso-ansi-language:#0400;
	mso-fareast-language:#0400;
	mso-bidi-language:#0400;}
</style>
<![endif]--><!--[if gte mso 9]><xml>
 <o:shapedefaults v:ext="edit" spidmax="3074"/>
</xml><![endif]--><!--[if gte mso 9]><xml>
 <o:shapelayout v:ext="edit">
  <o:idmap v:ext="edit" data="1"/>
 </o:shapelayout></xml><![endif]-->
</head>
<body lang=ZH-CN link=blue vlink=purple style='tab-interval:21.0pt;text-justify-trim:punctuation'>
<%
	// response.reset();
    // response.setContentType("application/pdf");
	
	String path = Global.getRealPath() + "cms/plugin/wiki/admin/template/";
	String t_dir = FileUtil.ReadFile(path + "t-dir.htm");
	String t_dir_t1 = FileUtil.ReadFile(path + "t-dir-t1.htm");
	String t_content_t1 = FileUtil.ReadFile(path + "t-content-t1.htm");
		
	String t_dir_t2 = FileUtil.ReadFile(path + "t-dir-t2.htm");
	
	LeafChildrenCacheMgr lccm = new LeafChildrenCacheMgr(dirCode);
	Vector chapterV = lccm.getList();
	Iterator ir = chapterV.iterator();
	int chapterNo = 1;
	com.redmoon.oa.fileark.Document doc = new com.redmoon.oa.fileark.Document();
	String bookDir = "";
	String bookCont = "";
	
	
	// 一级目录为章节
	while (ir.hasNext()) {
		Leaf lf = (Leaf)ir.next();
		
		String t = t_dir_t1;
		// 注意这里的t_dir_t1与t_dir_t2必须要区分，否则文档的目录将会被破坏，目录后面的正文内容也将会被认为是目录项，更新目录域时，会找不到目录
		// 使用_Toc时，鼠标浮上将显示"当前文档"，否则显示chapterNo
		if (chapterNo>1)
			t = t_dir_t2;
		t = t.replace("[title]", lf.getName());
		t = t.replace("[no]", NumberUtil.tran2CN(chapterNo));
		t = t.replaceAll("\\[anchor\\]", "_Toc" + chapterNo);
		
		String c = t_content_t1;
		c = c.replaceAll("\\[anchor\\]", "_Toc" + chapterNo);
		c = c.replace("[title]", lf.getName());
		c = c.replace("[no]", NumberUtil.tran2CN(chapterNo));
		
		bookDir += t;
		bookCont += c;
		
		chapterNo++;
	
	// if (chapterNo>1)
	//	break;
	// if (true) continue;;
		
		
		// 二、三级目录从文档中解析
		Iterator irdoc = doc.getDocumentsByDirCode(lf.getCode()).iterator();
		WikiDocumentDb wdd = new WikiDocumentDb();
		if (irdoc.hasNext()) {
			doc = (com.redmoon.oa.fileark.Document)irdoc.next();
			// System.out.println(getClass() + " " + doc.getContent(1));
			
			wdd = wdd.getWikiDocumentDb(doc.getId());
			int pageNum = wdd.getBestPageNum();
			
			WikiDoc wd = WikiUtil.parseDocument(doc.getContent(pageNum));
			
			bookDir += WikiUtil.renderDocDir(chapterNo, wd);
			bookCont += WikiUtil.renderDocContent(chapterNo, wd);
		}
	}
	
	String t_face = FileUtil.ReadFile(path + "t-face.htm");
	t_face = t_face.replace("[title]", showLeaf.getName());
%>
<%=t_face%>
<span lang=EN-US style='font-size:10.5pt;line-height:150%;font-family:仿宋_GB2312;
mso-fareast-font-family:宋体;mso-hansi-font-family:"Times New Roman";mso-bidi-font-family:
"Times New Roman";mso-font-kerning:1.0pt;mso-ansi-language:EN-US;mso-fareast-language:
ZH-CN;mso-bidi-language:AR-SA'><br clear=all style='page-break-before:always;
mso-break-type:section-break'>
</span>
<%
	bookDir = t_dir.replace("[dir]", bookDir);
	out.print(bookDir);
%>

<span lang=EN-US style='font-size:10.5pt;font-family:宋体;mso-bidi-font-family:
"Times New Roman";mso-font-kerning:1.0pt;mso-ansi-language:EN-US;mso-fareast-language:
ZH-CN;mso-bidi-language:AR-SA'><br clear=all style='page-break-before:always;
mso-break-type:section-break'>
</span>
<%
	String t_content = FileUtil.ReadFile(path + "t-content.htm");
	bookCont = t_content.replace("[content]", bookCont);
	out.print(bookCont);
%>
</body>
</html>