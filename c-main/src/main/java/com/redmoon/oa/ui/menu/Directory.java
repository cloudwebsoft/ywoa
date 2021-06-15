package com.redmoon.oa.ui.menu;

import java.sql.*;
import javax.servlet.http.*;
import cn.js.fan.db.*;
import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;
import java.util.Vector;
import java.util.Iterator;
import com.cloudwebsoft.framework.util.LogUtil;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 * ╋ 女性话题      一级目录
 *   ├『花样年华』  二级目录
 *   ├『花样年华』
 *   ╋ 女性话题     二级目录
 *     ├『花样年华』 三级目录
 * @author not attributable
 * @version 1.0
 */

public class Directory {

    public Directory() {
    }

    public Leaf getLeaf(String code) {
        Leaf leaf = new Leaf();
        return leaf.getLeaf(code);
    }

    public Vector getChildren(String code) throws ErrMsgException {
    	if ("-1".equals(code)) {
    		Leaf leaf = new Leaf();
    		leaf.setCode("-1");
    		return leaf.getChildren();
    	}
    	else {
	        Leaf leaf = getLeaf(code);
	        return leaf.getChildren();
    	}
    }
}
