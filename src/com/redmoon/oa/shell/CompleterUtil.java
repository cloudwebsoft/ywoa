package com.redmoon.oa.shell;

import org.json.*;

import com.cloudwebsoft.framework.security.ProtectUnit;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.WorkflowLinkComparator;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.StrUtil;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class CompleterUtil {
    final static String group = "CompleterUtil";
    
	/**
	 * 根据类名取得包名
	 * @return
	 */
	public static String getClassPackage(String className) {
		String pkgName = "";
		return pkgName;
	}
	
    public static void reload() {
        try  {
        	RMCache.getInstance().invalidateGroup(group);
        }
        catch (Exception e) {
            LogUtil.getLog(CompleterUtil.class).error(e.getMessage());
        }
    }
	
	public static JSONArray getMethods(String clsName) {
		JSONArray arr = null;
		RMCache rmCache = RMCache.getInstance();
        try {
            String strArr = (String)rmCache.getFromGroup(clsName, group);
            if (strArr!=null) {
            	arr = new JSONArray(strArr);
            }
        } catch (Exception e) {
            LogUtil.getLog(CompleterUtil.class).error(e.getMessage());
        }
        if (arr==null) {
        	arr = new JSONArray();
		    try {
		        Class clazz = Class.forName(clsName);
		        Method[] methods = clazz.getMethods();
		        int k = 1000;
		        for (Method method : methods) {
		        	k--;
		            String methodName = method.getName();
		            JSONObject json = new JSONObject();
		            // caption: "getParent", value: "getParent", score: 300, meta:
		            json.put("score", k);
		            json.put("meta", clsName);
		            // System.out.println("方法名称:" + methodName);
		            StringBuffer sb = new StringBuffer();
		            Class<?>[] parameterTypes = method.getParameterTypes();
		            for (Class<?> clas : parameterTypes) {
		                String parameterName = clas.getName();
		                // System.out.println("参数名称:" + parameterName);
		                // 只取最后一个类名
		                int p = parameterName.lastIndexOf(".");
		                if (p!=-1) {
		                	parameterName = parameterName.substring(p+1);
		                }
		                StrUtil.concat(sb, ",", parameterName);                
		            }
		            
		            StringBuffer caption = new StringBuffer();
		            caption.append(methodName);
		            caption.append("(");
		            caption.append(sb);
		            caption.append(")");
		            json.put("caption", caption);         
		            json.put("value", caption); // methodName);
		            arr.put(json);
		        }
		        if (arr.length()>0) {
		            try {
		                rmCache.putInGroup(clsName, group, arr.toString());
		            }
		            catch (Exception e) {
		                LogUtil.getLog(CompleterUtil.class).error(e.getMessage());
		            }
		        }
		    } catch (ClassNotFoundException e) {
		    	System.out.println("clsName=" + clsName);
		        e.printStackTrace();
		    } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	    return arr;
	}
	
	public static JSONArray getAllClasses(String importSection) {
		importSection += "\nimport java.util.*;";
		importSection += "\nimport java.lang.*;";
		
		JSONArray arr = new JSONArray();
		List list = new ArrayList();
		String[] ary = StrUtil.split(importSection, "\n");
		if (ary!=null) {
			for (int i=0; i<ary.length; i++) {
				String pkgName = ary[i].trim();
				// 因为import部分可能有注释
				if (!pkgName.startsWith("import ")) {
					continue;
				}
				int p = pkgName.lastIndexOf(";");
				if (p!=-1) { 
					pkgName = pkgName.substring(0, p);
				}
				p = pkgName.indexOf(" ");
				pkgName = pkgName.substring(p+1).trim();
				List lt = getClasses(pkgName);
				list.addAll(lt);
			}
		}
		
		// 排序，将包名中显示指定类的排在前面
        Comparator ct = new ClassUnitComparator();
        Collections.sort(list, ct);
        
        Iterator ir = list.iterator();
        while (ir.hasNext()) {
        	ClassUnit cu = (ClassUnit)ir.next();
        	JSONObject json = new JSONObject();
        	try {
        		String clsName = cu.getClassName();
        		int p = clsName.lastIndexOf(".");
        		String name = clsName.substring(p+1);
        		json.put("name", name);
				json.put("fullName", cu.getClassName());
				json.put("explicit", cu.isExplicit()?"1":"0");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	arr.put(json);
        }

		return arr;
	}
	
	public static List getClasses(String pkgName) {
		int p = pkgName.lastIndexOf("*");
		// 如果是类名，而不是包名
		if (p==-1) {
			List<Class<?>> classes = new ArrayList<Class<?>>();
			try {
				classes.add(Class.forName(pkgName));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			List<ClassUnit> list = new ArrayList<ClassUnit>();
			Iterator<Class<?>> ir = classes.iterator();
			while (ir.hasNext()) {
				Class cls = ir.next();
				ClassUnit cu = new ClassUnit(cls, true);
				list.add(cu);
			}
			return list;
		}
		
/*		if (pkgName.length()==1) {
			return null;
		}*/
		
		if (p==pkgName.length()-1) {
			pkgName = pkgName.substring(0, p-1);
		}
	
		// 检查是否为类名，如果是，则改为包名
		List classes = null;
		RMCache rmCache = RMCache.getInstance();
        try {
            classes = (List)rmCache.getFromGroup(pkgName, group);
        } catch (Exception e) {
            LogUtil.getLog(CompleterUtil.class).error(e.getMessage());
        }
        if (classes==null) {
        	classes = ClassUtil.getClasses(pkgName);
        	if (classes.size()>0) {
	            try {
	                rmCache.putInGroup(pkgName, group, classes);
	            }
	            catch (Exception e) {
	                LogUtil.getLog(CompleterUtil.class).error(e.getMessage());
	            }
        	}
        }
		
		List<ClassUnit> list = new ArrayList<ClassUnit>();
		Iterator<Class<?>> ir = classes.iterator();
		while (ir.hasNext()) {
			Class cls = ir.next();
			ClassUnit cu = new ClassUnit(cls, false);
			list.add(cu);
		}
		return list;
	}
}
