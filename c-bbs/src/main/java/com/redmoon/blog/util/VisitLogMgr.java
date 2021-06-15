package com.redmoon.blog.util;

import javax.servlet.http.*;

import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.*;
import com.redmoon.forum.SequenceMgr;
import java.util.Vector;
import java.util.Iterator;
import cn.js.fan.db.SQLFilter;
import java.sql.SQLException;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.util.IPStoreDb;
import com.redmoon.blog.Config;
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
public class VisitLogMgr {
    static Vector dirLog = new Vector();
    static Vector docLog = new Vector();

    static int MAX_COUNT = 5;

    public VisitLogMgr() {
        super();
    }

}
