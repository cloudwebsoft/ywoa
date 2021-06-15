package com.cloudwebsoft.framework.template;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>Title: 静态标签</p>
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
public class StaticPart implements ITemplate {
    String name;

    public StaticPart(String name) {
        this.name = name;
    }

    public void setName(String name) {
    }

    public String getName() {
        return "";
    }

    public void setParentName(String parentName) {
    }

    public String getParentName() {
        return "";
    }

    public String toString(HttpServletRequest request, List param) {
        return name;
    }
}
