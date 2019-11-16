<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.account.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.FileViewer"%>
<%@ page import="com.redmoon.oa.kernel.*"%>
<%@page import="com.redmoon.oa.pvg.RoleDb"%>
<%@page import="com.redmoon.oa.post.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="calsheet" scope="page" class="com.redmoon.oa.CalendarSheet"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>用户编辑本人信息</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
<script src="../inc/common.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/js/datepicker/jquery.datetimepicker.css"/>
<script src="<%=request.getContextPath() %>/js/datepicker/jquery.datetimepicker.js"></script>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String username = privilege.getUser(request);
if (!SecurityUtil.isValidSqlParam(username)) {
	out.print(StrUtil.jAlert("参数非法！","提示"));
	return;
}
UserMgr um = new UserMgr();
UserDb user = um.getUserDb(username);
if (user==null || !user.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该用户已不存在！","提示"));
	return;
}

String op = ParamUtil.get(request, "op");
if ("restoreIcon".equals(op)) {
	user.setPhoto("");
	user.save();
}
%>
<script>
function New(para_URL) {
	var URL=new String(para_URL);
	window.open(URL,'','resizable,scrollbars');
}
function CheckRegName() {
	var Name=document.form.RegName.value;
	window.open("checkregname.jsp?RegName="+Name,"","width=200,height=20");
}

