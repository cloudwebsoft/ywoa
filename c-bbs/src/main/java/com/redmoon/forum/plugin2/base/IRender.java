package com.redmoon.forum.plugin2.base;

import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.MsgDb;

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
public interface IRender {
    String rend(HttpServletRequest request, MsgDb msgDb);
}
