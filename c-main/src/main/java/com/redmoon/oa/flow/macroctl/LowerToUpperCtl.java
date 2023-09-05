package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;

import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysUtil;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.flow.FormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.kit.util.FileUpload;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 * <p>Title: 数字小写转换成大写</p>
 *
 * <p>Description: 默认值格式：小写输入框的编码</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class LowerToUpperCtl extends AbstractMacroCtl {
    public LowerToUpperCtl() {
        super();
    }

    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        String lowerFieldCode = ff.getDefaultValueRaw();

        String str = "<input name='" + ff.getName() + "' lowerFieldCode='" +
                     lowerFieldCode + "' value='' title='大写转换" + lowerFieldCode +
                     "'>";

        // 如果有任意的动作，都重新从小写框获取数据，并转换为大写
        String isLowerToUpperCtlJSWrited = (String) request.getAttribute(
                "isLowerToUpperCtlJSWrited");
        if (isLowerToUpperCtlJSWrited == null) {
            /*
                         str +=
                    "<script language=\"javascript\">";
                         str += "window.document.onclick = function() {";
                         str += "var dxary = [\"零\", \"壹\", \"贰\", \"叁\", \"肆\", \"伍\", \"陆\", \"柒\", \"捌\", \"玖\"];";

             str += "var controls = document.getElementsByTagName(\"input\");";
                         str += "for (var i=0; i<controls.length; i++) {";
                         str += "    if (controls[i].type==\"text\" && controls[i].getAttribute(\"lowerFieldCode\")!=null) {";
                         str += "        var lv = document.getElementById(controls[i].getAttribute(\"lowerFieldCode\")).value;";
                         str += "        if (lv!=\"\"){";
                         str += "          var v = parseInt(lv);";
             str += "          if (v!=\"undefined\" && !isNaN(v)) {";
                         str += "             controls[i].value = dxary[v];";
                         str += "          }";
                         str += "        }";
                         str += "    }";
                         str += "}";

                         str += "}";
                         str += "</script>";
             */

            SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
            /*str += "<script src='" + request.getContextPath() +
                    "/flow/macro/macro_lowertoupperctl.js" + "'></script>";*/
            str += "<script src=\"" + sysUtil.getPublicPath() + "/resource/js/macro/macro_lowertoupperctl.js\"></script>";

            request.setAttribute("isLowerToUpperCtlJSWrited", "y");
        }
        return str;
    }

    @Override
    public Object getValueForSave(FormField ff, int flowId, FormDb fd,
                                  FileUpload fu) {
        FormDAO fdao = new FormDAO(flowId, fd);
        fdao.load();

        String lowerFieldCode = ff.getDefaultValueRaw();
        String lowerValue = StrUtil.getNullStr(fu.getFieldValue(lowerFieldCode));
        if (lowerValue.equals(""))
            return "零";

        //if (StrUtil.toDouble(lowerValue, -65536) == -65536)
        try {
        	Double.parseDouble(lowerValue.trim());
        } catch (Exception e) {
            return "错";
        }

        LogUtil.getLog(getClass()).info("getValueForSave lowerValue=" +
                                        lowerValue);
        /*
        String[] ary = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String str = "";
        for (int i = 0; i < lowerValue.length(); i++) {
            int k = StrUtil.toInt("" + lowerValue.charAt(i));
            if (k >= 0 && k <= 9)
                str += ary[k];
            else if (lowerValue.charAt(i) == '.')
                str += "点";
        }
        return str;
        */

       return change(StrUtil.toDouble(lowerValue));

    }

    public Object getValueForCreate(FormField ff) {
        return "";
    }

    @Override
    public Object getValueForSave(FormField ff, FormDb fd, long formDAOId,
                                  FileUpload fu) {
        // ff参数来自于FormDAO中的doUpload，并不是来自于数据库，所以需从数据库中提取
        com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(
                formDAOId, fd);

        String lowerFieldCode = ff.getDefaultValueRaw();
        String lowerValue = fdao.getFieldValue(lowerFieldCode);
        if (lowerValue.equals(""))
            return "零";

        //if (StrUtil.toDouble(lowerValue, -65536) == -65536)
        try {
        	Double.parseDouble(lowerValue.trim());
        } catch (Exception e) {
            return "错";
        }

        /*
        String[] ary = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String str = "";
        for (int i = 0; i < lowerValue.length(); i++) {
            int k = StrUtil.toInt("" + lowerValue.charAt(i));
            if (k >= 0 && k <= 9)
                str += ary[k];
            else if (lowerValue.charAt(i) == '.')
                str += "点";
        }
        */

        return change(StrUtil.toDouble(lowerValue));
    }

    private static final String UNIT = "万仟佰拾亿仟佰拾万仟佰拾元角分";

    private static final String DIGIT = "零壹贰叁肆伍陆柒捌玖";

    private static final double MAX_VALUE = 9999999999999.99D;
    private static final double MIN_VALUE = -9999999999999.99D;

    public static String change(double v) {
        if (v < MIN_VALUE || v > MAX_VALUE)
            return "参数非法!";
        long l = Math.round(v * 100);
        if (l == 0)
            return "零元整";
        String strValue = Math.abs(l) + "";
        //i用来控制数
        int i = 0;
        //j用来控制单位
        int j = UNIT.length() - strValue.length();
        String rs = "";
        boolean isZero = false;
        for (; i < strValue.length(); i++, j++) {
            char ch = strValue.charAt(i);
            if (ch == '0') {
                isZero = true;
                if (UNIT.charAt(j) == '亿' || UNIT.charAt(j) == '万' ||
                    UNIT.charAt(j) == '元') {
                    rs = rs + UNIT.charAt(j);
                    isZero = false;
                }
            } else {
                if (isZero) {
                    rs = rs + "零";
                    isZero = false;
                }
                rs = rs + DIGIT.charAt(ch - '0') + UNIT.charAt(j);
            }
        }

        if (!rs.endsWith("分")) {
            rs = rs + "整";
        }
        rs = rs.replaceAll("亿万", "亿");
        if (l < 0) {
        	rs = "负" + rs;
        }
        return rs;
    }

    public String getControlType() {
         return "text";
     }

     public String getControlValue(String userName, FormField ff) {
    	 
		 if(ff.getValue()!=null && !ff.getValue().equals("")){
			 if(ff.getValue().equals(ff.getDefaultValueRaw())){
				 return "";
			 }
			 return ff.getValue();
			 
		 }else{
			 return "";
		 }
     }

     public String getControlText(String userName, FormField ff) {
    	 if(ff.getValue()!=null && !ff.getValue().equals("")){
			 if(ff.getValue().equals(ff.getDefaultValueRaw())){
				 return "";
			 }
			 return ff.getValue();
			 
		 }else{
			 return "";
		 }
     }

     public String getControlOptions(String userName, FormField ff) {
         return "";
     }

}
