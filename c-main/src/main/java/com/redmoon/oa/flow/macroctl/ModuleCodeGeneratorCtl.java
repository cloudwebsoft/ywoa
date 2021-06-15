package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.util.DateUtil;
import cn.js.fan.db.ResultRecord;
import java.sql.*;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * 默认值的格式：表单编码,前缀,date
 * 生成后的格式：bo20111028-123
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ModuleCodeGeneratorCtl extends AbstractMacroCtl {

    public ModuleCodeGeneratorCtl() {
    }

    public synchronized String getNextCode(FormField ff) {
        // 0-表单编码, 1-前缀(如：bo表示商机), 2-date(是否包含日期，日期格式20110416)
        return getRuleCode(ff);
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        return "<input name='" + ff.getName() + "' value='" +
                     getNextCode(ff) + "' size=15>";
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
     * @return String
     */
    public String getSetCtlValueScript(HttpServletRequest request,
                                       IFormDAO IFormDao, FormField ff,
                                       String formElementId) {
        // 如果序列尚未写入，则显示下一个序列
        if (ff.getValue()==null || ff.getValue().equals("")) {
            return "";
        } else {
            return super.getSetCtlValueScript(request, IFormDao, ff,
                                              formElementId);
        }
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        // 不disable序列
        return "";
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() +
                "','" + ff.getValue() + "');\n";
    }

    @Override
    public Object getValueForSave(FormField ff, FormDb fd, long formDAOId,
                                  FileUpload fu) {
        FormDAO fdao = new FormDAO();
        fdao = fdao.getFormDAO(formDAOId, fd);
        String valueFromDb = "";
        Vector vts = fdao.getFields();
        Iterator irt = vts.iterator();
        while (irt.hasNext()) {
            FormField ff2 = (FormField) irt.next();
            if (ff2.getName().equals(ff.getName())) {
                valueFromDb = ff2.getValue();
                break;
            }
        }
        LogUtil.getLog(getClass()).info(ff.getName() + "=" + ff.getValue() + " valueFromDb=" + valueFromDb);
        if (valueFromDb.equals(""))
            return getNextCode(ff);
        else {
            // @task:检查是否有重复
            return ff.getValue();
        }
    }

    public String getControlType() {
        return "";
    }
    public String getRuleCode(FormField ff){
    	String code = "";
    	String rule = ff.getDefaultValueRaw();
        String[] ary = StrUtil.split(rule, ",");
        String prefix = "";
        boolean isDate = false;
        if (ary == null)
            return "请设置编码前缀";
        if (ary.length >= 2) {
            prefix = ary[1].trim();
        }
        if (ary.length>=3) {
            isDate = ary[2].trim().equals("date");
        }

        FormDb fd = new FormDb();
        fd = fd.getFormDb(ary[0].trim());

        int num = 1;
        code = prefix;
        if (isDate)
            code += DateUtil.format(new java.util.Date(), "yyyyMMdd"); //  + "-" + num;
        // 取出最后一条记录
        String sql = "select " + ff.getName() + " from " +
                     fd.getTableNameByForm() + " order by id desc";
        JdbcTemplate jt = new JdbcTemplate();
        ResultIterator ri = null;
        try {
            ri = jt.executeQuery(sql);
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                String c = rr.getString(1);
                LogUtil.getLog(getClass()).info("code in db is " + c);
                int p = c.indexOf("-");
                if (p!=-1) {
                    String pre = c.substring(0, p);
                    if (pre.equals(code)) {
                        c = c.substring(p + 1);
                        num = StrUtil.toInt(c, 0) + 1;
                    }
                }
            }
            code += "-" + num;
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
        }
    	return code;
    }

    public String getControlValue(String userName, FormField ff) {
    	return getRuleCode(ff);
    }

    public String getControlText(String userName, FormField ff) {
        return "";
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

}
