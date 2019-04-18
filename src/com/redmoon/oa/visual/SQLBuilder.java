package com.redmoon.oa.visual;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.base.IFormMacroCtl;
import com.redmoon.oa.basic.SelectDb;
import com.redmoon.oa.basic.SelectMgr;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormDb;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.sys.DebugUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormMgr;
import com.redmoon.oa.flow.macroctl.BasicSelectCtl;
import com.redmoon.oa.flow.macroctl.CurrentUserCtl;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.macroctl.ModuleFieldSelectCtl;
import com.redmoon.oa.flow.macroctl.UserSelectCtl;
import com.redmoon.oa.flow.macroctl.UserSelectWinCtl;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.db.SQLFilter;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.visual.SQLBuilder.CondParam;

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
public class SQLBuilder {
	/**
	 * 模糊查询，包含
	 */
	public static String COND_TYPE_FUZZY = "0";
	
	/**
	 * 准确查询，等于
	 */
	public static String COND_TYPE_NORMAL = "1";
	
	/**
	 * 两个模块之间没有关联关系
	 */
	public static String IS_NOT_RELATED = "isNotRelated";
	
	/**
	 * 不限，不含临时记录
	 */
	public static int CWS_STATUS_NOT_LIMITED = 10000;
	/**
	 * 为空
	 */
	public static String IS_EMPTY = "=空";
	/**
	 * 不为空
	 */
	public static String IS_NOT_EMPTY = "<>空";
	
    public SQLBuilder() {
    }

