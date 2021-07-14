package com.redmoon.forum;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.kit.util.UploadDdxc;
import com.redmoon.kit.util.FileUpload;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import com.redmoon.forum.person.UserDb;
import com.redmoon.kit.util.UploadFileInfo;
import java.io.File;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.web.Global;

/**
 * <p>Title: 用于支持断点续传</p>
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
public class ForumUploadDdxc extends UploadDdxc {

    public ForumUploadDdxc() {
        maxFileSize = 1024 * 10; // 默认为10M
    }

    public String receive(ServletContext application,
                          HttpServletRequest request) throws ErrMsgException {
        return super.receive(application, request);
    }

    public String ReceiveUploadFileHeader(HttpServletRequest request, FileUpload fu) throws ErrMsgException {
        // 检查磁盘空间是否允许写Attachment
        Privilege privilege = new Privilege();
        if (!privilege.canUploadAttachment(request, 0)) {
            UserDb ud = new UserDb();
            ud = ud.getUser(privilege.getUser(request));
            String str = SkinUtil.LoadString(request, "res.forum.ForumUploadDdxc", "err_space");
            str = str.replaceFirst("\\$allowed", "" + ud.getDiskSpaceAllowed());
            str = str.replaceFirst("\\$used", "" + ud.getDiskSpaceUsed());
            throw new ErrMsgException(str); // "您的磁盘允许空间为" + ud.getDiskSpaceAllowed() + "字节，已用空间为" + ud.getDiskSpaceUsed() + "字节，不能再上传！");
        }
        visualPath = Config.getInstance().getAttachmentPath();
        return super.ReceiveUploadFileHeader(request, fu);
    }

    public String ReceiveUploadFileFinished(HttpServletRequest request, FileUpload fu) throws
            ErrMsgException {
        // 如果是文件的结束，则将文件整理并拷贝至相应的目录，然后删除临时文件
        String fileId = fu.getFieldValue("fileId");
        if (fileId == null)
            throw new ErrMsgException("Want fileId");
        UploadFileInfo ufi = (UploadFileInfo) getUploadFileInfos().get(fileId);
        // logger.info("ReceiveUploadFileFinished:" + ufi.getFilePath() + " ---- " + ufi.getClientFilePath());
        if (ufi == null)
                throw new ErrMsgException("Want thread header");

        String re = super.ReceiveUploadFileFinished(request, fu);
        // 如果未成功，则退出
        if (!re.equals(file_finished_ok))
            return re;

        // 在UploadFIleInfo指定的保存位置写入文件
        String fullSavePath = ufi.getFullSavePath(Global.getRealPath());

        File file = new File(fullSavePath);
        // logger.info("fullSavePath" + fullSavePath);
        if (file.exists()) {
            // 更新用户的磁盘已用空间
            Privilege privilege = new Privilege();
            UserDb ud = new UserDb();
            ud = ud.getUser(privilege.getUser(request));
            ud.setDiskSpaceUsed(ud.getDiskSpaceUsed() + file.length());
            ud.save();
        }

        return re;
    }
}
