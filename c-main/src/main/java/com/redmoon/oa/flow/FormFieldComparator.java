package com.redmoon.oa.flow;

import java.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FormFieldComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        FormField e1 = (FormField) o1;
        FormField e2 = (FormField) o2;

        if (e1.getName().compareTo(e2.getName())>0) { //这样比较是降序,如果把-1改成1就是升序.
            return 1;
        } else if (e1.getName().compareTo(e2.getName())<0) {
            return -1;
        } else {
            return 0;
        }
    }
}

