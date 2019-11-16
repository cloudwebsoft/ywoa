<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.flow.query.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = StrUtil.getNullString(request.getParameter("op"));

if (op.equals("getQueryCondField")) {
	int id = ParamUtil.getInt(request, "id", -1);
	FormQueryDb aqd = new FormQueryDb();
	if (id!=-1) {
		// 检查用户是否具备权限（是本人创建的，或者被授权）
		FormQueryPrivilegeMgr fqpm = new FormQueryPrivilegeMgr();
		if (!fqpm.canUserQuery(request, id)) {
			// out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
			// return;
		}
		
		aqd = aqd.getFormQueryDb(id);
		
		if (!aqd.isScript()) {
			String formCode = aqd.getTableCode();
			FormDb fd = new FormDb();
			fd = fd.getFormDb(formCode);
			%>
			<select id="queryField" name="queryField">
			<option value="">无</option>
			<%
			String sql = "select distinct condition_field_code from form_query_condition where query_id=" + id + " order by id";
			// System.out.println(getClass() + " sql=" + sql);
			JdbcTemplate jt = new JdbcTemplate();
			ResultIterator ri = jt.executeQuery(sql);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				String fieldCode = rr.getString(1);
				FormField ff = fd.getFormField(fieldCode);
				if (ff!=null) {
				%>
				  <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
				<%
				}
			}
			%>
			</select>
			<%
		}
		else {
			QueryScriptUtil qsu = new QueryScriptUtil();
			HashMap map = qsu.getCondFields(request, aqd);
			// System.out.println(getClass() + " map=" + map);
			Iterator ir = map.keySet().iterator();
			%>
			<select id="queryField" name="queryField">
			<option value="">无</option>	
			<%
			while (ir.hasNext()) {
				String keyName = (String) ir.next();
			%>
				<option value="<%=keyName%>"><%=map.get(keyName)%></option>
			<%
			}
			%>
			</select>
			<%
		}
	}

	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>管理模块选项卡</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script src="../inc/common.js"></script>
<script src="../inc/map.js"></script>
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<script src="<%=request.getContextPath() %>/inc/livevalidation_standalone.js"></script>

<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css" />
<script type="text/javascript" src="../js/jquery.flexbox.js"></script>

<script src="../js/select2/select2.js"></script>
<link href="../js/select2/select2.css" rel="stylesheet"/>

</head>
<body>
<%
String code = ParamUtil.get(request, "code");

String formCode = ParamUtil.get(request, "formCode");
FormDb fd = new FormDb(formCode);
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("该表单不存在！","提示"));
	return;
}

if (op.equals("addSubTag")) {
	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(code);
	double tagOrder = ParamUtil.getDouble(request, "tagOrder", -1);
	if (tagOrder==-1) {
		out.print(StrUtil.jAlert_Back("请填写顺序号！","提示"));
		return;
	}
	String tagUrl = ParamUtil.get(request, "tagUrl");
	if (tagUrl.equals(""))
		tagUrl = "#"; // 宽度置为一个空格，以便于split时生成数组
	String tagName = ParamUtil.get(request, "tagName");
	
	String tName = StrUtil.getNullStr(msd.getString("sub_nav_tag_name"));	
	String tOrder = StrUtil.getNullStr(msd.getString("sub_nav_tag_order"));
	String tUrl = StrUtil.getNullStr(msd.getString("sub_nav_tag_url"));
	
	// 检查名称是否重复
	if (("|" + tName + "|").indexOf("|" + tagName + "|")!=-1) {
		out.print(StrUtil.jAlert_Back("名称：" + tagName + " 重复","提示"));
		return;
	}
	
	if (tName.equals("")) {
		tName = tagName;
		tUrl = tagUrl;
		tOrder = "" + tagOrder;
	}
	else {
		tName += "|" + tagName;
		tUrl += "|" + tagUrl;
		tOrder += "|" + tagOrder;
	}

	// 根据tagOrder排序
	String[] strOrderAry = StrUtil.split(tOrder, "\\|");
	int len = strOrderAry.length;
	double[] orderAry = new double[len];
	for (int i=0; i<len; i++) {
		orderAry[i] = StrUtil.toDouble(strOrderAry[i]);
	}
	String[] nameAry = StrUtil.split(tName, "\\|");
	String[] urlAry = tUrl.split("\\|");
	
	double temp;
	int size = len;
	String tempStr;
	// 外层循环，控制“冒泡”的最终位置
	for(int i=size-1; i>=1; i--){
		boolean end = true;
		// 内层循环，用于相临元素的比较
		for(int j=0; j < i; j++) {
			if(orderAry[j] > orderAry[j+1]) {
				temp = orderAry[j];
				orderAry[j] = orderAry[j+1];
				orderAry[j+1] = temp;
				end = false;
				
				tempStr = nameAry[j];
				nameAry[j] = nameAry[j+1];
				nameAry[j+1] = tempStr;
				tempStr = urlAry[j];
				urlAry[j] = urlAry[j+1];
				urlAry[j+1] = tempStr;
			}
		}
		if(end == true) {
			break; 
		} 
	}

	tName = "";
	tOrder = "";
	tUrl = "";
	
	for (int i=0; i<len; i++) {
		if (i==0) {
			tName = nameAry[i];
			tOrder = "" + orderAry[i];
			tUrl = urlAry[i];
		}
		else {
			tName += "|" + nameAry[i];
			tOrder += "|" + orderAry[i];
			tUrl += "|" + urlAry[i];
		}
	}

	msd.set("sub_nav_tag_name", tName);
	msd.set("sub_nav_tag_order", tOrder);
	msd.set("sub_nav_tag_url", tUrl);
	
	boolean re = msd.save(); // new JdbcTemplate(), new Object[]{listField, msd.getString("query_field"), listFieldWidth, listFieldOrder, formCode});
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_view_edit.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("delSubTag")) {
	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(code);
	String tagName = ParamUtil.get(request, "tagName");

	String tName = StrUtil.getNullStr(msd.getString("sub_nav_tag_name"));
	String tUrl = StrUtil.getNullStr(msd.getString("sub_nav_tag_url"));
	String tOrder = StrUtil.getNullStr(msd.getString("sub_nav_tag_order"));
	String[] nameAry = StrUtil.split(tName, "\\|");
	String[] urlAry = StrUtil.split(tUrl, "\\|");
	String[] orderAry = StrUtil.split(tOrder, "\\|");
	
	tName = "";
	tUrl = "";
	tOrder = "";

	int len = nameAry.length;
	for (int i=0; i<len; i++) {
		if (nameAry[i].equals(tagName)) {
			continue;
		}
		if (tName.equals("")) {
			tName = nameAry[i];
			tUrl = urlAry[i];
			tOrder = orderAry[i];
		}
		else {
			tName += "|" + nameAry[i];
			tUrl += "|" + urlAry[i];
			tOrder += "|" + orderAry[i];
		}
	}
	msd.set("sub_nav_tag_name", tName);
	msd.set("sub_nav_tag_url", tUrl);
	msd.set("sub_nav_tag_order", tOrder);
	
	boolean re = msd.save(); // new JdbcTemplate(), new Object[]{listField, msd.getString("query_field"), listFieldWidth, listFieldOrder, formCode});
	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_view_edit.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}
