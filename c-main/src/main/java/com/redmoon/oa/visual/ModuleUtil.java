package com.redmoon.oa.visual;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.api.IBasicSelectCtl;
import com.cloudweb.oa.api.ICondUtil;
import com.cloudweb.oa.api.IModuleUtil;
import com.cloudweb.oa.bean.ExportExcelItem;
import com.cloudweb.oa.cache.ExportExcelCache;
import com.cloudweb.oa.cond.CondUnit;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import com.cloudweb.oa.utils.SysUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.*;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.query.QueryScriptUtil;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.sys.DebugUtil;
import com.redmoon.oa.util.RequestUtil;
import com.redmoon.oa.visual.func.CalculateFuncImpl;
import com.redmoon.oa.visual.func.ConnStrFuncImpl;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ModuleUtil {
	/**
	 * 用于在request置ModuleSetupDb属性，生成SQL语句时将会调用此属性
	 */
	public static final String MODULE_SETUP = "MODULE_SETUP";
	/**
	 * 用于在request属性中保存过滤条件，在module_list_nest_sel.jsp拉单页面中调用，生成SQL语句时将会调用此属性
	 */
	public static final String NEST_SHEET_FILTER = "NEST_SHEET_FILTER";

	public static final String NEST_SHEET_FILTER_USE_MODULE = "isUseModuleFilter";
	
	public static final String FILTER_CUR_USER = "{$curUser}";
	public static final String FILTER_CUR_USER_DEPT = "{$curUserDept}";
	public static final String FILTER_CUR_USER_ROLE = "{$curUserRole}";
	public static final String FILTER_ADMIN_DEPT = "{$admin.dept}";
	public static final String FILTER_CUR_USER_DEPT_AND_CHILDREN = "{$curUserDeptAndChildren}";
	/**
	 * 主表ID
	 */
	public static final String FILTER_MAIN_ID = "{$mainId}";
	
    public static final String seperator = "-|-";

    public static final String CHECKBOX_GROUP_PREFIX = "CHECK_GROUP_";

	/**
	 * 当前日期
	 */
	public static final String FILTER_CUR_DATE = "{$curDate}";

	public static final String FILTER_CUR_YEAR = "{$curYear}";

	public static final String FILTER_CUR_MONTH = "{$curMonth}";

	public ModuleUtil() {
        super();
    }
    
    public static String getFilterDesc(HttpServletRequest request, String preStr) {
    	if (FILTER_CUR_USER.equals(preStr)) {
    		return "当前用户";
    	}
    	else if (FILTER_CUR_USER_DEPT.equals(preStr)) {
    		return "当前用户所在的部门";
    	}
    	else if (FILTER_CUR_USER_DEPT_AND_CHILDREN.equals(preStr)) {
    		return "当前用户所在的部门及其子部门";
		}
    	else if (FILTER_CUR_USER_ROLE.equals(preStr)) {
    		return "当前用户的角色";
    	}
    	else if (FILTER_ADMIN_DEPT.equals(preStr)) {
    		return "当前用户分管的部门";
    	}    	
    	else if (FILTER_CUR_DATE.equals(preStr)) {
    		return "当前日期";
    	}
		else if (FILTER_CUR_YEAR.equals(preStr)) {
			return "当前年份";
		}
		else if (FILTER_CUR_MONTH.equals(preStr)) {
			return "当前月份";
		}
    	else if (FILTER_MAIN_ID.equals(preStr)) {
    		return "主表ID";
		}
    	else {
    		return "";
    	}
    }
    
    public static String getModuleSubTagUrl(String moduleCode, String tagName) {
       	ModuleSetupDb msd = new ModuleSetupDb();
    	msd = msd.getModuleSetupDb(moduleCode);
    	
    	String tName = StrUtil.getNullStr(msd.getString("sub_nav_tag_name"));	
    	String tUrl = StrUtil.getNullStr(msd.getString("sub_nav_tag_url"));

    	String[] nameAry = StrUtil.split(tName, "\\|");
    	String[] urlAry = tUrl.split("\\|");
    	
    	if (nameAry==null) {
    		return "";
    	}
    	String tagUrl = "";
    	for (int i=0; i<nameAry.length; i++) {
    		if (nameAry[i].equals(tagName)) {
    			tagUrl = urlAry[i];
    			break;
    		}
    	}
    	return tagUrl;
    }

	/**
	 * 过滤选项卡的链接，20130815，原同名方法为保持兼容性仍保留
	 * @param request
	 * @param moduleCode
	 * @param tagName
     * @return
     */
    public static String filterViewEditTagUrl(HttpServletRequest request, String moduleCode, String tagName) {
        // 在module_show.jsp中，setAttribute了cwsId
    	String cwsId = StrUtil.getNullStr((String)request.getAttribute("cwsId"));
    	if (cwsId.equals("")) {
    		cwsId = ParamUtil.get(request, "moduleId");
    	}
    	
    	String tagUrl = getModuleSubTagUrl(moduleCode, tagName);
    	if (tagUrl.equals("")) {
    		return "";
    	}
    	
    	if (tagUrl.startsWith("{")) {
    		try {
				JSONObject json = new JSONObject(tagUrl);
				int queryId = -1;
				int reportId = -1;
				try {
					String qId = json.getString("queryId");
					queryId = StrUtil.toInt(qId, -1);
				}
				catch (JSONException e) {
				}
				
				if (queryId==-1) {
					try {
						String rId = json.getString("reportId");
						reportId = StrUtil.toInt(rId, -1);
					}
					catch (JSONException e) {
					}					
				}
				
				String fieldSource = "";
				if (!json.isNull("fieldSource")) {
					fieldSource = json.getString("fieldSource");
				}
				
				if (queryId!=-1) {
					FormQueryDb fqd = new FormQueryDb();
					fqd = fqd.getFormQueryDb(queryId);
					if (fqd.isScript()) {
						tagUrl = request.getContextPath() + "/flow/form_query_script_list_do.jsp?id=" + queryId + "&parentId=" + cwsId + "&moduleId=" + cwsId + "&moduleCode=" + StrUtil.UrlEncode(moduleCode) + "&mode=moduleTag&tagName=" + StrUtil.UrlEncode(tagName);				
					}
					else {
						tagUrl = request.getContextPath() + "/flow/form_query_list_do.jsp?id=" + queryId + "&parentId=" + cwsId + "&moduleId=" + cwsId + "&moduleCode=" + StrUtil.UrlEncode(moduleCode) + "&mode=moduleTag&tagName=" + StrUtil.UrlEncode(tagName);
					}
				}
				else if (reportId!=-1){
					tagUrl = request.getContextPath() + "/flow/report/form_report_show_jqgrid.jsp?reportId=" + reportId + "&parentId=" + cwsId + "&moduleId=" + cwsId + "&moduleCode=" + StrUtil.UrlEncode(moduleCode) + "&mode=moduleTag&tagName=" + StrUtil.UrlEncode(tagName);
				}
				else if (!"".equals(fieldSource)) {
					// 通过选项卡标签关联
					String servletPath = request.getServletPath();
					int pTop = servletPath.lastIndexOf("/");
					servletPath = servletPath.substring(0, pTop);
					tagUrl = request.getContextPath() + servletPath + "/moduleListRelatePage.do?mode=subTagRelated&tagName=" + StrUtil.UrlEncode(tagName);
				}
			} catch (JSONException e) {
				LogUtil.getLog(ModuleUtil.class).error(e);
			}
    	}
    	else {
    		tagUrl = tagUrl.replaceAll("\\$formCode", moduleCode);
    		tagUrl = tagUrl.replaceAll("\\$code", moduleCode); // 为保持向下兼容，所以保留上面的$formCode
        	tagUrl = tagUrl.replaceAll("\\$cwsId", cwsId); // cwsId即parentId，为主模块记录的ID
			if (!tagUrl.startsWith("http:") && !tagUrl.startsWith("https:")) {
                tagUrl = request.getContextPath() + "/" + tagUrl;
            }
    	}
    	
        return tagUrl;
    }
    
    /**
     * 为保证兼容性，暂时保留
     * @param request
     * @param tagUrl
     * @return
     */
    public static String filterViewEditTagUrl(HttpServletRequest request, String tagUrl) {
        String formCode = ParamUtil.get(request, "formCode");
        String cwsId = StrUtil.getNullStr((String)request.getAttribute("cwsId"));

		tagUrl = tagUrl.replaceAll("\\$formCode", formCode);
		tagUrl = tagUrl.replaceAll("\\$cwsId", cwsId);
    	
        return tagUrl;
    }
    
    /**
     * 从request取出条件中的过滤字段，组装成pair，以便于在module_list.jsp中分页时带入request传入的参数
     * @param request
     * @param msd
     * @return
     */
    public static Map getFilterParams(HttpServletRequest request, ModuleSetupDb msd) {
		Privilege pvg = new Privilege();
		String userName = pvg.getUser(request);
       	String filter = StrUtil.getNullStr(msd.getFilter(userName));
    	Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(filter);
        Map map = new HashMap();
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String str = m.group(1);
            String val = "";
            if (str.startsWith("request.")) {
            	String key = str.substring("request.".length());
            	val = ParamUtil.get(request, key);
            	map.put(key, val);
            }
        }
        return map;
    }

	/**
	 * 解析关联模块条件，操作列链接显示的条件
	 * @param request
	 * @param ifdao
	 * @param conds
     * @return
     */
    public static String parseConds(HttpServletRequest request, IFormDAO ifdao, String conds) {
		return ((IModuleUtil)SpringUtil.getBean("moduleUtilService")).parseConds(request, ifdao, conds);
    }        
    
    /**
     * 解析模块验证条件，已被“验证规则”取代
     * @param request HttpServletRequest
     * @param fu FileUpload
	 * @param ifdao IFormDAO
     * @param filter String
     * @return
     */
    public static String parseValidate(HttpServletRequest request, FileUpload fu, IFormDAO ifdao, String filter) {
		FormDb fd = ifdao.getFormDb();
    	if (filter.startsWith("<items>")) {
            List filedList = new ArrayList();
            Iterator ir1 = fd.getFields().iterator();
            while(ir1.hasNext()){
         	   FormField ff =  (FormField)ir1.next();
         	   filedList.add(ff.getName());
            }

			SAXBuilder parser = new SAXBuilder();
			org.jdom.Document doc;
			try {
				doc = parser.build(new InputSource(new StringReader(filter)));
				
				StringBuffer sb = new StringBuffer();
				
				Element root = doc.getRootElement();
				List<Element> vroot = root.getChildren();
				int i = 0;							
				if (vroot != null) {
					String lastLogical = "";
					for (Element e : vroot) {
							String name = e.getChildText("name");
							String fieldName = e.getChildText("fieldName");
							String op = e.getChildText("operator");
							
							op = op.replaceAll("&lt;", "<");
							op = op.replaceAll("&gt;", ">");
							
							String logical = e.getChildText("logical");
							String value = e.getChildText("value");
							String firstBracket = e.getChildText("firstBracket");
							String twoBracket = e.getChildText("twoBracket");
							
							if(!filedList.contains(fieldName)) {
								break;
							}
							
							if(null == firstBracket || firstBracket.equals("")) {
								firstBracket = "";
							}
							if(null == twoBracket || twoBracket.equals("")){
								twoBracket = "";
							}
							if (name.equals(WorkflowPredefineDb.COMB_COND_TYPE_FIELD)) {
								String fieldVal = fu.getFieldValue(fieldName);
								if (fieldVal==null) {
									fieldVal = ifdao.getFieldValue(fieldName);
								}
								
								sb.append(firstBracket);

								FormField ff = fd.getFormField(fieldName);
							    if (ff.getFieldType()==FormField.FIELD_TYPE_VARCHAR || ff.getFieldType()==FormField.FIELD_TYPE_TEXT) {
							    	if ("=".equals(op)) {
							    		sb.append(fieldVal.equals(value));
							    	}
							    	else {
							    		// 不等于
							    		sb.append(!fieldVal.equals(value));						    		
							    	}
							    }
							    else if (ff.getFieldType()==FormField.FIELD_TYPE_DATE || ff.getFieldType()==FormField.FIELD_TYPE_DATETIME) {
						    		java.util.Date dt = null, dtValue = null;
						    		if (ff.getFieldType()==FormField.FIELD_TYPE_DATE) {
						    			dt = DateUtil.parse(fieldVal, "yyyy-MM-dd");
						    			dtValue = DateUtil.parse(value, "yyyy-MM-dd");
						    		}
						    		else {
						    			dt = DateUtil.parse(fieldVal, "yyyy-MM-dd HH:mm:ss");							    			
						    			dtValue = DateUtil.parse(value, "yyyy-MM-dd HH:mm:ss");
						    		}
						    		
						    		int r = DateUtil.compare(dt, dtValue);
						    		if ("=".equals(op)) {
						    			sb.append(r==0);
							    	}
						    		else if (">".equals(op)) {
						    			sb.append(r==1);
						    		}
						    		else if ("<".equals(op)) {
						    			sb.append(r==2);
						    		}
						    		else if (">=".equals(op)) {
						    			sb.append(r==1 || r==0);
						    		}
						    		else {
						    			sb.append(r==2 || r==0);
						    		}
							    }
							    else {
							    	double dbVal = StrUtil.toDouble(fieldVal, -1);
							    	
							    	ScriptEngineManager manager = new ScriptEngineManager();
							        ScriptEngine engine = manager.getEngineByName("javascript");
							        try {
							        	Boolean re = (Boolean)engine.eval(dbVal + op + value);
							        	sb.append(re);
							        }
							        catch (ScriptException ex) {
							        	LogUtil.getLog(ModuleUtil.class).error(ex);
							        }						    	
							    }

							    sb.append(twoBracket);										

							}
							
							// 去除最后一个逻辑判断
							if ( i!=vroot.size()-1 ) {
								sb.append(" " + logical + " ");
								lastLogical = logical;
							}
												
						i++;
					}
					String tempCond = sb.toString();
					//校验括弧对称性
					//boolean flag = checkComCond(tempCond);

					// 如果配置了条件
					if (!tempCond.equals("")) {
						String script = sb.toString();
						int p = script.lastIndexOf(" " + lastLogical + " ");
						
						LogUtil.getLog(ModuleUtil.class).info("filter script=" + script);
						if (p!=-1) {
							script = script.substring(0, p);
						}
						LogUtil.getLog(ModuleUtil.class).info("filter script2=" + script);
						
						filter = tempCond;
					}						
				}							
			} catch (JDOMException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
    	
    	filter = filter.replace(" and ", " && ");
    	filter = filter.replace(" or ", " || ");
    	
    	return filter;
    }

    public static String encodeFilter(String filter) {
		filter = filter.replaceAll("#", "%sharp");
		return filter;
	}
    
    /**
     * 解码filter中的回车换行
     * @param filter
     * @return
     */
    public static String decodeFilter(String filter) {
    	filter = StrUtil.decodeJSON(filter);
    	
    	String patternStr = "%rn";
        String replacementStr = "\r\n"; // 回车换行
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(filter);
        filter = matcher.replaceAll(replacementStr);    
        
        patternStr = "%n"; //
        replacementStr = "\n"; // 回车换行
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(filter);
        filter = matcher.replaceAll(replacementStr);      
        
        patternStr = "%simq"; //
        replacementStr = "\""; // 双引号
        pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher(filter);
        filter = matcher.replaceAll(replacementStr);    
        
    	// 当filter在字段的description中被StrUtil.decodeJSON时，<>会被还原，所以此处需转码回去
        filter = filter.replaceAll("><=</", ">&lt;=</"); // <operator><=</operator>
        filter = filter.replaceAll("><</", ">&lt;</");
        filter = filter.replaceAll(">>=</", ">&gt;=</");
		filter = filter.replaceAll(">></", ">&gt;</");
		filter = filter.replaceAll("><></", ">&lt;&gt;</");

		filter = filter.replaceAll("%sharp", "#");

		return filter;
    }
    
    /**
     * 解析拉单时的过滤条件
     * @param request
     * @param formCode
     * @param filter
     * @return
     */
    public static String[] parseFilter(HttpServletRequest request, String formCode, String filter) {
		return ((IModuleUtil) SpringUtil.getBean("moduleUtilService")).parseFilter(request, formCode, filter);
    }

	/**
	 * 解析模块中的过滤条件
	 * @param request
	 * @return
     */
    public static String[] parseFilter(HttpServletRequest request) {
    	ModuleSetupDb msd = (ModuleSetupDb)request.getAttribute(MODULE_SETUP);
    	if (msd==null) {
			return null;
		}

		Privilege pvg = new Privilege();
		String userName = pvg.getUser(request);
    	String filter = StrUtil.getNullStr(msd.getFilter(userName));

		return parseFilter(request, msd.getString("form_code"), filter);
    }    

    public static String getResultStr(String tableName, String sql, Map fieldsExcluded) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("[table]\r\n");
    	sb.append("[name]" + tableName + "[/name]\r\n");
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
	    	sb.append("[cols]");
			Map mapType = ri.getMapType();
			Iterator ir = mapType.keySet().iterator();
			int i = 0;
			while (ir.hasNext()) {
				String keyName = (String) ir.next();
				if (fieldsExcluded!=null) {
					if (fieldsExcluded.containsKey(keyName.toLowerCase()) || fieldsExcluded.containsKey(keyName.toUpperCase())) {
						continue;
					}
				}
				if (i==0) {
					sb.append(keyName);
				}
				else {
					sb.append("|" + keyName);
				}
				i++;
			}
			sb.append("[/cols]\r\n");
			sb.append("[records]\r\n");
	    	while (ri.hasNext()) {
	    		ResultRecord rr = (ResultRecord)ri.next();
	    		i = 0;
				ir = mapType.keySet().iterator();
				sb.append("[record]");
				while (ir.hasNext()) {
					String keyName = (String) ir.next();
					if (fieldsExcluded!=null) {
						if (fieldsExcluded.containsKey(keyName.toLowerCase()) || fieldsExcluded.containsKey(keyName.toUpperCase())) {
							continue;
						}
					}					
					String val = rr.getString(keyName);
					if (i==0) {
						sb.append(val);
					}
					else {
						sb.append(seperator + val);
					}
					i++;
				}
				sb.append("[/record]\r\n");
	    	}
			sb.append("[/records]\r\n");			
		} catch (SQLException e) {
			LogUtil.getLog(ModuleUtil.class).error(e);
		}
    	sb.append("[/table]\r\n");
		
		return sb.toString();
    }

    /**
     * 导出模块，已弃用
     * @param moduleCode
     * @return
     */
    public static String exportModule(String moduleCode) {
    	// 不导出表单及视图，因为要生成表，并且表单与流程是关联的，即用在哪种流程下
    	ModuleSetupDb msd = new ModuleSetupDb();
    	msd = msd.getModuleSetupDb(moduleCode);
    	
    	StringBuffer sb = new StringBuffer();
    	sb.append("[moduleCode]" + moduleCode + "[/moduleCode]\r\n");
    	sb.append("[moduleKind]" + msd.getInt("kind") + "[/moduleKind]\r\n");
    	
    	sb.append("[formCode]" + msd.getString("form_code") + "[/formCode]\r\n");
    	
    	// 导出模块
    	String sql = "select * from visual_module_setup where code=" + StrUtil.sqlstr(moduleCode);
    	sb.append(getResultStr("visual_module_setup", sql, null));

    	// 导出权限
    	// visual_module_priv
    	Map<String, String> fieldsExcluded = new HashMap<>();
    	fieldsExcluded.put("id", "");
    	sql = "select * from visual_module_priv where form_code=" + StrUtil.sqlstr(moduleCode);
    	sb.append(getResultStr("visual_module_priv", sql, fieldsExcluded));

    	// 导出查询
    	String[] subTagsTop = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_name")), "\\|");
    	// String[] subTagUrlsTop = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_url")), "\\|");
    	int subLenTop = 0;
    	if (subTagsTop!=null) {
			subLenTop = subTagsTop.length;
		}
    	for (int i=0; i<subLenTop; i++) {
        	String tagUrl = getModuleSubTagUrl(moduleCode, subTagsTop[i]);
    		
        	if (tagUrl.startsWith("{")) {
        		try {
    				JSONObject json = new JSONObject(tagUrl);
    				int queryId = -1;
    				int reportId = -1;
    				try {
    					String qId = json.getString("queryId");
    					queryId = StrUtil.toInt(qId, -1);
    				}
    				catch (JSONException e) {
    				}

    				if (queryId!=-1) {
    			    	sql = "select * from form_query where id=" + queryId;
    			    	sb.append(getResultStr("form_query", sql, fieldsExcluded));
    			    	
    			    	// 记录选项卡中的查询的对应关系
    			    	sb.append("[module_tag_query]" + moduleCode + ":" + subTagsTop[i] + "[/module_tag_query]\r\n");
    					
    			    	sql = "select * from form_query_condition where query_id=" + queryId;
    			    	sb.append(getResultStr("form_query_condition", sql, fieldsExcluded));    					
    				}
    			} catch (JSONException e) {
    				// TODO Auto-generated catch block
    				LogUtil.getLog(ModuleUtil.class).error(e);
    			}
        	}        	
    	}

    	// 导出查询权限，因为查询条件关联的是查询的ID，所以暂无法导出

    	// 导出关联关系
    	// visual_module_relate
    	
    	sql = "select * from visual_module_relate where code=" + StrUtil.sqlstr(moduleCode);
    	sb.append(getResultStr("visual_module_relate", sql, null));    	

    	// 根据关联关系导出从表单

    	// 导出从表单的字段

    	// 根据从表单导出从模块

    	// 导出子模块的权限 

    	// 导出提醒，不含id
    	sql = "select * from form_remind where table_name=" + StrUtil.sqlstr(msd.getString("form_code"));
    	sb.append(getResultStr("form_remind", sql, fieldsExcluded));  
    	
    	return sb.toString();
    }
    
    /**
     * 导出解决方案
     * @param formCodes
     * @return
     */
    public static String exportSolution(String formCodes) {
    	// 判断许可证是可以导出解决方案
    	License license = com.redmoon.oa.kernel.License.getInstance();
        if (!license.canExportSolution()) {
        	return license.getLicenseStr();
        }
    	
        MacroCtlMgr mm = new MacroCtlMgr();
        
    	StringBuffer sb = new StringBuffer();    	
    	String[] ary = StrUtil.split(formCodes, ",");
    	for (int j=0; j<ary.length; j++) {
    		String formCode = ary[j];
        	sb.append("[formCode]" + formCode + "[/formCode]\r\n");

        	Map fieldsExcluded = new HashMap();
        	fieldsExcluded.put("id", "");
    	   	
        	sb.append("[formTables]\r\n");
        	
    	   	// 导出表单相关的表：
    	   	String sql = "select * from form where code=" + StrUtil.sqlstr(formCode);
    	   	sb.append(getResultStr("form", sql, fieldsExcluded));      	
    	   	sql = "select * from form_field where formCode=" + StrUtil.sqlstr(formCode);
    	   	sb.append(getResultStr("form_field", sql, fieldsExcluded));    	
    	   	sql = "select * from form_view where form_code=" + StrUtil.sqlstr(formCode);
    	   	sb.append(getResultStr("form_view", sql, fieldsExcluded));            	
        	
    	   	// 导出流程类型
    	   	sql = "select * from flow_directory where formCode=" + StrUtil.sqlstr(formCode);
    	   	sb.append(getResultStr("flow_directory", sql, fieldsExcluded));        	
    	   	
    	   	// 导出流程相关的表
    	   	sql = "select p.* from flow_predefined p, flow_directory f where f.formCode=" + StrUtil.sqlstr(formCode) + " and p.typeCode=f.code";
    	   	sb.append(getResultStr("flow_predefined", sql, fieldsExcluded));    	
        	
        	// 导出提醒，不含id
        	sql = "select * from form_remind where table_name=" + StrUtil.sqlstr(formCode);
        	sb.append(getResultStr("form_remind", sql, fieldsExcluded));
        	
        	sb.append("[/formTables]\r\n");
        	sb.append("[modules]\r\n");
        	
        	ModuleSetupDb msd = new ModuleSetupDb();
        	sql = "select code from " + msd.getTable().getName() + " where form_code=" + StrUtil.sqlstr(formCode);
        	Iterator ir = msd.list(sql).iterator();
        	while (ir.hasNext()) {
        		msd = (ModuleSetupDb)ir.next();
        		
        		sb.append("[module]\r\n");
        		
        		String moduleCode = msd.getString("code");
            	msd = msd.getModuleSetupDb(moduleCode);
            	
            	sb.append("[moduleCode]" + moduleCode + "[/moduleCode]\r\n");
            	sb.append("[moduleKind]" + msd.getInt("kind") + "[/moduleKind]\r\n");
            	
            	// 导出模块
            	sql = "select * from visual_module_setup where code=" + StrUtil.sqlstr(moduleCode);
            	sb.append(getResultStr("visual_module_setup", sql, null));

            	// 导出权限
            	// visual_module_priv
            	sql = "select * from visual_module_priv where form_code=" + StrUtil.sqlstr(moduleCode);
            	sb.append(getResultStr("visual_module_priv", sql, fieldsExcluded));

            	// 导出查询
            	String[] subTagsTop = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_name")), "\\|");
            	// String[] subTagUrlsTop = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_url")), "\\|");
            	int subLenTop = 0;
            	if (subTagsTop!=null) {
					subLenTop = subTagsTop.length;
				}
            	for (int i=0; i<subLenTop; i++) {
                	String tagUrl = getModuleSubTagUrl(moduleCode, subTagsTop[i]);
            		
                	if (tagUrl.startsWith("{")) {
                		try {
            				JSONObject json = new JSONObject(tagUrl);
            				int queryId = -1;
            				int reportId = -1;
            				try {
            					String qId = json.getString("queryId");
            					queryId = StrUtil.toInt(qId, -1);
            				}
            				catch (JSONException e) {
            				}

            				if (queryId!=-1) {
            			    	sql = "select * from form_query where id=" + queryId;
            			    	sb.append(getResultStr("form_query", sql, fieldsExcluded));
            			    	
            			    	// 记录选项卡中的查询的对应关系
            			    	sb.append("[module_tag_query]" + moduleCode + ":" + subTagsTop[i] + "[/module_tag_query]\r\n");
            					
            			    	sql = "select * from form_query_condition where query_id=" + queryId;
            			    	sb.append(getResultStr("form_query_condition", sql, fieldsExcluded));    					
            				}
            			} catch (JSONException e) {
            				// TODO Auto-generated catch block
            				LogUtil.getLog(ModuleUtil.class).error(e);
            			}
                	}        	
            	}

            	// 导出查询权限，因为查询条件关联的是查询的ID，所以暂无法导出

            	// 导出关联关系
            	sql = "select * from visual_module_relate where code=" + StrUtil.sqlstr(moduleCode);
            	sb.append(getResultStr("visual_module_relate", sql, null));    	

            	// 根据关联关系导出从表单

            	// 导出从表单的字段

            	// 根据从表单导出从模块

            	// 导出子模块的权限
            	
        		sb.append("[/module]\r\n");            	
        	}
        	
        	sb.append("[/modules]\r\n");

			String scriptStr = FileUtil.ReadFile(Global.getRealPath() + "flow/form_js/form_js_" + formCode + ".jsp", "utf-8");
			sb.append("[form_js]\r\n");
			sb.append(scriptStr);
			sb.append("[/form_js]\r\n");
			
			// 导出基础数据
			sb.append("[basic_select_ctls]\r\n");
			FormDb fd = new FormDb();
			fd = fd.getFormDb(formCode);
			for (FormField ff : fd.getFields()) {
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					IFormMacroCtl ictl = mu.getIFormMacroCtl();
					if (ictl instanceof IBasicSelectCtl) {
						String ctlCode = ((IBasicSelectCtl) ictl).getCode(ff);

						sb.append("[basic_ctl]\r\n");
						sb.append("[code]" + ctlCode + "[/code]\r\n");

						sql = "select * from oa_select where code=" + StrUtil.sqlstr(ctlCode);
						sb.append(getResultStr("oa_select", sql, null));

						// 下拉菜单型的基础数据
						sql = "select * from oa_select_option where code=" + StrUtil.sqlstr(ctlCode);
						sb.append(getResultStr("oa_select_option", sql, null));
						// 树型的基础数据
						// ......

						sb.append("[/basic_ctl]\r\n");
					}
				}
			}
			sb.append("[/basic_select_ctls]\r\n");			
    	}
    	return sb.toString();
    }    
    
    /**
     * 导入解决方案，同时包含流程及表单 fgf 2016
     * @param application
     * @param request
     * @throws ErrMsgException
     */
    public static void importSolution(ServletContext application, HttpServletRequest request) throws ErrMsgException {
		// 判断许可证是可以导入解决方案
		License license = com.redmoon.oa.kernel.License.getInstance();
		if (!license.canExportSolution()) {
			throw new ErrMsgException(license.getLicenseStr());
		}

       	try {
			// String str = FileUtil.ReadFile("d:/export.txt");
	        String[] extnames = {"txt"};
	        FileUpload fu = new FileUpload();
	        fu.setValidExtname(extnames); // 设置可上传的文件类型
	    	
	        fu.setMaxFileSize(Global.FileSize);
	        int ret = 0;
	        try {
	        	// fu.setDebug(true);
	            ret = fu.doUpload(application, request);
	            if (ret!=FileUpload.RET_SUCCESS) {
					throw new ErrMsgException(fu.getErrMessage());
				}
	        }
	        catch (IOException e) {
	            LogUtil.getLog(FormUtil.class).error("doUpload:" + e.getMessage());
	            throw new ErrMsgException(e.getMessage());
	        }	
	        
	        Vector v = fu.getFiles();
	        // 置保存路径
	        // String filepath = Global.getRealPath() + "upfile/";
	        if (v.size() > 0) {
	            FileInfo fi = null;
                Iterator ir = v.iterator();
                if (ir.hasNext()) {
                    fi = (FileInfo) ir.next();
                    String content = FileUtil.ReadFile(fi.getTmpFilePath());
        			parseSolution(content);
                }
	        }	        
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(ModuleUtil.class).error(e);
		}
    }   
    
    /**
     * 仅导入模块，不包括流程及表单
     * @param application
     * @param request
     * @throws ErrMsgException
     */
    public static void importModule(ServletContext application, HttpServletRequest request) throws ErrMsgException {
    	try {
			// String str = FileUtil.ReadFile("d:/export.txt");
			
	        String[] extnames = {"txt"};
	        FileUpload fu = new FileUpload();
	        fu.setValidExtname(extnames); // 设置可上传的文件类型
	    	
	        fu.setMaxFileSize(Global.FileSize);
	        int ret = 0;
	        try {
	        	// fu.setDebug(true);
	            ret = fu.doUpload(application, request);
	            if (ret!=FileUpload.RET_SUCCESS)
	                throw new ErrMsgException(fu.getErrMessage());
	        }
	        catch (IOException e) {
	            LogUtil.getLog(FormUtil.class).error("doUpload:" + e.getMessage());
	            throw new ErrMsgException(e.getMessage());
	        }	
	        
	        Vector v = fu.getFiles();
	        // 置保存路径
	        // String filepath = Global.getRealPath() + "upfile/";
	        if (v.size() > 0) {
	            FileInfo fi = null;
                Iterator ir = v.iterator();
                if (ir.hasNext()) {
                    fi = (FileInfo) ir.next();
                    
                    String content = FileUtil.ReadFile(fi.getTmpFilePath());
                    String formCode = fu.getFieldValue("formCode");
        			parse(formCode, content);
                }
	        }
		} catch (FileNotFoundException e) {
			LogUtil.getLog(ModuleUtil.class).error(e);
		}
    }
    
	public static void main(String args[]) {
/*		try {
			importModule();
		} catch (ErrMsgException e) {
			LogUtil.getLog(ModuleUtil.class).error(e);
		}*/
	}    
	
	/**
	 * 解析解决方案
	 * @param content
	 * @throws ErrMsgException
	 */
	public static void parseSolution(String content) throws ErrMsgException {
    	int fb = content.indexOf("[formCode]");
    	
    	if (fb==-1) {
    		throw new ErrMsgException("格式非法！");
    	}
    	int fe = content.indexOf("[/formCode]\r\n", fb);
    	if (fe==-1) {
    		throw new ErrMsgException("格式非法！");
    	}
    	
    	FormDb fd = new FormDb();
    	while (fb!=-1) {
        	String formCode = content.substring(fb + "[formCode]".length(), fe);
        	fd = fd.getFormDb(formCode);
        	// 导入时删除原来的相同编码的表单
        	if (fd.isLoaded()) {
				fd.del();
			}
    		
    		int formBlockEnd = content.indexOf("[formCode]", fe);
    		if (formBlockEnd==-1) {
    			formBlockEnd = content.length();
    		}
	    	String cont = content.substring(fb, formBlockEnd);
	    	parseSingleForm(formCode, cont);
	    	
	    	fb = content.indexOf("[formCode]", fe);
	    	if (fb!=-1) {
		    	fe = content.indexOf("[/formCode]", fb);	
		    	if (fe==-1) {
		    		throw new ErrMsgException("格式非法！");
		    	}	    	
	    	}
    	}
	}
	
	/**
	 * 解析表
	 * @param content
	 * @param vFlowTypeCode 如果不为空，则解析的是[formTables]...[/formTable]
	 * @param moduleCode 如果不为为空，则解析的是[modules]...[/modules]
	 * @throws ErrMsgException
	 */
	public static void parseTables(String content, Vector vFlowTypeCode, String moduleCode) throws ErrMsgException {		
    	String tagTableBegin = "[table]\r\n";
    	String tagTableEnd = "[/table]\r\n";
    	
    	String tagNameBegin = "[name]";
    	String tagNameEnd = "[/name]\r\n";
    	
    	String tagColsBegin = "[cols]";
    	String tagColsEnd = "[/cols]\r\n";
    	
    	String tagRecordsBegin = "[records]\r\n";
    	String tagRecordsEnd = "[/records]\r\n";
    	
    	String tagRecordBegin = "[record]";
    	String tagRecordEnd = "[/record]\r\n";
    	    	
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		try {
			jt.beginTrans();
			
	    	int b = content.indexOf(tagTableBegin);
	    	while (b!=-1) {
	    		int e = content.indexOf(tagTableEnd, b);
	    		if (e==-1) {
	    			throw new ErrMsgException("格式非法，缺少[/table]");
	    		}
	    		
	    		String tableCont = content.substring(b + tagTableBegin.length(), e);
	    		// name
	    		int nameB = tableCont.indexOf(tagNameBegin);
	    		int nameE = tableCont.indexOf(tagNameEnd);
	    		
	    		String tableName = tableCont.substring(nameB + tagNameBegin.length(), nameE);
	    		
	    		boolean isFlowDir = false;
	    		if ("flow_directory".equalsIgnoreCase(tableName)) {
	    			// 记下节点的编码
	    			isFlowDir = true;
	    		}
				    		
				String sql = "select * from " + tableName;
				// LogUtil.getLog(ModuleUtil.class).info("parseTables sql=" + sql);
				ResultIterator ri;
				Map mapType;
				try {
					ri = jt.executeQuery(sql, 1, 1);
					mapType = ri.getMapType();
				} catch (SQLException e1) {
		    		b = content.indexOf(tagTableBegin, e);
					// TODO Auto-generated catch block
					e1.printStackTrace();
					continue;
				}

				LogUtil.getLog(ModuleUtil.class).info("tableName=" + tableName);

				int colsB = tableCont.indexOf(tagColsBegin, nameE);
	    		int colsE = tableCont.indexOf(tagColsEnd, colsB);
	    		
	    		String cols = tableCont.substring(colsB + tagColsBegin.length(), colsE);
	    		
	    		if (tableName.equalsIgnoreCase("flow_predefined")) {
	    			cols += "|ID";
	    		}
	    		else if (tableName.equalsIgnoreCase("visual_module_priv")) {
	    			cols += "|ID";
				}
	    		String[] colAry = StrUtil.split(cols, "\\|");
	    		// String formCode = FormDb.getCodeByTableName(tableName);
	    		
				LogUtil.getLog(ModuleUtil.class).info("cols=" + cols);

	    		int flowTypeCodeIndex = -1;
	    		String colFields = "";
	    		String colWh = "";
	    		for (int i=0; i<colAry.length; i++) {
	    			if ("".equals(colFields)) {
	    				colFields = colAry[i];
	    				colWh = "?";
	    			}
	    			else {
	    				colFields += "," + colAry[i];    	
	    				colWh += ",?";
	    			}
	    			
	    			if (isFlowDir) {
	    				if ("code".equalsIgnoreCase(colAry[i])) {
	    					flowTypeCodeIndex = i;
	    				}
	    			}
	    		}
	    		
	    		String insertSql = "insert into " + tableName + "(" + colFields + ") values (" + colWh + ")";    		
	    		
	    		int recordsB = tableCont.indexOf(tagRecordsBegin, nameE);
	    		int recordsE = tableCont.indexOf(tagRecordsEnd, recordsB);
	    		
	    		String records = tableCont.substring(recordsB + tagRecordsBegin.length(), recordsE);
	    		
	    		Object[] objs = new Object[colAry.length];
	    		int rb = records.indexOf(tagRecordBegin);
	    		while (rb!=-1) {
	    			int re = records.indexOf(tagRecordEnd, rb);
	    			
	    			String recordStr = records.substring(rb + tagRecordBegin.length(), re);
	    			// 为visual_module_priv在末尾增加ID，以便于下一步自动生成ID
					if (tableName.equalsIgnoreCase("visual_module_priv")) {
						recordStr += "-|-0";
					}
					
	    			rb = records.indexOf(tagRecordBegin, re);
	    			
	    			String[] ary = StrUtil.split(recordStr, "-\\|-");
	    			
	    			for (int i=0; ary!=null && i<ary.length; i++) {
	    				LogUtil.getLog(ModuleUtil.class).info("colAry[" + i + "]=" + colAry[i]);
		    			Integer iType = (Integer)mapType.get(colAry[i]);
		    			if (iType == null) {
							throw new ErrMsgException("字段 " + colAry[i] + " 在表 " + tableName + " 中不存在");
						}
		    			
		    			int fieldType = QueryScriptUtil.getFieldTypeOfDBType(iType.intValue());
		    				
		    			String val = ary[i];
		    			
		    			// 如果是处理flow_directory表，则取出code值，以便于为其生成父节点
		    			if (isFlowDir && flowTypeCodeIndex==i) {
		    				String oldVal = val;
		    				val = RandomSecquenceCreator.getId(20); // 重置为随机数，以免与原来的冲突
		    				vFlowTypeCode.addElement(oldVal + "," + val);
		    				// 不删除现有的流程，以免丢失数据
/*		    				Leaf lf = new Leaf();
		    				lf = lf.getLeaf(val);
		    				if (lf!=null && lf.isLoaded()) {
		    					lf.del(lf);
		    				}*/
		    			}
		    			
		    			if (tableName.equalsIgnoreCase("oa_select_option")) {
		    				if (colAry[i].equalsIgnoreCase("id")) {
				    	        int id = (int)SequenceManager.nextID(SequenceManager.OA_SELECT_OPTION);
				    	        val = String.valueOf(id);		    					
		    				}
			    		}
		    			else if (tableName.equalsIgnoreCase("visual_module_priv")) {
		    				if (colAry[i].equalsIgnoreCase("id")) {
								int id = (int)SequenceManager.nextID(SequenceManager.VISUAL_MODULE_PRIV);
								val = String.valueOf(id);
							}
						}
						else if (tableName.equalsIgnoreCase("flow_predefined")) {
							if (colAry[i].equalsIgnoreCase("id")) {
								int id = (int)SequenceManager.nextID(SequenceManager.OA_WORKFLOW_PREDEFINED);
								val = String.valueOf(id);
							}
						}
		    					    			
		    			if ("null".equals(val)) {
		    				val = null;
		    			}
		    			if (fieldType==FormField.FIELD_TYPE_DATE) {
		    				objs[i] = DateUtil.parse(val, "yyyy-MM-dd");
		    			}
		    			else if (fieldType==FormField.FIELD_TYPE_DATETIME) {
		    				objs[i] = DateUtil.parse(val, "yyyy-MM-dd HH:mm:ss");	    				
		    			}
		    			else if (fieldType==FormField.FIELD_TYPE_INT) {
		    				if (val!=null) { 
		    					objs[i] = new Integer(StrUtil.toInt(val));
		    				}
		    				else {
		    					objs[i] = null;
		    				}
		    			}   
		    			else if (fieldType==FormField.FIELD_TYPE_DOUBLE) {
		    				if (val!=null) { 
		    					objs[i] = new Double(StrUtil.toDouble(val));
		    				}
		    				else {
		    					objs[i] = null;
		    				}
		    			}    	    	
		    			else if (fieldType==FormField.FIELD_TYPE_FLOAT || fieldType==FormField.FIELD_TYPE_PRICE) {
		    				if (val!=null) { 
		    					objs[i] = new Float(StrUtil.toFloat(val));
		    				}
		    				else {
		    					objs[i] = null;
		    				}	    				
		    			}
		    			else {
		    				objs[i] = val;
		    			}
		    			
		    			LogUtil.getLog(ModuleUtil.class).info(colAry[i] + "=" + objs[i]);
	    			}
	    			
		    		if (tableName.equalsIgnoreCase("flow_predefined")) {
		    	        int fpid = (int)SequenceManager.nextID(SequenceManager.OA_WORKFLOW_PREDEFINED);
		    			objs[objs.length-1] = fpid;
		    		}	 

					LogUtil.getLog(ModuleUtil.class).info(ModuleUtil.class + " sql=" + insertSql);
	    			jt.executeUpdate(insertSql, objs);
	    		}
	    		
	    		// 如果模块选项卡中涉及条件
	    		if (moduleCode!=null && "form_query".equalsIgnoreCase(tableName)) {
	    			long lastId = SQLFilter.getLastId(jt);
	    				
	    			int tagB = tableCont.indexOf("[module_tag_query]", recordsE);
	    			while (tagB>0) {
		    			int tagE = tableCont.indexOf("[/module_tag_query]", tagB);
		    			
		    			String str = tableCont.substring(tagB + "[module_tag_query]".length(), tagE);
		    			String[] ary = StrUtil.split(str, ":");
		    			// String moduleCode = ary[0];
		    			String tagName = ary[1];
		    			
		    			ModuleSetupDb msd = new ModuleSetupDb();
		    			msd = msd.getModuleSetupDb(moduleCode);
		    			
		    	    	String[] subTagsTop = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_name")), "\\|");
		    	    	// String[] subTagUrlsTop = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_url")), "\\|");
		    	    	int subLenTop = 0;
		    	    	if (subTagsTop!=null)
		    	    		subLenTop = subTagsTop.length;
		    	    	String sub_nav_tag_url = "";
		    	    	for (int i=0; i<subLenTop; i++) {
			    			String tagUrl = getModuleSubTagUrl(moduleCode, subTagsTop[i]);
		    	    		if (tagName.equals(subTagsTop[i])) {
		    	    			if (tagUrl.startsWith("{")) {
		   	        				try {
			   	        				JSONObject json = new JSONObject(tagUrl);
		   	        					json.put("queryId", String.valueOf(lastId));
			   	        				tagUrl = json.toString();
		   	        				}
		   	        				catch (JSONException ex) {
		   	        					LogUtil.getLog(ModuleUtil.class).error(ex);
		   	        				}
		    	    			}
		    	    		}
		    	    		
		    	    		if ("".equals(sub_nav_tag_url)) {
		    	    			sub_nav_tag_url = tagUrl;
		    	    		}
		    	    		else {
		    	    			sub_nav_tag_url += "|" + tagUrl;
		    	    		}
		    	    	}
		    	    	
		    	    	if (subLenTop>0) {
		    	    		msd.set("sub_nav_tag_url", sub_nav_tag_url);
		    	    		try {
								msd.save();
							} catch (ResKeyException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
		    	    	}
		    	    	
		    	    	tagB = tableCont.indexOf("[module_tag_query]", tagE);
	    			}
	    		}
	    		
	    		b = content.indexOf(tagTableBegin, e);
	    	}			
			
			jt.commit();

		} catch (SQLException e2) {
			jt.rollback();
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		finally {
			jt.close();
		}			
	}
	
	public static void parseSingleForm(String formCode, String content) throws ErrMsgException {
    	int fb = content.indexOf("[formTables]");
    	int fe = content.indexOf("[/formTables]\r\n", fb);
    	
    	String str = content.substring(fb + "[formTables]".length(), fe);
    	Vector vFlowTypeCode = new Vector();
    	parseTables(str, vFlowTypeCode, null);
		
        LogUtil.getLog(ModuleUtil.class).info("parseSingleForm: vFlowTypeCode.size()=" + vFlowTypeCode.size());

        if (vFlowTypeCode.size()!=0) {
			String parentCode = "";
			// 创建父节点
	        Leaf lf = new Leaf();
	        lf = lf.getLeafByName("解决方案");
	        if (lf!=null && lf.isLoaded()) {
	        	parentCode = lf.getCode();
	        }
	        else {
	        	lf = new Leaf();
		        lf.setName("解决方案");
		        parentCode = RandomSecquenceCreator.getId(20);
		        lf.setCode(parentCode);
		        lf.setParentCode(Leaf.CODE_ROOT);
		        lf.setDescription("解决方案");
		        lf.setType(Leaf.TYPE_NONE);
		        lf.setPluginCode("");
		        lf.setFormCode("");
		        lf.setDept("");
		        lf.setOpen(true);
		        lf.setUnitCode(DeptDb.ROOTCODE);
		        lf.setDebug(false);
		        lf.setMobileStart(false);
		        
		        lf.setMobileLocation(true);
		        lf.setMobileCamera(true);
		        
		        lf.setQueryId(0);
		        lf.setQueryRole("");
		        lf.setQueryCondMap("");
	
		        Leaf leafRoot = lf.getLeaf(Leaf.CODE_ROOT);
		        leafRoot.AddChild(lf);		
	        }
			
			Iterator ir = vFlowTypeCode.iterator();
			while (ir.hasNext()) {
				String val = (String)ir.next();
				
				String[] ary = StrUtil.split(val, ",");
				String oldFlowTypeCode = ary[0];
				String newFlowTypeCode = ary[1];

				lf = lf.getLeaf(newFlowTypeCode);
				lf.update(parentCode);
				
		        // 流程类型的编码code不能变，因为脚本中可能会有流程的code				
				// 在flow_predefined表中更新，将原来的typeCode改为新的newFlowTypeCode
				String sql = "update flow_predefined set typeCode=? where typeCode=?";
				JdbcTemplate jt = new JdbcTemplate();
				try {
					jt.executeUpdate(sql, new Object[]{newFlowTypeCode, oldFlowTypeCode});
				} catch (SQLException e) {
					LogUtil.getLog(ModuleUtil.class).error(e);
				}
			}
		}

		int modulesb = content.indexOf("[modules]");
		if (modulesb!=-1) {
			int modulese = content.indexOf("[/modules]");
			
			String modulesStr = content.substring(modulesb + "[modules]".length(), modulese);
			
			int b = modulesStr.indexOf("[module]");
			while (b!=-1) {
				int e = modulesStr.indexOf("[/module]", b);
				
				str = modulesStr.substring(b + "[module]".length(), e);
				
		    	int mb = str.indexOf("[moduleCode]");
		    	int me = str.indexOf("[/moduleCode]\r\n", mb);
		    		
		    	String moduleCode = null;
		    	if (mb!=-1) {
		    		moduleCode = str.substring(mb + "[moduleCode]".length(), me);
		    	}
		    				
				parseTables(str, null, moduleCode);
			
				b = modulesStr.indexOf("[module]", e);
			}
			
			// 因为直接插入了数据库，所以需要清缓存
			ModuleSetupDb msd = new ModuleSetupDb();
			msd.refreshCreate();
			
			ModuleRelateDb mrd = new ModuleRelateDb();
			mrd.refreshCreate();
		}
		
		int formJsB = content.indexOf("[form_js]");
		
		if (formJsB!=-1) {
			int formJsE = content.indexOf("[/form_js]");
			String scriptStr = content.substring(formJsB + "[form_js]".length(), formJsE);	
			FileUtil.WriteFile(Global.getRealPath() + "/flow/form_js/form_js_" + formCode + ".jsp", scriptStr, "utf-8");
		}
		
		int basicb = content.indexOf("[basic_select_ctls]");
		
		if (basicb!=-1) {
			int basice = content.indexOf("[/basic_select_ctls]");
			String ctlsStr = content.substring(basicb + "[basic_select_ctls]".length(), basice);
			int b = ctlsStr.indexOf("[basic_ctl]");
			while (b!=-1) {
				int e = ctlsStr.indexOf("[/basic_ctl]", b);
				
				str = ctlsStr.substring(b + "[basic_ctl]".length(), e);

		    	int mb = str.indexOf("[code]");
		    	int me = str.indexOf("[/code]\r\n", mb);
		    	
		    	String code = null;
		    	if (mb!=-1) {
		    		code = str.substring(mb + "[code]".length(), me);
		    	}
	
		    	LogUtil.getLog(ModuleUtil.class).info("basic_ctl code=" + code);
		    	// 判断宏控件是否已存在，如存在，则不再导入
		    	JdbcTemplate jt = new JdbcTemplate();
	            String sql = "select * from oa_select where code=?";
	            try {
	                ResultIterator ri = jt.executeQuery(sql, new Object[] {code});
	                if (ri.size()==0) {
	    				parseTables(str, null, null);	                	
	                }
	            }
	            catch (SQLException ex) {
	            	LogUtil.getLog(ModuleUtil.class).error(ex);
	            }
	            
				b = ctlsStr.indexOf("[basic_ctl]", e);
			}
		}		
		
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
        // 解析content，在表form_field中建立相应的域
		FormParser fp = new FormParser(fd.getContent());
		Vector v = fp.getFields();
		Conn conn = new Conn(Global.getDefaultDB());
		try {
            conn.beginTrans();

            String sql = "";
            Vector vt = fd.generateCreateStr(v);
            Iterator ir = vt.iterator();
            while (ir.hasNext()) {
                sql = (String)ir.next();
                LogUtil.getLog(ModuleUtil.class).info("create2: sql=" + sql);
                conn.executeUpdate(sql);
            }
            
            vt = SQLGeneratorFactory.getSQLGenerator().generateCreateStrForLog(FormDb.getTableName(formCode), v);
            ir = vt.iterator();
            while (ir.hasNext()) {
                sql = (String)ir.next();
                conn.executeUpdate(sql);
            }         		
            
            conn.commit();
        } catch (SQLException e) {
        	LogUtil.getLog(ModuleUtil.class).info("create:" + StrUtil.trace(e) +
                         ". Now transaction rollback");
            conn.rollback();
            throw new ErrMsgException("插入时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }		
	}
    
	/**
	 * 解析模块
	 * @param formCode
	 * @param content
	 * @throws ErrMsgException
	 */
    public static void parse(String formCode, String content) throws ErrMsgException {
    	int mb = content.indexOf("[moduleCode]");
    	int me = content.indexOf("[/moduleCode]\r\n", mb);
    	
    	String moduleCode = content.substring(mb + "[moduleCode]".length(), me);
    	
    	int kb = content.indexOf("[moduleKind]");
    	int ke = content.indexOf("[/moduleKind]\r\n");
    	
    	int fb = content.indexOf("[formCode]");
    	int fe = content.indexOf("[/formCode]\r\n", fb);
    	
    	String thisFormCode = content.substring(fb + "[formCode]".length(), fe);
    	
    	if (!formCode.equals(thisFormCode)) {
    		throw new ErrMsgException("待导入模块的表单编码与当前模块的表单编码不一致！");
    	}
    	
    	String moduleKind = content.substring(kb + "[moduleKind]".length(), ke);
    	int kind = StrUtil.toInt(moduleKind, ModuleSetupDb.KIND_MAIN);
    	
    	if (kind==ModuleSetupDb.KIND_MAIN) {
    		try {
        		JdbcTemplate jt = new JdbcTemplate();

        		// 删除原来的主模块
        		String sql = "delete from visual_module_setup where code=" + StrUtil.sqlstr(formCode);
				jt.executeUpdate(sql);
				
				// 删除原来的关联关系
				sql = "delete from visual_module_relate where code=" + StrUtil.sqlstr(moduleCode);
				jt.executeUpdate(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(ModuleUtil.class).error(e);
			}
    	}
    	
    	String tagTableBegin = "[table]\r\n";
    	String tagTableEnd = "[/table]\r\n";
    	
    	String tagNameBegin = "[name]";
    	String tagNameEnd = "[/name]\r\n";
    	
    	String tagColsBegin = "[cols]";
    	String tagColsEnd = "[/cols]\r\n";
    	
    	String tagRecordsBegin = "[records]\r\n";
    	String tagRecordsEnd = "[/records]\r\n";
    	
    	String tagRecordBegin = "[record]";
    	String tagRecordEnd = "[/record]\r\n";
    	
		JdbcTemplate jt = new JdbcTemplate();
		jt.setAutoClose(false);
		try {
			jt.beginTrans();
			
	    	int b = content.indexOf(tagTableBegin, ke);
	    	while (b!=-1) {
	    		int e = content.indexOf(tagTableEnd, b);
	    		if (e==-1) {
	    			throw new ErrMsgException("格式非法，缺少[/table]");
	    		}
	    		
	    		String tableCont = content.substring(b + tagTableBegin.length(), e);
	    		// name
	    		int nameB = tableCont.indexOf(tagNameBegin);
	    		int nameE = tableCont.indexOf(tagNameEnd);
	    		
	    		String tableName = tableCont.substring(nameB + tagNameBegin.length(), nameE);
				    		
				String sql = "select * from " + tableName;
				ResultIterator ri;
				Map mapType;
				try {
					ri = jt.executeQuery(sql, 1, 1);
					mapType = ri.getMapType();				
				} catch (SQLException e1) {
					
		    		b = content.indexOf(tagTableBegin, e);

					// TODO Auto-generated catch block
					e1.printStackTrace();
					continue;
				}

	    		LogUtil.getLog(ModuleUtil.class).info("tableName=" + tableName);
	    		
	    		int colsB = tableCont.indexOf(tagColsBegin, nameE);
	    		int colsE = tableCont.indexOf(tagColsEnd, colsB);
	    		
	    		String cols = tableCont.substring(colsB + tagColsBegin.length(), colsE);
	    		String[] colAry = StrUtil.split(cols, "\\|");
	    		// String formCode = FormDb.getCodeByTableName(tableName);
	    		
	    		LogUtil.getLog(ModuleUtil.class).info("cols=" + cols);

	    		String colFields = "";
	    		String colWh = "";
	    		for (int i=0; i<colAry.length; i++) {
	    			if ("".equals(colFields)) {
	    				colFields = colAry[i];
	    				colWh = "?";
	    			}
	    			else {
	    				colFields += "," + colAry[i];    	
	    				colWh += ",?";
	    			}
	    		}
	    		
	    		String insertSql = "insert into " + tableName + "(" + colFields + ") values (" + colWh + ")";    		
	    		
	    		int recordsB = tableCont.indexOf(tagRecordsBegin, nameE);
	    		int recordsE = tableCont.indexOf(tagRecordsEnd, recordsB);
	    		
	    		String records = tableCont.substring(recordsB + tagRecordsBegin.length(), recordsE);
	    		
				LogUtil.getLog(ModuleUtil.class).info(ModuleUtil.class + " sql=" + insertSql);

	    		Object[] objs = new Object[colAry.length];
	    		int rb = records.indexOf(tagRecordBegin);
	    		while (rb!=-1) {
	    			int re = records.indexOf(tagRecordEnd, rb);
	    			
	    			String recordStr = records.substring(rb + tagRecordBegin.length(), re);
	    			
	    			// LogUtil.getLog(ModuleUtil.class).info(recordStr);
	    			
	    			rb = records.indexOf(tagRecordBegin, re);
	    			
	    			String[] ary = StrUtil.split(recordStr, "-\\|-");
	    			
	    			for (int i=0; ary!=null && i<ary.length; i++) {
		    			Integer iType = (Integer)mapType.get(colAry[i]);	
		    			
		    			// LogUtil.getLog(ModuleUtil.class).info(ModuleUtil.class + " " + colAry[i]);
		    			
		    			int fieldType = QueryScriptUtil.getFieldTypeOfDBType(iType.intValue());
		    				
		    			String val = ary[i];
		    			if ("null".equals(val)) {
		    				val = null;
		    			}
		    			if (fieldType==FormField.FIELD_TYPE_DATE) {
		    				objs[i] = DateUtil.parse(val, "yyyy-MM-dd");
		    			}
		    			else if (fieldType==FormField.FIELD_TYPE_DATETIME) {
		    				objs[i] = DateUtil.parse(val, "yyyy-MM-dd HH:mm:ss");	    				
		    			}
		    			else if (fieldType==FormField.FIELD_TYPE_INT) {
		    				if (val!=null) { 
		    					objs[i] = new Integer(StrUtil.toInt(val));
		    				}
		    				else {
		    					objs[i] = null;
		    				}
		    			}   
		    			else if (fieldType==FormField.FIELD_TYPE_DOUBLE) {
		    				if (val!=null) { 
		    					objs[i] = new Double(StrUtil.toDouble(val));
		    				}
		    				else {
		    					objs[i] = null;
		    				}
		    			}    	    	
		    			else if (fieldType==FormField.FIELD_TYPE_FLOAT || fieldType==FormField.FIELD_TYPE_PRICE) {
		    				if (val!=null) { 
		    					objs[i] = new Float(StrUtil.toFloat(val));
		    				}
		    				else {
		    					objs[i] = null;
		    				}	    				
		    			}
		    			else {
		    				objs[i] = val;
		    			}
	    			}

	    			jt.executeUpdate(insertSql, objs);
	    		}
	    		
	    		// 如果模块选项卡中涉及条件
	    		if ("form_query".equalsIgnoreCase(tableName)) {
	    			long lastId = SQLFilter.getLastId(jt);
	    				
	    			int tagB = tableCont.indexOf("[module_tag_query]", recordsE);
	    			while (tagB>0) {
		    			int tagE = tableCont.indexOf("[/module_tag_query]", tagB);
		    			
		    			String str = tableCont.substring(tagB + "[module_tag_query]".length(), tagE);
		    			String[] ary = StrUtil.split(str, ":");
		    			// String moduleCode = ary[0];
		    			String tagName = ary[1];
		    			
		    			ModuleSetupDb msd = new ModuleSetupDb();
		    			msd = msd.getModuleSetupDb(moduleCode);
		    			
		    	    	String[] subTagsTop = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_name")), "\\|");
		    	    	// String[] subTagUrlsTop = StrUtil.split(StrUtil.getNullStr(msd.getString("sub_nav_tag_url")), "\\|");
		    	    	int subLenTop = 0;
		    	    	if (subTagsTop!=null) {
							subLenTop = subTagsTop.length;
						}
		    	    	String sub_nav_tag_url = "";
		    	    	for (int i=0; i<subLenTop; i++) {
			    			String tagUrl = getModuleSubTagUrl(moduleCode, subTagsTop[i]);
		    	    		if (tagName.equals(subTagsTop[i])) {
		    	    			if (tagUrl.startsWith("{")) {
		   	        				try {
			   	        				JSONObject json = new JSONObject(tagUrl);
		   	        					json.put("queryId", String.valueOf(lastId));
			   	        				tagUrl = json.toString();
		   	        				}
		   	        				catch (JSONException ex) {
		   	        					LogUtil.getLog(ModuleUtil.class).error(ex);
		   	        				}
		    	    			}
		    	    		}
		    	    		
		    	    		if ("".equals(sub_nav_tag_url)) {
		    	    			sub_nav_tag_url = tagUrl;
		    	    		}
		    	    		else {
		    	    			sub_nav_tag_url += "|" + tagUrl;
		    	    		}
		    	    	}
		    	    	
		    	    	if (subLenTop>0) {
		    	    		msd.set("sub_nav_tag_url", sub_nav_tag_url);
		    	    		try {
								msd.save();
							} catch (ResKeyException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
		    	    	}
		    	    	
		    	    	tagB = tableCont.indexOf("[module_tag_query]", tagE);
	    			}
	    		}
	    		
	    		b = content.indexOf(tagTableBegin, e);
	    	}			
			
			jt.commit();
			
			// 因为直接插入了数据库，所以需要清缓存
			ModuleSetupDb msd = new ModuleSetupDb();
			msd.refreshCreate();
			
			ModuleRelateDb mrd = new ModuleRelateDb();
			mrd.refreshCreate();
			
		} catch (SQLException e2) {
			jt.rollback();
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		finally {
			jt.close();
		}

    }

	/**
	 * 渲染脚本按钮中的脚本
	 * @param request
	 * @param script
     * @return
     */
    public static String renderScript(HttpServletRequest request, String script) {
    	Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(script);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String str = m.group(1);
            String val = "";
            if (str.startsWith("request.")) {
            	String key = str.substring("request.".length());
            	val = ParamUtil.get(request, key);
            }
            else if ("vPath".equalsIgnoreCase(str)) {
            	// val = Global.getRootPath();
				SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
				val = sysUtil.getRootPath();
            }
            m.appendReplacement(sb, val);
        }    	
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 操作列发起流程时，将模块表单中的字段值映射至流程表单中
     * @param request
     * @param fdaoSource
     * @param fdaoDest
     * @param maps
     * @return
     * @throws JSONException
     * @throws ErrMsgException
     */
    public static void doMapOnFlow(HttpServletRequest request, FormDAO fdaoSource, com.redmoon.oa.flow.FormDAO fdaoDest, String maps) throws ErrMsgException, SQLException {
        com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(maps);
		com.alibaba.fastjson.JSONObject jsonRaw = json;

		com.alibaba.fastjson.JSONArray ary = json.getJSONArray("maps");
        String sourceForm = json.getString("sourceForm");
        String destForm = json.getString("destForm");

        FormDb sourcefd = new FormDb();
        sourcefd = sourcefd.getFormDb(sourceForm);

        FormDb destFd = new FormDb();
        destFd = destFd.getFormDb(destForm);

        // 取出相应的值，置于json字符串中返回
        String ret = "";
        MacroCtlMgr mm = new MacroCtlMgr();

        for (int i=0; i<ary.size(); i++) {
            json = (com.alibaba.fastjson.JSONObject)ary.get(i);
            String destF = (String)json.get("destField");
            String sourceF = (String)json.get("sourceField");
            FormField ffDest = destFd.getFormField(destF);
            if (ffDest==null) {
                // LogUtil.getLog(ModuleUtil.class).info(ModuleUtil.class + " destF=" + destF + " 不存在！");
                LogUtil.getLog(ModuleUtil.class).error("destF=" + destF + " 不存在！");
                continue;
            }

            boolean isNest = false;
            String nestFormCode = ""; // 目标表单中嵌套表宏控件对应的表单编码
            MacroCtlUnit mu = null;
            if (ffDest.getType().equals(FormField.TYPE_MACRO)) {
                mu = mm.getMacroCtlUnit(ffDest.getMacroType());
                if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
                    nestFormCode = ffDest.getDefaultValue();
                    isNest = true;
                }
            }

            if (!isNest) {
            	if (FormDAO.FormDAO_NEW_ID.equals(sourceF)) {
					fdaoDest.setFieldValue(destF, String.valueOf(fdaoSource.getId()));
				}
            	else {
					fdaoDest.setFieldValue(destF, fdaoSource.getFieldValue(sourceF));
				}
            }
        }

        fdaoDest.save();

        ary = jsonRaw.getJSONArray("mapsNest");

        String retNest = "";

        if (ary!=null && ary.size() > 0) {
            String sourceFormNest = (String)jsonRaw.get("sourceFormNest");
            FormDb sourcefdNest = new FormDb();
            sourcefdNest = sourcefdNest.getFormDb(sourceFormNest);

            String destFormNest = (String)jsonRaw.get("destFormNest");
            FormDb destfdNest = new FormDb();
            destfdNest = destfdNest.getFormDb(destFormNest);

            // 取出源嵌套表中的数据
            String sql = "select id from " + FormDb.getTableName(sourceFormNest) + " where cws_id='" + fdaoSource.getId() + "' order by cws_order";

            com.redmoon.oa.visual.FormDAO sourcefdaoNest = new com.redmoon.oa.visual.FormDAO();
            Vector vt = sourcefdaoNest.list(sourceFormNest, sql);
            Iterator ir = vt.iterator();
            while (ir!=null && ir.hasNext()) {
                com.redmoon.oa.visual.FormDAO fdaoSourceNest = (com.redmoon.oa.visual.FormDAO)ir.next();

                com.redmoon.oa.visual.FormDAO fdaoDestNest = new com.redmoon.oa.visual.FormDAO(destfdNest);
                String nestjson = "";
                for (int i=0; i<ary.size(); i++) {
                    json = ary.getJSONObject(i);
                    String destF = (String)json.get("destField");
                    String sourceF = (String)json.get("sourceField");

                    String ffValue = fdaoSourceNest.getFieldValue(sourceF);
                    // 创建记录
                    fdaoDestNest.setFieldValue(destF, ffValue);
                }
                fdaoDestNest.setFlowTypeCode(fdaoDest.getFlowTypeCode());
                fdaoDestNest.setFlowId(fdaoDest.getFlowId());
                fdaoDestNest.setCwsId(String.valueOf(fdaoDest.getId()));
                fdaoDestNest.setCwsParentForm(fdaoDest.getFormDb().getCode());
                fdaoDestNest.setUnitCode(fdaoDest.getUnitCode());
                fdaoDestNest.create();
            }
        }
    }

	public static String renderLinkUrl(HttpServletRequest request, IFormDAO fdao, String url, String linkName, String moduleCode) {
    	return renderLinkUrl(request, fdao, url, linkName, moduleCode, 0, "");
	}

	/**
	 * 渲染操作列中的链接
	 * @param request
	 * @param fdao
	 * @param url
	 * @param flag 0表示操作列 1表示工具条按钮
	 * @return
	 */
    public static String renderLinkUrl(HttpServletRequest request, IFormDAO fdao, String url, String linkName, String moduleCode, int flag, String pageType) {
    	url = StrUtil.decodeJSON(url);
    	if (url.startsWith("{") && url.endsWith("}")) {
    		String urlStr = "";
    		JSONObject json;
			try {
				json = new JSONObject(url);
	    		String flowTypeCode = json.getString("flowTypeCode");
	    		String strParams = json.getString("params");

	    		if (flag == 0) {
	    			if (ParamUtil.isMobile(request)) {
						urlStr = "weixin/flow/flow_dispose.jsp?typeCode=" + flowTypeCode + "&op=opLinkFlow&moduleId=" + fdao.getId() + "&moduleCode=" + moduleCode + "&linkName=" + StrUtil.UrlEncode(linkName);
					}
	    			else {
						urlStr = "flow_initiate1_do.jsp?typeCode=" + flowTypeCode + "&op=opLinkFlow&moduleId=" + fdao.getId() + "&moduleCode=" + moduleCode + "&linkName=" + StrUtil.UrlEncode(linkName);
					}
				}
	    		else {
					if (ParamUtil.isMobile(request)) {
						urlStr = "weixin/flow/flow_dispose?typeCode=" + flowTypeCode + "&op=opBtnFlow&moduleId=" + fdao.getId() + "&moduleCode=" + moduleCode + "&btnId=" + StrUtil.UrlEncode(linkName) + "&pageType=" + pageType;
					}
					else {
						urlStr = "flow_initiate1_do.jsp?typeCode=" + flowTypeCode + "&op=opBtnFlow&moduleId=" + fdao.getId() + "&moduleCode=" + moduleCode + "&btnId=" + StrUtil.UrlEncode(linkName) + "&pageType=" + pageType;
					}
				}

	    		if (!"".equals(strParams)) {
					JSONObject params = new JSONObject(strParams);
					JSONArray maps = params.getJSONArray("maps");
					for (int i = 0; i < maps.length(); i++) {
						JSONObject jsobj = maps.getJSONObject(i);
						String sourceField = jsobj.getString("sourceField");
						String sourceFieldVal = "";
						if (sourceField.equals(FormDAO.FormDAO_NEW_ID)) {
							sourceFieldVal = String.valueOf(fdao.getId());
						} else {
							sourceFieldVal = fdao.getFieldValue(sourceField);
						}
						String destField = jsobj.getString("destField");
						urlStr += "&" + destField + "=" + StrUtil.UrlEncode(sourceFieldVal);
					}
				}
			} catch (JSONException e) {
				LogUtil.getLog(ModuleUtil.class).error(e);
			}

    		return urlStr;
    	}

    	return parseUrl(request, url, fdao);
    }

	public static com.alibaba.fastjson.JSONObject renderLinkUrlToJson(HttpServletRequest request, IFormDAO fdao, String url, String linkName, String moduleCode) {
		return renderLinkUrlToJson(request, fdao, url, linkName, moduleCode, 0, "");
	}

	/**
	 * 渲染操作列中的链接
	 * @param request
	 * @param fdao
	 * @param url
	 * @param flag 0表示操作列 1表示工具条按钮
	 * @return
	 */
	public static com.alibaba.fastjson.JSONObject renderLinkUrlToJson(HttpServletRequest request, IFormDAO fdao, String url, String linkName, String moduleCode, int flag, String pageType) {
		com.alibaba.fastjson.JSONObject resultJson = new com.alibaba.fastjson.JSONObject();
		url = StrUtil.decodeJSON(url);
		if (url.startsWith("{") && url.endsWith("}")) {
			// String urlStr = "";
			com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(url);
			if (json.containsKey("flowTypeCode")) {
				resultJson.put("type", "FLOW");
				resultJson.put("flowTypeCode", json.getString("flowTypeCode"));

				resultJson.put("moduleId", fdao.getId());
				resultJson.put("moduleCode", moduleCode);
				resultJson.put("btnId", linkName);
				resultJson.put("pageType", pageType);

				/*
				// 已改在WorkflowController.java的init方法中通过moduleCode、pageType获取映射字段
				com.alibaba.fastjson.JSONObject paramsJson = new com.alibaba.fastjson.JSONObject();
				resultJson.put("params", paramsJson);

				String strParams = json.getString("params");
				if (!"".equals(strParams)) {
					com.alibaba.fastjson.JSONObject params = com.alibaba.fastjson.JSONObject.parseObject(strParams);
					com.alibaba.fastjson.JSONArray maps = params.getJSONArray("maps");
					for (int i = 0; i < maps.size(); i++) {
						com.alibaba.fastjson.JSONObject jsobj = maps.getJSONObject(i);
						String sourceField = jsobj.getString("sourceField");
						*//*String sourceFieldVal = "";
						if (sourceField.equals(FormDAO.FormDAO_NEW_ID)) {
							sourceFieldVal = String.valueOf(fdao.getId());
						} else {
							sourceFieldVal = fdao.getFieldValue(sourceField);
						}*//*
						String destField = jsobj.getString("destField");
						paramsJson.put(destField, sourceField);
					}
				}*/
			} else {
				resultJson.put("type", "MODULE");
				resultJson.put("moduleCode", json.getString("moduleCode"));
				resultJson.put("moduleId", fdao.getId());
				resultJson.put("btnId", linkName);
				resultJson.put("pageType", pageType);
			}

			return resultJson;
		}

		resultJson.put("type", "LINK");
		resultJson.put("link", parseUrl(request, url, fdao));
		return resultJson;
	}

	/**
	 * 从request取得过滤条件中的参数值，组装为url字符串，用于操作列的链接
	 * @param request
	 * @param url
	 * @param fdao
	 * @return
	 */
    public static String parseUrl(HttpServletRequest request, String url, IFormDAO fdao) {
		Privilege pvg = new Privilege();
		// 前为utf8中文范围，后为gb2312中文范围
		Pattern p = Pattern.compile(
				"\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(url);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String str = m.group(1);
			String val = "";
			if (str.startsWith("request.")) {
				String key = str.substring("request.".length());
				val = ParamUtil.get(request, key);
			}
			/*else if ("vPath".equalsIgnoreCase(str)) {
				// 操作列链接已改为路由，故取消vPath
				val = Global.getRootPath();
			}*/
			else {
				// 脚本型条件时，取主表单中的字段值
				String fieldName = str;

				if ("curUser".equalsIgnoreCase(fieldName)) {
					val = pvg.getUser(request);
				}
				else if ("curDate".equalsIgnoreCase(fieldName)) {
					val = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
				}
				else if ("curUserDept".equalsIgnoreCase(fieldName)) { // 当前用户所在的部门
					DeptUserDb dud = new DeptUserDb();
					Vector v = dud.getDeptsOfUser(pvg.getUser(request));
					if (v.size()>0) {
						Iterator ir = v.iterator();
						while (ir.hasNext()) {
							DeptDb dd = (DeptDb)ir.next();
							if ("".equals(val)) {
								val = dd.getCode();
							}
							else {
								val += "," + dd.getCode();
							}
						}
					}
				}
				else if ("curUserRole".equalsIgnoreCase(fieldName)) {
					UserDb ud = new UserDb();
					ud = ud.getUserDb(pvg.getUser(request));
					RoleDb[] ary = ud.getRoles();
					if (ary!=null && ary.length>0) {
						for (int i=0; i<ary.length; i++) {
							if ("".equals(val)) {
								val = ary[i].getCode();
							}
							else {
								val += "," + ary[i].getCode();
							}
						}
					}
				}
				else if ("mainId".equalsIgnoreCase(fieldName)) {
					val = ParamUtil.get(request, "mainId");
				}
				else if ("id".equalsIgnoreCase(fieldName)) {
					val = String.valueOf(fdao.getId());
				}
				else if ("cws_id".equalsIgnoreCase(fieldName)) {
					val = fdao.getCwsId();
				}
				else if ("flowId".equalsIgnoreCase(fieldName)) {
					val = String.valueOf(fdao.getFlowId());
				}
				else {
					val = fdao.getFieldValue(fieldName);
				}
			}

			m.appendReplacement(sb, StrUtil.UrlEncode(val));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public static boolean isPrompt(HttpServletRequest request, ModuleSetupDb msd, IFormDAO fdao) {
		String promptCond = StrUtil.getNullStr(msd.getString("prompt_cond"));
		if (promptCond.startsWith("<items>")) {
			return isLinkShow(request, msd, fdao, promptCond, "", "");
		} else {
			return isPromptOld(request, msd, fdao);
		}
	}

	/**
	 * 8.0后改为组合条件
	 * @param request
	 * @param msd
	 * @param fdao
	 * @return
	 */
	@Deprecated
	public static boolean isPromptOld(HttpServletRequest request, ModuleSetupDb msd, IFormDAO fdao) {
    	String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
    	String promptValue = StrUtil.getNullStr(msd.getString("prompt_value"));
    	String promptCond = StrUtil.getNullStr(msd.getString("prompt_cond"));
    	
    	// 取得字段类型
    	FormDb fd = new FormDb();
    	fd = fd.getFormDb(msd.getString("form_code"));
    	FormField ff = fd.getFormField(promptField);
    	if (ff==null) {
    		LogUtil.getLog(ModuleUtil.class).error("字段：" + promptField + " 不存在！");
    		return false;
    	}
    	
		boolean re = false;

    	int fieldType = ff.getFieldType();
    	if (fieldType==FormField.FIELD_TYPE_INT || fieldType==FormField.FIELD_TYPE_FLOAT
    			 || fieldType==FormField.FIELD_TYPE_LONG || fieldType==FormField.FIELD_TYPE_PRICE || fieldType==FormField.FIELD_TYPE_DOUBLE) {
        	try {
        		double value = StrUtil.toDouble(fdao.getFieldValue(promptField), -1);
        		String v = CalculateFuncImpl.calculate(fdao, promptValue, 2, true);
				double val = StrUtil.toDouble(v, -1);

				switch (promptCond) {
					case "=":
						re = value == val;
						break;
					case ">=":
						re = value >= val;
						break;
					case ">":
						re = value > val;
						break;
					case "<":
						re = value < val;
						break;
					case "<=":
						re = value <= val;
						break;
				}
			} catch (ErrMsgException e) {
				LogUtil.getLog(ModuleUtil.class).error(e);
			}
    	}
    	else if (fieldType==FormField.FIELD_TYPE_DATE) {
			Date d = DateUtil.parse(fdao.getFieldValue(promptField), "yyyy-MM-dd");
			if (d==null) {
				DebugUtil.i(ModuleUtil.class, "isPrompt", promptField + " 值为null");
				return false;
			}
			long value = d.getTime();
			Date promptDate;
			if ("current".equals(promptValue.toLowerCase())) {
				promptDate = DateUtil.parse(DateUtil.format(new Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
			}
			else {
				promptDate = DateUtil.parse(promptValue, "yyyy-MM-dd");
				if (promptDate==null) {
					DebugUtil.e(ModuleUtil.class, "isPrompt", promptValue + " 格式非法");
					return false;
				}
			}
			long val = promptDate.getTime();

			switch (promptCond) {
				case "=":
					re = value == val;
					break;
				case ">=":
					re = value >= val;
					break;
				case ">":
					re = value > val;
					break;
				case "<":
					re = value < val;
					break;
				case "<=":
					re = value <= val;
					break;
			}
		}
		else if (fieldType==FormField.FIELD_TYPE_DATETIME) {
			Date d = DateUtil.parse(fdao.getFieldValue(promptField), "yyyy-MM-dd HH:mm:ss");
			if (d==null) {
				DebugUtil.i(ModuleUtil.class, "isPrompt", promptField + " 值为null");
				return false;
			}
			long value = d.getTime();
			Date promptDate;
			if ("current".equals(promptValue.toLowerCase())) {
				promptDate = new Date();
			}
			else {
				promptDate = DateUtil.parse(promptValue, "yyyy-MM-dd HH:mm:ss");
				if (promptDate==null) {
					DebugUtil.e(ModuleUtil.class, "isPrompt", promptValue + " 格式非法");
					return false;
				}
			}
			long val = promptDate.getTime();

			switch (promptCond) {
				case "=":
					re = value == val;
					break;
				case ">=":
					re = value >= val;
					break;
				case ">":
					re = value > val;
					break;
				case "<":
					re = value < val;
					break;
				case "<=":
					re = value <= val;
					break;
			}
		}
    	else {
    		String value = StrUtil.getNullStr(fdao.getFieldValue(promptField));
    		// 如果条件中的值与数据库中取到的值均为数值型，则按双精度型比较
    		if (NumberUtil.isNumeric(value) && NumberUtil.isNumeric(promptValue)) {
    			double condVal = StrUtil.toDouble(promptValue);
    			double val = StrUtil.toDouble(value);
    			if ("=".equals(promptCond)) {
    				re = condVal == val;
    			}
    			else {
    				re = condVal!=val;
    			}
    			return re;
    		}    		
    		String val = ConnStrFuncImpl.doConn(fdao, promptValue);
			if ("=".equals(promptCond)) {
				re = value.equals(val);
			}
			else {
				re = !value.equals(val);
			}
    	}
    	
    	return re;
    }
    
    /**
     * 是否显示链接
     * @param request
     * @param msd
     * @param fdao
     * @param fieldName
     * @param fieldCond
     * @param fieldValue
     * @return
     */
    public static boolean isLinkShow(HttpServletRequest request, ModuleSetupDb msd, IFormDAO fdao, String fieldName, String fieldCond, String fieldValue) {
		// 如果fileName为空，则为判断系统按钮（如详情页中的打印按钮，默认其按钮属性只有id，无判断条件）是否有可见权限
    	if ("".equals(fieldName)) {
			return true;
		}
    	// 如果是组合条件
		if (fieldName.startsWith("<items>")) {
			String cond = ModuleUtil.parseConds(request, fdao, fieldName);
			javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
			javax.script.ScriptEngine engine = manager.getEngineByName("javascript");
			try {
				Boolean ret = (Boolean)engine.eval(cond);
				return ret;
			}
			catch (javax.script.ScriptException ex) {
				LogUtil.getLog(ModuleUtil.class).error(ex);
			}
		}

		// 4.0及之前的条件判断
		Privilege pvg = new Privilege();

		// 前为utf8中文范围，后为gb2312中文范围
    	Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(fieldValue);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String str = m.group(1);
            String val = "";
            if (str.startsWith("request.")) {
            	String key = str.substring("request.".length());
            	val = ParamUtil.get(request, key);
            }
            else if ("vPath".equalsIgnoreCase(str)) {
            	val = Global.getRootPath();
            }
            else {
            	// 脚本型条件时，取主表单中的字段值
            	String fName = str;
            	
            	if ("curUser".equalsIgnoreCase(fName)) {
   	            	val = pvg.getUser(request);
                }     
                else if ("curDate".equalsIgnoreCase(fName)) {
                	val = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
                }
                else if ("curUserDept".equalsIgnoreCase(fName)) { // 当前用户所在的部门
                	DeptUserDb dud = new DeptUserDb();
                	Vector v = dud.getDeptsOfUser(pvg.getUser(request));
                	if (v.size()>0) {
                		Iterator ir = v.iterator();
                		while (ir.hasNext()) {
                			DeptDb dd = (DeptDb)ir.next();
                			if ("".equals(val)) {
                				val = dd.getCode();
                			}
                			else {
                				val += "," + dd.getCode();
                			}
                		}
                	}
                }
                else if ("curUserRole".equalsIgnoreCase(fName)) {
                	UserDb ud = new UserDb();
                	ud = ud.getUserDb(pvg.getUser(request));
                	RoleDb[] ary = ud.getRoles();
                	if (ary!=null && ary.length>0) {
                		for (int i=0; i<ary.length; i++) {
                			if ("".equals(val)) {
                				val = ary[i].getCode();
                			}
                			else {
                				val += "," + ary[i].getCode();            				
                			}
                		}
                	}
                }
                else if ("mainId".equalsIgnoreCase(fName)) {
                	val = ParamUtil.get(request, "mainId");
                }            	
                else if ("id".equalsIgnoreCase(fName)) {
					val = String.valueOf(fdao.getId());
				}
				else if ("cws_id".equalsIgnoreCase(fName)) {
					val = fdao.getCwsId();
				}
				else if ("cws_flag".equalsIgnoreCase(fName)) {
					val = String.valueOf(fdao.getCwsFlag());
				}
				else if ("flowId".equalsIgnoreCase(fName)) {
					val = String.valueOf(fdao.getFlowId());
				}
				else {
					val = fdao.getFieldValue(fName);
				}
            }
            
            m.appendReplacement(sb, val);
        }    	
        m.appendTail(sb);
        
        fieldValue = sb.toString();
    	
    	// 取得字段类型
        int fieldType;
        String fieldVal;
        if ("cws_flag".equals(fieldName)) {
        	fieldType = FormField.FIELD_TYPE_INT;
        	fieldVal = String.valueOf(fdao.getCwsFlag());
        }
        else {
	    	FormDb fd = new FormDb();
	    	fd = fd.getFormDb(msd.getString("form_code"));
	    	FormField ff = fd.getFormField(fieldName);
	    	if (ff==null) {
	    		LogUtil.getLog(ModuleUtil.class).error("字段：" + fieldName + " 不存在！");
	    		return false;
	    	}
	    	fieldType = ff.getFieldType();	 
	    	fieldVal = fdao.getFieldValue(fieldName);
        }
    	
    	// LogUtil.getLog(ModuleUtil.class).info("ModuleUtil fieldValue=" + fieldValue);
		boolean re = false;
    	
    	if (fieldType==FormField.FIELD_TYPE_INT || fieldType==FormField.FIELD_TYPE_FLOAT
    			 || fieldType==FormField.FIELD_TYPE_LONG || fieldType==FormField.FIELD_TYPE_PRICE || fieldType==FormField.FIELD_TYPE_DOUBLE) {
        	try {
        		double value = StrUtil.toDouble(fieldVal, -1);
        		String v = CalculateFuncImpl.calculate(fdao, fieldValue, 2, true);
				double val = StrUtil.toDouble(v, -1);

				switch (fieldCond) {
					case "=":
						re = value == val;
						break;
					case ">=":
						re = value >= val;
						break;
					case ">":
						re = value > val;
						break;
					case "<":
						re = value < val;
						break;
					case "<=":
						re = value <= val;
						break;
					case "<>":
						re = value != val;
						break;
				}
			} catch (ErrMsgException e) {
				LogUtil.getLog(ModuleUtil.class).error(e);
			}
    	}
    	else {
    		String value = StrUtil.getNullStr(fieldVal);
    		// 如果条件中的值与数据库中取到的值均为数值型，则按双精度型比较
    		if (NumberUtil.isNumeric(value) && NumberUtil.isNumeric(fieldValue)) {
    			double condVal = StrUtil.toDouble(fieldValue);
    			double val = StrUtil.toDouble(value);
    			if ("=".equals(fieldCond)) {
    				re = condVal == val;
    			}
    			else {
    				re = condVal!=val;
    			}
    			return re;
    		}
    		
    		String val = ConnStrFuncImpl.doConn(fdao, fieldValue);
			if ("=".equals(fieldCond)) {
				re = value.equals(val);
			}
			else {
				re = !value.equals(val);
			}
    	}
    	
    	return re;
    }

	/**
	 * 取得验证规则生成的脚本
	 * @param request HttpServletRequest
	 * @param fdao FormDAO
	 * @param userName String
	 * @return
	 */
	public static boolean doCheckSetup(HttpServletRequest request, String userName, IFormDAO fdao, FileUpload fu) throws ErrMsgException {
		FormDb fd = new FormDb();
		fd = fd.getFormDb(fdao.getFormCode());
		if (StrUtil.isEmpty(fd.getCheckSetup())) {
			return true;
		}
		// <config><rules><rule><if>[{"field":"rq", "operator":"&gt;=", "val":""}]</if><then>[{"field":"rq", "operator":"&lt;&gt;", "val":""}]</then></rule></rules></config>
		try {
			List<String> filedList = new ArrayList<>();
			Iterator<FormField> ir1 = fd.getFields().iterator();
			while (ir1.hasNext()) {
				FormField ff = ir1.next();
				filedList.add(ff.getName());
			}

			StringBuffer sb = new StringBuffer();
			IModuleUtil iModuleUtil = SpringUtil.getBean(IModuleUtil.class);

			SAXBuilder parser = new SAXBuilder();
			org.jdom.Document doc = parser.build(new InputSource(new StringReader(fd.getCheckSetup())));
			Element root = doc.getRootElement();
			Iterator ir2 = root.getChild("rules").getChildren().iterator();
			while (ir2.hasNext()) {
				Element el = (Element)ir2.next();

				String desc = el.getChildText("desc");
				String strIf = el.getChildText("if");
				JSONArray aryIf = new JSONArray(strIf);
				boolean re = iModuleUtil.evalCheckSetupRule(request, userName, aryIf, fdao, filedList, fu);
				if (re) {
					String then = el.getChildText("then");
					JSONArray aryThen = new JSONArray(then);
					re = iModuleUtil.evalCheckSetupRule(request, userName, aryThen, fdao, filedList, fu);

					if (!re) {
						StrUtil.concat(sb, "\r\n", desc);
					}
				}
			}
			if (sb.length()>0) {
				throw new ErrMsgException(sb.toString());
			}
		} catch (IOException | JDOMException | JSONException ex) {
			LogUtil.getLog(ModuleUtil.class).error(ex);
		}
		return true;
	}

	/**
	 * 导出成xml格的xls文件，速度较快
	 * @param request
	 * @param os
	 * @param fields
	 * @param fd
	 * @param sql
	 * @param templateId
	 * @throws JSONException
	 */
	public static void exportXml(HttpServletRequest request, OutputStream os, String[] fields, String[] fieldsTitle, FormDb fd, String sql, long templateId, boolean isExporBySelCol, List<String> nestFormList, String uid, ModuleSetupDb msd) {
		if (templateId!=-1) {
			ModuleExportTemplateDb metd = new ModuleExportTemplateDb();
			metd = metd.getModuleExportTemplateDb(templateId);

			String columns = metd.getString("cols");
			// 第一列的序号
			boolean isSerialNo = "1".equals(metd.getString("is_serial_no"));
			if (isSerialNo) {
				columns = columns.substring(1); // [{}, {},...]去掉[
				columns = "[{\"field\":\"serialNoForExp\",\"title\":\"序号\",\"link\":\"#\",\"width\":80,\"name\":\"serialNoForExp\"}," + columns;
			}

			com.alibaba.fastjson.JSONArray arr = com.alibaba.fastjson.JSONArray.parseArray(columns);
			StringBuffer colsSb = new StringBuffer();
			for (int i = 0; i < arr.size(); i++) {
				com.alibaba.fastjson.JSONObject json = arr.getJSONObject(i);
				StrUtil.concat(colsSb, ",", json.getString("field"));
			}
			fields = StrUtil.split(colsSb.toString(), ",");
		}

		long total = 0;
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		try {
			ri = jt.executeQuery(SQLFilter.getCountSql(sql));
		} catch (SQLException e) {
			LogUtil.getLog(ModuleUtil.class).error(e);
		}
		if (ri.hasNext()) {
			ResultRecord rr = (ResultRecord)ri.next();
			total = rr.getLong(1);
		}

		StringBuilder sb = new StringBuilder();
		try {
			OutputStreamWriter write = new OutputStreamWriter(os, StandardCharsets.UTF_8);
			// OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(new File("d://aa.xls")), "UTF-8");
			BufferedWriter output = new BufferedWriter(write);
			sb.append("<?xml version=\"1.0\"?>");
			sb.append("\n");
			sb.append("<?mso-application progid=\"Excel.Sheet\"?>");
			sb.append("\n");
			sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"");
			sb.append("\n");
			sb.append("  xmlns:o=\"urn:schemas-microsoft-com:office:office\"");
			sb.append("\n");
			sb.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"");
			sb.append("\n");
			sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"");
			sb.append("\n");
			sb.append(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">");
			sb.append("\n");
			sb.append(" <Styles>\n");
			sb.append("  <Style ss:ID=\"Default\" ss:Name=\"Normal\">\n");
			sb.append("   <Alignment ss:Vertical=\"Center\"/>\n");
			sb.append("   <Borders/>\n");
			sb.append("   <Font ss:FontName=\"宋体\" x:CharSet=\"134\" ss:Size=\"12\"/>\n");
			sb.append("   <Interior/>\n");
			sb.append("   <NumberFormat/>\n");
			sb.append("   <Protection/>\n");
			sb.append("  </Style>\n");
			sb.append(" </Styles>\n");

			sb.append("<Worksheet ss:Name=\"Sheet0\">");
			sb.append("\n");
			sb.append("<Table ss:ExpandedColumnCount=\"" + fields.length
					+ "\" ss:ExpandedRowCount=\"" + (total + 1)
					+ "\" x:FullColumns=\"1\" x:FullRows=\"1\">");
			sb.append("\n");

			sb.append("<Row>");
			int len = fields.length;
			for (int i = 0; i < len; i++) {
				String fieldName = fields[i];

				String fieldTitle = fieldsTitle[i];
				String title = "";

				// 如果是选择列导出，则在modulecontroller中传入fieldTitle时已经赋予了值
				if (!isExporBySelCol) {
					if (fieldName.equals("serialNoForExp")) {
						title = "序号";
					} else if (fieldName.startsWith("main:")) {
						String[] mainToSub = StrUtil.split(fieldName, ":");
						if (mainToSub != null && mainToSub.length == 3) {
							FormDb ntfd = new FormDb();
							ntfd = ntfd.getFormDb(mainToSub[1]);
							FormField ff = ntfd.getFormField(mainToSub[2]);
							if (ff != null) {
								title = ff.getTitle();
							} else {
								title = fieldName + "不存在";
							}
						} else {
							title = fieldName;
						}
					} else if (fieldName.startsWith("other:")) {
						String[] otherFields = StrUtil.split(fieldName, ":");
						if (otherFields.length == 5) {
							FormDb otherFormDb = new FormDb(otherFields[2]);
							title = otherFormDb.getFieldTitle(otherFields[4]);
						}
					} else if ("cws_creator".equals(fieldName)) {
						title = "创建者";
					} else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
						title = "ID";
					} else if ("cws_status".equals(fieldName)) {
						title = "状态";
					} else if ("cws_flag".equals(fieldName)) {
						title = "冲抵状态";
					} else if ("flowId".equalsIgnoreCase(fieldName)) {
						title = "流程号";
					} else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
						title = "流程开始时间";
					} else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
						title = "流程结束时间";
					} else if ("cws_id".equals(fieldName)) {
						title = "关联ID";
					} else {
						title = fd.getFieldTitle(fieldName);
					}
				}

				if (!"#".equals(fieldTitle)) {
					title = fieldTitle;
				}

				if (title == null) {
					LogUtil.getLog(ModuleUtil.class).error("字段: " + fieldName + " 不存在");
					title = "";
				}

				title = title.replaceAll("<", "&lt;").replaceAll(">", "&gt ;");
				sb.append("<Cell><Data ss:Type=\"String\">" + title + "</Data></Cell>");
				sb.append("\n");
			}

			sb.append("</Row>");
			sb.append("\n");

			output.write(sb.toString());
			output.flush();
			sb.setLength(0);

			int serialNo = 0;
			WorkflowDb wf = new WorkflowDb();
			MacroCtlMgr mm = new MacroCtlMgr();
			UserMgr um = new UserMgr();

			long tDebug = System.currentTimeMillis();
			request.setAttribute(ConstUtil.IS_FOR_EXPORT, "true");
			int n = 0;

			AuthUtil authUtil = SpringUtil.getBean(AuthUtil.class);
			SysProperties sysProperties = SpringUtil.getBean(SysProperties.class);
			ExportExcelCache exportExcelCache = SpringUtil.getBean(ExportExcelCache.class);

			int curPage = 1;
			int totalPages = 1;
			int pageSize = 200;
			com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
			// 为防止内存不足，每次处理记录数为pageSize
			for (curPage=1; curPage <= totalPages; curPage++) {
				try {
					ListResult lr = fdao.listResult(fd.getCode(), sql, curPage, pageSize);
					if (lr.getResult().size() == 0) {
						return;
					}
					if (totalPages == 1) {
						totalPages = (int)Math.ceil((double)lr.getTotal() / pageSize);
					}

					// 当异步导出Excel时
					if (sysProperties.isExportExcelAsync() && Global.getInstance().isUseCache()) {
						if (curPage == 1) {
							FormDb fdItem = new FormDb();
							fdItem = fdItem.getFormDb(ConstUtil.FORM_EXPORT_EXCEL);
							FormDAO daoItem = new FormDAO(fdItem);
							daoItem.setFieldValue("sql_text", sql);
							daoItem.setFieldValue("operator", authUtil.getUserName());
							daoItem.setFieldValue("rows", lr.getTotal());
							daoItem.setFieldValue("module_code", msd.getCode());
							daoItem.setFieldValue("ext", "xls");
							daoItem.setCreator(authUtil.getUserName()); // 参数为用户名（创建记录者）
							daoItem.setUnitCode(authUtil.getUserUnitCode()); // 置单位编码
							daoItem.create();

							ExportExcelItem exportExcelItem = new ExportExcelItem();
							exportExcelItem.setUid(uid);
							exportExcelItem.setRows((int)lr.getTotal());
							exportExcelItem.setCurRow(0);
							exportExcelItem.setId(daoItem.getId());
							exportExcelCache.put(uid, exportExcelItem);
						}
					}

					Iterator irFdao = lr.getResult().iterator();
					while (irFdao.hasNext()) {
						fdao = (com.redmoon.oa.visual.FormDAO) irFdao.next();

						long tDebugRow = System.currentTimeMillis();

						n++;
						sb.append("<Row>");

						int logType = StrUtil.toInt(fdao.getFieldValue("log_type"), FormDAOLog.LOG_TYPE_CREATE);

						// 置SQL宏控件中需要用到的fdao
						RequestUtil.setFormDAO(request, fdao);

						// 取出嵌套表的记录
						Map<String, List<com.redmoon.oa.visual.FormDAO>> mapNest = new HashMap<>();
						if (isExporBySelCol) {
							for (String nestFormCode : nestFormList) {
								mapNest.put(nestFormCode, fdao.listNest(nestFormCode));
							}
						}

						for (int i = 0; i < len; i++) {
							long tDebugCol = System.currentTimeMillis();

							String fieldName = fields[i];
							String fieldValue = "";
							if (fieldName.equals("serialNoForExp")) {
								fieldValue = String.valueOf(++serialNo);
							} else if (fieldName.startsWith("main:")) {
								String[] subFields = fieldName.split(":");
								if (subFields.length == 3) {
									// 此时关联的是主表单，应该只有一条记录
									FormDb subfd = new FormDb(subFields[1]);
									FormDAO subfdao = new FormDAO(subfd);
									FormField subff = subfd.getFormField(subFields[2]);
									String subsql = "select id from " + subfdao.getTableName() + " where id=" + fdao.getCwsId() + " order by cws_order";
									StringBuilder sbSub = new StringBuilder();
									try {
										ResultIterator riSub = jt.executeQuery(subsql);
										while (riSub.hasNext()) {
											ResultRecord rr = riSub.next();
											int subid = rr.getInt(1);
											subfdao = new FormDAO(subid, subfd);
											String subFieldValue = subfdao.getFieldValue(subFields[2]);
											if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
												MacroCtlUnit mu = mm.getMacroCtlUnit(subff.getMacroType());
												if (mu != null) {
													RequestUtil.setFormDAO(request, subfdao);
													subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
													// 恢复request中原来的fdao，以免ModuleController中setFormDAO的值被修改为本方法中的fdao
													RequestUtil.setFormDAO(request, fdao);
												}
											}
											sbSub.append(subFieldValue).append(ri.hasNext() ? "," : "");
										}
									} catch (Exception e) {
										LogUtil.getLog(ModuleUtil.class).error(e);
									}
									fieldValue += sbSub.toString();
								}
							} else if (fieldName.startsWith("other:")) {
								// 将module_id:xmxxgl_qx:id:xmmc替换为module_id:xmxxgl_qx_log:cws_log_id:xmmc
								String fName = fieldName;
								if (logType == FormDAOLog.LOG_TYPE_DEL) {
									if ("module_log".equals(fd.getCode())) {
										if (fName.contains("module_id:")) {
											int p = fName.indexOf(":");
											p = fName.indexOf(":", p + 1);
											String prefix = fName.substring(0, p);
											fName = fName.substring(p + 1);
											p = fName.indexOf(":");
											String endStr = fName.substring(p);
											if (endStr.startsWith(":id:")) {
												// 将id替换为***_log表中的cws_log_id
												endStr = ":cws_log_id" + endStr.substring(3);
											}
											fName = fName.substring(0, p);
											fName += "_log";
											fName = prefix + ":" + fName + endStr;
										}
									}
								}
								fieldValue = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fName);
							} else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
								fieldValue = String.valueOf(fdao.getId());
							} else if ("cws_flag".equals(fieldName)) {
								fieldValue = String.valueOf(fdao.getCwsFlag());
							} else if ("cws_creator".equals(fieldName)) {
								fieldValue = StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName());
							} else if ("cws_status".equals(fieldName)) {
								fieldValue = com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus());
							} else if ("flowId".equalsIgnoreCase(fieldName)) {
								fieldValue = String.valueOf(fdao.getFlowId());
							} else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
								int flowId = fdao.getFlowId();
								if (flowId != -1) {
									wf = wf.getWorkflowDb(flowId);
									fieldValue = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm:ss");
								}
							}
							else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
								int flowId = fdao.getFlowId();
								if (flowId != -1) {
									wf = wf.getWorkflowDb(flowId);
									fieldValue = DateUtil.format(wf.getEndDate(), "yyyy-MM-dd HH:mm:ss");
								}
							}
							else if ("cws_id".equals(fieldName)) {
								fieldValue = fdao.getCwsId();
							}
							else {
								boolean isNestCol = false;
								if (isExporBySelCol) {
									// 嵌套表
									if (fieldName.contains(":")) {
										isNestCol = true;
										String[] ary = fieldName.split(":");
										List<com.redmoon.oa.visual.FormDAO> nestList = mapNest.get(ary[0]);
										StringBuilder stringBuilder = new StringBuilder();
										for (com.redmoon.oa.visual.FormDAO nestDao : nestList) {
											FormField nestFf = nestDao.getFormField(ary[1]);
											if (nestFf.getType().equals(FormField.TYPE_MACRO)) {
												MacroCtlUnit mu = mm.getMacroCtlUnit(nestFf.getMacroType());
												if (mu == null) {
													DebugUtil.e(ModuleUtil.class, "exportXml", nestFf.getTitle() + " 嵌套表 宏控件: " + nestFf.getMacroType() + " 不存在");
												} else if (!"macro_raty".equals(mu.getCode())) {
													StrUtil.concat(stringBuilder, ",", StrUtil.getAbstract(request, mu.getIFormMacroCtl().converToHtml(request, nestFf, nestFf.getValue()), 1000, ""));
												}
											} else {
												StrUtil.concat(stringBuilder, ",", nestFf.convertToHtml());
											}
										}
										fieldValue = stringBuilder.toString();
									}
								}

								if (!isNestCol) {
									// FormField ff = fd.getFormField(fieldName);
									FormField ff = fdao.getFormField(fieldName);

									if (ff == null) {
										fieldValue = "不存在！";
									} else {
										if (ff.getType().equals(FormField.TYPE_MACRO)) {
											MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
											if (mu != null) {
												fieldValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
											}
										} else {
											fieldValue = FuncUtil.renderFieldValue(fdao, ff);
										}
									}
								}
							}

							if (Global.getInstance().isDebug()) {
								DebugUtil.i(ModuleUtil.class, "export", "提取列" + fieldName + " 耗时：" + (System.currentTimeMillis() - tDebugCol) + " ms");
							}

							// XML转义
							fieldValue = StrUtil.getNullStr(fieldValue);
							fieldValue = fieldValue.replaceAll("<", "&lt;");
							fieldValue = fieldValue.replaceAll(">", "&gt ;");
							sb.append("<Cell><Data ss:Type=\"String\">").append(fieldValue).append("</Data></Cell>");
							sb.append("\n");
						}
						sb.append("</Row>");
						sb.append("\n");
						//每三百行数据批量提交一次
						if (n % 300 == 0) {
							output.write(sb.toString());
							output.flush();
							sb.setLength(0);
						}

						ExportExcelItem exportExcelItem = exportExcelCache.get(uid);
						if (exportExcelItem == null) {
							LogUtil.getLog(ModuleUtil.class).error("uid: " + uid + " is not found，it is may be expired.");
						} else {
							exportExcelItem.setUid(uid);
							exportExcelItem.setCurRow(n);
							exportExcelCache.put(uid, exportExcelItem);
						}

						if (Global.getInstance().isDebug()) {
							DebugUtil.i(ModuleUtil.class, "export", "one record: " + (System.currentTimeMillis() - tDebugRow) + " ms");
						}
					}
				} catch (ErrMsgException e) {
					LogUtil.getLog(ModuleUtil.class).error(e);
				} catch (SQLException e) {
					LogUtil.getLog(ModuleUtil.class).error(e);
				}
			}

			output.write(sb.toString());
			sb.setLength(0);
			sb.append("</Table>");
			sb.append("<WorksheetOptions xmlns=\"urn:schemas-microsoft-com:office:excel\">");
			sb.append("\n");
			sb.append("<ProtectObjects>False</ProtectObjects>");
			sb.append("\n");
			sb.append("<ProtectScenarios>False</ProtectScenarios>");
			sb.append("\n");
			sb.append("</WorksheetOptions>");
			sb.append("\n");
			sb.append("</Worksheet>");
			sb.append("</Workbook>");
			sb.append("\n");
			output.write(sb.toString());
			output.flush();
			output.close();

			// 当异步导出Excel时
			if (sysProperties.isExportExcelAsync() && Global.getInstance().isUseCache()) {
				ExportExcelItem exportExcelItem = exportExcelCache.get(uid);
				if (exportExcelItem == null) {
					LogUtil.getLog(ModuleUtil.class).error("uid: " + uid + " is not found，it is may be expired.");
				} else {
					exportExcelItem.setUid(uid);
					exportExcelItem.setCurRow(exportExcelItem.getRows());
					exportExcelCache.put(uid, exportExcelItem);

					FormDb fdItem = new FormDb();
					fdItem = fdItem.getFormDb(ConstUtil.FORM_EXPORT_EXCEL);
					FormDAO daoItem = new FormDAO();
					daoItem = daoItem.getFormDAOByCache(exportExcelItem.getId(), fdItem);
					daoItem.setFieldValue("finish_time", new Date());
					daoItem.save();
				}
			}

			DebugUtil.i(ModuleUtil.class, "export", "共 " + total + " 条 耗时：" + (System.currentTimeMillis()-tDebug) + " ms");

		} catch (FileNotFoundException e) {
			LogUtil.getLog(ModuleUtil.class).error(e);
		} catch (IOException e) {
			LogUtil.getLog(ModuleUtil.class).error(e);
		} finally {
			 jt.close();
		}
	}

	public static String getBtnDefaultName(String btnId) {
		String btnDefaultName = "";
		switch (btnId) {
			case ConstUtil.BTN_EDIT:
				btnDefaultName = "编辑";
				break;
			case ConstUtil.BTN_PRINT:
				btnDefaultName = "打印";
				break;
			case ConstUtil.BTN_OK:
				btnDefaultName = "确定";
				break;
			case ConstUtil.BTN_CLOSE:
				btnDefaultName = "关闭";
				break;
			case ConstUtil.BTN_BACK:
				btnDefaultName = "返回";
				break;
		}
		return btnDefaultName;
	}

	public static com.alibaba.fastjson.JSONArray getColProps(ModuleSetupDb msd, boolean isModuleListSel) throws ErrMsgException {
		return getColProps(msd, isModuleListSel, false);
	}

	public static com.alibaba.fastjson.JSONArray getColProps(ModuleSetupDb msd, boolean isModuleListSel, boolean isNested) throws ErrMsgException {
		com.alibaba.fastjson.JSONArray ary = new com.alibaba.fastjson.JSONArray();
		String[] fields = msd.getColAry(false, "list_field");
		if (fields == null || fields.length == 0) {
			throw new ErrMsgException("显示列未配置！");
		}
		String[] fieldsWidth = msd.getColAry(false, "list_field_width");
		String[] fieldsShow = msd.getColAry(false, "list_field_show");
		String[] fieldsTitle = msd.getColAry(false, "list_field_title");
		String[] fieldsAlign = msd.getColAry(false, "list_field_align");

		boolean isColCheckboxShow = true, isColOperateShow = true;
		com.alibaba.fastjson.JSONObject props = com.alibaba.fastjson.JSONObject.parseObject(msd.getString("props"));
		if (props != null) {
			isColCheckboxShow = props.getBoolean("isColCheckboxShow");
			isColOperateShow = props.getBoolean("isColOperateShow");
		}

		// 列表中的嵌套表格不显示操作列
		if (isNested) {
			isColOperateShow = false;
		}

		// 复选框列
		/*if (isColCheckboxShow) {
			if (!isModuleListSel) {
				com.alibaba.fastjson.JSONObject jsonChk = new com.alibaba.fastjson.JSONObject();
				jsonChk.put("type", "checkbox");
				jsonChk.put("align", "center");
				jsonChk.put("fixed", "left");
				ary.add(jsonChk);
			}
		}*/

		String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
		String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
		boolean isPrompt = false;
		if (!"".equals(promptField) && !"".equals(promptIcon)) {
			isPrompt = true;
		}
		if (isPrompt) {
			com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
			json.put("title", "图标");
			json.put("field", "colPrompt");
			json.put("width", 50);
			json.put("sort", false);
			ary.add(json);
		}

		FormDb fd = new FormDb();
		fd = fd.getFormDb(msd.getString("form_code"));

		// 合计列
		com.alibaba.fastjson.JSONObject jsonStat = null;
		String propStat = msd.getString("prop_stat");
		if (StringUtils.isNotEmpty(propStat)) {
			if ("".equals(propStat)) {
				propStat = "{}";
			}
			jsonStat = com.alibaba.fastjson.JSONObject.parseObject(propStat);
		}

		boolean isColOperateToShow = true;

		ICondUtil condUtil = SpringUtil.getBean(ICondUtil.class);
		int len = fields.length;
		for (int i = 0; i < len; i++) {
			String fieldName = fields[i];
			String fieldTitle = fieldsTitle[i];

			Object[] aryTitle = condUtil.getFieldTitle(fd, fieldName, fieldTitle);
			String title = (String) aryTitle[0];
			boolean sortable;
			if (isNested) {
				sortable = false;
			} else {
				sortable = (Boolean) aryTitle[1];
			}
			String macroType = (String)aryTitle[3];

			String w = fieldsWidth[i];
			int wid = StrUtil.toInt(w, 100);
			if (w.indexOf("%") == w.length() - 1) {
				w = w.substring(0, w.length() - 1);
				wid = 800 * StrUtil.toInt(w, 20) / 100;
			}
			wid += 30; // 因为layui table有排序符号

			if ("0".equals(fieldsShow[i])) {
				continue;
			}

			com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
			if ("colOperate".equals(fieldName) && isColOperateShow) {
				isColOperateToShow = false;
				if ("".equals(title)) {
					json.put("title", "操作");
				}
				else {
					json.put("title", title);
				}
				json.put("field", "colOperate");
				if (isModuleListSel) {
					json.put("width", 80);
				}
				else {
					json.put("width", wid);
				}
				json.put("sort", false);
				json.put("align", "center");
				json.put("fixed", "right");
			} else {
				json.put("title", title);
				json.put("field", fieldName);
				json.put("width", wid);
				json.put("sort", sortable);
				json.put("align", fieldsAlign[i]);
				json.put("hide", false);
				json.put("event", "editColumn");
				json.put("macroType", macroType);

				// 如果是合计字段，则置flag为INDEX
				/*if (jsonStat != null && jsonStat.containsKey(fieldName)) {
					json.put("flag", "INDEX");
				}*/
			}
			ary.add(json);
		}

		// 如果未定义colOperate，则将其加入，宽度默认为150
		if (isColOperateShow && isColOperateToShow) {
			com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
			json.put("title", "操作");
			json.put("field", "colOperate");
			json.put("width", 150);
			json.put("sort", false);
			json.put("fixed", "right");
			if (isModuleListSel) {
				json.put("align", "center");
			}
			ary.add(json);
		}
		return ary;
	}

	public static String getConditionHtml(HttpServletRequest request, ModuleSetupDb msd, ArrayList<String> dateFieldNamelist) {
		FormDb fd = new FormDb();
		fd = fd.getFormDb(msd.getString("form_code"));
		ICondUtil condUtil = SpringUtil.getBean(ICondUtil.class);

		String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
		String[] btnNames = StrUtil.split(btnName, ",");
		String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
		String[] btnScripts = StrUtil.split(btnScript, "#");
		if (btnNames != null) {
			int len = btnNames.length;
			for (int i = 0; i < len; i++) {
				if (btnScripts[i].startsWith("{")) {
					Map<String, String> checkboxGroupMap = new HashMap<String, String>();
					com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(btnScripts[i]);
					if ("queryFields".equals(json.getString("btnType"))) {
						String condFields = (String) json.get("fields");
						String condTitles = "";
						if (json.containsKey("titles")) {
							condTitles = (String) json.get("titles");
						}
						String[] fieldAry = StrUtil.split(condFields, ",");
						String[] titleAry = StrUtil.split(condTitles, ",");
						if (fieldAry.length == 0) {
							return "";
						}
						StringBuilder sb = new StringBuilder();
						for (int j = 0; j < fieldAry.length; j++) {
							String fieldName = fieldAry[j];
							String fieldTitle = "#";
							if (titleAry != null) {
								fieldTitle = titleAry[j];
								if ("".equals(fieldTitle)) {
									fieldTitle = "#";
								}
							}

							String condType = (String) json.get(fieldName);
							try {
								CondUnit condUnit = condUtil.getConditonUnit(request, msd, fd, fieldName, fieldTitle, condType, checkboxGroupMap, dateFieldNamelist);
								sb.append("<span class=\"cond-span\">");
								sb.append("<span class=\"cond-title\">");
								sb.append(condUnit.getFieldTitle());
								sb.append("</span>");
								sb.append("<span class=\"cond-ctl\">");
								sb.append(condUnit.getHtml());
								sb.append("</span>");
								sb.append("</span>");
								sb.append("<script>");
								sb.append(condUnit.getScript());
								sb.append("</script>");
							} catch (ErrMsgException e) {
								LogUtil.getLog(ModuleUtil.class).error(e);
							}
						}
						return sb.toString();
					}
				}
			}
		}

		return "";
	}

	public static String getModuleListSelCondScriptFromWinOpener(String conds) {
		if (StrUtil.isEmpty(conds)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("var condStr = '';\n");
		Pattern p = Pattern.compile(
				"\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(conds);
		while (m.find()) {
			String fieldName = m.group(1);
			if ("cwsCurUser".equals(fieldName) || "curUser".equals(fieldName)
					|| "curUserDept".equals(fieldName) || "curUserRole".equals(fieldName) || "admin.dept".equals(fieldName)) {
				continue;
			}
			// 当条件为包含时，fieldName以@开头
			if (fieldName.startsWith("@")) {
				fieldName = fieldName.substring(1);
			}

			String fieldNameReal = fieldName;
			if ("parentId".equals(fieldName) || "mainId".equals(fieldName)) {
				fieldNameReal = "cws_id";
			}

			sb.append("if (window.opener.o('" + fieldNameReal + "') == null) {\n");
			sb.append("	console.error(\"条件字段：" + fieldName + "在表单中不存在！\");\n");
			sb.append("} else {\n");
			sb.append("	if (condStr == \"\")\n");
			sb.append("		condStr = \"" + fieldName + "=\" + encodeURI(window.opener.o(\"" + fieldNameReal + "\").value);\n");
			sb.append("	else\n");
			sb.append("		condStr += \"&" + fieldName + "=\" + encodeURI(window.opener.o(\"" + fieldNameReal + "\").value);\n");
			sb.append("}\n");
		}
		return sb.toString();
	}

	/**
	 * 取得表单域选择宏控件的源模块条件中的字段
	 * @param conds
	 * @return
	 */
	public static String getModuleListNestSelCondScriptFromWinOpener(String conds) {
		if (StrUtil.isEmpty(conds)) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("var condStr = '';\n");
		Pattern p = Pattern.compile(
				"\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(conds);
		while (m.find()) {
			String fieldName = m.group(1);
			if ("cwsCurUser".equals(fieldName) || "curUser".equals(fieldName)
					|| "curUserDept".equals(fieldName) || "curUserRole".equals(fieldName) || "admin.dept".equals(fieldName) || "mainId".equals(fieldName)) {
				continue;
			}
			// 当条件为包含时，fieldName以@开头
			if (fieldName.startsWith("@")) {
				fieldName = fieldName.substring(1);
			}

			String fieldNameReal = fieldName;
			/*if ("parentId".equals(fieldName) || "mainId".equals(fieldName)) {
				fieldNameReal = "cws_id";
			}*/

			sb.append("	if (condStr == \"\")\n");
			sb.append("		condStr = \"" + fieldName + "=\" + encodeURI(window.opener.o(\"" + fieldNameReal + "\").value);\n");
			sb.append("	else\n");
			sb.append("		condStr += \"&" + fieldName + "=\" + encodeURI(window.opener.o(\"" + fieldNameReal + "\").value);\n");
		}
		return sb.toString();
	}

	/**
	 * 取得表单域选择宏控件的源模块条件中的字段
	 * @param conds
	 * @return
	 */
	public static List<String> getModuleListNestSelCondFields(String conds) {
		List<String> list = new ArrayList<>();
		if (StrUtil.isEmpty(conds)) {
			return list;
		}
		Pattern p = Pattern.compile(
				"\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(conds);
		while (m.find()) {
			String fieldName = m.group(1);
			if ("cwsCurUser".equals(fieldName) || "curUser".equals(fieldName)
					|| "curUserDept".equals(fieldName) || "curUserRole".equals(fieldName) || "admin.dept".equals(fieldName) || "mainId".equals(fieldName)) {
				continue;
			}
			// 当条件为包含时，fieldName以@开头
			if (fieldName.startsWith("@")) {
				fieldName = fieldName.substring(1);
			}

			list.add(fieldName);
		}
		return list;
	}

	/**
	 * 渲染选项卡
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public static com.alibaba.fastjson.JSONArray renderTabsBack(HttpServletRequest request) throws ErrMsgException {
		String codeTop = ParamUtil.get(request, "moduleCode");
		if ("".equals(codeTop)) {
			codeTop = ParamUtil.get(request, "code");
			if ("".equals(codeTop)) {
				codeTop = ParamUtil.get(request, "formCode");
			}
		}

		// 如果传了mainCode，则说明是从其它模块列表的头部选项卡链接过来
		String mainCode = ParamUtil.get(request, "mainCode");
		if (!"".equals(mainCode)) {
			codeTop = mainCode;
		}

		// 主模块的页面类型
		String pageTypeTop = (String) request.getAttribute("pageType");
		if (pageTypeTop == null || ConstUtil.PAGE_TYPE_LIST_RELATE.equals(pageTypeTop) || ConstUtil.PAGE_TYPE_ADD.equals(pageTypeTop)) {
			pageTypeTop = ParamUtil.get(request, "parentPageType"); // module_list_relate.jsp中
		}

		ModuleSetupDb msdTop = new ModuleSetupDb();
		msdTop = msdTop.getModuleSetupDb(codeTop);
		if (msdTop == null) {
			throw new ErrMsgException(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块：" + codeTop + "不存在"));
		}

		String formCodeTop = msdTop.getString("form_code");

		long parentIdTop = ParamUtil.getLong(request, "parentId", -1);
		if (parentIdTop == -1) {
			parentIdTop = ParamUtil.getLong(request, "id", -1);
		}

		com.redmoon.oa.visual.FormDAO fdaoTop = new com.redmoon.oa.visual.FormDAO();
		FormDb fdTop = new FormDb();
		fdTop = fdTop.getFormDb(formCodeTop);
		if (parentIdTop != -1) {
			fdaoTop = fdaoTop.getFormDAO(parentIdTop, fdTop);
		}
		String requestParamsTop;
		StringBuffer requestParamsBuf = new StringBuffer();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			String[] paramValues = request.getParameterValues(paramName);
			if (paramValues.length == 1) {
				String paramValue = ParamUtil.getParam(request, paramName);
				// 过滤掉formCode等
				if ("code".equals(paramName)
						|| "formCode".equals(paramName)
						|| "moduleCode".equals(paramName)
						|| "mainCode".equals(paramName)
						|| "menuItem".equals(paramName)
						|| "parentId".equals(paramName)
						|| "moduleCodeRelated".equals(paramName)
						|| "formCodeRelated".equals(paramName)
						|| "mode".equals(paramName) // 去掉mode及tagName，否则当存在mode=subTagRelated，关联模块中就会有问题
						|| "tagName".equals(paramName)
						|| "id".equals(paramName)
						|| "parentPageType".equals(paramName)
						|| "vOrders".equals(paramName)
				) {
					;
				} else {
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

		com.alibaba.fastjson.JSONArray aryTop = new com.alibaba.fastjson.JSONArray();

		// 当页面为edit、show、list即module_edit.jsp module_show.jsp module_list.jsp时不需要处理关联模块时
		if (parentIdTop == -1) {
			String moduleUrlList;
			if (msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_GANTT || msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_GANTT_LIST) {
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				moduleUrlList = request.getContextPath() + servletPathTop + "/module_list_gantt.jsp?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop);
				json.put("url", moduleUrlList);
				json.put("name", msdTop.getName() + "看板");
				json.put("id", "menu100");
			} else if (msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_CALENDAR || msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_CALENDAR_LIST) {
				moduleUrlList = request.getContextPath() + servletPathTop + "/module_list_calendar.jsp?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop) + "&menuItem=101";
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				json.put("url", moduleUrlList);
				json.put("name", msdTop.getName() + "日历看板");
				json.put("id", "menu101");
			} else {
				moduleUrlList = StrUtil.getNullStr(msdTop.getString("url_list"));
				if ("".equals(moduleUrlList)) {
					moduleUrlList = request.getContextPath() + "/visual/moduleListPage.do?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop) + "&" + requestParamsTop;
				} else {
					moduleUrlList = request.getContextPath() + "/" + moduleUrlList;
					if (!moduleUrlList.contains("&")) {
						moduleUrlList += "?" + requestParamsTop;
					} else {
						moduleUrlList += "&" + requestParamsTop;
					}
				}

				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				json.put("url", moduleUrlList);
				json.put("name", msdTop.getName());
				json.put("id", "menu1");
			}
			if (msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_GANTT_LIST) {
				moduleUrlList = request.getContextPath() + servletPathTop + "/moduleListPage.do?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop) + "&" + requestParamsTop;
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				json.put("url", moduleUrlList);
				json.put("name", msdTop.getName());
				json.put("id", "menu1");
			} else if (msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_CALENDAR_LIST) {
				moduleUrlList = request.getContextPath() + servletPathTop + "/moduleListPage.do?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop) + "&" + requestParamsTop;
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				json.put("url", moduleUrlList);
				json.put("name", msdTop.getName());
				json.put("id", "menu1");
			}

			com.redmoon.oa.pvg.Privilege pvgTop = new com.redmoon.oa.pvg.Privilege();
			// 此处判断模块权限应该使用模块的code，formCodeTop此时为formCode
			ModulePrivDb mpdTop = new ModulePrivDb(codeTop);
			if (msdTop.getInt("btn_log_show") == 1) {
				if (mpdTop.canUserLog(pvgTop.getUser(request)) || mpdTop.canUserManage(pvgTop.getUser(request))) {
					if (fdTop.isLog()) {
						long fdaoIdTop = ParamUtil.getLong(request, "id", -1);
						String opLog = "search";
						if (fdaoIdTop == -1) {
							opLog = "";
						}

						moduleUrlList = request.getContextPath() + servletPathTop + "/module_log_list.jsp?op=" + opLog + "&code=" + codeTop + "&fdaoId=" + fdaoIdTop + "&moduleFormCode=" + formCodeTop + "&moduleCodeLog=" + msdTop.getString("module_code_log") + "&" + requestParamsTop;
						com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
						aryTop.add(json);
						json.put("url", moduleUrlList);
						json.put("name", "修改日志");
						json.put("id", "menu4");

						com.redmoon.oa.Config cfgTop = com.redmoon.oa.Config.getInstance();
						boolean isModuleLogRead = cfgTop.getBooleanProperty("isModuleLogRead");
						if (isModuleLogRead) {
							moduleUrlList = request.getContextPath() + "/visual/moduleListPage.do?op=search&code=module_log_read&read_type=" + FormDAOLog.READ_TYPE_MODULE + "&module_code=" + codeTop + "&form_code=" + formCodeTop;
							json = new com.alibaba.fastjson.JSONObject();
							aryTop.add(json);
							json.put("url", moduleUrlList);
							json.put("name", "浏览日志");
							json.put("id", "menu5");
							json.put("target", "newTab");
						}
					}
				}
			}

			int menuItemTop = 6;

			// 列表页上的选项卡设置
			String[] tagsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("nav_tag_name")), ",");
			String[] tagUrlsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("nav_tag_url")), ",");
			int lenTop = 0;
			if (tagsTop != null) {
				lenTop = tagsTop.length;
			}
			for (int i = 0; i < lenTop; i++) {
				String url = tagUrlsTop[i];
				if (url.startsWith("{")) {
					com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(url);
					String moduleCodeTop = json.getString("moduleCode");
					if (!"".equals(mainCode)) {
						url = request.getContextPath() + "/visual/moduleListPage.do?code=" + moduleCodeTop + "&mainCode=" + mainCode + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
					} else {
						url = request.getContextPath() + "/visual/moduleListPage.do?code=" + moduleCodeTop + "&mainCode=" + codeTop + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
					}
				} else {
					if (!url.startsWith("http")) {
						url = request.getContextPath() + "/" + url;
						if (url.contains("?")) {
							if (!"".equals(mainCode)) {
								url += "&mainCode=" + mainCode + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
							} else {
								url += "&mainCode=" + codeTop + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
							}
						} else {
							if (!"".equals(mainCode)) {
								url += "?mainCode=" + mainCode + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
							} else {
								url += "?mainCode=" + codeTop + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
							}
						}
					}
				}

				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				json.put("url", url);
				json.put("name", tagsTop[i]);
				json.put("id", "menu" + menuItemTop);
				menuItemTop++;
			}
		} else {
			if (msdTop.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
				String url = FormUtil.parseAndSetFieldValue(msdTop.getString("url_show"), fdaoTop);
				if (!url.contains("?")) {
					url += "?";
				} else {
					url += "&";
				}

				String moduleUrlList = request.getContextPath() + "/" + url + "id=" + parentIdTop + "&parentId=" + parentIdTop + "&code=" + codeTop + "&formCode=" + formCodeTop + "&" + requestParamsTop;
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				json.put("url", moduleUrlList);
				json.put("name", msdTop.getName());
				json.put("id", "menu1");
			} else {
				if ("edit".equals(pageTypeTop)) {
					String moduleUrlList = request.getContextPath() + servletPathTop + "/moduleEditPage.do?id=" + parentIdTop + "&parentId=" + parentIdTop + "&code=" + codeTop + "&formCode=" + formCodeTop + "&" + requestParamsTop;
					com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
					aryTop.add(json);
					json.put("url", moduleUrlList);
					json.put("name", msdTop.getName());
					json.put("id", "menu1");
				} else {
					String moduleUrlList = request.getContextPath() + servletPathTop + "/moduleShowPage.do?id=" + parentIdTop + "&parentId=" + parentIdTop + "&code=" + codeTop + "&formCode=" + formCodeTop + "&" + requestParamsTop;
					com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
					aryTop.add(json);
					json.put("url", moduleUrlList);
					json.put("name", msdTop.getName());
					json.put("id", "menu1");
				}
			}

			int menuItemTop = 6;
			// 关联模块选项卡
			com.redmoon.oa.pvg.Privilege pvgTop = new com.redmoon.oa.pvg.Privilege();
			String curUserTop = pvgTop.getUser(request);
			ModuleRelateDb mrdTop = new ModuleRelateDb();
			ModuleSetupDb msdRelate = new ModuleSetupDb();
			com.alibaba.fastjson.JSONArray aryRelate = new com.alibaba.fastjson.JSONArray();
			for (ModuleRelateDb moduleRelateDb : mrdTop.getModulesRelated(formCodeTop)) {
				mrdTop = moduleRelateDb;
				// 有查看权限才能看到从模块选项卡
				ModulePrivDb mpdTop = new ModulePrivDb(mrdTop.getString("relate_code"));
				if (mpdTop.canUserSee(curUserTop) && mrdTop.getInt("is_on_tab") == 1) {
					// 条件检查
					String conds = StrUtil.getNullStr(mrdTop.getString("conds"));
					if (!"".equals(conds)) {
						String cond = ModuleUtil.parseConds(request, fdaoTop, conds);
						javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
						javax.script.ScriptEngine engine = manager.getEngineByName("javascript");
						try {
							Boolean ret = (Boolean) engine.eval(cond);
							if (!ret) {
								continue;
							}
						} catch (javax.script.ScriptException ex) {
							LogUtil.getLog(ModuleUtil.class).error(ex);
						}
					}

					msdRelate = msdRelate.getModuleSetupDb(mrdTop.getString("relate_code"));
					String url = request.getContextPath() + "/visual/moduleListRelatePage.do?parentPageType=" + pageTypeTop + "&parentId=" + parentIdTop + "&menuItem=" + menuItemTop + "&code=" + codeTop + "&moduleCodeRelated=" + mrdTop.getString("relate_code") + "&formCode=" + formCodeTop + "&" + requestParamsTop;
					com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
					aryRelate.add(json);
					json.put("url", url);
					json.put("name", msdRelate.getName());
					String tabName = moduleRelateDb.getString("tab_name");
					if (StrUtil.isEmpty(tabName)) {
						tabName = msdRelate.getName();
					}
					json.put("tabName", tabName);
					json.put("id", "menu" + menuItemTop);
					json.put("orders", moduleRelateDb.getInt("relate_order"));

					menuItemTop++;
				}
			}

			// 将orders一样的选项卡只保留第一个
			double lastOrders = -100;
			com.alibaba.fastjson.JSONObject lastJson = null;
			Iterator irRelate = aryRelate.iterator();
			while (irRelate.hasNext()) {
				com.alibaba.fastjson.JSONObject json = (com.alibaba.fastjson.JSONObject)irRelate.next();
				double orders = json.getDoubleValue("orders");
				if (orders != lastOrders) {
					lastOrders = orders;
					lastJson = json;
				}
				else {
					if (!lastJson.containsKey("isVertical")) {
						lastJson.put("isVertical", true);
						String url = lastJson.getString("url");
						url = url.replace("moduleListRelatePage.do", "module_list_relate_v.jsp");
						url += "&vOrders=" + lastOrders;
						lastJson.put("url", url);
					}
					irRelate.remove();
				}
			}

			aryTop.addAll(aryRelate);

			// 查询关联选项卡
			lastOrders = -100;
			lastJson = null;
			String[] subTagsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("sub_nav_tag_name")), "\\|");
			String[] subTagOrders = StrUtil.split(StrUtil.getNullStr(msdTop.getString("sub_nav_tag_order")), "\\|");
			int subLenTop = 0;
			if (subTagsTop != null) {
				subLenTop = subTagsTop.length;
			}
			for (int i = 0; i < subLenTop; i++) {
				String uri = ModuleUtil.filterViewEditTagUrl(request, codeTop, subTagsTop[i]);
				String url = "";
				if (uri.contains("?")) {
					url = uri + "&parentId=" + parentIdTop + "&code=" + codeTop + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
				} else {
					url = uri + "?parentId=" + parentIdTop + "&code=" + codeTop + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
				}

				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				json.put("url", url);
				json.put("name", subTagsTop[i]);
				json.put("id", "menu" + menuItemTop);
				json.put("tagName", subTagsTop[i]);

				// 通过order分组显示，只支持“模块链接选项卡”
				boolean isSubTagRelatedTop = uri.contains("subTagRelated");
				if (isSubTagRelatedTop) {
					double order = StrUtil.toDouble(subTagOrders[i], 0);
					if (lastOrders != order) {
						lastOrders = order;
						lastJson = json;
					}
					else {
						if (!lastJson.containsKey("isVertical")) {
							lastJson.put("isVertical", true);
							url = lastJson.getString("url");
							url = url.replace("moduleListRelatePage.do", "module_list_relate_v.jsp");
							url += "&vOrders=" + lastOrders;
							lastJson.put("url", url);
						}
						continue;
					}
				}

				aryTop.add(json);
				menuItemTop++;
			}
		}
		return aryTop;
	}

	/**
	 * 渲染选项卡
	 * @param request
	 * @return
	 * @throws ErrMsgException
	 */
	public static com.alibaba.fastjson.JSONArray renderTabs(HttpServletRequest request) throws ErrMsgException {
		String codeTop = ParamUtil.get(request, "moduleCode");
		if ("".equals(codeTop)) {
			codeTop = ParamUtil.get(request, "code");
			if ("".equals(codeTop)) {
				codeTop = ParamUtil.get(request, "formCode");
			}
		}

		// 如果传了mainCode，则说明是从其它模块列表的头部选项卡链接过来
		String mainCode = ParamUtil.get(request, "mainCode");
		if (!"".equals(mainCode)) {
			codeTop = mainCode;
		}

		// 主模块的页面类型
		String pageTypeTop = (String) request.getAttribute("pageType");
		if (pageTypeTop == null || ConstUtil.PAGE_TYPE_LIST_RELATE.equals(pageTypeTop) || ConstUtil.PAGE_TYPE_ADD.equals(pageTypeTop)) {
			pageTypeTop = ParamUtil.get(request, "parentPageType"); // module_list_relate.jsp中
		}

		ModuleSetupDb msdTop = new ModuleSetupDb();
		msdTop = msdTop.getModuleSetupDb(codeTop);
		if (msdTop == null) {
			throw new ErrMsgException(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块：" + codeTop + "不存在"));
		}

		String formCodeTop = msdTop.getString("form_code");

		long parentIdTop = ParamUtil.getLong(request, "parentId", -1);
		if (parentIdTop == -1) {
			parentIdTop = ParamUtil.getLong(request, "id", -1);
		}

		com.redmoon.oa.visual.FormDAO fdaoTop = new com.redmoon.oa.visual.FormDAO();
		FormDb fdTop = new FormDb();
		fdTop = fdTop.getFormDb(formCodeTop);
		if (parentIdTop != -1) {
			fdaoTop = fdaoTop.getFormDAO(parentIdTop, fdTop);
		}
		com.alibaba.fastjson.JSONObject requestParams = new com.alibaba.fastjson.JSONObject();
		String requestParamsTop;
		StringBuffer requestParamsBuf = new StringBuffer();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			String[] paramValues = request.getParameterValues(paramName);
			if (paramValues.length == 1) {
				String paramValue = ParamUtil.getParam(request, paramName);
				// 过滤掉formCode等
				if ("code".equals(paramName)
						|| "formCode".equals(paramName)
						|| "moduleCode".equals(paramName)
						|| "mainCode".equals(paramName)
						|| "menuItem".equals(paramName)
						|| "parentId".equals(paramName)
						|| "moduleCodeRelated".equals(paramName)
						|| "formCodeRelated".equals(paramName)
						|| "mode".equals(paramName) // 去掉mode及tagName，否则当存在mode=subTagRelated，关联模块中就会有问题
						|| "tagName".equals(paramName)
						|| "id".equals(paramName)
						|| "parentPageType".equals(paramName)
						|| "vOrders".equals(paramName)
				) {
					;
				} else {
					requestParams.put(paramName, paramValue);
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

		com.alibaba.fastjson.JSONArray aryTop = new com.alibaba.fastjson.JSONArray();

		// 当页面为edit、show、list即module_edit.jsp module_show.jsp module_list.jsp时不需要处理关联模块时
		if (parentIdTop == -1) {
			String moduleUrlList;
			if (msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_GANTT || msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_GANTT_LIST) {
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				/*moduleUrlList = request.getContextPath() + servletPathTop + "/module_list_gantt.jsp?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop);
				json.put("url", moduleUrlList);*/
				json.put("name", msdTop.getName() + "看板");
				json.put("id", "menu100");
				json.put("type", ConstUtil.PAGE_TYPE_GANTT);
				com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
				params.put("moduleCode", codeTop);
				params.put("formCode", formCodeTop);
				json.put("params", params);
			} else if (msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_CALENDAR || msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_CALENDAR_LIST) {
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				// json.put("name", msdTop.getName() + "日历视图");
				json.put("name", "日历视图");
				json.put("id", "menu101");
				json.put("type", ConstUtil.PAGE_TYPE_CALENDAR);
				com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
				params.put("moduleCode", codeTop);
				params.put("formCode", formCodeTop);
				json.put("params", params);
			} else {
				/*moduleUrlList = StrUtil.getNullStr(msdTop.getString("url_list"));
				if ("".equals(moduleUrlList)) {
					moduleUrlList = request.getContextPath() + "/visual/moduleListPage.do?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop) + "&" + requestParamsTop;
				} else {
					moduleUrlList = request.getContextPath() + "/" + moduleUrlList;
					if (!moduleUrlList.contains("&")) {
						moduleUrlList += "?" + requestParamsTop;
					} else {
						moduleUrlList += "&" + requestParamsTop;
					}
				}*/

				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				// json.put("url", moduleUrlList);
				json.put("name", msdTop.getName());
				json.put("id", "menu1");
				json.put("type", ConstUtil.PAGE_TYPE_LIST_RELATE);
				com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
				params.put("moduleCode", codeTop);
				params.put("formCode", formCodeTop);
				params.putAll(requestParams);
				json.put("params", params);
			}
			if (msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_GANTT_LIST) {
				// moduleUrlList = request.getContextPath() + servletPathTop + "/moduleListPage.do?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop) + "&" + requestParamsTop;
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				// json.put("url", moduleUrlList);
				json.put("name", msdTop.getName());
				json.put("id", "menu1");

				json.put("type", ConstUtil.PAGE_TYPE_LIST_RELATE);
				com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
				params.put("moduleCode", codeTop);
				params.put("formCode", formCodeTop);
				params.putAll(requestParams);
				json.put("params", params);
			} else if (msdTop.getInt("view_list") == ModuleSetupDb.VIEW_LIST_CALENDAR_LIST) {
				// moduleUrlList = request.getContextPath() + servletPathTop + "/moduleListPage.do?code=" + codeTop + "&formCode=" + StrUtil.UrlEncode(formCodeTop) + "&" + requestParamsTop;
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				// json.put("url", moduleUrlList);
				json.put("name", msdTop.getName());
				json.put("id", "menu1");

				json.put("type", ConstUtil.PAGE_TYPE_LIST_RELATE);
				com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
				params.put("moduleCode", codeTop);
				params.put("formCode", formCodeTop);
				params.putAll(requestParams);
				json.put("params", params);
			}

			com.redmoon.oa.pvg.Privilege pvgTop = new com.redmoon.oa.pvg.Privilege();
			// 此处判断模块权限应该使用模块的code，formCodeTop此时为formCode
			ModulePrivDb mpdTop = new ModulePrivDb(codeTop);
			if (msdTop.getInt("btn_log_show") == 1) {
				if (mpdTop.canUserLog(pvgTop.getUser(request)) || mpdTop.canUserManage(pvgTop.getUser(request))) {
					if (fdTop.isLog()) {
						long fdaoIdTop = ParamUtil.getLong(request, "id", -1);
						String opLog = "search";
						if (fdaoIdTop == -1) {
							opLog = "";
						}

						com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
						aryTop.add(json);
						json.put("name", "修改日志");
						json.put("id", "menu4");
						json.put("type", "logList");
						com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
						params.put("moduleCode", codeTop);
						params.put("moduleFormCode", formCodeTop);
						params.put("moduleCodeLog", msdTop.getString("module_code_log"));
						params.putAll(requestParams);
						json.put("params", params);

						com.redmoon.oa.Config cfgTop = com.redmoon.oa.Config.getInstance();
						boolean isModuleLogRead = cfgTop.getBooleanProperty("isModuleLogRead");
						if (isModuleLogRead) {
							// moduleUrlList = request.getContextPath() + "/visual/moduleListPage.do?op=search&code=module_log_read&read_type=" + FormDAOLog.READ_TYPE_MODULE + "&module_code=" + codeTop + "&form_code=" + formCodeTop;
							json = new com.alibaba.fastjson.JSONObject();
							aryTop.add(json);
							// json.put("url", moduleUrlList);
							json.put("name", "浏览日志");
							json.put("id", "menu5");
							json.put("type", "logListRead");
							params = new com.alibaba.fastjson.JSONObject();
							params.put("moduleCodeLog", "module_log_read");
							params.put("read_type", FormDAOLog.READ_TYPE_MODULE);
							params.put("moduleCode", codeTop);
							params.put("moduleFormCode", formCodeTop);
							json.put("params", params);
						}
					}
				}
			}

			int menuItemTop = 6;

			// 列表页上的选项卡设置
			String[] tagsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("nav_tag_name")), ",");
			String[] tagUrlsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("nav_tag_url")), ",");
			int lenTop = 0;
			if (tagsTop != null) {
				lenTop = tagsTop.length;
			}
			for (int i = 0; i < lenTop; i++) {
				com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
				String url = tagUrlsTop[i];
				String type = "", moduleCode = "";
				if (url.startsWith("{")) {
					com.alibaba.fastjson.JSONObject json = com.alibaba.fastjson.JSONObject.parseObject(url);
					moduleCode = json.getString("moduleCode");
					params.put("moduleCode", moduleCode);
					if (!"".equals(mainCode)) {
						// url = request.getContextPath() + "/visual/moduleListPage.do?code=" + moduleCodeTop + "&mainCode=" + mainCode + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
						params.put("mainCode", mainCode);
					} else {
						// url = request.getContextPath() + "/visual/moduleListPage.do?code=" + moduleCodeTop + "&mainCode=" + codeTop + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
						params.put("mainCode", codeTop);
					}
					type = ConstUtil.PAGE_TYPE_LIST_RELATE;
					url = "";
				} else {
					if (!url.startsWith("http")) {
						url = request.getContextPath() + "/" + url;
						if (url.contains("?")) {
							if (!"".equals(mainCode)) {
								url += "&mainCode=" + mainCode + "&" + requestParamsTop;
							} else {
								url += "&mainCode=" + codeTop + "&" + requestParamsTop;
							}
						} else {
							if (!"".equals(mainCode)) {
								url += "?mainCode=" + mainCode + "&" + requestParamsTop;
							} else {
								url += "?mainCode=" + codeTop + "&" + requestParamsTop;
							}
						}
					}
					type = ConstUtil.PAGE_TYPE_CUSTOM;
				}

				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				json.put("url", url);
				json.put("name", tagsTop[i]);
				json.put("id", "menu" + menuItemTop);

				json.put("type", type);
				params.putAll(requestParams);
				json.put("params", params);

				menuItemTop++;
			}
		} else {
			if (msdTop.getInt("view_show") == ModuleSetupDb.VIEW_SHOW_CUSTOM) {
				String url = FormUtil.parseAndSetFieldValue(msdTop.getString("url_show"), fdaoTop);
				if (!url.contains("?")) {
					url += "?";
				} else {
					url += "&";
				}

				String moduleUrlList = request.getContextPath() + "/" + url + "id=" + parentIdTop + "&parentId=" + parentIdTop + "&moduleCode=" + codeTop + "&formCode=" + formCodeTop + "&" + requestParamsTop;
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				aryTop.add(json);
				json.put("url", moduleUrlList);
				json.put("name", msdTop.getName());
				json.put("id", "menu1");

				json.put("type", ConstUtil.PAGE_TYPE_CUSTOM);
				com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
				params.put("parentId", parentIdTop);
				params.put("moduleCode", codeTop);
				params.put("formCode", formCodeTop);
				params.putAll(requestParams);
				json.put("params", params);
			} else {
				if ("edit".equals(pageTypeTop)) {
					// String moduleUrlList = request.getContextPath() + servletPathTop + "/moduleEditPage.do?id=" + parentIdTop + "&parentId=" + parentIdTop + "&code=" + codeTop + "&formCode=" + formCodeTop + "&" + requestParamsTop;
					com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
					aryTop.add(json);
					// json.put("url", moduleUrlList);
					json.put("name", msdTop.getName());
					json.put("id", "menu1");

					json.put("type", ConstUtil.PAGE_TYPE_EDIT);
					com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
					params.put("id", parentIdTop);
					params.put("moduleCode", codeTop);
					params.put("formCode", formCodeTop);
					params.putAll(requestParams);
					json.put("params", params);
				} else {
					// String moduleUrlList = request.getContextPath() + servletPathTop + "/moduleShowPage.do?id=" + parentIdTop + "&parentId=" + parentIdTop + "&code=" + codeTop + "&formCode=" + formCodeTop + "&" + requestParamsTop;
					com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
					aryTop.add(json);
					// json.put("url", moduleUrlList);
					json.put("name", msdTop.getName());
					json.put("id", "menu1");

					json.put("type", ConstUtil.PAGE_TYPE_SHOW);
					com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
					params.put("id", parentIdTop);
					params.put("moduleCode", codeTop);
					params.put("formCode", formCodeTop);
					params.putAll(requestParams);
					json.put("params", params);
				}
			}

			int menuItemTop = 6;
			// 关联模块选项卡
			com.redmoon.oa.pvg.Privilege pvgTop = new com.redmoon.oa.pvg.Privilege();
			String curUserTop = pvgTop.getUser(request);
			ModuleRelateDb mrdTop = new ModuleRelateDb();
			ModuleSetupDb msdRelate = new ModuleSetupDb();
			com.alibaba.fastjson.JSONArray aryRelate = new com.alibaba.fastjson.JSONArray();
			for (ModuleRelateDb moduleRelateDb : mrdTop.getModulesRelated(formCodeTop)) {
				mrdTop = moduleRelateDb;
				LogUtil.getLog(ModuleUtil.class).info("moduleRelateDb name=" + moduleRelateDb.getString("relate_code") + " is_on_tab=" + mrdTop.getInt("is_on_tab"));
				// 有查看权限才能看到从模块选项卡
				ModulePrivDb mpdTop = new ModulePrivDb(mrdTop.getString("relate_code"));
				if (mpdTop.canUserSee(curUserTop) && mrdTop.getInt("is_on_tab") == 1) {
					// 条件检查
					String conds = StrUtil.getNullStr(mrdTop.getString("conds"));
					LogUtil.getLog(ModuleUtil.class).info("moduleRelateDb conds=" + conds);
					if (!"".equals(conds)) {
						String cond = ModuleUtil.parseConds(request, fdaoTop, conds);
						javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
						javax.script.ScriptEngine engine = manager.getEngineByName("javascript");
						try {
							Boolean ret = (Boolean) engine.eval(cond);
							if (!ret) {
								continue;
							}
						} catch (javax.script.ScriptException ex) {
							LogUtil.getLog(ModuleUtil.class).error(ex);
						}
					}

					msdRelate = msdRelate.getModuleSetupDb(mrdTop.getString("relate_code"));
					if (msdRelate == null) {
						DebugUtil.e(ModuleUtil.class, "renderTabs", "关联模块: " + mrdTop.getString("relate_code") + " 不存在");
						msdRelate = new ModuleSetupDb();
						continue;
					}
					// String url = request.getContextPath() + "/visual/moduleListRelatePage.do?parentPageType=" + pageTypeTop + "&parentId=" + parentIdTop + "&menuItem=" + menuItemTop + "&code=" + codeTop + "&moduleCodeRelated=" + mrdTop.getString("relate_code") + "&formCode=" + formCodeTop + "&" + requestParamsTop;
					com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
					aryRelate.add(json);

					// json.put("url", url);
					json.put("name", msdRelate.getName());
					String tabName = moduleRelateDb.getString("tab_name");
					if (StrUtil.isEmpty(tabName)) {
						tabName = msdRelate.getName();
					}
					json.put("tabName", tabName);
					json.put("id", "menu" + menuItemTop);
					json.put("orders", moduleRelateDb.getInt("relate_order"));

					json.put("type", ConstUtil.PAGE_TYPE_LIST_RELATE);
					com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
					params.put("parentPageType", pageTypeTop);
					params.put("parentId", parentIdTop);
					params.put("moduleCode", codeTop);
					params.put("moduleCodeRelated", mrdTop.getString("relate_code"));
					params.putAll(requestParams);
					json.put("params", params);

					menuItemTop++;
				}
			}

			double lastOrders = -100;
			com.alibaba.fastjson.JSONObject lastJson = null;
			// 因暂不支持垂直排列，故先删除
			// 将orders一样的选项卡只保留第一个
			/*
			Iterator irRelate = aryRelate.iterator();
			while (irRelate.hasNext()) {
				com.alibaba.fastjson.JSONObject json = (com.alibaba.fastjson.JSONObject)irRelate.next();
				double orders = json.getDoubleValue("orders");
				if (orders != lastOrders) {
					lastOrders = orders;
					lastJson = json;
				}
				else {
					if (!lastJson.containsKey("isVertical")) {
						lastJson.put("isVertical", true);
						*//*String url = lastJson.getString("url");
						url = url.replace("moduleListRelatePage.do", "module_list_relate_v.jsp");
						url += "&vOrders=" + lastOrders;
						lastJson.put("url", url);*//*
					}
					irRelate.remove();
				}
			}*/

			aryTop.addAll(aryRelate);

			// 查询关联选项卡
			lastOrders = -100;
			lastJson = null;
			String[] subTagsTop = StrUtil.split(StrUtil.getNullStr(msdTop.getString("sub_nav_tag_name")), "\\|");
			String[] subTagOrders = StrUtil.split(StrUtil.getNullStr(msdTop.getString("sub_nav_tag_order")), "\\|");
			String[] subTagUrls = StrUtil.split(StrUtil.getNullStr(msdTop.getString("sub_nav_tag_url")), "\\|");

			int subLenTop = 0;
			if (subTagsTop != null) {
				subLenTop = subTagsTop.length;
			}
			for (int i = 0; i < subLenTop; i++) {
				String tagUrl = subTagUrls[i];
				com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject)com.alibaba.fastjson.JSONObject.parse(tagUrl);
				if (jsonObject != null && jsonObject.containsKey("conds")) {
					// 条件检查
					String conds = jsonObject.getString("conds");
					if (!StrUtil.isEmpty(conds)) {
						String cond = ModuleUtil.parseConds(request, fdaoTop, conds);
						javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
						javax.script.ScriptEngine engine = manager.getEngineByName("javascript");
						try {
							Boolean ret = (Boolean) engine.eval(cond);
							if (!ret) {
								continue;
							}
						} catch (javax.script.ScriptException ex) {
							ex.printStackTrace();
						}
					}
				}

				String uri = ModuleUtil.filterViewEditTagUrl(request, codeTop, subTagsTop[i]);
				String url = "";
				if (uri.contains("?")) {
					url = uri + "&parentId=" + parentIdTop + "&moduleCode=" + codeTop + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
				} else {
					url = uri + "?parentId=" + parentIdTop + "&moduleCode=" + codeTop + "&menuItem=" + menuItemTop + "&" + requestParamsTop;
				}

				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				json.put("url", url);
				json.put("name", subTagsTop[i]);
				json.put("id", "menu" + menuItemTop);
				json.put("tagName", subTagsTop[i]);

				json.put("type", ConstUtil.PAGE_TYPE_LIST_RELATE);
				com.alibaba.fastjson.JSONObject params = new com.alibaba.fastjson.JSONObject();
				params.put("parentPageType", pageTypeTop);
				params.put("parentId", parentIdTop);
				params.put("moduleCode", codeTop);
				params.putAll(requestParams);
				json.put("params", params);

				// 通过order分组显示，只支持“模块链接选项卡”
				boolean isSubTagRelatedTop = uri.contains("subTagRelated");
				if (isSubTagRelatedTop) {
					json.put("mode", "subTagRelated");

					double order = StrUtil.toDouble(subTagOrders[i], 0);
					if (lastOrders != order) {
						lastOrders = order;
						lastJson = json;
					}
					else {
						if (!lastJson.containsKey("isVertical")) {
							lastJson.put("isVertical", true);
							url = lastJson.getString("url");
							url = url.replace("moduleListRelatePage.do", "module_list_relate_v.jsp");
							url += "&vOrders=" + lastOrders;
							lastJson.put("url", url);
						}
						continue;
					}
				}

				aryTop.add(json);
				menuItemTop++;
			}
		}
		return aryTop;
	}

	public static String parseScript(int flowId, long id, String formCode, String moduleCode, long parentId, String formCodeRelated, String moduleCodeRelated, String pageType, String content) {
		if (content == null) {
			return "";
		}
		Pattern p = Pattern.compile(
				"\\{\\$([@A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(content);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String fieldName = m.group(1);

			String val = "";
			if ("id".equalsIgnoreCase(fieldName)) {
				val = String.valueOf(id);
			} else if ("flowId".equalsIgnoreCase(fieldName)) {
				val = String.valueOf(flowId);
			} else if ("formCode".equalsIgnoreCase(fieldName)) {
				val = formCode;
			} else if ("moduleCode".equalsIgnoreCase(fieldName)) {
				val = moduleCode;
			} else if ("formCodeRelated".equalsIgnoreCase(fieldName)) {
				val = formCodeRelated;
			} else if ("pageType".equalsIgnoreCase(fieldName)) {
				val = pageType;
			} else if ("moduleCodeRelated".equalsIgnoreCase(fieldName)) {
				val = moduleCodeRelated;
			} else if ("parentId".equalsIgnoreCase(fieldName)) {
				val = String.valueOf(parentId);
			}
			m.appendReplacement(sb, StrUtil.getNullStr(val));
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public static String parseField(FormDAO fdao, String str) {
		FormDb fd = fdao.getFormDb();
		MacroCtlMgr mm = new MacroCtlMgr();
		Pattern p = Pattern.compile(
				"\\{\\$([@A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String fieldName = m.group(1);
			String val = "";
			if ("id".equalsIgnoreCase(fieldName)) {
				val = String.valueOf(fdao.getId());
			}
			else if ("flowId".equalsIgnoreCase(fieldName)) {
				val = String.valueOf(fdao.getFlowId());
			}
			else {
				FormField ff = fdao.getFormField(fieldName);
				if (ff != null) {
					if (ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu != null) {
							val = mu.getIFormMacroCtl().converToHtml(SpringUtil.getRequest(), ff, ff.getValue());
						} else {
							val = ff.convertToHtml();
						}
					} else {
						val = ff.convertToHtml();
					}
				} else {
					val = "字段: " + fieldName + "不存在";
				}
			}
			m.appendReplacement(sb, val);
		}
		m.appendTail(sb);
		return sb.toString();
	}
}

