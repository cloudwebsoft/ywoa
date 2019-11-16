<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "java.util.*"%>
<%@ page import="com.redmoon.oa.Config" %>
<%
String codeTop = ParamUtil.get(request, "code");
if (codeTop.equals("")) {
	codeTop = ParamUtil.get(request, "moduleCode");
}
if (codeTop.equals("")) {
	codeTop = ParamUtil.get(request, "formCode");
}

// 如果传了mainCode，则说明是从其它模块列表的头部选项卡链接过来
String mainCode = ParamUtil.get(request, "mainCode");
if (!"".equals(mainCode)) {
	codeTop = mainCode;
}

ModuleSetupDb msdTop = new ModuleSetupDb();
msdTop = msdTop.getModuleSetupDb(codeTop);

String formCodeTop = msdTop.getString("form_code");

int parentIdTop = ParamUtil.getInt(request, "parentId", -1);

String requestParamsTop;
StringBuffer requestParamsBuf = new StringBuffer();
Enumeration paramNames = request.getParameterNames();
while (paramNames.hasMoreElements()) {
    String paramName = (String) paramNames.nextElement();
    String[] paramValues = request.getParameterValues(paramName);
    if (paramValues.length == 1) {
        String paramValue = ParamUtil.getParam(request, paramName);
        // 过滤掉formCode等
		if (paramName.equals("code")
				|| paramName.equals("formCode")
				|| paramName.equals("moduleCode")
				|| paramName.equals("mainCode")
				|| paramName.equals("menuItem")
				|| paramName.equals("parentId")
				|| paramName.equals("moduleCodeRelated")
				|| paramName.equals("formCodeRelated")
				|| paramName.equals("mode") // 去掉mode及tagName，否则当存在mode=subTagRelated，关联模块中就会有问题
				|| paramName.equals("tagName")
				|| paramName.equals("id")
		) {
			;
		}
        else {
            StrUtil.concat(requestParamsBuf, "&", paramName + "=" + StrUtil.UrlEncode(paramValue));
        }
    }
}
requestParamsTop = requestParamsBuf.toString();
request.setAttribute("reqParams", requestParamsTop); // module_add.jsp中会调用到

String servletPathTop = request.getServletPath();
int pTop = servletPathTop.lastIndexOf("/");
servletPathTop = servletPathTop.substring(0, pTop);
if (!"".equals(mainCode)) {
	servletPathTop = "/visual";
}
// System.out.println(getClass() + " servletPathTop=" + servletPathTop);
%>
<div class="tabs1Box">
<div id="tabs1">
  <ul>