else if (op.equals("modifySubTag")) {
	ModuleSetupDb msd = new ModuleSetupDb();
	msd = msd.getModuleSetupDb(code);
	String tagName = ParamUtil.get(request, "tagName");
	String newTagName = ParamUtil.get(request, "newTagName");

	String tagOrder = ParamUtil.get(request, "tagOrder");
	String tagUrl = ParamUtil.get(request, "tagUrl");
	if (tagUrl.equals(""))
		tagUrl = "#"; // 宽度置为一个空格，以便于split时生成数组

	String tName = StrUtil.getNullStr(msd.getString("sub_nav_tag_name"));
	String tUrl = StrUtil.getNullStr(msd.getString("sub_nav_tag_url"));
	String tOrder = StrUtil.getNullStr(msd.getString("sub_nav_tag_order"));
	String[] nameAry = StrUtil.split(tName, "\\|");
	String[] urlAry = StrUtil.split(tUrl, "\\|");
	String[] strOrderAry = StrUtil.split(tOrder, "\\|");
	
	tName = "";
	tUrl = "";
	tOrder = "";

	int len = nameAry.length;
	for (int i=0; i<len; i++) {
		if (nameAry[i].equals(tagName)) {
			nameAry[i] = newTagName;
			strOrderAry[i] = tagOrder;
			urlAry[i] = tagUrl;
		}
		if (tName.equals("")) {
			tName = nameAry[i];
			tUrl = urlAry[i];
			tOrder = strOrderAry[i];
		}
		else {
			tName += "|" + nameAry[i];
			tUrl += "|" + urlAry[i];
			tOrder += "|" + strOrderAry[i];
		}
	}

	// 根据fieldOrder排序
	double[] orderAry = new double[len];
	for (int i=0; i<len; i++) {
		orderAry[i] = StrUtil.toDouble(strOrderAry[i]);
	}
	
	double temp;
	int size = len;
	String tempStr;
	// 外层循环，控制“冒泡”的最终位置
	for(int i=size-1; i>=1; i--){
		boolean end = true;
		// 内层循环，用于相临元素的比较
		for(int j=0; j < i; j++) {
			if(orderAry[j] > orderAry[j+1]) {
				temp = orderAry[j];
				orderAry[j] = orderAry[j+1];
				orderAry[j+1] = temp;
				end = false;
				
				tempStr = nameAry[j];
				nameAry[j] = nameAry[j+1];
				nameAry[j+1] = tempStr;
				tempStr = urlAry[j];
				urlAry[j] = urlAry[j+1];
				urlAry[j+1] = tempStr;
			}
		}
		if(end == true) {
			break; 
		} 
	}
	
	tName = "";
	tUrl = "";
	tOrder = "";
	
	for (int i=0; i<len; i++) {
		if (i==0) {
			tName = nameAry[i];
			tUrl = urlAry[i];
			tOrder = ""+orderAry[i];
		}
		else {
			tName += "|" + nameAry[i];
			tUrl += "|" + urlAry[i];
			tOrder += "|" + orderAry[i];
		}
	}

	msd.set("sub_nav_tag_name", tName);
	msd.set("sub_nav_tag_url", tUrl);
	msd.set("sub_nav_tag_order", tOrder);
	
	boolean re = msd.save();

	if (re) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "module_view_edit.jsp?code=" + code + "&formCode=" + formCode));
	}
	else {
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	}
	return;
}

