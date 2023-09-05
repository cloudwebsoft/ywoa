<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%
/*
- 功能描述:
- 访问规则：
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：2016-12-20
==================
*/
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<title>表单域选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<%
String op = ParamUtil.get(request, "op");
String formCode = ParamUtil.get(request, "formCode");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	// out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "表单" + formCode + "不存在！"));
	return;
}
Vector v = fd.getFields();

Iterator ir = v.iterator();
String options = "";
String fields = ParamUtil.get(request, "fields");

String[] fds = fields.split(",");
int len = fds.length;
if (fields.equals(""))
	len = 0; // 当为空时，split所得的数组长度为1
String[] fdsText = new String[len];
while (ir.hasNext()) {
	FormField ff = (FormField) ir.next();
		
	// 判断是否已被选中
	boolean isFinded = false;
	for (int i=0; i<len; i++) {
		if (ff.getName().equals(fds[i])) {
			isFinded = true;
			fdsText[i] = ff.getTitle();
		}
	}
	if (!isFinded)
		options += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
}

MacroCtlMgr mm = new MacroCtlMgr();		

/*
// 取出嵌套表
ir = v.iterator();
while (ir.hasNext()) {
	FormField ff = (FormField)ir.next();
	if (ff.getType().equals(FormField.TYPE_MACRO)) {
        MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
		// if (ff.getMacroType().equals("nest_table") || ff.getMacroType().equals("nest_sheet")) {
			// String nestFormCode = ff.getDefaultValue();
			
			String nestFormCode = ff.getDefaultValue();
			try {
				String defaultVal;
				if (mu.getNestType()==MacroCtlUnit.NEST_DETAIL_LIST) {
					defaultVal = StrUtil.decodeJSON(ff.getDescription());				
				}
				else {
					defaultVal = StrUtil.decodeJSON(ff.getDefaultValueRaw()); // ff.getDefaultValueRaw()		
				}
				// 20131123 fgf 添加
				JSONObject json = new JSONObject(defaultVal);
				nestFormCode = json.getString("destForm");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// LogUtil.getLog(getClass()).info(nestFormCode + " is old version before 20131123.");
			}
			
			FormDb nestfd = new FormDb();
			nestfd = nestfd.getFormDb(nestFormCode);
			
			ModuleSetupDb msd = new ModuleSetupDb();
			msd = msd.getModuleSetupDbOrInit(nestFormCode);

			String listField = "," + StrUtil.getNullStr(msd.getString("list_field")) + ",";
			Iterator ir2 = nestfd.getFields().iterator();
			while (ir2.hasNext()) {
				FormField ff2 = (FormField)ir2.next();
				// 判断是否在模块中已设置为显示于列表中
				if (listField.indexOf("," + ff2.getName() + ",")!=-1) {
					
					// 判断是否已被选中
					boolean isFinded = false;
					for (int i=0; i<len; i++) {
						if (("nest." + ff2.getName()).equals(fds[i])) {
							isFinded = true;
							fdsText[i] = ff2.getTitle() + "(嵌套表)";
						}
					}
					if (!isFinded)
						options += "<option value='nest." + ff2.getName() + "'>" + ff2.getTitle() + "(嵌套表)</option>";
				}
			}
			// break;
		}
	}
}
*/

String selOptions = "";
for (int i=0; i<len; i++) {
	selOptions += "<option value='" + fds[i] + "'>" + fdsText[i] + "</option>";
}
%>
<script language="JavaScript">
function setFieldWrite() {
	var str = "";
	var strText = "";
	var opts = fieldsSelected.options;
	var len = opts.length;
	for (var i=0; i<len; i++) {
		if (str=="") {
			str = opts[i].value;
			strText = opts[i].text;
		}
		else {
			str += "," + opts[i].value;
			strText += "," + opts[i].text;
		}
	}
	window.opener.setFields(str);
	window.close();
}

