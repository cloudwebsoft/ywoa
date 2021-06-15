package com.redmoon.blog.template;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
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
public class ListBlogTemplateImpl extends ListPart {
    String query = "";

    public ListBlogTemplateImpl() {
    }

    public String toString(HttpServletRequest request, List params) {
        if (steps == null)
            return "";

        // System.out.println(getClass() + " dirCode=" + dirCode);

        String kind = StrUtil.getNullStr((String) props.get("kind"));

        if (kind!=null && kind.startsWith("request")) {
            int p = kind.indexOf(".");
            if (p!=-1) {
                String param = kind.substring(p + 1);
                // LogUtil.getLog(getClass()).info("param=" + param + " request=" + request);
                kind = ParamUtil.get(request, param);
            }
        }

        String ps = (String)props.get("pageSize");
        if (ps!=null && !StrUtil.isNumeric(ps))
            throw new IllegalArgumentException("pageSize:" + ps +
                                               " is not a number");
        int pageSize = StrUtil.toInt(ps, 10);

        // 如果没有设置start和end，则根据传递的参数自动获取
        int curPage = StrUtil.toInt(ParamUtil.get(request, "CPages"), 1);

        String sql = StrUtil.getNullStr((String) props.get("query"));
        UserConfigDb ucd = new UserConfigDb();
        if (kind.equals(""))
            sql = "select id from " + ucd.getTableName() + " ORDER BY addDate desc";
        else
            sql = "select id from " + ucd.getTableName() + " where kind=" +
                  StrUtil.sqlstr(kind) + " ORDER BY addDate desc";

        ListResult lr = null;
        try {
            lr = ucd.ListBlog(sql, curPage, pageSize);
        }
        catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("toString:" + e.getMessage());
            return "";
        }

        paginator = new Paginator(request, lr.getTotal(), pageSize);

        // LogUtil.getLog(getClass()).info("query=" + query);

        StringBuffer buf = new StringBuffer();
        Iterator ir = lr.getResult().iterator();

        while (ir.hasNext()) {
            ucd = (UserConfigDb) ir.next();
            int nSteps = steps.size();
            // LogUtil.getLog(getClass()).info("nSteps=" + nSteps);

            for (int i = 0; i < nSteps; i++) {
                ITemplate step = (ITemplate) steps.get(i);
                // LogUtil.getLog(getClass()).info("step=" + step.getClass());
                if (step instanceof PaginatorPart) {
                    PaginatorPart pp = (PaginatorPart)step;
                    pp.setUrl("?kind=" + StrUtil.UrlEncode(kind));
                    // System.out.println(getClass() + " pp.writeCount=" + pp.writeCount);
                    String writeCount = (String)request.getAttribute(pp.hashCode() + "_WriteCount");
                    // 第一条数据
                    if ((i==0 || !ir.hasNext()) && writeCount==null) {
                        buf.append(pp.write(request, this));
                        request.setAttribute(pp.hashCode() + "_WriteCount", "y");
                    }
                    else
                        continue;
                }
                else if (step instanceof FieldPart) {
                    FieldPart fp = (FieldPart)step;

                    if (fp.getName().equals("type")) {
                        if (ucd.getType()==UserConfigDb.TYPE_GROUP) {
                            buf.append("团队");
                        }
                        else
                            buf.append("个人");
                    }
                    else {
                        // LogUtil.getLog(getClass()).info("toString:" + step.getClass());
                        buf.append(((FieldPart) step).write(ucd));
                    }
                } else if (step instanceof ListPart) {
                    ListPart listPart = (ListPart) step;
                    String dynName = listPart.getName();
                    buf.append("<!-- BEGIN:" + dynName + "-->\n");
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