ModuleSetupDb vsd = new ModuleSetupDb();
vsd = vsd.getModuleSetupDb(code);
%>
<%@ include file="module_setup_inc_menu_top.jsp"%>
<script>
o("menu4").className="current"; 
</script>
<div class="spacerH"></div>
<table cellspacing="0" class="tabStyle_1 percent80" cellpadding="3" width="95%" align="center">
  <tr>
    <td class="tabStyle_1_title" nowrap="nowrap" width="7%">序号</td>
    <td class="tabStyle_1_title" nowrap="nowrap" width="19%">选项卡名称</td>
    <td class="tabStyle_1_title" nowrap="nowrap" width="41%">链接</td>
    <td class="tabStyle_1_title" nowrap="nowrap" width="12%">顺序</td>
    <td width="21%" nowrap="nowrap" class="tabStyle_1_title">操作</td>
  </tr>
<%
String sub_nav_tag_name = StrUtil.getNullStr(vsd.getString("sub_nav_tag_name"));
String[] sub_tags = StrUtil.split(sub_nav_tag_name, "\\|");

String sub_nav_tag_order = StrUtil.getNullStr(vsd.getString("sub_nav_tag_order"));
String[] sub_tagOrders = StrUtil.split(sub_nav_tag_order, "\\|");

String sub_nav_tag_url = StrUtil.getNullStr(vsd.getString("sub_nav_tag_url"));
String[] sub_tagUrls = StrUtil.split(sub_nav_tag_url, "\\|");

int len = 0;
if (sub_tags!=null)
	len = sub_tags.length;
