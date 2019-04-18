<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.integration.cwbbs.*"%>
<%@ page import="java.util.*"%>
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
<title>添加用户</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="../inc/livevalidation_standalone.js"></script>
<script>
function New(para_URL){var URL=new String(para_URL);window.open(URL,'','resizable,scrollbars')}

var errFunc = function(response) {
    //alert('Error ' + response.status + ' - ' + response.statusText);
	jAlert(response.responseText,"提示");
}

function doCheckNameDone(response) {
	var ret = response.responseText;
	o("checkNameResult").innerHTML = ret;
}

function checkName(name) {
	o("checkNameResult").innerHTML = "";
	var str = "op=name&name=" + name;
	var myAjax = new cwAjax.Request( 
		"user_check.jsp", 
		{ 
			method:"post", 
			parameters:str, 
			onComplete:doCheckNameDone,
			onError:errFunc
		}
	);
}

function doCheckAccountDone(response) {
	var ret = response.responseText;
	$("checkAccountResult").innerHTML = ret;
}

function chkAccount() {
	o("checkAccountResult").innerHTMl = "";
	var str = "op=account&account=" + o("account").value;
	var myAjax = new cwAjax.Request( 
		"user_check.jsp", 
		{ 
			method:"post",
			parameters:str, 
			onComplete:doCheckAccountDone,
			onError:errFunc
		}
	);
}

function selectNode(code, name) {
	o("deptCode").value = code;
	o("deptName").value = name;
}

