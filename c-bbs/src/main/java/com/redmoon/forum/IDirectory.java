package com.redmoon.forum;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

public interface IDirectory {
    public boolean AddRootChild(HttpServletRequest request) throws
            ErrMsgException ;

    public boolean AddChild(ServletContext application, HttpServletRequest request) throws
            ErrMsgException;

    public void del(ServletContext application, String delcode) throws ErrMsgException;

    public boolean update(ServletContext application, HttpServletRequest request) throws ErrMsgException;

    public boolean move(HttpServletRequest request) throws ErrMsgException;

}