for (int i=0; i<len; i++) {
	String tagName = sub_tags[i];
	%>
  <form action="module_view_edit.jsp?op=modifySubTag" method="post" name="formSubTag<%=i%>" id="formSubTag<%=i%>">
    <tr class="highlight">
      <td align="center"><%=i+1%></td>
      <td>
      	  <input name="newTagName" value="<%=tagName%>" />
          <input name="code" value="<%=code%>" type="hidden" />
          <input name="formCode" value="<%=formCode%>" type="hidden" />
          <input name="tagName" value="<%=tagName%>" type="hidden" />
      </td>
      <td>
<%
      if (sub_tagUrls[i].startsWith("{")) {
		  try {
			  JSONObject json = new JSONObject(sub_tagUrls[i]);
			  if (!json.isNull("queryId")) {
				  int qId = StrUtil.toInt(json.getString("queryId"));
				  FormQueryDb fqd = new FormQueryDb();
				  fqd = fqd.getFormQueryDb(qId);
				  if (fqd.isLoaded()) {
					  FormDb fdQuery = new FormDb();
					  fdQuery = fdQuery.getFormDb(fqd.getTableCode());
					  String queryFieldDesc = "";
					  Iterator ir = json.keys();
					  while (ir.hasNext()) {
						String key = (String)ir.next();
						if (key.equals("queryId")) {
							continue;
						}
						else {
							FormField ff = fdQuery.getFormField(json.getString(key));
							if (ff!=null) {
								if (queryFieldDesc.equals(""))
									queryFieldDesc = ff.getTitle();
								else
									queryFieldDesc += "，" + ff.getTitle();
							}
						}
					  }
					  %>
					  查询：
					  <%if (fqd.isScript()) {%>
					  <a href="javascript:;" title="编辑查询" onClick="addTab('<%=fqd.getQueryName()%>', '<%=request.getContextPath()%>/flow/form_query_script.jsp?id=<%=qId%>')"><%=fqd.getQueryName()%></a>
	    <%}else{%>
					  <a href="javascript:;" title="编辑查询" onClick="addTab('<%=fqd.getQueryName()%>', '<%=request.getContextPath()%>/flow/designer/designer.jsp?id=<%=qId%>')"><%=fqd.getQueryName()%></a>
<%}%>
					  ，条件字段：<%=queryFieldDesc%>
				  <%}else {
					out.print("查询不存在！");
				  }%>
<%}else if (!json.isNull("reportId")) {
				  int reportId = StrUtil.toInt(json.getString("reportId"));
				  FormQueryReportDb fqrd = new FormQueryReportDb();
				  fqrd = (FormQueryReportDb)fqrd.getQObjectDb(new Integer(reportId));
				  
				  if (fqrd!=null) {
					  int qId = StrUtil.toInt(fqrd.getString("query_id"));
					  FormQueryDb fqd = new FormQueryDb();
					  fqd = fqd.getFormQueryDb(qId);
					  FormDb fdQuery = new FormDb();
					  fdQuery = fdQuery.getFormDb(fqd.getTableCode());
					  String queryFieldDesc = "";
					  Iterator ir = json.keys();
					  while (ir.hasNext()) {
						String key = (String)ir.next();
						if (key.equals("queryId")) {
							continue;
						}
						else {
							FormField ff = fdQuery.getFormField(json.getString(key));
							if (ff!=null) {
								if (queryFieldDesc.equals(""))
									queryFieldDesc = ff.getTitle();
								else
									queryFieldDesc += "，" + ff.getTitle();
							}
						}
					  }
					  %>
					  报表：
					  <a href="javascript:;" title="编辑报表" onClick="addTab('<%=fqrd.getString("title")%>', '<%=request.getContextPath()%>/flow/report/designer.jsp?id=<%=reportId%>')"><%=fqrd.getString("title")%></a>
				  ，主键字段：<%=queryFieldDesc%>
				  <%}else {
					out.print("报表不存在！");
				  }%>
   	<%} else if (!json.isNull("fieldSource")) {%>
        		关联模块：
        		<%
        		String fieldSource = json.getString("fieldSource");
        		String fieldRelated = json.getString("fieldRelated");
				
				ModuleSetupDb msdRelated = new ModuleSetupDb();
				msdRelated = msdRelated.getModuleSetupDb(json.getString("formRelated"));
				
        		FormDb fdRelated = new FormDb();
        		fdRelated = fdRelated.getFormDb(msdRelated.getString("form_code"));
        		out.print("<a href='javascript:;' onclick=\"addTab('" + msdRelated.getString("name") + "', '" + request.getContextPath() + "/visual/module_field_list.jsp?formCode=" + fdRelated.getCode() + "&code=" + msdRelated.getString("code") + "')\">" + msdRelated.getString("name") + "</a>");
        		
        		String ffTitle = "";
        		if ("id".equalsIgnoreCase(fieldSource)) {
        			ffTitle = "ID";
        		}
        		else {
	        		FormField ff = fd.getFormField(fieldSource);
        			if (ff!=null) {
        				ffTitle = ff.getTitle();
        			}
        			else {
        				ffTitle = fieldSource + "不存在！";
        			}
        		}
        		String ffRelatedTitle = "";
        		if ("cws_id".equals(fieldRelated)) {
        			ffRelatedTitle = "cws_id";
        		}
        		else {
        			if ("".equals(fieldRelated)) {
        				ffRelatedTitle = "无";
        			}
        			else {
		        		FormField ffRelated = fdRelated.getFormField(fieldRelated);
		        		if (ffRelated!=null) {
		        			ffRelatedTitle = ffRelated.getTitle();
		        		}
		        		else {
		        			ffRelatedTitle = fieldRelated + "不存在";
		        		}
	        		}
        		}
        		%>
        		，<%=ffTitle%>=<%=ffRelatedTitle%>
			<%
			if (json.has("fieldOtherRelated")) {
	        	FormField ff = fdRelated.getFormField(json.getString("fieldOtherRelated"));
				if (ff!=null) {
				%>
					，<%=ff.getTitle()%>=<%=json.getString("fieldOtherRelatedVal")%>
				<%
				}
				else {
				%>
					，<%=json.getString("fieldOtherRelated")%>不存在
				<%
				}
			}
			if (json.has("cwsStatus")) {
				%>
				，状态：
				<%
				int cwsStatus = StrUtil.toInt(json.getString("cwsStatus"), -100);
				if (cwsStatus==-100) {
					out.print("不限");
				}
				else if (cwsStatus==com.redmoon.oa.flow.FormDAO.STATUS_DONE) {
					out.print("流程已结束");
				}
				else if (cwsStatus==com.redmoon.oa.flow.FormDAO.STATUS_NOT) {
					out.print("流程未走完");
				}
				else if (cwsStatus==com.redmoon.oa.flow.FormDAO.STATUS_REFUSED) {
					out.print("流程被拒绝");
				}
				else if (cwsStatus==com.redmoon.oa.flow.FormDAO.STATUS_DISCARD) {
					out.print("流程被放弃");
				}
			}
			if (json.has("viewList")) {
				%>
				，视图：
				<%
				int viewList = StrUtil.toInt(json.getString("viewList"), ModuleSetupDb.VIEW_DEFAULT);
				if (viewList == ModuleSetupDb.VIEW_DEFAULT) {
					out.print("默认");
				}
				else {
					out.print("任务看板");
				}
			}
			%>
   	  <%} %>
			  <input name="tagUrl" type="hidden" value='<%=sub_tagUrls[i]%>' />
		  <%
		  }
		  catch (JSONException e) {
		  	out.print(e.getMessage());
		  }
      }else{%>
      	<input name="tagUrl" size="35" value="<%=sub_tagUrls[i]%>" />
      <%}%>
      </td>
      <td><input name="tagOrder" size="5" value="<%=sub_tagOrders[i]%>" /></td>
      <td align="center"><input class="btn" name="submit22" type="submit" value="修改" />
        &nbsp;&nbsp;
        <input class="btn" name="button2" type="button" onClick="jConfirm('您确定要删除么？','提示',function(r){if(!r){return;}else{window.location.href='module_view_edit.jsp?op=delSubTag&code=<%=code%>&formCode=<%=formCode%>&amp;tagName=<%=StrUtil.UrlEncode(tagName)%>'} }) " value="删除" />
      </td>
    </tr>
  </form>
  <%}%>
