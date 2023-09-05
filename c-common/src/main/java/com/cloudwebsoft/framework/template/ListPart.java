package com.cloudwebsoft.framework.template;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.db.Paginator;

/**
 * <p>Title: 列表型标签</p>
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
public class ListPart implements ITemplate {
    public static final String TOP = "-1"; // ROOT 的父节点为TOP
    public static final String ROOT = "root";

    public HashMap properties;
    public List steps = null;

    public ListPart() {

    }

    public ListPart(String parentName, String name) {
        this.parentName = parentName;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getName() {
        return name;
    }

    public String getParentName() {
        return parentName;
    }

    public void addStep(ITemplate step) {
        if (steps == null) {
            steps = new ArrayList();
        }
        steps.add(step);
    }

    /**
     * 解析属性(len=10, format=yyyy-MM-dd HH:mm:ss)
     * @param propsStr String
     */
    public void parseProps(String propsStr) {
        String[] propPairs = StrUtil.split(propsStr, ",");
        LogUtil.getLog(getClass()).info("parseProps:" + propsStr + " propPairs=" + propPairs);
        if (propPairs==null)
            return;
        int len = propPairs.length;
        for (int i=0; i<len; i++) {
            String str = propPairs[i];
            String[] pair = StrUtil.split(str, "=");
            if (pair!=null) {
                props.put(pair[0].trim(), pair[1].trim());
            }
        }
    }

    public String toString(HttpServletRequest request, List param) {
        if (steps == null)
            return "";
        StringBuffer buf = new StringBuffer();
        int nSteps = steps.size();
        for (int i = 0; i < nSteps; i++) {
            ITemplate step = (ITemplate) steps.get(i);
            if (step instanceof ListPart) {
                ListPart listPart = (ListPart) step;
                String dynName = listPart.getName();
                buf.append("<!-- begin." + dynName + "-->\n");
                buf.append(listPart.toString(request, null));
                buf.append("<!-- end." + dynName + "-->\n");
            }
            else { // StaticPart or VariablePart or IgnoredPart
                buf.append(step.toString(request, null));
            }
        }

        return buf.toString();
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public Paginator paginator;
    private String name;
    private String parentName;
    public HashMap props = new HashMap();

}
