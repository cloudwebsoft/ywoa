<%@ page contentType="text/html;charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.questionnaire.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "admin.questionnaire")) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	QuestionnaireFormDb qfd = new QuestionnaireFormDb();
	Vector vForm = qfd.list();
	Iterator iForm = vForm.iterator();
	
	String op = ParamUtil.get(request, "op");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>问卷表单-添加</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<script type="text/javascript" src="../ckeditor/ckeditor.js" mce_src="../ckeditor/ckeditor.js"></script>
<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
</head>
<body>
<%@ include file="questionnaire_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
<div class="spacerH"></div>
<table border="0" align="center" cellpadding="6" cellspacing="0"  class="tabStyle_1 percent80">
  <tr>
    <td colspan="2" align="left" class="tabStyle_1_title" id="form_title">添加问卷</td>
  </tr>
  <form id="form" action="questionnaire_form_m.jsp" method="post">
    <input id="op" name="op" type="hidden" value="add" />
    <input id="form_id" name="form_id" type="hidden" />
    <!--for save&del-->
    <tr>
      <td>问卷名称：</td>
      <td><input id="form_name" name="form_name" type="text" style="width:300px" maxlength="40"/>
      </td>
    </tr>
    <tr>
      <td>开始日期：</td>
      <td align="left">
        <input readonly type="text" id="beginDate" name="beginDate" size="10">
      </td>
    </tr>
    <tr>
      <td>结束日期：</td>
      <td align="left">
        <input readonly type="text" id="endDate" name="endDate" size="10">
		<script>
          var form_name = new LiveValidation('form_name');
          form_name.add(Validate.Presence, { failureMessage:'请填写名称！'} );			
          var beginDate = new LiveValidation('beginDate');
          beginDate.add(Validate.Presence);			
          var endDate = new LiveValidation('endDate');
          endDate.add(Validate.Presence);			
        </script>      
        
      (结束日期不含在内)</td>
    </tr>
    <tr>
      <td>是否启用：</td>
      <td align="left"><select name="isOpen">
        <option value="1" selected="selected">是</option>
        <option value="0">否</option>
      </select>
      </td>
    </tr>
    <tr style="display:none">
      <td>是否公开：</td>
      <td align="left"><select name="isPublic">
        <option value="1" selected="selected">是</option>
        <option value="0">否</option>
      </select></td>
    </tr>
    <tr>
      <td>问卷描述：</td>
      <td><textarea name="description" style="width:300px;height:150px;"></textarea></td>
    </tr>
    <tr>
      <td colspan="2" align="center"><input type="submit" id="add_or_edit" value="确定" class="btn" /></td>
    </tr>
  </form>
</table>
</body>
<script>
$(function(){
	$('#beginDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d'
    });
 	$('#endDate').datetimepicker({
 		lang:'ch',
 		timepicker:false,
 		format:'Y-m-d',
 		formatDate:'Y/m/d'
 	});
})
</script>
</html>
