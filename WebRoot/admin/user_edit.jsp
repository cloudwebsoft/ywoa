<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.account.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>编辑用户信息</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<style>
.photoImg {
	width:230px;
}
</style>
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../inc/livevalidation_standalone.js"></script>
<script>
function New(para_URL){var URL=new String(para_URL);window.open(URL,'','resizable,scrollbars')}

function check_checkbox(myitem,myvalue){
     var checkboxs = document.all.item(myitem);
	 var myary = myvalue.split("|");
     if (checkboxs!=null)
     {
       for (i=0; i<checkboxs.length; i++)
          {
            if (checkboxs[i].type=="checkbox" )
              {
				for (k=0; k<myary.length; k++) {
				 if (checkboxs[i].value==myary[k])
	                 checkboxs[i].checked = true
				}
              }
          }
     }
}
</script>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<%
String username = ParamUtil.get(request, "name");
if (!SecurityUtil.isValidSqlParam(username)) {
	out.print(StrUtil.jAlert("参数非法！","提示"));
	return;
}

if (!privilege.canAdminUser(request, username)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

UserMgr um = new UserMgr();
UserDb user = um.getUserDb(username);
if (user==null || !user.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该用户已不存在！","提示"));
	return;
}
UserSetupDb usd = new UserSetupDb().getUserSetupDb(user.getName());
%>
<%@ include file="user_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<form method="post" action="user_edit_do.jsp?name=<%=StrUtil.UrlEncode(user.getName())%>" enctype="MULTIPART/FORM-DATA" name="memberform" onsubmit="return memberform_onsubmit()">
      <table class="tabStyle_1" width=100% border=0 cellpadding=2 cellspacing=0>
                <tr>
                  <td colspan="4" align=left class="tabStyle_1_title">人员信息 </td>
                </tr>
                <tr> 
                  <td width="13%" align=left>用户名&nbsp;&nbsp;&nbsp;&nbsp;</td>
                  <td width="37%"><%=user.getName()%>
                    <input type=hidden name="name" size=20 value="<%=user.getName()%>"></td>
                  <td colspan="2" rowspan="8" align="center" valign="center" id="photoTd">
                  <%if (!"".equals(user.getPhoto())) {
                  		
                  %>
                 	 <img class="photoImg" src="<%=request.getContextPath()%>/img_show.jsp?path=<%=user.getPhoto() %>" style="width:130px" />
                  
                  <%
                  	}else{
                  %>
                  <script>
					$(function() {
						<%if (user.getGender()==0) {%>
						  $("#photoTd").css('background', 'url(../images/man.png) center center no-repeat');
						<%}else{%>
						  $("#photoTd").css('background', 'url(../images/woman.png) center center no-repeat');
						<%}%>
					});
				  </script>
				  <%} %>
                  </td>
                </tr>
                <tr> 
                  <td align="left"> 密码</td>
                  <td><input type=password name=Password size=20 onkeyup="checkPwd(this.value)">
                    <font color="#FF0000"><span id="checkResult"></span>（如不需更改密码，则不用填写） </font> </td>
                </tr>
                <tr>
                  <td align="left"> 确认密码</td>
                  <td><input type=password name=Password2 size=20 />                  </td>
                </tr>
                <tr>
                  <td height="22" align=left>启用帐号</td>
                  <td height="22" align=left><select name=isValid size=1>
                  <option value="1" selected>是</option>
                      <option value="0">否</option>
                  </select> <script language="javascript">
					<!--
					memberform.isValid.value = "<%=user.getValid()%>"
					//-->
					</script> 
                  &nbsp;&nbsp; </td>
                </tr>
                <tr> 
                  <td height="22" align=left> 
                    真实姓名
                  </td>
                  <td height="22" align=left>
                  <input type=text name=RealName size=12 maxlength=8 value="<%=user.getRealName()%>">
				  <script>
                    var RealName = new LiveValidation('RealName');
                    RealName.add(Validate.Presence);
                  </script>
                  </td>
                </tr>
                <tr>
                  <td height="22" align=left>工号</td>
                  <td height="22" align=left><%
				  	AccountDb acc = new AccountDb();
					acc = acc.getUserAccount(user.getName());
					String userAccount = "";
					if (acc!=null && acc.isLoaded()) {
						userAccount = acc.getName();
					}
				  %>
                  <input type='hidden' name="account" value="<%=userAccount%>" /><%=userAccount%></td>
                </tr>
                <tr>
                  <td height="22" align=left>员工编号</td>
                  <td height="22" align=left>
                  <input type="text" id="personNo" name="personNo" size="10" value="<%=user.getPersonNo()%>" />
                  <script>
                    // var personNo = new LiveValidation('personNo');
                    // personNo.add(Validate.Presence);		
                  </script>
                  <span id="checkPersonNoResult" style="color:red"></span>                    
                    </td>
                </tr>
                <tr>
                   <td>KEY</td>
                  <td>
                   <font color="green"><div id="keyShow" sytle="BORDER: red 2px solid;BACKGROUND-COLOR: #ffffe1">
                       <%
                       if("".equals(StrUtil.getNullString(usd.getKeyId())))
                       {%>
                            当前用户未设置Key
                       <%}
                       else
                       {%>
                         <%=StrUtil.getNullString(usd.getKeyId())%>
                       <%}
                       %> 
                   </div></font>
                   <input type='button' value="生成KEY" class="btn" onclick='addkey()'/>
                   <input type='button' value="清除KEY" class="btn" onclick='clearkey()'/>
                   <input id="keyId" name="keyId" type=hidden size="30" value="<%=StrUtil.getNullString(usd.getKeyId())%>"/>
                  </td>
                </tr>
                <tr>
                  <td height="22" align=left>性别</td>
                  <td height="22" align=left><% 
				  String isM = "";
				  String isF="";
				  if (user.getGender()==0)
				  	isM = "checked";
				  else
				  	isF = "checked";
				  %> <input type=radio name=gender value=0 <%=isM%>>
                    男 
                    <input type=radio name=gender value=1 <%=isF%>>
                  女</td>
                  <td id="photoTd" width="12%" align="left" valign="center">照片</td>
                  <td id="photoTd" width="38%" align="left" valign="center"><input name="photo" type="file" id="photo" /></td>
                </tr>
                <tr> 
                  <td width=13% height="28" align=left>云盘份额&nbsp;</td>
                  <td width="37%" height="28"><input name="diskSpaceAllowed" value="<%=user.getDiskSpaceAllowed()%>" />
字节 </td>
                  <td width="12%" height="28" align="left">内部邮箱份额</td>
                  <td height="28" class=stable>
                  <input name="msgSpaceAllowed" value="<%=usd.getMsgSpaceAllowed()%>" />
字节</td>
                </tr>
                <tr> 
                  <td width=13% height="28" align=left> 
                    出生日期</td>
                  <td height="28"> 
				  <%
				Date bd = user.getBirthday();
				String y="",m="",d="";
				if (bd!=null) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(bd);
					y = "" + cal.get(cal.YEAR);
					m = "" + (cal.get(cal.MONTH) + 1);
					d = "" + cal.get(cal.DAY_OF_MONTH);
				}
				  %>
					<jsp:useBean id="calsheet" scope="page" class="com.redmoon.oa.CalendarSheet"/>
                    <select name="BirthYear">
					<option value="">请选择</optoin>
                      <%
				int curyear = calsheet.getCurYear();
				int curmonth = calsheet.getCurMonth();
				int curday = calsheet.getCurDay();
				int monthdays = 31;// calsheet.getDays(curmonth,curyear);
				String isselected = "";
				for (int k=curyear-80; k<=curyear+50; k++) {
					if ((k+"").equals(y))
						isselected = "selected";
				%>
                      <option value="<%=k%>" <%=isselected%>><%=k%></option>
                      <%
					isselected = "";
				}
				%>
                    </select>