function selDept() {
	openWin("../dept_sel.jsp", 450, 400, "yes");
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%@ include file="user_list_inc_menu_top.jsp"%>
<script>
o("menu3").className="current";
</script>
<div class="spacerH"></div>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<form method="post" action="user_add_do.jsp" enctype="MULTIPART/FORM-DATA" name="memberform" onsubmit="return memberform_onsubmit()">
  <table width=100% border=0 cellpadding=0 cellspacing=0 class="tabStyle_1">
                <tr>
                  <td colspan="4" align=left class="tabStyle_1_title">添加用户</td>
                </tr>
                <tr> 
                  <td width="120" align=left>用户名</td>
                  <td align=left><input type=text name="name" size=20 onchange="checkName(this.value)">
				  <script>
                    var name = new LiveValidation('name');
                    name.add(Validate.Presence);		
                  </script>                  
                  <span id="checkNameResult"></span></td>
                  <td colspan="2" rowspan="8" align=left>
                  <input name="photo" type="file" id="photo">
                  </td>
                </tr>
                <tr>
                  <td align=left>真实姓名 </td>
                  <td align=left><input type=text maxlength=8 name=RealName size=20 />
				  <script>
                    var RealName = new LiveValidation('RealName');
                    RealName.add(Validate.Presence);		
                  </script>                   
                  </td>
                </tr>
<tr>
      <td align=left>密码 </td>
      <td align=left><%
				  com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();				  
				  %>
        <input type=password name=Password size=20 value="<%=scfg.getInitPassword()%>" />
        <%
				  if (scfg.isForceChangeInitPassword()) {
				  	out.print("默认密码：" + scfg.getInitPassword());
				  }
				  %>
      <script>
                    var RealName = new LiveValidation('Password');
                    RealName.add(Validate.Presence);		
                    </script></td>
      </tr>
                <tr>
                  <td align=left>确认密码 </td>
                  <td align=left><input type=password name=Password2 size=20 value="<%=scfg.getInitPassword()%>" />
                  <script>
                    var RealName = new LiveValidation('Password2');
                    RealName.add(Validate.Presence);		
                    </script></td>
                </tr>
                <tr>
                  <td align=left>工号</td>
                  <td align=left><input id="account" name="account" onchange="chkAccount()" />
                  <span id="checkAccountResult"></span>
                  </td>
                </tr>
                <tr>
                  <td align=left>部门</td>
                  <td align=left><input id="deptName" name="deptName" readonly />
                    <input id="deptCode" name="deptCode" type="hidden" />
                    <script>
                    	var DeptName = new LiveValidation('deptName');
                    	DeptName.add(Validate.Presence);		
                    </script>
                    &nbsp;<a href="javascript:;" onclick="selDept()">选择</a></td>
                </tr>
                <tr align="left"> 
                  <td align="left">员工编号</td>
                  <td>
                  	<%
					String pn = "";
					com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
					if (cfg.getBooleanProperty("personNoAutoCreate")) {
						pn = UserDb.getNextPersonNo();
					}
					%>
                  	<input type="text" id="personNo" name="personNo" size="10" value="<%=pn%>" />
                    <%if (!License.getInstance().isGov()) {%>
                    <script>
                    var personNo = new LiveValidation('personNo');
                    personNo.add(Validate.Presence);			
                    </script>
                  <%}%>
                  <span id="checkPersonNoResult" style="color:red"></span>
                  </td>
                </tr>
                <tr align="left">
                  <td>KEY</td>
                  <td>
                  <font color="green"><div id="keyShow" sytle="BORDER: red 2px solid;BACKGROUND-COLOR: #ffffe1">当前用户未设置Key</div></font>
                  <input type='button' value="生成KEY" class="btn" onclick='addkey()'/>
                  <input type='button' value="清除KEY" class="btn" onclick='clearkey()'/>
                  <input id="keyId" name="keyId" type=hidden size="30" />
                  </td>
                </tr>
                <tr align="left" style="display:none">
                  <td align="left">职级</td>
                  <td><select name="rankCode">
                    <option value="">无</option>
                  </select></td>
                  <td colspan="2">
                  <%
					CWBBSConfig ccfg = CWBBSConfig.getInstance();
					if (ccfg.getBooleanProperty("isUse")) {
					%>
                    <input id="isCWBBS" name="isCWBBS" type="checkbox" value="1" />同时加为网站后台用户
					<%}
                  %>
                  </td>
                </tr>
                <tr> 
                  <td width=120 height="25" align=left> 
                    性别</td>
                  <td width="449" height="25"> <input type=radio name=gender value="<%=UserDb.GENDER_MAN%>" checked>
                    男 
                    <input type=radio name=gender value="<%=UserDb.GENDER_WOMAN%>">
                  女</td>
                  <td width=146 height="25"> 
                    婚姻状况</td>
                  <td height="25" class=stable><select name=Marriage size=1>
                    <option value="1">已婚</option>
                    <option value="0">未婚</option>
                  </select></td>
                </tr>
                <tr> 
                  <td width=120 height="28" align=left> 
                    出生日期</td>
                  <td height="28">
					<jsp:useBean id="calsheet" scope="page" class="com.redmoon.oa.CalendarSheet"/>
				   <select name="BirthYear">
                      <option value="">请选择
                      <%
				int curyear = calsheet.getCurYear();
				int curmonth = calsheet.getCurMonth();
				int curday = calsheet.getCurDay();
				int monthdays = 31;//calsheet.getDays(curmonth,curyear);
				String isselected = "";
				for (int k=curyear-80; k<=curyear+50; k++) {
				%>
                      <option value="<%=k%>" <%=isselected%>><%=k%></option>
                      <%
					isselected = "";
				}
				%>
                    </select>
年
<select name="BirthMonth">
  <option value="">...
  <%
			  String v = "";
			  for (int k=1; k<=12; k++) {
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
  <option value="">...
  <%
			  for (int k=1; k<=monthdays; k++) {
				v = k+"";
			  %>
  <option value="<%=v%>" <%=isselected%>><%=k%></option>
  <%
			  	isselected = "";
			  }
			  %>
</select></td>
                  <td width=146 height="28"> 
                    QQ</td>
                  <td class=stable height="28"><input type=text name=QQ size=16 maxlength="15"></td>
                </tr>
                <tr> 
                  <td width=120 height="25" align=left> 
                    E-mail</td>
                  <td height="25"> <input type=text id="Email" name="Email" size=20 maxlength="50">
				  <script>
                    var Email = new LiveValidation('Email');
                    Email.add(Validate.Email);			
                  </script>                  
                  </td>
                  <td width=146 height="25">短号</td>
                  <td class=stable height="25"><input type=text name=MSN size=16 maxlength="15"></td>
                </tr>
                <tr>
                  <td height="25" align=left>身份证号码</td>
                  <td height="25"><input type=text name=IDCard size=30 />
                  <script>
                    var IDCard = new LiveValidation('IDCard');
                    IDCard.add(Validate.IdCardNo);			
                  </script>
                  </td>
                  <td height="25">&nbsp;</td>
                  <td class=stable height="25">&nbsp;</td>
                </tr>
                <tr> 
                  <td width=120 height="25" align=left> 
                    电话</td>
                  <td height="25"> <input type=text name=Phone size=16 maxlength="20"></td>
                  <td width=146 height="25"> 
                    手机号码</td>
                  <td class=stable height="25"> <input type=text name=mobile size=16 maxlength="16">                  </td>
                </tr>
                <%if (License.getInstance().isGov()) {%>
                <tr>
                  <td height="27" align=left>职务</td>
                  <td height="27"><input id="duty" name="duty" type="text" size="30" />
                  
                  </td>
                  <td height="27" align=left>政治面貌</td>
                  <td height="27"><input id="party" name="party" type="text" size="30" /></td>
                </tr>
                <tr>
                  <td height="27" align=left>地址</td>
                  <td height="27"><input type="text" name="Address" size="30" /></td>
                  <td height="27" align=left>&nbsp;</td>
                  <td height="27">&nbsp;</td>
                </tr>
                <tr>
                  <td height="27" align=left>个人简历 </td>
                  <td height="27" colspan="3"><textarea name="resume" cols="70" rows="8" id="resume"></textarea></td>
                </tr>
                <%}else{%>
                <tr> 
                  <td width=120 height="27" align=left>省份</td>
                  <td height="27"> <select name=State size=1>
                      <option value="0" selected>请选择…</option>
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
                    </select> </td>
                  <td height="27" align=left>城市</td>
                  <td height="27"><input type=text name=City size=10>                  </td>
                </tr>
                <tr>
                  <td height="36" align=left>地址</td>
                  <td height="36"><input type=text name=Address size=30>                  </td> 
                  <td height="36" align=left>邮政编码</td>
                  <td height="36"><input type=text name=postCode size=10>                  </td>
                </tr>
                <tr>
                  <td height="25" align=left>兴趣爱好</td>
                  <td height="25">              
                  <input name="Hobbies" type=text size="30" /></td> 
                  <td>&nbsp;</td>
                  <td>&nbsp;</td>
                </tr>
                <%}%>
                <tr>
                  <td colspan="4" align=center>
                  <input class="btn" type=submit id="write" name="Write" value=" 提 交 " />
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                  <input class="btn" type=reset name=reset value=" 重 填 " /></td>
                </tr>
  </table>
</form>
<embed id="s_simnew61"  type="application/npsyunew6-plugin" hidden="true"> </embed><!--创建firefox,chrome等插件-->
</body>
<SCRIPT>
function memberform_onsubmit()
{
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
	memberform.action += "?name=" + encodeURIComponent(memberform.name.value)  + "&deptCode=" + memberform.deptCode.value+ "&account=" + memberform.account.value;
	o("write").disabled = true;
	return true;
}

function checkPersonNo(pn) {
	$.ajax({
		type: "post",
		url: "user_check.jsp",
		data: {
			op: "personNo",
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
			jAlert(XMLHttpRequest.responseText,"提示");
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
			name: ''
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
			alert("生成KEY发生异常！");
		}
	});	
}
$(function() {
	$('#personNo').bind('input propertychange', function() {
		checkPersonNo(this.value);
	});
});
</SCRIPT>
</html>