package cn.js.fan.module.cms.job;

import java.util.*;

import cn.js.fan.mail.*;
import cn.js.fan.module.cms.*;
import cn.js.fan.module.cms.Config;
import cn.js.fan.module.cms.ext.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.forum.person.*;
import org.quartz.*;


/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class EmailSubscribeJob implements Job {
    public EmailSubscribeJob() {
    }

    public void send(String to, String subject, String senderName,
                     String content) {
        SendMail sendmail = new SendMail();
        senderName = StrUtil.GBToUnicode(senderName);
        senderName += "<" + Global.getEmail() + ">";
        try {
            sendmail.initSession(Global.getSmtpServer(), Global.getSmtpPort(),
                                 Global.getSmtpUser(), Global.getSmtpPwd());
            sendmail.initMsg(to, senderName, subject, content, true);
            sendmail.send();
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("send:" + e.getMessage());
        } finally {
            sendmail.clear();
        }
    }

    /**
     * execute
     *
     * @param jobExecutionContext JobExecutionContext
     * @throws JobExecutionException
     * @todo Implement this org.quartz.Job method
     */
    public void execute(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        UserEmailSubscribeDb uesd = new UserEmailSubscribeDb();
        Vector v = uesd.list();
        Iterator ir = v.iterator();
        Config cfg = new Config();
        UserMgr um = new UserMgr();

        while (ir.hasNext()) {
            uesd = (UserEmailSubscribeDb) ir.next();
            String dirCodes = uesd.getString("cms_dirs");
            LogUtil.getLog(getClass()).info("execute:dirCodes" + dirCodes);

            String moduleCodes = uesd.getString("module_codes");
            UserDb user = um.getUser(uesd.getString("user_name"));
            String str = "";

            String[] d = StrUtil.split(dirCodes, ",");
            if (d != null) {
                Directory dir = new Directory();
                int len = d.length;

                Document doc = new Document();
                for (int i = 0; i < len; i++) {
                    Leaf lf = dir.getLeaf(d[i]);
                    if (lf != null && lf.isLoaded()) {
                        String query = SQLBuilder.getDirDocListSql(lf.getCode());
                        String groupKey = lf.getCode();
                        // LogUtil.getLog(getClass()).info("execute:" + query);
                        // LogUtil.getLog(getClass()).info("execute:emailSubscribeCount=" + cfg.getIntProperty("cms.emailSubscribeCount"));

                        ir = doc.getDocuments(query, groupKey, 0,
                                              cfg.
                                              getIntProperty(
                                "cms.emailSubscribeCount"));
                        while (ir.hasNext()) {
                            doc = (Document) ir.next();
                            str += "<a href='" + Global.getRootPath() +
                                    "/doc_show.jsp?id=" + doc.getId() + "'>" +
                                    doc.getTitle() + "</a><BR>";
                        }
                    }
                }
            }

            if (!str.equals(""))
                send(user.getEmail(), Global.AppName, Global.AppName, str);

            try {
                Thread.sleep(5);
            } catch (Exception e) {

            }
        }
    }


}
