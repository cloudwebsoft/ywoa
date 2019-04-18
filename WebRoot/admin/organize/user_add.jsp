<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.integration.cwbbs.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.sql.*"%>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@ page import="cn.js.fan.db.*" %>
<%@page import="org.json.JSONObject"%>
<jsp:useBean id="calsheet" scope="page" class="com.redmoon.oa.CalendarSheet"/>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
String op = ParamUtil.get(request,"op");
if ("checkName".equals(op)){
	String realName = request.getParameter("name");
	String name = "";
	String sql = "select name from users where isvalid=1 and name="+StrUtil.sqlstr(realName);
	JdbcTemplate jt = new JdbcTemplate();
	try{
		ResultIterator ri = jt.executeQuery(sql);
		if (ri.size()>0){
			for(int k=1;k<10;k++){
				sql = "select name from users where isvalid=1 and name="+StrUtil.sqlstr(realName+k);
				ri = jt.executeQuery(sql);
				if(ri.size()==0){
					name = realName+k;
					break;
				}
			}
		} else {
			name = realName;
		}
	} catch (SQLException e){
		LogUtil.getLog(getClass()).error(e.getMessage());
	}
	JSONObject json = new JSONObject();
	json.put("name",name);
	out.print(json.toString());
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>添加用户</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script type="text/javascript" src="<%=request.getContextPath() %>/inc/common.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery1.7.2.min.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="<%=request.getContextPath() %>/inc/livevalidation_standalone.js"></script>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/js/datepicker/jquery.datetimepicker.css"/>
<script src="<%=request.getContextPath() %>/js/datepicker/jquery.datetimepicker.js"></script>
<link href="<%=request.getContextPath() %>/js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
	<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery-showLoading/jquery.showLoading.js"></script>
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
	o("name").value=name;
	if (name.indexOf('\'') > -1) {
		o("checkNameResult").innerHTML = "<font color='red'>名称中不能含有 \'</font>";
		return;
	}
	var str = "op=name&name=" + name;
	var myAjax = new cwAjax.Request( 
		"<%=request.getContextPath() %>/admin/user_check.jsp", 
		{ 
			method:"post", 
			parameters:str, 
			onComplete:doCheckNameDone,
			onError:errFunc
		}
	);
}
// 校验手机号唯一性
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
function doCheckAccountDone(response) {
	var ret = response.responseText;
	$("checkAccountResult").innerHTML = ret;
}

