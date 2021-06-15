package com.redmoon.oa.util;

import com.redmoon.oa.base.IFormDAO;
import org.htmlparser.Tag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.TextareaTag;
import org.htmlparser.visitors.NodeVisitor;

import java.util.Map;

/**
 * 改变嵌套表格中控件的name值为nest_field_***_rowId，置行首checkbox的rowId值，以及dataId
 * 用法：
 * NodeList nodeList = Parser.createParser(trTmp, "utf-8").parse(new TagNameFilter("tr"));
 * Node trObj = nodeList.elementAt(0);
 * trObj.accept(new NestTableNodeVisitor(nestFieldName, rowId, fdao));
 * trTmp = trObj.toHtml();
 */
public class NestTableNodeVisitor extends NodeVisitor {
    int rowId;
    IFormDAO fdao;
    String nestFieldName;
    Map<String, String> mapAlign;

    public NestTableNodeVisitor(String nestFieldName, int rowId, IFormDAO fdao, Map<String, String> mapAlign) {
        this.nestFieldName = nestFieldName;
        this.rowId = rowId;
        this.fdao = fdao;
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
            String tmpR = name.substring(p);
            String newName = "nest_field_" + tmpL + "_" + rowId + tmpR;
            tag.setAttribute("name", newName);
            tag.setAttribute("id", newName);
            setStyleAlign(tag, tmpL);
        }
        else {
            if (id!=null) {
                p = id.indexOf("_realshow");
            }
            if (p!=-1) {
                String tmpL = id.substring(0, p);
                String tmpR = id.substring(p);
                String newName = "nest_field_" + tmpL + "_" + rowId + tmpR;
                tag.setAttribute("name", newName);
                tag.setAttribute("id", newName);
                setStyleAlign(tag, tmpL);
            }
            else {
                tag.setAttribute("name", "nest_field_" + name + "_" + rowId);
                tag.setAttribute("id", "nest_field_" + name + "_" + rowId);
                setStyleAlign(tag, name);
            }
        }
    }

    public void setStyleAlign(Tag tag, String name) {
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
