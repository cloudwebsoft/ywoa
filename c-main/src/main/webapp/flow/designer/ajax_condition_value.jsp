<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "org.json.*"%>
<%@page import="cn.js.fan.db.ResultIterator"%>
<%@page import="cn.js.fan.db.ResultRecord"%>
<%@page import="com.redmoon.oa.dept.DeptDb"%>
<%@page import="com.redmoon.oa.dept.DeptUserDb"%>
<%@page import="com.redmoon.oa.pvg.RoleDb"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
- 功能描述：用于获取查询中的条件字段
- 访问规则：从flow/designer/designer.jsp及flow/form_query_list_do.jsp中$.get调用
- 过程描述：
- 注意事项：当从模块自定义选项卡中关联查询时，mode为moduleTag，此时，需过滤掉与模块表单中有映射关系的字段
- 创建者：fgf 
- 创建时间：
==================
- 修改者：
- 修改时间：
- 修改原因：
- 修改点：
*/

String mode = ParamUtil.get(request, "mode");
int queryId = ParamUtil.getInt(request, "id", -1);
if (queryId==-1)
	return;
String formCode = ParamUtil.get(request, "formCode");

if (formCode.equals(""))
	return;

if (!mode.equals("moduleTag")) {
	if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
		String sql = "";

		UserDb user = new UserDb();
		user = user.getUserDb(privilege.getUser(request));
		RoleDb[] rgs = user.getRoles();			
		int len = rgs.length;

		String roles = "";
		for (int i=0; i<len; i++) {
			if (roles.equals("")) {
				roles = StrUtil.sqlstr(rgs[i].getCode());
			}
			else
				roles += "," + StrUtil.sqlstr(rgs[i].getCode());
		}
			
		DeptUserDb dud = new DeptUserDb();
		Iterator ir = dud.getDeptsOfUser(privilege.getUser(request)).iterator();
		String depts = "";
		while (ir.hasNext()) {
			DeptDb dd = (DeptDb)ir.next();
			if (depts.equals(""))
				depts = StrUtil.sqlstr(dd.getCode());
			else
				depts += "," + StrUtil.sqlstr(dd.getCode());
		}

		if ("".equals(depts)) {
			sql = "select count(query_id) from form_query_privilege where query_id=" + queryId + " and (priv_type=" + FormQueryPrivilegeDb.TYPE_USER + " and user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + ") or (priv_type=" + FormQueryPrivilegeDb.TYPE_ROLE + " and user_name in (" + roles + ")) order by query_id desc";
		}
		else {
			sql = "select count(query_id) from form_query_privilege where query_id=" + queryId + " and (priv_type=" + FormQueryPrivilegeDb.TYPE_USER + " and user_name=" + StrUtil.sqlstr(privilege.getUser(request)) + ") or (priv_type=" + FormQueryPrivilegeDb.TYPE_ROLE + " and user_name in (" + roles + ")) or (priv_type=" + FormQueryPrivilegeDb.TYPE_DEPT + " and user_name in (" + depts + ")) order by query_id desc";
		}
		JdbcTemplate jt = new JdbcTemplate();
		boolean re = false;
		try {
			ResultIterator ri = jt.executeQuery(sql);
			if (ri.hasNext()) {
				ResultRecord rr = (ResultRecord) ri.next();
				int count = rr.getInt(1);
				if (count > 0) {
					re = true;
				}
			}
		} catch (Exception e) {
		} finally {
			jt.close();
		}
		if (!re) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
			return;
		}
	}
}

String isSystem = ParamUtil.get(request, "isSystem");

JSONObject jsonTabSetup = null;
%>
<form style="margin:0px; padding:0px" id="formConditionFieldCode" name="formConditionFieldCode" method="post" action="<%=request.getContextPath()%>/flow/designer/ajax_do.jsp?op=modifyConditionFieldCode">
  <%
	String conditionFieldCodeStr = ParamUtil.get(request, "fieldsSelected");
    String[] conditionFieldCodeArr = StrUtil.split(conditionFieldCodeStr, ",");
	int fieldsLen = 0;
	if (conditionFieldCodeArr!=null)
		fieldsLen = conditionFieldCodeArr.length;
    String options = "";
	String sql="", fieldName="", tableShortCode="", tableFieldCode="", conditionSign="";
	tableShortCode = formCode;
	
	// 如果是从模块关联选项卡中进入，然后再调出条件搜索
	if (mode.equals("moduleTag")) {
		// 取出jsonTabSetup
		String tagName = ParamUtil.get(request, "tagName");
		String moduleFormCode = ParamUtil.get(request, "moduleFormCode");

		// 取得选项卡中的条件字段映射关系
		String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleFormCode, tagName);
		if (tagUrl.equals("")) {
			out.print("未找到模块中的条件字段映射关系:" + tagName + "！");
			return;
		}
		
		jsonTabSetup = new JSONObject(tagUrl);
	}	
	
