package com.redmoon.oa.flow;

import java.util.Vector;
import java.util.Iterator;

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
