package cn.js.fan.module.cms.plugin.software;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import cn.js.fan.module.cms.plugin.base.IPluginDocumentAction;
import cn.js.fan.module.cms.CMSMultiFileUploadBean;
import cn.js.fan.module.cms.Document;
import cn.js.fan.util.StrUtil;
import javax.servlet.http.HttpServletRequest;

public class SoftwareDocumentAction implements IPluginDocumentAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public SoftwareDocumentAction() {
    }

    public boolean create(Document doc) {
        String urls = "";
        SoftwareDocumentDb idd = new SoftwareDocumentDb();
        idd.setSmallImg("");
        idd.setDocId(doc.getId());
        idd.setUrls(urls);
        idd.setSoftType("");
        idd.setSoftRank("");
        idd.setAccredit("");
        idd.setFileType("");
        idd.setLang("");
        idd.setOs("");
        idd.setOfficalUrl("");
        idd.setOfficalDemo("");
        idd.setFileSize(0);
        idd.setUnit("");
        idd.setDirCode(doc.getDirCode());
        idd.setParentCode(doc.getParentCode());
        return idd.create();
    }

    public boolean create(ServletContext application, HttpServletRequest request,
                          CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException {
        String smallImg = StrUtil.getNullStr(mfu.getFieldValue("smallImg"));
        cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
	int softwareMaxCount = cfg.getIntProperty("cms.softwareMaxCount");
        String urls = "";
        for (int i=0; i<softwareMaxCount; i++) {
            String url = StrUtil.getNullStr(mfu.getFieldValue("softUrl" + i)).trim();
            if (!url.equals("")) {
                if (urls.equals(""))
                    urls += url;
                else
                    urls += SoftwareDocumentDb.SEPERATOR_URL + url;
            }
        }
        if (urls.equals(""))
            throw new ErrMsgException("请填写下载地址");

        String softRank = mfu.getFieldValue("softRank");
        String accredit = mfu.getFieldValue("accredit");
        String fileType = mfu.getFieldValue("fileType");
        String lang = mfu.getFieldValue("lang");
        String softType = mfu.getFieldValue("softType");
        String os = mfu.getFieldValue("os");
        String officalUrl = mfu.getFieldValue("officalUrl");
        String officalDemo = mfu.getFieldValue("officalDemo");
        int fileSize = StrUtil.toInt(mfu.getFieldValue("fileSize"), 0);
        String unit = mfu.getFieldValue("unit");

        SoftwareDocumentDb idd = new SoftwareDocumentDb();
        idd.setSmallImg(smallImg);
        idd.setDocId(doc.getId());
        idd.setUrls(urls);
        idd.setSoftType(softType);
        idd.setSoftRank(softRank);
        idd.setAccredit(accredit);
        idd.setFileType(fileType);
        idd.setLang(lang);
        idd.setOs(os);
        idd.setOfficalUrl(officalUrl);
        idd.setOfficalDemo(officalDemo);
        idd.setFileSize(fileSize);
        idd.setUnit(unit);
        idd.setDirCode(doc.getDirCode());
        idd.setParentCode(doc.getParentCode());
        return idd.create();
    }

    public boolean update(ServletContext application,HttpServletRequest request,
                   CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException {
        String smallImg = StrUtil.getNullStr(mfu.getFieldValue("smallImg"));
        cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
        int softwareMaxCount = cfg.getIntProperty("cms.softwareMaxCount");
        String urls = "";
        for (int i=0; i<softwareMaxCount; i++) {
            String url = StrUtil.getNullStr(mfu.getFieldValue("softUrl" + i)).trim();
            if (!url.equals("")) {
                if (urls.equals(""))
                    urls += url;
                else
                    urls += SoftwareDocumentDb.SEPERATOR_URL + url;
            }
        }
        if (urls.equals(""))
            throw new ErrMsgException("请填写下载地址");
        String softRank = mfu.getFieldValue("softRank");
        String accredit = mfu.getFieldValue("accredit");
        String fileType = mfu.getFieldValue("fileType");
        String lang = mfu.getFieldValue("lang");
        String softType = mfu.getFieldValue("softType");
        String os = mfu.getFieldValue("os");
        String officalUrl = mfu.getFieldValue("officalUrl");
        String officalDemo = mfu.getFieldValue("officalDemo");
        int fileSize = StrUtil.toInt(mfu.getFieldValue("fileSize"), 0);
        String unit = mfu.getFieldValue("unit");

        SoftwareDocumentDb idd = new SoftwareDocumentDb();
        idd = idd.getSoftwareDocumentDb(doc.getId());
        idd.setSmallImg(smallImg);
        idd.setUrls(urls);
        idd.setSoftType(softType);
        idd.setSoftRank(softRank);
        idd.setAccredit(accredit);
        idd.setFileType(fileType);
        idd.setLang(lang);
        idd.setOs(os);
        idd.setOfficalUrl(officalUrl);
        idd.setOfficalDemo(officalDemo);
        idd.setFileSize(fileSize);
        idd.setUnit(unit);
        idd.setDirCode(doc.getDirCode());
        idd.setParentCode(doc.getParentCode());
        return idd.save();
    }
}
