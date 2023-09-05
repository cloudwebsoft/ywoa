package com.redmoon.oa.flow;

import cn.js.fan.util.*;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.*;
import java.util.*;
import java.lang.*;
import cn.js.fan.db.SQLFilter;
import com.redmoon.oa.flow.macroctl.MacroCtlUnit;
import com.redmoon.oa.flow.macroctl.MacroCtlMgr;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FormQueryConditionMgr {
    public FormQueryConditionMgr() {
    }

    public boolean create(FormQueryDb aqd) throws ErrMsgException {
        FormQueryConditionDb aqcd = new FormQueryConditionDb();
        boolean re = false;
        int i = 0;
        String conditionFieldCode = "", conditionSign = "", conditionValue = "", conditionType = "", inputValue = "";
        String compareType = "";
        String[] conditionFieldCodeArr = StrUtil.split(aqd.getConditionFieldCode(), "\\$");
        // 如果没有条件
        if (conditionFieldCodeArr==null)
            return true;

        while (i < conditionFieldCodeArr.length) {
            if (conditionFieldCodeArr[i].trim().equals("")) {
                // 第一个条件在切分后可能为空
                // 如：getConditionFieldCode为以下值时，因为开头的字母即为$
                // $AUI.establishment|=|'0.0'|SELECTED|0.0$AUI.real_name|like|'%%'|INPUT|$AUI.sex|like|'%%'|INPUT|$AUI.USER_NAME|=|AUI.USER_NAME|SYSTEM|null|$AUI.USER_NAME|=|ASI.USER_NAME|SYSTEM|null|$AUI.USER_NAME|=|AREW.USER_NAME|SYSTEM|null|$AUI.USER_NAME|=|ARES.USER_NAME|SYSTEM|null|$AUI.USER_NAME|=|API.USER_NAME|SYSTEM|null|
                i++;
                continue;
            }

            String[] ary = conditionFieldCodeArr[i].split("\\|");
            conditionFieldCode = ary[0];
            conditionSign = ary[1];
            conditionValue = ary[2];
            conditionType = ary[3];
            if (ary.length >= 5)
                inputValue = ary[4];
            else
                inputValue = "";
            if (ary.length >= 6)
                compareType = ary[5];
            else
                compareType = "";

            aqcd.setQueryId(aqd.getId());
            aqcd.setConditionFieldCode(conditionFieldCode);
            aqcd.setConditionSign(conditionSign);
            aqcd.setConditionValue(conditionValue);
            aqcd.setConditionType(conditionType);
            aqcd.setInputValue(inputValue);
            aqcd.setCompareType(StrUtil.toInt(compareType, FormQueryConditionDb.COMPARE_TYPE_NONE));
            re = aqcd.create();
            i++;
        }
        return re;
    }


    public boolean del(int queryId) throws ErrMsgException {
        boolean re = true;
        FormQueryConditionDb aqcd = new FormQueryConditionDb();
        Vector vt = aqcd.list(FormSQLBuilder.getFormQueryCondition(queryId));
        Iterator ir = vt.iterator();
        while (ir != null && ir.hasNext()) {
            aqcd = (FormQueryConditionDb) ir.next();
            re = aqcd.del();
        }
        return re;
    }

    public String getQueryCondition(HttpServletRequest request) throws ErrMsgException {
        int i = 0;
        String fieldCode = "", conditionSign = "", conditionValue = "",
                                                                    saveConditionFieldCode = "", fromDate = "",
                                                                                                            toDate = "";
        String formCode = ParamUtil.get(request, "formCode");

        String conditionFieldCodeStr = ParamUtil.get(request,
                                                     "conditionFieldCodeStr");
        String[] conditionFieldCodeArr = StrUtil.split(conditionFieldCodeStr, ",");

        FormDb fd = new FormDb();
        fd = fd.getFormDb(formCode);

        int len = 0;
        if (conditionFieldCodeArr!=null)
            len = conditionFieldCodeArr.length;

        MacroCtlUnit mu = null;
        MacroCtlMgr mm = new MacroCtlMgr();

        while (i < len) {
            fieldCode = conditionFieldCodeArr[i];

            FormField ff = fd.getFormField(fieldCode);
            String macroCode = "";
            if (ff.getType().equals(FormField.TYPE_MACRO)) {
                mu = mm.getMacroCtlUnit(ff.getMacroType());
                macroCode = mu.getCode();
            }

            // 处理日期字段
            if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
                conditionSign = ParamUtil.get(request,
                                              formCode + "_" + fieldCode +
                                              "_COND");
                switch (Integer.parseInt(conditionSign)) {

                case (0):

                    // 精确段时间
                    fromDate = ParamUtil.get(request,
                                             formCode + "_" + fieldCode +
                                             "_FROMDATE");
                    toDate = ParamUtil.get(request,
                                           formCode + "_" + fieldCode +
                                           "_TODATE");
                    /*
                    saveConditionFieldCode += conditionFieldCodeArr[i] +
                            "|>=|TO_DATE(" + StrUtil.sqlstr(fromDate) + ",'YYYY-MM-DD')|" +
                            "SELEDATE" + "|" + fromDate;
                    saveConditionFieldCode += "$" + conditionFieldCodeArr[i] +
                            "|<=|TO_DATE(" + StrUtil.sqlstr(toDate) + ",'YYYY-MM-DD')|" +
                            "SELEDATE" + "|" + toDate;
                    */

                    saveConditionFieldCode += conditionFieldCodeArr[i] +
                            "|>=|" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd") + "|" +
                            "SELEDATE" + "|" + fromDate;
                    saveConditionFieldCode += "$" + conditionFieldCodeArr[i] +
                            "|<=|" + SQLFilter.getDateStr(toDate, "yyyy-MM-dd") + "|" +
                            "SELEDATE" + "|" + toDate;

                    break;

                default:
                    // 精确点时间
                    conditionSign = ParamUtil.get(request,
                                                  formCode + "_" + fieldCode + "_SIGN");
                    conditionValue = ParamUtil.get(request,
                                                   formCode + "_" + fieldCode + "_DATE");
                    /*
                    saveConditionFieldCode += conditionFieldCodeArr[i] +
                            "|" + conditionSign + "|TO_DATE(" +
                            StrUtil.sqlstr(conditionValue) + ",'YYYY-MM-DD')|" + "SELEDATE"
                            + "|" + conditionValue;
                    */
                    saveConditionFieldCode += conditionFieldCodeArr[i] +
                            "|" + conditionSign + "|" + SQLFilter.getDateStr(conditionValue, "yyyy-MM-dd") + "|" + "SELEDATE"
                            + "|" + conditionValue;

                    break;
                }
            }
            // 处理列表字段
            else if (ff.getType().equals(FormField.TYPE_SELECT) || ff.getType().equals(FormField.TYPE_LIST) || macroCode.equals("macro_flow_select") || macroCode.equals("macro_dept_select") || macroCode.equals("macro_my_dept_select")) {
                String[] conditionValueArr = ParamUtil.getParameters(request,
                                                                     formCode + "_" + fieldCode);
                if (conditionValueArr == null) {
                    throw new ErrMsgException(formCode + "_" + fieldCode + "不能为空！");
                }

                // 检查是否为比较型，如 乡科级正职以上
                boolean isComp = false;
                int comp = ParamUtil.getInt(request, formCode + "_" + fieldCode + "_COMPARE", 0);
                if (comp != 0) {
                    // 当选择了比较方式时，即使选了多个option，也只取第一个
                    saveConditionFieldCode += conditionFieldCodeArr[i] +
                            "|=|" + StrUtil.sqlstr(conditionValueArr[0]) +
                            "|" + "SELECTED" + "|" + conditionValueArr[0] + "|" + comp;
                    isComp = true;
                }

                if (!isComp) {
                    int k = 0;
                    while (k < conditionValueArr.length) {
                        if (conditionValueArr[k].equals("")) {
                            saveConditionFieldCode += conditionFieldCodeArr[i] +
                                    "|is|null|" + "SELECTED" + "|null";
                        } else {
                            saveConditionFieldCode += conditionFieldCodeArr[i] +
                                    "|=|" + StrUtil.sqlstr(conditionValueArr[k]) +
                                    "|" + "SELECTED" + "|" + conditionValueArr[k];
                        }
                        if (k < conditionValueArr.length - 1) {
                            saveConditionFieldCode += "$";
                        }
                        k++;
                    }
                }
            }
            else {
                conditionSign = ParamUtil.get(request,
                                              formCode + "_" + fieldCode + "_SIGN");
                conditionValue = ParamUtil.get(request,
                                               formCode + "_" + fieldCode);
                if (conditionSign.equals("like")) {
                    saveConditionFieldCode += conditionFieldCodeArr[i] +
                            "|" + conditionSign + "|" +
                            StrUtil.sqlstr("%" + conditionValue + "%") +
                            "|" + "INPUT" + "|" + conditionValue;
                } else {
                    String val = conditionValue;
                    if (ff.getFieldType()==FormField.FIELD_TYPE_VARCHAR || ff.getFieldType()==FormField.FIELD_TYPE_TEXT) {
                        val = StrUtil.sqlstr(conditionValue);
                    }
                    else {
                    	// 如果是非字符串型，则可能为整型、浮点型或双精度型等，在条件中为空没有意义
                        if (val.equals("")) {
                            i++;
                            // continue;
                        	throw new ErrMsgException(ff.getTitle() + " 不是字符型字段，设为条件时不能为空！");
                        }
                    }
                    saveConditionFieldCode += conditionFieldCodeArr[i] +
                            "|" + conditionSign + "|" +
                            val + "|" + "INPUT" + "|" + conditionValue;
                }
            }

            if (i < conditionFieldCodeArr.length - 1) {
                saveConditionFieldCode += "$";
            }

            i++;
        }

        return saveConditionFieldCode;
    }

}
