package com.redmoon.forum;

import java.util.Vector;
import java.util.Iterator;

/**
 *
 * <p>Title: 用于在后台管理中，在左侧菜单上列出一级版块</p>
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
public class Menu {
    Vector menuItem = new Vector();

    public Menu() {
    }

    public Iterator Iterator() {
        return menuItem.iterator();
    }

    public void addItem(MenuItem mi) {
        menuItem.addElement(mi);
    }
}
