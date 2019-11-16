<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.account.*"%>
<%@ page import="cn.js.fan.security.*"%>
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
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>编辑用户</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script type="text/javascript" src="<%=request.getContextPath() %>/inc/common.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery1.7.2.min.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="<%=request.getContextPath() %>/inc/livevalidation_standalone.js"></script>
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/js/datepicker/jquery.datetimepicker.css"/>
<script src="<%=request.getContextPath() %>/js/datepicker/jquery.datetimepicker.js"></script>
<script>
function New(para_URL){var URL=new String(para_URL);window.open(URL,'','resizable,scrollbars')}
var errFunc = function(response) {
    //alert('Error ' + response.status + ' - ' + response.statusText);
	jAlert(response.responseText,"提示");
}
function selectNode(code, name) {
	o("deptCode").value = code;
	o("deptName").value = name;
}
function selDept() {
	var deptCode = o("deptCode").value;
	openWin("<%=request.getContextPath() %>/admin/organize/organize_dept_sel.jsp?deptCode="+deptCode, 450, 400, "yes");
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
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<div class="spacerH"></div>
<%
if (!privilege.isUserPrivValid(request, "admin.user")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String username = ParamUtil.get(request, "name");
String selectDeptCode = ParamUtil.get(request, "selectDeptCode");    // 当前列表的部门
if (!SecurityUtil.isValidSqlParam(username)) {
	out.print(StrUtil.jAlert("参数非法！","提示"));
	return;
}
/*
if (!privilege.canAdminUser(request, username)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/
UserMgr um = new UserMgr();
UserDb user = um.getUserDb(username);
String deptName = "";
String deptCode = "";
DeptUserDb du = new DeptUserDb();
Vector deptVec = du.getDeptsOfUser(username);
Iterator deptIr = deptVec.iterator();
while (deptIr.hasNext()){
	DeptDb dd = (DeptDb)deptIr.next();
	if ("".equals(deptName)){
		deptName = dd.getName();
	} else {
		deptName += "," + dd.getName(); 
	}
	if ("".equals(deptCode)){
		deptCode = dd.getCode();
	} else {
		deptCode += "," + dd.getCode();
	}
}
if (user==null || !user.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该用户已不存在！","提示"));
	return;
}
UserSetupDb usd = new UserSetupDb().getUserSetupDb(user.getName());
%>
<div id="d" style="border-left:1px solid #DDD;">
<form method="post" action="<%=request.getContextPath()%>/admin/user_edit_do.jsp" enctype="MULTIPART/FORM-DATA" onsubmit="return memberform_onsubmit()" name="memberform" >
  <table width=100% border=0 cellpadding=0 cellspacing=0 class="tabStyle_1">
                <tr>
                  <td colspan="4" align=left class="tabStyle_1_title">填写信息</td>
                </tr>
                <tr>
                <td>帐号</td>
                <td><font color="green" width="150"><%=user.getName() %></font><input type="hidden" name="name" id="name" value="<%=user.getName() %>" />&nbsp;&nbsp;&nbsp;&nbsp;
                <a href="javascript:;" onclick="addTab('<%=user.getRealNameRaw()%>的权限', '<%=request.getContextPath()%>/admin/user_op.jsp?op=edit&name=<%=StrUtil.UrlEncode(user.getName())%>')">查看权限</a>
				<%
				com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
			    boolean isUseAccount = cfg.getBooleanProperty("isUseAccount");			
				AccountDb ad = new AccountDb();
				ad = ad.getUserAccount(user.getName());
				String aName = "";
				if (ad!=null) {
					aName = ad.getName();
					%>
					&nbsp;&nbsp;&nbsp;&nbsp;工号：<%=aName%>
					<%
				}				
				%>                
                </td>
                <td rowspan="3">头像</td>
                  <td colspan="1" rowspan="3" align=left id="photoTd">
                   <%if (!"".equals(user.getPhoto())) {
                  %>
                 	 <img class="photoImg" src="<%=request.getContextPath()%>/img_show.jsp?path=<%=user.getPhoto() %>" style="width:130px" />
                  
                  <%
                  	}else{
                  %>
                  <%if (user.getGender()==0) {%>
						<img class="photoImg" src="<%=request.getContextPath()%>/images/man.png" style="width:29px" />
						<%}else{%>
						<img class="photoImg" src="<%=request.getContextPath()%>/images/woman.png" style="width:29px" />
						<%}%>
				  <%} %>
				  <br/><br/>
                  <input name="photo" type="file" id="photo" />
                  </td>
                </tr>
                <tr>
                  <td align=left>姓名</td>
                  <td align=left>
                  <input type=text name=RealName maxlength=20 size=20 value="<%=user.getRealNameRaw() %>" />
				  <script>
                    var RealName = new LiveValidation('RealName');
                    RealName.add(Validate.Presence);
                  </script>
                 </td>
                </tr>
                <tr>
                  <td align=left>手机号 </td>
                  <td align=left><input type=text size=20 maxlength="16" name=mobile  value="<%=user.getMobile() %>"  onchange="checkMobile(this.value)"/>
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
      				<td align=left>
      				<input type=password name=Password size=21 onkeyup="checkPwd(this.value)">
                    <font color="#FF0000"><span id="checkResult"></span>（如不需更改，则不用填写） </font> </td>
                    </td>
                    <td width=146 height="25"> 
                    	婚否</td>
                  <td height="25" class=stable><select name=Marriage size=1>
                  	<option value="" selected>请选择</option>
                    <option value="1">已婚</option>
                    <option value="0">未婚</option>
                  </select>
                  <script language="JavaScript">
					<!--
					memberform.Marriage.value="<%=user.getMarriaged()%>";
					//-->
					</script>
                  </td>
     			 </tr>
                <tr>
                  <td align=left>确认密码 </td>
                  <td align=left>
                  <input type=password name=Password2 size=21 /> 
                   </td>
                   <td width=146 height="28"> 
                    QQ</td>
                  <td class=stable height="28"><input type=text name=QQ size=20 maxlength="15" value="<%=user.getQQ()%>"></td>
                </tr>
                <tr>
                  <td align=left>部门</td>
                  <td align=left><input id="deptName" name="deptName" value="<%=deptName %>" readonly size=20 />
                    <input id="deptCode" name="deptCode" type="hidden" value="<%=deptCode %>"/>
                    <script>
                    	var DeptName = new LiveValidation('deptName');
                    	DeptName.add(Validate.Presence);		
                    </script>
                    &nbsp;<a href="javascript:;" onclick="selDept()">选择</a></td>
                    <td>入职日期</td>
                  <td>
				   <input type="text" id="entryDate" name="entryDate" value="<%=DateUtil.format(user.getEntryDate(),"yyyy-MM-dd")%>" size=20/>
                  </td>
                </tr>
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
                    <script language="JavaScript">
					$('#type').val("<%=user.getType()%>");
					</script>                  
                  </td>
                  <td>&nbsp;</td>
                  <td></td>
                </tr>                
                <tr> 
                <%if (com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {%>
                <td width=120 height="28" align=left> 
                  	他的领导
                   </td>
                  <td height="28">
                  <%
                  String leaders = StrUtil.getNullStr(usd.getMyleaders());
                  String leadersName = "";
        		  if (!leaders.equals("")) {
        			  String[] leadersAry = StrUtil.split(leaders, ",");
        			  for (int i = 0; i < leadersAry.length; i++) {
        				  UserDb ud = new UserDb(leadersAry[i]);
        				  if (ud == null || !ud.isLoaded()) {
        					  continue;
        				  }
        				  leadersName += (leadersName.equals("") ? "" : ",") + ud.getRealName();
        			  }
        		  }
                  %>
                  <input id="leaderName" name="leaderName"  type=text readonly size=20 value="<%=leadersName %>" />
                    <input id="leaderCode" name="leaderCode" type="hidden" value="<%=leaders %>" />
                    &nbsp;<a href="javascript:;" onclick="openWin('../../user_multi_sel.jsp', 800, 600)">&nbsp;&nbsp;选择</a>
				  </td>
				  <%} else { %>
				  <td width=120 height="28" align=left> 
                  	邮政编码
                   </td>
                  <td height="28">
				  <input type=text name=postCode size=20 value="<%=user.getPostCode()%>">
                </td>
				  <%} %>
                 <td height="27" align=left>地址</td>
                  <td height="27"><input type="text" name="Address" size="20" value="<%=StrUtil.getNullString(user.getAddress())%>" /></td>
                </tr>
                <tr> 
                <td width=120 height="25" align=left> 
                    性别
                  </td>
                  <td width="449" height="25"> 
                  <%
                  String isM = "";
				  String isF="";
				  if (user.getGender()==0)
				  	isM = "checked";
				  else
				  	isF = "checked";
				  %> 
                  <input type=radio name=gender value="<%=UserDb.GENDER_MAN%>"  <%=isM%> >
                  		  男 
                  <input type=radio name=gender value="<%=UserDb.GENDER_WOMAN%>"  <%=isF%> >
                 		 女
                 </td>
                  <td width=120 height="28" align=left> 
                  	 出生日期
                   </td>
                  <td height="28">
                  <input type="text" name="birthday" id="birthday"  value="<%=DateUtil.format(user.getBirthday(),"yyyy-MM-dd")%>" size=20/>
				  </td>
                </tr>
                <tr> 
                  <td width=120 height="25" align=left> 
                    E-mail</td>
                  <td height="25"> <input type=text id="Email" name="Email" size=20 maxlength="50" value="<%=user.getEmail() %>">
				  <script>
                    var Email = new LiveValidation('Email');
                    Email.add(Validate.Email);			
                  </script>                  
                  </td>
                  <td width=146 height="25">身份证号码</td>
                 <td height="25"><input type=text name=IDCard size=20 value="<%=StrUtil.getNullString(user.getIDCard())%>" />
                  <script>
                    var IDCard = new LiveValidation('IDCard');
                    IDCard.add(Validate.IdCardNo);			
                  </script>
                  </td>
                </tr>
                 <tr> 
                  <td height="25" align=left>兴趣爱好</td>
                  <td height="25">              
                  <input name="Hobbies" type=text size="20" value="<%=user.getHobbies()%>" /></td> 
                   <td width=120 height="25" align=left> 
                    电话</td>
                  <td height="25"> <input type=text name=Phone size=20 maxlength="20" value="<%=StrUtil.getNullString(user.getPhone())%>" ></td>
                </tr>
                 <tr> 
                  <td width=13% height="28" align=left>云盘份额&nbsp;</td>
                  <td width="37%" height="28"><input name="diskSpaceAllowed" value="<%=user.getDiskSpaceAllowed()%>" size=20 />
字节 </td>
                  <td width="12%" height="28" align="left">内部邮箱份额</td>
                  <td height="28" class=stable>
                  <input name="msgSpaceAllowed" value="<%=usd.getMsgSpaceAllowed()%>" size=20 />
字节</td>
                </tr>
                <%
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
                  	<input type="text" id="personNo" name="personNo" title="用于集成考勤机" size="20" value="<%=user.getPersonNo()%>" />
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
                  <input type=text name=MSN size=20 value="<%=user.getMSN() %>" />
                  </td>
                </tr>
                <tr style="display:<%=isShowKey%>;">
                <td align="left">KEY</td>
                  <td colspan="3">
                  <font color="green"><div id="keyShow" sytle="BORDER: red 2px solid;BACKGROUND-COLOR: #ffffe1">当前用户未设置Key</div></font>
                  <input type='button' value="生成KEY" class="btn" onclick='addkey()'/>
                  <input type='button' value="清除KEY" class="btn" onclick='clearkey()'/>
                  <input id="keyId" name="keyId" type=hidden size="30" value="<%=StrUtil.getNullString(usd.getKeyId())%>" />
                  </td>
                </tr>
                <tr style="display:none;">
                <td><input type="text" name="isPass"  value="1"/></td>
                </tr>
                <tr>
                  <td colspan="4" align=center>
				 	<input name="party" value="<%=StrUtil.getNullStr(user.getParty()) %>" type="hidden"/>                  
                  <input class="btn" type=submit id="write" name="Write" value=" 提 交 " />
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                  <input class="btn" type=reset name=reset value=" 重 填 " /></td>
                </tr>
  </table>
</form>
</div>
</body>
<SCRIPT>
function memberform_onsubmit()
{
	var mobileCheck = $("#checkMobileResult").text();
	var r = mobileCheck.match(/手机号已注册/i);  
	if (r!=null){
		return false; 
	}
	// parent.parent.showLoading();  

	memberform.action += "?name=<%=StrUtil.UrlEncode(user.getName())%>&selectDeptCode=<%=selectDeptCode%>&deptCode=" + memberform.deptCode.value;
	
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
	   document.getElementById("d").style.height = parent.document.body.clientHeight+"px";
});
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
function checkPwd(pwd) {
		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/user/user_edit_do.jsp",
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
</SCRIPT>
</html>