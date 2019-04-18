<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.Config" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.account.AccountDb" %>
<%@ page import="com.redmoon.oa.dept.DeptUserDb" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<%@ page import="com.redmoon.oa.dept.DeptMgr" %>
<%@ page import="org.json.JSONObject" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
	if ("invalidate".equals(op)) {
		JSONObject json = new JSONObject();
		String userNames = ParamUtil.get(request, "userNames");
		String[] ary = StrUtil.split(userNames, ",");
		if (ary==null) {
			json.put("ret", 0);
			json.put("msg", "请选择记录！");
			out.print(json.toString());
			return;
		}
		UserDb ud = new UserDb();
		for (String userName : ary) {
			ud = ud.getUserDb(userName);
			if (ud.isLoaded()) {
				ud.del();
			}
		}
		json.put("ret", 1);
		json.put("msg", "操作成功！");
		out.print(json.toString());
		return;
	}
%>
<%!
	// 设置错误信息
	public String setPrompt(String prompt,String msg){
		if (prompt.contains(msg))
			return prompt;
		if ("".equals(prompt))
			prompt = msg;
		else 
			prompt += ","+msg;
		return prompt;
	}
%>
<%
String priv = "admin.user";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String [][] info = (String[][])request.getSession().getAttribute("info");
String name = "";
String realName = "";
String mobile = "";
String gender = "";
String personNO = "";
String dept = "";
String prompt = "";
String roleName = "";

String accountName = "";
String postName = "";
String idCard = "";
String marriaged = "";
String email = "";
String birthday = "";
String QQ = "";
String phone = "";
String entryDate = "";
String shortMobile = "";
String hobbies = "";
String address = "";

JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = null;
List<List<String>> successList = new ArrayList<List<String>>();
List<List<String>> failList = new ArrayList<List<String>>();
HashMap<String, String> userMap = new HashMap<String, String>();
HashMap<String, String> cellMap = new HashMap<String, String>();
HashMap<String, String> noMap = new HashMap<String, String>();

HashMap<String, String> accountNameMap = new HashMap<String, String>();

//判断工号是否启用
Config cf = new Config();
boolean isUseAccount = cf.getBooleanProperty("isUseAccount");

