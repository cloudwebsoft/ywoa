package com.redmoon.oa.util;

import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.sys.DebugUtil;
import org.htmlparser.Tag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.TextareaTag;
import org.htmlparser.visitors.NodeVisitor;

import java.util.Map;

/**
 * 调整嵌套表格中控件对齐样式
 */
public class NestTableNodeAlignVisitor extends NodeVisitor {
    String nestFieldName;
    Map<String, String> mapAlign;

    public NestTableNodeAlignVisitor(String nestFieldName, Map<String, String> mapAlign) {
        this.nestFieldName = nestFieldName;
        this.mapAlign = mapAlign;
    }

    @Override
    public void visitTag(Tag tag) {
        if (!(tag.getClass() == InputTag.class || tag.getClass() == SelectTag.class || tag.getClass() == TextareaTag.class)) {
            return;
        }
        String name = tag.getAttribute("name");
        String id = tag.getAttribute("id");

        // _realshow 处理
        if (name==null && id==null) {
            return;
        }

        if (name!=null && (name.equals("chk" + nestFieldName) || name.equals("rowId" + nestFieldName))) {
            return;
        }

        // 如果名称为带有_realshow，则按规律转换
        int p = -1;
        if (name!=null) {
            p = name.indexOf("_realshow");
        }
        if (p!=-1) {
            String tmpL = name.substring(0, p);
            setStyleAlign(tag, tmpL);
        }
        else {
            if (id!=null) {
                p = id.indexOf("_realshow");
            }
            if (p!=-1) {
                String tmpL = id.substring(0, p);
                setStyleAlign(tag, tmpL);
            }
            else {
                // DebugUtil.i(getClass(), "visitTag", tag.toHtml() + " name=" + name + " id=" + id);
                if (id!=null) {
                    setStyleAlign(tag, id);
                }
                else {
                    setStyleAlign(tag, name);
                }
            }
        }
    }

    public void setStyleAlign(Tag tag, String name) {
        if (name.startsWith("nest_field_")) {
            int len = "nest_field_".length();
            int p = name.indexOf("_", len);
            name = name.substring(len, p);
        }
        String align = mapAlign.get(name);
        // 仅处理居右的情况
        if ("right".equals(align)) {
            String style = tag.getAttribute("style");
            if (style == null) {
                style = "text-align:right;";
                tag.setAttribute("style", style);
                return;
            }

            style = style.trim();
            // 如果其中已有text-align，则去除
            int a = style.indexOf("text-align");
            if (a != -1) {
                int b = style.indexOf(";", a);
                // text-align在末尾
                if (b == -1) {
                    style = style.substring(0, a);
                } else {
                    style = style.substring(0, a) + style.substring(b + 1);
                }
            }

            if ("".equals(style)) {
                style = "text-align:right;";
            } else if (style.endsWith(";")) {
                style += "text-align:right;";
            } else {
                style += ";text-align:right;";
            }

            tag.setAttribute("style", style);
        }
    }
}
