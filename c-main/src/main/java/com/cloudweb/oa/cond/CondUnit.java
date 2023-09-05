package com.cloudweb.oa.cond;

import com.redmoon.oa.flow.FormDb;
import com.redmoon.oa.visual.ModuleSetupDb;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

public class CondUnit {

    public static final String FIELD_CWS_STATUS = "cws_status";
    public static final String FIELD_CWS_FLAG = "cws_flag";

    String fieldName;
    String fieldTitle;
    String html;
    String script;
    /**
     * 对应于SQLBuilder中的COND_TYPE_***
     */
    String condType;

    HttpServletRequest request;
    ModuleSetupDb msd;
    FormDb fd;
    Map<String, String> checkboxGroupMap;
    ArrayList<String> dateFieldNamelist;
    String queryValue;

    public CondUnit(HttpServletRequest request, ModuleSetupDb msd, FormDb fd, String fieldName, String fieldTitle, String condType, Map<String, String> checkboxGroupMap, ArrayList<String> dateFieldNamelist, String queryValue) {
        this.request = request;
        this.msd = msd;
        this.fd = fd;
        this.fieldName = fieldName;
        this.fieldTitle = fieldTitle;
        this.condType = condType;
        this.checkboxGroupMap = checkboxGroupMap;
        this.dateFieldNamelist = dateFieldNamelist;
        this.queryValue = queryValue;

        init();
    }

    public void init() {

    }

    public String getFieldTitle() {
        return fieldTitle;
    }

    public void setFieldTitle(String fieldTitle) {
        this.fieldTitle = fieldTitle;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
