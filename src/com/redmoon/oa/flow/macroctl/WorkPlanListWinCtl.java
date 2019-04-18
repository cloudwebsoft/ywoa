package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.FormDAO;
import cn.js.fan.util.ParamUtil;
import com.redmoon.oa.workplan.WorkPlanDb;
import cn.js.fan.util.*;
import com.redmoon.oa.dept.*;
import com.redmoon.oa.person.*;
import java.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WorkPlanListWinCtl extends AbstractMacroCtl {
    public WorkPlanListWinCtl() {
           super();
    }

    /**
     * 当report时，取得用来替换控件的脚本
     * @param ff FormField
     * @return String
     */
    public String getReplaceCtlWithValueScript(FormField ff) {
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType() + "','');\n";
    }

     public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
       String defaultValue = ff.getDefaultValue();
       String v = "";
       String str = "";
       int wid = 0;
       String flowId = (String)request.getAttribute("cwsId");
       if (request.getAttribute("macro_js_workplan_list")==null) {
           str = "<script src='" + request.getContextPath() + "/flow/macro/macro_js_workplan_list.jsp?flowId="+flowId+"'></script>";
           request.setAttribute("macro_js_workplan_list", "macro_js_workplan_list");
       }

       if (ff.getValue() != null && !ff.getValue().equals("")) {
           String workPlanIds[] = ff.getValue().split(",");
           for (int i=0; i<workPlanIds.length; i++) {
               String workPlanId = workPlanIds[i];
           // for (String workPlanId : workPlanIds) {
               wid = StrUtil.toInt(workPlanId, -1);

               WorkPlanDb wpd = new WorkPlanDb();
               wpd = wpd.getWorkPlanDb(wid);
               String title = wpd.getTitle();
               String sendDate = DateUtil.format(wpd.getEndDate(), "yyyy-MM-dd");
               UserMgr um = new UserMgr();
               String[] principalAry = wpd.getPrincipals();
               int len = 0;
               if (principalAry!=null)
                   len = principalAry.length;
               String principals = "";
               for (int y = 0; y < len; y++) {
                   if (principalAry[y].equals(""))
                       continue;
                   UserDb user = um.getUserDb(principalAry[y]);
                   if (principals.equals("")) {
                       principals = user.getRealName();
                   } else {
                       principals += "，" + user.getRealName();
                   }
               }

               int iDays = DateUtil.datediff(wpd.getEndDate(), new Date());
               if(iDays<0){
                   iDays = 0;
               }

               String s = "<div id='wp_" + wid + "'>" + "<a href='javascript:;' onclick=\"addTab('" + title + "', 'workplan/workplan_show.jsp?id=" + wid +
                          "')\">" + principals + "：" + title + "，期限"  + sendDate +
                          "，剩余"+iDays+"天"+"</a>"+"&nbsp;&nbsp;" + "<a style='color:red; font-size:14px; padding-left:5px; cursor:pointer' href='javascript:void(0)' onclick=\"removeWorkplanDiv('" +
                          wid + "', '" + ff.getName() + "')\">×</a>" + "</div>";

               if (v.equals("")) {
                   v = s;
               } else {
                   v += s;
               }
           }
       }

       str += "<div name='" + ff.getName() + "_realshow' id='" + ff.getName() +
                                "_realshow' style='font-size:14px;font-family:'宋体'' >" + v + "</div>";
                   str += "<input id='" + ff.getName() + "' name='" + ff.getName() + "' value='"+StrUtil.getNullStr(ff.getValue())+"' type='hidden' style='display:none'>";
       str +="<input id='"+ff.getName()+"_btn' class='btn' type=button class=btn value='添加计划' onClick='openWinWorkPlanList(" +
                ff.getName() + ")'>";
       return str;
   }

   public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request, FormField ff) {
       if (ff.getValue()!=null) {
           if (ff.getValue().equals(ff.getDefaultValue())) {
               ff.setValue("");
           }
       }
       else
           ff.setValue("");
       return FormField.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
   }


   public String getDisableCtlScript(FormField ff, String formElementId) {
       return "if (o('" + ff.getName() + "_btn')) o('" + ff.getName() + "_btn').outerHTML='';";
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
