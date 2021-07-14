package com.redmoon.forum.plugin2.alipay;

import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import com.redmoon.forum.plugin.base.IPluginMsgAction;
import org.apache.log4j.Logger;
import com.redmoon.forum.MsgDb;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.ResKeyException;
import com.redmoon.kit.util.FileUpload;
import cn.js.fan.web.SkinUtil;

public class AlipayMsgAction implements IPluginMsgAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public AlipayMsgAction() {
    }

    public boolean AddNew(ServletContext application,
                          HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        String plugin2Code = "alipay";
        String seller = StrUtil.getNullStr(fu.getFieldValue("alipay_seller")).trim();
        logger.info("AddNew: seller=" + seller);
        if (seller.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.plugin2.alipay", "err_want_seller"));
        String subject = StrUtil.getNullStr(fu.getFieldValue("alipay_subject")).trim();
        if (subject.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.plugin2.alipay", "err_want_subject"));
        String price = StrUtil.getNullStr(fu.getFieldValue("alipay_price"));
        if (price.equals(""))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.plugin2.alipay", "err_want_price"));

        String transport = StrUtil.getNullStr(fu.getFieldValue("alipay_transport"));
        String demo = StrUtil.getNullStr(fu.getFieldValue("alipay_demo"));
        String ordinary = StrUtil.getNullStr(fu.getFieldValue("alipay_ordinary"));
        String express = StrUtil.getNullStr(fu.getFieldValue("alipay_express"));
        String ww = StrUtil.getNullStr(fu.getFieldValue("alipay_ww"));
        String qq = StrUtil.getNullStr(fu.getFieldValue("alipay_qq"));
        if (!StrUtil.isNumeric(qq)) {
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.plugin2.alipay", "err_QQ"));
        }

        AlipayDb ad = new AlipayDb();
        ad.setMsgRootId(md.getId());
        ad.setSeller(seller);
        ad.setSubject(subject);
        ad.setPrice(price);
        ad.setTransport(Integer.parseInt(transport));
        ad.setDemo(demo);
        ad.setOrdinary(ordinary);
        ad.setExpress(express);
        ad.setWw(ww);
        ad.setQq(qq);
        return ad.create();
    }

    /**
     *
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param md MsgDb 所存储的是ReceiveData后得来的信息
     * @return boolean
     * @throws ErrMsgException
     */
    public synchronized boolean editTopic(ServletContext application,
                                          HttpServletRequest request,
                                          MsgDb md, FileUpload fu) throws
            ErrMsgException {
        boolean re = true;
        logger.info("editTopic:replyId=" + md.getReplyid() + " id=" + md.getId());
        if (md.getReplyid() == -1) {
            String seller = StrUtil.getNullStr(fu.getFieldValue("alipay_seller")).trim();
            if (seller.equals(""))
                throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.plugin2.alipay", "err_want_seller"));
            String subject = StrUtil.getNullStr(fu.getFieldValue("alipay_subject")).trim();
            if (subject.equals(""))
                throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.plugin2.alipay", "err_want_subject"));
            String price = StrUtil.getNullStr(fu.getFieldValue("alipay_price"));
            if (price.equals(""))
                throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.plugin2.alipay", "err_want_price"));
            String transport = StrUtil.getNullStr(fu.getFieldValue("alipay_transport"));
            String demo = StrUtil.getNullStr(fu.getFieldValue("alipay_demo"));
            String ordinary = StrUtil.getNullStr(fu.getFieldValue("alipay_ordinary"));
            String express = StrUtil.getNullStr(fu.getFieldValue("alipay_express"));
            String ww = StrUtil.getNullStr(fu.getFieldValue("alipay_ww"));
            String qq = StrUtil.getNullStr(fu.getFieldValue("alipay_qq"));
            if (!StrUtil.isNumeric(qq)) {
                throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.plugin2.alipay", "err_QQ"));
            }

            AlipayDb ad = new AlipayDb();
            ad = ad.getAlipaydDb(md.getId());
            ad.setSeller(seller);
            ad.setSubject(subject);
            ad.setPrice(price);
            ad.setTransport(Integer.parseInt(transport));
            ad.setDemo(demo);
            ad.setOrdinary(ordinary);
            ad.setExpress(express);
            ad.setWw(ww);
            ad.setQq(qq);
            re = ad.save();
        }
        return re;
    }

    public boolean AddQuickReply(ServletContext application,
                                 HttpServletRequest request, long replyMsgId) throws
            ErrMsgException {
        return true;
    }

    /**
     * 本方法置于MsgMgr中delTopic真正删除贴子之前，使在删除插件相应内容后，再删除贴子本身
     * @param application ServletContext
     * @param request HttpServletRequest
     * @param md MsgDb
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delTopic(ServletContext application,
                            HttpServletRequest request, MsgDb md) throws
            ErrMsgException {
        // 如果是删除根贴
        if (md.getReplyid() == -1) {
            AlipayDb ad = new AlipayDb();
            ad = ad.getAlipaydDb(md.getId());
            if (ad.isLoaded()) {
                return ad.del();
            }
        }
        return true;
    }

    /**
     * 此函数置于MsgDb delSingleMsg中真正删除贴子之前，以便于递归删除贴子
     * @param delId int
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean delSingleMsg(long delId) throws
            ResKeyException {
        return true;
    }

    public boolean AddReply(ServletContext application,
                            HttpServletRequest request, MsgDb md, FileUpload fu) throws
            ErrMsgException {
        return true;
    }
}
