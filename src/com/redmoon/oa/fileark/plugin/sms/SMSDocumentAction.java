package com.redmoon.oa.fileark.plugin.sms;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.module.cms.plugin.wiki.WikiDocumentDb;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.aop.*;
import com.cloudwebsoft.framework.aop.Pointcut.*;
import com.cloudwebsoft.framework.aop.base.*;
import com.redmoon.oa.fileark.*;
import com.redmoon.oa.fileark.plugin.base.*;
import com.redmoon.oa.message.*;
import org.apache.log4j.*;
import org.htmlparser.*;
import org.htmlparser.filters.*;
import org.htmlparser.nodes.*;
import org.htmlparser.tags.*;
import org.htmlparser.util.*;

public class SMSDocumentAction implements IPluginDocumentAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public SMSDocumentAction() {
    }

    public static String getTextFromHTML(String content) {
        String str = "";
        try {
            Parser myParser;
            NodeList nodeList = null;
            myParser = Parser.createParser(content, "utf-8");
            NodeFilter textFilter = new NodeClassFilter(TextNode.class);
            NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
            NodeFilter imgFilter = new NodeClassFilter(ImageTag.class);
            // 暂时不处理 meta
            // NodeFilter metaFilter = new NodeClassFilter(MetaTag.class);
            OrFilter lastFilter = new OrFilter();
            lastFilter.setPredicates(new NodeFilter[] {textFilter, linkFilter,
                                     imgFilter});
            nodeList = myParser.parse(lastFilter);
            Node[] nodes = nodeList.toNodeArray();
            for (int i = 0; i < nodes.length; i++) {
                Node anode = (Node) nodes[i];
                String line = "";
                if (anode instanceof TextNode) {
                    TextNode textnode = (TextNode) anode;
                    // line = textnode.toPlainTextString().trim();
                    line = textnode.getText();
                }
                str += line;
            }
        }
        catch (ParserException e) {
            Logger.getLogger(SMSDocumentAction.class).error("getAbstract:" + e.getMessage());
        }
        return str;
    }
    
    public boolean del(HttpServletRequest request, Document doc, boolean isToDustbin) throws ErrMsgException {
    	return true;
    }    

    public boolean create(ServletContext application, HttpServletRequest request,
                          CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException {
        String receiver = StrUtil.getNullStr(mfu.getFieldValue("receiver"));
        boolean isToMobile = StrUtil.getNullStr(mfu.getFieldValue("isToMobile")).equals("true");
        IMessage imsg = null;
        String[] users = StrUtil.split(receiver, ",");
        if (users==null)
            return true;
        // 当审查通过时，才发送信息
        if (doc.getExamine()==doc.EXAMINE_PASS) {
            if (isToMobile) {
                ProxyFactory proxyFactory = new ProxyFactory(
                        "com.redmoon.oa.message.MessageDb");
                Advisor adv = new Advisor();
                MobileAfterAdvice mba = new MobileAfterAdvice();
                adv.setAdvice(mba);
                adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
                proxyFactory.addAdvisor(adv);
                imsg = (IMessage) proxyFactory.getProxy();
                int len = users.length;
                String msgContent = getTextFromHTML(doc.getContent(1));

                for (int i = 0; i < len; i++) {
                    imsg.sendSysMsg(users[i], doc.getTitle(), msgContent);
                }
            }
            else {
                // 发送信息
                MessageDb md = new MessageDb();
                int len = users.length;
                for (int i = 0; i < len; i++) {
                    md.sendSysMsg(users[i], doc.getTitle(), doc.getContent(1));
                }
            }
        }
        return true;
    }

    public boolean update(ServletContext application,HttpServletRequest request,
                   CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException {
        String receiver = StrUtil.getNullStr(mfu.getFieldValue("receiver"));
        boolean isToMobile = StrUtil.getNullStr(mfu.getFieldValue("isToMobile")).equals("true");
        IMessage imsg = null;
        String[] users = StrUtil.split(receiver, ",");
        if (users==null)
            return true;
        // 当审查通过时，才发送信息
        if (doc.getExamine()==doc.EXAMINE_PASS) {
            if (isToMobile) {
                ProxyFactory proxyFactory = new ProxyFactory(
                        "com.redmoon.oa.message.MessageDb");
                Advisor adv = new Advisor();
                MobileAfterAdvice mba = new MobileAfterAdvice();
                adv.setAdvice(mba);
                adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
                proxyFactory.addAdvisor(adv);
                imsg = (IMessage) proxyFactory.getProxy();
                int len = users.length;
                String msgContent = getTextFromHTML(doc.getContent(1));

                for (int i = 0; i < len; i++) {
                    imsg.sendSysMsg(users[i], doc.getTitle(), msgContent);
                }
            } else {
                // 发送信息
                MessageDb md = new MessageDb();
                int len = users.length;
                for (int i = 0; i < len; i++) {
                    md.sendSysMsg(users[i], doc.getTitle(), doc.getContent(1));
                }
            }
        }
        return true;
    }
}
