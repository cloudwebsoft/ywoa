package com.redmoon.forum.person;

import cn.js.fan.base.AbstractForm;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import java.io.IOException;
import com.redmoon.kit.util.FileUpload;
import cn.js.fan.web.Global;
import cn.js.fan.util.StrUtil;
import javax.servlet.ServletContext;
import com.redmoon.forum.Config;
import cn.js.fan.web.SkinUtil;
import java.util.*;
import com.redmoon.kit.util.FileInfo;
import java.awt.Image;
import javax.swing.ImageIcon;
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

public class UserForm extends AbstractForm {

    public UserForm() {
    }

    public ForumFileUpload doUpload(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        ForumFileUpload fu = new ForumFileUpload();
        Config cfg = Config.getInstance();
        int photoSize = cfg.getIntProperty("forum.photo_max_size");
        fu.setMaxFileSize(photoSize);
        String[] extnames = {"jpg", "gif", "png", "bmp"};
        fu.setValidExtname(extnames);//设置可上传的文件类型

        int ret = 0;
        // logger.info("ret=" + ret);
        try {
            ret = fu.doUpload(application, request);
            if (ret == -4) {
                throw new ErrMsgException(fu.getErrMessage(request));
            }

            if (ret == -3) {
                String str = SkinUtil.LoadString(request, "res.forum.person.UserForm", "err_photo_to_large");
                str = str.replaceFirst("\\$s", "" + photoSize);

                throw new ErrMsgException(StrUtil.makeErrMsg(str)); // "<a href='javascript:history.back()'>您上传的相片太大,请把相片大小限制在100K以内!</a>"));
            }
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
        }
        return fu;
    }

    public ForumFileUpload checkDIYMyface(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        init();
        ForumFileUpload fu = doUpload(application, request);
        Vector v = fu.getFiles();
        Iterator ir = v.iterator();
        if (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            Image img = new ImageIcon(
                    fi.getTmpFilePath()).getImage();
            int w = img.getWidth(null); // 200
            int h = img.getHeight(null); // 200
            Config cfg = Config.getInstance();
            int faceWidth = cfg.getIntProperty("forum.faceWidth");
            int faceHeight = cfg.getIntProperty("forum.faceHeight");
            if (w > faceWidth || h > faceHeight) {
                log(StrUtil.format(SkinUtil.LoadString(request, "res.forum.person.UserDb", "err_face_size"), new Object[] {""+faceWidth, ""+faceHeight}));
            }
        }
        report();
        return fu;
    }
}
