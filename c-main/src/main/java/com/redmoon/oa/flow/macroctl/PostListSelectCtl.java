package com.redmoon.oa.flow.macroctl;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import cn.js.fan.util.StrUtil;

import javax.servlet.http.HttpServletRequest;

import com.cloudweb.oa.entity.Post;
import com.cloudweb.oa.service.IPostService;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.flow.FormField;

/**
 * <p>
 * Title:
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 *
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class PostListSelectCtl extends AbstractMacroCtl {
    public PostListSelectCtl() {
    }

    @Override
    public String convertToHTMLCtl(HttpServletRequest request, FormField ff) {
        StringBuilder sb = new StringBuilder();
        IPostService postService = SpringUtil.getBean(IPostService.class);
        if (ff.isEditable()) {
            sb.append("<select name=").append(StrUtil.sqlstr(ff.getName()))
                    .append(" id=").append(StrUtil.sqlstr(ff.getName()))
                    .append(" >");
            sb.append("<option value = ''>请选择</option>");

            Privilege pvg = new Privilege();
            List<Post> list = postService.listByUnitCode(pvg.getUserUnitCode(request));
            for (Post post : list) {
                int postId = post.getId();
                String pName = post.getName();
                sb
                        .append("<option value = '")
                        .append(postId)
                        .append("'")
                        .append(" ")
                        .append(
                                StrUtil.getNullStr(ff.getValue()).equals(postId + "") ? "selected"
                                        : "").append(">");
                sb.append(pName);
                sb.append("</option>");
            }
            sb.append("</select>\n");

            String pageType = StrUtil.getNullStr((String) request.getAttribute("pageType"));
            if (!ConstUtil.PAGE_TYPE_SHOW.equals(pageType)) {
                sb.append("<script>\n");
                sb.append("$('#" + ff.getName() + "').select2();\n");
                sb.append("</script>\n");
            }
        } else {
            String pName = "";
            String value = StrUtil.getNullStr(ff.getValue());
            int post_id = 0;
            if (!"".equals(value)) {
                post_id = Integer.parseInt(value);
                Post post = postService.getById(post_id);
                pName = post.getName();
            }
            sb.append("<span>").append(pName).append("</span>");
            sb.append("<input type='hidden' id='").append(ff.getName())
                    .append("'").append(" value='").append(ff.getValue())
                    .append("' />");
        }

        return sb.toString();
    }

    @Override
    public String getControlOptions(String userName, FormField arg1) {
        Privilege pvg = new Privilege();
        IPostService postService = SpringUtil.getBean(IPostService.class);
        List<Post> list = postService.listByUnitCode(pvg.getUserUnitCode());
        JSONArray selects = new JSONArray();
        for (Post post : list) {
            JSONObject select = new JSONObject();
            int postId = post.getId();
            String pName = post.getName();
            try {
                select.put("name", pName);
                select.put("value", String.valueOf(postId));
                selects.put(select);
            } catch (JSONException ex) {
                LogUtil.getLog(getClass()).error(ex);
            }
        }
        return selects.toString();
    }

    @Override
    public String getControlText(String arg0, FormField ff) {
        String v = StrUtil.getNullStr(ff.getValue());
        return postNameById(v);
    }

    @Override
    public String getControlType() {
        return "select";
    }

    @Override
    public String getControlValue(String arg0, FormField ff) {
        return ff.getValue();
    }

    /*@Override
    public String getSetCtlValueScript(HttpServletRequest request,
                                       IFormDAO IFormDao, FormField ff, String formElementId) {
        return "";

    }*/

    /**
     * @param request
     * @param ff
     * @param fieldValue
     * @return
     * @Description:
     */
    public String converToHtml(HttpServletRequest request, FormField ff,
                               String fieldValue) {
        return postNameById(fieldValue);

    }

    @Override
    public String getReplaceCtlWithValueScript(FormField ff) {
        return "ReplaceCtlWithValue('" + ff.getName() + "', '" + ff.getType()
                + "','" + postNameById(ff.getValue()) + "');\n";
    }

    public String postNameById(String id) {
        String res = "";
        if (id != null && !"".equals(id)) {
            IPostService postService = SpringUtil.getBean(IPostService.class);
            Post post = postService.getById(id);
            if (post != null) {
                res = post.getName();
            }
        }
        return res;
    }
}
