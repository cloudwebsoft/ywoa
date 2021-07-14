package com.redmoon.blog.template;

import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.blog.photo.*;

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
public class ListBlogPhotoTemplateImpl extends ListPart {
    String query = "";

    public ListBlogPhotoTemplateImpl() {
    }

    public String toString(HttpServletRequest request, List params) {
        if (steps == null)
            return "";

        // System.out.println(getClass() + " dirCode=" + dirCode);
        PhotoDb pd = new PhotoDb();

        String dirCode = ParamUtil.get(request, "dirCode");

        String orderBy = ParamUtil.get(request, "orderBy");
        if (orderBy.equals("")) {
            orderBy = "addDate";
        }
        else {
            orderBy = "score";
        }

        String sql = "";
        if (dirCode.equals(""))
            sql = "select id from " + pd.getTableName();
        else {
            sql = "select id from " + pd.getTableName() + " where dir_code=" + StrUtil.sqlstr(dirCode);
        }
        sql += " order by " + orderBy + " desc";

        String ps = (String)props.get("pageSize");
        if (ps!=null && !StrUtil.isNumeric(ps))
            throw new IllegalArgumentException("pageSize:" + ps +
                                               " is not a number");
        int pageSize = StrUtil.toInt(ps, 10);

        // 如果没有设置start和end，则根据传递的参数自动获取
        int curPage = StrUtil.toInt(ParamUtil.get(request, "CPages"), 1);
        ListResult lr = null;

        try {
            lr = pd.listResult(sql, curPage, pageSize);
        }
        catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("toString:" + e.getMessage());
        }
        paginator = new Paginator(request, lr.getTotal(), pageSize);

        // LogUtil.getLog(getClass()).info("query=" + query);

        StringBuffer buf = new StringBuffer();
        Iterator ir = lr.getResult().iterator();

        while (ir.hasNext()) {
            pd = (PhotoDb) ir.next();
            int nSteps = steps.size();
            // LogUtil.getLog(getClass()).info("nSteps=" + nSteps);

            for (int i = 0; i < nSteps; i++) {
                ITemplate step = (ITemplate) steps.get(i);
                // LogUtil.getLog(getClass()).info("step=" + step.getClass());
                if (step instanceof PaginatorPart) {
                    PaginatorPart pp = (PaginatorPart)step;
                    pp.setUrl("?dirCode=" + dirCode + "&orderBy=" + orderBy);
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
                    // FieldPart fp = (FieldPart) step;

                    // LogUtil.getLog(getClass()).info("toString:" + step.getClass());
                    buf.append(((FieldPart) step).write(pd));
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