function chkAccount() {
	o("checkAccountResult").innerHTMl = "";
	var str = "op=account&account=" + o("account").value;
	var myAjax = new cwAjax.Request( 
		"<%=request.getContextPath() %>/admin/user_check.jsp", 
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
	// parent.deptFrame.positionNode(code);                               //部门更改后，同步定位jstree节点
}

function selDept() {
	var deptCode = o("deptCode").value;
	openWin("organize_dept_sel.jsp?deptCode="+deptCode, 450, 400, "yes");
}


function setUsers(users, userRealNames) {
	o("leaderCode").value = users;
	o("leaderName").value = userRealNames;
}

function getSelUserNames() {
	return o("leaderCode").value;
}

function getSelUserRealNames() {
	return o("leaderName").value;
}

</script>
</head>
<body style="overflow-x:hidden;">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<div id="d" style="border-left:1px solid #DDD;height:100%;width:100%;">
<div class="spacerH"></div>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<form  method="post" action="<%=request.getContextPath() %>/admin/user_add_do.jsp" enctype="MULTIPART/FORM-DATA" name="memberform" onsubmit="return memberform_onsubmit()">
  <table width=100% border=0 cellpadding=0 cellspacing=0 class="tabStyle_1">
                <tr>
                  <td colspan="4" align=left class="tabStyle_1_title">填写信息</td>
                </tr>
                <tr>
                <td width="120" align=left>帐号</td>
                  <td align=left>
                  <input type=text name="name" maxlength=20 size=20 onchange="checkName(this.value)" />
				  <script>
                    var name = new LiveValidation('name');
                    name.add(Validate.Presence);	
                  </script>                  
                  <span id="checkNameResult"></span></td>
                  <td rowspan="3">头像</td>
                  <td colspan="1" rowspan="3" align=left>
                  <input name="photo" type="file" id="photo" />
                  </td>
                </tr>
                <tr> 
                  <td align=left>姓名</td>
                  <td align=left><input type=text maxlength=20 name=RealName size=20  />
				  <script>
                    var RealName = new LiveValidation('RealName');
                    RealName.add(Validate.Presence);	
                  </script>  
                 </td>
                </tr>
                <tr>
                  <td align=left>手机号 </td>
                  <td align=left><input type=text size=20 maxlength="16" name=mobile  onchange="checkMobile(this.value)"/>
                  <script>
                      var mobile = new LiveValidation('mobile');
                      mobile.add(Validate.Mobile);
                      <%
                      com.redmoon.weixin.Config weixinCfg = com.redmoon.weixin.Config.getInstance();
                      com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
                      if (weixinCfg.getBooleanProperty("isUse") || dingdingCfg.isUseDingDing()) {
                      %>
                      mobile.add(Validate.Presence);
                      <%
                      }
                      %>
                   </script>
                   <span id="checkMobileResult"></span>
                  </td>
                </tr>
				<tr>
     				<td align=left>密码 </td>
      				<td align=left><%
				  com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();				  
				  %>
        			<input type=password name=Password size=21 value="<%=scfg.getInitPassword()%>" />
        		  <%
				  if (scfg.isForceChangeInitPassword()) {
				  	out.print("默认密码：" + scfg.getInitPassword());
				  }
				  %>
      				<script>
                    var Password = new LiveValidation('Password');
                    Password.add(Validate.Presence);		
                    </script>
                    </td>
                    <td width=146 height="25"> 
                    	婚否
                    </td>
                  <td height="25" class=stable><select name=Marriage size=1>
                  	<option value="" selected>请选择</option>
                    <option value="1">已婚</option>
                    <option value="0">未婚</option>
                  </select></td>
     			 </tr>
                <tr>
                  <td align=left>确认密码 </td>
                  <td align=left><input type=password name=Password2 size=21 value="<%=scfg.getInitPassword()%>" />
	                  <script>
	                    var Password2 = new LiveValidation('Password2');
	                    Password2.add(Validate.Presence);		
	                    </script>
                   </td>
                   <td width=146 height="28"> 
                    QQ</td>
                  <td class=stable height="28"><input type=text name=QQ size=20 maxlength="15"></td>
                </tr>
                <tr>
                  <td align=left>部门</td>
                  <td align=left><input id="deptName" name="deptName"  type=text readonly size=20 />
                    <input id="deptCode" name="deptCode" type="hidden" />
                    <script>
                    	var DeptName = new LiveValidation('deptName');
                    	DeptName.add(Validate.Presence);		
                    </script>
                    &nbsp;<a href="javascript:;" onclick="selDept()">选择</a></td>
                    <td>入职日期</td>
                  <td>
				   <input type="text" id="entryDate" name="entryDate" size=20 />
                  </td>
                </tr>
                <tr>
                <tr>
                  <td align=left>类型</td>
                  <td align=left>
                    <select id="type" name="type">
                    <%
				  	SelectMgr sm = new SelectMgr();
        			SelectDb sd = sm.getSelect("user_type");
					Vector vType = sd.getOptions(new com.cloudwebsoft.framework.db.JdbcTemplate());
					Iterator irType = vType.iterator();
					while (irType.hasNext()) {
						SelectOptionDb sod = (SelectOptionDb) irType.next();
						String selected = "";
						if (sod.isDefault())
							selected = "selected";
						String clr = "";
						if (!sod.getColor().equals(""))
							clr = " style='color:" + sod.getColor() + "' ";
						out.print("<option value='" + sod.getValue() + "' " + selected + clr +
								   ">" + sod.getName() + "</option>");
					}
				  %>
                  </select>            
                  </td>
                  <td>&nbsp;</td>
                  <td></td>
                </tr>                
                <%if (com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {%>
                <td width=120 height="28" align=left> 
                  	他的领导
                   </td>
                  <td height="28">
                  <input id="leaderName" name="leaderName"  type=text readonly size=20 />
                    <input id="leaderCode" name="leaderCode" type="hidden" />
                    &nbsp;<a href="javascript:;" onclick="javascript:showModalDialog('../../user_multi_sel.jsp',window.self,'dialogWidth:800px;dialogHeight:600px;status:no;help:no;')">&nbsp;&nbsp;&nbsp;选择</a>
				  </td>
				  <%} else { %>
				  <td width=120 height="28" align=left> 
                  	邮政编码
                   </td>
                  <td height="28">
				  <input type=text name=postCode size=20>
                </td>
				  <%} %>
                 <td height="27" align=left>地址</td>
                  <td height="27"><input type="text" name="Address" size="20" /></td>
                </tr>
                <tr> 
                  <td width=120 height="25" align=left> 
                    	性别
                  </td>
                  <td width="449" height="25"> <input type=radio name=gender value="<%=UserDb.GENDER_MAN%>" checked>
                  		  男 
                    <input type=radio name=gender value="<%=UserDb.GENDER_WOMAN%>">
                 		 女
                 </td>
<td height="36" align=left> 出生日期</td>
                  <td height="36"><input type="text" name="birthday" id="birthday" size=20 />
                  </td>
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
                  <td width=146 height="25">身份证号码</td>
                 <td height="25"><input type=text name=IDCard size=20 />
                  <script>
                    var IDCard = new LiveValidation('IDCard');
                    IDCard.add(Validate.IdCardNo);			
                  </script>
                  </td>
                </tr>
                 <tr> 
                  <td height="25" align=left>兴趣爱好</td>
                  <td height="25">              
                  <input name="Hobbies" type=text size="20" /></td> 
                   <td width=120 height="25" align=left> 
                    电话</td>
                  <td height="25"> <input type=text name=Phone size=20 maxlength="20"></td>
                </tr>
                <%
                com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
                String isShowKey = "";
                if (cfg.getBooleanProperty("usbKey")){
                	isShowKey = "";
                } else {
                	isShowKey = "none";
                }
                %>
                <tr> 
                  <td align="left">员工编号</td>
                  <td>
                  	<%
					String pn = "";
					if (cfg.getBooleanProperty("personNoAutoCreate")) {
						pn = UserDb.getNextPersonNo();
					}
					%>
                  	<input type="text" id="personNo" name="personNo" title="用于集成考勤机" size="20" value="<%=pn%>" />
                    <%if (!License.getInstance().isGov()) {%>
                    <script>
                    var personNo = new LiveValidation('personNo');
                    // personNo.add(Validate.Presence);			
                    </script>
                  <%}%>
                  <span id="checkPersonNoResult" style="color:red"></span>
                  </td>
                  <td align="left">短号</td>
                  <td>
                  	<input type=text name=MSN size=20 />
                  </td>
                </tr>
                <tr style="display:<%=isShowKey%>;">
                  <td align="left">KEY</td>
                  <td colspan="3">
                  <font color="green"><div id="keyShow" sytle="BORDER: red 2px solid;BACKGROUND-COLOR: #ffffe1">当前用户未设置Key</div></font>
                  <input type='button' value="生成KEY" class="btn" onclick='addkey()'/>
                  <input type='button' value="清除KEY" class="btn" onclick='clearkey()'/>
                  <input id="keyId" name="keyId" type=hidden size="30" />
                  </td>
                 </tr>
                <tr style="display:none;">
                	<td><input type="text" name="isPass"  value="1"/></td>
                </tr>
                <tr>
                  <td colspan="4" align=center>
                  <input class="btn" type=submit id="write" name="Write" value=" 提 交 " />
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                  <input class="btn" type=reset name=reset value=" 重 填 " /></td>
                </tr>
  </table>
</form>
<embed id="s_simnew61"  type="application/npsyunew6-plugin" hidden="true"> </embed><!--创建firefox,chrome等插件-->
</div>
</body>
<SCRIPT>
// 表单提交校验
function memberform_onsubmit()
{
	if (memberform.name.value.indexOf('\'') > -1) {
		o("checkNameResult").innerHTML = "<font color='red'>名称中不能含有 \'</font>";
		return false;
	}
	var mobileCheck = $("#checkMobileResult").text();
	var r = mobileCheck.match(/手机号已注册/i);  
	if (r!=null){
		return false; 
	}
	<%if (com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {%>
	memberform.action += "?name=" + encodeURIComponent(memberform.name.value)  + "&deptCode=" + memberform.deptCode.value + "&leaderCode=" + memberform.leaderCode.value;
	<%}else{%>
	memberform.action += "?name=" + encodeURIComponent(memberform.name.value)  + "&deptCode=" + memberform.deptCode.value;
	<%}%>
	parent.parent.showLoading();
	return true;
}

function checkPersonNo(pn) {
	$.ajax({
		type: "post",
		url: "<%=request.getContextPath() %>/admin/user_check.jsp",
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
		url: "<%=request.getContextPath() %>/admin/user_add_checkkey.jsp",
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
	 $('#entryDate').datetimepicker({
       	lang:'ch',
       	timepicker:false,
       	format:'Y-m-d',
       	step:1
       });
      $('#birthday').datetimepicker({
         	lang:'ch',
         	timepicker:false,
         	format:'Y-m-d',
         	step:1
         });
});
// 设置部门name和code
function setDeptCodeAndName(selectDeptCode,selectDeptName){
	if ("root"==selectDeptCode||selectDeptCode==undefined){
		selectDeptCode = "";
		selectDeptName = "";
	}
	$("#deptCode").val(selectDeptCode);
	$("#deptName").val(selectDeptName);
}
function checkUserName(name){
	$.ajax({
		type: "post",
		url: "user_add.jsp",
		data: {
			op: "checkName",
		  name: name
		},
		dataType: "json",
		beforeSend: function(XMLHttpRequest){
		},
		success: function(data, status){
			$("#name").val(data.name);
		},
		complete: function(XMLHttpRequest, status){
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			jAlert(XMLHttpRequest.responseText,"提示");
		}
	});
}
</SCRIPT>
</html>