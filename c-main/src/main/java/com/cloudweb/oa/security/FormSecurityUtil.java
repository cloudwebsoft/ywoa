package com.cloudweb.oa.security;

import com.cloudwebsoft.framework.security.*;
import com.redmoon.kit.util.FileUpload;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormSecurityUtil {

    public static ProtectFormUnit getFilterUnit(String formCode) {
        ProtectFormConfig pc = new ProtectFormConfig();
        // 如果被排除，则不再检测
        Vector<ProtectFormUnit> vun = pc.getAllUnit();
        Iterator<ProtectFormUnit> irun = vun.iterator();
        while (irun.hasNext()) {
            ProtectFormUnit pu = irun.next();
            // 如果规则中formCode为*，则表示适配所有的表
            if (pu.getFormCode().equals("*")) {
                return pu;
            }
            if (pu.getType() == ProtectUnit.TYPE_INCLUDE) {
                if (formCode.indexOf(pu.getFormCode())!=-1) {
                    return pu;
                }
            }
            else {
                // 正则
                Pattern pattern = Pattern.compile(pu.getFormCode(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(formCode);
                if (matcher.find()) {
                    return pu;
                }
            }
        }
        return null;
    }

    /**
     * 是否排除字段
     * @param protectFormUnit
     * @param field
     * @return
     */
    public static boolean isExcludeField(ProtectFormUnit protectFormUnit, String field) {
        return protectFormUnit.getFields().contains(field);
    }

    public static void filter(String formCode, FileUpload fileUpload) throws ProtectXSSException, ProtectSQLInjectException {
        ProtectFormUnit protectFormUnit = getFilterUnit(formCode);
        if (protectFormUnit != null) {
            // 如果规则中fields为*，则表示排除全部字段
            if (protectFormUnit.getFields().contains("*")) {
                return;
            }
        }

        Enumeration en = fileUpload.getFields();
        while(en.hasMoreElements()) {
            String field = (String) en.nextElement();
            // 辅助字段不检测
            if (field.startsWith("cws_textarea_")) {
                continue;
            }
            if (protectFormUnit!=null && !isExcludeField(protectFormUnit, field)) {
                SecurityUtil.filter(field, fileUpload.getFieldValue(field));
            }
        }
    }
}