</table>

<form action="module_view_edit.jsp?op=addSubTag" method="post" name="formSubTagModuleRelate" id="formSubTagModuleRelate">
<table class="tabStyle_1 percent80" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td class="tabStyle_1_title" colspan="2" align="center">模块链接选项卡</td>
  </tr>
  <tr>
    <td width="12%" align="center">名称</td>
    <td width="88%">
      <input id="mTagName" name="tagName" />
    </td>
  </tr>
  <tr>
    <td align="center">模块</td>
    <td>
<%
				ModuleSetupDb msd = new ModuleSetupDb();
				String sql = "select code from visual_module_setup where is_use=1 order by code asc"; // orders asc";
				Vector v = msd.list(sql);
				Iterator ir = v.iterator();
				String jsonStr = "";
				while (ir.hasNext()) {
					msd = (ModuleSetupDb)ir.next();
					
					if (jsonStr.equals(""))
						jsonStr = "{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";
					else
						jsonStr += ",{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";

          		}
          %>
        <div id="relateCodeSel"></div>
        <input id="relateCode" name="relateCode" type="hidden" />
		<script>
		var relateCodeSel = $('#relateCodeSel').flexbox({        
				"results":[<%=jsonStr%>], 
				"total":<%=v.size()%>
			},{
			initialValue:'',
		    watermark: '请选择模块',
		    paging: false,
			maxVisibleRows: 10,
			onSelect: function() {
				o("relateCode").value = $("input[name=relateCodeSel]").val();
				getFieldOptions(o("relateCode").value);
			}
		});
		</script>         
    </td>
  </tr>
  <tr>
    <td align="center">主键</td>
    <td>
    <%
	Iterator irFields = fd.getFields().iterator();
	String opts = "";
	while (irFields.hasNext()) {
		FormField ff = (FormField)irFields.next();
		opts += "<option value='" + ff.getName() + "'>" + ff.getTitle() + "</option>";
	}	
	%>
    <select id="fieldSource" name="fieldSource">
    <option value="id">ID</option>
    <%=opts%>
    </select>
    =
    <select id="fieldRelated" name="fieldRelated">
    
    </select>
    <input id="mTagUrl" name="tagUrl" type="hidden" />
    <script>
		function getFieldOptions(code) {
			var str = "op=getOptions&code=" + code;
			var myAjax = new cwAjax.Request(
				"module_field_ajax.jsp", 
				{
					method:"post",
					parameters:str,
					onComplete:doGetFieldOptions,
					onError:errFunc
				}
			);		
		}
		
		function doGetFieldOptions(response) {
			var rsp = response.responseText.trim();
			var rspOrig = rsp;
			$("#fieldRelated").empty();
			
			rsp += "<option value='cws_id'>cws_id(关联主模块ID)</option>";
			rsp += "<option value=''>无</option>";
			$("#fieldRelated").append(rsp);
			
			$("#fieldOtherRelated").empty();
			$("#fieldOtherRelated").append(rspOrig);
		}
		
		var errFunc = function(response) {
			window.status = response.responseText;	
		}		
	 </script>
    （无表示无主键对应关系）</td>
  </tr>
  <tr>
    <td align="center">条件</td>
    <td>
    <select id="fieldOtherRelated" name="fieldOtherRelated">
    
    </select>
    =
	<input id="fieldOtherRelatedVal" name="fieldOtherRelatedVal" />
	（空值表示无条件
    ）</td>
  </tr>
  <tr>
    <td align="center">视图</td>
    <td>
    <select id="viewList" name="viewList" title="如果选择看板，则需在模块中配置相关参数">
      <option value="<%=ModuleSetupDb.VIEW_DEFAULT%>" selected>默认</option>
      <option value="<%=ModuleSetupDb.VIEW_LIST_GANTT%>">任务看板</option>
    </select>
    </td>
  </tr>
  <tr>
    <td align="center">
    状态
    </td>
    <td>
        <select id="cwsStatus" name="cwsStatus">
        <option value="-100">不限</option>
        <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DONE%>' selected>流程已结束</option>
        <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>'>流程未走完</option>
        <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_REFUSED%>'>流程被拒绝</option>
        <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DISCARD%>'>流程被放弃</option>
        </select></td>
  </tr>
  <tr>
    <td align="center">顺序</td>
    <td>
      <input id="mTagOrder" name="tagOrder" size="5" />
      <input name="code" value="<%=code%>" type="hidden" />
      <input name="formCode" value="<%=formCode%>" type="hidden" />
      
      <script>
        var mTagName = new LiveValidation('mTagName');
        mTagName.add(Validate.Presence);
       	var mTagOrder = new LiveValidation('mTagOrder');
        mTagOrder.add(Validate.Presence);	
      </script>  
    </td>
  </tr>
  <tr>
    <td colspan="2" align="center"><span style="PADDING-LEFT: 10px">
      <input class="btn" type="button" value="确定" onClick="createTabModuleRelate()" />
    </span></td>
    </tr>
