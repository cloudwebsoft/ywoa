<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.ParamUtil"%>
<%@ page import="cn.js.fan.util.StrUtil"%>
<%@ page import="com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.flow.FormQueryDb"%>
<%@ page import="com.redmoon.oa.flow.FormQueryReportDb"%>
<%@ page import="com.redmoon.oa.flow.FormSQLBuilder"%>
<%@ page import="com.redmoon.oa.flow.query.QueryScriptUtil"%>
<%@ page import="com.redmoon.oa.sys.DebugUtil"%>
<%@ page import="com.redmoon.oa.ui.SkinMgr"%>
<%@ page import="com.redmoon.oa.visual.ModuleSetupDb"%>
<%@ page import="org.json.JSONException"%>
<%@ page import="org.json.JSONObject"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Iterator" %><%@ page import="java.util.Vector"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.flow")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>管理模块选项卡</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <script src="../inc/common.js"></script>
    <script src="../inc/map.js"></script>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
    <script src="../js/jquery.toaster.js"></script>
    <script src="<%=request.getContextPath() %>/inc/livevalidation_standalone.js"></script>

    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexbox/flexbox.css"/>
    <script type="text/javascript" src="../js/jquery.flexbox.js"></script>

    <script src="../js/select2/select2.js"></script>
    <link href="../js/select2/select2.css" rel="stylesheet"/>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>
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
if (sub_tags!=null) {
	len = sub_tags.length;
}
for (int i=0; i<len; i++) {
    String tagName = sub_tags[i];
%>
    <form action="module_view_edit.jsp" method="post" name="formSubTag<%=i%>" id="formSubTag<%=i%>">
        <tr id="tr<%=i%>" class="highlight">
            <td align="center"><%=i + 1%>
            </td>
            <td>
                <input name="newTagName" value="<%=tagName%>"/>
                <input name="code" value="<%=code%>" type="hidden"/>
                <input name="formCode" value="<%=formCode%>" type="hidden"/>
                <input name="tagName" value="<%=tagName%>" type="hidden"/>
            </td>
            <td>
                <%
                    if (sub_tagUrls[i].startsWith("{")) {
                        try {
                            JSONObject json = new JSONObject(sub_tagUrls[i]);
                            if (!json.isNull("queryId")) {
                                StringBuffer queryFieldDesc = new StringBuffer();
                                int qId = StrUtil.toInt(json.getString("queryId"));
                                FormQueryDb fqd = new FormQueryDb();
                                fqd = fqd.getFormQueryDb(qId);
                                if (fqd.isLoaded()) {
                                    if (!fqd.isScript()) {
                                        FormDb fdQuery = new FormDb();
                                        fdQuery = fdQuery.getFormDb(fqd.getTableCode());
                                        if (fdQuery.isLoaded()) {
                                            Iterator ir = json.keys();
                                            while (ir.hasNext()) {
                                                String key = (String) ir.next();
                                                if (key.equals("queryId")) {
                                                    continue;
                                                } else {
                                                    FormField ff = fdQuery.getFormField(json.getString(key));
                                                    if (ff != null) {
                                                        if (queryFieldDesc.length()==0) {
                                                            queryFieldDesc.append(ff.getTitle());
                                                        } else {
                                                            queryFieldDesc.append("，" + ff.getTitle());
                                                        }
                                                    } else {
                                                        DebugUtil.e(getClass(), "字段不存在：", json.getString(key));
                                                    }
                                                }
                                            }
                                        } else {
                                            DebugUtil.i(getClass(), "表格不存在，code", fqd.getTableCode());
                                        }
                                    }
                                    else {
                                        QueryScriptUtil qsu = new QueryScriptUtil();
                                        HashMap condFields = qsu.getCondFields(request, fqd);
                                        Iterator ir = json.keys();
                                        while (ir.hasNext()) {
                                            String key = (String) ir.next();
                                            if (key.equals("queryId")) {
                                                continue;
                                            } else {
                                                if (queryFieldDesc.length()==0) {
                                                    queryFieldDesc.append((String)condFields.get(json.getString(key)));
                                                } else {
                                                    queryFieldDesc.append("，" + (String)condFields.get(json.getString(key)));
                                                }
                                            }
                                        }
                                    }
                %>
                查询：
                <%
                    if (fqd.isScript()) {
                %>
                <a href="javascript:;" title="编辑查询" onClick="addTab('<%=fqd.getQueryName()%>', '<%=request.getContextPath()%>/flow/form_query_script.jsp?id=<%=qId%>')"><%=fqd.getQueryName()%>
                </a>
                <%
                    } else {
                %>
                <a href="javascript:;" title="编辑查询" onClick="addTab('<%=fqd.getQueryName()%>', '<%=request.getContextPath()%>/flow/designer/designer.jsp?id=<%=qId%>')"><%=fqd.getQueryName()%>
                </a>
                <%
                    }
                %>
                ，条件字段：<%=queryFieldDesc%>
                <%
                    } else {
                        out.print("查询不存在！");
                    }
                } else if (!json.isNull("reportId")) {
                    int reportId = StrUtil.toInt(json.getString("reportId"));
                    FormQueryReportDb fqrd = new FormQueryReportDb();
                    fqrd = (FormQueryReportDb) fqrd.getQObjectDb(new Integer(reportId));

                    if (fqrd != null) {
                        int qId = StrUtil.toInt(fqrd.getString("query_id"));
                        FormQueryDb fqd = new FormQueryDb();
                        fqd = fqd.getFormQueryDb(qId);
                        FormDb fdQuery = new FormDb();
                        fdQuery = fdQuery.getFormDb(fqd.getTableCode());
                        String queryFieldDesc = "";
                        Iterator ir = json.keys();
                        while (ir.hasNext()) {
                            String key = (String) ir.next();
                            if (key.equals("queryId")) {
                                continue;
                            } else {
                                FormField ff = fdQuery.getFormField(json.getString(key));
                                if (ff != null) {
                                    if (queryFieldDesc.equals("")) {
                                        queryFieldDesc = ff.getTitle();
                                    } else {
                                        queryFieldDesc += "，" + ff.getTitle();
                                    }
                                }
                            }
                        }
                %>
                报表：
                <a href="javascript:;" title="编辑报表" onClick="addTab('<%=fqrd.getString("title")%>', '<%=request.getContextPath()%>/flow/report/designer.jsp?id=<%=reportId%>')"><%=fqrd.getString("title")%>
                </a>
                ，主键字段：<%=queryFieldDesc%>
                <%
                    } else {
                        out.print("报表不存在！");
                    }
                } else if (!json.isNull("fieldSource")) {
                %>
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
                    } else {
                        FormField ff = fd.getFormField(fieldSource);
                        if (ff != null) {
                            ffTitle = ff.getTitle();
                        } else {
                            ffTitle = fieldSource + "不存在！";
                        }
                    }
                    String ffRelatedTitle = "";
                    if ("cws_id".equals(fieldRelated)) {
                        ffRelatedTitle = "cws_id";
                    } else {
                        if ("".equals(fieldRelated)) {
                            ffRelatedTitle = "无";
                        } else {
                            FormField ffRelated = fdRelated.getFormField(fieldRelated);
                            if (ffRelated != null) {
                                ffRelatedTitle = ffRelated.getTitle();
                            } else {
                                ffRelatedTitle = fieldRelated + "不存在";
                            }
                        }
                    }
                %>
                ，<%=ffTitle%>=<%=ffRelatedTitle%>
                <%
                    if (json.has("fieldOtherRelated")) {
                        FormField ff = fdRelated.getFormField(json.getString("fieldOtherRelated"));
                        if (ff != null) {
                %>
                ，<%=ff.getTitle()%>=<%=json.getString("fieldOtherRelatedVal")%>
                <%
                } else {
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
                        if (cwsStatus == -100) {
                            out.print("不限");
                        } else if (cwsStatus == com.redmoon.oa.flow.FormDAO.STATUS_DONE) {
                            out.print("流程已结束");
                        } else if (cwsStatus == com.redmoon.oa.flow.FormDAO.STATUS_NOT) {
                            out.print("流程未走完");
                        } else if (cwsStatus == com.redmoon.oa.flow.FormDAO.STATUS_REFUSED) {
                            out.print("流程被拒绝");
                        } else if (cwsStatus == com.redmoon.oa.flow.FormDAO.STATUS_DISCARD) {
                            out.print("流程被放弃");
                        }
                    }
                    if (json.has("viewList")) {
                %>
                ，视图：
                <%
                        int viewList = StrUtil.toInt(json.getString("viewList"), ModuleSetupDb.VIEW_DEFAULT);
                        if (viewList == ModuleSetupDb.VIEW_LIST_GANTT) {
                            out.print("任务看板");
                        } else if (viewList == ModuleSetupDb.VIEW_LIST_CALENDAR) {
                            out.print("日历看板");
                        } else {
                            out.print("默认");
                        }
                    }
                %>
                <%} %>
                <input name="tagUrl" type="hidden" value='<%=sub_tagUrls[i]%>'/>
                <%
                    } catch (JSONException e) {
                        out.print(e.getMessage());
                    }
                } else {%>
                <input name="tagUrl" size="35" value="<%=sub_tagUrls[i]%>"/>
                <%}%>
            </td>
            <td><input name="tagOrder" size="5" value="<%=sub_tagOrders[i]%>"/></td>
            <td align="center">
                <input class="btn" type="button" value="修改" onclick="modifySubTag('<%=i%>')"/>
                &nbsp;&nbsp;
                <input class="btn" name="button2" type="button" onclick="delSubTag('<%=i%>', '<%=tagName%>')" value="删除"/>
            </td>
        </tr>
    </form>
    <%
    }
    %>
