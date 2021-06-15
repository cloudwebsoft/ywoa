package cn.js.fan.util;
/*
 * CookieBean.java
 * Created on July 17, 2000, 3:50 PM
 */

import javax.servlet.http.*;
import java.net.URLEncoder;
import java.net.URLDecoder;

/**
 * Adds cookie functionality.
 * @author Calvert-Bettis
 * @version 1.0
 */
public class CookieBean {

  public CookieBean() {
  }

  /**
   * Add a new cookie to Response with
   * specified name and value.
   * @param res HttpServletResponse cookie is added to.
   * @param cookieName Name of new cookie.
   * @param cookieValue Value of new cookie.
   */
  public static void addCookie(HttpServletResponse res, String cookieName, String cookieValue) {
    String v = "";
    try {
      v = URLEncoder.encode(cookieValue, "UTF-8");//这样可支持中文
    }
    catch (Exception e) {}

    Cookie c = new Cookie(cookieName, v);
    res.addCookie(c);
  }
  /**
   * Add a new cookie to Response with
   * specified name and value.
   * @param res HttpServletResponse cookie is added to.
   * @param cookieName Name of new cookie.
   * @param cookieValue Value of new cookie.
   * @param path path of new cookie.
   */
  // 如果使用addCookie(res,cookiename,cookievalue)后再setCookiePath后无效,故而注释掉addCookiePath
  public static void addCookie(HttpServletResponse res, String cookieName,
                               String cookieValue, String path) {
      String v = "";
      try {
          v = URLEncoder.encode(cookieValue, "UTF-8"); //这样可支持中文
      } catch (Exception e) {}
      Cookie c = new Cookie(cookieName, v);
      c.setPath(path);
      res.addCookie(c);
  }

  public static void addCookie(HttpServletResponse res, String cookieName,
                               String cookieValue, String domain, String path, int maxAge) {
      String v = "";
      try {
          v = URLEncoder.encode(cookieValue, "UTF-8"); //这样可支持中文
      } catch (Exception e) {}
      Cookie c = new Cookie(cookieName, v);
      c.setPath(path);
      c.setMaxAge(maxAge);
      c.setDomain(domain);
      res.addCookie(c);
  }

  public static void addCookie(HttpServletResponse res, String cookieName,
                               String cookieValue, String path, int maxAge) {
      String v = "";
      try {
          v = URLEncoder.encode(cookieValue, "UTF-8"); //这样可支持中文
      } catch (Exception e) {}
      Cookie c = new Cookie(cookieName, v);
      c.setPath(path);
      c.setMaxAge(maxAge);
      res.addCookie(c);
  }

  public static void delCookie(HttpServletResponse res, String cookieName,
                               String domain, String path) {
      Cookie killMyCookie = new Cookie(cookieName, null);
      // 正值表示cookie将在这么多秒以后失效。
      // 注意这个值是cookie将要存在的最大时间，而不是cookie现在的存在时间。
      // 负值表示当浏览器关闭时，Cookie将会被删除。零值则是要删除该Cookie。
      killMyCookie.setMaxAge(0);
      killMyCookie.setPath(path);
      killMyCookie.setDomain(domain);
      res.addCookie(killMyCookie);
  }

  public static void delCookie(HttpServletResponse res, String cookieName, String path) {
    Cookie killMyCookie = new Cookie(cookieName, null);
    // 正值表示cookie将在这么多秒以后失效。
    // 注意这个值是cookie将要存在的最大时间，而不是cookie现在的存在时间。
    // 负值表示当浏览器关闭时，Cookie将会被删除。零值则是要删除该Cookie。
    killMyCookie.setMaxAge(0);
    killMyCookie.setPath(path);
    res.addCookie(killMyCookie);
  }

  /**
   * Setter for property cookieMaxAge.
   * @param req HttpServletRequest for retrieving cookie array.
   * @param res HttpServletResponse for writing updated cookie.
   * @param cookieName Name of cookie to update.
   * @param cookieExpires New value of property cookieMaxAge 单位为秒.
   */
  public static void setCookieMaxAge(HttpServletRequest req,
                                     HttpServletResponse res,
                                     String cookieName,
                                     int cookieExpiresSeconds) {
      Cookie c[] = req.getCookies();
      if (c != null) {
          for (int i = 0; i < c.length; i++) {
              if (c[i].getName().equals(cookieName)) {
                  c[i].setMaxAge(cookieExpiresSeconds);
                  res.addCookie(c[i]);
              }
          }
      }
  }

  public static void setCookieDomain(HttpServletRequest req, HttpServletResponse res, String cookieName, String domain) {
    Cookie c[] = req.getCookies();
    if (c != null) {
      for (int i = 0; i < c.length; i++) {
        if (c[i].getName().equals(cookieName)) {
          c[i].setDomain(domain);
          res.addCookie(c[i]);
        }
      }
    }
  }
/*
  public static void setCookiePath(HttpServletRequest req, HttpServletResponse res, String cookieName, String path) {
    Cookie c[] = req.getCookies();
    if (c != null) {
      for (int i = 0; i < c.length; i++) {
        if (c[i].getName().equals(cookieName)) {
          c[i].setPath(path);
          res.addCookie(c[i]);
        }
      }
    }
  }
*/

  /**
   * Getter for property CookieMaxAge.
   * @param req HttpServletRequest for retrieving cookie array.
   * @param cookieName Name of specific cookie to be examined.
   * @return Value of property cookieMaxAge.
   */
  public static int getCookieMaxAge(HttpServletRequest req, String cookieName) {
    Cookie c[] = req.getCookies();
    if (c != null) {
      for (int i = 0; i < c.length; i++) {
        if (c[i].getName().equals(cookieName)) {
          return c[i].getMaxAge();
        }
      }
    }
    return -1;
  }

  /**
   * Getter for property cookieValue.
   * @param req HttpServletRequest for retrieving cookie array.
   * @param cookieName Name of specific cookie to be examined.
   * @return Value of property cookieValue.
   */
  public static String getCookieValue (HttpServletRequest req, String cookieName) {
    Cookie c[] = req.getCookies();
    if (c != null) {
      for (int i = 0; i < c.length; i++) {
        if (c[i].getName().equals(cookieName)) {
          String str = "";
          try {
            str = URLDecoder.decode(c[i].getValue(), "UTF-8");//这样可支持中文
          }
          catch (Exception e) {}
          return str;
        }
      }
    }
    return "";
  }

  /**
   * Setter for property cookieValue.Cookie必须已存在
   * @param req HttpServletRequest for retrieving cookie array.
   * @param res HttpServletResponse for writing updated cookie.
   * @param cookieName Specific cookie to be updated.
   * @param cookieValue New value of property cookieValue.
   */
  public static void setCookieValue (HttpServletRequest req, HttpServletResponse res, String cookieName, String cookieValue) {
    Cookie c[] = req.getCookies();
    if (c != null) {
      for (int i = 0; i < c.length; i++) {
        if (c[i].getName().equals(cookieName)) {
          c[i].setValue(cookieValue);
          res.addCookie(c[i]);
        }
      }
    }
  }
}
