package com.redmoon.oa.help;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;

public interface IDirectory {
    public boolean AddRootChild(HttpServletRequest request) throws
            ErrMsgException ;

    public boolean AddChild(HttpServletRequest request) throws
            ErrMsgException;

    public void del(HttpServletRequest request, String delcode) throws ErrMsgException;

    public boolean update(HttpServletRequest request) throws ErrMsgException;

    public boolean move(HttpServletRequest request) throws ErrMsgException;

}
