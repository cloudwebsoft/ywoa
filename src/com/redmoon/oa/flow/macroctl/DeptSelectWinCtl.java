package com.redmoon.oa.flow.macroctl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptMgr;
import com.redmoon.oa.dept.DeptUserDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;

public class DeptSelectWinCtl extends AbstractMacroCtl {
	
    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String deptCode = StrUtil.getNullStr(fieldValue);
        if (!deptCode.equals("") && !deptCode.equals("user_admin_dept") && !deptCode.startsWith("{")) {
        	DeptMgr dm = new DeptMgr();
        	
        	DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            if (!dd.isLoaded()) {
            	return "";
            }
            
			String deptName = dd.getName();

			String pCode = dd.getParentCode();
			while (!pCode.equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {	
				dd = dm.getDeptDb(pCode);
				if (!dd.isLoaded()) {
					break;
				}
				deptName = dd.getName() + "\\" + deptName;
				pCode = dd.getParentCode();
			}
            return deptName;
        }
        else
            return "";
    }
    

	@Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String str = "";
		
		String deptName = "";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
			DeptDb dept = new DeptDb();
			dept = dept.getDeptDb(ff.getValue());
			deptName = dept.getName();        	
        }
        
        String dirCode = "";
        String defaultDept = StrUtil.getNullStr(ff.getDefaultValueRaw());     
        if (!"".equals(defaultDept)) {
	        if (defaultDept.equals("my")) {
	        	DeptUserDb dud = new DeptUserDb();
	        	Privilege pvg = new Privilege();
	        	Iterator ir = dud.getDeptsOfUser(pvg.getUser(request)).iterator();
	        	if (ir.hasNext()) {
	        		DeptDb dd = (DeptDb)ir.next();
	        		dirCode = dd.getCode();
	        	}
	        }
	        else if (defaultDept.startsWith("{$") && defaultDept.endsWith("}")) {
	        	defaultDept = defaultDept.substring(2, defaultDept.length()-1);
	        	// 解析出父节点对应的字段名
	        	String fieldName = defaultDept.substring(2, defaultDept.length()-1);
	        	if (formDaoFlow!=null) {	        	
	        		dirCode = formDaoFlow.getFieldValue(fieldName);  
	        	}
	        }	        
	        else {
	        	dirCode = defaultDept;
	        }
        }
        
		str = "<input type=\"text\" name=\"" + ff.getName() + "_show\" id=\""
				+ ff.getName() + "_show\" readonly value=\"" + deptName + "\" size=\"10\" />";
		str += "&nbsp;<input type=\"button\" class=btn name=\""
				+ ff.getName()
				+ "_btn\" id=\""
				+ ff.getName()
				+ "_btn\" value=\"选择\" onclick=\"curObjId='"
				+ ff.getName()
				+ "';window.open('"
				+ request.getContextPath()
				+ "/dept_sel.jsp?dirCode=" + dirCode + "','_blank','toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width=640,height=480')\" />";
		str += "<input type=\"hidden\" name=\"" + ff.getName() + "\" id=\""	+ ff.getName() + "\" value=\"\" />";
		str += "<script>var curObjId;function selectNode(code, name) {o(curObjId).value=code; o(curObjId + \"_show\").value = name;}</script>";
		return str;
	}

	public String getDisableCtlScript(FormField ff, String formElementId) {
		String deptCode = StrUtil.getNullStr(ff.getValue());
		String deptName = "";
		if (!deptCode.equals("")) {		
			DeptMgr dm = new DeptMgr();
        	DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(deptCode);
            if (!dd.isLoaded()) {
            	return "";
            }
            
			deptName = dd.getName();
			String pCode = dd.getParentCode();
			while (!pCode.equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {	
				dd = dm.getDeptDb(pCode);
				if (!dd.isLoaded()) {
					break;
				}
				deptName = dd.getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + deptName;
				pCode = dd.getParentCode();
			}		
			deptName = "var deptName=\"" + deptName + "\";\n";			
		}
		else {
			deptName = "var deptName=\"\";\n";
		}		

        String str = deptName;
        str += "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
        	"', deptName, '" + ff.getValue() + "');\n";
		str += "if (o('" + ff.getName() + "_btn')) o('" + ff.getName() + "_btn').outerHTML='';";
				return str;
	}

	public String getReplaceCtlWithValueScript(FormField ff) {
		String deptName = "";
		if (ff.getValue() != null && !ff.getValue().equals("")) {
/*			DeptMgr dm = new DeptMgr();
			DeptDb lf = dm.getDeptDb(ff.getValue());
			deptName = lf.getName();*/
			
			DeptMgr dm = new DeptMgr();
        	
        	DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(ff.getValue());
            if (!dd.isLoaded()) {
            	return "";
            }
            
			deptName = dd.getName();

			String pCode = dd.getParentCode();
			while (!pCode.equals(DeptDb.ROOTCODE) && !dd.getCode().equals(DeptDb.ROOTCODE)) {	
				dd = dm.getDeptDb(pCode);
				if (!dd.isLoaded()) {
					break;
				}
				deptName = dd.getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + deptName;
				pCode = dd.getParentCode();
			}		
			deptName = "var deptName=\"" + deptName + "\";\n";
		}
		else {
			deptName = "var deptName=\"\";\n";
		}
		return deptName + "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
				+ "', deptName);\n";
	}

	public String getControlOptions(String userName, FormField ff) {
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        String unitcode = ud.getUnitCode();
        // 可能是用admin账户进行测试
        if (unitcode==null) {
        	unitcode = DeptDb.ROOTCODE;
        }        
        
        String deptCode = unitcode;
        boolean isInclude = true; // 是否包含指定的部门

        String defaultDept = StrUtil.getNullStr(ff.getDefaultValueRaw());
        if (!"".equals(defaultDept)) {
	        if (defaultDept.equals("my")) {
	        	DeptUserDb dud = new DeptUserDb();
	        	Iterator ir = dud.getDeptsOfUser(userName).iterator();
	        	if (ir.hasNext()) {
	        		DeptDb lf = (DeptDb)ir.next();
	        		deptCode = lf.getCode();
	        	}
	        }
	        else if (defaultDept.startsWith("{$") && defaultDept.endsWith("}")) {
	        	// 解析出对应的字段名
	        	String fieldName = defaultDept.substring(2, defaultDept.length()-1);
	        	if (formDaoFlow!=null) {
	        		deptCode = formDaoFlow.getFieldValue(fieldName);
	        	}
	        }
	        else if (defaultDept.startsWith("#")){
	        	deptCode = defaultDept.substring(1);
	        	isInclude = false;	        			        		
	        }
        }             
        
        JSONArray childrens = new JSONArray();
        try {
        	DeptSelectCtl dsc = new DeptSelectCtl();
            return dsc.getDeptNameAsOptions(deptCode, childrens, isInclude).toString();
        } catch (ErrMsgException ex1) {
            return "";
        }
	}

	public String getControlText(String userName, FormField ff) {
		// TODO Auto-generated method stub
		  String deptName = "";
	        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
	            String deptCode = ff.getValue();
	            DeptDb dd = new DeptDb();
	            dd = dd.getDeptDb(deptCode);
	            deptName = dd.getName();
	        } else {
	            // Privilege privilege = new Privilege();
	            UserDb ud = new UserDb();
	            ud = ud.getUserDb(userName);
	            DeptUserDb udd = new DeptUserDb();
	            Vector vdept = udd.getDeptsOfUser(ud.getName());
	            if (vdept != null && vdept.size() > 0) {
	                String deptCode = ((DeptDb) vdept.get(0)).getCode();
	                DeptDb dd = new DeptDb();
	                dd = dd.getDeptDb(deptCode);
	                deptName = dd.getName();
	            }
	        }
	        return deptName;
	}

	public String getControlType() {
		// TODO Auto-generated method stub
		return "selectWin";
	}

	public String getControlValue(String userName, FormField ff) {
		// TODO Auto-generated method stub
		String deptCode ="";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            deptCode = ff.getValue();
        } else {
            // Privilege privilege = new Privilege();
            UserDb ud = new UserDb();
            ud = ud.getUserDb(userName);
            DeptUserDb udd = new DeptUserDb();
            Vector vdept = udd.getDeptsOfUser(ud.getName());
            if (vdept != null && vdept.size() > 0) {
                deptCode = ((DeptDb) vdept.get(0)).getCode();
            }
        }
        return deptCode;
	}
	
	 public String getValueByName(FormField formField, String name) {
 /*    	DeptDb dd = new DeptDb();
     	String deptCode = dd.getCodeByName(name);
     	return StrUtil.getNullStr(deptCode);*/
     	
 		String deptCode = "";
    	
 		HashMap<String, String> map = new HashMap<String, String>();
 		String sql = "select code from department order by code";
 		JdbcTemplate jt = new JdbcTemplate();
 		ResultIterator ri;
 		try {
 			ri = jt.executeQuery(sql);
 			while (ri.hasNext()) {
 				ResultRecord rr = (ResultRecord)ri.next();
 				String code = rr.getString(1);
 				String deptName = getFullNameOfDept(code);
 				map.put(deptName, code);
 			}		
 			
 			String deptName = "";
 			String newDeptName[] = name.split("\\\\");
 			if (newDeptName==null) {
 				LogUtil.getLog(getClass()).error("部门为空！");
 				return "";				
 			}
 			int depLevel = newDeptName.length;
 			for (int j = 0; j < depLevel; j++) {
 				deptName += deptName.equals("") ? newDeptName[j] : ("-" + newDeptName[j]);
 			}	
 			deptCode = map.get(deptName);
 			if (deptCode == null || deptCode.equals("")) {
 				LogUtil.getLog(getClass()).error("部门：" + deptName + " 不存在！");
 				return "";
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return deptCode;
     }
     
 	public String getFullNameOfDept(String code) {
 		DeptDb dd = new DeptDb(code);
 		String name = dd.getName();
 		while (!dd.getParentCode().equals("-1")
 				&& !dd.getParentCode().equals(DeptDb.ROOTCODE)) {
 			dd = new DeptDb(dd.getParentCode());
 			if (dd != null && !dd.getParentCode().equals("")) {
 				name = dd.getName() + "-" + name;
 			} else {
 				return "";
 			}
 		}
 		return name;
 	}    	
 	
    /**
     * 取得根据名称（而不是值）查询时需用到的SQL语句，如果没有特定的SQL语句，则返回空字符串
     * @param request
     * @param ff 当前被查询的字段
     * @param value
     * @param isBlur 是否模糊查询
     * @return
     */
    public String getSqlForQuery(HttpServletRequest request, FormField ff, String value, boolean isBlur) {
		// 包含时，取出本部门及所有子部门
    	if ("".equals(value)){
    		return "";
    	}
    	if (isBlur) {
    		DeptDb dd = new DeptDb();
    		dd = dd.getDeptDb(value);
    		Vector vt = new Vector();
    		try {
				dd.getAllChild(vt, dd);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StringBuffer sb = new StringBuffer();
			sb.append(StrUtil.sqlstr(value));
			Iterator ir = vt.iterator();
			while (ir.hasNext()) {
				dd = (DeptDb)ir.next();
				StrUtil.concat(sb, ",", StrUtil.sqlstr(dd.getCode()));
			}
			return sb.toString();

/*			return "select code from department where code=" +
          		StrUtil.sqlstr(value) + " or parentCode=" + StrUtil.sqlstr(value);*/
		}
		else {
			return "";
		}
	}   	

}
