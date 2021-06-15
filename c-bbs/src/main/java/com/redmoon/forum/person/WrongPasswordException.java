package com.redmoon.forum.person;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;

/**
 * Title:        风青云[商城]
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      船艇学院
 * @author 		 风青云
 * @version 1.0
 */
public class WrongPasswordException extends Exception {
    public WrongPasswordException(String msg) {
        super(msg);
    }

    public WrongPasswordException(HttpServletRequest request) {
        super(SkinUtil.LoadString(request, "res.forum.person.WrongPasswordException", "err_wrong_pwd"));
        // super("密码错误，请检查大小写或长度是否有误！");
    }
}
