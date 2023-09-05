package com.redmoon.oa.flow.macroctl;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.oa.flow.FormField;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.base.IFormDAO;
import java.util.Enumeration;
import com.redmoon.oa.flow.WorkflowMgr;
import com.redmoon.oa.flow.WorkflowParams;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.flow.FormDb;

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
public class SearchHelperCtl extends AbstractMacroCtl {
    public SearchHelperCtl() {
           super();
    }

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
       String defaultValue = ff.getDefaultValue();
       String str = "";
       String titleField = defaultValue;

       //String workflowParams = request.getAttribute("workflowParams").toString();
       str = "<textarea id='" + ff.getName() + "' name='" + ff.getName() + "' style='display:none' title='查询内容' ></textarea>";
       return str;
   }

   @Override
   public String getOuterHTMLOfElementsWithRAWValueAndHTMLValue(HttpServletRequest request, FormField ff) {
       if (ff.getValue()!=null && ff.getValue().equals(ff.getDefaultValue())) {
           ff.setValue("");
       }
       return FormField.getOuterHTMLOfElementsWithRAWValueAndHTMLValue(request, ff);
   }

/*
   public String getSetCtlValueScript(HttpServletRequest request, IFormDAO IFormDao, FormField ff, String formElementId) {
       if (ff.getValue().equals(ff.getDefaultValue())) {
           ff.setValue("");
           return "setCtlValue('" + ff.getName() + "', '" + ff.getType() + "', '');";
       }
       else
           return FormField.getSetCtlValueScript(request, IFormDao, ff, formElementId);
    }*/
     @Override
     public Object getValueForSave(FormField ff, int flowId, FormDb fd, FileUpload fu) {
         Enumeration en = fu.getFields();
         String allValue="";
         String value = "";
         while(en.hasMoreElements()){
             String key = (String) en.nextElement();
             if(key.equals(ff.getName())||key.equals("cws_textarea_"+ff.getName())){
                 continue;
             }
             try {
                 value = fu.getFieldValue(key);
             } catch (ClassCastException e) {
                 String[] array = fu.getFieldValues(key);
                 for (int i = 0; i < array.length; i++) {
                     if(value.equals("")){
                         value =array[i];
                     }else{
                         value +="，"+array[i];
                     }
                 }
             }
             if(value==null){
                continue;
             }
                 if (allValue.equals("")) {
                     allValue = value;
                 } else {
                     allValue += "," + value;
                 }
             }
         return allValue;
     }

     /**
          * 当report时，取得用来替换控件的脚本
          * @param ff FormField
          * @return String
          */
         public String getReplaceCtlWithValueScript(FormField ff) {
             return "ReplaceCtlWithValue('" + ff.getName() +"', '" + ff.getType() + "','');\n";
      }


    public Object getValueForCreate(FormField ff) {
        return "";
    }

    @Override
    public String getHideCtlScript(FormField formField, String string) {
        return "";
    }

    public String getDisableCtlScript(FormField ff, String formElementId) {
        // String str = super.getDisableCtlScript(ff, formElementId);
        // str += "o('" + ff.getName() + "_btn').outerHTML='';";
        return "";
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
