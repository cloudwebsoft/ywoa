package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;

import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;

public class CurrentUnitNameCtl  extends AbstractMacroCtl {
    public CurrentUnitNameCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        StringBuffer sb = new StringBuffer();
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
        // 取得用户名
        String userName = privilege.getUser(request);
        UserDb ud = new UserDb();
        ud = ud.getUserDb(userName);
        DeptDb dd = new DeptDb();
        dd = dd.getDeptDb(ud.getUnitCode());
        sb.append("<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='" + dd.getName() + "' size=15>");
        return sb.toString();
    }

    /**
     * 取得用来保存宏控件原始值及toHtml后的值的表单中的HTML元素，通常前者为textarea，后者为span
     * @return String
     */
    @Override
    public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(
            HttpServletRequest request, FormField ff) {
        // 如果是当前用户宏控件，则检查如果没有赋值就赋予其当前用户名称
        FormField ffNew = new FormField();
        ffNew.setName(ff.getName());
        ffNew.setValue(ff.getValue());
        ffNew.setType(ff.getType());
        ffNew.setFieldType(ff.getFieldType());

        // 如果是当前用户宏控件，则检查如果没有赋值就赋予其当前用户名称
        if (StrUtil.getNullStr(ff.getValue()).equals("")) {
            com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
            // 取得用户名
            String userName = privilege.getUser(request);
            UserDb ud = new UserDb();
            ud = ud.getUserDb(userName);
            DeptDb dd = new DeptDb();
            dd = dd.getDeptDb(ud.getUnitCode());
            
            ffNew.setValue(dd.getName());
        }

        // System.out.println(getClass() + " getOuterHTMLOfElementsWithRAWValueAndHTMLValue ffNew=" + ffNew.getValue());
        return super.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request,
                ffNew);
    }

    public String getControlType() {
        return "text";
    }

    public String getControlText(String userName,
                                 FormField formField) {
    	String deptCode = formField.getValue();
    	if (deptCode!=null) {
    		if (!"".equals(deptCode)) {
    			DeptDb dd = new DeptDb();
    			dd = dd.getDeptDb(deptCode);
    			return dd.getName();
    		}
    	}
    	else {
    		deptCode = "";
    	}
        return deptCode;
    }

    public String getControlValue(String userName, FormField ff) {
        return ff.getValue();
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

}