</table>
<form action="module_view_edit.jsp" method="post" name="formSubTagModuleRelate" id="formSubTagModuleRelate">
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
			  Vector v = msd.listUsed();
			  Iterator ir = v.iterator();
			  String jsonStr = "";
			  while (ir.hasNext()) {
				  msd = (ModuleSetupDb) ir.next();

				  if (jsonStr.equals("")) {
					  jsonStr = "{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";
				  } else {
					  jsonStr += ",{\"id\":\"" + msd.getString("code") + "\", \"name\":\"" + msd.getString("name") + "\"}";
				  }
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
		<option value="<%=ModuleSetupDb.VIEW_LIST_CALENDAR%>">日历看板</option>
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
      <input id="btnTabModuleRelate" class="btn btn-default" type="button" value="确定" />
    </span>
	</td>
    </tr>
</table>
</form>

<form action="module_view_edit.jsp" method="post" name="formSubTag" id="formSubTag">
    <table class="tabStyle_1 percent80" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td class="tabStyle_1_title" colspan="2" align="center">添加简单链接选项卡</td>
        </tr>
        <tr>
            <td width="12%" align="center">名称</td>
            <td width="88%">
                <input id="tagName" name="tagName"/>
            </td>
        </tr>
        <tr>
            <td align="center">链接</td>
            <td>
                <input id="tagUrl" name="tagUrl" style="width:300px"/>
                &nbsp;$code可替换为模块编码
            </td>
        </tr>
        <tr>
            <td align="center">顺序</td>
            <td>
                <input id="tagOrder" name="tagOrder" size="5"/>
                <input name="code" value="<%=code%>" type="hidden"/>
                <input name="formCode" value="<%=formCode%>" type="hidden"/>
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
            <td colspan="2" align="center">
                  <input id="btnSimpleTag" class="btn" type="button" value="确定" title="$formCode及$cwsId将会被自动替换为表单编码及记录的ID"/>
            </td>
        </tr>
    </table>
</form>

<form action="module_view_edit.jsp" method="post" name="formSubTagQuery" id="formSubTagQuery">
    <table class="tabStyle_1 percent80" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
            <td class="tabStyle_1_title" colspan="2" align="center">添加查询关联选项卡</td>
        </tr>
        <tr>
            <td align="center">名称</td>
            <td><input id="tagNameQuery" name="tagName"/></td>
        </tr>
        <tr>
            <td width="12%" align="center">查询</td>
            <td width="88%"><input id="queryId" name="queryId" type="hidden"/>
                <span id="queryTitle"></span> <a href="javascript:;" onclick="selQuery()">选择查询</a> &nbsp;[<a href="javascript:" onClick="addFields()">添加映射</a>]
            </td>
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
                                        FormField ff = (FormField) ir.next();
                                        if (!ff.isCanQuery()) {
                                            continue;
                                        }
                                %>
                                <option value="<%=ff.getName()%>"><%=ff.getTitle()%>
                                </option>
                                <%
                                    }
                                %>
                            </select>
                            <span style="font-family:宋体">-&gt;</span>
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
            <td><input id="tagOrderQuery" name="tagOrder" size="5"/>
                <input id="tagUrlQuery" name="tagUrl" type="hidden"/>
                <input name="code" value="<%=code%>" type="hidden"/>
                <input name="formCode" value="<%=formCode%>" type="hidden"/>
                <script>
                    var tagNameQuery = new LiveValidation('tagNameQuery');
                    tagNameQuery.add(Validate.Presence);
                    var tagOrderQuery = new LiveValidation('tagOrderQuery');
                    tagOrderQuery.add(Validate.Presence);
                </script>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center"><input class="btn" type="button" value="确定" onClick="createTab()"/></td>
        </tr>
    </table>
