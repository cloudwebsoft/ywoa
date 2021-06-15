package com.redmoon.oa.ui.menu;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.web.Global;
import com.redmoon.kit.util.FileUpload;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import java.util.Iterator;
import com.redmoon.oa.person.UserSetupDb;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class WallpaperMgr {
    public WallpaperMgr() {
    }

    public boolean create(ServletContext application,
                          HttpServletRequest request) throws
            ErrMsgException {
        String contentType = request.getContentType();
        if (contentType!=null && contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        FileUpload fu = new FileUpload();
        String[] extAry = {"png", "gif", "jpg"};
        fu.setValidExtname(extAry);
        fu.setMaxFileSize(Global.FileSize); // 35000); // 最大35000K

        int ret = -1;
        try {
            ret = fu.doUpload(application, request);
        } catch (IOException e) {
            throw new ErrMsgException(e.getMessage());
        }
        if (ret!=FileUpload.RET_SUCCESS) {
            throw new ErrMsgException(fu.getErrMessage(request));
        }

        WallpaperDb wd = new WallpaperDb();
        String diskName = wd.writeFile(request, fu);

        if (diskName.equals(""))
            throw new ErrMsgException("请上传图片！");

        boolean re = false;
        try {
            // 删除原来的壁纸
            String userName = fu.getFieldValue("userName");
            String sql = wd.getTable().getSql("getImgPath");
            Iterator ir = wd.list(sql, new Object[]{userName}).iterator();
            while (ir.hasNext()) {
                wd = (WallpaperDb)ir.next();
                wd.del();
            }
            // 创建新壁纸
            re = wd.create(new JdbcTemplate(), new Object[]{userName, diskName});

            if (re) {
                // 置当前壁纸为新上传的文件
                UserSetupDb usd = new UserSetupDb();
                usd = usd.getUserSetupDb(userName);
                usd.setWallpaper("#");
                usd.save();
            }
        } catch (ResKeyException ex) {
            throw new ErrMsgException(ex.getMessage(request));
        }

        return re;
    }

}