function check_checkbox(myitem,myvalue) {
	var checkboxs = document.all.item(myitem);
	var myary = myvalue.split("|");
	if(checkboxs!=null) {
		for (i=0; i<checkboxs.length; i++) {
			if (checkboxs[i].type=="checkbox" ) {
				for (k=0; k<myary.length; k++) {
					if (checkboxs[i].value==myary[k])
						checkboxs[i].checked = true;
				}
			}
		}
	}
}
function memberform_onsubmit()
{
	var mobileCheck = $("#checkMobileResult").text();
	var r = mobileCheck.match(/手机号已注册/i);  
	if (r!=null){
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
		jAlert("手机号已注册","提示");
		return false; 
	}
	if (memberform.RealName.value=="") {
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
		jAlert("请输入用户姓名","提示");
		return false;
	}
	if (memberform.Password.value != memberform.Password2.value)
	{
		$(".loading").css({"display":"none"});
		$(".treeBackground").css({"display":"none"});
		$(".treeBackground").removeClass("SD_overlayBG2");
		jAlert("两遍输入的口令不一致","提示");
		memberform.Password.focus();
		return false;
	}

	return true;
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
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
</head>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<div class="spacerH"></div>
<form action="user_edit_do.jsp?name=<%=StrUtil.UrlEncode(user.getName())%>" method="post" enctype="multipart/form-data" name="memberform" onsubmit="return memberform_onsubmit()">
<table width="764" class="tabStyle_1 percent98">
	<tr>
		<td colspan="4" class="tabStyle_1_title">帐号信息</td>
	</tr>
	<tr>
      <td align="left">我的角色</td>
      <td colspan="3"><%
		com.redmoon.oa.pvg.RoleDb[] rld = user.getRoles();
      	int num = 0;
		int rolelen = 0;
		if (rld!=null)
			rolelen = rld.length;
		String roleNames = "";
		for (int k=0; k<rolelen; k++) {
			if (rld[k].getCode().equals(RoleDb.CODE_MEMBER)) {
				if (++num == 2) {
					continue;
				}
			}
			if (roleNames.equals(""))
				roleNames = rld[k].getDesc() !=null ? rld[k].getDesc() : "";//防止rld[k].getDesc()为null报错问题
			else
				roleNames += "，" + (rld[k].getDesc() !=null ? rld[k].getDesc() : "");
		}
		out.print(roleNames);
		%>      
		</td>
    </tr>
    <%
    com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
    boolean isUseAccount = cfg.getBooleanProperty("isUseAccount");
    if (isUseAccount) {
    %>    
    <tr>
      <td width="224" align="left">我的工号</td>
      <td colspan="3">
		<%
			AccountDb ad = new AccountDb();
			ad = ad.getUserAccount(username);
			if (ad!=null) {
				out.print(ad.getName());
			}
		%>	  
		</td>
    </tr>    
    <%} %>
    <tr>
      <td align="left">我的部门</td>
      <td colspan="3"><%		
		DeptMgr dm = new DeptMgr();		
		DeptUserDb du = new DeptUserDb();	  
			Iterator ir2 = du.getDeptsOfUser(user.getName()).iterator();
			int kk = 0;
			while (ir2.hasNext()) {
				DeptDb dd = (DeptDb)ir2.next();
				String deptName = "";
				if (!dd.getParentCode().equals(DeptDb.ROOTCODE) && !dd.getParentCode().equals("-1")) {
					deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName() + "&nbsp;&nbsp;";
				}
				else
					deptName = dd.getName() + "&nbsp;&nbsp;";
				if (kk==0) {
					out.print(deptName);
				}
				else {
					out.print("<BR>" + deptName);
				}
				kk++;
			} 
			%>
		</td>
    </tr>
    <%
	String dis = "display:none";
	if (com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {
		dis = "";
	}
	%>    
    <tr style="<%=dis%>">
      <td align="left">我的领导</td>
      <td colspan="3">
      <%
                  UserSetupDb usd = new UserSetupDb(username);
                  String leaders = usd.getMyleaders();
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
                  <input id="leaderName" name="leaderName"  type=text readonly size=25 value="<%=leadersName %>" />
                  <input id="leaderCode" name="leaderCode" type="hidden" value="<%=leaders %>" />
                  <a href="javascript:;" onclick="openWin('../user_multi_sel.jsp', 800, 600)">&nbsp;&nbsp;&nbsp;选择</a>
      </td>
    </tr>
    <tr>
      <td width="224" align="left">我的岗位</td>
      <td colspan="3">
		<%
			PostUserMgr puMgr = new PostUserMgr();
			puMgr.setUserName(username);
			PostUserDb pudb = puMgr.postByUserName();
			if (pudb != null && pudb.isLoaded()) {
				PostDb pdb = new PostDb();
				pdb = pdb.getPostDb(pudb.getInt("post_id"));
				if (pdb != null && pdb.isLoaded()) {
					out.print(pdb.getString("name"));
				}
			}
		%>	  
		</td>
    </tr>
	<tr>
		<td width="119">用户名</td>
		<td colspan="3"><%=user.getName()%><input type="hidden" name="name" size=25 value="<%=user.getName()%>"><input type="hidden" name="isValid" value="<%=user.getValid()%>"></td>
	</tr>
	<tr style="display:none">
		<td>登录密码</td>
		<td colspan="3">
			<input type=password id="Password" name=Password size=20 onkeyup="checkPwd(this.value)">
			<font color="#FF0000"><span id="checkResult"></span>（如不需更改密码，则不用填写）</font><br /></td>
	</tr>
	<tr style="display:none">
	  <td>确认密码</td>
	  <td colspan="3"><input type=password id="Password2" name=Password2 size=20 />
	<script>
	var Password2 = new LiveValidation('Password2');
	Password2.add(Validate.Confirmation, { match: 'Password', failureMessage:'密码不匹配'} );	
	</script>
	  </td>
	  </tr>
    <tr>
		<td>真实姓名</td>
        <td colspan="3">
	  <input type=text name=RealName size=25 maxlength=8 value="<%=user.getRealNameRaw()%>" readonly>	
	  <script>
        var RealName = new LiveValidation('RealName');
        RealName.add(Validate.Presence);		
      </script>             	
			<input type="hidden" name="diskSpaceAllowed" value="<%=user.getDiskSpaceAllowed()%>">
		</td>
    </tr>
	<tr>
		<td colspan="4" class="tabStyle_1_title">个人资料</td>
    </tr>
    <tr>
        <td>性别</td>
        <td width="312">
<% 
	String isM = "";
	String isF="";
	if(user.getGender()==0)
		isM = "checked";
	else
		isF = "checked";
%>
			<input type=radio name=gender value=0 <%=isM%>>男
            <input type=radio name=gender value=1 <%=isF%>>女		</td>
		<td width="81">婚姻状况</td>
        <td width="232">
			<select name=Marriage size=1>
            	<option value="" selected>请选择</option>
                <option value="0">已婚</option>
                <option value="1">未婚</option>
            </select>
<script language="JavaScript">
<!--
	memberform.Marriage.value="<%=user.getMarriaged()%>";
-->
</script>		</td>
	</tr>
    <tr>
      <td>头像</td>
      <td colspan="3" id="photoTd">
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
				  <input name="photo" type="file" id="photo">
		  <a href="javascript:;" onclick="restoreIcon()">恢复默认</a>
		  <script>
			  function restoreIcon() {
				  jConfirm("您确定要恢复默认头像么", "提示", function (r) {
					  if (!r) {
						  return;
					  } else {
						  window.location.href = "user_edit.jsp?op=restoreIcon";
					  }
				  })
			  }
		  </script>
      </td>
      </tr>
    <tr>
    	<td>出生日期</td>
        <td>
        	<input type="text" name="birthday" id="birthday" size="25" value="<%=DateUtil.format(user.getBirthday(),"yyyy-MM-dd")%>"/>
        </td>
        <td>QQ</td>
        <td><input type=text name=QQ size=25 maxlength="15" value="<%=user.getQQ()%>"></td>
	</tr>
    <tr>
    	<td>E-mail</td>
        <td><input type=text id="Email" name=Email size=25 maxlength="50" value="<%=StrUtil.getNullString(user.getEmail())%>">
	<script>
	var Email = new LiveValidation('Email');
	Email.add(Validate.Email, {failureMessage:'Email格式错误'} );	
	</script>		
		</td>
        <td>短号</td>
        <td><input type=text name=MSN size=25 maxlength="30" value="<%=StrUtil.getNullString(user.getMSN())%>"></td>
	</tr>
    <tr>
		<td>电话</td>
		<td><input type=text name=Phone size=25 maxlength="20" value="<%=StrUtil.getNullString(user.getPhone())%>"></td>
		<td>手机</td>
		<td>
			<input type=text id="mobile" name=mobile size=25 maxlength="16" value="<%=user.getMobile()%>" onchange="checkMobile(this.value)">
	<script>
	var mobile = new LiveValidation('mobile');
    mobile.add(Validate.Mobile);	
	</script>
	<span id="checkMobileResult"></span> 
	<input type="hidden" name="personNo" value="<%=user.getPersonNo()%>" />
	<input type="hidden" name="rankCode" value="<%=user.getRankCode()%>" />
	<input type="hidden" name="type" value="<%=user.getType()%>" />
    </td>
	</tr>
    <tr>
	  <td></td><td></td>
      <td>身份证号码</td>
      <td><input id="IDCard" type=text name=IDCard size=25 value="<%=StrUtil.getNullString(user.getIDCard())%>">
    <script>
	var IDCard = new LiveValidation('IDCard');
	IDCard.add(Validate.IdCardNo, {});	
	</script></td>
      </tr>
	<%if (License.getInstance().isGov()) {%>
    <tr>
      <td height="27" align=left>职务</td>
      <td height="27">
      <input id="duty" name="duty" type="text" size="30" value="<%=user.getDuty()%>" />
      </td>
      <td height="25" align=left>政治面貌</td>
      <td height="25"><input id="party" name="party" type="text" size="30" value="<%=user.getParty()%>" /></td>
    </tr>
    <tr>
      <td height="27" align=left>个人简历 </td>
      <td height="27" colspan="3"><textarea name="resume" cols="70" rows="8" id="resume"><%=user.getResume()%></textarea></td>
    </tr>
    <%}else{%>        
    <tr>
		<td>省份</td>
		<td>
			<select name=State size=1>
				<option value="" selected>请选择</option>
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
			</select>
<script language="JavaScript">
<!--
	memberform.State.value="<%=StrUtil.getNullString(user.getState())%>"
//-->
</script>		</td>
		<td>城市</td>
		<td><input type=text name=City size=25 value="<%=StrUtil.getNullString(user.getCity())%>"></td>
	</tr>
	<tr>
		<td>地址/邮政地址</td>
		<td><input type=text name=Address size=25 value="<%=StrUtil.getNullString(user.getAddress())%>"></td>
		<td>邮政编码</td>
		<td><input type=text name=postCode size=25 value="<%=user.getPostCode()%>"></td>
	</tr>
	<tr>
		<td>兴趣爱好</td>
		<td><input type=text name=Hobbies size=25 value="<%=StrUtil.getNullString(user.getHobbies())%>">
		</td>
		<td>&nbsp;</td>
		<td></td>
    </tr>
    <%}%>
    <tr>
		<td colspan="4" align="center">
		<input name="party" value="<%=StrUtil.getNullStr(user.getParty()) %>" type="hidden"/>
		<input class="btn" type=submit value=" 确定 ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input class="btn" type=reset value=" 重 填 "></td>
    </tr>
</table>
</form>
</body>
<script>
var automaticOnSubmit = Password2.form.onsubmit;
Password2.form.onsubmit = function() {
	var valid = automaticOnSubmit();
	if(valid) {
		showLoading();	
		return true;
	}
	else
		return false;
}

function showLoading(){
	$(".treeBackground").addClass("SD_overlayBG2");
	$(".treeBackground").css({"display":"block"});
	$(".loading").css({"display":"block"});
}

function checkPwd(pwd) {
		$.ajax({
			type: "post",
			url: "user_edit_do.jsp",
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
				jAlert(XMLHttpRequest.responseText,"提示");
			}
		});	
}
$(function() {
	$('#birthday').datetimepicker({
	   	lang:'ch',
	   	timepicker:false,
	   	format:'Y-m-d',
	   	step:1
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
</script>
</html>