    public static String[] getModuleListSqlAndUrlStr(HttpServletRequest request,
            FormDb fd, String op, String orderBy, String sort) throws ErrMsgException {
        return getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort, "", "");
    }

    /**
     * 获取关联模块列表sql语句
     * @param request HttpServletRequest
     * @param fd FormDb
     * @param op String
     * @param orderBy String
     * @param sort String
     * @param userName String
     * @param fieldUserName String
     * @return String[]
     */
    public static String[] getModuleListSqlAndUrlStr(HttpServletRequest request,
            FormDb fd, String op, String orderBy, String sort, String userName,
            String fieldUserName) throws ErrMsgException {
        Privilege privilege = new Privilege();
        String sql = "select distinct t1.id from " + fd.getTableNameByForm() + " t1";
        boolean isEncrypted = false;
        String urlStr = "";
    	ModuleSetupDb msd = (ModuleSetupDb)request.getAttribute(ModuleUtil.MODULE_SETUP);	        	

    	String cond = "";

        String query = ParamUtil.get(request, "query");
        if (!query.equals("")) {
            sql = query;
            // 有可能是加密过的，如customer_list.jsp中查询后导出时
            if (sql.toLowerCase().indexOf("select ")==-1) {
            	isEncrypted = true;
            	com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();            	
            	sql = cn.js.fan.security.ThreeDesUtil.decrypthexstr(cfg.getKey(), sql);            	
            }            
        }
        else {
            if (op.equals("search")) {
        		Object[] aryCondAndUrlStr = fitCondAndUrlStr(request, msd, fd);
        		cond = (String)aryCondAndUrlStr[0];
        		urlStr = (String)aryCondAndUrlStr[1];
        		Map<String, CondParam> fieldsRelatedMap = (HashMap<String, CondParam>)aryCondAndUrlStr[2];

        		// 如果fieldsRelatedMap不为空，则说明有其它的main:或other:或sub:相关表
        		if (fieldsRelatedMap.size()>0) {
        			// 组装表名，并记录序号
            		Map<String, String> tableMap = new HashMap<String, String>();
        			Iterator<String> irMap = fieldsRelatedMap.keySet().iterator();
        			while (irMap.hasNext()) {
        				String key = irMap.next();
        				CondParam cp = fieldsRelatedMap.get(key);
        				if (!tableMap.containsKey(cp.formCode)) { // 防止表名重复
        					// 如果条件字段所传的参数不为空
        					if (!cp.isCondFieldBlank) {
		        				sql += ", form_table_" + cp.formCode + " t" + cp.order;
		        				tableMap.put(cp.formCode, String.valueOf(cp.order));
        					}
        				}
        			}
        		}                
        		
                String userUnitCode = privilege.getUserUnitCode(request);
                String unitCode = ParamUtil.get(request, "unitCode"); // 来自于module_list.jsp中的单位下拉菜单
                if ("-1".equals(unitCode)) {
                	/*
                	// 如果是不限，则判断是否为总部人员（因为有时组织架构上总部人员自身也是一个子单位，其unitCode并不是root）
                	if (!userUnitCode.equals(DeptDb.ROOTCODE)) {
        	        	throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));        		
                	}
                	*/
                }
                else {
                	/*
        	        if (!"".equals(unitCode) && !privilege.canUserAdminUnit(request, unitCode)) {
        	        	throw new ErrMsgException(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid"));
        	        }*/
                }
                
                if (!userName.equals("")) {
                    if (sql.indexOf(" where ") == -1)
                        sql += " where t1." + fieldUserName + "=" +
                                StrUtil.sqlstr(privilege.getUser(request));
                    else
                        sql += " and " + "t1." + fieldUserName + "=" + StrUtil.sqlstr(privilege.getUser(request));             	
                }
                else {
                	if (!"-1".equals(unitCode)) {
                		if ("".equals(unitCode)) {
                			// 如果request中为空，则说明条件中没有unitCode，根据单位的配置过滤
            	        	String msdUnitCode = msd.getString("unit_code");  
            	        	// 本单位
            	        	if (msdUnitCode.equals("0")) {
            	        		if (sql.indexOf(" where ") == -1) {
            	        			sql += " where t1.unit_code=" + StrUtil.sqlstr(userUnitCode);
            	        		}
            	        		else {
            	        			sql += " and t1.unit_code=" + StrUtil.sqlstr(userUnitCode);            	        			
            	        		}
            	        	}
                		}
                		else {
        	        		if (sql.indexOf(" where ") == -1) {             			
        	        			sql += " where t1.unit_code=" + StrUtil.sqlstr(unitCode);        	
        	        		}
        	        		else {
        	        			sql += " and t1.unit_code=" + StrUtil.sqlstr(unitCode);        	        	        			
        	        		}
                		}
                	}
                }             		
                
                if (!cond.equals("")) {
                    if (sql.indexOf(" where ") == -1)
                        sql = sql + " where " + cond;
                    else
                        sql += " and " + cond;
                }
            }
        }
        
        // System.out.println(SQLBuilder.class.getName() + " " + sql);
        
        boolean isFilterSelectSQL = false;
        // 如果未加密
        if (!isEncrypted) {
	        sql = sql.toLowerCase();
	        
	        String[] filter = null;
	        
	        // 如果是拉单module_list_nest_sel，则使用拉单中配置的过滤条件，否则使用模块的过滤条件，即通过requests的attribute中的msd获得过滤条件
	        String nestFilter = (String)request.getAttribute(ModuleUtil.NEST_SHEET_FILTER);
	        if (nestFilter!=null && !"".equals(nestFilter)) {
		        if (nestFilter.equals("none"))
		        	nestFilter = "";
		        
		        filter = ModuleUtil.parseFilter(request, fd.getCode(), nestFilter);
	        }
	        else {
	        	filter = ModuleUtil.parseFilter(request);
	        }
	        
	        if (filter!=null) {
		        if (!"".equals(filter[0]) && filter[0]!=null) {
		        	String filterStr = filter[0].toLowerCase();
		        	// 如果filter中以select 打头，则说明是个完整的sql语句 
		        	if (filterStr.startsWith("select ")) {
		        		isFilterSelectSQL = true;
		        		sql = filterStr;

						// 为完整的sql语句加入search查询条件
		        		// 判断语句中是否有 t1.，如果是，则说明sql是合乎规范的，可以加cond条件
						if (sql.indexOf(" t1.") != -1) {
							int p = sql.lastIndexOf(" order by ");
							String orderByStr = "";
							if (p!=-1) {
								sql = sql.substring(0, p);
								orderByStr = sql.substring(p);
							}

							if (!cond.equals("")) {
								if (sql.indexOf(" where ") == -1)
									sql = sql + " where " + cond;
								else
									sql += " and " + cond;
							}

							if (!"".equals(orderByStr)) {
								sql += orderByStr;
							}
						}
		        	}
		        	else {		        	
			        	// 如果过滤条件中含有where
			        	if (sql.indexOf(" where ")!=-1) {
			        		// 如果过滤条件中只含有order by
			        		if (filterStr.trim().startsWith("order by ")) {
			        			sql += " " + filterStr;
			        		}
			        		else {
			        			sql += " and " + filterStr;
			        		}
			        	}
			        	else {
			        		if (filterStr.trim().startsWith("order by ")) {
			        			sql += " " + filterStr;
			        		} 
			        		else {
			        			sql += " where " + filter[0];
			        		}
			        	}
		        	}
		        }
	        }
	        
	        // 如果filter中不是完整的SQL语句，且sql语句中未显示指定cws_status，则根据配置附加cws_status条件
	        if (!isFilterSelectSQL && sql.indexOf("cws_status")==-1) {
	        	int cwsStatus = msd.getInt("cws_status");
	        	int p = sql.toLowerCase().lastIndexOf(" where ");
	        	// 如果where已存在，则说明sql语句中必然带有条件
		    	if (p!=-1) {
		    		// 检查是不是有order by，如果有则应写入在order by之前
		    		int q = sql.lastIndexOf(" order by ");
		    		if (q!=-1) {
			    		String sqlLeft = sql.substring(0, q);
			    		String sqlRight = sql.substring(q);
			    		if (cwsStatus!=CWS_STATUS_NOT_LIMITED) {
			    			sql = sqlLeft + " and t1.cws_status=" + cwsStatus + sqlRight;
			    		}
			    		else {			    			
			    			sql = sqlLeft + " and t1.cws_status<>" + com.redmoon.oa.flow.FormDAO.STATUS_TEMP + " and t1.cws_status<>" + com.redmoon.oa.flow.FormDAO.STATUS_DELETED + sqlRight;
			    		}
		    		}
		    		else {
		    			if (cwsStatus!=CWS_STATUS_NOT_LIMITED) {
		    				sql += " and t1.cws_status=" + cwsStatus;
		    			}
		    		}
		    	}
		    	else {
		    		// 检查是不是有order by，如果有则应写入在order by之前
		    		int q = sql.lastIndexOf("order by ");
		    		if (q!=-1) {
			    		String sqlLeft = sql.substring(0, q);
			    		String sqlRight = sql.substring(q);
			    		if (cwsStatus!=CWS_STATUS_NOT_LIMITED) {
			    			sql = sqlLeft + " where t1.cws_status=" + cwsStatus + " " + sqlRight;
			    		}
			    		else {
			    			sql = sqlLeft + " where t1.cws_status<>" + com.redmoon.oa.flow.FormDAO.STATUS_TEMP + " and t1.cws_status<>" + com.redmoon.oa.flow.FormDAO.STATUS_DELETED + " " + sqlRight;
			    		}
		    		}
		    		else {
			    		if (cwsStatus!=CWS_STATUS_NOT_LIMITED) {
			    			sql += " where t1.cws_status=" + cwsStatus;
			    		}
			    		else {
			    			sql += " where t1.cws_status<>" + com.redmoon.oa.flow.FormDAO.STATUS_TEMP + " and t1.cws_status<>" + com.redmoon.oa.flow.FormDAO.STATUS_DELETED;
			    		}
		    		}		    		
		    	}
	        }
	        
	        // 如果sql语句中含有order by，则说明sql中脚本条件中含有order by，如： select field from table order by dt asc;
	        int p = sql.lastIndexOf("order by ");
	        if (p!=-1) {
	        	// 脚本条件无排序设置，所以orderBy默认值为id
	        	if (orderBy.equals("id")) {
		        	// 如果orderBy为id，说明是默认排序，即没有点击表头指定orderBy，则仍按sql语句中的排序
	        	}
	        	else {
		        	// 如果不是id，则说明点击了表头进行排序
	        		sql = sql.substring(0, p);
		        	sql += " order by t1." + orderBy + " " + sort;
	        	}
	        }
	        else {
	        	sql += " order by t1." + orderBy + " " + sort;
	        }
        }

        String[] ary = new String[2];
        ary[0] = sql;
        ary[1] = urlStr;

        return ary;
    }
    
    /**
     * 装配条件及url参数，生成fieldsRelatedMap，以记录相关联的其它表的相关信息
     * @param request
	 * @param msd
	 * @param fd
     * @return
     */
    public static Object[] fitCondAndUrlStr(HttpServletRequest request, ModuleSetupDb msd, FormDb fd) {
    	Object[] aryCondAndUrlStr = new Object[3];
        String urlStr = "";
        MacroCtlMgr mm = new MacroCtlMgr();   
        FormMgr fm = new FormMgr();
    	
    	// 拼合所有的字段
    	Vector<FormField> vt = new Vector<FormField>();

        String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
        String[] btnNames = StrUtil.split(btnName, ",");                
        String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
        String[] btnScripts = StrUtil.split(btnScript, "#");
        // fieldsRelatedMap的key为字段名
        Map<String, CondParam> fieldsRelatedMap = new HashMap<String, CondParam>();
        
        String[] fields = null;
    	if (btnNames!=null) {
    		int len = btnNames.length;
    		for (int i=0; i<len; i++) {
    		  if (btnScripts[i].startsWith("{")) {
    			JSONObject json;
				try {
					json = new JSONObject(btnScripts[i]);
        			if (((String)json.get("btnType")).equals("queryFields")) {
        				String condFields = (String)json.get("fields");
        				fields = StrUtil.split(condFields, ",");
        			}							
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		  }
    		}
    	}
        
    	int fieldsLen = 0;
    	if (fields!=null) {
    		fieldsLen = fields.length;
    	}
		for (int n=0; n<fieldsLen; n++) {
			String field = fields[n];	
			FormField ff = null;
			if (field.startsWith("main:")) { // 关联的主表
				// main:personbasic:whcd
				String[] ary = StrUtil.split(field, ":");			
				if (ary.length==3) {
					FormDb mainFormDb = fm.getFormDb(ary[1]);
					ff = mainFormDb.getFormField(ary[2]);
					if (ff!=null) {
						fieldsRelatedMap.put(fields[n], new CondParam(ary[1], ary[2], "main"));
						try {
							ff = (FormField)ff.clone();
						} catch (CloneNotSupportedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// 仍置为main:开头，以便于取得request中传来的条件字段的参数            						        						
						ff.setName(fields[n]);
						vt.addElement(ff);
					}
				}
			}
			else if (field.startsWith("sub:")) { // 关联的子表
				String[] ary = StrUtil.split(field, ":");			
				if (ary.length==3) {
					FormDb mainFormDb = fm.getFormDb(ary[1]);
					ff = mainFormDb.getFormField(ary[2]);
					if (ff!=null) {
						fieldsRelatedMap.put(fields[n], new CondParam(ary[1], ary[2], "sub"));
						try {
							ff = (FormField)ff.clone();
						} catch (CloneNotSupportedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// 置为sub:开头，以便于取得request中传来的条件字段的参数        						
						ff.setName(fields[n]);
						vt.addElement(ff);
					}
				}        				
			}
			else if (field.startsWith("other:")) { // 映射的字段，多重映射不支持
				String[] ary = StrUtil.split(field, ":");
				if (ary.length<5 || ary.length>=8) {
					LogUtil.getLog(SQLBuilder.class).error("fitCondAndUrlStr:" + field + " 格式非法！");
					continue;
				}
				else {        					
					// other:cws_id:personbasic:id:realname
					FormDb otherFormDb = fm.getFormDb(ary[2]);
					if (ary.length>=5) {
						ff = otherFormDb.getFormField(ary[4]);
    					if (ff!=null) {
    						fieldsRelatedMap.put(fields[n], new CondParam(ary[2], ary[4], "other", ary[1], ary[3]));        						
    						try {
								ff = (FormField)ff.clone();
							} catch (CloneNotSupportedException e) {
								e.printStackTrace();
							}
    						// 置为other:开头，以便于取得request中传来的条件字段的参数            						
    						ff.setName(fields[n]);            						
    						vt.addElement(ff);
    					}        						
					}
					// 不考虑多重映射
					if (ary.length>=8) {
					}
				}
			}	
			else {
				ff = fd.getFormField(field);		
    			if (ff!=null) {
					try {
						ff = (FormField)ff.clone();
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					vt.addElement(ff);   
    			}
			}		
		}
        
		if (fieldsRelatedMap.size()>0) {
			// 置关联表的顺序号
    		int p = 2;
    		Map<String, String> tableMap = new HashMap<String, String>();
			Iterator<String> irMap = fieldsRelatedMap.keySet().iterator();
			while (irMap.hasNext()) {
				String key = irMap.next();
				CondParam cp = fieldsRelatedMap.get(key);
				if (!tableMap.containsKey(cp.formCode)) {
    				cp.order = p;
    				p++;
    				tableMap.put(cp.formCode, String.valueOf(cp.order));
				}
				else {
					String strOrder = tableMap.get(cp.formCode);
					cp.order = StrUtil.toInt(strOrder);
				}
			}
		} 
		
		String cond = "";
        Iterator<FormField> ir = vt.iterator();
        while (ir.hasNext()) {
            FormField ff = ir.next();
            // ff中的name此时可能为main:***,other:***,sub:***
            
            // String value = ParamUtil.get(request, ff.getName());
            // 防止出现中文问题，@task应该把ParamUtil.get中改为支持chrome浏览器            
            String value = StrUtil.getNullStr(request.getParameter(ff.getName())).trim();                       
			if (!Global.requestSupportCN) {
                value = StrUtil.UnicodeToUTF8(value);
            }
            // System.out.println(SQLBuilder.class.getName() + " field=" + ff.getName() + " value=" + value);			
            
            String name_cond = ParamUtil.get(request, ff.getName() + "_cond");
    		// 当在module_list_nest_sel.jsp中，name_cond为空说明查询条件中不含有此字段，如果不排除掉可能会误将filter中需要从父窗口带入的字段作为查询条件，如filter中含有承包商contractor字段，则在表单中如果也含有此字段，会被视为条件
    		if ("".equals(name_cond)) {
    			continue;
    		}
    		
            String tableAlias = "t1"; // 别名
            String field = ff.getName();
            CondParam cp = null;
            // 根据映射的字段取得其对应表名的信息，并置字段对应的表的别名
            if (field.startsWith("main:") || field.startsWith("other:") || field.startsWith("sub:")) { // 映射的字段
				cp = fieldsRelatedMap.get(field);
				tableAlias = "t" + cp.order;
				FormDb mainFormDb = fm.getFormDb(cp.formCode);  
				
				// 将ff的name恢复，否则其name此时可能为main:***，other:***，sub:***
				ff = mainFormDb.getFormField(cp.fieldName);
			}
            
            if (ff.getType().equals(FormField.TYPE_DATE) ||
                ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                if (urlStr.equals(""))
                    urlStr = field + "_cond=" + name_cond;
                else
                    urlStr += "&" + field + "_cond=" + name_cond;
                if (name_cond.equals("0")) {
                    // 时间段
					String fDate = ParamUtil.get(request, field + "FromDate");
					String tDate = ParamUtil.get(request, field + "ToDate");
                    if (!fDate.equals("")) {
                    	if (cp!=null) {
                    		cp.isCondFieldBlank = false;
                    	}
                        if (cond.equals("")) {
                            cond += tableAlias + "." + ff.getName() + ">=" + StrUtil.sqlstr(fDate);
                            if (urlStr.equals(""))
                                urlStr += field + "FromDate=" + fDate;
                            else
                                urlStr += "&" + field + "FromDate=" + fDate;
                        } else {
                            cond += " and " + ff.getName() + ">=" + StrUtil.sqlstr(fDate);
                            urlStr += "&" + field + "FromDate=" + fDate;
                        }
                    }
                    if (!tDate.equals("")) {
                    	if (cp!=null) {
                    		cp.isCondFieldBlank = false;
                    	}
						if (cond.equals("")) {
							cond += tableAlias + "." + ff.getName() + "<=" + StrUtil.sqlstr(tDate);
							if (urlStr.equals(""))
								urlStr += field + "ToDate=" + tDate;
							else
								urlStr += "&" + field + "ToDate=" + tDate;
						} else {
							cond += " and " + tableAlias + "." + ff.getName() + "<=" + StrUtil.sqlstr(tDate);
							urlStr += "&" + field + "ToDate=" + tDate;
						}
                    }
                } else {
                    // 时间点
                    String d = ParamUtil.get(request, field);
                    if (!d.equals("")) {
                    	if (cp!=null) {
                    		cp.isCondFieldBlank = false;
                    	}
						cond = SQLFilter.concat(cond, "and", tableAlias + "." + ff.getName() + "=" + StrUtil.sqlstr(d));
						if (urlStr.equals(""))
							urlStr = field + "=" + StrUtil.UrlEncode(d);
						else
							urlStr += "&" + field + "=" + StrUtil.UrlEncode(d);
                    }
                }
            } else if (ff.getType().equals(FormField.TYPE_SELECT)) {
                String[] ary = ParamUtil.getParameters(request, field);
                if (ary != null) {
                    if (urlStr.equals(""))
                        urlStr = field + "_cond=" + name_cond;
                    else
                        urlStr += "&" + field + "_cond=" + name_cond;
                    
                    int len = ary.length;
                    if (len == 1) {
                        if (!ary[0].equals("")) {
                        	if (cp!=null) {
                        		cp.isCondFieldBlank = false;
                        	}
							if (cond.equals("")) {
								cond += tableAlias + "." + ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
								if (urlStr.equals("")) {
									urlStr += field + "=" + StrUtil.UrlEncode(ary[0]);
								} else {
									urlStr += "&" + field + "=" + StrUtil.UrlEncode(ary[0]);
								}
							} else {
								cond += " and " + tableAlias + "." + ff.getName() + "=" + StrUtil.sqlstr(ary[0]);
								urlStr += "&" + field + "=" + StrUtil.UrlEncode(ary[0]);
							}
                        }
                    } else {
                        String orStr = "";
                        for (int n = 0; n < len; n++) {
                            if (!ary[n].equals("")) {
								orStr = SQLFilter.concat(orStr, "or",
										tableAlias + "." + ff.getName() + "=" + StrUtil.sqlstr(ary[n]));
								if (urlStr.equals("")) {
									urlStr = field + "=" + StrUtil.UrlEncode(ary[n]);
								} else {
									urlStr += "&" + field + "=" + StrUtil.UrlEncode(ary[n]);
								}
                            }
                        }
                        if (!orStr.equals("")) {
                        	if (cp!=null) {
                        		cp.isCondFieldBlank = false;
                        	}
                            cond = SQLFilter.concat(cond, "and", orStr);
                        }
                    }
                }
            }
    		else if (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
                if (!value.equals("")) {
                	if (cp!=null) {
                		cp.isCondFieldBlank = false;
                	}
					if (cond.equals("")) {
						cond += tableAlias + "." + ff.getName() + name_cond + value;
						if (urlStr.equals("")) {
							urlStr += field + "=" + StrUtil.UrlEncode(value);
						} else {
							urlStr += "&" + field + "=" + StrUtil.UrlEncode(value);
						}
					} else {
						cond += " and " + tableAlias + "." + ff.getName() + name_cond + value;
						urlStr += "&" + field + "=" + StrUtil.UrlEncode(value);
					}
                    urlStr += "&" + field + "_cond=" + name_cond;
                }            			
    		}
            else {
            	boolean isSpecial = false; // 是否为特殊的控件（即支持模糊查询的宏控件）
				if (value.equals(SQLBuilder.IS_EMPTY) || value.equals(SQLBuilder.IS_NOT_EMPTY)) {
					// 如果是为空或者不为空的查询，则不获取宏控件的模糊查询，否则会生成类似这样的无意义的条件 in (select name from users where realname like '%<>空%')
				}
				else {
					if (ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						IFormMacroCtl ifmc = mu.getIFormMacroCtl();
						String oldCond = cond;
						String[] ary = getMacroCondsAndUrlStrs(request, ifmc, ff, name_cond, value, cond, urlStr, tableAlias);
						if (ary != null) {
							isSpecial = true;
							if (!"".equals(ary[0])) {
								// ary[0]中会把cond之前的值带进去，ary[1]中会把urlStr之前的值带进去
								cond = ary[0];
								urlStr = ary[1];
								// 如果不相等，说明条件中的宏控件request有传值
								if (!oldCond.equals(cond)) {
									if (cp != null) {
										cp.isCondFieldBlank = false;
									}
								}
							}
						}
					}
				}
            	
            	if (!isSpecial) {
                    if (name_cond.equals("0")) {
                        if (!value.equals("")) {
                        	if (cp!=null) {
                        		cp.isCondFieldBlank = false;
                        	}
                            if (cond.equals("")) {
                            	if (value.equals(IS_EMPTY)) {
                            		cond = "(" + tableAlias + "." + ff.getName() + " is null or " + tableAlias + "." + ff.getName() + "='')";
                            	}
                            	else if (value.equals(IS_NOT_EMPTY)) {
                            		cond = "(" + tableAlias + "." + ff.getName() + " is not null and " + tableAlias + "." + ff.getName() + "<>'')";                            		
                            	}
                            	else {
                            		cond = tableAlias + "." + ff.getName() + " like " + StrUtil.sqlstr("%" + value + "%");
                            	}
								if (urlStr.equals("")) {
									urlStr += field + "=" + StrUtil.UrlEncode(value);
								} else {
									urlStr += "&" + field + "=" + StrUtil.UrlEncode(value);
								}
								String realValue = ParamUtil.get(request, field + "_realshow");
								if (!realValue.equals("")) {
									urlStr += "&" + field + "_realshow=" + StrUtil.UrlEncode(realValue);
								}
                            } else {
                            	if (value.equals(IS_EMPTY)) {
                            		cond += " and (" + tableAlias + "." + ff.getName() + " is null or " + tableAlias + "." + ff.getName() + "='')";
                            	}
                            	else if (value.equals(IS_NOT_EMPTY)) {
                            		cond += " and (" + tableAlias + "." + ff.getName() + " is not null and " + tableAlias + "." + ff.getName() + "<>'')";                            		
                            	}
                            	else {
									cond += " and " + tableAlias + "." + ff.getName() + " like "
											+ StrUtil.sqlstr("%" + value + "%");
                            	}
								urlStr += "&" + field + "=" + StrUtil.UrlEncode(value);

								String realValue = ParamUtil.get(request, field + "_realshow");
								if (!realValue.equals("")) {
									urlStr += "&" + field + "_realshow=" + StrUtil.UrlEncode(realValue);
								}                                
                            }
                            urlStr += "&" + field + "_cond=" + name_cond;
                        }
                    } else if (name_cond.equals("1")) {
                        if (!value.equals("")) {
                        	if (cp!=null) {
                        		cp.isCondFieldBlank = false;
                        	}
							if (cond.equals("")) {
                            	if (value.equals(IS_EMPTY)) {
                            		cond = "(" + tableAlias + "." + ff.getName() + " is null or " + tableAlias + "." + ff.getName() + "='')";
                            	}
                            	else if (value.equals(IS_NOT_EMPTY)) {
                            		cond = "(" + tableAlias + "." + ff.getName() + " is not null and " + tableAlias + "." + ff.getName() + "<>'')";                            		
                            	}
                            	else {
                            		cond = tableAlias + "." + ff.getName() + "=" + StrUtil.sqlstr(value);
                            	}
								if (urlStr.equals("")) {
									urlStr += field + "=" + StrUtil.UrlEncode(value);
								} else {
									urlStr += "&" + field + "=" + StrUtil.UrlEncode(value);
								}
							} else {
                            	if (value.equals(IS_EMPTY)) {
                            		cond += " and (" + tableAlias + "." + ff.getName() + " is null or " + tableAlias + "." + ff.getName() + "='')";
                            	}
                            	else if (value.equals(IS_NOT_EMPTY)) {
                            		cond += " and (" + tableAlias + "." + ff.getName() + " is not null and " + tableAlias + "." + ff.getName() + "<>'')";                            		
                            	}
                            	else {
                            		cond += " and " + tableAlias + "." + ff.getName() + "=" + StrUtil.sqlstr(value);
                            	}
								urlStr += "&" + field + "=" + StrUtil.UrlEncode(value);
							}
							urlStr += "&" + field + "_cond=" + name_cond;
                        }
                    }
                }
            }
        }
		
		// 取得关联的子表，其所关联的字段
		Map<String, String> mapSubRelateField = new HashMap<String, String>();
		ModuleRelateDb mrd = new ModuleRelateDb();
		ModuleSetupDb msdRelate = new ModuleSetupDb();
		java.util.Iterator irTop = mrd.getModulesRelated(fd.getCode()).iterator();
		while (irTop.hasNext()) {
			mrd = (ModuleRelateDb)irTop.next();
			msdRelate = msdRelate.getModuleSetupDb(mrd.getString("relate_code"));
			if (msdRelate==null) {
				msdRelate = new ModuleSetupDb();
				DebugUtil.log(SQLBuilder.class, "fitCondAndUrlStr", fd.getName() + "关联的模块 " + mrd.getString("relate_code") + " 已不存在");
				continue;
			}
			String relateField = mrd.getString("relate_field");
			mapSubRelateField.put(msdRelate.getString("form_code"), relateField);
		}        			
		        			
		// 组装表之间的关联条件
		Iterator<String> irMap = fieldsRelatedMap.keySet().iterator();
		while (irMap.hasNext()) {
			String key = irMap.next();
			CondParam cp = fieldsRelatedMap.get(key);
			// 如果条件字段的值为空，则忽略该表
			if (cp.isCondFieldBlank)
				continue;
			
			if (cp.type=="main") {
				if ("".equals(cond)) {
					cond = "t" + cp.order + ".id=t1.cws_id";        					
				}
				else {
					cond += " and " + " t" + cp.order + ".id=t1.cws_id";        					        						
				}
			}
			else if (cp.type=="sub") {
				String relateField = mapSubRelateField.get(cp.formCode);
				if ("".equals(cond)) {
					cond = "t" + cp.order + ".cws_id=t1." + relateField;
				}
				else {
					cond += " and " + " t" + cp.order + ".cws_id=t1." + relateField;        					
				}
			}
			else { // other型
				if ("".equals(cond)) {
					cond += "t1." + cp.thisField + "=" + "t" + cp.order + "." + cp.otherField;
				}
				else {
					cond += " and " + "t1." + cp.thisField + "=" + "t" + cp.order + "." + cp.otherField;        						
				}
			}
		}
		
    	int cws_status = ParamUtil.getInt(request, "cws_status", -10000);           	
    	if (cws_status!=-10000) {
            String value = StrUtil.getNullStr(request.getParameter("cws_status")).trim();            		
            String name_cond = ParamUtil.get(request, "cws_status_cond");            		
            if (cond.equals("")) {
            	if (cws_status!=CWS_STATUS_NOT_LIMITED) {
            		cond = "t1.cws_status" + name_cond + value;
            	}
            	else {
            		// 不限，不包括临时记录及被删除的记录
            		cond = "t1.cws_status<>" + com.redmoon.oa.flow.FormDAO.STATUS_TEMP + " and t1.cws_status<>" + com.redmoon.oa.flow.FormDAO.STATUS_DELETED;
            	}
                if (urlStr.equals("")) {
                    urlStr = "cws_status=" + StrUtil.UrlEncode(value);
                }
                else {
                    urlStr += "&cws_status=" + StrUtil.UrlEncode(value);
                }
            } else {
            	if (cws_status!=CWS_STATUS_NOT_LIMITED) {                    	
            		cond += " and t1.cws_status" + name_cond + value;
            	}
            	else {
            		cond += " and t1.cws_status<>" + com.redmoon.oa.flow.FormDAO.STATUS_TEMP + " and t1.cws_status<>" + com.redmoon.oa.flow.FormDAO.STATUS_DELETED;
            	}
            	
                urlStr += "&cws_status=" + StrUtil.UrlEncode(value);
            }
            urlStr += "&cws_status_cond=" + name_cond;         		
    	}
    	
    	int cws_flag = ParamUtil.getInt(request, "cws_flag", -1);     
    	if (cws_flag!=-1) {
            String value = StrUtil.getNullStr(request.getParameter("cws_flag")).trim();            		
            String name_cond = ParamUtil.get(request, "cws_flag_cond");            		
            if (cond.equals("")) {
            	cond = "t1.cws_flag" + name_cond + value;
                if (urlStr.equals("")) {
                    urlStr = "cws_flag=" + StrUtil.UrlEncode(value);
                }
                else {
                    urlStr += "&cws_flag=" + StrUtil.UrlEncode(value);
                }
            } else {
            	cond += " and t1.cws_flag" + name_cond + value;
                urlStr += "&cws_flag=" + StrUtil.UrlEncode(value);
            }
            urlStr += "&cws_flag_cond=" + name_cond;         		
    	}    		
		
        aryCondAndUrlStr[0] = cond;
        aryCondAndUrlStr[1] = urlStr;
        aryCondAndUrlStr[2] = fieldsRelatedMap;
        return aryCondAndUrlStr; 	
    }

    public static String[] getModuleListRelateSqlAndUrlStr(HttpServletRequest
            request,
            FormDb fd, String op, String orderBy, String sort,
            String relateFieldValue) {
        return getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue, "", "");
    }

    /**
     * 获取关联模块列表sql语句
     * @param request HttpServletRequest
     * @param fd FormDb
     * @param op String
     * @param orderBy String
     * @param sort String
     * @param relateFieldValue String
     * @param userName String
     * @param fieldUserName String
     * @return String[]
     */
    public static String[] getModuleListRelateSqlAndUrlStr(HttpServletRequest
            request,
            FormDb fd, String op, String orderBy, String sort,
            String relateFieldValue, String userName,
            String fieldUserName) {
    	
    	// 检查是否相关联
        String sql;
        String condStatusAndRelate = "";
        
        if (IS_NOT_RELATED.equals(relateFieldValue)) {
        	sql = "select t1.id from " + FormDb.getTableName(fd.getCode()) + " t1";
        }
        else {
            sql = "select t1.id from " + FormDb.getTableName(fd.getCode()) + " t1";
            condStatusAndRelate = "t1.cws_id=" + StrUtil.sqlstr(relateFieldValue);
        }
        
        String mode = ParamUtil.get(request, "mode");
        if ("subTagRelated".equals(mode)) {
        	sql = "select t1.id from " + fd.getTableNameByForm() + " t1";
        }
        
        ModuleSetupDb msd = new ModuleSetupDb();
        
        int cwsStatusSubTag = -100;
        String tagName = ParamUtil.get(request, "tagName");
        // 通过选项卡标签关联模块
        if ("subTagRelated".equals(mode)) {
        	String moduleCode = ParamUtil.get(request, "code");
        	if ("".equals(moduleCode)) {
        		// 来自于H5页面，module_detail.jsp中的mui_module.js
        		moduleCode = ParamUtil.get(request, "moduleCode");
        	}
        	
        	ModuleSetupDb parentMsd = new ModuleSetupDb();
        	parentMsd = parentMsd.getModuleSetupDbOrInit(moduleCode);        	
        	
        	if ("".equals(tagName)) {
        		// 来自于手机端，因为tagName为中文，传过来会为乱码，所以传的是选项卡的顺序号，从0开始
				int subTagIndex = ParamUtil.getInt(request, "subTagIndex", -1);
				if (subTagIndex!=-1) {
					String[] subTags = StrUtil.split(StrUtil.getNullStr(parentMsd.getString("sub_nav_tag_name")), "\\|");
					int subLen = 0;
					if (subTags!=null)
						subLen = subTags.length;
					for (int i=0; i<subLen; i++) {
						if (i==subTagIndex) {
							tagName = subTags[i];
							break;
						}
					}
				}
        	}       	

        	String parentFormCode = parentMsd.getString("form_code");        	
           	String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleCode, tagName);
        	try {
        		JSONObject json = new JSONObject(tagUrl);
        		String fieldSource = "", fieldRelated = "";
        		if (!json.isNull("fieldSource")) {
        			fieldSource = json.getString("fieldSource");
        			fieldRelated = json.getString("fieldRelated");
        			
        			if (json.has("cwsStatus")) {
        				cwsStatusSubTag = StrUtil.toInt(json.getString("cwsStatus"), -100);
        			}
        			
            		if (!json.isNull("formRelated")) {
            			// formCodeRelated = json.getString("formRelated");
            			String moduleCodeRelated = json.getString("formRelated");
            			msd = msd.getModuleSetupDb(moduleCodeRelated);
            		}
        			
        			int parentId = ParamUtil.getInt(request, "parentId", -1);
        			FormDb fdParent = new FormDb();
        			fdParent = fdParent.getFormDb(parentFormCode);
        			
        			FormDAO fdao = new FormDAO();
        			fdao = fdao.getFormDAO(parentId, fdParent);
        			FormField ff = fdao.getFormField(fieldSource);
        			
        			String sourceFieldValue = "";
        			if ("id".equalsIgnoreCase(fieldSource)) {
        				sourceFieldValue = String.valueOf(fdao.getId());
        			}
        			else {
        				sourceFieldValue = fdao.getFieldValue(fieldSource);
        			}
        			
        			if (!"".equals(fieldRelated)) {
	        			if (ff!=null && (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE)) {
	        				condStatusAndRelate += " and " + fieldRelated + "=" + sourceFieldValue;
	        			}
	        			else {
	        				condStatusAndRelate += " and " + fieldRelated + "=" + StrUtil.sqlstr(sourceFieldValue);        				
	        			}
        			}
        			
        			if (json.has("fieldOtherRelated")) {
        				String fieldOtherRelated = json.getString("fieldOtherRelated");
        				String fieldOtherRelatedVal = json.getString("fieldOtherRelatedVal");
        				// 如果存在条件值
        				if (!"".equals(fieldOtherRelatedVal)) {
	            			if (ff!=null && (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE)) {
	            				condStatusAndRelate += " and " + fieldOtherRelated + "=" + fieldOtherRelatedVal;
	            			}
	            			else {
	            				condStatusAndRelate += " and " + fieldOtherRelated + "=" + StrUtil.sqlstr(fieldOtherRelatedVal);        				
	            			}        				
        				}
        			}
        		}
        		else {
        			LogUtil.getLog(SQLBuilder.class).error("选项卡关联配置不正确！");
        		}
        	} catch (JSONException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}

        	int cws_status = ParamUtil.getInt(request, "cws_status", -10000);           	
    		// 如果request中无请求，则cws_status以指定的为准
        	if (cws_status==-10000) {
	        	if (cwsStatusSubTag!=-100) {
	        		condStatusAndRelate += " and cws_status=" + cwsStatusSubTag;
	        	}
        	}
        }
        else {
        	// 来自于PC端的请求
        	String formCode = ParamUtil.get(request, "formCode");
        	if ("".equals(formCode)) {
        		// 来自于手机端的请求，public/android/module/listRelate
        		String moduleCode = ParamUtil.get(request, "moduleCode");
        		if (!"".equals(moduleCode)) {
        			ModuleSetupDb parentMsd = new ModuleSetupDb();
        			parentMsd = msd.getModuleSetupDb(moduleCode);
        			formCode = parentMsd.getString("form_code");
        		}
        	}
        	
        	// 传入的formCodeRelated可能为模块名
        	String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
        	if ("".equals(moduleCodeRelated)) {
        		moduleCodeRelated = ParamUtil.get(request, "formCodeRelated"); 
        	}
        	msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
        	
        	int cws_status = ParamUtil.getInt(request, "cws_status", -10000);      
    		// 如果request中无请求，则cws_status以指定的为准        	
        	if (cws_status==-10000) {    	
	        	String formCodeRelated = msd.getString("form_code");
	        	ModuleRelateDb mrd = new ModuleRelateDb();
	        	mrd = mrd.getModuleRelateDb(formCode, formCodeRelated);
	        	// 两个模块有可能不相关联，而是通过简单选项卡链接，如：module_list_relate.jsp?formCodeRelated=ksjd
	        	if (mrd!=null) {
		        	int cwsStatus = mrd.getInt("cws_status");
		        	if (cwsStatus!=-100) {
		        		condStatusAndRelate += " and t1.cws_status=" + cwsStatus;
		        	}
	        	}
        	}
        }

        String query = ParamUtil.get(request, "query");
        String cond="", urlStr = "";
        if (!query.equals("")) {
            sql = query;
        }
		else {
			if (op.equals("search")) {				
        		Object[] aryCondAndUrlStr = fitCondAndUrlStr(request, msd, fd);
        		cond = (String)aryCondAndUrlStr[0];
        		urlStr = (String)aryCondAndUrlStr[1];
        		Map<String, CondParam> fieldsRelatedMap = (HashMap<String, CondParam>)aryCondAndUrlStr[2];

        		// 如果fieldsRelatedMap不为空，则说明有其它的main:或other:相关表
        		if (fieldsRelatedMap.size()>0) {
        			// 组装表名，并记录序号
            		Map<String, String> tableMap = new HashMap<String, String>();
        			Iterator<String> irMap = fieldsRelatedMap.keySet().iterator();
        			while (irMap.hasNext()) {
        				String key = irMap.next();
        				CondParam cp = fieldsRelatedMap.get(key);
        				if (!tableMap.containsKey(cp.formCode)) {
        					// 如果条件字段所传的参数不为空
        					if (!cp.isCondFieldBlank) {        					
        						sql += ", form_table_" + cp.formCode + " t" + cp.order;
        						tableMap.put(cp.formCode, String.valueOf(cp.order));
        					}
        				}
        			}
        		}
			}

    		sql += " where 1=1";
			if (!cond.equals("")) {
				sql += " and " + cond;
			}
    		sql += " and " + condStatusAndRelate;     				
		}
        
        // System.out.println(SQLBuilder.class.getName() + " " + sql);

        // 加入从模块对应的主模块的过滤条件
        String[] filter = ModuleUtil.parseFilter(request);
        if (filter!=null) {
	        if (!"".equals(filter[0]) && filter[0]!=null) {
	        	// 如果filter中以select 打头，则说明是个完整的sql语句 
	        	if (filter[0].toLowerCase().startsWith("select ")) {
	        		sql = filter[0]; 
	        	}
	        	else {
		        	if (sql.toLowerCase().indexOf(" where ")!=-1)
		        		sql += " and " + filter[0];
		        	else
		        		sql += " where " + filter[0];
	        	}
	        }
        }        
        
        sql += " order by " + orderBy + " " + sort;

        String[] ary = new String[2];
        ary[0] = sql;
        ary[1] = urlStr;

        return ary;
    }
    
    /**
     * 取得宏控件的条件和url字符串
     * @param request
     * @param ifmc
     * @param ff
     * @param name_cond
     * @param value
     * @param cond
     * @param urlStr
     * @return
     */
    public static String[] getMacroCondsAndUrlStrs(HttpServletRequest request, IFormMacroCtl ifmc, FormField ff, String name_cond, String value, String cond, String urlStr, String tableAlias) {
    	// 手机端尚不支持多表联合查询，tableAlias会为空
    	if (!"".contentEquals(tableAlias)) {
    		tableAlias += ".";
    	}
    	boolean isSpecial = false;
		if (ifmc instanceof CurrentUserCtl ||
			ifmc instanceof UserSelectWinCtl || ifmc instanceof UserSelectCtl
			) {
			isSpecial = true;
			
			if (name_cond.equals("0")) {
                if (!value.equals("")) {
                    if (cond.equals("")) {
                        cond += tableAlias + ff.getName() + " in (select name from users where realname like " +
                                StrUtil.sqlstr("%" + value + "%") + " and isValid=1)";
                        
                        if (urlStr.equals("")) {
                            urlStr += ff.getName() + "=" +
                                    StrUtil.UrlEncode(value);
                        } else {
                            urlStr += "&" + ff.getName() + "=" +
                                    StrUtil.UrlEncode(value);
                        }
                    } else {
                        cond += " and " + tableAlias + ff.getName() + " in (select name from users where realname like " +
                        StrUtil.sqlstr("%" + value + "%") + " and isValid=1)";
                        urlStr += "&" + ff.getName() + "=" +
                                StrUtil.UrlEncode(value);
                    }
                    urlStr += "&" + ff.getName() + "_cond=" + name_cond;
                }
            } else if (name_cond.equals("1")) {
                if (!value.equals("")) {
                    if (cond.equals("")) {
                        cond += tableAlias + ff.getName() + " in (select name from users where realname=" +
                        	StrUtil.sqlstr(value) + " and isValid=1)";
                        if (urlStr.equals("")) {
                            urlStr += ff.getName() + "=" +
                                    StrUtil.UrlEncode(value);
                        }
                        else {
                            urlStr += "&" + ff.getName() + "=" +
                                    StrUtil.UrlEncode(value);
                        }
                    } else {
                        cond += " and " + tableAlias + ff.getName() + " in (select name from users where realname=" +
                    		StrUtil.sqlstr(value) + " and isValid=1)";
                        urlStr += "&" + ff.getName() + "=" +
                                StrUtil.UrlEncode(value);
                    }
                    urlStr += "&" + ff.getName() + "_cond=" +
                            name_cond;
                }
            }    							
		}
		else if (ifmc instanceof BasicSelectCtl) {
			isSpecial = true;
			
	        String code = ff.getDefaultValueRaw();
	        SelectMgr sm = new SelectMgr();
	        SelectDb sd = sm.getSelect(code);

			if (name_cond.equals("0")) { // 模糊
                if (!value.equals("")) {
                    if (cond.equals("")) {
				        if (sd.getType()==SelectDb.TYPE_LIST) {    							
                            cond += tableAlias + ff.getName() + " in (select value from oa_select_option where name like " +
                                    StrUtil.sqlstr("%" + value + "%") + ")";
				        }
				        else {
                            cond += tableAlias + ff.getName() + " in (select code from oa_tree_select where name like " +
                            StrUtil.sqlstr("%" + value + "%") + ")";    	        					        	
				        }
                        
                        if (urlStr.equals("")) {
                            urlStr += ff.getName() + "=" +
                                    StrUtil.UrlEncode(value);
                        } else {
                            urlStr += "&" + ff.getName() + "=" +
                                    StrUtil.UrlEncode(value);
                        }
                    } else {
				        if (sd.getType()==SelectDb.TYPE_LIST) {    							
                            cond += " and " + tableAlias + ff.getName() + " in (select value from oa_select_option where name like " +
                                    StrUtil.sqlstr("%" + value + "%") + ")";
				        }
				        else {
                            cond += " and " + tableAlias + ff.getName() + " in (select code from oa_tree_select where name like " +
                            StrUtil.sqlstr("%" + value + "%") + ")";    	        					        	
				        }    	                                	
                        urlStr += "&" + ff.getName() + "=" +
                                StrUtil.UrlEncode(value);
                    }
                    urlStr += "&" + ff.getName() + "_cond=" +
                            name_cond;
                }
            } else if (name_cond.equals("1")) {
                if (!value.equals("")) {
                    if (cond.equals("")) {
				        if (sd.getType()==SelectDb.TYPE_LIST) {    							
                            cond += tableAlias + ff.getName() + "=" + StrUtil.sqlstr(value);
				        }
				        else {
                            cond += tableAlias + ff.getName() + "=" + StrUtil.sqlstr(value);    	        					        	
				        }

                        if (urlStr.equals("")) {
                            urlStr += ff.getName() + "=" +
                                    StrUtil.UrlEncode(value);
                        }
                        else {
                            urlStr += "&" + ff.getName() + "=" +
                                    StrUtil.UrlEncode(value);
                        }
                    } else {
				        if (sd.getType()==SelectDb.TYPE_LIST) {    							
                            cond += " and " + tableAlias + ff.getName() + "=" + StrUtil.sqlstr(value);
				        }
				        else {
                            cond += " and " + tableAlias + ff.getName() + "=" + StrUtil.sqlstr(value);    	        					        	
				        }    	                                	

                        urlStr += "&" + ff.getName() + "=" +
                                StrUtil.UrlEncode(value);
                    }
                    urlStr += "&" + ff.getName() + "_cond=" +
                            name_cond;
                }
            }  
		}
		/*
		 * 已改为启用ModuleFieldSelectCtl中的getSqlForQuery
		 * else if (ifmc instanceof ModuleFieldSelectCtl) {
	        String strDesc = ff.getDefaultValueRaw();
			try {
				strDesc = ModuleFieldSelectCtl.formatJSONStr(strDesc);
				JSONObject json = new JSONObject(strDesc);
		        String formCode = json.getString("sourceFormCode");
		        String byFieldName = json.getString("idField").toLowerCase();
		        String showFieldName = json.getString("showField");
		        if ("id".equals(byFieldName)) {
		        	if (!"id".equals(showFieldName)) {
						isSpecial = true;
						if (name_cond.equals("0")) {
                            if (!value.equals("")) {
                                if (cond.equals("")) {
                                    cond += ff.getName() + " in (select id from form_table_" + formCode + " where " + showFieldName + " like " +
                                          	StrUtil.sqlstr("%" + value + "%") + ")";
                                    
                                    if (urlStr.equals("")) {
                                        urlStr += ff.getName() + "=" +
                                                StrUtil.UrlEncode(value);
                                    } else {
                                        urlStr += "&" + ff.getName() + "=" +
                                                StrUtil.UrlEncode(value);
                                    }
                                } else {
                                    cond += " and " + ff.getName() + " in (select id from form_table_" + formCode + " where " + showFieldName + " like " +
                                    	StrUtil.sqlstr("%" + value + "%") + ")";
                                    urlStr += "&" + ff.getName() + "=" +
                                            StrUtil.UrlEncode(value);
                                }
                                urlStr += "&" + ff.getName() + "_cond=" +
                                        name_cond;
                            }
                        } else if (name_cond.equals("1")) {
                            if (!value.equals("")) {
                                if (cond.equals("")) {
                                    cond += ff.getName() + " in (select id from form_table_" + formCode + " where " + showFieldName + "=" +
                                  		StrUtil.sqlstr(value) + ")";
                                    if (urlStr.equals("")) {
                                        urlStr += ff.getName() + "=" +
                                                StrUtil.UrlEncode(value);
                                    }
                                    else {
                                        urlStr += "&" + ff.getName() + "=" +
                                                StrUtil.UrlEncode(value);
                                    }
                                } else {
                                    cond += " and " + ff.getName() + " in (select id from form_table_" + formCode + " where " + showFieldName + "=" +
                              			StrUtil.sqlstr(value) + ")";
                                    urlStr += "&" + ff.getName() + "=" +
                                            StrUtil.UrlEncode(value);
                                }
                                urlStr += "&" + ff.getName() + "_cond=" +
                                        name_cond;
                            }
                        }			    							
		        	}
		        }
			}
		    catch (JSONException e) {
		    	// TODO Auto-generated catch block
		    	System.out.println("strDesc:" + strDesc);
	    		e.printStackTrace();
	    	}				        
			
		}*/
		else {
			// 如果是其它需根据名称（而非值）来查询的宏控件
            if (!value.equals("")) {
				String subSql = ifmc.getSqlForQuery(request, ff, value, name_cond.equals("0"));
				if (!"".equals(subSql)) {
					isSpecial = true;
					
					if (cond.equals("")) {
                        cond += tableAlias + ff.getName() + " in (" + subSql + ")";
                        
                        if (urlStr.equals("")) {
                            urlStr += ff.getName() + "=" +
                                    StrUtil.UrlEncode(value);
                        } else {
                            urlStr += "&" + ff.getName() + "=" +
                                    StrUtil.UrlEncode(value);
                        }
                    } else {
                        cond += " and " + tableAlias + ff.getName() + " in (" + subSql + ")";
                        urlStr += "&" + ff.getName() + "=" +
                                StrUtil.UrlEncode(value);
                    }
                    urlStr += "&" + ff.getName() + "_cond=" + name_cond;	        							
				}
            }
		}
		if (!isSpecial) {
			return null;
		}
		String[] ary = new String[2];
		ary[0] = cond;
		ary[1] = urlStr;
		return ary;
    }
    
    public static String getCondTypeDesc(HttpServletRequest request, String condType) {
    	String desc = "";
    	if (condType.equals(COND_TYPE_FUZZY)) {
    		desc = "模糊";
    	}
    	else if (condType.equals(COND_TYPE_NORMAL)) {
    		desc = "等于";
    	}
    	return desc;
    }
    
	static class CondParam {
		String formCode;
		String fieldName; // 关联表中的条件字段
		int order = 1;
		String type = "main"; // 或者other、sub
		boolean isCondFieldBlank = true; // 条件字段是否为空
		
		// other型字段格式，  cws_id:personbasic:id:realname
		String thisField; // 本表中的字段
		String otherField; // 对应本表字段的关联表中的字段，与本表中的字段相等
		
		CondParam(String formCode, String fieldName, String type) {
			this.formCode = formCode;
			this.fieldName = fieldName;
			this.type = type;
		}
		
		CondParam(String formCode, String fieldName, String type, String thisField, String otherField) {
			this.formCode = formCode;
			this.fieldName = fieldName;			
			this.type = type;
			this.thisField = thisField;
			this.otherField = otherField;
		}		
	}    
}

