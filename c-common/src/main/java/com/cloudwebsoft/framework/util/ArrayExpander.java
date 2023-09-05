package com.cloudwebsoft.framework.util;

import java.lang.reflect.Array;

/**
 *
 * <p>Title: 扩展数组</p>
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
public final class ArrayExpander {

    public static Object expand(Object array, int newSize) {
        if (array == null) {
            return null;
        }

        Class c = array.getClass();
        if (c.isArray()) {
            int len = Array.getLength(array);
            if (len >= newSize) {
                return array;
            } else {
                Class cc = c.getComponentType();
                Object newArray = Array.newInstance(cc, newSize);
                System.arraycopy(array, 0, newArray, 0, len);
                return newArray;
            }
        } else {
            throw new ClassCastException("Need  array");
        }
    }

    /**
     * 增加数组大小，只能用于int型
     * @param a int[]
     * @param size int
     * @return int[]
     */
    public static int[] expand(int[] a, int size) {
        if (size <= a.length) {
            return a;
        }
        int[] t = new int[size];
        // System.arraycopy(a, 0, t, 0, a.length);
        return t;
    }

    public static void main(String[] args) throws Exception {
        ArrayExpander ae = new ArrayExpander();
        int[] a = {1, 2, 3, 4, 5, 6};
        a = expand(a, 10);
        for (int i = 0; i < a.length; ++i) {
            LogUtil.getLog(ArrayExpander.class).info(a.toString());
        }
    }
}
