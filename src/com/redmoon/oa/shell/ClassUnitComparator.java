package com.redmoon.oa.shell;

import java.util.Comparator;

public class ClassUnitComparator implements Comparator {
    public int compare(Object o1, Object o2) {
    	ClassUnit e1 = (ClassUnit) o1;
        ClassUnit e2 = (ClassUnit) o2;
        
        if (e1.isExplicit()) {
        	return 1;
        }
        else {
        	return -1;
        }
    }
}