package com.cloudweb.oa.utils;

import org.springframework.web.servlet.view.InternalResourceView;

import java.io.File;
import java.util.Locale;

public class HandleResourceViewExists extends InternalResourceView {

    /**
     * AbstractUrlBasedView中的checkResource永远都返回true，表示视图存在，不会再进入其他的视图解析器。
     * 重写了checkResource方法，若当前视图无法解析，则返回false，使之能够进入下一个视图。
     * @param locale
     * @return
     */
    @Override
    public boolean checkResource(Locale locale) {
        File file = new File(this.getServletContext().getRealPath("/") + getUrl());
        return file.exists(); //判断页面是否存在
    }
}