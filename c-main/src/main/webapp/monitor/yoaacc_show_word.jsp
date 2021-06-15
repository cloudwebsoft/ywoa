<%@ page language="java" contentType="text/html; charset=utf-8"%><%@ page import="java.util.regex.*"%><%@ page import="cn.js.fan.util.*"%><%
String cont = ParamUtil.get(request, "cont");
cont = filterString(cont);
response.setContentType("application/msword;charset=utf-8");

// 注意来自于嵌套表nest_table_view.jsp中的数据，如果不过滤style就会出现乱码
// 采用以下方式导入的css文件，不会出现乱码：@import url("..."); 
// 可能是因为生成word后，丢失了css，所以不会出现乱码

// 过滤javascript
String regExScript = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; //定义script的正则表达式{或]*?>[\\s\\S]*?<\\/script> } 
//String regExScript = "<script[^>]*>.*</script[^>]*>"; // 此行过滤不了，@task:AntiXSS.stripScriptTag中可能存在同样问题

Pattern pat = Pattern.compile(regExScript, Pattern.CASE_INSENSITIVE);
Matcher m = pat.matcher(cont);
cont = m.replaceAll("");

// 过滤style
String regExStyle = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; //定义style的正则表达式{或]*?>[\\s\\S]*?<\\/style> } 
pat = Pattern.compile(regExStyle, Pattern.CASE_INSENSITIVE);
m = pat.matcher(cont);
cont = m.replaceAll("");

// 过滤html
// String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式 


// System.out.println(getClass() + cont);
%><style>
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
table.MsoTableProfessional{
	mso-style-name:专业型;
	mso-tstyle-rowband-size:0;
	mso-tstyle-colband-size:0;
	border:solid black 1.0pt;
	mso-border-alt:solid black .75pt;
	mso-padding-alt:0cm 5.4pt 0cm 5.4pt;
	mso-border-insideh:.75pt solid black;
	mso-border-insidev:.75pt solid black;
	mso-para-margin:0cm;
	mso-para-margin-bottom:.0001pt;
	mso-pagination:widow-orphan;
	font-size:10.0pt;
	font-family:"Times New Roman";
	mso-ansi-language:#0400;
	mso-fareast-language:#0400;
	mso-bidi-language:#0400;
	border-collapse:collapse;
}
table.MsoTableProfessional td {	
border:solid black 1.0pt;
mso-border-alt:solid black .75pt;
padding:0cm 5.4pt 0cm 5.4pt;
font-size:12px;
}
table.MsoTableProfessionalFirstRow
	{mso-style-name:专业型;
	mso-table-condition:first-row;
	mso-tstyle-shading:white;
	mso-tstyle-pattern:solid black;
	mso-tstyle-diagonal-down:0cm none windowtext;
	mso-tstyle-diagonal-up:0cm none windowtext;
	color:windowtext;
	mso-ansi-font-weight:bold;
	mso-bidi-font-weight:bold;}
</style><%

out.print(cont);
%><%!
	// 过滤超链接、隐藏输入框
	String filterString(String content){
		// content = "asdfasdf<input type=\"hidden\" name=\"checkItemsSel\" id=\"checkItemsSel\" value=\"\" />asdfasdf";
        String patternStr = "", replacementStr = "";
        Pattern pattern;
        Matcher matcher;
        replacementStr = "";
        patternStr = "<a .*?style=['|\"]?display:none['|\"]?>(.*?)</div>"; 
        pattern = Pattern.compile(patternStr,
                                  Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(replacementStr);
        
        patternStr = "<input .*?type=['|\"]?hidden['|\"]? .*?>"; 
        pattern = Pattern.compile(patternStr,
                                  Pattern.CASE_INSENSITIVE); //在指定DOTAll模式时"."匹配所有字符
        matcher = pattern.matcher(content);
        content = matcher.replaceAll(replacementStr);
        
        return content;
	}
%>