年
<select name="BirthMonth">
<option value="">...</optoin>
  <%
			  String v = "";
			  for (int k=1; k<=12; k++) {
			  	if ((k+"").equals(m))
					isselected = "selected";
				v = k+"";
			  %>
  <option value="<%=v%>" <%=isselected%>><%=k%></option>
  <%
			  	isselected = "";
			  }
			  %>
</select>
月
<select name="BirthDay">
<option value="">...</optoin>
  <%
			  for (int k=1; k<=monthdays; k++) {
			  	if ((k+"").equals(d))
					isselected="selected";
				v = k+"";
			  %>
  <option value="<%=v%>" <%=isselected%>><%=k%></option>
  <%
			  	isselected = "";
			  }
			  %>
</select>
日 &nbsp;</td>
                  <td width="12%" height="28" align="left">QQ</td>
                  <td height="28" align="left" class=stable><input type=text name=QQ size=16 maxlength="15" value="<%=user.getQQ()%>">                  </td>
                </tr>
                <tr> 
                  <td width=13% height="25" align=left> 
                    E-mail</td>
                  <td height="25">
                  <input type=text id="Email" name="Email" size=20 maxlength="50" value="<%=StrUtil.getNullString(user.getEmail())%>">
				  <script>
                    var Email = new LiveValidation('Email');
                    Email.add(Validate.Email);			
                  </script>
                  </td>
                  <td width=12% height="25" align="left">短号</td>
                  <td class=stable height="25"><input type=text name=MSN size=16 maxlength="16" value="<%=StrUtil.getNullString(user.getMSN())%>"></td>
                </tr>
                <tr> 
                  <td width=13% height="25" align=left> 
                    电话</td>
                  <td height="25"> <input type=text name=Phone size=16 maxlength="20" value="<%=StrUtil.getNullString(user.getPhone())%>">                  </td>
                  <td width=12% height="25" align="left"> 
                    手机号码</td>
                  <td class=stable height="25">
                  <input type=text id='mobile' name=mobile size=16 maxlength="16" value="<%=user.getMobile()%>" onchange="checkMobile(this.value)">
                  <script>
                  var mobile = new LiveValidation('mobile');
                  mobile.add(Validate.Mobile);	
                   </script>
                   <span id="checkMobileResult"></span> 
                  <input type="hidden" name="RealPic" value="1">
                  </td>
                </tr>
                <tr>
                  <td height="27" align=left>身份证号码</td>
                  <td height="27"><input type=text name=IDCard size=30 value="<%=StrUtil.getNullString(user.getIDCard())%>">
				  <script>
                    var IDCard = new LiveValidation('IDCard');
                    IDCard.add(Validate.IdCardNo);			
                  </script>
                  </td>
                  <td align="left">婚姻状况</td>
                  <td>
                  <select name=Marriage size=1>
                      <option value="" selected>请选...</option>
                      <option value="0">已婚</option>
                      <option value="1">未婚</option>
                    </select>
					<script language="JavaScript">
					<!--
					memberform.Marriage.value="<%=user.getMarriaged()%>";
					//-->
					</script>
                    </td>
                </tr>
                <%if (License.getInstance().isGov()) {%>
                <tr>
                  <td height="27" align=left>职务</td>
                  <td height="27"><input id="duty" name="duty" type="text" size="30" value="<%=user.getDuty()%>" />
                  
                  </td>
                  <td height="27" align=left>政治面貌</td>
                  <td height="27"><input id="party" name="party" type="text" size="30" value="<%=user.getParty()%>" /></td>
                </tr>
                <tr>
                  <td height="27" align=left>地址</td>
                  <td height="27"><input type="text" name="Address" size="30" value="<%=StrUtil.getNullString(user.getAddress())%>" /></td>
                  <td height="27" align=left>&nbsp;</td>
                  <td height="27">&nbsp;</td>
                </tr>
                <tr>
                  <td height="27" align=left>个人简历 </td>
                  <td height="27" colspan="3"><textarea name="resume" cols="70" rows="8" id="resume"><%=user.getResume()%></textarea></td>
                </tr>
                <%}else{%>                
                <tr> 
                  <td width=13% height="27" align=left>省份</td>
                  <td height="27"> <select name=State size=1>
                      <option value="" selected>请选择…</option>
                      <option value="安徽">安徽</option>
                      <option value="北京">北京</option>
                      <option value="重庆">重庆</option>
                      <option value="福建">福建</option>
                      <option value="甘肃">甘肃</option>
                      <option value="广东">广东</option>
                      <option value="广西">广西</option>
                      <option value="贵州">贵州</option>
                      <option value="海南">海南</option>
                      <option value="河北">河北</option>
                      <option value="黑龙江">黑龙江</option>
                      <option value="河南">河南</option>
                      <option value="湖北">湖北</option>
                      <option value="湖南">湖南</option>
                      <option value="内蒙古">内蒙古</option>
                      <option value="江苏">江苏</option>
                      <option value="江西">江西</option>
                      <option value="吉林">吉林</option>
                      <option value="辽宁">辽宁</option>
                      <option value="宁夏">宁夏</option>
                      <option value="青海">青海</option>
                      <option value="山西">山西</option>
                      <option value="陕西">陕西</option>
                      <option value="山东">山东</option>
                      <option value="上海">上海</option>
                      <option value="四川">四川</option>
                      <option value="天津">天津</option>
                      <option value="西藏">西藏</option>
                      <option value="新疆">新疆</option>
                      <option value="云南">云南</option>
                      <option value="浙江">浙江</option>
                      <option value="其他">其他</option>
                    </select> <script language="JavaScript">
					<!--
					memberform.State.value="<%=StrUtil.getNullString(user.getState())%>"
					//-->
					</script> </td>
                  <td width="12%" align="left">城市
                  <br></td>
                  <td><input type=text name=City size=10 value="<%=StrUtil.getNullString(user.getCity())%>"></td>
                </tr>
                <tr> 
                  <td width=13% height="25" align=left>地址</td>
                  <td height="25"><input type=text name=Address size=30 value="<%=StrUtil.getNullString(user.getAddress())%>"></td>
                  <td width="12%" align="left">邮政编码</td>
                  <td><input type=text name=postCode size=10 value="<%=user.getPostCode()%>"></td>
                </tr>
                <tr> 
                  <td width=13% align=left><img src="images/c.gif" width=1 height=8>兴趣爱好</td>
                  <td><input type=text size="30" name="Hobbies" value="<%=user.getHobbies()%>" ></td>
                  <td></td>
                  <td>                 
                  <br /></td>
                </tr>
                <%}%>
                <tr>
                  <td colspan="4" align=center><input class="btn" type=submit name=Write value="确定" />
                  <!--
                  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				  <input class="btn" type=reset name=reset value="返回" onclick="window.location.href='user_list.jsp'" />
                  -->
                  </td>
                </tr>
              </table>
