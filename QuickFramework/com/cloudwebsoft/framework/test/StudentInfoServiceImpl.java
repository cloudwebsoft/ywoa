package com.cloudwebsoft.framework.test;

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
public class StudentInfoServiceImpl implements StudentInfoService {
    public void findInfo(String name) {
        System.out.println("你目前输入的名字是:" + name);
    }
}
