package com.cloudwebsoft.framework.template;

/**
 * <p>Title: 模板规范化类</p>
 *
 * <p>Description: </p>
 * 当模板中有列表标签时，列表标签以<!--开始，以-->结束，在有些编辑器中，如Fckeditor中，会自动格式化
 * 而列表标签的开始与结束符必须顶格，否则会导致解析出错，所以在编辑完内容后，需进行format规范化处理
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class TemplateUtil {
    public TemplateUtil() {
    }

    /**
     * 对内容进行按照模板规范的格式化，如下列情况：FCKEditor会去除回车符
     * @param content String
     * @return String
     */
    public static String format(String content) {
        content = content.replaceAll("<\\!--", "\n<!--");
	content = content.replaceAll("-->", "-->\n");
        return content;
    }
}