%>

    <%   
    // 获得条件字段	
	int j = fieldsLen - 1;
	String[] fieldCodeArr = null;
	FormQueryConditionDb fqcd = new FormQueryConditionDb();
	
	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
		
	MacroCtlMgr mm = new MacroCtlMgr();

	int selItem = 0;
	int moduleTagCondsCount = 0;
	while(j>=0) {
		fieldName = conditionFieldCodeArr[j];
		
		// 如果来自于模块选项卡，则将其中被映射的条件字段过滤掉，因为这些字段将会被模块中相应元素的值替换
		if (mode.equals("moduleTag")) {
			boolean isFound = false;
			Iterator irJson = jsonTabSetup.keys();
			while (irJson.hasNext()) {
				String key = (String) irJson.next();
				String queryField = jsonTabSetup.getString(key);
				if (queryField.equals(fieldName)) {
					isFound = true;
					break;
				}
			}
			if (isFound) {
				j--;
				continue;
			}
		}
		
		moduleTagCondsCount++;
		
		String fieldCode = fieldName;
		
		FormField ff = fd.getFormField(fieldName);

		sql = "select id from form_query_condition where query_id=" + queryId + " and condition_field_code=" + StrUtil.sqlstr(conditionFieldCodeArr[j]) + " order by id";
		
		MacroCtlUnit mu = null;
		String macroCode = "";
		if(ff.getType().equals(FormField.TYPE_MACRO)) {
			 mu = mm.getMacroCtlUnit(ff.getMacroType());
			 macroCode = mu.getCode();
		}
		
		// 处理日期字段
		if(ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)){
			// 从数据库中获取该字段的相关条件
		   	Vector vt1 = fqcd.list(sql);
%>
            <div class="condFieldDiv">
                <span class="condFieldLSpan"><%=ff.getTitle()%>：</span>
                <span class="condFieldRSpan">
                <span id="<%=formCode%>_<%=fieldName%>_TABLE" width="100%">
<%
			    if(vt1.size() > 0){					
					String inputValue1 = "", inputValue2 = "", type = "0";
					int isFind = -1, value1 = 0, value2 = 0;
					Iterator ir1 = vt1.iterator();
					if(ir1.hasNext()){
						FormQueryConditionDb aqcd = (FormQueryConditionDb)ir1.next();
						conditionSign = aqcd.getConditionSign();
						if(ir1.hasNext()){
						    isFind = aqcd.getInputValue().indexOf("_");
							if(isFind == -1){
							    inputValue1 = aqcd.getInputValue();
								type = "0";
								aqcd = (FormQueryConditionDb)ir1.next();
								inputValue2 = aqcd.getInputValue();
							}else{
								value1 = Integer.parseInt(aqcd.getInputValue().split("\\_")[1]);
								value2 = Integer.parseInt(aqcd.getInputValue().split("\\_")[0]);
								inputValue1 = Integer.toString(value1 - value2);
								type = "2";
								aqcd = (FormQueryConditionDb)ir1.next();
								value1 = Integer.parseInt(aqcd.getInputValue().split("\\_")[1]);
								value2 = Integer.parseInt(aqcd.getInputValue().split("\\_")[0]);
								inputValue2 = Integer.toString(value1 - value2);
							}
						}else{
							isFind = aqcd.getInputValue().indexOf("_");
							if(isFind == -1){
								inputValue1 = aqcd.getInputValue();
								type = "1";
							}else{
								value1 = Integer.parseInt(aqcd.getInputValue().split("\\_")[1]);
								value2 = Integer.parseInt(aqcd.getInputValue().split("\\_")[0]);
								inputValue1 = Integer.toString(value1 - value2);
								type = "3";
							}
						}						
%>
				<select class="fl" id="<%=tableShortCode%>_<%=fieldCode%>_COND" name="<%=tableShortCode%>_<%=fieldCode%>_COND" onChange="selDate('<%=tableShortCode%>_<%=fieldCode%>_TABLE','<%=tableShortCode%>_<%=fieldCode%>_COND')">
                  <option value="0">精确段时间</option>
                  <option value="1">精确点时间</option>
                </select>
				  <script>
					formConditionFieldCode.<%=tableShortCode%>_<%=fieldCode%>_COND.value = '<%=type%>';
				  </script>
				<span class="fl" id="SEGMENT_DATE_TD" <%if(!type.equals("0")){%> style="display:none" <%}%>>
					从 
					  <input size="12" id="<%=tableShortCode%>_<%=fieldCode%>_FROMDATE" name="<%=tableShortCode%>_<%=fieldCode%>_FROMDATE" <%if(type.equals("0")){%> value="<%=inputValue1%>" <%}%> kind="date">
					至 <input size="12" id="<%=tableShortCode%>_<%=fieldCode%>_TODATE" name="<%=tableShortCode%>_<%=fieldCode%>_TODATE" <%if(type.equals("0")){%> value="<%=inputValue2%>" <%}%> kind="date">
                </span>
				<span class="fl" id="POINT_DATE_TD" <%if(!type.equals("1")){%> style="display:none" <%}%>>
					<select name="<%=tableShortCode%>_<%=fieldCode%>_SIGN">
                        <option value="=">等于</option>
                        <option value="&gt;">大于</option>
                        <option value="&lt;">小于</option>
                        <option value="&lt;=">小于等于</option>
                        <option value="&gt;=">大于等于</option>
                      </select>
                    <%if(type.equals("1")){%>
					<script>
					formConditionFieldCode.<%=tableShortCode%>_<%=fieldCode%>_SIGN.value = '<%=aqcd.getConditionSign()%>';
					</script>
					<%}%>
                    &nbsp;<input size="12" id="<%=tableShortCode%>_<%=fieldCode%>_DATE" name="<%=tableShortCode%>_<%=fieldCode%>_DATE" <%if(type.equals("1")){%> value="<%=inputValue1%>" <%}%> kind="date">
                </span>
<%
					}
				}else{ // 如果数据库中没有找到
%>
					<select class="fl" id="<%=tableShortCode%>_<%=fieldCode%>_COND" name="<%=tableShortCode%>_<%=fieldCode%>_COND" onChange="selDate('<%=tableShortCode%>_<%=fieldCode%>_TABLE','<%=tableShortCode%>_<%=fieldCode%>_COND')">
						<option value="0" selected>精确段时间</option>
						<option value="1">精确点时间</option>
					</select>                
				<span class="fl" id="SEGMENT_DATE_TD">
					从 
					  <input size="12" id="<%=tableShortCode%>_<%=fieldCode%>_FROMDATE" name="<%=tableShortCode%>_<%=fieldCode%>_FROMDATE" kind="date">
					至 <input size="12" id="<%=tableShortCode%>_<%=fieldCode%>_TODATE" name="<%=tableShortCode%>_<%=fieldCode%>_TODATE" kind="date">
                </span>
				<span class="fl" id="POINT_DATE_TD" style="display:none">
					<select name="<%=tableShortCode%>_<%=fieldCode%>_SIGN">
						<option value="=" selected>等于</option>
						<option value=">">大于</option>
						<option value="&lt;">小于</option>
						<option value="<=">小于等于</option>
						<option value=">=">大于等于</option>
					</select>
					&nbsp;&nbsp;<input size="12" id="<%=tableShortCode%>_<%=fieldCode%>_DATE" name="<%=tableShortCode%>_<%=fieldCode%>_DATE" kind="date">
                </span> 
<%
				}	
%>
                  </span>
                </span>
              </div>
<%         }
		   // 处理下拉菜单字段
		   else if(ff.getType().equals(FormField.TYPE_SELECT) || macroCode.equals("macro_flow_select")){
			   Vector vt1 = fqcd.list(sql);
			   if(ff.getType().equals(FormField.TYPE_SELECT)) {
				   	options = FormParser.getOptionsOfSelect(fd, ff);
			   }
			   else {
				    // 基础数据控件
				   	SelectMgr sm = new SelectMgr();
				   	SelectDb sd = sm.getSelect(ff.getDefaultValue());				   
					options = "";
					if (sd.getType() == SelectDb.TYPE_LIST) {
						Vector v = sd.getOptions(new JdbcTemplate());
						Iterator ir = v.iterator();
						while (ir.hasNext()) {
							SelectOptionDb sod = (SelectOptionDb) ir.next();
							String selected = "";
							if (sod.isDefault())
								selected = "selected";
							String clr = "";
							if (!sod.getColor().equals(""))
								clr = " style='color:" + sod.getColor() + "' ";
							options += "<option value='" + sod.getValue() + "' " + selected + clr +
									   ">" +
									   sod.getName() +
									   "</option>";
						}
					} else {
						TreeSelectDb tsd = new TreeSelectDb();
						tsd = tsd.getTreeSelectDb(sd.getCode());
						TreeSelectView tsv = new TreeSelectView(tsd);
						StringBuffer sb = new StringBuffer();
						try {
							options = tsv.getTreeSelectAsOptions(sb, tsd, 1).toString();
						} catch (ErrMsgException e) {
							e.printStackTrace();
						}
					}			
			   }
%>
          <div class="condFieldDiv">
            <span class="condFieldLSpan"><%=ff.getTitle()%>：</span>
			<span class="condFieldRSpan">
				<input type="hidden" value="<%=options%>" name="<%=tableShortCode%>_<%=fieldCode%>_OPTION">
				<%
				FormQueryConditionDb aqcd = new FormQueryConditionDb();				
				if(vt1.size() > 0){
					Iterator ir1 = vt1.iterator();
					int indexs = 0, len = vt1.size();
					while(ir1.hasNext()){
						aqcd = (FormQueryConditionDb)ir1.next();
						selItem++;
						if(indexs!=0)
							out.print("<span id=\"div" + aqcd.getId() + selItem + "\">或者");
				%>
				  <select id="<%=tableShortCode%>_<%=fieldCode%>_idx<%=indexs%>" name="<%=tableShortCode%>_<%=fieldCode%>">
					 <%=options%>
				  </select>
				  <script>
				  document.getElementById("<%=tableShortCode%>_<%=fieldCode%>_idx<%=indexs%>").value = "<%=aqcd.getInputValue()%>";
				  </script>
				<%
						if (indexs!=0)
							out.print("<a href=\"javascript:;\" onclick=\"div" + aqcd.getId() + selItem + ".outerHTML='';\" style='color:red'>&nbsp;×&nbsp;</a></span>");
						indexs++;
					}
				}else{
				%>			  	
				  <select name="<%=tableShortCode%>_<%=fieldCode%>">
					 <%=options%>
				  </select>
				 <%
				 } 
				 %>		  
				<input class="btn" type="button" onClick='addORConditionSel(this,"<%=tableShortCode%>_<%=fieldCode%>")' value="或者">
                <select name="<%=tableShortCode%>_<%=fieldCode%>_COMPARE">
                <option value="<%=FormQueryConditionDb.COMPARE_TYPE_NONE%>">选择比较模式</option>
                <option value="<%=FormQueryConditionDb.COMPARE_TYPE_NOT_EQUALS%>">不等于</option>
                </select>
                (选择比较模式后，将忽略或者条件) 
				<script>
                o("<%=tableShortCode%>_<%=fieldCode%>_COMPARE").value = "<%=aqcd.getCompareType()%>";
                </script>                     
			 </span>
          </div>         
      <%		 
		} else {
			  // 处理文本字段
			  boolean likeOrEquals = true;
			  Vector vt1 = fqcd.list(sql);
			  if(vt1.size() > 0){
				  Iterator ir1 = vt1.iterator();
				  if(ir1!=null && ir1.hasNext()){
					  fqcd = (FormQueryConditionDb)ir1.next();
					  conditionSign = fqcd.getConditionSign();
					  if(conditionSign.equals("="))
						  likeOrEquals = true;
					  else
						  likeOrEquals = false;
				  }
			  }
%>
              <div class="condFieldDiv">
                <span class="condFieldLSpan"><%=ff.getTitle()%>：</span>
                <span class="condFieldRSpan">
                  <select name="<%=formCode%>_<%=fieldName%>_SIGN">
                    <option value="=" <%if(likeOrEquals){%>selected<%}%>>等于</option>
				  	<option value="<>" <%=conditionSign.equals("<>")?"selected":""%>>不等于</option>                    
                    <%if (ff.getFieldType()==FormField.FIELD_TYPE_VARCHAR || ff.getFieldType()==FormField.FIELD_TYPE_TEXT) {%>
                    <option value="like" <%if(!likeOrEquals){%>selected<%}%>>包含</option>
                    <%}else{%>
                    <option value=">=" <%=conditionSign.equals(">=")?"selected":""%>>>=</option>
                    <option value=">" <%=conditionSign.equals(">")?"selected":""%>>></option>
                    <option value="<=" <%=conditionSign.equals("<=")?"selected":""%>><=</option>
                    <option value="&lt;" <%=conditionSign.equals("<")?"selected":""%>><</option>
                    <%}%>
                  </select>
                  &nbsp;
<%
			  if(ff.getType().equals(FormField.TYPE_MACRO)) {
				out.print(mu.getIFormMacroCtl().convertToHTMLCtlForQuery(request, ff));
				%>
				<script>
				if (o("<%=ff.getName()%>")) {
				o("<%=ff.getName()%>").value = "<%=StrUtil.getNullStr(fqcd.getInputValue())%>";
				o("<%=ff.getName()%>").name = "<%=formCode%>_<%=fieldName%>";
				}
				</script>
                <%if (macroCode.equals("macro_dept_select") || macroCode.equals("macro_my_dept_select")) {%>
                  <select name="<%=formCode%>_<%=fieldName%>_COMPARE">
                  <option value="<%=FormQueryConditionDb.COMPARE_TYPE_NONE%>">本部门</option>
                  <option value="<%=FormQueryConditionDb.COMPARE_TYPE_UNDER%>">含子部门</option>
                  </select>
				  <script>
                  o("<%=formCode%>_<%=fieldName%>_COMPARE").value = "<%=fqcd.getCompareType()%>";
                  </script>                 
                <%}%>
				<%
			  }
			  else {
				  boolean isNumeric = false;
				  if (ff.getFieldType()==FormField.FIELD_TYPE_INT ||
				  		ff.getFieldType()==FormField.FIELD_TYPE_LONG ||
						ff.getFieldType()==FormField.FIELD_TYPE_FLOAT ||
						ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE ||
						ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
				  	isNumeric = true;
				  }
				  %>
                  <input id="<%=formCode%>_<%=fieldName%>" name="<%=formCode%>_<%=fieldName%>" size="20" value="<%=StrUtil.getNullStr(fqcd.getInputValue())%>" <%=isNumeric ? "onpropertychange='onpropchange_" + formCode + "_" + fieldName + "(this)'" : ""%> />
                  <%
				  if (isNumeric) {							
					%>
                    <script>
					function onpropchange_<%=formCode%>_<%=fieldName%>(arg) {
						if (!isNumeric(arg.value)) {
							alert("请填写数字！");
							// arg.value = "0";
						}
					}
					/*ajax后无效
			        var f_<%=formCode%>_<%=fieldName%> = new LiveValidation('<%=formCode%>_<%=fieldName%>');
                    // f_<%=formCode%>_<%=fieldName%>.add(Validate.Presence, {});
                    f_<%=formCode%>_<%=fieldName%>.add(Validate.Numericality, {});
					*/
					</script>
					<%
				  }
				  %>
              <%}%>
                  </span>
              </div>
      <%		   
		   }			 
	   j--;
	}
%>
<%if (mode.equals("moduleTag")) {
	if (moduleTagCondsCount==0) {
	%>
	查询中无其它条件
	<%}%>
<%}%>
  <input type="hidden" name="conditionFieldCodeStr" value="<%=conditionFieldCodeStr%>">
  <input type="hidden" name="formCode" value="<%=formCode%>">
  <input type="hidden" name="id" value="<%=queryId%>">
  <input type="hidden" name="isSystem" value="<%=isSystem%>" />
</form>
