package com.cloudwebsoft.framework.aop.Pointcut;

import com.cloudwebsoft.framework.aop.base.Pointcut;
import java.lang.reflect.Method;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
public class RegexpMethodPointcut implements Pointcut {
    String patternString;

    public RegexpMethodPointcut(String patternString) {
        this.patternString = patternString;
    }

    public boolean hit(Object proxy, Method method, Object[] args) {
        // String pat = "<textarea.*?title=['|\"]{0,1}([^'\" >]*)['|\"]{0,1}.*?name=['|\"]{0,1}([^'\"> ]*)['|\"]{0,1}.*?>([^<]*)";
        Pattern p = Pattern.compile(patternString, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(method.getName());
        return m.find();
    }
}
