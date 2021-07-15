package com.cloudweb.oa.api;

import com.redmoon.oa.base.IFormDAO;
import org.htmlparser.Tag;

import java.util.Map;

public interface INestTableNodeVisitorHelper {
    void visitTag(Tag tag, String nestFieldName, int rowId, IFormDAO fdao, Map<String, String> mapAlign);
}

