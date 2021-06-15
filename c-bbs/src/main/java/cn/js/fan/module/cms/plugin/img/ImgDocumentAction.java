package cn.js.fan.module.cms.plugin.img;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import cn.js.fan.module.cms.plugin.base.IPluginDocumentAction;
import cn.js.fan.module.cms.CMSMultiFileUploadBean;
import cn.js.fan.module.cms.Document;
import cn.js.fan.util.StrUtil;
import javax.servlet.http.HttpServletRequest;

public class ImgDocumentAction implements IPluginDocumentAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public ImgDocumentAction() {
    }

    public boolean create(Document doc) {
        ImgDocumentDb idd = new ImgDocumentDb();
        idd.setSmallImg("");
        idd.setPageType(Document.PAGE_TYPE_TAG);
        idd.setDocId(doc.getId());
        idd.setImages("");
        return idd.create();
    }

    public boolean create(ServletContext application, HttpServletRequest request,
                          CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException {
        String smallImg = StrUtil.getNullStr(mfu.getFieldValue("smallImg"));
        String strPageType = StrUtil.getNullStr(mfu.getFieldValue("imgPageType"));
        cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
	int imgMaxCount = cfg.getIntProperty("cms.imgMaxCount");
        String imgStr = "";
        for (int i=0; i<imgMaxCount; i++) {
            String imgUrl = StrUtil.getNullStr(mfu.getFieldValue("imgUrl" + i)).trim();
            if (!imgUrl.equals("")) {
                String imgDesc = StrUtil.getNullStr(mfu.getFieldValue("imgDesc" + i));
                if (imgStr.equals(""))
                    imgStr += imgUrl + ImgDocumentDb.SEPERATOR_IMG_URL_DESC + imgDesc;
                else
                    imgStr += ImgDocumentDb.SEPERATOR_IMG + imgUrl + ImgDocumentDb.SEPERATOR_IMG_URL_DESC + imgDesc;
            }
        }
        ImgDocumentDb idd = new ImgDocumentDb();
        idd.setSmallImg(smallImg);
        idd.setPageType(StrUtil.toInt(strPageType, Document.PAGE_TYPE_TAG));
        idd.setDocId(doc.getId());
        idd.setImages(imgStr);
        return idd.create();
    }

    public boolean update(ServletContext application,HttpServletRequest request,
                   CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException {
        String smallImg = StrUtil.getNullStr(mfu.getFieldValue("smallImg"));
        String strPageType = StrUtil.getNullStr(mfu.getFieldValue("imgPageType"));
        cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
        int imgMaxCount = cfg.getIntProperty("cms.imgMaxCount");
        String imgStr = "";
        for (int i=0; i<imgMaxCount; i++) {
            String imgUrl = StrUtil.getNullStr(mfu.getFieldValue("imgUrl" + i)).trim();
            if (!imgUrl.equals("")) {
                String imgDesc = StrUtil.getNullStr(mfu.getFieldValue("imgDesc" + i));
                if (imgStr.equals(""))
                    imgStr += imgUrl + ImgDocumentDb.SEPERATOR_IMG_URL_DESC + imgDesc;
                else
                    imgStr += ImgDocumentDb.SEPERATOR_IMG + imgUrl + ImgDocumentDb.SEPERATOR_IMG_URL_DESC + imgDesc;
            }
        }
        ImgDocumentDb idd = new ImgDocumentDb();
        idd = idd.getImgDocumentDb(doc.getId());
        idd.setSmallImg(smallImg);
        idd.setPageType(StrUtil.toInt(strPageType, Document.PAGE_TYPE_TAG));
        idd.setImages(imgStr);
        return idd.save();
    }
}
