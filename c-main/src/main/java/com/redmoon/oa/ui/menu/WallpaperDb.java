package com.redmoon.oa.ui.menu;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.kit.util.FileUpload;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.web.Global;
import com.redmoon.kit.util.FileInfo;
import cn.js.fan.util.ErrMsgException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;
import java.io.File;

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
public class WallpaperDb extends QObjectDb {
    public WallpaperDb() {
    }

    /**
     * 取得壁纸图片的路径
     * @param userName String 用户名
     * @return String 图片路径
     */
    public String getImgPath(String userName) {
        String sql = getTable().getSql("getImgPath");
        // String sql = "select id from " + getTable().getName() + " where user_name=?";
        Iterator ir = list(sql, new Object[]{userName}).iterator();
        if (ir.hasNext()) {
            WallpaperDb wd = (WallpaperDb)ir.next();
            return wd.getString("img_path");
        }
        return null;
    }

    public String writeFile(HttpServletRequest request, FileUpload fu) throws ErrMsgException {
       if (fu.getRet() == FileUpload.RET_SUCCESS) {
            Vector v = fu.getFiles();
            Iterator ir = v.iterator();
            String vpath = "wallpaper/";
            // 置保存路径
            String filepath = Global.getRealPath() + "upfile/" + vpath;
            // 置路径
            fu.setSavePath(filepath);
            if (ir.hasNext()) {
                FileInfo fi = (FileInfo) ir.next();

                // 使用随机名称写入磁盘
                fi.write(fu.getSavePath(), true);
                return fi.getDiskName();
            }
        }
        return "";
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        boolean re = false;
        re = super.del(jt);
        if (re) {
            // 删除磁盘文件
            File f = new File(Global.getRealPath() + "upfile/wallpaper/" + getString("img_path"));
            f.delete();
        }
        return re;
    }

}