for(int i=0;i<info.length;i++) {
	List<String> list = new ArrayList<String>();

	// 腾讯通格式：uid	name	deptname	rtxno	email	mobile	position	phone	fax	homepage	address	postcode	country	province	city	college	age	gender	birthday	bloodtype
	accountName = info[i][0];
	// 如果工号小于6位，则左补齐0，比如：000081会在导入时变为81
	if (accountName.length()<6) {
		accountName = StrUtil.PadString(accountName, '0', 6, true);
		info[i][0] = accountName;
	}
	name = accountName; // 帐户以工号临时替代，否则检查将无法通过，但在user_import_txt_confirm.jsp中首先置为了空
	realName = info[i][1];
	// System.out.println("name=" + name + " realName=" + realName + " len=" + info.length);
	// excel中可能会出现行为null或空，用格式刷后有可能出现这种情况
	if ((name==null && realName==null) || ("".equals(name) && "".equals(realName))) {
		continue;
	}

	dept = info[i][2];
	email = info[i][4];
	mobile = info[i][5];
	phone = info[i][7];
	address = info[i][10];
	gender = info[i][17];
	postName = ""; // info[i][11];

	// personNO = info[i][0]; // 用户编码以工号替代
	// roleName = info[i][7];
	// idCard = info[i][9];
	// marriaged = info[i][10];
	// birthday = info[i][11];
	// QQ = info[i][13];
	// entryDate = info[i][15];
	// shortMobile = info[i][16];
	// hobbies = info[i][17];

	System.out.println("name=" + name + " realName=" + realName + " email=" + info[i][4]);

	String nameCol = "";
	String realNameCol = "";
	String mobileCol = "";
	String personNOCol="";
	String deptCol = "";
	String accountNameCol = "";
	//帐号校验
	String sql = "select name from users where name="+StrUtil.sqlstr(name);
	if ("".equals(name)){
		prompt = setPrompt(prompt, "必填项内容为空");
		nameCol = "1";
	} else {
		if (name.length()>20){
			prompt = setPrompt(prompt, "帐号不能大于20个字符");
			nameCol = "1";
		} else {
			/*
			ri = jt.executeQuery(sql);
			if (ri.size()>0){
				prompt = setPrompt(prompt,"帐号已注册");
				nameCol = "1";
			}
			*/
		}
	}
	if (!nameCol.equals("1")) {
		if (userMap.containsKey(name)) {
			// 判断是否为同一个人
			if (!userMap.get(name).equals(realName)) {
				prompt = setPrompt(prompt, "导入文件中存在相同帐号");
				nameCol = "1";
			}
		} else {
			userMap.put(name, realName);
		}
	}
	//判断工号是否启用，如果启用进入判断，如果不启用不判断
	if (isUseAccount) {
		if (!accountNameCol.equals("1")) {
			if (accountNameMap.containsKey(accountName)) {
				// 判断是否为同一个人
				if (!accountNameMap.get(accountName).equals(name)) {
					prompt = setPrompt(prompt, "导入文件中存在相同工号");
					accountNameCol = "1";
				}
			} else {
				accountNameMap.put(accountName, name);
			}
		}
	}
	
	//姓名校验
	if ("".equals(realName)){
		prompt = setPrompt(prompt,"必填项内容为空");
		realNameCol = "1";
	} else {
		if (realName.length()>20){
			prompt = setPrompt(prompt,"姓名不能大于20个字符");
			realNameCol = "1";
		}
	}
	// 手机校验
	if (false && !"".equals(mobile)){
		java.util.regex.Pattern p = java.util.regex.Pattern.compile("^\\d{11}$");       
		java.util.regex.Matcher m = p.matcher(mobile);   
		if (!m.matches()){
			prompt = setPrompt(prompt,"手机号非法");
			mobileCol = "1";
		} else {
			/*
			此处不应从数据库中检查手机号，因为本次导入的人员虽可能与库中相同，但有可能是号码确实换了，只要这个手机号与本次导入的用户不冲突就可以了
			sql = "select mobile,realName from users where realName<>" + StrUtil.sqlstr(realName) + " and mobile = "+StrUtil.sqlstr(mobile);
			ri = jt.executeQuery(sql);
			if (ri.hasNext()){
				ResultRecord rr = (ResultRecord)ri.next();
				prompt = setPrompt(prompt,"手机号已被" + rr.getString(2) + "占用");
				mobileCol = "1";
			}
			*/
		} 
		if (!mobileCol.equals("1")) {
			if (cellMap.containsKey(mobile)) {
				// 判断是否为同一个人
				if (!cellMap.get(mobile).equals(name)) {
					prompt = setPrompt(prompt, "导入文件中存在相同手机号");
					mobileCol = "1";
				}
			} else {
				cellMap.put(mobile, name);
			}
		}
	}
	
	//员工编号校验
	if (false && !"".equals(personNO)){
		if (personNO.length()>20){
			prompt = setPrompt(prompt,"员工编号不能大于20个字符");
			personNOCol = "1";
		} else {
			sql = "select person_no from users where name<>" + StrUtil.sqlstr(name) + " and person_no = "+StrUtil.sqlstr(personNO);
			ri = jt.executeQuery(sql);
			if (ri.size()>0) {
				prompt = setPrompt(prompt,"员工编号已存在");
				personNOCol = "1";
			}
		}
		if (!personNOCol.equals("1")) {
			if (noMap.containsKey(personNO)) {
				// 判断是否为同一个人
				if (!noMap.get(personNO).equals(name)) {
					prompt = setPrompt(prompt, "导入文件中存在相同的人员编号");
					personNOCol = "1";
				}
			} else {
				noMap.put(personNO, name);
			}
		}
	}
	
	//部门校验
	if("".equals(dept)){
		prompt = setPrompt(prompt,"必填项内容为空");
		deptCol = "1";
	} else {
			/*String[] deptArr = dept.split("\\\\");
		if (deptArr.length>12){
			prompt = setPrompt(prompt,"部门不能超过12级");
			deptCol = "1";
		}*/
	}
	list.add(name);
	list.add(realName);
	list.add(accountName);
	list.add(mobile);
	list.add(gender);
	list.add(personNO);
	list.add(dept);
	list.add(prompt);
	list.add(nameCol);
	list.add(realNameCol);
	list.add(mobileCol);
	list.add(personNOCol);
	list.add(deptCol);
	list.add(roleName);
	
	list.add(postName);
	list.add(idCard);
	list.add(marriaged);
	list.add(birthday);
	list.add(email);
	list.add(QQ);
	list.add(phone);
	list.add(entryDate);
	list.add(shortMobile);
	list.add(hobbies);
	list.add(address);
	
	list.add(accountNameCol);
	
	if (!"".equals(prompt)){
		failList.add(list);
	} else {
		successList.add(list);
	}
	prompt = "";
}

