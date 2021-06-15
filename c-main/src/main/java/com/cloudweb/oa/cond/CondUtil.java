package com.cloudweb.oa.cond;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import com.redmoon.oa.flow.FormDb;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Map;

public class CondUtil {
    public static CondUnit getCondUnit(HttpServletRequest request, FormDb fd, String fieldName, String fieldTitle, String condType, Map<String, String> checkboxGroupMap, ArrayList<String> list ) throws ErrMsgException {
        String queryValue = ParamUtil.get(request, fieldName);
        if ("cws_status".equals(fieldName)) {
            return new CwsStatusCondUnit(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
        }
        else if ("cws_flag".equals(fieldName)) {
            return new CwsFlagCondUnit(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
        }
        else if ("ID".equals(fieldName)) {
            return new IdCondUnit(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
        }
        else if ("flowId".equals(fieldName)) {
            return new FlowIdCondUnit(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
        }
        else if ("flow:begin_date".equals(fieldName) || "flow:end_date".equals(fieldName)) {
            return new DateCondUnit(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
        }
        else if ("cws_id".equals(fieldName)) {
            return new CwsIdCondUnit(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
        }
        else {
            return new FieldCondUnit(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, list, queryValue);
        }
    }

    public static Object[] getFieldTitle(FormDb fd, String fieldName, String fieldTitle) {
        String title = "";
        boolean sortable = true;

        if ("#".equals(fieldTitle)) {
            if (fieldName.startsWith("main:")) {
                String[] subFields = StrUtil.split(fieldName, ":");
                if (subFields.length == 3) {
                    FormDb subfd = new FormDb(subFields[1]);
                    title = subfd.getFieldTitle(subFields[2]);
                    sortable = false;
                }
            } else if (fieldName.startsWith("other:")) {
                String[] otherFields = StrUtil.split(fieldName, ":");
                if (otherFields.length == 5) {
                    FormDb otherFormDb = new FormDb(otherFields[2]);
                    String showFieldName = otherFields[4];
                    if (!"id".equalsIgnoreCase(showFieldName)) {
                        title = otherFormDb.getFieldTitle(showFieldName);
                    }
                    else {
                        title = "ID";
                    }
                    sortable = false;
                }
            } else if (fieldName.equals("cws_creator")) {
                title = "创建者";
            }
            else if (fieldName.equals("ID")) {
                fieldName = "CWS_MID"; // ModuleController中也作了同样转换
                title = "ID";
            }
            else if (fieldName.equals("cws_progress")) {
                title = "进度";
            }
            else if (fieldName.equals("cws_status")) {
                title = "状态";
            }
            else if (fieldName.equals("flowId")) {
                title = "流程号";
            }
            else if (fieldName.equals("cws_flag")) {
                title = "冲抵状态";
            }
            else if (fieldName.equals("colOperate")) {
                title = "操作";
            }
            else if (fieldName.equals("cws_create_date")) {
                title = "创建时间";
            }
            else if (fieldName.equals("flow_begin_date")) {
                title = "流程开始时间";
            }
            else if (fieldName.equals("flow_end_date")) {
                title = "流程结束时间";
            }
            else if (fieldName.equals("cws_id")) {
                title = "关联ID";
            }
            else {
                title = fd.getFieldTitle(fieldName);
            }
        }
        else {
            title = fieldTitle;
        }
        Object[] ary = new Object[2];
        ary[0] = title;
        ary[1] = sortable;
        return ary;
    }
}
