package com.redmoon.oa.idiofileark;

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


    /**
     * 取得菜单，以layer级目录为大标题，layer+1级目录为小标题
     * @param root_code String 所属的根目录
     */
    public Menu getMenu(String root_code) throws ErrMsgException;
}