</form>
<embed id="s_simnew61"  type="application/npsyunew6-plugin" hidden="true"> </embed><!--创建firefox,chrome等插件-->
</body>
<SCRIPT>
function memberform_onsubmit()
{
	var mobileCheck = $("#checkMobileResult").text();
	var r = mobileCheck.match(/手机号已注册/i);  
	if (r!=null){
		return false; 
	}
	if (memberform.RealName.value=="") {
		jAlert("请输入用户姓名","提示");
		return false;
	}
	if (memberform.Password.value != memberform.Password2.value)
	{
		jAlert("两遍输入的口令不一致","提示");
		memberform.Password.focus();
		return false;
	}
	if (<%=user.getDiskSpaceUsed()%> > memberform.diskSpaceAllowed.value)
	{
		jAlert("网盘份额必须大于已用网盘份额","提示");
		memberform.diskSpaceAllowed.focus();
		return false;
	}
	if (<%=usd.getMsgSpaceUsed()%> > memberform.msgSpaceAllowed.value)
	{
		jAlert("内部邮箱份额必须大于已用内部邮箱份额","提示");
		memberform.msgSpaceAllowed.focus();
		return false;
	}
	return true;
}

function checkPwd(pwd) {
		$.ajax({
			type: "post",
			url: "../user/user_edit_do.jsp",
			data: {
				op: "checkPwd",
				pwd: pwd
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
			},
			success: function(data, status){
				data = $.parseJSON(data);
				o("checkResult").innerHTML = data.msg;
			},
			complete: function(XMLHttpRequest, status){
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});	
}

function checkPersonNo(pn) {
	$.ajax({
		type: "post",
		url: "user_check.jsp",
		data: {
			op: "personNoExcept",
			userName: "<%=username%>",
			personNo: pn
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		success: function(data, status){
			data = $.parseJSON(data);
			o("checkPersonNoResult").innerHTML = data.msg;
		},
		complete: function(XMLHttpRequest, status){
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});
}

var digitArray = new Array('0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f');
function toHex( n ) {
 var result = ''
 var start = true;
 for ( var i=32; i>0; ) {
         i -= 4;
         var digit = ( n >> i ) & 0xf;
         if (!start || digit != 0) {
                 start = false;
                 result += digitArray[digit];
         }
 }
 return ( result == '' ? '0' : result );
}

function addkey()
{
    var s_simnew;
	try
	{
	   if(navigator.userAgent.indexOf("MSIE") > 0 && !navigator.userAgent.indexOf("opera") > -1) {
	      s_simnew = new ActiveXObject("Syunew6A.s_simnew6");
	   }
	   else 
	   {
	      s_simnew = document.getElementById('s_simnew61');
	   }
	}
	catch(e)
	{
	  jAlert("请先安装KEY驱动包！","提示");
	  return;
	}
	var DevicePath = s_simnew.FindPort(0);
    var keyId=toHex(s_simnew.GetID_1(DevicePath))+toHex(s_simnew.GetID_2(DevicePath));
    if(keyId=="ffffffd2ffffffd2"||keyId=="ffffffa4ffffffa4")
    {
      jAlert("请先插入KEY！","提示");
	  return;
    }
    //检测keyId是否被别人使用
    checkKeyIsUsed(keyId);
}
function clearkey()
{
   $('#keyId').val('');
   $('#keyShow').html('当前用户未设置Key');
}
//检测用户信息
function checkKeyIsUsed(keyid)
{
  //检测
  $.ajax({
		url: "user_add_checkkey.jsp",
		async:true,
		data: {
			keyid: keyid,
			name: "<%=username%>",
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			// $('#bodyBox').showLoading();
		},
		success: function(data, status){
            // 过滤掉其它字符，只保留JSON字符串
			var m = data.match(/\{.*?\}/gi);
			if (m!=null) {
				if (m.length==1) {
					data = m[0];
				}
			}
			try {
				data = jQuery.parseJSON(data);
			}
			catch(e) {
				// 异常出错处理
				jAlert("生成KEY发生异常！","提示");
				return;
			}
			if (data==null)
			{
			  jAlert("生成KEY发生异常！","提示");
			  return;
			}
			data.msg = data.msg.replace(/\\r/ig, "<BR>");
			 if (data.ret=="0") {
				 $('#keyId').val(keyid);
				 $('#keyShow').html(keyid);
			} else {
			    jAlert(data.msg,"提示");//KEY被人占用
			}
		},
		complete: function(XMLHttpRequest, status){
			// $('#bodyBox').hideLoading();				
		},
		error: function(XMLHttpRequest, textStatus){
			jAlert("生成KEY发生异常3！","提示");
		}
	});	
}
$(function() {
	$('#personNo').bind('input propertychange', function() {
		checkPersonNo(this.value);
	});
});

//校验手机号唯一性
function checkMobile(mobile) {
	o("checkMobileResult").innerHTML = "";
	o("mobile").value=mobile;
	var str = "op=mobile&mobile=" + mobile;
	var myAjax = new cwAjax.Request( 
		"<%=request.getContextPath() %>/admin/user_check.jsp", 
		{ 
			method:"post", 
			parameters:str, 
			onComplete:doCheckMobileDone,
			onError:errFunc
		}
	);
}
function doCheckMobileDone(response) {
	var ret = response.responseText;
	o("checkMobileResult").innerHTML = ret;
}
var errFunc = function(response) {
    //alert('Error ' + response.status + ' - ' + response.statusText);
	jAlert(response.responseText,"提示");
}
</SCRIPT>
</html>