function sel() {
	var opts = fieldsNotSelected.options;
	var len = opts.length;
	var ary = new Array(len);
	for (var i=0; i<len; i++) {
		ary[i] = "0";
		if (opts[i].selected) {
			fieldsSelected.options.add(new Option(opts[i].text, opts[i].value));
			ary[i] = opts[i].value;
		}
	}
	for (var i=0; i<len; i++) {
		for (var j=0; j<len; j++) {
			if (ary[i]!="0") {
				try {
				    // 删除项目后，options会变短，因此用异常捕获来防止出错
					if (opts[j].value==ary[i])
						opts.remove(j);
				}
				catch(e) {
				}
			}
		}
	}
}

function notsel() {
	var opts = fieldsSelected.options;
	var len = opts.length;
	var ary = new Array(len);
	for (var i=0; i<len; i++) {
		ary[i] = "0";
		if (opts[i].selected) {
			fieldsNotSelected.options.add(new Option(opts[i].text, opts[i].value));
			ary[i] = opts[i].value;
		}
	}
	
	for (var i=0; i<len; i++) {
		for (var j=0; j<len; j++) {
			if (ary[i]!="0") {
				try {
				    // 删除项目后，options会变短，因此用异常捕获来防止出错
					if (opts[j].value==ary[i])
						opts.remove(j);
				}
				catch(e) {
				}
			}
		}
	}	
}
</script>
</head>
<body>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="cfg" scope="page" class="com.redmoon.oa.Config"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width="501" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1">
  <tr>
    <td height="23" colspan="3" class="tabStyle_1_title">选择表单域 (表单名称：<%=fd.getName()%>)</td>
  </tr>
  <tr>
    <td width="231" height="22" align="center">以下为已选的域</td>
    <td width="37">&nbsp;</td>
    <td width="231" height="22" align="center">以下为备选的域</td>
  </tr>
  <tr>
    <td height="22" align="center"><input id="destField" name="destField" size="15" onkeypress="return findDestField()" />
    <input type="button" value="查找" onclick="findDestField()" /></td>
    <td>&nbsp;</td>
    <td height="22" align="center"><input id="field" name="field" size="15" onkeypress="return findField()" />
    <input type="button" value="查找" onclick="findField()" /></td>
  </tr>
  <tr>
    <td align="center"><select id="fieldsSelected" name="fieldsSelected" size=15 multiple style="width:200px;height:345px;" ondblclick="notsel();" >
	<%=selOptions%>
    </select>
    </td>
    <td align="center" valign="middle"><input type="button" name="sel" style="font-family:'宋体'" value=" &lt; " onclick="sel()" />
      <br>
      <br>
      <input type="button" name="notsel" style="font-family:'宋体'" value=" &gt; " onclick="notsel()" /></td>
    <td align="center">
	<select id="fieldsNotSelected" name="fieldsNotSelected" size=15 multiple style="width:200px;height:345px;" ondblclick="sel();">
	<%=options%>
    </select>
    </td>
  </tr>
  <tr align="center">
    <td height="28" colspan="3">
    <input class="btn" type="button" name="okbtn" value="确定" onclick="setFieldWrite()" />
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <input class="btn" type="button" name="cancelbtn" value="取消" onclick="window.close()" />
    </td>
  </tr>
</table>
</body>
<script>
function findField() {
	var obj = o("fieldsNotSelected");
	for (var i=0; i<obj.options.length; i++) {
		if (obj.options[i].text.indexOf(o("field").value)!=-1) {
			obj.options[i].selected = true;
		}
		else {
			obj.options[i].selected = false;
		}
	}
}

function findDestField() {
	var obj = o("fieldsSelected");
	for (var i=0; i<obj.options.length; i++) {
		if (obj.options[i].text.indexOf(o("destField").value)!=-1) {
			obj.options[i].selected = true;
		}
		else {
			obj.options[i].selected = false;
		}
	}
}
</script>
</html>
