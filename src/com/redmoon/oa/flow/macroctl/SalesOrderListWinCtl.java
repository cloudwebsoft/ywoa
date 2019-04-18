package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.*;

import com.redmoon.oa.flow.*;
import cn.js.fan.util.StrUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.visual.FormDAO;
import cn.js.fan.util.ParamUtil;
import com.redmoon.oa.base.IFormDAO;
import cn.js.fan.web.Global;

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
public class SalesOrderListWinCtl extends AbstractMacroCtl {
    public SalesOrderListWinCtl() {
    }

    public Object getValueForCreate(FormField ff) {
        return ff.getValue();
    }

    public FormDAO getFormDAOOfOrder(long id) {
        FormDb fd = new FormDb();
        fd = fd.getFormDb("sales_order");
        FormDAO fdao = new FormDAO(id, fd);
        return fdao;
    }

    /**
     * 用于列表中显示宏控件的值
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String
     * @return String
     */
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String v = StrUtil.getNullStr(fieldValue);
        if (!v.equals("")) {
            // LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" + StrUtil.toInt(v));
            FormDAO fdao = getFormDAOOfOrder(StrUtil.toInt(v));
            String str = "<a href='" + Global.getRootPath() + "/sales/customer_sales_order_show.jsp?customerId=" + fdao.getFieldValue("customer") + "&parentId=" + fdao.getFieldValue("customer") +  "&id=" + fdao.getId() + "&formCodeRelated=sales_order&formCode=sales_customer&isShowNav=0' target='_blank'>" + fdao.getFieldValue("code") + "</a>";
            return str;
        }
        else
            return "";
    }

    /**
     * convertToHTMLCtl
     *
     * @param request HttpServletRequest
     * @param ff FormField
     * @return String
     * @todo Implement this com.redmoon.oa.base.IFormMacroCtl method
     */
     public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
         String str = "";
         String v = "";
         String strOrderId = "";
         if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
             // LogUtil.getLog(getClass()).info("StrUtil.toInt(ff.getValue())=" + StrUtil.toInt(ff.getValue()));
             FormDAO fdao = getFormDAOOfOrder(StrUtil.toInt(ff.getValue()));
             // LogUtil.getLog(getClass()).info("mobile=" + fdao.getFieldValue("mobile"));
            //  v = "商机编号：" + fdao.getId();
            v = fdao.getFieldValue("code");
            strOrderId = ff.getValue();
         }
         else {
             long orderId = ParamUtil.getLong(request, "orderId", -1);
             if (orderId!=-1) {
                 FormDAO fdao = getFormDAOOfOrder(orderId);
                 v = fdao.getFieldValue("code");
                 strOrderId = "" + orderId;
             }
         }

         str += "<input id='" + ff.getName() + "_realshow' name='" + ff.getName() +
                 "_realshow' value='" + v +
                 "' size=15 readonly>";

         str += "<input id='" + ff.getName() + "' name='" + ff.getName() +
                 "' value='" + strOrderId + "' type='hidden'>";

         str +=
                 "&nbsp;<input type=button value=\"选择\" class=btn onClick=\"openWinSalesOrderList(document.getElementById('" +
                 ff.getName() + "'))\">";
         return str;
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        if (ff.getValue() != null && !ff.getValue().equals("")) {
            // LogUtil.getLog(getClass()).info("StrUtil.toInt(v)=" + StrUtil.toInt(v));
            FormDAO fdao = getFormDAOOfOrder(StrUtil.toInt(ff.getValue()));
            // v = fdao.getFieldValue("find_date");
            String visualPath = "";
            if (!Global.virtualPath.equals(""))
                visualPath = "/" + Global.virtualPath;
            v = "<a href=\"" + visualPath + "/sales/customer_sales_chance_show.jsp?customerId=" + fdao.getFieldValue("customer") + "&parentId=" + fdao.getFieldValue("customer") +  "&id=" + fdao.getId() + "&formCodeRelated=sales_order&formCode=sales_customer&isShowNav=1\" target=\"_blank\">" + fdao.getFieldValue("code") + "</a>";

        }
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
     }

     /**
      * 获取用来保存宏控件原始值的表单中的HTML元素中保存的值，生成用以给控件赋值的脚本
      * @return String
      */
     public String getSetCtlValueScript(HttpServletRequest request,
                                        IFormDAO IFormDao, FormField ff,
                                        String formElementId) {
         // 如果为空，则说明是添加记录
         if (StrUtil.getNullStr(ff.getValue()).equals("")) {
             return "";
         } else {
             return super.getSetCtlValueScript(request, IFormDao, ff,
                                               formElementId);
         }
    }

    public String getControlType() {
        return "";
    }

    public String getControlValue(String userName, FormField ff) {
        return "";
    }

    public String getControlText(String userName, FormField ff) {
        return "";
    }

    public String getControlOptions(String userName, FormField ff) {
        return "";
    }

}
