package com.redmoon.oa.ui.menu;

import java.util.Vector;

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

    public Vector getChildren(String code) {
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
