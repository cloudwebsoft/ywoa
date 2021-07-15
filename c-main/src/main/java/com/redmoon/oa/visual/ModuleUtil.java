package com.redmoon.oa.visual;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.api.IAttachmentCtl;
import com.cloudweb.oa.api.IBasicSelectCtl;
import com.cloudweb.oa.api.IModuleUtil;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
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
	
	public static final String FILTER_CUR_USER = "{$curUser}";
	public static final String FILTER_CUR_USER_DEPT = "{$curUserDept}";
	public static final String FILTER_CUR_USER_ROLE = "{$curUserRole}";
	public static final String FILTER_ADMIN_DEPT = "{$admin.dept}";
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
    	else if (FILTER_CUR_USER_ROLE.equals(preStr)) {
    		return "当前用户的角色";
    	}
    	else if (FILTER_ADMIN_DEPT.equals(preStr)) {
    		return "当前用户管理的部门";
    	}    	
    	else if (FILTER_CUR_DATE.equals(preStr)) {
    		return "当前日期";
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
					tagUrl = request.getContextPath() + servletPath + "/module_list_relate.jsp?mode=subTagRelated&tagName=" + StrUtil.UrlEncode(tagName);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
       	String filter = StrUtil.getNullStr(msd.getString("filter"));
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
							        	ex.printStackTrace();
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
    	        
    	String filter = StrUtil.getNullStr(msd.getString("filter"));

		return parseFilter(request, msd.getString("form_code"), filter);
    }    

    public static String getResultStr(String tableName, String sql, Map fieldsExcluded) {
    	StringBuffer sb = new StringBuffer();
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
			e.printStackTrace();
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
    				e.printStackTrace();
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
    	// 判断许可证是否为src
    	License license = com.redmoon.oa.kernel.License.getInstance();
        if (!license.isSrc()) {
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
            				e.printStackTrace();
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
			Iterator irField = fd.getFields().iterator();
			while (irField.hasNext()) {
				FormField ff = (FormField)irField.next();
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
		            MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		            IFormMacroCtl ictl = mu.getIFormMacroCtl();
		            if (ictl instanceof IBasicSelectCtl) {
		            	String ctlCode = ff.getDefaultValue();
		            	if (StringUtils.isEmpty(ctlCode)) {
		            		ctlCode = ff.getDescription();
						}
		            	
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
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	public static void main(String args[]) {
/*		try {
			importModule();
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				// System.out.println("parseTables sql=" + sql);
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

	    		System.out.println("tableName=" + tableName);
	    		
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
	    		
	    		System.out.println("cols=" + cols);

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
	    		    		System.out.println("flowTypeCodeIndex=" + i);
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
	    			
	    			// System.out.println(recordStr);
	    			
	    			rb = records.indexOf(tagRecordBegin, re);
	    			
	    			String[] ary = StrUtil.split(recordStr, "-\\|-");
	    			
	    			for (int i=0; ary!=null && i<ary.length; i++) {
	    				System.out.println("colAry[" + i + "]=" + colAry[i]);
		    			Integer iType = (Integer)mapType.get(colAry[i]);
		    			if (iType == null) {
							throw new ErrMsgException("字段 " + colAry[i] + " 在表 " + tableName + " 中不存在");
						}
		    			
/*		    			if (isFlowDir) {
		    				System.out.println(ModuleUtil.class + " i=" + i + " colAry[" + i + "]=" + colAry[i] + " flowTypeCodeIndex=" + flowTypeCodeIndex);
		    			}*/
		    			
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
		    			
		    			System.out.println(colAry[i] + "=" + objs[i]);		    			
	    			}
	    			
		    		if (tableName.equalsIgnoreCase("flow_predefined")) {
		    	        int fpid = (int)SequenceManager.nextID(SequenceManager.OA_WORKFLOW_PREDEFINED);
		    			objs[objs.length-1] = fpid;
		    		}	 

					System.out.println(ModuleUtil.class + " sql=" + insertSql);	    			
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
		   	        					ex.printStackTrace();
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
		
        System.out.println("parseSingleForm: vFlowTypeCode.size()=" + vFlowTypeCode.size());

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
					// TODO Auto-generated catch block
					e.printStackTrace();
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
	
		    	System.out.println("basic_ctl code=" + code);
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
	            	ex.printStackTrace();
	            }
	            
				b = ctlsStr.indexOf("[basic_ctl]", e);
			}
		}		
		
		FormDb fd = new FormDb();
		fd = fd.getFormDb(formCode);
        // 解析content，在表form_field中建立相应的域
		FormParser fp = null;
		try {
			fp = new FormParser(fd.getContent());
		} catch (Exception e) {
			throw new ErrMsgException(e.getMessage());
		}
		Vector v = fp.getFields();
		Conn conn = new Conn(Global.getDefaultDB());
		try {
            conn.beginTrans();

            String sql = "";
            Vector vt = fd.generateCreateStr(v);
            Iterator ir = vt.iterator();
            while (ir.hasNext()) {
                sql = (String)ir.next();
                System.out.println("create2: sql=" + sql);
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
        	System.out.println("create:" + StrUtil.trace(e) +
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
				e.printStackTrace();
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

	    		System.out.println("tableName=" + tableName);
	    		
	    		int colsB = tableCont.indexOf(tagColsBegin, nameE);
	    		int colsE = tableCont.indexOf(tagColsEnd, colsB);
	    		
	    		String cols = tableCont.substring(colsB + tagColsBegin.length(), colsE);
	    		String[] colAry = StrUtil.split(cols, "\\|");
	    		// String formCode = FormDb.getCodeByTableName(tableName);
	    		
	    		System.out.println("cols=" + cols);

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
	    		
				System.out.println(ModuleUtil.class + " sql=" + insertSql);

	    		Object[] objs = new Object[colAry.length];
	    		int rb = records.indexOf(tagRecordBegin);
	    		while (rb!=-1) {
	    			int re = records.indexOf(tagRecordEnd, rb);
	    			
	    			String recordStr = records.substring(rb + tagRecordBegin.length(), re);
	    			
	    			// System.out.println(recordStr);
	    			
	    			rb = records.indexOf(tagRecordBegin, re);
	    			
	    			String[] ary = StrUtil.split(recordStr, "-\\|-");
	    			
	    			for (int i=0; ary!=null && i<ary.length; i++) {
		    			Integer iType = (Integer)mapType.get(colAry[i]);	
		    			
		    			// System.out.println(ModuleUtil.class + " " + colAry[i]);
		    			
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
		   	        					ex.printStackTrace();
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
            	val = Global.getRootPath();
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
    public static void doMapOnFlow(HttpServletRequest request, FormDAO fdaoSource, com.redmoon.oa.flow.FormDAO fdaoDest, String maps) throws JSONException, ErrMsgException, SQLException {
        JSONObject json = new JSONObject(maps);
        JSONObject jsonRaw = json;

        JSONArray ary = (JSONArray)json.get("maps");
        String sourceForm = (String)json.get("sourceForm");
        String destForm = (String)json.get("destForm");

        FormDb sourcefd = new FormDb();
        sourcefd = sourcefd.getFormDb(sourceForm);

        FormDb destFd = new FormDb();
        destFd = destFd.getFormDb(destForm);

        // 取出相应的值，置于json字符串中返回
        String ret = "";
        MacroCtlMgr mam = new MacroCtlMgr();
        UserMgr um = new UserMgr();
        MacroCtlMgr mm = new MacroCtlMgr();

        for (int i=0; i<ary.length(); i++) {
            json = (JSONObject)ary.get(i);
            String destF = (String)json.get("destField");
            String sourceF = (String)json.get("sourceField");
            FormField ffDest = destFd.getFormField(destF);
            if (ffDest==null) {
                // System.out.println(getClass() + " destF=" + destF + " 不存在！");
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
                fdaoDest.setFieldValue(destF, fdaoSource.getFieldValue(sourceF));
            }
        }

        fdaoDest.save();

        try {
            ary = (JSONArray)jsonRaw.get("mapsNest");
        }
        catch (JSONException e) {
            ary = null;
            e.printStackTrace();
        }
        String retNest = "";

        if (ary!=null && ary.length() > 0) {
            String sourceFormNest = (String)jsonRaw.get("sourceFormNest");
            FormDb sourcefdNest = new FormDb();
            sourcefdNest = sourcefdNest.getFormDb(sourceFormNest);

            String destFormNest = (String)jsonRaw.get("destFormNest");
            FormDb destfdNest = new FormDb();
            destfdNest = destfdNest.getFormDb(destFormNest);

            // 取出源嵌套表中的数据
            String sql = "select id from " + FormDb.getTableName(sourceFormNest) + " where cws_id=" + StrUtil.sqlstr(String.valueOf(fdaoSource.getId())) + " order by cws_order";

            com.redmoon.oa.visual.FormDAO sourcefdaoNest = new com.redmoon.oa.visual.FormDAO();
            Vector vt = sourcefdaoNest.list(sourceFormNest, sql);
            Iterator ir = vt.iterator();
            while (ir!=null && ir.hasNext()) {
                com.redmoon.oa.visual.FormDAO fdaoSourceNest = (com.redmoon.oa.visual.FormDAO)ir.next();

                com.redmoon.oa.visual.FormDAO fdaoDestNest = new com.redmoon.oa.visual.FormDAO(destfdNest);
                String nestjson = "";
                for (int i=0; i<ary.length(); i++) {
                    json = (JSONObject)ary.get(i);
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
	 * @param flag 0表示操作列 1表示按钮
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
					urlStr = "flow_initiate1_do.jsp?typeCode=" + flowTypeCode + "&op=opLinkFlow&moduleId=" + fdao.getId() + "&moduleCode=" + moduleCode + "&linkName=" + StrUtil.UrlEncode(linkName);
				}
	    		else {
					urlStr = "flow_initiate1_do.jsp?typeCode=" + flowTypeCode + "&op=opBtnFlow&moduleId=" + fdao.getId() + "&moduleCode=" + moduleCode + "&btnId=" + StrUtil.UrlEncode(linkName) + "&pageType=" + pageType;
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
				e.printStackTrace();
			}

    		return urlStr;
    	}
    	
    	return parseUrl(request, url, fdao);
    }

	/**
	 * 从request取得过滤条件中的参数值，组装为url字符串
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
			else if ("vPath".equalsIgnoreCase(str)) {
				val = Global.getRootPath();
			}
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
				e.printStackTrace();
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
				return ret.booleanValue();
			}
			catch (javax.script.ScriptException ex) {
				ex.printStackTrace();
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
    	
    	// System.out.println("ModuleUtil fieldValue=" + fieldValue);
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
				e.printStackTrace();
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

    public static String makeViewJS(FormDb fd, String fieldName, String token, String val, boolean isField, JSONObject json) throws JSONException {
    	// 如果val是表单中的字段
    	if (isField) {
    		val = "o('" + val + "').value";
		}

    	FormField ff = fd.getFormField(fieldName);
		if (ff == null) {
			DebugUtil.e(WorkflowUtil.class, "makeViewJS", "表单：" + fd.getName() + "（" + fd.getCode() + "）中的字段：" + fieldName + " 已不存在");
			return "";
		}

		String str = "";
		String rand = RandomSecquenceCreator.getId(10);
		Iterator ir3 = json.keys();

		str += "var tagName" + rand + "='input';\n";
		str += "if (o('" + fieldName + "')) { tagName" + rand + "=o('" + fieldName + "').tagName; }\n";
		str += "if ($(o('" + fieldName + "')).attr('type')=='radio' || $(o('" + fieldName + "')).attr('type')=='checkbox') {\n";
		str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']:checked\").val()" + token + val + ") {\n";
		while (ir3.hasNext()) {
			String key = (String) ir3.next();
			str += "  var obj=$('#" + key + "')[0];\n";
			str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
			str += "  obj." + json.get(key) + "();\n";
		}
		str += "  }\n";
		str += "}else{\n";
		str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']\").val()" + token + val + ") {\n";
		ir3 = json.keys();
		while (ir3.hasNext()) {
			String key = (String) ir3.next();
			str += "  var obj=$('#" + key + "')[0];\n";
			str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
			str += "  obj." + json.get(key) + "();\n";
		}
		str += "  }\n";
		str += "}\n";

		// 处理事件
		// str += "var evt = 'propertychange';\n"; // 如果有多个条件，会出现多次重复定义
		if (ff.getType().equals(FormField.TYPE_RADIO) || ff.getType().equals(FormField.TYPE_CHECKBOX)) {
			str += "$(tagName" + rand + " + \"[name='" + fieldName + "']\").click(function(e) {\n";
		}
		else {
			str += "$(tagName" + rand + " + \"[name='" + fieldName + "']\").change(function(e) {\n";
		}

		// 判断是否为radio
		str += "if ($(o('" + fieldName + "')).attr('type')=='radio' || $(o('" + fieldName + "')).attr('type')=='checkbox') {\n";
		str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']:checked\").val()" + token + val + ") {\n";
		ir3 = json.keys();
		while (ir3.hasNext()) {
			String key = (String) ir3.next();
			str += "  var obj=$('#" + key + "')[0];\n";
			str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
			str += "  obj." + json.get(key) + "();\n";
		}
		str += "  }\n";
		str += "}else{\n";
		str += "  if ($(tagName" + rand + " + \"[name='" + fieldName + "']\").val()" + token + val + ") {\n";
		ir3 = json.keys();
		while (ir3.hasNext()) {
			String key = (String) ir3.next();
			str += "  var obj=$('#" + key + "')[0];\n";
			str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
			str += "  obj." + json.get(key) + "();\n";
		}
		str += "  }\n";
		str += "}\n";
		str += "});\n";

		return str;
	}

	/**
	 * 取得用来控制是否显示的脚本
	 * @param request HttpServletRequest
	 * @param fd FormDb
	 * @param fdao FormDAO
	 * @param userName String
	 * @param isForReport 是否用于查看时
     * @return
     */
    public static String doGetViewJS(HttpServletRequest request, FormDb fd, IFormDAO fdao, String userName, boolean isForReport) {
        if ("".equals(fd.getViewSetup())) {
			return "";
		}

        // -----5.0版前格式---------
        // <config><views><view><condition>#is_yxxxxm=="是"</condition><display>{"tr_yxxxqjsj":"show"}</display></view><view><condition>#is_yxxxxm=="否"</condition><display>{"tr_yxxxqjsj":"hide"}</display></view><view><condition>#is_yxxxxm==null</condition><display>{"tr_yxxxqjsj":"hide"}</display></view></views></config>
		// -----5.0版后格式---------
		// <config><views><view><condition /><fieldName>shudi</fieldName><operator>=</operator><value>境内</value><display>{"province":"show","city":"show","gb":"hide","jw_city":"hide"}</display></view><view><condition /><fieldName>shudi</fieldName><operator>=</operator><value>境外</value><display>{"province":"hide","city":"hide","gb":"show","jw_city":"show"}</display></view></views></config>

		String scriptOnLoad = "";
		String str = "<script>\n";
		str += "function doViewJS() {\n";
		try {
			Privilege pvg = new Privilege();
			List filedList = new ArrayList();
			Iterator ir1 = fd.getFields().iterator();
			while (ir1.hasNext()) {
				FormField ff = (FormField) ir1.next();
				filedList.add(ff.getName());
			}

			SAXBuilder parser = new SAXBuilder();
            org.jdom.Document doc = parser.build(new InputSource(new StringReader(fd.getViewSetup())));
            Element root = doc.getRootElement();
            Iterator ir2 = root.getChild("views").getChildren().iterator();
            while (ir2.hasNext()) {
                Element el = (Element)ir2.next();
                String condition = el.getChildText("condition").trim();
                if (!"".equals(condition)) {
					// 如果以#开头且不在流程查看时，对前台事件进行处理
					if (condition.startsWith("#")) {
						// 非流程查看时
						if (!isForReport) {
							String token = "==";
							if (condition.indexOf(">=")!=-1) {
								token = ">=";
							} else if (condition.indexOf("<=")!=-1) {
								token = "<=";
							} else if (condition.indexOf("!=")!=-1) {
								token = "!=";
							} else if (condition.indexOf(">")!=-1) {
								token = ">";
							} else if (condition.indexOf("<")!=-1) {
								token = "<";
							}
							String[] ary = StrUtil.split(condition, token);
							if (ary.length==2) {
								String fieldName = ary[0].substring(1); // 去掉#号
								ary[1] = ary[1].replaceAll("\"", "'");
								String val = ary[1];

								String display = el.getChildText("display");
								JSONObject json = new JSONObject(display);
								str += makeViewJS(fd, fieldName, token, val, false, json);
							}
							else {
								LogUtil.getLog(WorkflowUtil.class).error("condition=" + condition + ", 格式错误！");
							}
						}
					}
					else {
						// 如果不以#开头，则通过BranchMatcher.match在服务器端判断条件是否成立，条件表达式同分支线上的脚本表达式
						// 如果条件为空或者条件为真
						// 當添加時，fdao爲null
						if (condition.equals("") || (fdao!=null &&
								BranchMatcher.match(condition, fd, fdao, userName))) {
							String display = el.getChildText("display");
							JSONObject json = new JSONObject(display);
							Iterator ir3 = json.keys();
							while (ir3.hasNext()) {
								String key = (String) ir3.next();
								// str += "$('#" + key + "')." + json.get(key) + "();\n";
								str += "  var obj=$('#" + key + "')[0];\n";
								str += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
								str += "  obj." + json.get(key) + "();\n";
							}
						}
					}
				} else {
					// 5.0版后
					boolean formFlag = true;

					String fieldName = el.getChildText("fieldName");
					if (fieldName==null) {
					    // 可能为空的<view/>
					    continue;
                    }
					String token = el.getChildText("operator");

					token = token.replaceAll("&lt;", "<");
					token = token.replaceAll("&gt;", ">");
					if (token.equals("=")) {
						token = "==";
					}
					else if (token.equals("<>")) {
						token = "!=";
					}

					String val = el.getChildText("value");

					formFlag = filedList.contains(fieldName);
					if (!formFlag && !"cws_id".equals(fieldName) && !"cws_status".equals(fieldName) && "!cws_flag".equals(fieldName)) {
						break;
					}

					boolean isField = false;
					if (val.equals(FILTER_CUR_USER)) {
						val = pvg.getUser(request);
					} else if (val.equals(FILTER_CUR_DATE)) {
						val = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
					} else if (val.startsWith("{$")) {
						Pattern p = Pattern.compile(
								"\\{\\$([@A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
								Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
						Matcher m = p.matcher(val);
						while (m.find()) {
							String fName = m.group(1);
							if (fName.startsWith("request.")) {
								String key = fName.substring("request.".length());
								val = ParamUtil.get(request, key);
							}
							else {
								// 判断fName是否为表单中的字段
								if (fd.getFormField(fName)!=null) {
									val = fName;
									isField = true;
								}
							}
						}
					}

					String display = el.getChildText("display");
					JSONObject json = new JSONObject(display);
					FormField ff = fd.getFormField(fieldName);
					// 如果是字符串型，则需加上双引号
					if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR || ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
						// 如当radio默认未选择时，其值为null，故 JS 判别时无需加双引号
						if (!"null".equals(val) && !isField) {
							val = "\"" + val + "\"";
						}
					}

					// ------------为前端生成 JS----------------
					String pageType = (String)request.getAttribute("pageType");
					// 如果不是在flow_modify.jsp页面，则生成 JS
					if (!"show".equals(pageType)) {
						str += makeViewJS(fd, fieldName, token, val, isField, json);
					}

					// 为加载页面时根据数据库中记录值生成JS，scriptOnLoad不能放在doViewJS中，否则当表单域选择宏控件重新选择后，调用doViewJS时，scriptOnLoad脚本会有冲突
					if (isField) {
						val = "{$" + val + "}";
					}
					String condStr = "{$" + fieldName + "}" + token + val;
					// 判断是否为字符串型，如果是则需要变成equals
					if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR || ff.getFieldType() == FormField.FIELD_TYPE_TEXT) {
						if (!"null".equals(val)) {
							if ("==".equals(token)) {
								condStr = val + ".equals({$" + fieldName + "})";
							} else if ("<>".equals(token)) {
								condStr = "!" + val + ".equals({$" + fieldName + "})";
							}
						}
						else {
							if ("==".equals(token)) {
								condStr = val + "=={$" + fieldName + "} || \"\".equals({$" + fieldName + "})";
							} else if ("<>".equals(token)) {
								// condStr = val + "!={$" + fieldName + "} || \"\".equals({$" + fieldName + "})";
								condStr = val + "!={$" + fieldName + "} && !\"\".equals({$" + fieldName + "})";
							}
						}
					}
					else if (ff.getType().equals(FormField.TYPE_CHECKBOX)){
						if ("<>".equals(token)) {
							condStr = val + "!={$" + fieldName + "}";
						}
					}
					else if (ff.getType().equals(FormField.TYPE_DATE)) {
						Date d;
						if ("current".equals(val.toLowerCase())) {
							d = DateUtil.parse(DateUtil.format(new Date(), "yyyy-MM-dd"), "yyyy-MM-dd");
						}
						else {
							d = DateUtil.parse(val, "yyyy-MM-dd");
							if (d==null) {
								throw new ErrMsgException(val + " 格式非法");
							}
						}
						String t = String.valueOf(d.getTime());
						condStr = "cn.js.fan.util.DateUtil.parse({$" + fieldName + "}, \"yyyy-MM-dd\")!=null ? cn.js.fan.util.DateUtil.parse({$" + fieldName + "}, \"yyyy-MM-dd\").getTime()" + token + t + "L : false";
					}
					else if (ff.getType().equals(FormField.TYPE_DATE_TIME)) {
						Date d;
						if ("current".equals(val.toLowerCase())) {
							d = new Date();
						}
						else {
							d = DateUtil.parse(val, "yyyy-MM-dd HH:mm:ss");
							if (d==null) {
								throw new ErrMsgException(val + " 格式非法");
							}
						}
						String t = String.valueOf(d.getTime());
						condStr = "cn.js.fan.util.DateUtil.parse({$" + fieldName + "}, \"yyyy-MM-dd HH:mm:ss\")!=null ? cn.js.fan.util.DateUtil.parse({$" + fieldName + "}, \"yyyy-MM-dd HH:mm:ss\").getTime()" + token + t + "L : false";
					}

					long t = System.currentTimeMillis();
					if (fdao!=null && BranchMatcher.match(condStr, fd, fdao, userName)) {
						// DebugUtil.i(ModuleUtil.class, "doGetViewJS", "condStr=" + condStr);
						Iterator ir3 = json.keys();
						while (ir3.hasNext()) {
							String key = (String) ir3.next();
							scriptOnLoad += "  var obj=$('#" + key + "')[0];\n";
							scriptOnLoad += "  if (!obj) obj = o('" + key + "'); obj=$(obj);\n";
							scriptOnLoad += "  obj." + json.get(key) + "();\n";
						}
					}
					if (Global.getInstance().isDebug()) {
						long t2 = System.currentTimeMillis();
						// DebugUtil.i(ModuleUtil.class, condStr, " match " + (t2-t) + " ms isMatched=" + isMatched);
					}
				}
            }
        } catch (IOException | JDOMException | JSONException ex) {
            ex.printStackTrace();
        } catch (ErrMsgException ex) {
			ex.printStackTrace();
            LogUtil.getLog(WorkflowUtil.class).trace(ex);
        }
		str += "}\n";
		str += "doViewJS();\n";
		str += scriptOnLoad + "\n";
		str += "</script>\n";
        return str;
    }

	/**
	 * 取得检验规则生成的脚本
	 * @param request HttpServletRequest
	 * @param fdao FormDAO
	 * @param userName String
	 * @return
	 */
	public static boolean doCheckSetup(HttpServletRequest request, String userName, IFormDAO fdao, FileUpload fu) throws ErrMsgException {
		if ("".equals(fdao.getFormDb().getCheckSetup())) {
			return true;
		}
		// <config><rules><rule><if>[{"field":"rq", "operator":"&gt;=", "val":""}]</if><then>[{"field":"rq", "operator":"&lt;&gt;", "val":""}]</then></rule></rules></config>
		try {
			List<String> filedList = new ArrayList<>();
			Iterator<FormField> ir1 = fdao.getFormDb().getFields().iterator();
			while (ir1.hasNext()) {
				FormField ff = ir1.next();
				filedList.add(ff.getName());
			}

			StringBuffer sb = new StringBuffer();

			SAXBuilder parser = new SAXBuilder();
			org.jdom.Document doc = parser.build(new InputSource(new StringReader(fdao.getFormDb().getCheckSetup())));
			Element root = doc.getRootElement();
			Iterator ir2 = root.getChild("rules").getChildren().iterator();
			while (ir2.hasNext()) {
				Element el = (Element)ir2.next();

				boolean re = true;
				String desc = el.getChildText("desc");
				String strIf = el.getChildText("if");
				JSONArray aryIf = new JSONArray(strIf);
				re = evalCheckSetupRule(request, userName, aryIf, fdao, filedList, fu);

				if (re) {
					String then = el.getChildText("then");
					JSONArray aryThen = new JSONArray(then);
					re = evalCheckSetupRule(request, userName, aryThen, fdao, filedList, fu);

					if (!re) {
						StrUtil.concat(sb, "\r\n", desc);
					}
				}
			}
			if (sb.length()>0) {
				throw new ErrMsgException(sb.toString());
			}
		} catch (IOException | JDOMException | JSONException ex) {
			ex.printStackTrace();
		}
		return true;
	}

	public static boolean evalCheckSetupRule(HttpServletRequest request, String userName, JSONArray ary, IFormDAO fdao, List filedList, FileUpload fu) throws JSONException, ErrMsgException {
		boolean res = false;
		for (int i=0; i<ary.length(); i++) {
			JSONObject json = ary.getJSONObject(i);

			boolean formFlag = true;
			String condStrOrig = "";

			String field = json.getString("field");
			String operator = json.getString("operator");
			operator = operator.replaceAll("&lt;", "<");
			operator = operator.replaceAll("&gt;", ">");
			String logical;
			if (json.has("logical")) {
				logical = json.getString("logical");
			}
			else {
				logical = "and"; // 向下兼容
			}

			String val = json.getString("val");
			formFlag = filedList.contains(field);
			if (!formFlag && !"cws_id".equals(field) && !"cws_status".equals(field) && "!cws_flag".equals(field)) {
				if (!field.startsWith(CHECKBOX_GROUP_PREFIX)) {
					DebugUtil.e(ModuleUtil.class, "evalCheckSetupRule", "字段：" + field + "不存在");
					break;
				}
			}

			if (val.equals(FILTER_CUR_USER)) {
				val = userName;
			} else if (val.equals(FILTER_CUR_DATE)) {
				val = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
			} else if (val.startsWith("{$")) {
				// 前为utf8中文范围，后为gb2312中文范围
				Pattern p = Pattern.compile(
						"\\{\\$([@A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}",
						Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(val);
				while (m.find()) {
					String fName = m.group(1);
					if (fName.startsWith("request.")) {
						String key = fName.substring("request.".length());
						val = ParamUtil.get(request, key);
					}
				}
			}

			boolean isCheckboxGroup = false;
			List<String> listChkVal = new ArrayList<String>();
			if (field.startsWith(CHECKBOX_GROUP_PREFIX)) {
				String groupName = field.substring(CHECKBOX_GROUP_PREFIX.length());
				isCheckboxGroup = true;
				boolean isFound = false;
				// 从表单中找到对应的字段
				Iterator ir = fdao.getFormDb().getFields().iterator();
				while (ir.hasNext()) {
					FormField ff = (FormField)ir.next();
					if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
						String desc = ff.getDescription();
						if (desc == null) {
							desc = "";
							DebugUtil.e(ModuleUtil.class, "evalCheckSetupRule", fdao.getFormDb().getName() + " 中字段 " + ff.getName() + " 描述为null");
						}
						if (desc.equals(groupName)) {
							isFound = true;
							String valChk = fu.getFieldValue(ff.getName());
							if (valChk!=null && !"".equals(valChk)) {
								listChkVal.add(valChk);
							}
						}
					}
				}
				if (!isFound) {
					DebugUtil.e(ModuleUtil.class, "evalCheckSetupRule", "复选框组：" + groupName + "不存在");
					continue;
				}
			}

			String condStr = "";
			if (isCheckboxGroup) {
				val = "\"" + val + "\"";
				String values;
				if (listChkVal.size()>0) {
					values = "\"" + StringUtils.join(listChkVal, ",") + "\"";
				}
				else {
					values = "\"\"";
				}
				if ("=".equals(operator)) {
					condStr = values + "==" + val;
				} else if ("<>".equals(operator)) {
					condStr = values + "!=" + val;
				}
				else {
					condStr = values + operator + val;
				}
				condStrOrig = condStr;
			}
			else {
				FormField ff = fdao.getFormDb().getFormField(field);
				if (ff == null) {
					DebugUtil.e(ModuleUtil.class, "evalCheckSetupRule", field + " 字段不存在");
					continue;
				}
				// 如果是字符串型，则需加上双引号
				if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR || ff.getFieldType() == FormField.FIELD_TYPE_TEXT || ff.getType().equals(FormField.TYPE_CHECKBOX)) {
					val = "\"" + val + "\"";
				}

				// 服务器端验证
				condStr = "{$" + field + "}" + operator + val;
				condStrOrig = condStr;

				// 判断是否为字符串型，如果是则需要变成equals
				if (ff.getFieldType() == FormField.FIELD_TYPE_VARCHAR || ff.getFieldType() == FormField.FIELD_TYPE_TEXT || ff.getType().equals(FormField.TYPE_CHECKBOX)) {
					// 加上双引号，否则javascript会认为其为变量
					if ("=".equals(operator)) {
						condStr = val + "==\"{$" + field + "}\"";
					} else if ("<>".equals(operator)) {
						condStr = val + "!=\"{$" + field + "}\"";
					}
				} else if (ff.getFieldType() == FormField.FIELD_TYPE_DATE || ff.getFieldType() == FormField.FIELD_TYPE_DATETIME) {
					if ("<>".equals(operator)) {
						// 判断是否不为空
						// new Date("") 会报： invalid date（safari） 或为 null
						condStr = "\"" + val + "\"!=\"{$" + field + "}\"";
					}
					else {
						condStr = "(new Date(" + "\"{$" + field + "}\"" + ".replace(/-/g,'\\/')))" + operator + "(new Date(\"" + val + "\".replace(/-/g,'\\/')))";
					}
				}

				MacroCtlMgr mm = new MacroCtlMgr();
				Privilege pvg = new Privilege();
				Pattern p = Pattern.compile(
						"\\{\\$([@A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff\\.]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
						Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(condStr);
				StringBuffer sb = new StringBuffer();
				while (m.find()) {
					String fieldName = m.group(1);

					String value = "";
					if (fieldName.startsWith("request.")) {
						String key = fieldName.substring("request.".length());
						value = ParamUtil.get(request, key);
					} else if ("curUser".equalsIgnoreCase(fieldName)) {
						value = StrUtil.sqlstr(pvg.getUser(request));
					} else if ("curDate".equalsIgnoreCase(fieldName)) {
						value = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
					} else {
						value = StrUtil.getNullStr(fu.getFieldValue(fieldName));
						// 如果fu中获取的值为空，则从fdao中获取，以便于字段不可写时也能验证
						if ("".equals(value)) {
							// 如果是文件宏控件，则从数据库再取一次值，以便于判断是否为空
							FormField ffCond = fdao.getFormField(fieldName);
							if (ffCond!=null && FormField.TYPE_MACRO.equals(ffCond.getType())) {
								MacroCtlUnit mu = mm.getMacroCtlUnit(ffCond.getMacroType());
								IFormMacroCtl imc = mu.getIFormMacroCtl();
								if (imc instanceof IAttachmentCtl) {
									value = StrUtil.getNullStr(fdao.getFieldValue(fieldName));
								}
							}
						}
					}
					m.appendReplacement(sb, value);
				}
				m.appendTail(sb);
				condStr = sb.toString();
			}

			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("javascript");
			try {
				Boolean ret = (Boolean)engine.eval(condStr);
				boolean re = ret;
				DebugUtil.i(ModuleUtil.class, "evalCheckSetupRule", condStrOrig + " " +condStr + " re=" + re);

				if (i > 0) {
					if ("and".equals(logical)) {
						if (!re) {
							return false;
						}
						res = res && re;
					}
					else if ("or".equals(logical)){
						res = res || re;
					}
				}
				else {
					res = re;
				}
			}
			catch (ScriptException ex) {
				// ex.printStackTrace();
				DebugUtil.e(ModuleUtil.class, "evalCheckSetupRule", condStr);
				DebugUtil.e(ModuleUtil.class, "evalCheckSetupRule", fdao.getFormDb().getName() + " " + fdao.getFormDb().getCode() + "ID:" + fdao.getId());
				DebugUtil.e(ModuleUtil.class, "evalCheckSetupRule", StrUtil.trace(ex));
			}
		}
		return res;
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
	public static void exportXml(HttpServletRequest request, OutputStream os, String[] fields, String[] fieldsTitle, FormDb fd, String sql, long templateId) throws JSONException {
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

			JSONArray arr = new JSONArray(columns);
			StringBuffer colsSb = new StringBuffer();
			for (int i = 0; i < arr.length(); i++) {
				JSONObject json = arr.getJSONObject(i);
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
			e.printStackTrace();
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
				if (fieldName.equals("serialNoForExp")) {
					title = "序号";
				}
				else if (fieldName.startsWith("main:")) {
					String[] mainToSub = StrUtil.split(fieldName, ":");
					if (mainToSub != null && mainToSub.length == 3) {
						FormDb ntfd = new FormDb();
						ntfd = ntfd.getFormDb(mainToSub[1]);
						FormField ff = ntfd.getFormField(mainToSub[2]);
						if (ff != null) {
							title = ff.getTitle();
						}
						else {
							title = fieldName + "不存在";
						}
					} else {
						title = fieldName;
					}
				}
				else if (fieldName.startsWith("other:")) {
					String[] otherFields = StrUtil.split(fieldName, ":");
					if (otherFields.length == 5) {
						FormDb otherFormDb = new FormDb(otherFields[2]);
						title = otherFormDb.getFieldTitle(otherFields[4]);
					}
				}
				else if ("cws_creator".equals(fieldName)) {
					title = "创建者";
				}
				else if ("ID".equalsIgnoreCase(fieldName) || "CWS_MID".equalsIgnoreCase(fieldName)) {
					title = "ID";
				}
				else if ("cws_status".equals(fieldName)) {
					title = "状态";
				}
				else if ("cws_flag".equals(fieldName)) {
					title = "冲抵状态";
				}
				else if ("flowId".equalsIgnoreCase(fieldName)) {
					title = "流程号";
				}
				else if ("flow_begin_date".equalsIgnoreCase(fieldName)) {
					title = "流程开始时间";
				}
				else if ("flow_end_date".equalsIgnoreCase(fieldName)) {
					title = "流程结束时间";
				}
				else if ("cws_id".equals(fieldName)) {
					title = "关联ID";
				}
				else {
					title = fd.getFieldTitle(fieldName);
				}

				if (!"#".equals(fieldTitle)) {
					title = fieldTitle;
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

					Iterator irFdao = lr.getResult().iterator();
					while (irFdao.hasNext()) {
						fdao = (com.redmoon.oa.visual.FormDAO) irFdao.next();

						long tDebugRow = System.currentTimeMillis();

						n++;
						sb.append("<Row>");

						int logType = StrUtil.toInt(fdao.getFieldValue("log_type"), FormDAOLog.LOG_TYPE_CREATE);

						// 置SQL宏控件中需要用到的fdao
						RequestUtil.setFormDAO(request, fdao);

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
											ResultRecord rr = (ResultRecord) riSub.next();
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
										e.printStackTrace();
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
								fieldValue = StrUtil.getNullStr(fdao.getCwsId());
							}
							else {
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
						if (Global.getInstance().isDebug()) {
							DebugUtil.i(ModuleUtil.class, "export", "one record: " + (System.currentTimeMillis() - tDebugRow) + " ms");
						}
					}
				} catch (ErrMsgException e) {
					e.printStackTrace();
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

			DebugUtil.i(ModuleUtil.class, "export", "共" + total + " 条 耗时：" + (System.currentTimeMillis()-tDebug) + " ms");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
		}
		return btnDefaultName;
	}
}

