package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.WorkflowDb;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.FormDb;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.base.IFormDAO;
import cn.js.fan.util.ParamUtil;

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
public class ProductListWinCtl extends AbstractMacroCtl {
    public ProductListWinCtl() {
    }

    public FormDAO getFormDAOOfProduct(int id) {
        FormDb fd = new FormDb();
        fd = fd.getFormDb("sales_product_info");
        FormDAO fdao = new FormDAO(id, fd);
        return fdao;
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {

        String str = "";
        String v = "";
        if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
            // LogUtil.getLog(getClass()).info("StrUtil.toInt(ff.getValue())=" + StrUtil.toInt(ff.getValue()));
            FormDAO fdao = getFormDAOOfProduct(StrUtil.toInt(ff.getValue()));
            // LogUtil.getLog(getClass()).info("mobile=" + fdao.getFieldValue("mobile"));
            v = fdao.getFieldValue("product_name");
         }

        str += "<input id='" + ff.getName() + "_realshow' name='" + ff.getName() + "_realshow' value='" + v +
                "' size=15 readonly>";
        str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='' type='hidden'>";

        str += "&nbsp;<input type=button class=btn value='选择' onClick='openWinProductList(" + ff.getName() + ")'>";
        return str;
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
            FormDAO fdao = getFormDAOOfProduct(StrUtil.toInt(v));
            String str = fdao.getFieldValue("product_name");
            return str;
        }
        else
            return "";
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
            FormDAO fdao = getFormDAOOfProduct(StrUtil.toInt(ff.getValue()));
            v = fdao.getFieldValue("product_name");
        }
        return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','" + v + "');\n";
     }

     /**
      * 用于nesttable双击单元格编辑时ajax调用
      * @param request HttpServletRequest
      * @param oldValue String 单元格原来的真实值 （如product的ID）
      * @param oldShowValue String 单元格原来的显示值（如product的名称）
      * @param objId String 单元格原来的显示值的input输入框的ID
      * @return String
      */
     @Override
     public String ajaxOnNestTableCellDBClick(HttpServletRequest request, String formCode, String fieldName,
                                              String oldValue,
                                              String oldShowValue, String objId) {

         String str = "";
         // 注意下面三行的顺序不能变
         str += "<input id=\"" + objId + "_realshow\" size=\"10\" readonly name=\"" + objId + "_realshow\" value=\"" + oldShowValue + "\">";
         str += "<input type=\"hidden\" id=\"" + objId + "\" name=\"" + objId + "\" value=\"" + oldValue + "\">";
         str += "<input type=\"button\" class=btn value=\"...\" onclick=\"openWinProductList(" + objId + ")\">";

         return str;
     }

     /**
      * 用于visual可视化模块处理
      * @param ff FormField
      * @param fu FileUpload
      * @param fd FormDb
      * @return Object
      */
     @Override
     public Object getValueForCreate(FormField ff, FileUpload fu, FormDb fd) {
         return ff.getValue();
    }

    public String getControlType() {
         return "buttonSelect";
     }

     public String getControlValue(String userName, FormField ff) {
    	 if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
 			return ff.getValue();
 		}
 		return "";
     }

     public String getControlText(String userName, FormField ff) {
    		String v = "";
    		if (!StrUtil.getNullStr(ff.getValue()).equals("")) {
    			FormDAO fdao = getFormDAOOfProduct(StrUtil.toInt(ff.getValue()));
    			v = fdao.getFieldValue("product_name");
    		}
    		return v;
     }
     
     
     /**
      * 产品选择
      */
     public String getControlOptions(String userName, FormField ff) {
 		String sql = "select id from form_table_sales_product_info";
		FormDAO fdao = new FormDAO();
		JSONArray options = new JSONArray();
		try {
			Vector vector = fdao.list("sales_product_info", sql);
			if(vector != null && vector.size()>0){
				Iterator ir = null;
				ir = vector.iterator();
				while(ir.hasNext()){
					fdao = (FormDAO)ir.next();
					JSONObject product = new JSONObject();
					product.put("value",fdao.getId());
					product.put("name",fdao.getFieldValue("product_name"));//产品名称
					options.put(product);
				}
			}
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(SalesCustomerListWinCtl.class).error(e.getMessage());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			LogUtil.getLog(SalesCustomerListWinCtl.class).error(e.getMessage());
		}
		return options.toString();
     }

}
