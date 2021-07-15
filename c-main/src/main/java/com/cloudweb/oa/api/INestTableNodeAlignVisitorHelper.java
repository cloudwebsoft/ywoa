package com.cloudweb.oa.api;

import org.htmlparser.Tag;

import java.util.Map;

public interface INestTableNodeAlignVisitorHelper {
    void visitTag(Tag tag, String nestFieldName, Map<String, String> mapAlign);
}
