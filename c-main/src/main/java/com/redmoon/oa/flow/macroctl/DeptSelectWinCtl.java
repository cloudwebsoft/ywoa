package com.redmoon.oa.flow.macroctl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ParamUtil;
import com.redmoon.oa.base.IFormDAO;
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
    @Override
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
        else {
			return "";
		}
    }

	@Override
	public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
		String str = "";
		
		String deptName = "";
        if (!StrUtil.isEmpty(ff.getValue())) {
			DeptDb dept = new DeptDb();
			dept = dept.getDeptDb(ff.getValue());
			deptName = dept.getName();        	
        }
        
        String deptCode = "", parentCode = "";
        String defaultDept = StrUtil.getNullStr(ff.getDescription());
		if (!"".equals(defaultDept) && StrUtil.isEmpty(ff.getValue())) {
	        if ("my".equals(defaultDept)) {
	        	DeptUserDb dud = new DeptUserDb();
	        	Privilege pvg = new Privilege();
	        	Iterator<DeptDb> ir = dud.getDeptsOfUser(pvg.getUser(request)).iterator();
	        	if (ir.hasNext()) {
	        		DeptDb dd = ir.next();
					deptCode = dd.getCode();
					deptName = dd.getName();
	        	}
	        }
	        else if (defaultDept.startsWith("{$") && defaultDept.endsWith("}")) {
	        	// defaultDept = defaultDept.substring(2, defaultDept.length()-1);
	        	// 解析出父节点对应的字段名
	        	String fieldName = defaultDept.substring(2, defaultDept.length()-1);
	        	if (iFormDAO!=null) {
					parentCode = iFormDAO.getFieldValue(fieldName);
	        	}
	        }	        
	        else {
				deptCode = defaultDept;
	        }
        }
        
		str = "<input type=\"text\" name=\"" + ff.getName() + "_realshow\" id=\""
				+ ff.getName() + "_realshow\" readonly value=\"" + deptName + "\" size=\"10\" />";
		str += "&nbsp;<input type=\"button\" class=\"btn btn-default\" name=\""
				+ ff.getName()
				+ "_btn\" id=\""
				+ ff.getName()
				+ "_btn\" value=\"选择\" onclick='openWinDeptsSelect(findObj(\"" + ff.getName() + "\"), true, \"" + parentCode + "\")' />";
		str += "<input type=\"hidden\" name=\"" + ff.getName() + "\" id=\""	+ ff.getName() + "\" value=\"" + deptCode + "\" />";
		return str;
	}

	@Override
	public String getSetCtlValueScript(HttpServletRequest request,
									   IFormDAO IFormDao, FormField ff, String formElementId) {
		String deptCode = "";
		if (!StrUtil.isEmpty(ff.getValue())) {
			DeptDb dept = new DeptDb();
			dept = dept.getDeptDb(ff.getValue());
			deptCode = dept.getCode();
		}
		else {
			String defaultDept = StrUtil.getNullStr(ff.getDescription());
			if (!"".equals(defaultDept)) {
				if (defaultDept.equals("my")) {
					DeptUserDb dud = new DeptUserDb();
					Privilege pvg = new Privilege();
					Iterator<DeptDb> ir = dud.getDeptsOfUser(pvg.getUser(request)).iterator();
					if (ir.hasNext()) {
						DeptDb dd = ir.next();
						deptCode = dd.getCode();
					}
				}
				else if (defaultDept.startsWith("{$") && defaultDept.endsWith("}")) {
					defaultDept = defaultDept.substring(2, defaultDept.length()-1);
					// 解析出父节点对应的字段名
					String fieldName = defaultDept.substring(2, defaultDept.length()-1);
					if (iFormDAO!=null) {
						deptCode = iFormDAO.getFieldValue(fieldName);
					}
				}
				else {
					deptCode = defaultDept;
				}
			}
		}

		return "setCtlValue('" + ff.getName() + "', '" + ff.getType() + "', '" + deptCode + "');\n";
	}

	@Override
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

	@Override
	public String getReplaceCtlWithValueScript(FormField ff) {
		String deptName = "";
		if (ff.getValue() != null && !"".equals(ff.getValue())) {
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

	@Override
	public String getControlOptions(String userName, FormField ff) {
        return "";
	}

	@Override
	public String getControlText(String userName, FormField ff) {
		String deptName = "";
		if (!"".equals(StrUtil.getNullStr(ff.getValue()))) {
			String deptCode = ff.getValue();
			DeptDb dd = new DeptDb();
			dd = dd.getDeptDb(deptCode);
			deptName = dd.getName();
		} else {
			if ("my".equals(ff.getDescription())) {
				UserDb ud = new UserDb();
				ud = ud.getUserDb(userName);
				DeptUserDb udd = new DeptUserDb();
				Vector<DeptDb> vdept = udd.getDeptsOfUser(ud.getName());
				if (vdept != null && vdept.size() > 0) {
					String deptCode = ((DeptDb) vdept.get(0)).getCode();
					DeptDb dd = new DeptDb();
					dd = dd.getDeptDb(deptCode);
					deptName = dd.getName();
				}
			}
		}
		return deptName;
	}

	@Override
	public String getControlType() {
		return "selectWin";
	}

	@Override
	public String getControlValue(String userName, FormField ff) {
		String deptCode ="";
        if (!"".equals(StrUtil.getNullStr(ff.getValue()))) {
            deptCode = ff.getValue();
        } else {
        	if ("my".equals(ff.getDescription())) {
				UserDb ud = new UserDb();
				ud = ud.getUserDb(userName);
				DeptUserDb udd = new DeptUserDb();
				Vector<DeptDb> vdept = udd.getDeptsOfUser(ud.getName());
				if (vdept != null && vdept.size() > 0) {
					deptCode = (vdept.get(0)).getCode();
				}
			}
        }
        return deptCode;
	}
	
	 @Override
     public String getValueByName(FormField formField, String name) {
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
 			String[] newDeptName = name.split("\\\\");
 			if (newDeptName.length==0) {
 				LogUtil.getLog(getClass()).error("部门为空！");
 				return "";				
 			}
 			int depLevel = newDeptName.length;
			for (String s : newDeptName) {
				deptName += deptName.equals("") ? s : ("-" + s);
			}
 			deptCode = map.get(deptName);
 			if (deptCode == null || "".equals(deptCode)) {
 				LogUtil.getLog(getClass()).error("部门：" + deptName + " 不存在！");
 				return "";
 			}
 		} catch (SQLException e) {
 			LogUtil.getLog(getClass()).error(e);
 		}
 		
 		return deptCode;
     }
     
 	public String getFullNameOfDept(String code) {
		DeptDb dd = new DeptDb();
		dd = dd.getDeptDb(code);
		String name = dd.getName();
		while (!"-1".equals(dd.getParentCode())
				&& !dd.getParentCode().equals(DeptDb.ROOTCODE)) {
			dd = dd.getDeptDb(dd.getParentCode());
			if (!"".equals(dd.getParentCode())) {
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
    @Override
    public String getSqlForQuery(HttpServletRequest request, FormField ff, String value, boolean isBlur) {
		// 包含时，取出本部门及所有子部门
    	if ("".equals(value)){
    		return "";
    	}
    	if (isBlur) {
    		DeptDb dd = new DeptDb();
    		dd = dd.getDeptDb(value);
    		Vector<DeptDb> vt = new Vector<>();
    		try {
				dd.getAllChild(vt, dd);
			} catch (ErrMsgException e) {
				LogUtil.getLog(getClass()).error(e);
			}
			StringBuffer sb = new StringBuffer();
			sb.append(StrUtil.sqlstr(value));
			for (DeptDb deptDb : vt) {
				dd = deptDb;
				StrUtil.concat(sb, ",", StrUtil.sqlstr(dd.getCode()));
			}
			return sb.toString();
		}
		else {
			return "";
		}
	}   	

}
