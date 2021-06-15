package com.cloudwebsoft.framework.template;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>Title: </p>
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
public interface ITemplate extends java.io.Serializable {
    public void setName(String name);
    public String getName();
    public void setParentName(String parentName);
    public String getParentName();
    public String toString(HttpServletRequest request, List param);
}
