package com.redmoon.blog.template;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.template.*;
import com.redmoon.blog.*;

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
public class ListBlogDirTemplateImpl extends ListPart {
    String query = "";

    public ListBlogDirTemplateImpl() {
    }

    public String toString(HttpServletRequest request, List param) {
        if (steps == null)
            return "";

        // System.out.println(getClass() + " dirCode=" + dirCode);

        String parentCode = "";

        if (param==null) {
            parentCode = StrUtil.getNullStr((String) props.get("parentCode"));

            if (parentCode != null) {
                if (parentCode.startsWith("request")) {
                    int p = parentCode.indexOf(".");
                    if (p != -1) {
                        String para = parentCode.substring(p + 1);
                        // LogUtil.getLog(getClass()).info("param=" + param + " request=" + request);
                        parentCode = ParamUtil.get(request, para);
                    }
                } else if (parentCode.equals("outerParentCode")) {
                    this.getParentName();
                }
            }
        }
        else {
            // 如果是内嵌的循环
            Vector v = (Vector)param;
            Leaf parentLf = (Leaf)v.elementAt(0);
            parentCode = parentLf.getCode();
        }

        com.redmoon.blog.LeafChildrenCacheMgr dlcm = new com.redmoon.blog.LeafChildrenCacheMgr(parentCode);
        java.util.Vector vt = dlcm.getDirList();

        StringBuffer buf = new StringBuffer();
        Iterator ir = vt.iterator();

        while (ir.hasNext()) {
            Leaf lf = (Leaf) ir.next();
            int nSteps = steps.size();
            // LogUtil.getLog(getClass()).info("nSteps=" + nSteps);

            for (int i = 0; i < nSteps; i++) {
                ITemplate step = (ITemplate) steps.get(i);
                // LogUtil.getLog(getClass()).info("step=" + step.getClass());
                if (step instanceof FieldPart) {
                    // LogUtil.getLog(getClass()).info("toString:" + step.getClass());
                    buf.append(((FieldPart) step).write(lf));
                } else if (step instanceof ListPart) {
                    ListPart listPart = (ListPart) step;
                    String dynName = listPart.getName();
                    buf.append("<!-- BEGIN:" + dynName + "-->\n");
                    if (listPart instanceof ListBlogDirTemplateImpl) {
                        Vector v = new Vector();
                        v.addElement(lf);
                        buf.append(listPart.toString(request, v));
                    }
                    else
                        buf.append(listPart.toString(request, null));
                    buf.append("<!-- END:" + dynName + "-->\n");
                } else { // StaticPart or VariablePart or IgnoredPart
                    buf.append(step.toString(request, null));
                }
            }
        }
        return buf.toString();
    }
}
