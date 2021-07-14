package com.cloudwebsoft.framework.template;

import com.cloudwebsoft.framework.util.BeanUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.StrUtil;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>Title: 子域型模板，用在循环体中</p>
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
public class FieldPart  implements ITemplate {
    String name;
    String parentName;
    String subField; // 子域，如：paginator.total

    HashMap props = new HashMap();

    public FieldPart(String fieldString) {
        Pattern varNamePat2 = Pattern.compile(
                "\\@([^\\(\\.]+)(\\.([^\\(]+))?(\\((.*?)\\))?",
             Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
         Matcher m = varNamePat2.matcher(fieldString);
         if (m.find()) {
             name = m.group(1);
             if (m.groupCount()>=3) {
                 subField = StrUtil.getNullStr(m.group(3));
             }
             if (m.groupCount()>=5) {
                 String propStr = StrUtil.getNullStr(m.group(5));
                 parseProps(propStr);
             }
         }
    }

    public String toString(HttpServletRequest request, List param) {
        return name;
    }

    public Object write(Object obj) {
        BeanUtil bu = new BeanUtil();
        if (subField.equals("")) {
            // LogUtil.getLog(getClass()).info("write:" + obj.getClass() + " name=" + name);
            return VarPart.format(bu.getProperty(obj, name), props);
        }
        else {
            return VarPart.format(bu.getProperty(bu.getProperty(obj, name), subField), props);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentName() {
        return parentName;
    }

    /**
     * 解析属性(len=10, format=yyyy-MM-dd HH:mm:ss)
     * @param propsStr String
     */
    public void parseProps(String propsStr) {
        // LogUtil.getLog(getClass()).info("parseProps=" + propsStr);
        String[] propPairs = StrUtil.split(propsStr, ",");
        if (propPairs==null)
            return;
        int len = propPairs.length;
        for (int i=0; i<len; i++) {
            String str = propPairs[i];
            String[] pair = StrUtil.split(str, "=");
            if (pair!=null) {
                props.put(pair[0].trim().toLowerCase(), pair[1].trim());
            }
        }
    }

    public HashMap getProps() {
        return props;
    }
}
