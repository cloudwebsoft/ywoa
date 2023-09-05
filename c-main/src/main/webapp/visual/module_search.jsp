<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // 用于有sales管理权限的人员管理时
String code = ParamUtil.get(request, "code");
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(code);
if (msd==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(code);

String formCode = msd.getString("form_code");
if (formCode.equals("")) {
	out.print(SkinUtil.makeErrMsg(request, "编码不能为空！"));
	return;
}

String modUrlList = StrUtil.getNullStr(msd.getString("url_list"));
if (modUrlList.equals("")) {
	modUrlList = request.getContextPath() + "/" + "visual/moduleListPage.do?formCode=" + StrUtil.UrlEncode(formCode);
}
else {
	modUrlList = request.getContextPath() + "/" + modUrlList;
}

FormMgr fm = new FormMgr();
FormDb fd = fm.getFormDb(formCode);

MacroCtlMgr mm = new MacroCtlMgr();
%>
<!DOCTYPE html>
<html>
<head>
<title>智能模块设计-查询</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../inc/flow_dispose_js.jsp"></script>
<script src="../inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/flow_dispose_js.jsp"></script>
<script>
function setradio(myitem,v)
{
     var radioboxs = document.all.item(myitem);
     if (radioboxs!=null)
     {
       for (i=0; i<radioboxs.length; i++)
          {
            if (radioboxs[i].type=="radio")
              {
                 if (radioboxs[i].value==v)
				 	radioboxs[i].checked = true;
              }
          }
     }
}
</script>
</head>
<body>
<%@ include file="module_inc_menu_top.jsp"%>
<script>
o("menu3").className="current"; 
</script>
<div class="spacerH"></div>
  <form action="<%=modUrlList%>" method="get" name="form2" id="form2">
    <table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
      <tr>
        <td colspan="2" class="tabStyle_1_title">表单数据信息（表单名称：<%=fd.getName()%>） </td>
      </tr>
      <%
			Iterator ir = fd.getFields().iterator();
			while (ir.hasNext()) {
				FormField ff = (FormField)ir.next();
                if (!ff.isCanQuery())
                    continue;				
			%>
      <tr>
        <td width="20%"><%=ff.getTitle()%>：</td>
        <td width="80%" nowrap="nowrap">
		<%if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {%>
		  <select name="<%=ff.getName()%>_cond" onchange="displayDateSel('<%=ff.getName()%>', this.value)">
            <option value="0">时间段</option>
            <option value="1">时间点</option>
          </select>
		  <span id="span<%=ff.getName()%>_seg">
          大于
          <input name="<%=ff.getName()%>FromDate" id="<%=ff.getName()%>FromDate" size="10" />
          <script type="text/javascript">
          $(function(){
                $('#<%=ff.getName()%>FromDate').datetimepicker({
                	lang:'ch',
                	timepicker:false,
                	format:'Y-m-d',
                	formatDate:'Y/m/d'
                });
           })
                </script>
          <!-- <img style="CURSOR: hand" onclick="SelectDate('<%=ff.getName()%>FromDate', 'yyyy-MM-dd')" src="<%=Global.getRootPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" />-->
          小于
          <input name="<%=ff.getName()%>ToDate" id="<%=ff.getName()%>ToDate" size="10" />
           <script type="text/javascript">
          		//var fid = <%=ff.getName()%>+'FromDate';
          		//alert(fid);
          		$(function(){
	                $('#<%=ff.getName()%>ToDate').datetimepicker({
	                	lang:'ch',
	                	timepicker:false,
	                	format:'Y-m-d',
	                	formatDate:'Y/m/d'
	                });
                })
                </script>
          <!-- <img style="CURSOR: hand" onclick="SelectDate('<%=ff.getName()%>ToDate', 'yyyy-MM-dd')" src="<%=Global.getRootPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" />-->
		  </span>
		  <span id="span<%=ff.getName()%>_point" style="display:none">
		  <input name="<%=ff.getName()%>" size="6" />
          <img style="CURSOR: hand" onclick="SelectDate('<%=ff.getName()%>', 'yyyy-MM-dd')" src="<%=Global.getRootPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" />
		  </span>
        <%}
		else if (ff.getType().equals(FormField.TYPE_SELECT)) {
			String opts = FormParser.getOptionsOfSelect(fd, ff);
			opts = opts.replaceAll("selected", "");
		%>
          <select name="<%=ff.getName()%>_cond">
            <option value="1">等于</option>
          </select>
		  <select name="<%=ff.getName()%>">
		  <option value="">请选择</option>
		  <%=opts%>
		  </select>
		  <input value="或者" onclick="addOrCond(this, '<%=ff.getName()%>')" type="button">
        <%}
		else if(ff.getType().equals(FormField.TYPE_MACRO)) {
			MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		%>
          <select name="<%=ff.getName()%>_cond">
            <option value="1">等于</option>
            <option value="0" selected="selected">包含</option>
          </select>
			<%
			out.print(mu.getIFormMacroCtl().convertToHTMLCtlForQuery(request, ff));
		}
		else if (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
			%>
          <select name="<%=ff.getName()%>_cond">
            <option value="=" selected="selected">=</option>
            <option value=">">></option>
            <option value="&lt;"><</option>
            <option value=">=">>=</option></option>
            <option value="<="><=</option>
          </select>
          <input name="<%=ff.getName()%>" />			
			<%
		}
		else{%>
          <select name="<%=ff.getName()%>_cond">
            <option value="1">等于</option>
            <%if (ff.getType().equals(ff.TYPE_TEXTFIELD) || ff.getType().equals(ff.TYPE_TEXTAREA)) {%>
            <option value="0" selected="selected">包含</option>
            <%}%>
          </select>
          <input name="<%=ff.getName()%>" />
        <%}%>
        (<%=ff.getTypeDesc()%>)
	    </td>
      </tr>
      <%}
		
		boolean isShowUnitCode = false;
		String myUnitCode = privilege.getUserUnitCode(request);
		DeptDb dd = new DeptDb();
		dd = dd.getDeptDb(myUnitCode);
		
		Vector vtUnit = new Vector();
		vtUnit.addElement(dd);
			
		// 向下找两级单位
		DeptChildrenCache dl = new DeptChildrenCache(dd.getCode());
		java.util.Vector vt = dl.getDirList();	
		Iterator irDept = vt.iterator();
		while (irDept.hasNext()) {
			dd = (DeptDb)irDept.next();
			if (dd.getType()==DeptDb.TYPE_UNIT) {
				vtUnit.addElement(dd);
				DeptChildrenCache dl2 = new DeptChildrenCache(dd.getCode());
				Iterator ir2 = dl2.getDirList().iterator();
				while (ir2.hasNext()) {
					dd = (DeptDb)ir2.next();
					if (dd.getType()==DeptDb.TYPE_UNIT) {
						vtUnit.addElement(dd);
					}
					
					DeptChildrenCache dl3 = new DeptChildrenCache(dd.getCode());
					Iterator ir3 = dl3.getDirList().iterator();
					while (ir3.hasNext()) {
						dd = (DeptDb)ir3.next();
						if (dd.getType()==DeptDb.TYPE_UNIT) {
							vtUnit.addElement(dd);
						}
					}				
				}
			}
		}
		
		// 如果是集团单位，且能够管理模块
		if (vtUnit.size()>1 && mpd.canUserManage(privilege.getUser(request))) {
			isShowUnitCode = true;
		}
		
		if (isShowUnitCode) {
	  %>
      <tr>
        <td>单位</td>
        <td nowrap="nowrap">
        <select id="unitCode" name="unitCode" onChange="onChangeUnitCode(this.value);">
        <%if (privilege.getUserUnitCode(request).equals(DeptDb.ROOTCODE)) {%>    
        <option value="-1">不限</option>
        <%}%>
        <%
        Iterator irUnit = vtUnit.iterator();
        while (irUnit.hasNext()) {
            dd = (DeptDb)irUnit.next();
            int layer = dd.getLayer();
            String layerStr = "";
            for (int i=2; i<layer; i++) {
                layerStr += "&nbsp;&nbsp;";
            }
            if (layer>1) {
                layerStr += "├";
            }
        %>
        <option value="<%=dd.getCode()%>"><%=layerStr%><%=dd.getName()%></option>
        <%}%>
        </select>
        </td>
      </tr>
        <%}%>
      <tr>
        <td colspan="2" align="center">
          <input name="submit" type="submit" class="btn"  value="查  询" />
          &nbsp;&nbsp;&nbsp;
          <input type="hidden" name="op" value="search" />
          <input type="hidden" name="action" value="<%=action%>" />
          <input type="hidden" name="code" value="<%=code%>" />
          <input type="hidden" name="formCode" value="<%=formCode%>" />
		</td>
      </tr>
</table>
</form>
</body>
<script>
function displayDateSel(fieldName, flag) {
	if (flag=="0") {
		$("span" + fieldName + "_seg").style.display = "";
		$("span" + fieldName + "_point").style.display = "none";
	}
	else {
		$("span" + fieldName + "_seg").style.display = "none";
		$("span" + fieldName + "_point").style.display = "";
	}
}
function addOrCond(btnObj,name){
    var text = "&nbsp;或者&nbsp;<select name='" + name + "'>" + $(name).innerHTML + "</select>";
	btnObj.insertAdjacentHTML("BeforeBegin", text);
}
</script>
</html>