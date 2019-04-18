<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="cn.js.fan.db.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%!
	//设置错误信息
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
JdbcTemplate jt = new JdbcTemplate();
ResultIterator ri = null;
List<List<String>> successList = new ArrayList<List<String>>();
List<List<String>> failList = new ArrayList<List<String>>();
HashMap<String, Boolean> userMap = new HashMap<String, Boolean>();
HashMap<String, Boolean> cellMap = new HashMap<String, Boolean>();
HashMap<String, Boolean> noMap = new HashMap<String, Boolean>();
for(int i=0;i<info.length;i++){
	List<String> list = new ArrayList<String>();
	name = info[i][0];
	realName = info[i][1];
	mobile = info[i][2];
	gender = info[i][3];
	personNO = info[i][4];
	dept = info[i][5];
	String nameCol = "";
	String realNameCol = "";
	String mobileCol = "";
	String personNOCol="";
	String deptCol = "";
	//帐号校验
	String sql = "select name from users where name="+StrUtil.sqlstr(name);
	if ("".equals(name)){
		prompt = setPrompt(prompt,"必填项内容为空");
		nameCol = "1";
	} else {
		if (name.length()>20){
			prompt = setPrompt(prompt,"帐号不能大于20个字符");
			nameCol = "1";
		} else {
			ri = jt.executeQuery(sql);
			if (ri.size()>0){
				prompt = setPrompt(prompt,"帐号已注册");
				nameCol = "1";
			}
		}
	}
	if (!nameCol.equals("1")) {
		if (userMap.containsKey(name)) {
			prompt = setPrompt(prompt, "导入文件中存在相同帐号");
			nameCol = "1";
		} else {
			userMap.put(name, true);
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
	//手机校验
	if (!"".equals(mobile)){
		java.util.regex.Pattern p = java.util.regex.Pattern.compile("^\\d{11}$");       
		java.util.regex.Matcher m = p.matcher(mobile);   
		if (!m.matches()){
			prompt = setPrompt(prompt,"手机号非法");
			mobileCol = "1";
		} else {
			sql = "select mobile from users where  mobile = "+StrUtil.sqlstr(mobile);
			ri = jt.executeQuery(sql);
			if (ri.size()>0){
				prompt = setPrompt(prompt,"手机号已被占用");
				mobileCol = "1";
			}
		} 
		if (!mobileCol.equals("1")) {
			if (cellMap.containsKey(mobile)) {
				prompt = setPrompt(prompt, "导入文件中存在相同手机号");
				mobileCol = "1";
			} else {
				cellMap.put(mobile, true);
			}
		}
	}
	
	//员工编号校验
	if (!"".equals(personNO)){
		if (personNO.length()>20){
			prompt = setPrompt(prompt,"员工编号不能大于20个字符");
			personNOCol = "1";
		} else {
			sql = "select person_no from users where  person_no = "+StrUtil.sqlstr(personNO);
			ri = jt.executeQuery(sql);
			if (ri.size()>0){
				prompt = setPrompt(prompt,"员工编号已存在");
				personNOCol = "1";
			}
		}
		if (!personNOCol.equals("1")) {
			if (noMap.containsKey(personNO)) {
				prompt = setPrompt(prompt, "导入文件中存在相同人员编号");
				personNOCol = "1";
			} else {
				noMap.put(personNO, true);
			}
		}
	}
	
	//部门校验
	if("".equals(dept)){
		prompt = setPrompt(prompt,"必填项内容为空");
		deptCol = "1";
	} else {
			String[] deptArr = dept.split("\\\\");
		if (deptArr.length>12){
			prompt = setPrompt(prompt,"部门不能超过12级");
			deptCol = "1";
		}
	}
	list.add(name);
	list.add(realName);
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
	if (!"".equals(prompt)){
		failList.add(list);
	} else {
		successList.add(list);
	}
	prompt = "";
}
%>	
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>办公用品导入</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/hopscotch/css/hopscotch.css" />
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/hopscotch/hopscotch.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/inc/common.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
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
<!--1步文字--><div class="oranize-txt1">导入Excel</div>
<!--2步文字--><div class="oranize-txt2 oranize-txt-sel">确认信息</div>
<!--3步文字--><div class="oranize-txt3">完成</div>
</div>

</body>
<script type="text/javascript">
	parent.hiddenLoading();    
	function previous(){
		window.location.href = "<%=request.getContextPath()%>/admin/organize/user_import.jsp";
	}
	function next(){
		parent.showLoading();
		window.location.href = "<%=request.getContextPath()%>/admin/organize/user_import_finish.jsp";
	}
</script>
</html>