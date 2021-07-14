<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="java.net.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.setup.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.sms.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="org.jdom.Element"%>
<%@ page import ="com.redmoon.forum.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.myinfo" key="myinfo"/> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="forum/<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<script src="inc/common.js"></script>
<script>
function New(para_URL){var URL=new String(para_URL);window.open(URL,'','resizable,scrollbars')}
function CheckRegName(){
	var Name=document.frmAnnounce.RegName.value;
	window.open("checkregname.jsp?RegName="+Name,"","width=200,height=20");
}

function check_checkbox(myitem,myvalue){
     var checkboxs = document.all.item(myitem);
     if (checkboxs!=null)
     {
       for (i=0; i<checkboxs.length; i++)
          {
            if (checkboxs[i].type=="checkbox" && checkboxs[i].value==myvalue)
              {
                 checkboxs[i].checked = true
              }
          }
     }
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
  <table height="25" cellspacing="0" cellpadding="1" width="98%" align="center" border="1" class="tableCommon">
    <tbody>
      <tr>
        <td><lt:Label res="res.label.forum.inc.position" key="cur_position"/>
            <a href="<%=request.getContextPath()%>/forum/index.jsp">
            <lt:Label res="res.label.forum.inc.position" key="forum_home"/>
            </a>&nbsp;<b>&raquo;</b>&nbsp;<a href="<%=request.getContextPath()%>/usercenter.jsp">
            <lt:Label res="res.label.forum.menu" key="user_center"/>
          </a></td>
      </tr>
    </tbody>
  </table><br />
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="userservice" scope="page" class="com.redmoon.forum.person.userservice" />
<%
if (!privilege.isMasterLogin(request) && !privilege.isUserLogin(request)) {
	response.sendRedirect("door.jsp");
	return;
}
com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();

String privurl = request.getRequestURL()+"?"+request.getQueryString();

String RegName="",Question="",Answer="";
String RealName="",Career="";
String Gender="",Job="";
int BirthYear = 0;
int BirthMonth = 0;
int BirthDay = 0;
Date Birthday = null;
String Phone="",Mobile="";
int Marriage = 0;
String State="",City="",Address="";
String PostCode="",IDCard="",RealPic="",sign="";
String Email="",OICQ="";
String Hobbies="",myface="";

String name = "";
if (privilege.isMasterLogin(request)) {
	name = ParamUtil.get(request, "userName");
}
if (name.equals("")) {
	name = privilege.getUser(request);
}

try {
	com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "userName", name, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

UserDb user = new UserDb();
user = user.getUser(name);

RegName = user.getNick();
Question = user.getQuestion();
Answer = user.getAnswer();
RealName = user.getRealName();
Career = user.getCareer();
Gender = StrUtil.getNullStr(user.getGender());
Job = user.getJob();
Birthday = user.getBirthday();
if (Birthday!=null) {
	Calendar cld = Calendar.getInstance();
	cld.setTime(Birthday);
	BirthYear = cld.get(Calendar.YEAR);
	BirthMonth = cld.get(Calendar.MONTH)+1;
	BirthDay = cld.get(Calendar.DAY_OF_MONTH);
	
	//BirthYear = Birthday.getYear()+1900;
	//BirthMonth = Birthday.getMonth()+1;
	//BirthDay = Birthday.getDate();
}
Marriage = user.getMarriage();
Phone = user.getPhone();
Mobile = user.getMobile();
State = user.getState();
City = user.getCity();
Address = user.getAddress();
PostCode = user.getPostCode();
IDCard = user.getIDCard();
RealPic = user.getRealPic();
Hobbies = user.getHobbies();
Email = user.getEmail();
OICQ = user.getOicq();
sign = user.getSign();
myface = user.getMyface();
%>

<form method="post" action="myinfo_do.jsp"  name="frmAnnounce" onSubmit="return VerifyInput()">
  <table width=100% class="tableCommon">
  	<thead>
    <tr>
      <td colspan="4" align=center><lt:Label res="res.label.regist" key="nick_pwd"/>
          <lt:Label res="res.label.regist" key="notice"/>
      </td>
    </tr>
	</thead>
    <tr>
      <td width="14%" height="28" align="left" >&nbsp;
          <lt:Label res="res.label.forum.user" key="RegName"/></td>
      <td height="28" colspan="3" align="left" valign="middle"><%if (user.isCanRename()) {%>
          <input name="nick" value="<%=StrUtil.toHtml(RegName)%>">
          <%}else{%>
          <%=RegName%>
          <input name="nick" value="<%=StrUtil.toHtml(RegName)%>" type="hidden">
          <%}%>
          <input type="hidden" name="RegName" size="20" value="<%=name%>" /></td>
    </tr>
    <tr>
      <td height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="Password"/></td>
      <td height="28" colspan="3" align="left"><input name="Password" type="password" autocomplete="off" size="20" maxlength="20" />
          <font color="#FF0000">*</font>
          <lt:Label res="res.label.forum.user" key="Password2"/>
        &nbsp;
        <input name="Password2" type="password" autocomplete="off" size="20" maxlength="20" />
        <font color="#FF0000"> *&nbsp;</font><font color=red>
          <lt:Label res="res.label.myinfo" key="not_fill_not_change_pwd"/>
        </font></td>
    </tr>
    <tr>
      <td height="28" align="left" valign="middle">&nbsp;
          <lt:Label res="res.label.forum.user" key="Gender"/></td>
      <td height="28" colspan="3" align="left" valign="middle"><input type=radio name=Gender value=M <%=Gender.equals("M")?"checked":""%>>
          <lt:Label res="res.label.forum.user" key="man"/>
          <input type=radio name=Gender value=F <%=Gender.equals("F")?"checked":""%>>
          <lt:Label res="res.label.forum.user" key="woman"/></td>
    </tr>
    <tr>
      <td height="28" align="left" valign="middle">&nbsp;&nbsp;Email</td>
      <td height="28" colspan="3" align="left" valign="middle"><input name="Email" type="text"  value="<%=Email%>" size="20" maxlength="50" />
          <font color="#FF0000">*</font> </td>
    </tr>
    <tr>
      <td height="28" align="left" >&nbsp;
          <lt:Label res="res.label.forum.user" key="Question"/></td>
      <td height="28" colspan="3" align="left" ><input name="Question" type="text"  value="<%=Question%>" size="20" maxlength="50" />
          <lt:Label res="res.label.forum.user" key="Answer"/>
        &nbsp;&nbsp;
        <input name="Answer" type="text"  value="<%=Answer%>" size="20" maxlength="50" />
        &nbsp;
        <%
			UserPropDb upd = new UserPropDb();
			upd = upd.getUserPropDb(user.getName());				
			if (cfg.getBooleanProperty("forum.isBkMusic")) {
			%>
        <lt:Label res="res.label.forum.user" key="is_music_autostart"/>
        <%}%>
        <%
			if (cfg.getBooleanProperty("forum.isBkMusic")) {
			%>
        <select name="is_music_autostart">
          <option value="1">是</option>
          <option value="0">否</option>
        </select>
        <script>
				frmAnnounce.is_music_autostart.value="<%=upd.getInt("is_music_autostart")%>";
			    </script>
        <%}else{%>
        <input name="is_music_autostart" type="hidden" value="<%=upd.getInt("is_music_autostart")%>">
        <%}%></td>
    </tr>
    <%
if (cfg.getBooleanProperty("forum.isFactionUsed")) {
%>
    <tr>
      <td height="28" align="left" >&nbsp;
          <lt:Label res="res.label.forum.user" key="faction"/></td>
      <td height="28" colspan="3" align="left" ><select name="faction">
          <option value="">
            <lt:Label key="wu"/>
            </option>
          <%
			FactionDb fd = new FactionDb();
			fd = fd.getFactionDb(FactionDb.CODE_ROOT);
			FactionView fv = new FactionView(fd);
			fv.ShowDirectoryAsOptionsWithCode(out, fd, fd.getLayer());
			%>
        </select>
          <script language="JavaScript">
				<!--
				frmAnnounce.faction.value="<%=StrUtil.getNullStr(upd.getString("faction"))%>"
				//-->
				</script></td>
    </tr>
    <%}%>
    <tr>
      <td colspan="4">
	  <%
	  String checked = "";
	  if (user.isSecret()) {
		checked = "checked";
	  }
	  %>
	  <input name="isSecret" value="true" <%=checked%> type="checkbox">
	  <lt:Label res="res.label.forum.user" key="secret"/>
	  </td>
    </tr>
    <tr>
      <td align="left" height="28" >&nbsp;
          <lt:Label res="res.label.forum.user" key="RealName"/></td>
      <td width="25%" height="28" valign="middle" ><input name=RealName type=text  value="<%=RealName%>" size=12 maxlength=20></td>
      <td width="22%" height="28" align="left" >&nbsp;
          <lt:Label res="res.label.forum.user" key="Career"/></td>
      <td width="39%" height="28" align="left" ><select name=Career size=1>
          <option value="0" selected>
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
          <script language="JavaScript">
					<!--
					frmAnnounce.Career.value="<%=Career%>"
					//-->
				</script>
      </td>
    </tr>
    <tr>
      <td height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="Mobile"/></td>
      <td height="28">
	  <%
	  boolean isMobileValidate = cfg.getBooleanProperty("forum.isMobileValidate");
	  String readonly = "";
	  if (!privilege.isMasterLogin(request)) {
	  	if (isMobileValidate)
			readonly = "readonly";
	  }
	  %>
        <input name=Mobile type=text value="<%=Mobile%>" size=16 maxlength="16" <%=readonly%>>
            <%
			if (isMobileValidate && SMSFactory.isUseSMS()) {
				if (!user.isMobileValid()) {
			%>
				<input type="button" value="验证号码" onClick="window.location.href='user_mobile_validate1.jsp'">
			<%
				}
				else {
				%>
				<input type="button" value="修改号码" onClick="window.location.href='user_mobile_validate1.jsp'">
				<%
				}
				%>
                <input name="button" type="button" onclick="frmAnnounce.Mobile.value=''" value="清除" /></td>
				<%
			}
			%>		
      <td height="28">&nbsp;
          <lt:Label res="res.label.forum.user" key="Job"/></td>
      <td height="28" class="l15"><select name=Job size=1>
          <option value="0" selected>
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
          <script>
				  frmAnnounce.Job.value = "<%=Job%>"
				  </script>
      </td>
    </tr>
    <tr>
      <td height="28" align="left" >&nbsp;
          <lt:Label res="res.label.forum.user" key="birthday"/></td>
      <td height="28"><input name=BirthYear type=text  value="<%=BirthYear%>" size=5>
        年
        <input name=BirthMonth type=text  value="<%=BirthMonth%>" size=2>
        月
        <input name=BirthDay type=text  value="<%=BirthDay%>" size=2>
        日</td>
      <td height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="marry_status"/></td>
      <td height="28" class="l15"><select name=Marriage size=1>
          <option value="0" selected>
            <lt:Label res="res.label.forum.user" key="marry_not"/>
            </option>
          <option value="2">
            <lt:Label res="res.label.forum.user" key="marry_not_know"/>
            </option>
          <option value="1">
            <lt:Label res="res.label.forum.user" key="marry_yes"/>
            </option>
        </select>
          <script language="JavaScript">
					<!--
					frmAnnounce.Marriage.value="<%=Marriage%>";
					//-->
					</script>
        *</td>
    </tr>
    <tr>
      <td height="28" align="left">&nbsp;
          <lt:Label res="res.label.forum.user" key="phone"/></td>
      <td height="28" valign="middle"><input name=Phone type=text  value="<%=Phone%>" size=16 maxlength="20"></td>
      <td height="28">&nbsp;QQ</td>
      <td height="28" class="l15"><input name=OICQ type=text  value="<%=OICQ%>" size=16 maxlength="15">
        *</td>
    </tr>
    <tr>
      <td height="28" align=left><img src=/images/c.gif width=1 height=5>&nbsp;
          <lt:Label res="res.label.forum.user" key="State"/></td>
      <td valign="middle" height="28"><select id="province_id" name="province_id" onchange="ajaxShowCityCountry(this.value, '')">
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
      <script>
	  o("province_id").value = "<%=user.getProvinceId()%>";
	  </script>
      </td>
      <td colspan="2" rowspan="6" align="left" valign="middle"><table width="98%"  border="0">
          <tr>
            <td align="left"><font color="#FF0000"><b><font color="#000000"></font></b> <img src="forum/images/face/<%=RealPic.equals("") ? "face.gif" : RealPic%>" name="tus">&nbsp;
                  <script>function showimage(){document.images.tus.src="forum/images/face/"+document.frmAnnounce.RealPic.options[document.frmAnnounce.RealPic.selectedIndex].value;}</script>
                  <%
 String path = Global.getRootPath() + "/forum/images/face/";
 FileViewer fileViewer = new FileViewer(cn.js.fan.web.Global.getAppPath(request) + "/forum/images/face/");
 fileViewer.init();
 int i = 1;
%>
                  <select id="RealPic" name="RealPic" size=1 onChange="showimage()">
                    <option value="face.gif">
                      <lt:Label res="res.label.forum.user" key="default_icon"/>
                    </option>
                    <% while(fileViewer.nextFile()){
							  if (fileViewer.getFileName().lastIndexOf("gif") != -1 || fileViewer.getFileName().lastIndexOf("jpg") != -1 || fileViewer.getFileName().lastIndexOf("png") != -1 || fileViewer.getFileName().lastIndexOf("bmp") != -1 && fileViewer.getFileName().indexOf("face") != -1) {
							   String fileName = fileViewer.getFileName();
							%>
                    <option value="<%=fileName%>"><%=i++%></option>
                    <% }
							} %>
                  </select>
                  <a href="JavaScript:New('images/index.jsp')">
                    <lt:Label res="res.label.forum.user" key="view_all_icon"/>
                  </a> </font>
                <script language="JavaScript">
						  <!--
						  frmAnnounce.RealPic.value = "<%=RealPic%>"
						  //-->
						  </script>
            </td>
            <td width="31%" rowspan="2" valign="top"><font color="#FF0000">
              <%if (!myface.equals("")) {%>
              <img src="<%=URLDecoder.decode(user.getMyfaceUrl(request), "UTF-8")%>">
              <%}%>
            </font></td>
          </tr>
          <tr>
            <td><iframe src="addmyface.jsp" width=100% height="95" frameborder="0" scrolling="no"></iframe></td>
          </tr>
      </table></td>
    </tr>
    <tr>
      <td align=left height="28" >&nbsp;
          <lt:Label res="res.label.forum.user" key="City"/></td>
      <td height="28" valign="middle"><select id="city_id" name="city_id" onchange="if (this.value!='') ajaxShowCityCountry('',this.value)">
        <%
	  int cityId = user.getCityId();
	  if (cityId!=0) {
		  crd = (com.redmoon.forum.tools.ChinaRegionDb)crd.getQObjectDb(new Integer(cityId));
		  irRegion = crd.list(crd.getTable().getSql("listCity"), new Object[]{new Integer(crd.getInt("parent_id"))}).iterator();
		  while (irRegion.hasNext()) {
			crd = (com.redmoon.forum.tools.ChinaRegionDb)irRegion.next();
		  %>
		  	<option value="<%=crd.getInt("region_id")%>"><%=crd.getString("region_name")%></option>
		  <%}%>
      <%}%>
    </select>
      <%if (cityId!=0) {%>
      <script>
      o("city_id").value = "<%=user.getCityId()%>";
      </script>
      <%}%>
      <select id="country_id" name="country_id">
      <%
	  int countryId = user.getCountryId();
	  if (countryId!=0) {
		  crd = (com.redmoon.forum.tools.ChinaRegionDb)crd.getQObjectDb(new Integer(countryId));
		  irRegion = crd.list(crd.getTable().getSql("listCountry"), new Object[]{new Integer(crd.getInt("parent_id"))}).iterator();
		  while (irRegion.hasNext()) {
			crd = (com.redmoon.forum.tools.ChinaRegionDb)irRegion.next();
		  %>
		  	<option value="<%=crd.getInt("region_id")%>"><%=crd.getString("region_name")%></option>
		  <%}%>
      <%}%>
      </select> 
      <%if (countryId!=0) {%>
      <script>
      o("country_id").value = "<%=user.getCountryId()%>";
      </script>
      <%}%>     
      </td>
    </tr>
    <tr>
      <td align=left height="28" >&nbsp;
          <lt:Label res="res.label.forum.user" key="Address"/></td>
      <td height="28" valign="middle"><input name=Address type=text  value="<%=Address%>" size=30 maxlength="100"></td>
    </tr>
    <tr>
      <td align=left height="28" >&nbsp;
          <lt:Label res="res.label.forum.user" key="PostCode"/></td>
      <td height="28" valign="middle"><input name=PostCode type=text  value="<%=PostCode%>" size=10 maxlength="30"></td>
    </tr>
    <tr>
      <td align=left height="28" >&nbsp;
          <lt:Label res="res.label.forum.user" key="IDCard"/></td>
      <td height="28" valign="middle"><input name=IDCard type=text  value="<%=IDCard%>" size=21></td>
    </tr>
    <tr>
      <td align=left height="37" >&nbsp;
          <lt:Label res="res.label.forum.user" key="Hobbies"/></td>
      <td valign="middle"><input name=Hobbies type=text value="<%=Hobbies%>" size=30 maxlength="50"></td>
    </tr>
    <tr>
      <td align=left height="37" >&nbsp;
          <lt:Label res="res.label.forum.user" key="home"/></td>
      <td colspan="3" valign="middle" ><input name=home type=text value="<%=user.getHome()%>" size=30 maxlength="50"></td>
    </tr>
    <tr>
      <td align=left height="37" >&nbsp;&nbsp;MSN</td>
      <td colspan="3" valign="middle" ><input name=msn type=text value="<%=user.getMsn()%>" size=30 maxlength="50"></td>
    </tr>
    <tr>
      <td align=left height="37" >&nbsp;
          <lt:Label res="res.label.forum.user" key="fetion"/></td>
      <td colspan="3" valign="middle" ><input name=fetion type=text value="<%=user.getFetion()%>" size=30 maxlength="50"></td>
    </tr>
    <tr>
      <td align=left height="29" >&nbsp;
          <lt:Label res="res.label.forum.user" key="locale"/></td>
      <td height="29" colspan="3" valign="middle"><select name=locale size=1>
          <option value="" selected>
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
          <script>
			frmAnnounce.locale.value = "<%=user.getLocale()%>";
			</script>
      </td>
    </tr>
    <tr>
      <td align=left height="29" >&nbsp;
          <lt:Label res="res.label.forum.user" key="timeZone"/></td>
      <td height="29" colspan="3" valign="middle"><select name="timeZone">
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
        </select>
          <script>
				frmAnnounce.timeZone.value = "<%=user.getTimeZone().getID()%>";
				</script>
      </td>
    </tr>
    <tr>
      <td align=left valign="middle" >&nbsp;
          <lt:Label res="res.label.forum.user" key="sign"/>
          <br>
          <br>
        &nbsp;&nbsp;UBB：
        <%
				  if (cfg.getBooleanProperty("forum.sign_ubb"))
				  	out.print(SkinUtil.LoadString(request, "res.label.forum.user", "support"));
				  else
				  	out.print(SkinUtil.LoadString(request, "res.label.forum.user", "not_support"));
				  %></td>
      <td colspan="3" valign="middle"><%
			int level = cfg.getIntProperty("forum.signUserLevel");
			if (user.getUserLevelDb().getLevel()>=level) {
			%>
          <%@ include file="forum/inc/getubb.jsp"%>
          <br>
          <textarea cols="75" name="Content" rows="5" wrap="VIRTUAL" title="<lt:Label res="res.label.forum.user" key="sign"/>"><%=sign%></textarea>
          <font color="#000000"><br>
          <lt:Label res="res.label.forum.user" key="sign_limit_count"/>
          <%=cfg.getIntProperty("forum.sign_length")%></font>
          <%}else{
				UserLevelDb uld = new UserLevelDb();
				uld = uld.getUserLevelDbByLevel(level);
			%>
        &nbsp;&nbsp;<%=StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.user", "signUserLevel"), new Object[] {uld.getDesc()})%>
        <textarea name="Content" style="display:none"><%=sign%></textarea>
        <%}%>
      </td>
    </tr>
    <tr>
      <td colspan="4"><table border=0 cellpadding=0 cellspacing=0 width=100%>
          <tr valign=bottom>
            <td height=41 align="center" valign="middle"><font color="#FF0000">&nbsp; </font><br>
                <input name=Write type=submit  value="<lt:Label key="ok"/>">
              &nbsp;&nbsp;&nbsp;&nbsp;
              <input name=reset type=reset  value="<lt:Label key="reset"/>">
              <br></td>
          </tr>
      </table></td>
    </tr>
  </table>
</form>
</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
<SCRIPT>
function VerifyInput() {
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