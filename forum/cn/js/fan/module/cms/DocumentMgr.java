package cn.js.fan.module.cms;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.module.cms.plugin.*;
import cn.js.fan.module.cms.plugin.base.*;
import cn.js.fan.module.cms.search.*;
import cn.js.fan.module.cms.template.*;
import cn.js.fan.module.pvg.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;

public class DocumentMgr {
    Logger logger = Logger.getLogger(DocumentMgr.class.getName());

    public DocumentMgr() {
    }

    public Document getDocument(int id) {
        Document doc = new Document();
        return doc.getDocument(id);
    }

    public Document getDocument(HttpServletRequest request, int id,
                                IPrivilege privilege) throws
            ErrMsgException {
        boolean isValid = false;
        Document doc = getDocument(id);
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (lp.canUserSee(privilege.getUser(request))) {
            return doc;
        }

        if (!isValid)
            throw new ErrMsgException(Privilege.MSG_INVALID);
        return getDocument(id);
    }

    public static String getWebEditPage() {
        Config cfg = new Config();
        boolean isWebEdit = cfg.getBooleanProperty("cms.editor_default_webedit");
        if (isWebEdit)
            return "fckwebedit.jsp";
        else
            return "fckwebedit_new.jsp";
    }

    /**
     * 当directory的结点code的类型为文章时，取其文章，如果文章不存在，则创建文章
     * @param request HttpServletRequest
     * @param code String
     * @param privilege IPrivilege
     * @return Document
     * @throws ErrMsgException
     */
    public Document getDocumentByCode(HttpServletRequest request, String code,
                                      IPrivilege privilege) throws
            ErrMsgException {
        boolean isValid = false;

        LeafPriv lp = new LeafPriv(code);
        if (lp.canUserSee(privilege.getUser(request)))
            isValid = true;

        if (!isValid)
            throw new ErrMsgException(Privilege.MSG_INVALID);
        Document doc = new Document();
        int id = doc.getIDOrCreateByCode(code, privilege.getUser(request));
        return getDocument(id);
    }

    public CMSMultiFileUploadBean doUpload(ServletContext application,
                                           HttpServletRequest request) throws
            ErrMsgException {
        CMSMultiFileUploadBean mfu = new CMSMultiFileUploadBean();
        mfu.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        // String[] ext = {"htm", "gif", "bmp", "jpg", "png", "rar", "doc", "hs", "ppt", "rar", "zip", "jar"};
        // mfu.setValidExtname(ext);
        int ret = 0;
        // System.out.println(getClass() + " doUpload: contentType=" + request.getContentType());
        // logger.info("ret=" + ret);
        try {
            ret = mfu.doUpload(application, request);
            if (ret == -3) {
                throw new ErrMsgException(mfu.getErrMessage(request));
            }
            if (ret == -4) {
                throw new ErrMsgException(mfu.getErrMessage(request));
            }
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
        }
        return mfu;
    }

