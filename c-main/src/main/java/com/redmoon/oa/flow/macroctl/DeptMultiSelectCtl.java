package com.redmoon.oa.flow.macroctl;

import cn.js.fan.util.StrUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.flow.FormField;
import com.redmoon.oa.person.UserDb;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: qcg
 * @Description:
 * @Date: 2017/12/6 14:59
 */
public class DeptMultiSelectCtl extends AbstractMacroCtl {
    public DeptMultiSelectCtl() {
    }

    /**
     * 此方法用于 添加or编辑页面时 拼成html返回到td中，此处只需拼成一个input和选择按钮
     * @param request HttpServletRequest
     * @param ff FormField
     * @return
     * 返回值样式:<input id="" name="" value="" size=20 readonly><input id="" name="" value="" type="hidden"><input 选择>
     */
    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        /*//页面真正显示的input
        String html = "<input id='" + ff.getName()+"_realshow' name='" +ff.getName()+"_realshow' value='' size=20 readonly />";
        //隐藏input，用于存放实际deptcode
        html += "<input id='"+ff.getName()+"' name='" +ff.getName()+"' value='' type='hidden' />";
        //String html = "<input id='cssss' name='cssss' value='' type='hidden' />";
        //生成选择按钮
        html += "<input id='"+ff.getName()+ "_btn' type='button' class='btn' value='选择' onclick='openWinDeptsSelect(o(\"" + ff.getName()+"\"))' />";
        return html;*/
        String str = "";
        String deptNames = "";
        String v = StrUtil.getNullStr(ff.getValue());
        if(!"".equals(v)){
            DeptDb dd = new DeptDb();

            String[] fields = v.split(",");
            for (String deptCode:fields) {
                dd = dd.getDeptDb(deptCode);
                String deptName = dd.getName();
                if ("".equals(deptNames)){
                    deptNames = deptName;
                }else{
                    deptNames += "," + deptName;
                }
            }
        }
        str += "<div class='user_group_box'>";
        str += "<input id='" + ff.getName() + "_realshow' name='" + ff.getName() + "_realshow" +
                "' readonly style='float:left; width:" + ff.getCssWidth() + "' value='" + deptNames + "' />";
        str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='" + v + "' type='hidden' />";
        str += "<div id='" + ff.getName() + "_btn' class='user_group_btn' onclick='openWinDeptsSelect(o(\"" + ff.getName() + "\"))'></div>";
        str += "</div>";

        str += "<script>";
        str += "$('#" + ff.getName() + "_btn').hover(\n";
        str += "function() {$('#" + ff.getName() + "_btn').toggleClass('user_group_btn_hover');},\n";
        str += "function() {$('#" + ff.getName() + "_btn').toggleClass('user_group_btn_hover');}\n";
        str += ");\n";
        str += "</script>";

        return str;
    }

    @Override
    public String getControlType() {
        return "text";
    }

    @Override
    public String getControlOptions(String s, FormField formField) {
        return "";
    }

    @Override
    public String getControlValue(String s, FormField formField) {
        return StrUtil.getNullStr(formField.getValue());
    }

    @Override
    public String getControlText(String s, FormField ff) {
        return "";
    }

    /**
     * 显示在列表中的值，此处参照单部门选择框，只是显示样式不同，需要对数据
     * 做处理,与多用户选择框相似
     * @param request HttpServletRequest
     * @param ff FormField
     * @param fieldValue String 格式：a,b
     * @return
     */
    @Override
    public String converToHtml(HttpServletRequest request, FormField ff, String fieldValue) {
        String v = StrUtil.getNullStr(fieldValue);
        if(!"".equals(v)){
            DeptDb dd = new DeptDb();
            StringBuffer deptNames = new StringBuffer();
            String[] fields = v.split(",");
            for (String deptCode:fields) {
                dd = dd.getDeptDb(deptCode);
                String deptName = dd.getName();
                
                StrUtil.concat(deptNames, ",", deptName);
            }
            return deptNames.toString();
        }else{
            return "";
        }
    }
    

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        String v = "";
        if (ff.getValue() != null && !"".equals(ff.getValue())) {
            DeptDb dd = new DeptDb();
        	String[] ary = StrUtil.split(ff.getValue(), ",");
            for (String s : ary) {
                dd = dd.getDeptDb(s);
                if ("".equals(v)) {
                    v = dd.getName();
                } else {
                    v += "," + dd.getName();
                }
            }
        }
        String str = "$('#" + ff.getName() + "_btn').hide();\n";
        return str + "ReplaceCtlWithValue('" + ff.getName() + "_realshow', '" + ff.getType() + "','" + v + "');\n";
     }    
    
    @Override
    public String getDisableCtlScript(FormField ff, String formElementId) {
        String realName = "";
        if (ff.getValue() != null && !"".equals(ff.getValue())) {
        	String[] ary = StrUtil.split(ff.getValue(), ",");
            DeptDb dd = new DeptDb();
            for (String s : ary) {
                dd = dd.getDeptDb(s);
                if ("".equals(realName)) {
                    realName = dd.getName();
                } else {
                    realName += "," + dd.getName();
                }
            }
        }

        String str = "DisableCtl('" + ff.getName() + "', '" + ff.getType() +
                     "','" + realName + "','" + ff.getValue() + "');\n";
        str += "DisableCtl('" + ff.getName() + "_realshow', '" + ff.getType() +
                "','" + "" + "','" + ff.getValue() + "');\n";
        str += "if (o('" + ff.getName() + "_btn')) o('" + ff.getName() + "_btn').outerHTML='';";
        return str;
    }    
}
