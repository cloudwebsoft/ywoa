package com.redmoon.oa.util;

import com.cloudweb.oa.api.INestTableNodeVisitorHelper;
import com.cloudweb.oa.utils.SpringUtil;
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
        INestTableNodeVisitorHelper nestTableNodeVisitorHelper = SpringUtil.getBean(INestTableNodeVisitorHelper.class);
        nestTableNodeVisitorHelper.visitTag(tag, nestFieldName, rowId, fdao, mapAlign);
    }
}