</table>
</form>

<form action="module_view_edit.jsp?op=addSubTag" method="post" name="formSubTag" id="formSubTag">
<table class="tabStyle_1 percent80" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td class="tabStyle_1_title" colspan="2" align="center">添加简单链接选项卡</td>
  </tr>
  <tr>
    <td width="12%" align="center">名称</td>
    <td width="88%">
      <input id="tagName" name="tagName" />
    </td>
  </tr>
  <tr>
    <td align="center">链接</td>
    <td>
      <input id="tagUrl" name="tagUrl" style="width:300px" />
&nbsp;$code可替换为模块编码</td>
  </tr>
  <tr>
    <td align="center">顺序</td>
    <td>
      <input id="tagOrder" name="tagOrder" size="5" />
      <input name="code" value="<%=code%>" type="hidden" />
      <input name="formCode" value="<%=formCode%>" type="hidden" />
      <script>
        var tagName = new LiveValidation('tagName');
        tagName.add(Validate.Presence);
       	var tagUrl = new LiveValidation('tagUrl');
        tagUrl.add(Validate.Presence);        
       	var tagOrder = new LiveValidation('tagOrder');
        tagOrder.add(Validate.Presence);	
      </script>        
    </td>
  </tr>
  <tr>
    <td colspan="2" align="center"><span style="PADDING-LEFT: 10px">
      <input class="btn" name="submit" type="submit" value="确定" title="$formCode及$cwsId将会被自动替换为表单编码及记录的ID" />
    </span></td>
    </tr>
</table>
</form>
<form action="module_view_edit.jsp?op=addSubTag" method="post" name="formSubTagQuery" id="formSubTagQuery">
<table class="tabStyle_1 percent80" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td class="tabStyle_1_title" colspan="2" align="center">添加查询关联选项卡</td>
  </tr>
  <tr>
    <td align="center">名称</td>
    <td><input id="tagNameQuery" name="tagName" /></td>
  </tr>
  <tr>
    <td width="12%" align="center">查询</td>
    <td width="88%"><input id="queryId" name="queryId" type="hidden" />
      <span id="queryTitle"></span> <a href="javascript:;" onClick="selQuery()">选择查询</a> &nbsp;[<a href="javascript:" onClick="addFields()">添加映射</a>]</td>
  </tr>
  <tr>
    <td align="center">映射</td>
    <td>
      <div id="divFields">
      <%
        int count = 0;
        sub_nav_tag_url = "{'field1':'sqlfield1'}";
        JSONObject json = new JSONObject(sub_nav_tag_url);
        Iterator ir3 = json.keys();
        while (ir3.hasNext()) {
            String key = (String) ir3.next();
            %>
            <div id="divField<%=count%>">
              <div>
              <select id="field<%=count%>" name="field">
                <option value="">无</option>
                <option value="<%=FormSQLBuilder.PRIMARY_KEY_ID%>">主键ID</option>
                <%
                ir = fd.getFields().iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField)ir.next();
                    if (!ff.isCanQuery())
                        continue;
                    %>
                    <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
                	<%
                }
                %>
                </select>
              <font style="font-family:宋体">-&gt;</font>
              <span id="spanQueryField<%=count%>"></span>
              <a href='javascript:;' onClick="if ($(this).parent().parent().parent().children().length==1) {jAlert('至少需映射一个条件字段！','提示'); return;} var pNode=this.parentNode; pNode.parentNode.parentNode.removeChild(pNode.parentNode);">×</a>
              </div>
            </div>
        <%
            count++;
        }
        %>
        </div>    
      </td>
  </tr>
  <tr>
    <td align="center">顺序</td>
    <td><input id="tagOrderQuery" name="tagOrder" size="5" />
    <input id="tagUrlQuery" name="tagUrl" type="hidden" />
    <input name="code" value="<%=code%>" type="hidden" />
    <input name="formCode" value="<%=formCode%>" type="hidden" />
      <script>
        var tagNameQuery = new LiveValidation('tagNameQuery');
        tagNameQuery.add(Validate.Presence);       
       	var tagOrderQuery = new LiveValidation('tagOrderQuery');
        tagOrderQuery.add(Validate.Presence);	
      </script>     
    </td>
  </tr>
  <tr>
    <td colspan="2" align="center"><input class="btn" type="button" value="确定" onClick="createTab()" /></td>
  </tr>
