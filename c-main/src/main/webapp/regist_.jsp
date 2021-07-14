<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.UserSet"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.jdom.Element"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
PassportConfig pc = PassportConfig.getInstance();
if (pc.getBooleanProperty("passport.isUsed")) {
	response.sendRedirect(pc.getProperty("passport.regUrl"));
	return;
}

com.redmoon.forum.RegConfig rcfg = new com.redmoon.forum.RegConfig();
boolean permitRegUser = true,regAdvance = false,regCompact = false;
int IPRegCtrl = 0;
permitRegUser = rcfg.getBooleanProperty("permitRegUser");
regAdvance = rcfg.getBooleanProperty("regAdvance");
regCompact = rcfg.getBooleanProperty("regCompact");
IPRegCtrl = rcfg.getIntProperty("IPRegCtrl");
	
if(!permitRegUser){
	response.sendRedirect("info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "res.label.forum.user","nopermitreguser")));
	return;
}
	
if(regCompact){
	String ruleSubmit = ParamUtil.get(request,"ruleSubmit");
	if(!ruleSubmit.equals("true")){
		response.sendRedirect("regist_contract.jsp");
		return;
	}
}
	
String skinPath = SkinMgr.getSkinPath(request);

//seo
com.redmoon.forum.util.SeoConfig scfg = new com.redmoon.forum.util.SeoConfig();
String seoTitle = scfg.getProperty("seotitle");
String seoKeywords = scfg.getProperty("seokeywords");
String seoDescription = scfg.getProperty("seodescription");
String seoHead = scfg.getProperty("seohead");

IPMonitor ipmr = new IPMonitor();
long end = System.currentTimeMillis();
long begin = 0;
UserDb ud = new UserDb();
ud = ud.getUserDbByIP(request.getRemoteAddr());
if(ipmr.isIPOfRegistSpecialScope(request.getRemoteAddr())){
	if(ud != null){
		begin = ud.getRegDate().getTime();
		if(end - begin < 24 * 60 * 60000)	{
			response.sendRedirect("info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "res.label.forum.user", "specialipregctrl")));
			return;
		}
	}
}
// IP注册间隔限制(小时)
if(ud != null && IPRegCtrl != 0){
	begin = ud.getRegDate().getTime();
	if(end - begin < IPRegCtrl * 60 * 60000)	{
		response.sendRedirect("info.jsp?info=" + StrUtil.UrlEncode(StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.user", "specialipregctrl_interval"), new Object[]{"" + IPRegCtrl})));
		return;
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.regist" key="regist"/> - <%=Global.AppName%> <%=seoTitle%></title>
<%=seoHead%>
<META name="keywords" content="<%=seoKeywords%>">
<META name="description" content="<%=seoDescription%>">
<LINK href="forum/<%=skinPath%>/css.css" type=text/css rel=stylesheet>
<script src="inc/livevalidation_standalone.js"></script>
<script src="inc/common.js"></script>
<script>
function New(para_URL){var URL=new String(para_URL);window.open(URL,'','resizable,scrollbars')}

function findObj(theObj, theDoc){
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}

//-------------check code begin--------------------
function initCheckFrame() {
	var checkFrame = window.frames["checkFrame"];
	checkFrame.document.open();
	checkFrame.document.write("<form name=\"form_param\" method=\"post\">");
	checkFrame.document.write("</form>");
	checkFrame.document.close();
	checkFrame.document.title="Check Param";
	checkFrame.document.charset="UTF-8";
}

function initCheck(formAction) {
	initCheckFrame();
	var checkFrame = window.frames["checkFrame"];
	checkFrame.document.form_param.action = formAction;
	checkFrame.document.form_param.innerHTML = "";
}

function addCheckParam(paramName, paramValue) {
	var inputTxt = "<input name='" + paramName + "' value='" + paramValue + "'>";
	var checkFrame = window.frames["checkFrame"];
	checkFrame.document.form_param.innerHTML += inputTxt;
}

function doCheck() {
	var checkFrame = window.frames["checkFrame"];
	checkFrame.document.form_param.submit();
}

function showCheckResult(spanName, result) {
	var spanObj = findObj(spanName);
	if (spanObj!=null)
		spanObj.innerHTML = result;
}
//-------------check code end--------------------

function CheckRegName(){
	var Name = document.frmAnnounce.RegName.value;
	initCheck("regist_check.jsp");
	addCheckParam("RegName", Name);
	addCheckParam("op", "chkRegName");
	doCheck();
}

function CheckEmail() {
	var email = document.frmAnnounce.Email.value;
	initCheck("regist_check.jsp");
	addCheckParam("Email", email);
	addCheckParam("op", "chkEmail");
	doCheck();
}

function showTableDetail() {
	if (tableDetail.style.display=="none")
		tableDetail.style.display = "";
	else
		tableDetail.style.display = "none";
}

var errFunc = function(response) {
	window.status = 'Error ' + response.status + ' - ' + response.statusText;
}

function doCheckQuiz(response) {
	var rsp = response.responseText.trim();
	if(rsp.indexOf("-")==0){
		$('span_quiz').innerText="答案不正确，请重新输入！";
	} else {
		$('span_quiz').innerText="答案正确！";
	}
}

function CheckQuiz() {
	var str = "quizAnswer=" + document.getElementById('quizAnswer').value + "&qid=" +document.getElementById('qid').value;
	var myAjax = new cwAjax.Request( 
		"regist_ajax_quiz_check.jsp", 
		{ 
			method:"post",
			parameters:str,
			onComplete:doCheckQuiz,
			onError:errFunc
		}
	);
}

var errFuncCityCountry = function(response) {
	alert(response.responseText);
}

function doGetCityCountry(response) {
	var ary = response.responseText.trim().split('|');
	if (o('city_id')==null) alert('缺少城市输入框');
	else if (ary[0]!='') o('city_id').outerHTML=ary[0];
	if (o('country_id')==null) alert('缺少区县输入框');
	else o('country_id').outerHTML=ary[1].trim();
}

function ajaxShowCityCountry(province, city) {
	var str = 'isCity=true&isCountry=true&province=' + province + '&city=' + city;
	var myAjax = new cwAjax.Request(
		'tools/ajax_get_city_country.jsp',
		{ 
			method:'post',
			parameters:str,
			onComplete:doGetCityCountry,
			onError:errFuncCityCountry
		}
	);
}
</script>
<script src="forum/inc/ubbcode.jsp"></script>
</head>
<body>
<div id="wrapper">
<%@ include file="forum/inc/header.jsp"%>
<div id="main">
<Form method="post" action="regist_do.jsp"  name="frmAnnounce" onSubmit="return VerifyInput()">
  <table width="100%" class="tableCommon" border="0" cellpadding="0" cellspacing="0">
    <thead>
	<tr>
      <td height="28" colspan="2" align="left">
        <lt:Label res="res.label.regist" key="nick_pwd"/>
        <lt:Label res="res.label.regist" key="notice"/>
        &nbsp;&nbsp;&nbsp;&nbsp;
        <%
		RegConfig rc = new RegConfig();
        int regVerify = rc.getIntProperty("regVerify");
        if (regVerify==rc.REGIST_VERIFY_EMAIL)
			out.print(SkinUtil.LoadString(request, "res.forum.Privilege", "info_need_check_email"));
        else
			out.print(SkinUtil.LoadString(request, "res.forum.Privilege", "info_need_check_manual"));
%>      </td>
    </tr></thead>
    <tr>
      <td width="128" height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="RegName"/></td>
      <td align="left">&nbsp;
          <input id="RegName" name="RegName" type="text" size="20" maxlength="50" onblur="CheckRegName()" />
          <font color="#FF0000"> * </font>
          <input name="Button" type="button" onclick="javascript:CheckRegName()" value='<lt:Label res="res.label.forum.user" key="check_user_name"/>' />
          <font color="red"><span id="span_RegName"></span></font> </td>
    </tr>
    <tr>
      <td height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="Password"/></td>
      <td align="left" valign="top"><img src="/images/c.gif" width="1" height="5" /><br />
        &nbsp;
        <input id="Password" name="Password" type="password" size="20" maxlength="20" />
        <font color="#FF0000">*</font>
        <lt:Label res="res.label.forum.user" key="Password2"/>
        <input id="Password2" name="Password2" type="password" size="20" maxlength="20" />
        <font color="#FF0000"> *</font></td>
    </tr>
    <tr>
      <td height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="Gender"/></td>
      <td height="25" align="left" valign="middle">&nbsp;
          <input id="Gender" type="radio" name="Gender" value="M" checked="checked" onclick="frmAnnounce.RealPic.value='face.gif';showimage()" />
          <lt:Label res="res.label.forum.user" key="man"/>
          <input id="Gender" type="radio" name="Gender" value="F" onclick="frmAnnounce.RealPic.value='face0.gif';showimage()" />
          <lt:Label res="res.label.forum.user" key="woman"/>
        &nbsp;</td>
    </tr>
    <tr>
      <td height="28" align="left">&nbsp;&nbsp;Email</td>
      <td height="25" align="left" valign="middle">&nbsp;
          <input id="Email" name="Email" type="text" size="20" maxlength="50" onblur="CheckEmail()" />
          <font color="#FF0000">*&nbsp;<span id="span_email"></span></font></td>
    </tr>
<%
com.redmoon.forum.RegConfig cfg1 = new com.redmoon.forum.RegConfig();
if (cfg1.getBooleanProperty("isRegQuiz")) {
	QuizMgr qm = new QuizMgr();
	QuizDb quiz = qm.getRandomQuizDb();
	if (quiz!=null) {
%>	
    <tr>
      <td height="28" align="left">&nbsp;&nbsp;注册提问</td>
      <td height="25" align="left" valign="middle">&nbsp;
		<input id="quizAnswer" name="quizAnswer" type="text" size="20" onblur="CheckQuiz()" />
		<font color="#FF0000">*&nbsp;
		<%=quiz.getString("question")%>
		<input name="qid" id="qid" type="hidden" value="<%=quiz.getInt("id")%>" />
		<span id="span_quiz"></span>
		</font>
		</td>
    </tr>
<%
	}
	else {%>
    <tr>
      <td height="28" align="left">&nbsp;&nbsp;注册提问</td>
      <td height="25" align="left" valign="middle">&nbsp;
		<span style="color:red">注册问题尚未初始化，请联系管理员！</span>
		</td>
    </tr>
	<%
	}
}
if (cfg1.getBooleanProperty("registUseValidateCode")) {
	int charNum = cfg1.getIntProperty("registUseValidateCodeLen");
%>

    <tr>
      <td height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="validate_code"/></td>
      <td height="25" align="left" valign="middle">&nbsp;
          <input id="validateCode" name="validateCode" size="6" />
          <!--xxx 目的是为了兼容刷新firefox-->
          <img style="cursor:pointer" alt="<lt:Label res="res.label.forum.user" key="click_to_refresh"/>" id="imgValidateCode" src='validatecode.jsp?charNum=<%=charNum%>' border="0" align="absmiddle" onclick="$('imgValidateCode').src='validatecode.jsp?charNum=<%=charNum%>' + '&amp;xxx=' + new Date().getTime();" />
          <lt:Label res="res.label.forum.user" key="click_to_refresh"/></td>
    </tr>
    <%}%>
    <tr>
      <td height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="Question"/></td>
      <td height="25" align="left" valign="middle">&nbsp;
          <input id="Question" name="Question" type="text" size="20" maxlength="50" />
          <lt:Label res="res.label.forum.user" key="Answer"/>
          <input id="Answer" name="Answer" type="text" size="20" maxlength="50" />
          ( 用于找回密码 )</td>
    </tr>
  </table>
  <table width="100%" class="tableCommon" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td height="23">+ <a href="javascript:showTableDetail()">
        <lt:Label res="res.label.forum.user" key="click_here"/>
        </a>&nbsp;&nbsp;
        <lt:Label res="res.label.forum.user" key="fill_detail"/>
        <input id="isSecret" name="isSecret" value="true" type="checkbox" />
        <lt:Label res="res.label.forum.user" key="secret"/></td>
      </tr>
  </table>
  <table id="tableDetail" name="tableDetail" width="100%" class="tableCommon" <%if(!regAdvance){%>style="display:none"<%}%>>
    <tr>
      <td width="128" align="left" height="28">&nbsp;
          <lt:Label res="res.label.forum.user" key="RealName"/></td>
      <td width="188" height="28" valign="middle">&nbsp;
          <input id="RealName" name="RealName" type="text" size="12" maxlength="20" />
      </td>
      <td width="103" height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="Career"/></td>
      <td height="28" align="left">&nbsp;
          <select id="Career" name="Career" size="1">
            <option value="" selected="selected">
              <lt:Label res="res.label.forum.user" key="select"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="government"/>">
              <lt:Label res="res.label.forum.user" key="government"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="student"/>">
              <lt:Label res="res.label.forum.user" key="student"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="communication"/>">
              <lt:Label res="res.label.forum.user" key="communication"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="computer"/>">
              <lt:Label res="res.label.forum.user" key="computer"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="web"/>">
              <lt:Label res="res.label.forum.user" key="web"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="trade"/>">
              <lt:Label res="res.label.forum.user" key="trade"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="bank"/>">
              <lt:Label res="res.label.forum.user" key="bank"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="tax"/>">
              <lt:Label res="res.label.forum.user" key="tax"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="refer"/>">
              <lt:Label res="res.label.forum.user" key="refer"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="service"/>">
              <lt:Label res="res.label.forum.user" key="service"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="tour"/>">
              <lt:Label res="res.label.forum.user" key="tour"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="health"/>">
              <lt:Label res="res.label.forum.user" key="health"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="realty"/>">
              <lt:Label res="res.label.forum.user" key="realty"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="transport"/>">
              <lt:Label res="res.label.forum.user" key="transport"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="law"/>">
              <lt:Label res="res.label.forum.user" key="law"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="culture"/>">
              <lt:Label res="res.label.forum.user" key="culture"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="ad"/>">
              <lt:Label res="res.label.forum.user" key="ad"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="education"/>">
              <lt:Label res="res.label.forum.user" key="education"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="agriculture"/>">
              <lt:Label res="res.label.forum.user" key="agriculture"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="manufacturing"/>">
              <lt:Label res="res.label.forum.user" key="manufacturing"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="soho"/>">
              <lt:Label res="res.label.forum.user" key="soho"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="other"/>">
              <lt:Label res="res.label.forum.user" key="other"/>
            </option>
          </select>
      </td>
    </tr>
    <tr>
      <td width="128" height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="Mobile"/></td>
      <td height="28"><span class="l15"> &nbsp;
            <input id="Mobile" name="Mobile" type="text" size="16" maxlength="16" />
      </span></td>
      <td width="103" height="28">&nbsp;
          <lt:Label res="res.label.forum.user" key="Job"/></td>
      <td height="28" class="l15">&nbsp;
          <select id="Job" name="Job" size="1">
            <option value="" selected="selected">
              <lt:Label res="res.label.forum.user" key="select"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="staffer"/>">
              <lt:Label res="res.label.forum.user" key="staffer"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="engineer"/>">
              <lt:Label res="res.label.forum.user" key="engineer"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="ceo"/>">
              <lt:Label res="res.label.forum.user" key="ceo"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="market_manager"/>">
              <lt:Label res="res.label.forum.user" key="market_manager"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="administration_manager"/>">
              <lt:Label res="res.label.forum.user" key="administration_manager"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="person_manager"/>">
              <lt:Label res="res.label.forum.user" key="person_manager"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="finance_manager"/>">
              <lt:Label res="res.label.forum.user" key="finance_manager"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="technology_manager"/>">
              <lt:Label res="res.label.forum.user" key="technology_manager"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="retire"/>">
              <lt:Label res="res.label.forum.user" key="retire"/>
            </option>
            <option value="<lt:Label res="res.label.forum.user" key="other"/>">
              <lt:Label res="res.label.forum.user" key="other"/>
            </option>
          </select>
      </td>
    </tr>
    <tr>
      <td width="128" height="28" align="left" >&nbsp;
          <lt:Label res="res.label.forum.user" key="birthday"/></td>
      <td height="28">&nbsp;
          <select id="BirthYear" name="BirthYear" class="input1">
            <script language="JavaScript" type="text/javascript">
	  <!--
		var tmpDate = new Date();
		var year= tmpDate.getUTCFullYear();
		var month = tmpDate.getMonth()+1;
		var date = tmpDate.getDate();
		for (var i=-50; i<=100; i++)
			document.write("<option value="+(year+i)+">"+(year+i)+"</option>")
	  //-->
	  </script>
          </select>
          <lt:Label res="res.label.forum.user" key="BirthYear"/>
          <select id="BirthMonth" name="BirthMonth" class="input1">
            <option value="1" selected="selected">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
            <option value="6">6</option>
            <option value="7">7</option>
            <option value="8">8</option>
            <option value="9">9</option>
            <option value="10">10</option>
            <option value="11">11</option>
            <option value="12">12</option>
          </select>
          <lt:Label res="res.label.forum.user" key="BirthMonth"/>
          <select id="BirthDay" name="BirthDay" class="input1">
            <option value="1" selected="selected">1</option>
            <option value="2">2</option>
            <option value="3">3</option>
            <option value="4">4</option>
            <option value="5">5</option>
            <option value="6">6</option>
            <option value="7">7</option>
            <option value="8">8</option>
            <option value="9">9</option>
            <option value="10">10</option>
            <option value="11">11</option>
            <option value="12">12</option>
            <option value="13">13</option>
            <option value="14">14</option>
            <option value="15">15</option>
            <option value="16">16</option>
            <option value="17">17</option>
            <option value="18">18</option>
            <option value="19">19</option>
            <option value="20">20</option>
            <option value="21">21</option>
            <option value="22">22</option>
            <option value="23">23</option>
            <option value="24">24</option>
            <option value="25">25</option>
            <option value="26">26</option>
            <option value="27">27</option>
            <option value="28">28</option>
            <option value="29">29</option>
            <option value="30">30</option>
            <option value="31">31</option>
          </select>
          <lt:Label res="res.label.forum.user" key="BirthDay"/>
          <script language="JavaScript" type="text/javascript">
	  <!--
		tmpDate = new Date();
		year= ""+tmpDate.getYear();
		month = tmpDate.getMonth()+1;
		//month = ""+month;
		//if (month.length==1)
		//	month = "0"+month;
		//date = ""+tmpDate.getDate();
		//if (date.length==1)
		//	date = "0"+date;
	  
	  frmAnnounce.BirthYear.value = year;
	  frmAnnounce.BirthMonth.value = month;
	  frmAnnounce.BirthDay.value = date;
	  //-->
	  </script>
      </td>
      <td width="103" height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="marry_status"/></td>
      <td height="28" class="l15">&nbsp;
          <select id="Marriage" name="Marriage" size="1">
            <option value="0" selected="selected">
              <lt:Label res="res.label.forum.user" key="marry_not"/>
            </option>
            <option value="2">
              <lt:Label res="res.label.forum.user" key="marry_not_know"/>
            </option>
            <option value="1">
              <lt:Label res="res.label.forum.user" key="marry_yes"/>
            </option>
          </select>
      </td>
    </tr>
    <tr>
      <td width="128" height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="phone"/></td>
      <td height="28" valign="middle">&nbsp;
          <input id="Phone" name="Phone" type="text" size="16" maxlength="20" />
      </td>
      <td width="103" height="28">&nbsp;&nbsp;QQ</td>
      <td height="28" class="l15">&nbsp;
          <input id="OICQ" name="OICQ" type="text" size="16" maxlength="15" /></td>
    </tr>
    <tr>
      <td width="128" height="28" align="left"><img src="/images/c.gif" width="1" height="5" />
          <lt:Label res="res.label.forum.user" key="State"/></td>
      <td valign="middle" height="28" >&nbsp;
        <select id="province_id" name="province_id" onchange="ajaxShowCityCountry(this.value, '')">
          <option value="0">无</option>
      <%
	  com.redmoon.forum.tools.ChinaRegionDb crd = new com.redmoon.forum.tools.ChinaRegionDb();
	  Iterator irRegion = crd.list(crd.getTable().getSql("listProvince")).iterator();
	  while (irRegion.hasNext()) {
		  crd = (com.redmoon.forum.tools.ChinaRegionDb)irRegion.next();
	  %>
      <option value="<%=crd.getInt("region_id")%>"><%=crd.getString("region_name")%></option>
      <%}%>
    </select>    
      </td>
      <td colspan="2" rowspan="5" valign="middle" align="left"><table width="87%" border="0" cellspacing="0" cellpadding="0" align="left">
          <tr>
            <td height="56"  bgcolor="#FFFFFF">&nbsp;&nbsp; <img src="forum/images/face/face.gif" name="tus" id="tus" />&nbsp;
                <script>function showimage(){document.images.tus.src="forum/images/face/"+document.frmAnnounce.RealPic.options[document.frmAnnounce.RealPic.selectedIndex].value;}</script>
                <select id="RealPic" name="RealPic" size="1" onchange="showimage()">
                  <option value="face.gif">
                    <lt:Label res="res.label.forum.user" key="default_icon"/>
                  </option>
                  <%
							FileViewer fileViewer = new FileViewer(cn.js.fan.web.Global.realPath + "/forum/images/face/");
							fileViewer.init();
							int i = 1;
							while(fileViewer.nextFile()){
							  if (fileViewer.getFileName().lastIndexOf("gif") != -1 || fileViewer.getFileName().lastIndexOf("jpg") != -1 || fileViewer.getFileName().lastIndexOf("png") != -1 || fileViewer.getFileName().lastIndexOf("bmp") != -1 && fileViewer.getFileName().indexOf("face") != -1) {
							   String fileName = fileViewer.getFileName();
							%>
                  <option value="<%=fileName%>"><%=i++%></option>
                  <% }
							} %>
              </select></td>
          </tr>
          <tr>
            <td height="56"  bgcolor="#FFFFFF">&nbsp;<a href="JavaScript:New('images/index.jsp')">
              <lt:Label res="res.label.forum.user" key="view_all_icon"/>
            </a></td>
          </tr>
      </table></td>
    </tr>
    <tr>
      <td width="128" align="left" height="28">&nbsp;
          <lt:Label res="res.label.forum.user" key="City"/></td>
      <td height="28" valign="middle">&nbsp;
        <select id="city_id" name="city_id" onchange="if (this.value!='') ajaxShowCityCountry('',this.value)">
        </select>
      <select id="country_id" name="country_id">
      </select>          
      </td>
    </tr>
    <tr>
      <td width="128" align="left" height="28">&nbsp;
          <lt:Label res="res.label.forum.user" key="Address"/></td>
      <td height="28" valign="middle">&nbsp;
          <input id="Address" name="Address" type="text" size="25" maxlength="100" />
      </td>
    </tr>
    <tr>
      <td width="128" align="left" height="28">&nbsp;
          <lt:Label res="res.label.forum.user" key="PostCode"/></td>
      <td height="28" valign="middle">&nbsp;
          <input id="PostCode" name="PostCode" type="text" size="10" maxlength="20" />
      </td>
    </tr>
    <tr>
      <td width="128" align="left" height="29">&nbsp;
          <lt:Label res="res.label.forum.user" key="IDCard"/></td>
      <td height="29" valign="middle">&nbsp;
          <input id="IDCard" name="IDCard" type="text" size="21" maxlength="50" />
      </td>
    </tr>
    <tr>
      <td align="left" height="29">&nbsp;
          <lt:Label res="res.label.forum.user" key="Hobbies"/></td>
      <td height="29" colspan="3" valign="middle">&nbsp;
          <input id="Hobbies" name="Hobbies" type="text" size="30" /></td>
    </tr>
    <tr>
      <td align="left" height="37">&nbsp;
          <lt:Label res="res.label.forum.user" key="home"/></td>
      <td colspan="3" valign="middle">&nbsp;
          <input id="home" name="home" type="text" value="" size="30" maxlength="50" /></td>
    </tr>
    <tr>
      <td align="left" height="37">&nbsp;MSN</td>
      <td colspan="3" valign="middle">&nbsp;
          <input id="msn" name="msn" type="text" value="" size="30" maxlength="50" /></td>
    </tr>
    <tr>
      <td align="left" height="37">&nbsp;
          <lt:Label res="res.label.forum.user" key="fetion"/></td>
      <td colspan="3" valign="middle">&nbsp;
          <input id="fetion" name="fetion" type="text" value="" size="30" maxlength="20" /></td>
    </tr>
    <tr>
      <td align="left" height="29">&nbsp;
          <lt:Label res="res.label.forum.user" key="locale"/></td>
      <td height="29" colspan="3" valign="middle">&nbsp;
          <select id="locale" name="locale" size="1">
            <option value="" selected="selected">
            <lt:Label res="res.label.forum.user" key="locale_default"/>
            </option>
            <%
            XMLConfig xc = new XMLConfig("config_i18n.xml", false, "utf-8");
            Element root = xc.getRootElement();
            Element child = root.getChild("support");
            List list = child.getChildren();
            if (list != null) {
                Iterator ir = list.iterator();
                while (ir.hasNext()) {
                    Element e = (Element) ir.next();
					String loc = e.getChildText("lang") + "_" + e.getChildText("country");
                    out.print("<option value=" + loc + ">" + SkinUtil.LoadString(request, "res.config.config_i18n", loc) + "</option>");
                }
            }
%>
          </select>
      </td>
    </tr>
    <tr>
      <td align="left" height="29">&nbsp;
          <lt:Label res="res.label.forum.user" key="timeZone"/></td>
      <td height="29" colspan="3" valign="middle">&nbsp;
          <select id="timeZone" name="timeZone">
            <option value="GMT-11:00">(GMT-11.00)
              <lt:Label res="res.label.cms.config" key="GMT-11.00"/>
            </option>
            <option value="GMT-10:00">(GMT-10.00)
              <lt:Label res="res.label.cms.config" key="GMT-10.00"/>
            </option>
            <option value="GMT-09:00">(GMT-9.00)
              <lt:Label res="res.label.cms.config" key="GMT-9.00"/>
            </option>
            <option value="GMT-08:00">(GMT-8.00)
              <lt:Label res="res.label.cms.config" key="GMT-8.00"/>
            </option>
            <option value="GMT-07:00">(GMT-7.00)
              <lt:Label res="res.label.cms.config" key="GMT-7.00"/>
            </option>
            <option value="GMT-06:00">(GMT-6.00)
              <lt:Label res="res.label.cms.config" key="GMT-6.00"/>
            </option>
            <option value="GMT-05:00">(GMT-5.00)
              <lt:Label res="res.label.cms.config" key="GMT-5.00"/>
            </option>
            <option value="GMT-04:00">(GMT-4.00)
              <lt:Label res="res.label.cms.config" key="GMT-4.00"/>
            </option>
            <option value="GMT-03:00">(GMT-3.00)
              <lt:Label res="res.label.cms.config" key="GMT-3.00"/>
            </option>
            <option value="GMT-02:00">(GMT-2.00)
              <lt:Label res="res.label.cms.config" key="GMT-2.00"/>
            </option>
            <option value="GMT-01:00">(GMT-1.00)
              <lt:Label res="res.label.cms.config" key="GMT-1.00"/>
            </option>
            <option value="GMT">(GMT)
              <lt:Label res="res.label.cms.config" key="GMT"/>
            </option>
            <option value="GMT+01:00">(GMT+1.00)
              <lt:Label res="res.label.cms.config" key="GMT+1.00"/>
            </option>
            <option value="GMT+02:00">(GMT+2.00)
              <lt:Label res="res.label.cms.config" key="GMT+2.00"/>
            </option>
            <option value="GMT+03:00">(GMT+3.00)
              <lt:Label res="res.label.cms.config" key="GMT+3.00"/>
            </option>
            <option value="GMT+04:00">(GMT+4.00)
              <lt:Label res="res.label.cms.config" key="GMT+4.00"/>
            </option>
            <option value="GMT+04:30">(GMT+4.30)
              <lt:Label res="res.label.cms.config" key="GMT+4.30"/>
            </option>
            <option value="GMT+05:00">(GMT+5.00)
              <lt:Label res="res.label.cms.config" key="GMT+5.00"/>
            </option>
            <option value="GMT+05:30">(GMT+5.30)
              <lt:Label res="res.label.cms.config" key="GMT+5.30"/>
            </option>
            <option value="GMT+05:45">(GMT+5.45)
              <lt:Label res="res.label.cms.config" key="GMT+5.45"/>
            </option>
            <option value="GMT+06:00">(GMT+6.00)
              <lt:Label res="res.label.cms.config" key="GMT+6.00"/>
            </option>
            <option value="GMT+06:30">(GMT+6.30)
              <lt:Label res="res.label.cms.config" key="GMT+6.30"/>
            </option>
            <option value="GMT+07:00">(GMT+7.00)
              <lt:Label res="res.label.cms.config" key="GMT+7.00"/>
            </option>
            <option value="GMT+08:00" selected="selected">(GMT+8.00)
              <lt:Label res="res.label.cms.config" key="GMT+8.00"/>
            </option>
            <option value="GMT+09:00">(GMT+9.00)
              <lt:Label res="res.label.cms.config" key="GMT+9.00"/>
            </option>
            <option value="GMT+09:30">(GMT+9.30)
              <lt:Label res="res.label.cms.config" key="GMT+9.30"/>
            </option>
            <option value="GMT+10:00">(GMT+10.00)
              <lt:Label res="res.label.cms.config" key="GMT+10.00"/>
            </option>
            <option value="GMT+11:00">(GMT+11.00)
              <lt:Label res="res.label.cms.config" key="GMT+11.00"/>
            </option>
            <option value="GMT+12:00">(GMT+12.00)
              <lt:Label res="res.label.cms.config" key="GMT+12.00"/>
            </option>
            <option value="GMT+13:00">(GMT+13.00)
              <lt:Label res="res.label.cms.config" key="GMT+13.00"/>
            </option>
        </select></td>
    </tr>
    <tr>
      <td align="left" valign="middle">&nbsp;
          <lt:Label res="res.label.forum.user" key="sign"/>
          <br />
          <br />
        &nbsp;UBB：
        <%
				  com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
				  if (cfg.getBooleanProperty("forum.sign_ubb"))
				  	out.print(SkinUtil.LoadString(request, "res.label.forum.user", "support"));
				  else
				  	out.print(SkinUtil.LoadString(request, "res.label.forum.user", "not_support"));
				  %>
      </td>
      <td colspan="3" valign="middle"><%
				int level = StrUtil.toInt(cfg.getProperty("forum.signUserLevel"), 0);
				if (level>0) {
				%>
        &nbsp;
        <%@ include file="forum/inc/getubb.jsp"%>
        <br />
        &nbsp;
        <textarea id="Content" cols="75" name="Content" rows="5" wrap="virtual" title="签名档"></textarea>
        <font color="#000000"><br />
          &nbsp;&nbsp;
          <lt:Label res="res.label.forum.user" key="sign_limit_count"/>
          <%=cfg.getIntProperty("forum.sign_length")%></font>
        <%}else{
					UserLevelDb uld = new UserLevelDb();
					uld = uld.getUserLevelDbByLevel(level);
				%>
        <input id="Content" name="Content" type="hidden" />
        &nbsp;&nbsp;<%=StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.user", "signUserLevel"), new Object[] {uld.getDesc()})%>
        <%}%>
      </td>
    </tr>
  </table>
<table class="tableCommon"><tr><td align="center">
<input id="Write" name=Write type=submit value="<lt:Label key="ok"/>">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input name=reset type=reset value="<lt:Label key="reset"/>">
</td>
</tr></table>
</form>
<iframe width=0 height=0 src="regist_check.jsp" name="checkFrame" id="checkFrame"></iframe>
</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
<SCRIPT>
<%
	cn.js.fan.util.ParamConfig pcf = new cn.js.fan.util.ParamConfig("form_rule.xml");
	ParamChecker pck = new ParamChecker(request);
	out.print(pck.doGetCheckJS(pcf.getFormRule("regist")));
%>
function VerifyInput()
{
var newDateObj = new Date()
if (document.frmAnnounce.RegName.value == "")
{
alert("<lt:Label res="res.label.forum.user" key="need_regname"/>");
document.frmAnnounce.RegName.focus();
return false;
}

if (document.frmAnnounce.Password.value == "")
{
alert("<lt:Label res="res.label.forum.user" key="need_pwd"/>");
document.frmAnnounce.Password.focus();
return false;
}
if (document.frmAnnounce.Password2.value == "")
{
alert("<lt:Label res="res.label.forum.user" key="need_pwd2"/>");
document.frmAnnounce.Password2.focus();
return false;
}
if (document.frmAnnounce.Password.value != document.frmAnnounce.Password2.value)
{
alert("<lt:Label res="res.label.forum.user" key="pwd_not_equal_pwd2"/>");
document.frmAnnounce.Password.focus();
return false;
}

if (document.frmAnnounce.Email.value == "")
{
alert("<lt:Label res="res.label.forum.user" key="need_email"/>");
document.frmAnnounce.Email.focus();
return false;
}

if (document.frmAnnounce.BirthYear.value > 0)  {
	if (isNaN(document.frmAnnounce.BirthYear.value) || document.frmAnnounce.BirthYear.value > newDateObj.getFullYear()  || document.frmAnnounce.BirthYear.value < 1900)
	{
		alert("<lt:Label res="res.label.forum.user" key="err_birthday"/>");
		document.frmAnnounce.BirthYear.focus();
		return false;
	}
}

var signlen = <%=cfg.getIntProperty("forum.sign_length")%>;
if (document.frmAnnounce.Content.value.length>signlen)
{
	alert("<lt:Label res="res.label.forum.user" key="sign_limit_count"/>" + signlen);
	document.frmAnnounce.Content.focus();
	return false;
}

return true;
}
</SCRIPT>
</html>