<%
// 当页面为edit、show、list即module_edit.jsp module_show.jsp module_list.jsp时不需要处理关联模块时
//if (!(pageTypeTop.equals("edit") || pageTypeTop.equals("show") || pageTypeTop.equals("list"))) {
if (parentIdTop==-1) {
	String moduleUrlList;
	if (msdTop.getInt("view_list")==ModuleSetupDb.VIEW_LIST_GANTT || msdTop.getInt("view_list")==ModuleSetupDb.VIEW_LIST_GANTT_LIST) {
		moduleUrlList = request.getContextPath() + servletPathTop + "/module_list_gantt.jsp?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop);
		%>
	    <li id="menu100">
	    <a href="<%=moduleUrlList%>"><span><%=msdTop.getString("name")%>看板</span></a>
	    </li>			
		<%		  
	}
	else {
		moduleUrlList = StrUtil.getNullStr(msdTop.getString("url_list"));
		if (moduleUrlList.equals("")) {
		  moduleUrlList = request.getContextPath() + "/visual/module_list.jsp?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop) + "&" + requestParamsTop;
		}
		else {
		  moduleUrlList = request.getContextPath() + "/" + moduleUrlList;
		  if (moduleUrlList.indexOf("&")==-1) {
		  	moduleUrlList += "?" + requestParamsTop;
		  }
		  else {
		  	moduleUrlList += "&" + requestParamsTop;
		  }
		}
		%>
	    <li id="menu1">    
	    <a href="<%=moduleUrlList%>"><span><%=msdTop.getString("name")%></span></a>
	    </li>			
		<%
	}
	if (msdTop.getInt("view_list")==ModuleSetupDb.VIEW_LIST_GANTT_LIST) {
		  moduleUrlList = request.getContextPath() + servletPathTop + "/module_list.jsp?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop) + "&" + requestParamsTop;	
	%>
    <li id="menu1"><a href="<%=moduleUrlList%>"><span><%=msdTop.getString("name")%></span></a></li>	
	<%
	}
    %>
	<%
	com.redmoon.oa.pvg.Privilege pvgTop = new com.redmoon.oa.pvg.Privilege();
	// 此处判断模块权限应该使用模块的code，formCodeTop此时为formCode
	ModulePrivDb mpdTop = new ModulePrivDb(codeTop);
	if (mpdTop.canUserAppend(pvgTop.getUser(request))) {
	%>
	<li id="menu2" style="display:none"><a href="<%=request.getContextPath() + servletPathTop%>/module_add.jsp?formCode=<%=formCodeTop%>&<%=requestParamsTop %>"><span>添加</span></a></li>
	<%}%>
	<%if (false && mpdTop.canUserSearch(pvgTop.getUser(request))) {
	%>
	<li id="menu3"><a href="<%=request.getContextPath() + servletPathTop%>/module_search.jsp?code=<%=codeTop%>&formCode=<%=formCodeTop%>&<%=requestParamsTop %>"><span>查询</span></a></li>
	<%}%>
	<%
	if (msdTop.getInt("btn_log_show")==1) {
		if (mpdTop.canUserLog(pvgTop.getUser(request)) || mpdTop.canUserManage(pvgTop.getUser(request))) {
			FormDb fdTop = new FormDb();
			fdTop = fdTop.getFormDb(formCodeTop);
			if (fdTop.isLog()) {
				long fdaoIdTop = ParamUtil.getLong(request, "id", -1);
				String opLog = "search";
				if (fdaoIdTop==-1) {
					opLog = "";
				}
				%>
				<li id="menu4"><a href="<%=request.getContextPath() + servletPathTop%>/module_log_list.jsp?op=<%=opLog%>&code=<%=codeTop%>&fdaoId=<%=fdaoIdTop%>&moduleFormCode=<%=formCodeTop%>&moduleCodeLog=<%=msdTop.getString("module_code_log")%>&<%=requestParamsTop %>"><span>修改日志</span></a></li>
				<%
					Config cfgTop = new Config();
					boolean isModuleLogRead = cfgTop.getBooleanProperty("isModuleLogRead");
					if (isModuleLogRead) {
				%>
	  			<li id="menu5"><a href="javascript:;" onclick="addTab('浏览日志', '<%=request.getContextPath()%>/visual/module_list.jsp?op=search&code=module_log_read&read_type=<%=FormDAOLog.READ_TYPE_MODULE%>&module_code=<%=codeTop%>&form_code=<%=formCode%>')"><span>浏览日志</span></a></li>
	  			<%
					}
			}
		}
	}
}
else {
	if (msdTop.getInt("view_show")==ModuleSetupDb.VIEW_SHOW_CUSTOM) {	
		%>
		<li id="menu1"><a href="<%=request.getContextPath()%>/<%=msdTop.getString("url_show")%>?id=<%=parentIdTop%>&parentId=<%=parentIdTop%>&code=<%=codeTop%>&formCode=<%=formCodeTop%>&<%=requestParamsTop %>"><span><%=msdTop.getString("name")%></span></a></li>
		<%
	}
	else {
		%>
		<li id="menu1"><a href="<%=request.getContextPath() + servletPathTop%>/module_show.jsp?id=<%=parentIdTop%>&parentId=<%=parentIdTop%>&code=<%=codeTop%>&formCode=<%=formCodeTop%>&<%=requestParamsTop %>"><span><%=msdTop.getString("name")%></span></a></li>
		<%
	}
}

