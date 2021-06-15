package com.redmoon.forum;

import cn.js.fan.util.*;
import com.redmoon.forum.Config;
import com.redmoon.forum.person.UserDb;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.SkinUtil;

/**
 *
 * <p>Title: 收藏夹</p>
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
public class Favoriate {
    String connname = "forum";

    public Favoriate() {
    }

    /**
     * 取得user的所有favoriate的id
     * @param user String
     * @return String[]
     */
    public String getIDS(String user) {
        UserDb ud = new UserDb();
        ud = ud.getUser(user);
        String fav = StrUtil.getNullStr(ud.getFavoriate());
        return fav;
    }

    public boolean Add(HttpServletRequest request, String user, String id) throws ErrMsgException {
        UserDb ud = new UserDb();
        ud = ud.getUser(user);
        if (!ud.isLoaded())
            throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.Favoriate", "info_user_not_exist")); // "该用户不存在！");
        String fav = StrUtil.getNullStr(ud.getFavoriate());
        //往专区中添加目录
        if (!fav.equals("")) {
            String[] ary = fav.split(",");
            if (ary != null) {
                int len = ary.length;
                Config cfg = Config.getInstance();
                String num = cfg.getProperty("forum.favoriateNum");
                int n = Integer.parseInt(num);
                if (len >= n) {
                    String str = SkinUtil.LoadString(request, "res.forum.Favoriate", "err_full");
                    str = str.replaceFirst("\\$n", num);
                    throw new ErrMsgException(str);
                }
                for (int i = 0; i < len; i++) {
                    if (ary[i].equals(id))
                        throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.Favoriate", "err_already_in"));
                }
            }
        }
        if (fav.equals(""))
            fav = id;
        else
            fav += "," + id;
        ud.setFavoriate(fav);
        return ud.save();
    }

    public boolean Remove(HttpServletRequest request, String user, String id) throws ErrMsgException {
        UserDb ud = new UserDb();
        ud = ud.getUser(user);
        if (!ud.isLoaded())
            throw new ErrMsgException(SkinUtil.LoadString(request,"res.forum.Favoriate", "info_user_not_exist")); // "该用户不存在！");
        String fav = StrUtil.getNullStr(ud.getFavoriate());
        boolean isfinded = false;
        String newfav = "";
        boolean re = false;
        if (!fav.equals("")) {
            // 删除id
            String[] ary = fav.split(",");
            if (ary != null) {
                int len = ary.length;
                for (int i = 0; i < len; i++) {
                    if (ary[i].equals(id))
                        isfinded = true;
                    else
                    if (newfav.equals(""))
                        newfav = ary[i];
                    else
                        newfav += "," + ary[i];
                }
            }
        }
        if (isfinded) {
            ud.setFavoriate(newfav);
            re = ud.save();
        } else
            re = false;
        return re;
    }

}
