package com.redmoon.oa.util;

import cn.js.fan.util.DateUtil;
import com.cloudweb.oa.api.INestTableNodeAlignVisitorHelper;
import com.cloudweb.oa.utils.SpringUtil;
import com.redmoon.oa.base.IFormDAO;
import com.redmoon.oa.sys.DebugUtil;
import org.htmlparser.Tag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.SelectTag;
import org.htmlparser.tags.TextareaTag;
import org.htmlparser.visitors.NodeVisitor;

import java.util.Date;
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
        INestTableNodeAlignVisitorHelper nestTableNodeAlignVisitorHelper = SpringUtil.getBean(INestTableNodeAlignVisitorHelper.class);
        nestTableNodeAlignVisitorHelper.visitTag(tag, nestFieldName, mapAlign);
    }
}