    public boolean Operate(ServletContext application,
                           HttpServletRequest request, IPrivilege privilege) throws
            ErrMsgException {
        // 检查是否到期，用于猫头鹰
        // java.util.Date logicalDay = new java.util.Date(2006, 7, 1);
        // if (DateUtil.compare(new java.util.Date(), logicalDay) == 1)
        //    return false;

        CMSMultiFileUploadBean mfu = doUpload(application, request);

        fileUpload = mfu;

        String op = StrUtil.getNullStr(mfu.getFieldValue("op"));
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));

        dirCode = dir_code;
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dir_code);
        if (lf==null)
            throw new ErrMsgException("目录" + dir_code + "不存在！");
        Document doc = new Document();

        if (op.equals("edit")) {
            String idstr = StrUtil.getNullString(mfu.getFieldValue("id"));
            if (!StrUtil.isNumeric(idstr))
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        SkinUtil.ERR_ID));
            int id = Integer.parseInt(idstr);
            doc = doc.getDocument(id);

            LeafPriv lp = new LeafPriv(doc.getDirCode());
            if (!lp.canUserModify(privilege.getUser(request)))
                throw new ErrMsgException(SkinUtil.LoadString(request,
                        SkinUtil.PVG_INVALID));
            boolean re = doc.Update(application, mfu);
            if (re) {
                PluginMgr pm = new PluginMgr();
                PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
                if (pu != null) {
                    IPluginDocumentAction ipda = pu.getDocumentAction();
                    re = ipda.update(application, request, mfu, doc);
                }

                // 生成静态页面
                Config cfg = new Config();
                boolean isCreateHtml = cfg.getBooleanProperty("cms.html_auto");
                if (isCreateHtml) {
                    createHtml(request, doc, 1);
                    // 根据页面位置重新生成列表页
                    // 生成列表页面
                    String sql = SQLBuilder.getDirDocListSql(dir_code);
                    // Leaf lf = new Leaf();
                    // lf = lf.getLeaf(dir_code);
                    int total = doc.getDocCount(sql);

                    int listPageSize = cfg.getIntProperty("cms.listPageSize");
                    ListDocPagniator paginator = new ListDocPagniator(request, total, listPageSize);
                    // 根据文章所在的页码，重新生成其所在的列表页
                    int pageNum = paginator.pageNo2Num(doc.getPageNo());
                    createListPageHtml(request, doc.getDirCode(), pageNum);
                    // 生成文章所在栏目的静态面
                    createColumnPageHtmlOfDoc(request, doc);
                }
            }
            return re;
        } else {
            // 如果不能投稿，则检查是否有权限
            if (!lf.isPost()) {
                LeafPriv lp = new LeafPriv(dir_code);
                if (!lp.canUserAppend(privilege.getUser(request)))
                    throw new ErrMsgException(SkinUtil.LoadString(request,
                            SkinUtil.PVG_INVALID));
            }
            boolean re = false;
            try {
                re = doc.create(application, mfu, privilege.getUser(request));
            } catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
            if (re) {
                PluginMgr pm = new PluginMgr();
                PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
                if (pu != null) {
                    IPluginDocumentAction ipda = pu.getDocumentAction();
                    doc = doc.getDocument(doc.getID());
                    re = ipda.create(application, request, mfu, doc);
                }

                // 生成静态页面
                Config cfg = new Config();
                boolean isCreateHtml = cfg.getBooleanProperty("cms.html_auto");
                if (isCreateHtml) {
                    doc = doc.getDocument(doc.getId());
                    createHtml(request, doc, 1);
                    if (doc.getExamine()==Document.EXAMINE_PASS) {
                        // 一次生成前N页，第N+1页有部分内容可能会与第N页重复
                        int listPageHtmlCreateCount = cfg.getIntProperty("cms.listPageHtmlCreateCount");
                        for (int k=1; k<=listPageHtmlCreateCount; k++) {
                            createListPageHtml(request, doc.getDirCode(), k);
                        }
                    }
                    // 生成文章所在栏目的静态面
                    createColumnPageHtmlOfDoc(request, doc);
                }
            }
            return re;
        }
    }

    /**
     * 当文件创建、编辑或者删除时，对其所在的栏目的静态首页进行更新
     * @param request HttpServletRequest
     * @param doc Document
     */
    public void createColumnPageHtmlOfDoc(HttpServletRequest request, Document doc) {
        Leaf plf = new Leaf();
        plf = plf.getLeaf(doc.getDirCode());
        String parentCode = plf.getParentCode();

        while (!parentCode.equals(Leaf.ROOTCODE)) {
            plf = plf.getLeaf(parentCode);
            if (plf == null || !plf.isLoaded())
                break;
            if (plf.getType()==Leaf.TYPE_COLUMN) {
                createColumnPageHtml(request, plf.getCode());
                break;
            }
            parentCode = plf.getParentCode();
        }
    }

    public void delBatch(HttpServletRequest request, boolean isDustbin) throws ErrMsgException {
        Privilege privilege = new Privilege();
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids==null)
            return;
        int len = ids.length;
        for (int i=0; i<len; i++) {
            del(request, Integer.parseInt(ids[i]), privilege, isDustbin);
        }
    }

    public void resumeBatch(HttpServletRequest request) throws ErrMsgException {
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids==null)
            return;
        // 恢复静态文件
        Config cfg = new Config();
        boolean isAutoHtml = cfg.getBooleanProperty("cms.html_auto");

        int len = ids.length;
        Document doc = null;
        for (int i=0; i<len; i++) {
            doc = getDocument(Integer.parseInt(ids[i]));
            doc.UpdateExamine(Document.EXAMINE_PASS);
            if (isAutoHtml) {
                createAllPageHtml(request, doc);
            }
        }
    }

    public boolean resume(HttpServletRequest request, int id) throws ErrMsgException {
        Document doc = getDocument(id);
        boolean re = doc.UpdateExamine(Document.EXAMINE_PASS);
        if (re) {
            // 恢复静态文件
            Config cfg = new Config();
            boolean isAutoHtml = cfg.getBooleanProperty("cms.html_auto");
            if (isAutoHtml) {
                createAllPageHtml(request, doc);
            }
        }
        return re;
    }

    public void passExamineBatch(HttpServletRequest request, int EXAMINE_STATUS) throws ErrMsgException {
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids==null)
            return;
        int len = ids.length;
        Document doc = null;
        Privilege privilege = new Privilege();
        for (int i=0; i<len; i++) {
            doc = getDocument(Integer.parseInt(ids[i]));
            LeafPriv lp = new LeafPriv(doc.getDirCode());
            if (lp.canUserExamine(privilege.getUser(request))) {
                doc.UpdateExamine(EXAMINE_STATUS); // Document.EXAMINE_PASS);
                // 重新生成第一个列表页
                createListPageHtml(request, doc.getDirCode(), 1);

                // 重新生成其所在的栏目首页
                createColumnPageHtmlOfDoc(request, doc);
            }
            else {
                throw new ErrMsgException(Privilege.MSG_INVALID);
            }
        }
    }

    /**
     * 将文件彻底删除或者删至回收站
     * @param request HttpServletRequest
     * @param id int
     * @param privilege IPrivilege
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean del(HttpServletRequest request, int id, IPrivilege privilege, boolean isDustbin) throws
            ErrMsgException {
        Document doc = new Document();
        doc = getDocument(id);
        if (doc == null || !doc.isLoaded()) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.cms.Document", "err_file_not_found"));
        }

        // 删除文章静态页面
        Config cfg = new Config();
        boolean isDelHtml = cfg.getBooleanProperty("cms.html_auto");
        if (isDelHtml) {
            delAllPageHtml(request, doc);
        }

        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (lp.canUserDel(privilege.getUser(request))) {
            boolean re = false;
            if (isDustbin) {
                re = doc.UpdateExamine(Document.EXAMINE_DUSTBIN);
            }
            else {
                // 在彻底删除文章前先生成HTML，以免在模板中用到该doc时，如{$ad.id(request.id).header}，会出现空指针错误
                if (isDelHtml) {
                    // 重新生成该文章所在列表页静态页面
                    String sql = SQLBuilder.getDirDocListSql(doc.getDirCode());
                    int total = doc.getDocCount(sql);
                    int pageSize = cfg.getIntProperty("cms.listPageSize");
                    ListDocPagniator paginator = new ListDocPagniator(request, total, pageSize);
                    int pageNum = paginator.pageNo2Num(doc.getPageNo());
                    createListPageHtml(request, doc.getDirCode(), pageNum);

                    // 重新生成其所在的栏目首页
                    createColumnPageHtmlOfDoc(request, doc);
                }
                // 彻底删除
                re = doc.del();
                if (re) {
                    // 删除索引　@task:需优化，当删至回收站时对索引的同步删除及恢复
                    Indexer idx = new Indexer();
                    idx.delDocument(id);
                }
            }

            return re;
        }
        else
            throw new ErrMsgException(Privilege.MSG_INVALID);
    }

    public boolean UpdateSummary(ServletContext application,
                                 HttpServletRequest request,
                                 IPrivilege privilege) throws
            ErrMsgException {

        CMSMultiFileUploadBean mfu = doUpload(application, request);
        fileUpload = mfu;

        int id = 0;
        try {
            id = Integer.parseInt(mfu.getFieldValue("id"));
        } catch (Exception e) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    SkinUtil.ERR_ID));
        }
        Document doc = new Document();
        doc = getDocument(id);

        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (!lp.canUserModify(privilege.getUser(request))) {
            throw new ErrMsgException(privilege.MSG_INVALID);
        }

        boolean re = false;
        try {
            re = doc.UpdateSummary(application, mfu);
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public void clearDustbin(HttpServletRequest request) throws ErrMsgException {
        Document doc = new Document();
        doc.clearDustbin();
    }

    public boolean increaseHit(HttpServletRequest request, int id,
                               IPrivilege privilege) throws
            ErrMsgException {
        Document doc = getDocument(id);
        boolean re = doc.increaseHit();
        return re;
    }

    public boolean UpdateIsHome(HttpServletRequest request, int id,
                                IPrivilege privilege) throws
            ErrMsgException {
        Document doc = getDocument(id);
        String v = ParamUtil.get(request, "value");
        boolean re = doc.UpdateIsHome(v.equals("y") ? true : false);
        return re;
    }

    public boolean vote(HttpServletRequest request, int id) throws
            ErrMsgException {
               Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));
        String[] opts = ParamUtil.getParameters(request, "votesel");
        if (opts==null)
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.MsgDb", "err_vote_none"));

        String name = privilege.getUser(request);


        DocPollDb mpd = new DocPollDb();
        mpd = (DocPollDb)mpd.getQObjectDb(new Integer(id));

        Date d = mpd.getDate("expire_date");

        // 检查是否已过期
        if (d!=null) {
            if (DateUtil.compare(d, new java.util.Date()) != 1)
                throw new ErrMsgException(StrUtil.format(SkinUtil.LoadString(request,"res.forum.MsgDb",
                        "err_vote_expire"),
                                          new Object[] {DateUtil.format(d, "yyyy-MM-dd")}));
        }

        int len = opts.length;
        int max_choice = mpd.getInt("max_choice");
        if (len > max_choice) {
            throw new ErrMsgException(StrUtil.format(SkinUtil.LoadString(request,"res.forum.MsgDb",
                    "err_vote_max_count"),
                                          new Object[] {"" + max_choice}));
        }

        // 检查用户是否已投过票
        DocPollOptionDb mpod = new DocPollOptionDb();
        Vector v = mpd.getOptions(id);
        int optLen = v.size();
        for (int i=0; i<optLen; i++) {
            DocPollOptionDb mo = mpod.getDocPollOptionDb(id, i);
            String vote_user = StrUtil.getNullString(mo.getString("vote_user"));
            String[] ary = StrUtil.split(vote_user, ",");
            // System.out.println(getClass() + " ary=" + ary);
            if (ary!=null) {
                int len2 = ary.length;
                for (int k=0; k<len2; k++) {
                    if (ary[k].equals(name))
                        throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.MsgDb", "err_vote_repeat"));
                }
            }
        }

        boolean re = true;
        for (int i=0; i<len; i++) {
            DocPollOptionDb mo = mpod.getDocPollOptionDb(id, StrUtil.toInt(opts[i]));
            mo.set("vote_count", new Integer(mo.getInt("vote_count") + 1));
            String vote_user = StrUtil.getNullString(mo.getString("vote_user"));
            if (vote_user.equals(""))
                vote_user = name;
            else
                vote_user += "," + name;
            mo.set("vote_user", vote_user);
            try {
                re = mo.save();
            }
            catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
        }

        return re;
    }

    public boolean OperatePage(ServletContext application,
                               HttpServletRequest request, IPrivilege privilege) throws
            ErrMsgException {
        CMSMultiFileUploadBean mfu = doUpload(application, request);

        fileUpload = mfu;

        String op = StrUtil.getNullStr(mfu.getFieldValue("op"));
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));

        boolean isValid = false;

        LeafPriv lp = new LeafPriv();
        lp.setDirCode(dir_code);
        if (op.equals("add")) {
            if (lp.canUserAppend(privilege.getUser(request)))
                isValid = true;
        }
        if (op.equals("edit")) {
            if (lp.canUserModify(privilege.getUser(request)))
                isValid = true;
        }

        if (!isValid)
            throw new ErrMsgException(Privilege.MSG_INVALID);

        String strdoc_id = StrUtil.getNullStr(mfu.getFieldValue("id"));
        int doc_id = Integer.parseInt(strdoc_id);
        Document doc = new Document();
        doc = doc.getDocument(doc_id);

        // logger.info("filepath=" + mfu.getFieldValue("filepath"));

        if (op.equals("add")) {
            String content = StrUtil.getNullStr(mfu.getFieldValue(
                    "htmlcode"));
            boolean re = doc.AddContentPage(application, mfu, content);
            if (re) {
                // 重新生成所有页面的静态页面
                Config cfg = new Config();
                boolean isCreateHtml = cfg.getBooleanProperty("cms.html_auto");
                if (isCreateHtml) {
                    createAllPageHtml(request, doc);
                }
            }
            return re;
        }
        else if (op.equals("edit")) {
            // return doc.EditContentPage(content, pageNum);
            boolean re = doc.EditContentPage(application, mfu);
            if (re) {
                String strpageNum = StrUtil.getNullStr(mfu.getFieldValue(
                        "pageNum"));
                int pageNum = Integer.parseInt(strpageNum);
                // 生成静态页面
                Config cfg = new Config();
                boolean isCreateHtml = cfg.getBooleanProperty("cms.html_auto");
                if (isCreateHtml) {
                    createHtml(request, doc, pageNum);
                }
            }
            return re;
        }

        return false;
    }

    public boolean uploadDocument(ServletContext application,
                                  HttpServletRequest request) throws
            ErrMsgException {
        java.util.Date currentTime = new java.util.Date();
        long inserttime = currentTime.getTime();
        String filenm = String.valueOf(inserttime);
        // String[] extnames = {"jpg", "gif", "xls", "rar", "doc", "rm", "avi",
        //                    "bmp", "swf"};
        FileUpload TheBean = new FileUpload();
        // TheBean.setValidExtname(extnames); // 设置可上传的文件类型
        TheBean.setMaxFileSize(Global.FileSize); // 最大35000K
        int ret = 0;
        try {
            ret = TheBean.doUpload(application, request);
            if (ret == -3) {
                String str = SkinUtil.LoadString(request, "res.cms.Document",
                                                 "err_upload_size");
                str = StrUtil.format(str, new Object[] {"" + Global.FileSize});
                throw new ErrMsgException(str);
            }
            if (ret == -4) {
                throw new ErrMsgException(TheBean.getErrMessage(request));
            }
        } catch (Exception e) {
            logger.error("uploadDocument:" + e.getMessage());
        }
        if (ret == 1) {
            Document doc = new Document();
            boolean re = false;
            try {
                re = doc.uploadDocument(TheBean);
            } catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
            return re;
        } else
            return false;
    }

    /**
     * 创建列表页的静态页面
     * @param request HttpServletRequest
     * @param dirCode String
     * @param pageNum int
     */
    public void createListPageHtml(HttpServletRequest request, String dirCode,
                                   int pageNum) throws ErrMsgException {
        // 检查如果是子站点下面的文章，则不生成HTML
        if (Leaf.isLeafOfSubsite(dirCode))
            return;

        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        try {
            TemplateDb td = lf.getTemplateDb();
            if (td==null) {
                throw new ErrMsgException("获取模板错误，请检查目录上模板组的设置！");
            }
            String filePath = Global.realPath + td.getString("path");

            TemplateLoader tl = new TemplateLoader(request, filePath);

            request.setAttribute("dirCode", dirCode);
            request.setAttribute("isCreateHtml", "true"); // 用于静态化时生成页码的判断
            request.setAttribute("CPages", "" + pageNum);

            FileUtil fu = new FileUtil();
            File f = new File(Global.getRealPath() + lf.getListHtmlPath());
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            // 在生成后，将第pageNum对应的pageNo页码记录至doc中，这里pageNo，对应的是totalpages - pageNum + 1
            Document doc = new Document();
            String sql = SQLBuilder.getDirDocListSql(dirCode);
            int total = doc.getDocCount(sql);
            Config cfg = new Config();
            int pageSize = cfg.getIntProperty("cms.listPageSize");
            ListDocPagniator paginator = new ListDocPagniator(request, total, pageSize);
            int pageNo = paginator.pageNum2No(pageNum);

            request.setAttribute("pageNo", "" + pageNo);

            fu.WriteFile(Global.getRealPath() +
                         lf.getListHtmlNameByPageNo(pageNo),
                         tl.toString(), "UTF-8");

            ListResult lr = doc.listResult(sql, pageNum, pageSize);
            Iterator ir = lr.getResult().iterator();
            while (ir.hasNext()) {
                doc = (Document)ir.next();
                doc.setPageNo(pageNo);
                doc.save();
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("createListPageHtml:" +
                                             e.getMessage());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("createListPageHtm2:" +
                                             e.getMessage());
        }
    }

    public void createSubjectListHtmlOfDirecroty(HttpServletRequest request) throws
            ErrMsgException {
        String dirCode = ParamUtil.get(request, "code");

        boolean isIncludeChildren = ParamUtil.getBoolean(request,
                "isIncludeChildren", false);

        int pageNumBegin = ParamUtil.getInt(request, "pagNumBegin", 1);
        int pageNumEnd = ParamUtil.getInt(request, "pagNumEnd", -1);

        createSubjectListHtmlOfDirectory(request, dirCode, isIncludeChildren, pageNumBegin, pageNumEnd);
    }

    public void createSubjectListHtmlOfDirectory(HttpServletRequest request,
                                          String dirCode,
                                          boolean isIncludeChildren,
                                          int pageNumBegin, int pageNumEnd) throws
            ErrMsgException {
        String sql = SQLBuilder.getSubjectDocListSql(dirCode);

        // System.out.println(getClass() + " createListHtmlOfDirectory: dirCode=" + dirCode);

        SubjectListDb doc = new SubjectListDb();
        int total = doc.getDocCount(sql);

        cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
        int pageSize = cfg.getIntProperty("cms.listPageSize");

        Paginator paginator = new Paginator(request, total, pageSize);

        int totalpages = paginator.getTotalPages();
        if (totalpages == 0) {
            totalpages = 1;
        }

        if (pageNumEnd == -1) {
            pageNumEnd = totalpages;
        }

        // System.out.println(getClass() + " pageNumEnd=" + pageNumEnd);

        for (int num = pageNumBegin; num <= pageNumEnd; num++) {
            createSubjectListPageHtml(request, dirCode, num);
        }

        if (isIncludeChildren) {
            SubjectDb lf = new SubjectDb();
            lf = lf.getSubjectDb(dirCode);
            Vector vchild = lf.getChildren();
            Iterator irchild = vchild.iterator();
            while (irchild.hasNext()) {
                Leaf childlf = (Leaf) irchild.next();
                createSubjectListHtmlOfDirectory(request, childlf.getCode(),
                                          isIncludeChildren, pageNumBegin,
                                          pageNumEnd);
            }
        }
    }


    /**
     * 创建专题列表页的静态页面
     * @param request HttpServletRequest
     * @param dirCode String
     * @param pageNum int
     */
    public void createSubjectListPageHtml(HttpServletRequest request, String subjectCode,
                                   int pageNum) {
        SubjectDb lf = new SubjectDb();
        lf = lf.getSubjectDb(subjectCode);
        try {
            TemplateDb td = lf.getTemplateDb();
            String filePath = Global.realPath + td.getString("path");
            TemplateLoader tl = new TemplateLoader(request, filePath);

            request.setAttribute("dirCode", subjectCode);
            request.setAttribute("isCreateHtml", "true"); // 用于静态化时生成页码的判断
            request.setAttribute("CPages", "" + pageNum);

            FileUtil fu = new FileUtil();
            File f = new File(Global.getRealPath() + lf.getListHtmlPath());
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            // 在生成后，将第pageNum对应的pageNo页码记录至doc中，这里pageNo，对应的是totalpages - pageNum + 1
            Document doc = new Document();
            String sql = SQLBuilder.getSubjectDocListSql(subjectCode);
            int total = doc.getDocCount(sql);
            Config cfg = new Config();
            int pageSize = cfg.getIntProperty("cms.listPageSize");
            ListDocPagniator paginator = new ListDocPagniator(request, total, pageSize);
            int pageNo = paginator.pageNum2No(pageNum);

            request.setAttribute("pageNo", "" + pageNo);

            fu.WriteFile(Global.getRealPath() +
                         lf.getListHtmlNameByPageNo(pageNo),
                         tl.toString(), "UTF-8");

        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("createListPageHtml:" +
                                             e.getMessage());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("createListPageHtm2:" +
                                             e.getMessage());
        }
    }

    /**
     * 生成栏目首页的静态页面
     * @param request HttpServletRequest
     * @param dirCode String
     */
    public void createColumnPageHtml(HttpServletRequest request, String dirCode) {
        Leaf lf = new Leaf();
        lf = lf.getLeaf(dirCode);
        try {
            TemplateDb td = lf.getTemplateDb();
            String filePath = Global.realPath + td.getString("path");
            TemplateLoader tl = new TemplateLoader(request, filePath);

            request.setAttribute("dirCode", dirCode);
            request.setAttribute("isCreateHtml", "true"); // 用于静态化时生成页码的判断

            FileUtil fu = new FileUtil();
            File f = new File(Global.getRealPath() + lf.getListHtmlPath());
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            Config cfg = new Config();

            // System.out.println(getClass() + " htmlpath = " + Global.getRealPath() + lf.getListHtmlPath() +
            //             "/index." + cfg.getProperty("cms.html_ext"));
            fu.WriteFile(Global.getRealPath() + lf.getListHtmlPath() +
                         "/index." + cfg.getProperty("cms.html_ext"),
                         tl.toString(), "UTF-8");
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("createColumnPageHtml:" +
                                             e.getMessage());
        } catch (ErrMsgException e) {
            LogUtil.getLog(getClass()).error("createColumnPageHtm2:" +
                                             e.getMessage());
        }
    }

    /**
     * 删除静态页面
     * @param request HttpServletRequest
     * @param doc Document
     * @throws ErrMsgException
     */
    public void delAllPageHtml(HttpServletRequest request, Document doc) throws ErrMsgException  {
        for (int i = 0; i < doc.getPageCountPlugin(); i++) {
            File f = new File(Global.getRealPath() + doc.getDocHtmlName(i + 1));
            f.delete();
        }
    }

    /**
     * 生成某篇文章的所有的页面
     * @param request HttpServletRequest
     * @param doc Document
     */
    public void createAllPageHtml(HttpServletRequest request, Document doc) throws ErrMsgException  {
        for (int i = 0; i < doc.getPageCountPlugin(); i++) {
            createHtml(request, doc, i + 1);
        }
    }

    /**
     * 生成单个页面
     * @param request HttpServletRequest
     * @param doc Document
     * @param pageNum int
     */
    public void createHtml(HttpServletRequest request, Document doc,
                           int pageNum) throws ErrMsgException {
        // 检查如果是子站点下面的文章，则不生成HTML
        if (Leaf.isLeafOfSubsite(doc.getDirCode()))
            return;
        String filePath = "";
        TemplateDb td = doc.getTemplateDb();

        filePath = Global.getRealPath() + td.getString("path");
        try {
            // 加入ID标识
            request.setAttribute("id", "" + doc.getID());
            request.setAttribute("isCreateHtml", "true"); // 用于静态化时生成页码的判断
            request.setAttribute("CPages", "" + pageNum);
            // LogUtil.getLog(getClass()).info("createHtml1:id=" + doc.getID() + " CPages=" + pageNum);
            TemplateLoader tl = new TemplateLoader(request, filePath);
            FileUtil fu = new FileUtil();
            File f = new File(Global.getRealPath() + doc.getDocHtmlPath());
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            fu.WriteFile(Global.getRealPath() +
                         doc.getDocHtmlName(pageNum),
                         tl.toString(), "UTF-8");
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("createHtml:" + e.getMessage());
        }
    }

    /**
     * 从cms/document_list.jsp中生成静态列表页面
     * @param request HttpServletRequest
     * @throws ErrMsgException
     */
    public void createListHtmlOfDirecroty(HttpServletRequest request) throws
            ErrMsgException {
        String dirCode = ParamUtil.get(request, "dir_code");

        boolean isIncludeChildren = ParamUtil.getBoolean(request,
                "isIncludeChildren", false);

        int pageNumBegin = ParamUtil.getInt(request, "pagNumBegin", 1);
        int pageNumEnd = ParamUtil.getInt(request, "pagNumEnd", -1);

        createListHtmlOfDirectory(request, dirCode, isIncludeChildren, pageNumBegin, pageNumEnd);
    }

    public void createListHtmlOfDirectory(HttpServletRequest request,
                                          String dirCode,
                                          boolean isIncludeChildren,
                                          int pageNumBegin, int pageNumEnd) throws
            ErrMsgException {
        String sql = SQLBuilder.getDirDocListSql(dirCode);

        // System.out.println(getClass() + " createListHtmlOfDirectory: dirCode=" + dirCode);

        Document doc = new Document();
        int total = doc.getDocCount(sql);

        cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
        int pageSize = cfg.getIntProperty("cms.listPageSize");

        Paginator paginator = new Paginator(request, total, pageSize);

        int totalpages = paginator.getTotalPages();
        if (totalpages == 0) {
            totalpages = 1;
        }

        if (pageNumEnd == -1) {
            pageNumEnd = totalpages;
        }

        for (int num = pageNumBegin; num <= pageNumEnd; num++) {
            createListPageHtml(request, dirCode, num);
        }
        if (isIncludeChildren) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(dirCode);
            Vector vchild = lf.getChildren();
            Iterator irchild = vchild.iterator();
            while (irchild.hasNext()) {
                Leaf childlf = (Leaf) irchild.next();
                createListHtmlOfDirectory(request, childlf.getCode(),
                                          isIncludeChildren, pageNumBegin,
                                          pageNumEnd);
            }
        }
    }

    public void createHtmlOfDirecroty(HttpServletRequest request) throws
            ErrMsgException {
        String dirCode = ParamUtil.get(request, "dir_code");
        String bDate = ParamUtil.get(request, "beginDate");
        String eDate = ParamUtil.get(request, "endDate");
        Date beginDate = DateUtil.parse(bDate, "yyyy-MM-dd");
        Date endDate = DateUtil.parse(eDate, "yyyy-MM-dd");
        if (beginDate == null || endDate == null) {
            // throw new ErrMsgException("请填写开始和结束日期！");
        }
        boolean isIncludeChildren = ParamUtil.getBoolean(request,
                "isIncludeChildren", false);

        String sql;
        if (beginDate==null && endDate==null)
            sql = "select id from document where class1=? and examine=2";
        else if (beginDate==null) {
            sql = "select id from document where class1=? and modifiedDate<=? and examine=2";
        }
        else if (endDate==null) {
            sql = "select id from document where class1=? and modifiedDate>=? and examine=2";
        }
        else
            sql = "select id from document where class1=? and modifiedDate>=? and modifiedDate<=? and examine=2";

        createHtmlOfDirectory(request, dirCode, sql, isIncludeChildren, beginDate, endDate);
    }

    public void createHtmlOfDirectory(HttpServletRequest request,
                                      String dirCode, String sql,
                                      boolean isIncludeChildren, Date beginDate, Date endDate) throws ErrMsgException  {
        Object[] params = null;
        if (beginDate==null && endDate==null)
            params = new Object[] {dirCode};
        else if (beginDate == null) {
            params = new Object[] {dirCode,
                     DateUtil.toLongString(endDate)};
        }
        else if (endDate==null) {
            params = new Object[] {dirCode,
                     DateUtil.toLongString(beginDate)};
        }
        else
            params = new Object[] {dirCode,
                          DateUtil.toLongString(beginDate),
                          DateUtil.toLongString(endDate)};
        Document doc = new Document();
        Vector v = doc.list(sql, params);
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            doc = (Document) ir.next();
            // System.out.println(getClass() + " dirCode=" + dirCode + " title=" +
            //                   doc.getTitle());

            createAllPageHtml(request, doc);
        }

        if (isIncludeChildren) {
            Leaf lf = new Leaf();
            lf = lf.getLeaf(dirCode);
            Vector vchild = lf.getChildren();
            Iterator irchild = vchild.iterator();
            while (irchild.hasNext()) {
                Leaf childlf = (Leaf) irchild.next();
                createHtmlOfDirectory(request, childlf.getCode(), sql, isIncludeChildren, beginDate, endDate);
            }
        }
    }

    public boolean ChangeDir(HttpServletRequest request, int id,
                             String newDirCode) throws ErrMsgException {
        Privilege privilege = new Privilege();
        if (!privilege.isUserPrivValid(request, Priv.PRIV_ADMIN)) {
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    SkinUtil.PVG_INVALID));
        }
        Document doc = getDocument(id);
        return doc.ChangeDir(newDirCode);
    }


    public void createSubjectHtmlOfDirecroty(HttpServletRequest request) throws
            ErrMsgException {
        String dirCode = ParamUtil.get(request, "code");
        String bDate = ParamUtil.get(request, "beginDate");
        String eDate = ParamUtil.get(request, "endDate");
        Date beginDate = DateUtil.parse(bDate, "yyyy-MM-dd");
        Date endDate = DateUtil.parse(eDate, "yyyy-MM-dd");
        if (beginDate == null || endDate == null) {
            // throw new ErrMsgException("请填写开始和结束日期！");
        }
        boolean isIncludeChildren = ParamUtil.getBoolean(request,
                "isIncludeChildren", false);

        String sql;

        if (beginDate==null && endDate==null)
            sql = "select d.id from cws_cms_subject_doc s,document d where s.doc_id=d.id and s.code=? and d.examine=2";
        else if (beginDate==null) {
            sql = "select d.id from cws_cms_subject_doc s,document d where s.doc_id=d.id and s.code=? and d.modifiedDate<=? and d.examine=2";
        }
        else if (endDate==null) {
            sql = "select d.id from cws_cms_subject_doc s,document d where s.doc_id=d.id and s.code=? and d.modifiedDate>=? and d.examine=2";
        }
        else
            sql = "select d.id from cws_cms_subject_doc s,document d where s.doc_id=d.id and s.code=? and d.modifiedDate>=? and d.modifiedDate<=? and examine=2";

        createSubjectHtmlOfDirectory(request, dirCode, sql, isIncludeChildren, beginDate, endDate);
    }

    public void createSubjectHtmlOfDirectory(HttpServletRequest request,
                                      String subjectCode, String sql,
                                      boolean isIncludeChildren, Date beginDate, Date endDate) throws ErrMsgException  {
        Object[] params = null;
        if (beginDate==null && endDate==null)
            params = new Object[] {subjectCode};
        else if (beginDate == null) {
            params = new Object[] {subjectCode,
                     DateUtil.toLongString(endDate)};
        }
        else if (endDate==null) {
            params = new Object[] {subjectCode,
                     DateUtil.toLongString(beginDate)};
        }
        else
            params = new Object[] {subjectCode,
                          DateUtil.toLongString(beginDate),
                          DateUtil.toLongString(endDate)};

        // System.out.println(getClass() + " createSubjectHtmlOfDirectory:" + sql + " subjectCode=" + subjectCode);

        Document doc = new Document();
        Vector v = doc.list(sql, params);

        // System.out.println(getClass() + " createSubjectHtmlOfDirectory:" + sql + " v.size=" + v.size());

        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            doc = (Document) ir.next();
            // System.out.println(getClass() + " dirCode=" + dirCode + " title=" +
            //                   doc.getTitle());

            createSubjectAllPageHtml(request, subjectCode, doc);
        }

        if (isIncludeChildren) {
            SubjectDb lf = new SubjectDb();
            lf = lf.getSubjectDb(subjectCode);
            Vector vchild = lf.getChildren();
            Iterator irchild = vchild.iterator();
            while (irchild.hasNext()) {
                Leaf childlf = (Leaf) irchild.next();
                createSubjectHtmlOfDirectory(request, childlf.getCode(), sql, isIncludeChildren, beginDate, endDate);
            }
        }
    }

    /**
     * 生成专题文章所有的页面
     * @param request HttpServletRequest
     * @param doc Document
     */
    public void createSubjectAllPageHtml(HttpServletRequest request, String subjectCode, Document doc) throws ErrMsgException  {
        for (int i = 0; i < doc.getPageCountPlugin(); i++) {
            createSubjectHtml(request, subjectCode, doc, i + 1);
        }
    }

    /**
     * 生成专题文章的单个页面
     * @param request HttpServletRequest
     * @param doc Document
     * @param pageNum int
     */
    public void createSubjectHtml(HttpServletRequest request, String subjectCode, Document doc,
                           int pageNum) throws ErrMsgException {
        String filePath = "";
        SubjectDb sd = new SubjectDb();
        sd = sd.getSubjectDb(subjectCode);

        TemplateDb td = new TemplateDb();
        if (sd.getPageTemplateId()!=SubjectDb.NOTEMPLATE) {
                td = td.getTemplateDb(sd.getPageTemplateId());
        }
        else {
            td = doc.getTemplateDb();
        }

        filePath = Global.getRealPath() + td.getString("path");
        try {
            // 加入ID标识
            request.setAttribute("id", "" + doc.getID());
            request.setAttribute("isCreateHtml", "true"); // 用于静态化时生成页码的判断
            request.setAttribute("CPages", "" + pageNum);
            // LogUtil.getLog(getClass()).info("createHtml1:id=" + doc.getID() + " CPages=" + pageNum);
            TemplateLoader tl = new TemplateLoader(request, filePath);
            FileUtil fu = new FileUtil();
            File f = new File(Global.getRealPath() + SubjectListDb.getDocHtmlPath(subjectCode, doc));
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            fu.WriteFile(Global.getRealPath() +
                         SubjectListDb.getDocHtmlName(subjectCode, doc, pageNum),
                         tl.toString(), "UTF-8");
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("createHtml:" + e.getMessage());
        }
    }

    /**
     * 往文章中插入图片
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return String[] 图片的ID数组
     * @throws ErrMsgException
     */
    public String[] uploadImg(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        // if (!privilege.isUserLogin(request))
        //    throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));

        FileUpload fu = new FileUpload();
        String[] ext = new String[] {"jpg", "gif", "png", "bmp", "swf"};
        if (ext!=null)
            fu.setValidExtname(ext);

        int ret = 0;
        try {
            ret = fu.doUpload(application, request);
            if (ret!=fu.RET_SUCCESS) {
                throw new ErrMsgException(fu.getErrMessage(request));
            }
            if (fu.getFiles().size()==0)
                throw new ErrMsgException("请上传文件！");
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }

        Calendar cal = Calendar.getInstance();
        String year = "" + (cal.get(cal.YEAR));
        String month = "" + (cal.get(cal.MONTH) + 1);
        String virtualpath = year +
                             "/" +
                             month;

        Config cfg = new Config();
        String attPath = cfg.getProperty("cms.file_doc_attach");

        String filepath = Global.getRealPath() + attPath + "/" +
                          virtualpath + "/";

        File f = new File(filepath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }

        fu.setSavePath(filepath); // 设置保存的目录
        // logger.info(filepath);
        String[] re = null;
        Vector v = fu.getFiles();
        Iterator ir = v.iterator();
        int orders = 0;

        String attachmentBasePath = request.getContextPath() + "/";

        if (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            // 保存至磁盘相应路径
            String fname = FileUpload.getRandName() + "." +
                           fi.getExt();
            // 记录于数据库
            Attachment att = new Attachment();
            att.setDiskName(fi.getDiskName());
            // logger.info(fpath);
            att.setDocId(att.TEMP_DOC_ID);
            att.setName(fi.getName());
            att.setDiskName(fname);
            att.setOrders(orders);
            att.setVisualPath(attPath + "/" + virtualpath);
            att.setUploadDate(new java.util.Date());
            att.setSize(fi.getSize());
            if (att.create()) {
                fi.write(filepath, fname);
                re = new String[2];
                re[0] = "" + att.getId();

                re[1] = attachmentBasePath + att.getVisualPath() + "/" + att.getDiskName();
            }
        }

        return re;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public String getDirCode() {
        return dirCode;
    }

    private FileUpload fileUpload;

    private String dirCode;
}
