<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.basic.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.integration.cwbbs.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="java.lang.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request,"read")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, 	cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // 用于有sales管理权限的人员管理时
String formCode = ParamUtil.get(request, "formCode");
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

String modUrlList = StrUtil.getNullStr(msd.getString("url_list"));
System.out.println(modUrlList);
if (modUrlList.equals("")) {
	modUrlList = request.getContextPath() + "/" + "salary/salary_list.jsp?formCode=" + StrUtil.UrlEncode(formCode);
}
else {
	modUrlList = request.getContextPath() + "/" + modUrlList;
}

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);

MacroCtlMgr mm = new MacroCtlMgr();


String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>高级查询</title>

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../inc/livevalidation_standalone.js"></script>

<script>
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
<%@ include file="salary_inc_menu_top.jsp"%>

<div class="spacerH"></div>

  <form  method="get" name="form2" id="form2" action="<%=modUrlList%>">
    <table class="tabStyle_1 percent98" >
      <tr>
        <td colspan="2" class="tabStyle_1_title">高级查询（表单名称：工资表） </td>
      </tr>
      
       <tr>
       <td>姓名</td>
       <td>
           <input type="text"  name="employeeName"/>
       </td>
    </tr>
    <tr>
       <td>年份</td>
       <td>
            <select name="year_search" id="year_search">
              <option value="">-请选择-</option>
              <option value="2008">2008</option>
              <option value="2009">2009</option>
              <option value="2010">2010</option>
              <option value="2011">2011</option>
              <option value="2012">2012</option>
              <option value="2013">2013</option>
              <option value="2014">2014</option>
              <option value="2015">2015</option>
              <option value="2016">2016</option>
              <option value="2017">2017</option>
              <option value="2018">2018</option>
              <option value="2019">2019</option>
              <option value="2020">2020</option>
              <option value="2021">2021</option>
              <option value="2022">2022</option>
              <option value="2023">2023</option>
              <option value="2024">2024</option>
              <option value="2025">2025</option>
              <option value="2026">2026</option>
              <option value="2027">2027</option>
        </select>
       </td>
    </tr>
    <tr>
       <td>月份</td>
       <td>
         <select  name="month_search" id="month_search" >
             <option value="">-请选择-</option>
             <option value="1月">1月</option>
             <option value="2月">2月</option>
             <option value="3月">3月</option>
             <option value="4月">4月</option>
             <option value="5月">5月</option>
             <option value="6月">6月</option>
             <option value="7月">7月</option>
             <option value="8月">8月</option>
             <option value="9月">9月</option>
             <option value="10月">10月</option>
             <option value="11月">11月</option>
             <option value="12月">12月</option>
        </select>  
       </td>
    </tr>
    <tr>
       <td>部门</td>
       <td align=left>
				  <input id="deptName" name="deptName" readonly />
                  <input id="deptCode" name="deptCode" type="hidden" />&nbsp;<a href="javascript:;" onclick="selDept()">选择</a>
      </td>
    </tr>
    <tr>
       <td>基本工资范围</td>
       <td>
            从<input type="text" name="basicWageFrom" />元&nbsp;&nbsp;~&nbsp;&nbsp;<input type="text" name="basicWageTo"/>元
       </td>
    </tr>
    <tr>
       <td>应发工资范围</td>
       <td>
            从<input type="text" name="shouldPayFrom" />元&nbsp;&nbsp;~&nbsp;&nbsp;<input type="text" name="shouldPayTo"/>元
       </td>
    </tr>
    <tr>
       <td>扣除工资范围</td>
       <td>
           从<input type="text" name="deductionWageFrom" />元&nbsp;&nbsp;~&nbsp;&nbsp;<input type="text" name="deductionWageTo"/>元
       </td>
    </tr>
    <tr>
       <td>实发工资范围</td>
       <td> 
           从<input type="text" name="realWageFrom" />元&nbsp;&nbsp;~&nbsp;&nbsp;<input type="text" name="realWageTo"/>元       
       </td>
    </tr>
    <tr>
       <td>请假次数</td>
       <td>
           <select name="symbolOfLeaveTimes">
              <option value=">">></option>
              <option value="&lt;">< </option>
              <option value="=">=</option>
           </select> 
           <input type="text" name="leaveTimes"/>&nbsp;次
    </tr>
      <tr>
        <td colspan="2" align="center">
            <input  type="submit" value="查询" name="search" class="btn"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <input type="hidden" name="op" value="search" />
            <input type="hidden" name="action" value="<%=action%>"/>
            <input type="hidden" name="formCode" value="<%=formCode%>"/>
        </td>
      </tr>
</table>
</form>
</body>

</html>