</form>

<form action="module_view_edit.jsp" method="post" name="formSubTagReport" id="formSubTagReport">
    <table class="tabStyle_1 percent80" width="95%" border="0" align="center" cellpadding="0" cellspacing="0" style="display:none">
        <tr>
            <td class="tabStyle_1_title" colspan="2" align="center">添加报表关联选项卡</td>
        </tr>
        <tr>
            <td align="center">名称</td>
            <td><input id="tagNameReport" name="tagName"/></td>
        </tr>
        <tr>
            <td width="12%" align="center">查询</td>
            <td width="88%"><input id="reportId" name="reportId" type="hidden"/>
                <span id="reportTitle"></span> <a href="javascript:;" onClick="selReport()">选择报表</a> &nbsp;[<a href="javascript:" onClick="addFieldsReport()">添加映射</a>]
            </td>
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
                                        FormField ff = (FormField) ir.next();
                                        if (!ff.isCanQuery()) {
                                            continue;
                                        }
                                %>
                                <option value="<%=ff.getName()%>"><%=ff.getTitle()%>
                                </option>
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
            <td><input id="tagOrderReport" name="tagOrder" size="5"/>
                <input id="tagUrlReport" name="tagUrl" type="hidden"/>
                <input name="code" value="<%=code%>" type="hidden"/>
                <input name="formCode" value="<%=formCode%>" type="hidden"/>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center"><input class="btn" type="button" value="确定" onClick="createTabReport()"/></td>
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
        if (id == $('#queryId').val())
            return;

        $("#queryId").val(id);
        $("#queryTitle").html(title);

        $.ajax({
            type: "POST",
            url: "getQueryCondField.do",
            data: "id=" + id,
            success: function (html) {
                $("#spanQueryField0").html(html);
            }
        });
    }

    $.fn.outerHTML = function () {
        return $("<p></p>").append(this.clone()).html();
    };

    function addFields() {
        if (o("queryId").value == "") {
            jAlert("请先选择查询！", "提示");
            return;
        }

        if (fieldsMapStr == "")
            fieldsMapStr = $("#divField0").outerHTML();
        $("#divFields").append(fieldsMapStr);
    }

    function createTab() {
        if (!LiveValidation.massValidate(tagNameQuery.formObj.fields)) {
            return;
        }

        if (o("queryId").value == "") {
            jAlert("请先选择查询！", "提示");
            return;
        }

        if (o('tagNameQuery').value == "") {
            jAlert("请输入名称！", "提示");
            o('tagNameQuery').focus();
            return;
        }

        if (o('tagOrderQuery').value == "") {
            jAlert("请输入顺序！", "提示");
            o('tagOrderQuery').focus();
            return;
        }

        // 字段合计描述字符串处理
        var str = "";
        var queryFields = $("select[name='queryField']");

        var map = new Map();
        var queryMap = new Map();
        var isFound = false;
        $("select[name='field']").each(function (i) {
            if ($(this).val() != "" && queryFields.eq(i).val() != "") {
                if (!map.containsKey($(this).val()))
                    map.put($(this).val(), $(this).val());
                else {
                    isFound = true;
                    jAlert("表单中的字段 " + $(this).find("option:selected").text() + " 出现重复！", "提示");
                    return false;
                }

                if (!queryMap.containsKey(queryFields.eq(i).val())) {
                    queryMap.put(queryFields.eq(i).val());
                } else {
                    isFound = true;
                    jAlert("查询条件中的字段 " + queryFields.eq(i).find("option:selected").text() + " 出现重复！", "提示");
                    return false;
                }

                if (str == "")
                    str = "\"" + $(this).val() + "\":\"" + queryFields.eq(i).val() + "\"";
                else
                    str += "," + "\"" + $(this).val() + "\":\"" + queryFields.eq(i).val() + "\"";
            }
        });

        if (isFound)
            return;

        if (str == "") {
            jAlert("请选择映射关系！", "提示");
            return;
        }

        str += ",\"queryId\":\"" + o("queryId").value + "\"";
        str = "{" + str + "}";
        o("tagUrlQuery").value = str;

        $.ajax({
            type: "post",
            url: "addSubTag",
            data: $('#formSubTagQuery').serialize(),
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == 1) {
                    jAlert(data.msg, '提示', function () {
                        window.location.reload();
                    });
                } else {
                    jAlert(data.msg, '提示');
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    $(function () {
        $('#btnTabModuleRelate').click(function (e) {
            e.preventDefault();
            createTabModuleRelate();
        });
    });

    function createTabModuleRelate() {
        var str = "\"fieldSource\":\"" + o("fieldSource").value + "\", \"formRelated\":\"" + o("relateCode").value + "\", \"fieldRelated\":\"" + o("fieldRelated").value + "\", \"cwsStatus\":\"" + o("cwsStatus").value + "\", \"viewList\":\"" + o("viewList").value + "\"";
        str += ",\"fieldOtherRelated\":\"" + o("fieldOtherRelated").value + "\"";
        str += ",\"fieldOtherRelatedVal\":\"" + o("fieldOtherRelatedVal").value + "\"";
        str = "{" + str + "}";
        o("mTagUrl").value = str;
        if (!LiveValidation.massValidate(mTagName.formObj.fields)) {
            return;
        }

        $.ajax({
            type: "post",
            url: "addSubTag",
            data: $('#formSubTagModuleRelate').serialize(),
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                if (data.ret == 1) {
                    jAlert(data.msg, '提示', function () {
                        window.location.reload();
                    });
                } else {
                    jAlert(data.msg, '提示');
                }
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }

    function selReport() {
        openWin("../flow/report/form_report_list.jsp?action=sel", 800, 600);
    }

    function doSelReport(id, title) {
        if (id == $('#queryId').val())
            return;

        $("#queryIdReport").val(id);
        $("#queryTitleReport").html(title);

        $.ajax({
            type: "POST",
            url: "getQueryCondField",
            data: "id=" + id,
            success: function (html) {
                $("#spanQueryField0").html(html);
            }
        });
    }

    function addFieldsReport() {
        if (o("reportId").value == "") {
            jAlert("请先选择报表！", "提示");
            return;
        }

        if (fieldsMapStr == "")
            fieldsMapStr = $("#divFieldReport0").outerHTML();
        $("#divFieldsReport").append(fieldsMapStr);
    }

    function createTabReport() {
        if (o("reportId").value == "") {
            jAlert("请先选择报表！", "提示");
            return;
        }

        if (o('tagNameReport').value == "") {
            jAlert("请输入名称！", "提示");
            o('tagNameReport').focus();
            return;
        }

        if (o('tagOrderReport').value == "") {
            jAlert("请输入顺序！", "提示");
            o('tagOrderReport').focus();
            return;
        }

        // 字段合计描述字符串处理
        var str = "";
        var queryFields = $("select[name='queryField']");

        var map = new Map();
        var queryMap = new Map();
        var isFound = false;
        $("select[name='fieldReport']").each(function (i) {
            if ($(this).val() != "" && queryFields.eq(i).val() != "") {
                if (!map.containsKey($(this).val()))
                    map.put($(this).val(), $(this).val());
                else {
                    isFound = true;
                    jAlert("表单中的字段 " + $(this).find("option:selected").text() + " 出现重复！", "提示");
                    return false;
                }

                if (!queryMap.containsKey(queryFields.eq(i).val())) {
                    queryMap.put(queryFields.eq(i).val());
                } else {
                    isFound = true;
                    jAlert("查询条件中的字段 " + queryFields.eq(i).find("option:selected").text() + " 出现重复！", "提示");
                    return false;
                }

                if (str == "")
                    str = "\"" + $(this).val() + "\":\"" + queryFields.eq(i).val() + "\"";
                else
                    str += "," + "\"" + $(this).val() + "\":\"" + queryFields.eq(i).val() + "\"";
            }
        });

        if (isFound)
            return;

        if (str == "") {
            jAlert("请选择映射关系！", "提示");
            return;
        }

        str += ",\"reportId\":\"" + o("reportId").value + "\"";

        str = "{" + str + "}";

        o("tagUrlReport").value = str;

        o("formSubTagReport").submit();
    }

    function doSelReport(reportId, queryId, reportTitle) {
        if (reportId == $('#queryIdReport').val())
            return;

        $("#reportId").val(reportId);
        $("#reportTitle").html(reportTitle);

        $.ajax({
            type: "POST",
            url: "getQueryCondField.do",
            data: "id=" + queryId,
            success: function (html) {
                $("#spanReportField0").html(html);
            }
        });
    }

    $(function () {
        $('#btnSimpleTag').click(function (e) {
            e.preventDefault();

            $.ajax({
                type: "post",
                url: "addSubTag",
                data: $('#formSubTag').serialize(),
                dataType: "html",
                beforeSend: function (XMLHttpRequest) {
                    $('body').showLoading();
                },
                success: function (data, status) {
                    data = $.parseJSON(data);
                    if (data.ret == 1) {
                        jAlert(data.msg, '提示', function () {
                            window.location.reload();
                        });
                    } else {
                        jAlert(data.msg, '提示');
                    }
                },
                complete: function (XMLHttpRequest, status) {
                    $('body').hideLoading();
                },
                error: function (XMLHttpRequest, textStatus) {
                    // 请求出错处理
                    jAlert(XMLHttpRequest.responseText, "提示");
                }
            });
        })
    });

    function delSubTag(index, tagName) {
        jConfirm('您确定要删除么？', '提示', function (r) {
            if (!r) {
                return;
            } else {
                $.ajax({
                    type: "post",
                    url: "delSubTag",
                    data: $('#formSubTag' + index).serialize(),
                    dataType: "html",
                    beforeSend: function (XMLHttpRequest) {
                        $('body').showLoading();
                    },
                    success: function (data, status) {
                        data = $.parseJSON(data);
                        if (data.ret == 1) {
                            $('#tr' + index).remove();
                        }
                        $.toaster({priority: 'info', message: data.msg});
                    },
                    complete: function (XMLHttpRequest, status) {
                        $('body').hideLoading();
                    },
                    error: function (XMLHttpRequest, textStatus) {
                        // 请求出错处理
                        jAlert(XMLHttpRequest.responseText, "提示");
                    }
                });
            }
        })
    }

    function modifySubTag(index, tagName) {
        $.ajax({
            type: "post",
            url: "modifySubTag",
            data: $('#formSubTag' + index).serialize(),
            dataType: "html",
            beforeSend: function (XMLHttpRequest) {
                $('body').showLoading();
            },
            success: function (data, status) {
                data = $.parseJSON(data);
                $.toaster({priority: 'info', message: data.msg});
            },
            complete: function (XMLHttpRequest, status) {
                $('body').hideLoading();
            },
            error: function (XMLHttpRequest, textStatus) {
                // 请求出错处理
                jAlert(XMLHttpRequest.responseText, "提示");
            }
        });
    }
</script>
</html>