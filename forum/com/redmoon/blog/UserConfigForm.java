package com.redmoon.blog;

import cn.js.fan.base.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.db.SQLFilter;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ParamUtil;
import com.redmoon.forum.Privilege;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileUpload;
import java.awt.Image;
import java.io.IOException;
import cn.js.fan.web.Global;
import javax.swing.ImageIcon;
import com.redmoon.kit.util.FileInfo;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.ServletContext;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.person.UserMgr;
import com.redmoon.forum.person.UserDb;
import com.redmoon.forum.util.ForumFileUpload;

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
public class UserConfigForm extends AbstractForm {
    UserConfigDb ucd;
    HttpServletRequest request;
    ServletContext application;
    ForumFileUpload fileUpload;

    public UserConfigForm(ServletContext application, HttpServletRequest request) {
        this.request = request;
        this.application = application;
        ucd = new UserConfigDb();
    }

    public UserConfigDb getUserConfigDb() {
        return ucd;
    }

    public FileUpload doUpload(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new ForumFileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] extnames = {"jpg", "gif", "png", "bmp"};
        fileUpload.setValidExtname(extnames);//设置可上传的文件类型

        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret != fileUpload.RET_SUCCESS) {
                throw new ErrMsgException("ret=" + ret + " " + fileUpload.getErrMessage(request));
            }
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
        }

        Vector v = fileUpload.getFiles();
        Iterator ir = v.iterator();
        if (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            Image img = new ImageIcon(
                    fi.getTmpFilePath()).getImage();
            int w = img.getWidth(null); // 200
            int h = img.getHeight(null); // 200
            Config cfg = Config.getInstance();
            int iconWidth = cfg.getIntProperty("iconWidth");
            int iconHeight = cfg.getIntProperty("iconHeight");
            if (w > iconWidth || h > iconHeight) {
                throw new ErrMsgException(StrUtil.format(SkinUtil.LoadString(request, "res.blog.UserConfigDb", "err_logo_size"), new Object[] {""+iconWidth, ""+iconHeight}));
            }
        }
        return fileUpload;
    }

    public UserConfigForm(ServletContext application, HttpServletRequest request, UserConfigDb ucd) {
        this.request = request;
        this.application = application;
        this.ucd = ucd;
    }

    public long chkId() {
        long id = -1;
        try {
            id = Long.parseLong(fileUpload.getFieldValue("id"));
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("chkId:" + e.getMessage());
        }
        return id;
    }

    public int chkType() {
        int type = UserConfigDb.TYPE_PERSON;
        try {
            type = Integer.parseInt(fileUpload.getFieldValue("blogType"));
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("chkType:" + e.getMessage());
        }
        ucd.setType(type);
        return type;
    }

    public String chkUserName() {
        Privilege privilege = new Privilege();
        String userName = privilege.getUser(request);
        ucd.setUserName(userName);
        return userName;
    }

    public String chkTitle() {
        String title = StrUtil.getNullStr(fileUpload.getFieldValue("title"));
        if (title.equals("")) {
            log("标题必须填写！");
        }
        if (!SQLFilter.isValidSqlParam(title))
            log("请勿使用' ; 等字符！");
        ucd.setTitle(title);
        return title;
    }

    public String chkFriends() {
        String friends = StrUtil.getNullStr(fileUpload.getFieldValue("friends"));
        if (!SQLFilter.isValidSqlParam(friends))
            log("请勿使用' ; 等字符！");
        // 检查friends是否存在
        String[] ary = StrUtil.split(friends, ",");
        if (ary!=null) {
            int len = ary.length;
            UserMgr um = new UserMgr();
            for (int i=0; i<len; i++) {
                UserDb user = um.getUserDbByNick(ary[i]);
                if (user==null || !user.isLoaded())
                    log(ary[i] + " 不存在!");
                else {
                    // 检查该用户博客是否存在
                    UserConfigDb ucd = new UserConfigDb();
                    ucd = ucd.getUserConfigDbByUserName(user.getName());
                    if (ucd==null || !ucd.isLoaded()) {
                        log(ary[i] + "的博客尚未开通!");
                    }
                }
            }
        }
        ucd.setFriends(friends);
        return friends;
    }

    public String chkSubTitle() {
        String subtitle = StrUtil.getNullStr(fileUpload.getFieldValue("subtitle"));
        if (subtitle.equals("")) {
            log("内容必须填写！");
        }
        if (!SQLFilter.isValidSqlParam(subtitle))
            log("请勿使用' ; 等字符！");
        ucd.setSubtitle(subtitle);
        return subtitle;
    }

    public String chkPenName() {
        String penName = StrUtil.getNullStr(fileUpload.getFieldValue("penName"));
        if (penName.equals("")) {
            log("笔名必须填写！");
        }
        if (!SQLFilter.isValidSqlParam(penName))
            log("请勿使用' ; 等字符！");
        ucd.setPenName(penName);
        return penName;
    }

    public String chkSkin() {
        String skin = StrUtil.getNullStr(fileUpload.getFieldValue("skin"));
        if (skin.equals("")) {
            log("皮肤必须填写！");
        }
        if (!SQLFilter.isValidSqlParam(skin))
            log("请勿使用' ; 等字符！");
        ucd.setSkin(skin);
        return skin;
    }

    public String chkNotice() {
        String notice = StrUtil.getNullStr(fileUpload.getFieldValue("notice"));
        if (!SQLFilter.isValidSqlParam(notice))
            log("请勿使用' ; 等字符！");
        ucd.setNotice(notice);
        return notice;
    }

    public String chkDomain() {
        String domain = StrUtil.getNullStr(fileUpload.getFieldValue("domain"));
        if (!SQLFilter.isValidSqlParam(domain))
            log("请勿使用' ; 等字符！");

        if (StrUtil.isNumeric(domain)) {
            log("域名不能全为数字！");
        }

        ucd.setDomain(domain);
        return domain;
    }

    public boolean chkBkMusic() {
        boolean bkMusic = StrUtil.toInt(fileUpload.getFieldValue("isBkMusic"), 0)==1;
        ucd.setBkMusic(bkMusic);
        return bkMusic;
    }

    public boolean chkUserCss() {
        boolean userCss = StrUtil.toInt(fileUpload.getFieldValue("isUserCss"), 0)==1;
        ucd.setUserCss(userCss);
        return userCss;
    }

    public String chkKind() {
        String kind = StrUtil.getNullStr(fileUpload.getFieldValue("kind"));
        if (kind.equals(""))
            log("博客分类必须选择！");
        if (!SQLFilter.isValidSqlParam(kind))
            log("请勿使用' ; 等字符！");
        ucd.setKind(kind);
        return kind;
    }

    public boolean chkIsOpen() {
        int intOpen = StrUtil.toInt(fileUpload.getFieldValue("isOpen"), 1);
        ucd.setOpen(intOpen==1);
        return intOpen==1;
    }

    public boolean chkFootprint() {
        boolean re = fileUpload.getFieldValue("is_footprint").equals("1");
        ucd.setFootprint(re);
        return re;
    }

    public boolean chkPhotoDig() {
        boolean re = fileUpload.getFieldValue("isPhotoDig").equals("1");
        ucd.setPhotoDig(re);
        return re;
    }

    public boolean checkCreate() throws ErrMsgException {
        init();
        doUpload(application, request);
        chkUserName();
        chkTitle();
        chkSubTitle();
        chkPenName();
        chkSkin();
        chkNotice();
        chkKind();
        chkType();
        chkFootprint();
        chkDomain();
        report();
        return true;
    }

    public boolean checkModify() throws ErrMsgException {
        init();
        doUpload(application, request);
        long id = chkId();
        ucd = ucd.getUserConfigDb(id);
        chkTitle();
        chkSubTitle();
        chkPenName();
        chkSkin();
        chkNotice();
        chkKind();
        chkFriends();
        chkFootprint();

        chkDomain();
        chkPhotoDig();

        chkIsOpen();

        // 如果域名不一致，则检查是否新域名是否已被使用
        if (!ucd.getDomain().equals("") && !ucd.oldDomain.equals(ucd.getDomain())) {
            UserConfigDb ud = ucd.getUserConfigDbByDomain(ucd.getDomain());
            if (ud!=null)
                log("域名：" + ucd.getDomain() + " 已被使用！");
        }

        chkBkMusic();
        chkUserCss();

        report();
        return true;
    }
}
