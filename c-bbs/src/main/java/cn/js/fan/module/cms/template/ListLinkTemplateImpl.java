package cn.js.fan.module.cms.template;

import java.util.*;

import cn.js.fan.module.nav.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.template.*;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Title: </p>
 *
 * <p>Description: 友情链接</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ListLinkTemplateImpl extends ListPart {
    public ListLinkTemplateImpl() {

    }

    public String toString(HttpServletRequest request, List params) {
        if (steps == null)
            return "";

        LinkDb ld = new LinkDb();

        String kind = StrUtil.getNullStr((String) props.get("kind"));
        if (kind.equals(""))
            kind = LinkDb.KIND_DEFAULT;

        String listsql = ld.getListSql(kind, LinkDb.USER_SYSTEM);
        Iterator irlink = ld.list(listsql).iterator();

        StringBuffer buf = new StringBuffer();

        while (irlink.hasNext()) {
            ld = (LinkDb) irlink.next();
            int nSteps = steps.size();
            // LogUtil.getLog(getClass()).info("nSteps=" + nSteps);

            for (int i = 0; i < nSteps; i++) {
                ITemplate step = (ITemplate) steps.get(i);
                if (step instanceof FieldPart) {
                    // LogUtil.getLog(getClass()).info("toString:" + step.getClass());
                    if (((FieldPart) step).getName().equals("imgTitle")) {
                        if (ld.getImage() != null && !ld.getImage().equals("")) {
                            buf.append("<img src='"+
                                    ld.getImageUrl(request) + "' border=0>");
                        } else {
                            buf.append(ld.getTitle());
                        }
                    } else
                        buf.append(((FieldPart) step).write(ld));
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
