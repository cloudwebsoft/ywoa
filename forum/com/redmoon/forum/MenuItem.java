package com.redmoon.forum;

import java.util.Vector;
import java.util.Iterator;

/**
 *
 * <p>Title:用于在后台管理中，在左侧菜单上列出一级版块菜单项 </p>
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
public class MenuItem {
    Leaf headLeaf;
    Vector childLeaves = new Vector();

    public MenuItem() {
    }

    public void setHeadLeaf(Leaf l) {
        headLeaf = l;
    }

    public Leaf getHeadLeaf() {
        return headLeaf;
    }

    public void addChildLeaf(Leaf l) {
        childLeaves.addElement(l);
    }

    public Iterator getChildLeaves() {
        return childLeaves.iterator();
    }
}
