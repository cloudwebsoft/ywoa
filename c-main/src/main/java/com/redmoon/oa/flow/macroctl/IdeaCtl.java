package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.StrUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.*;
import com.cloudwebsoft.framework.util.LogUtil;

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
public class IdeaCtl extends AbstractMacroCtl {
    public IdeaCtl() {
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String str = FormField.toHtml(StrUtil.getNullString(ff.getValue()));
        str += "<BR><textarea id='" + ff.getName() + "' name='" + ff.getName() + "' style='cursor:hand;width:95%' readonly onClick='openWinIdea(this)' rows=8 cols=100 title='意见框'></textarea>";
        return str;
    }

    /**
     * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
     * @return String
     */
    public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
        return "";
    }

    /**
     * 获取用来保存宏控件toHtml后的值的表单中的HTML元素中保存的值，生成用以禁用控件的脚本
     * @return String
     */
    public String getDisableCtlScript(FormField ff, String formElementId) {
        return "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                "','', '');\n";
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" +
                ff.getType() + "','');\n";
    }

    @Override
    public Object getValueForSave(FormField ff, int flowId, FormDb fd, FileUpload fu) {
        // 意见框，追加数据ff.getName()
        // 取得以前的数据，ff中已经是数据库中的数据了，因为20130406 fgf 修改了FormDAO.update方法，使其load了数据
        String valueFromDb = "";
        FormDAO fdao = new FormDAO(flowId, fd);
        fdao.load();
        Vector vts = fdao.getFields();
        Iterator irt = vts.iterator();
        while (irt.hasNext()) {
            FormField ff2 = (FormField) irt.next();
            if (ff2.getName().equals(ff.getName())) {
                valueFromDb = StrUtil.getNullStr(ff2.getValue()).trim();
                LogUtil.getLog(getClass()).info("save: valueFromDb=" + valueFromDb + " ff.getValue()=" + ff.getValue());
                break;
            }
        }
        if (!valueFromDb.equals(""))
            return valueFromDb + "\r\n" + StrUtil.getNullStr(fu.getFieldValue(ff.getName()));
        else
            return ff.getValue();
    }

    public String getControlType() {
        return "text";
    }

    public String getControlValue(String userName, FormField ff) {
        return ff.getValue();
    }

    public String getControlText(String userName, FormField ff) {
        return ff.getValue();
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

}