</table>
</form>

<form action="module_view_edit.jsp?op=addSubTag" method="post" name="formSubTagReport" id="formSubTagReport">
<table class="tabStyle_1 percent80" width="95%" border="0" align="center" cellpadding="0" cellspacing="0" style="display:none">
  <tr>
    <td class="tabStyle_1_title" colspan="2" align="center">添加报表关联选项卡</td>
  </tr>
  <tr>
    <td align="center">名称</td>
    <td><input id="tagNameReport" name="tagName" /></td>
  </tr>
  <tr>
    <td width="12%" align="center">查询</td>
    <td width="88%"><input id="reportId" name="reportId" type="hidden" />
      <span id="reportTitle"></span> <a href="javascript:;" onClick="selReport()">选择报表</a> &nbsp;[<a href="javascript:" onClick="addFieldsReport()">添加映射</a>]</td>
  </tr>
  <tr>
    <td align="center">映射</td>
    <td>
      <div id="divFieldsReport">
      <%
        count = 0;
        sub_nav_tag_url = "{'field1':'sqlfield1'}";
        json = new JSONObject(sub_nav_tag_url);
        ir3 = json.keys();
        while (ir3.hasNext()) {
            String key = (String) ir3.next();
            %>
            <div id="divFieldReport<%=count%>">
              <div>
              <select id="field<%=count%>Report" name="fieldReport">
                <option value="">无</option>
                <option value="<%=FormSQLBuilder.PRIMARY_KEY_ID%>">主键ID</option>
                <%
                ir = fd.getFields().iterator();
                while (ir.hasNext()) {
                    FormField ff = (FormField)ir.next();
                    if (!ff.isCanQuery())
                        continue;
                    %>
                    <option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
                	<%
                }
                %>
                </select>
              <font style="font-family:宋体">-&gt;</font>
              <span id="spanReportField<%=count%>"></span>
              <a href='javascript:;' onClick="if ($(this).parent().parent().parent().children().length==1) {jAlert('至少需映射一个条件字段！','提示'); return;} var pNode=this.parentNode; pNode.parentNode.parentNode.removeChild(pNode.parentNode);">×</a>
              </div>
            </div>
        <%
            count++;
        }
        %>
        </div>    
      </td>
  </tr>
  <tr>
    <td align="center">顺序</td>
    <td><input id="tagOrderReport" name="tagOrder" size="5" />
    <input id="tagUrlReport" name="tagUrl" type="hidden" />
    <input name="code" value="<%=code%>" type="hidden" />
    <input name="formCode" value="<%=formCode%>" type="hidden" />
    </td>
  </tr>
  <tr>
    <td colspan="2" align="center"><input class="btn" type="button" value="确定" onClick="createTabReport()" /></td>
  </tr>
</table>
</form>
</body>
<script>
var fieldsMapStr = "";

function selQuery() {
	openWin("../flow/form_query_list_sel.jsp?type=all", 800, 600);
	fieldsMapStr = "";
}

function doSelQuery(id, title) {
	if (id==$('#queryId').val())
		return;
	
	$("#queryId").val(id);
	$("#queryTitle").html(title);
	
	$.ajax({
	   type: "POST",
	   url: "module_view_edit.jsp",
	   data: "op=getQueryCondField&id=" + id,
	   success: function(html){
			$("#spanQueryField0").html(html);
		}
	});
}

$.fn.outerHTML = function(){  return $("<p></p>").append(this.clone()).html(); }

function addFields() {
	if (o("queryId").value=="") {
		jAlert("请先选择查询！","提示");
		return;
	}	

	if (fieldsMapStr=="")
		fieldsMapStr = $("#divField0").outerHTML();
	$("#divFields").append(fieldsMapStr);
}

