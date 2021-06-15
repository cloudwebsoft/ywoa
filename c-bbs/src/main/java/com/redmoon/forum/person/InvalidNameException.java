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
public class InvalidNameException extends Exception {
  public InvalidNameException(HttpServletRequest request) {
      super(SkinUtil.LoadString(request, "res.forum.person.InvalidNameException", "err_no_user")); // "该用户不存在！");
  }

  public InvalidNameException(String msg) {
      super(msg);
  }
}