int menuItemTop = 6;
if (parentIdTop==-1) {
	String[] tagsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("nav_tag_name")), ",");
	String[] tagUrlsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("nav_tag_url")), ",");
	int lenTop = 0;
	if (tagsTop!=null)
		lenTop = tagsTop.length;
	for (int i=0; i<lenTop; i++) {
		String url = tagUrlsTop[i];
		if (url.startsWith("{")) {
			JSONObject json = new JSONObject(url);
			String moduleCodeTop = json.getString("moduleCode");
			if (!"".equals(mainCode)) {
				url = request.getContextPath() + "/visual/module_list.jsp?code=" + moduleCodeTop + "&mainCode=" + mainCode + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
			}
			else {	
				url = request.getContextPath() + "/visual/module_list.jsp?code=" + moduleCodeTop + "&mainCode=" + codeTop + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
			}	
		}
		else {
			if (!url.startsWith("http")) {
				url = request.getContextPath() + "/" + url;
				if (url.indexOf("?")!=-1) {
					if (!"".equals(mainCode)) {
						url += "&mainCode=" + mainCode + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
					}
					else {	
						url += "&mainCode=" + codeTop + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
					}
				}
				else {
					if (!"".equals(mainCode)) {
						url += "?mainCode=" + mainCode + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
					}
					else {	
						url += "?mainCode=" + codeTop + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
					}			
				}
			}
		}
	%>
	    <li id="menu<%=menuItemTop%>"><a href="<%=url%>"><span><%=tagsTop[i]%></span></a></li>
	<%
		menuItemTop++;
	}
}

// 当需要处理关联模块时
// if (pageTypeTop.equals("edit") || pageTypeTop.equals("show") || pageTypeTop.equals("list")) {
if (parentIdTop!=-1) {
	// 关联模块标签	
	FormDb fdTop = new FormDb();
	fdTop = fdTop.getFormDb(formCodeTop);
	com.redmoon.oa.visual.FormDAO fdaoTop = new com.redmoon.oa.visual.FormDAO();
	fdaoTop = fdaoTop.getFormDAO(parentIdTop, fdTop);
	
	com.redmoon.oa.pvg.Privilege pvgTop = new com.redmoon.oa.pvg.Privilege();
	String curUserTop = pvgTop.getUser(request);			
	ModuleRelateDb mrdTop = new ModuleRelateDb();
	ModuleSetupDb msdRelate = new ModuleSetupDb();
	java.util.Iterator irTop = mrdTop.getModulesRelated(formCodeTop).iterator();
	while (irTop.hasNext()) {
		mrdTop = (ModuleRelateDb)irTop.next();
		// 有查看权限才能看到从模块选项卡
		ModulePrivDb mpdTop = new ModulePrivDb(mrdTop.getString("relate_code"));
		if (mpdTop.canUserSee(curUserTop) && mrdTop.getInt("is_on_tab")==1) {	
	        // 条件检查
	        String conds = StrUtil.getNullStr(mrdTop.getString("conds"));
	        if (!"".equals(conds)) {
	        	String cond = ModuleUtil.parseConds(request, fdaoTop, conds);
		    	javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
		        javax.script.ScriptEngine engine = manager.getEngineByName("javascript");
		        try {
		        	Boolean ret = (Boolean)engine.eval(cond);
		        	if (!ret.booleanValue()) {
		        		continue;
		        	}
		        }
		        catch (javax.script.ScriptException ex) {
		        	ex.printStackTrace();
		        }        	
	        }		
		
			msdRelate = msdRelate.getModuleSetupDb(mrdTop.getString("relate_code"));
	%>
			<li id="menu<%=menuItemTop%>"><a href="<%=request.getContextPath()%>/visual/module_list_relate.jsp?parentId=<%=parentIdTop%>&menuItem=<%=menuItemTop%>&code=<%=codeTop%>&moduleCodeRelated=<%=mrdTop.getString("relate_code")%>&formCode=<%=formCodeTop%>&<%=requestParamsTop %>"><span><%=msdRelate.getString("name")%></span></a></li>
	<%
		}
		menuItemTop++;
	}

	// 其它标签
	String[] subTagsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("sub_nav_tag_name")), "\\|");
	String[] subTagUrlsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("sub_nav_tag_url")), "\\|");
	int subLenTop = 0;
	if (subTagsTop!=null)
		subLenTop = subTagsTop.length;
	for (int i=0; i<subLenTop; i++) {
		String uri = ModuleUtil.filterViewEditTagUrl(request, codeTop, subTagsTop[i]);
		String url = "";
		if (uri.contains("?"))
			url = uri+"&parentId=" + parentIdTop + "&code="+codeTop+"&menuItem="+menuItemTop + "&" + requestParamsTop;
		else 
			url = uri+"?parentId=" + parentIdTop + "&code="+codeTop+"&menuItem="+menuItemTop + "&" + requestParamsTop;
	%>
		<li id="menu<%=menuItemTop%>" tagName="<%=subTagsTop[i]%>"><a href="<%=url%>"><span><%=subTagsTop[i]%></span></a></li>
	<%
		menuItemTop++;
	}
}
%>
  </ul>
</div>
</div>