function createTab() {
	if (!LiveValidation.massValidate(tagNameQuery.formObj.fields)) {
		return;
	}	
	
	if (o("queryId").value=="") {
		jAlert("请先选择查询！","提示");
		return;
	}
	
	if (o('tagNameQuery').value=="") {
		jAlert("请输入名称！","提示");
		o('tagNameQuery').focus();
		return;
	}
	
	if (o('tagOrderQuery').value=="") {
		jAlert("请输入顺序！","提示");
		o('tagOrderQuery').focus();
		return;
	}
	
	// 字段合计描述字符串处理
	var str = "";
	var queryFields = $("select[name='queryField']");
	
	var map = new Map();
	var queryMap = new Map();
	var isFound = false;
	$("select[name='field']").each(function(i) {
		if ($(this).val()!="" && queryFields.eq(i).val()!="") {
			if (!map.containsKey($(this).val()))
				map.put($(this).val(), $(this).val());
			else {
				isFound = true;
				jAlert("表单中的字段 " + $(this).find("option:selected").text() + " 出现重复！","提示");
				return false;
			}
			
			if (!queryMap.containsKey(queryFields.eq(i).val())) {
				queryMap.put(queryFields.eq(i).val());
			}
			else {
				isFound = true;
				jAlert("查询条件中的字段 " + queryFields.eq(i).find("option:selected").text() + " 出现重复！","提示");
				return false;
			}

			if (str=="")
				str = "\"" + $(this).val() + "\":\"" + queryFields.eq(i).val() + "\"";
			else
				str += "," + "\"" + $(this).val() + "\":\"" + queryFields.eq(i).val() + "\"";
		}
	})
		
	if (isFound)
		return;

	if (str=="") {
		jAlert("请选择映射关系！","提示");
		return;
	}
	
	str += ",\"queryId\":\"" + o("queryId").value + "\"";
	
	str = "{" + str + "}";
	
	o("tagUrlQuery").value = str;
	
	o("formSubTagQuery").submit();
}

function createTabModuleRelate() {
	var str = "\"fieldSource\":\"" + o("fieldSource").value + "\", \"formRelated\":\"" + o("relateCode").value + "\", \"fieldRelated\":\"" + o("fieldRelated").value + "\", \"cwsStatus\":\"" + o("cwsStatus").value + "\", \"viewList\":\"" + o("viewList").value + "\"";
	str += ",\"fieldOtherRelated\":\"" + o("fieldOtherRelated").value + "\"";
	str += ",\"fieldOtherRelatedVal\":\"" + o("fieldOtherRelatedVal").value + "\"";
	str = "{" + str + "}";
	o("mTagUrl").value = str;
	if (!LiveValidation.massValidate(mTagName.formObj.fields)) {
		return;
	}		
	o("formSubTagModuleRelate").submit();
}

function selReport() {
	openWin("../flow/report/form_report_list.jsp?action=sel", 800, 600);	
}

function doSelReport(id, title) {
	if (id==$('#queryId').val())
		return;
	
	$("#queryIdReport").val(id);
	$("#queryTitleReport").html(title);
	
	$.ajax({
	   type: "POST",
	   url: "module_view_edit.jsp",
	   data: "op=getQueryCondField&id=" + id,
	   success: function(html){
			$("#spanQueryField0").html(html);
		}
	});
}

function addFieldsReport() {
	if (o("reportId").value=="") {
		jAlert("请先选择报表！","提示");
		return;
	}	

	if (fieldsMapStr=="")
		fieldsMapStr = $("#divFieldReport0").outerHTML();
	$("#divFieldsReport").append(fieldsMapStr);
}

function createTabReport() {
	if (o("reportId").value=="") {
		jAlert("请先选择报表！","提示");
		return;
	}
	
	if (o('tagNameReport').value=="") {
		jAlert("请输入名称！","提示");
		o('tagNameReport').focus();
		return;
	}
	
	if (o('tagOrderReport').value=="") {
		jAlert("请输入顺序！","提示");
		o('tagOrderReport').focus();
		return;
	}
	
	// 字段合计描述字符串处理
	var str = "";
	var queryFields = $("select[name='queryField']");
	
	var map = new Map();
	var queryMap = new Map();
	var isFound = false;
	$("select[name='fieldReport']").each(function(i) {
		if ($(this).val()!="" && queryFields.eq(i).val()!="") {
			if (!map.containsKey($(this).val()))
				map.put($(this).val(), $(this).val());
			else {
				isFound = true;
				jAlert("表单中的字段 " + $(this).find("option:selected").text() + " 出现重复！","提示");
				return false;
			}
			
			if (!queryMap.containsKey(queryFields.eq(i).val())) {
				queryMap.put(queryFields.eq(i).val());
			}
			else {
				isFound = true;
				jAlert("查询条件中的字段 " + queryFields.eq(i).find("option:selected").text() + " 出现重复！","提示");
				return false;
			}

			if (str=="")
				str = "\"" + $(this).val() + "\":\"" + queryFields.eq(i).val() + "\"";
			else
				str += "," + "\"" + $(this).val() + "\":\"" + queryFields.eq(i).val() + "\"";
		}
	})
		
	if (isFound)
		return;

	if (str=="") {
		jAlert("请选择映射关系！","提示");
		return;
	}
	
	str += ",\"reportId\":\"" + o("reportId").value + "\"";
	
	str = "{" + str + "}";
	
	o("tagUrlReport").value = str;
	
	o("formSubTagReport").submit();
}

function doSelReport(reportId, queryId, reportTitle) {
	if (reportId==$('#queryIdReport').val())
		return;
	
	$("#reportId").val(reportId);
	$("#reportTitle").html(reportTitle);
	
	$.ajax({
	   type: "POST",
	   url: "module_view_edit.jsp",
	   data: "op=getQueryCondField&id=" + queryId,
	   success: function(html){
			$("#spanReportField0").html(html);
		}
	});
}
</script>
</html>