// 将被停用的用户
List<List<String>> invalidList = new ArrayList<List<String>>();
UserDb ud = new UserDb();
AccountDb ad = new AccountDb();
DeptUserDb du = new DeptUserDb();
DeptMgr dm = new DeptMgr();
Iterator ir = ud.list().iterator();
while (ir.hasNext()) {
	ud = (UserDb)ir.next();
	if (ud.getName().equals("admin") || ud.getName().equals("system")) {
		continue;
	}

	ad = ad.getAccountDb(ud.getName());
	accountName = ad.getName();
	if (accountName.length()<6) {
		accountName = StrUtil.PadString(accountName, '0', 6, true);
	}
	realName = ud.getRealNameRaw();

	boolean isFound = false;
	for (int i=0; i<info.length; i++) {
		// 如果工号及姓名均相等
		// if (info[i][0].equals(accountName) && info[i][1].equals(realName)) {
		// 重名的也保留，以免工号有问题，被误删除
		if (info[i][1].equals(realName)) {
			isFound = true;
			break;
		}
	}
	if (!isFound) {
		List<String> list = new ArrayList<String>();
		list.add(ud.getName());
		list.add(ud.getRealName());
		list.add(accountName);
		list.add(ud.getMobile());
		list.add(ud.getGender()==0?"男":"女");
		list.add(ud.getPersonNo());

		Iterator ir2 = du.getDeptsOfUser(ud.getName()).iterator();
		int k = 0;
		String depts = "";
		while (ir2.hasNext()) {
			DeptDb dd = (DeptDb)ir2.next();
			String deptName = "";
			if (!dd.getParentCode().equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {
				deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName();
			}
			else
				deptName = dd.getName();
			if (k==0) {
				depts = deptName;
			}
			else {
				depts += "，" + deptName;
			}
			k++;
		}
		list.add(depts);
		invalidList.add(list);
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>用户导入</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/hopscotch/css/hopscotch.css" />
<script type="text/javascript" src="<%=request.getContextPath() %>/inc/common.js"></script>
<script src="<%=request.getContextPath()%>/js/jquery-1.9.1.min.js"></script>
<script src="<%=request.getContextPath()%>/js/jquery-migrate-1.2.1.min.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/skin/common/organize.css" />
<style>
td {overflow:hidden;}
</style>
</head>
<body>
<div class="oranize-number">
<!--最底层灰色条--><div class="oranize-number-linegray"></div>
<!--蓝色条1--><div class="oranize-number-lineblue1" ></div>
<!--灰色条2--><div  class="oranize-number-lineblue1 oranize-number-lineblue2"></div>

<!--1步--><div class="oranize-blue1">1</div>
<!--2步--><div class="oranize-blue2 oranize-roundness-blue2">2</div>
<!--3步--><div class="oranize-gray3">3</div>
<!--1步文字--><div class="oranize-txt1 ">导入Excel</div>
<!--2步文字--><div class="oranize-txt2 oranize-txt-sel">确认信息</div>
<!--3步文字--><div class="oranize-txt3">完成</div>
</div>
<table border="0" align="center" cellspacing="0" class="tabStyle_1 percent80">
<tr>
<td>
您共填写<font color="red" ><%=info.length%></font>条数据，格式正确数据<font color="red" ><%=successList.size()%></font>条，格式错误数据<font color="red" ><%=failList.size()%></font>条。<%if (failList.size()>0){out.print("由于存在错误数据您需要修改员工资料后重新上传表格。");} %><br />
导入失败的原因包括：<br/>
1. 必填项内容为空  2. 手机号码已被占用  3. 手机号非法 4. 员工编号已存在 <br/>

</td>
</tr>
</table>
<span style="margin-left:10px;">格式正确数据<%=successList.size()%>条</span>
<table border="0" align="center" cellspacing="0" class="tabStyle_1" style="width:2000px;table-layout:fixed;">
	<thead>
		<tr>
			<th class="tabStyle_1_title" width="10%">帐号</th>
			<th class="tabStyle_1_title" width="10%">姓名</th>
			<th class="tabStyle_1_title" width="10%">工号</th>
			<th class="tabStyle_1_title" width="10%">手机</th>
			<th class="tabStyle_1_title" width="10%">性别</th>
			<th class="tabStyle_1_title" width="10%">员工编号</th>
			<th class="tabStyle_1_title" width="20%">部门</th>
			<th class="tabStyle_1_title" width="10%">角色</th>
			
			<th class="tabStyle_1_title" width="10%">岗位</th>
			<th class="tabStyle_1_title" width="10%">身份证</th>
			<th class="tabStyle_1_title" width="10%">婚否</th>
			<th class="tabStyle_1_title" width="10%">出生日期 </th>
			<th class="tabStyle_1_title" width="10%">E-mail</th>
			<th class="tabStyle_1_title" width="10%">QQ</th>
			<th class="tabStyle_1_title" width="10%">电话</th>
			<th class="tabStyle_1_title" width="10%">入职时间</th>			
			<th class="tabStyle_1_title" width="10%">短号</th>
			<th class="tabStyle_1_title" width="10%">兴趣爱好</th>
			<th class="tabStyle_1_title" width="20%">地址</th>
			<th class="tabStyle_1_title" width="10%">提示</th>
		</tr>
	</thead>
	<tbody>
		<%
			for(int i=0;i<successList.size();i++){
				List<String> list = successList.get(i);
				name = list.get(0);
				realName = list.get(1);
				accountName = list.get(2);
				mobile = list.get(3);
				gender = list.get(4);
				personNO = list.get(5);
				dept = list.get(6);
				roleName = list.get(7);
				
				postName = list.get(14);
				idCard = list.get(15);
				marriaged = list.get(16);
				birthday = list.get(17);
				email = list.get(18);
				QQ = list.get(19);
				phone = list.get(20);
				entryDate = list.get(21);
				shortMobile = list.get(22);
				hobbies = list.get(23);
				address = list.get(24);			
		%>
		<tr>
			<td><%=name %></td>
			<td><%=realName %></td>
			<td><%=accountName %></td>
			<td><%=mobile %></td>
			<td style="text-align:center"><%=gender %></td>
			<td><%=personNO %></td>
			<td><%=dept %></td>
			<td><%=roleName %></td>
			<td><%=postName %></td>
			<td><%=idCard %></td>
			<td style="text-align:center"><%=marriaged %></td>
			<td><%=birthday %></td>
			<td><%=email %></td>
			<td><%=QQ %></td>
			<td><%=phone %></td>
			<td><%=entryDate %></td>
			<td><%=shortMobile %></td>
			<td><%=hobbies %></td>
			<td><%=address %></td>
			<td align="center"><img width="16" src="<%=request.getContextPath()%>/skin/images/organize/icon-finish.png"/></td>
		</tr>
		<%
		}
		%>
	</tbody>
</table>
<br />
<div style="margin-left:10px;">格式错误数据<%=failList.size()%>条</div>
<table border="0" align="center" cellspacing="0" class="tabStyle_1" style="width:2000px;table-layout:fixed;">
	<thead>
		<tr>
			<th class="tabStyle_1_title"  width="10%">帐号</th>
			<th class="tabStyle_1_title"  width="10%">姓名</th>
			<th class="tabStyle_1_title" width="10%">工号</th>			
			<th class="tabStyle_1_title"  width="11%">手机</th>
			<th class="tabStyle_1_title"  width="10%" style="text-align:center">性别</th>
			<th class="tabStyle_1_title"  width="10%">员工编号</th>
			<th class="tabStyle_1_title"  width="20%">部门</th>
			<th class="tabStyle_1_title"  width="10%">角色</th>
			<th class="tabStyle_1_title"  width="10%">岗位</th>
			<th class="tabStyle_1_title" width="10%">身份证</th>
			<th class="tabStyle_1_title" width="10%">婚否</th>
			<th class="tabStyle_1_title" width="10%">出生日期 </th>
			<th class="tabStyle_1_title" width="10%">E-mail</th>
			<th class="tabStyle_1_title" width="10%">QQ</th>
			<th class="tabStyle_1_title" width="10%">电话</th>
			<th class="tabStyle_1_title" width="10%">入职时间</th>
			<th class="tabStyle_1_title" width="10%">短号</th>
			<th class="tabStyle_1_title" width="10%">兴趣爱好</th>
			<th class="tabStyle_1_title" width="20%">地址</th>			
			<th class="tabStyle_1_title"  width="10%">提示</th>
		</tr>
	</thead>
	<tbody>
	<tr></tr>
		<%
			for(int i=0;i<failList.size();i++){
				List<String> list = failList.get(i);
				name = list.get(0);
				realName = list.get(1);
				accountName = list.get(2);
				mobile = list.get(3);
				gender = list.get(4);
				personNO = list.get(5);
				dept = list.get(6);
				prompt = list.get(7);
				String nameCol = list.get(8);
				String realNameCol = list.get(9);
				String mobileCol = list.get(10);
				String personNOCol = list.get(11);
				String deptCol = list.get(12);
				roleName = list.get(13);	
				
				postName = list.get(14);
				idCard = list.get(15);
				marriaged = list.get(16);
				birthday = list.get(17);
				email = list.get(18);
				QQ = list.get(19);
				phone = list.get(20);
				entryDate = list.get(21);
				shortMobile = list.get(22);
				hobbies = list.get(23);
				address = list.get(24);		
				
				String accountNameCol = list.get(25);
		%>
		<tr> 
			<%if ("1".equals(nameCol)){ %>
			<td style="border:1px solid #F90"><%=name %></td>
		<%} else { %>
			<td><%=name %></td>
		<%};
			if ("1".equals(realNameCol)){ %>
			<td style="border:1px solid #F90"><%=realName %></td>
		<%} else { %>
			<td><%=realName %></td>
		<%}%>
		<%if ("1".equals(accountNameCol)){ %>
			<td style="border:1px solid #F90"><%=accountName %></td>
		<%} else { %>
			<td><%=accountName %></td>		
		<%} %>		
		<%if ("1".equals(mobileCol)){ %>
			<td style="border:1px solid #F90"><%=mobile %></td>
		<%} else { %>
			<td><%=mobile %></td>
		<%} %>
			<td style="text-align:center"><%=gender %></td>
		<%if ("1".equals(personNOCol)){ %>
			<td style="border:1px solid #F90"><%=personNO %></td>
		<%} else { %>
			<td><%=personNO %></td>
		<%};
		if ("1".equals(deptCol)){ %>
			<td style="border:1px solid #F90"><%=dept %></td>
		<%} else { %>
			<td><%=dept %></td>
		<%} %>
			<td><%=roleName %></td>
			<td><%=postName %></td>
			<td><%=idCard %></td>
			<td style="text-align:center"><%=marriaged %></td>
			<td><%=birthday %></td>
			<td><%=email %></td>
			<td><%=QQ %></td>
			<td><%=phone %></td>
			<td><%=entryDate %></td>
			<td><%=shortMobile %></td>
			<td><%=hobbies %></td>
			<td><%=address %></td>			
			<td><%=prompt %></td>
		</tr>
		<%
		}
		%>
	</tbody>
</table>
<div style="margin-left:10px;">将被停用的用户<%=invalidList.size()%>个 <input id="btnInvalidate" type="button" class="btn" value="停用" onclick="invaldUsers()" /> </div>
<table border="0" align="center" cellspacing="0" class="tabStyle_1" style="width:2000px;table-layout:fixed;">
	<thead>
	<tr>
		<th class="tabStyle_1_title"  width="3%">
			<input type="checkbox" value="1" onclick="if (this.checked) selAllCheckBox('userNames'); else deSelAllCheckBox('userNames')" />
		</th>
		<th class="tabStyle_1_title"  width="10%">帐号</th>
		<th class="tabStyle_1_title"  width="10%">姓名</th>
		<th class="tabStyle_1_title"  width="10%">工号</th>
		<th class="tabStyle_1_title"  width="11%">手机</th>
		<th class="tabStyle_1_title"  width="10%" style="text-align:center">性别</th>
		<th class="tabStyle_1_title"  width="7%">员工编号</th>
		<th class="tabStyle_1_title">部门</th>
	</tr>
	</thead>
	<tbody>
	<%
		for(int i=0;i<invalidList.size();i++){
			List<String> list = invalidList.get(i);
			name = list.get(0);
			realName = list.get(1);
			accountName = list.get(2);
			mobile = list.get(3);
			gender = list.get(4);
			personNO = list.get(5);
			dept = list.get(6);
	%>
	<tr id="tr<%=name%>">
		<td><input type="checkbox" id="userNames" name="userNames" value="<%=name%>"/></td>
		<td><%=name %></td>
		<td><%=realName %></td>
		<td><%=accountName %></td>
		<td><%=mobile %></td>
		<td><%=gender %></td>
		<td><%=personNO %></td>
		<td><%=dept %></td>
	</tr>
	<%
		}
	%>
	</tbody>
</table>
<table align="center">
	<tr>
	<td>
	<%if(failList.size()>0){ %>
	<input type="button"  value="上一步" onclick="previous()" class="org-btn"/>
	<%} else { %>
	<input type="button" value="上一步" onclick="previous()" class="org-btn"/>
	<input type="button" value="下一步" onclick="next()" class="org-btn" style="margin-left:100px;"/>
	<%} %>
	</td></tr>
</table>
</body>
<script type="text/javascript">
	// parent.hiddenLoading();
	function previous(){
		window.location.href = "<%=request.getContextPath()%>/admin/organize/user_import_txt.jsp";
	}
	
	function next(){
		// parent.showLoading();
		window.location.href = "<%=request.getContextPath()%>/admin/organize/user_import_txt_finish.jsp";
	}

	function selAllCheckBox(checkboxname){
		var checkboxboxs = document.getElementsByName(checkboxname);
		if (checkboxboxs!=null)
		{
			// 如果只有一个元素
			if (checkboxboxs.length==null) {
				checkboxboxs.checked = true;
			}
			for (i=0; i<checkboxboxs.length; i++)
			{
				checkboxboxs[i].checked = true;
			}
		}
	}
	function deSelAllCheckBox(checkboxname) {
		var checkboxboxs = document.getElementsByName(checkboxname);
		if (checkboxboxs!=null)
		{
			if (checkboxboxs.length==null) {
				checkboxboxs.checked = false;
			}
			for (i=0; i<checkboxboxs.length; i++)
			{
				checkboxboxs[i].checked = false;
			}
		}
	}
	
	function invaldUsers () {
		jConfirm('您确定要停用么？', '提示', function(r) {
			if (r) {
				$.ajax({
					type: "post",
					url: "user_import_txt_confirm.jsp",
					data: {
						op: "invalidate",
						userNames: getCheckboxValue('userNames')
					},
					dataType: "html",
					beforeSend: function(XMLHttpRequest) {
						// $('body').showLoading();
					},
					success: function(data, status){
						data = $.parseJSON(data);
						if (data.ret=="0") {
							jAlert(data.msg, "提示");
						}
						var ary = getCheckboxValue('userNames').split(",");
						for (var k in ary) {
							$('#tr' + ary[k]).remove();
						}
						jAlert(data.msg, "提示");
					},
					complete: function(XMLHttpRequest, status){
						// $('body').hideLoading();
					},
					error: function(XMLHttpRequest, textStatus){
						// 请求出错处理
						alert(XMLHttpRequest.responseText);
					}
				});
			}
		});
	}

</script>
</html>