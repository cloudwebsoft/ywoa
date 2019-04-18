package com.redmoon.forum.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.util.*;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.*;
import com.redmoon.forum.person.UserDb;
import com.redmoon.forum.plugin.base.IPluginRender;
import org.apache.log4j.Logger;
import com.redmoon.forum.plugin.base.IPluginScore;
import com.redmoon.forum.person.UserGroupDb;
import com.redmoon.forum.person.UserGroupPrivDb;
import java.util.Vector;
import com.redmoon.forum.person.UserMgr;
import com.redmoon.forum.setup.UserLevelDb;
import com.redmoon.forum.ui.ForumPage;


/**
 * 一个版块只能有一个显示插件生效
 * @author Administrator
 *
 */
public class DefaultRender implements IPluginRender {
    static Logger logger = Logger.getLogger(DefaultRender.class.getName());

    public DefaultRender() {
    }
    
    public String RenderThreadTitle(HttpServletRequest request, MsgDb md, int length) {
        StringBuffer sb = new StringBuffer();
        sb.append("<a title=\"" + StrUtil.toHtml(md.getTitle()) + "\" href=\"" + ForumPage.getShowTopicPage(request, md.getId()) + "\">");

		String color = StrUtil.getNullString(md.getColor());
		String tp = RenderTitle(request, md, 76);
		if (!color.equals(""))
			tp = "<font color='" + color + "'>" + tp + "</font>";
		if (md.isBold())
			tp = "<B>" + tp + "</B>";
        sb.append(tp);
        sb.append("</a>");
        return sb.toString();
    }

    /**
     * 显示标题，用于showtopic.jsp和showtopic_tree.jsp
     * @param request HttpServletRequest
     * @param md MsgDb
     * @return String
     */
    public String RenderTitle(HttpServletRequest request, MsgDb md) {
        if (!md.isLoaded())
            return "";
        if (!md.getName().equals("")) {
            UserDb user = new UserDb();
            user = user.getUser(md.getName());
            if (!user.isValid()) {
                return "===========";
            }
        }
        String str = "";
        if (md.getExpression()!=MsgDb.EXPRESSION_NONE)
            str += "<img src='images/brow/" + md.getExpression() + ".gif' border=0>&nbsp;";
        str += "<b>" + StrUtil.ubb(request, StrUtil.toHtml(md.getTitle()), true) + "</b>";
        return str;
    }

    /**
     * 显示标题，用于除showtopic.jsp和showtopic_tree.jsp外的页面
     * @param request
     * @param md
     * @return
     */
    public static String RenderFullTitle(HttpServletRequest request, MsgDb md) {
        if (!md.isLoaded())
            return "";
        boolean isUBB = false;
        if (!md.getName().equals("")) {
            UserDb user = new UserDb();
            user = user.getUser(md.getName());
            if (user.isLoaded() && !user.isValid()) {
                return "===========";
            }
            if (user.isLoaded()) {
                com.redmoon.forum.Config cfg = com.redmoon.forum.Config.
                                               getInstance();
                isUBB = cfg.getBooleanProperty("forum.isUBBTopicTitle");
                if (isUBB) {
                    int level = StrUtil.toInt(cfg.getProperty(
                            "forum.UBBTopicTitleUserLevel"), 0);
                    // 如果用户等级小于UBB要求的等级
                    if (level > 0 && user.getUserLevelDb().getLevel() < level) {
                        isUBB = false;
                    }
                }
            }
        }
        String str = StrUtil.toHtml(md.getTitle());
        if (request!=null) {
        	// 因为有可能在调度中被调用,如BlogCacherefreshHomePage,此时request为null
	        if (isUBB)
	            str = StrUtil.ubb(request, str, true);
        }
        return str;
    }

    /**
     * 用于listtopic.jsp等列表页显示标题
     * @param request HttpServletRequest
     * @param md MsgDb
     * @param len int
     * @return String
     */
    public static String RenderTitle(HttpServletRequest request, MsgDb md, int len) {
        String str = RenderFullTitle(request, md);
        // if (len>0)
            str = StrUtil.getLeft(str, len);
        return str;
    }

    public MsgPollDb RenderVote(HttpServletRequest request, MsgDb md) {
        UserDb user = new UserDb();
        user = user.getUser(md.getName());
        if (user.isValid()) {
            MsgPollDb mpd = new MsgPollDb();
            return (MsgPollDb)mpd.getQObjectDb(new Long(md.getId()));
        }
        else
            return null;
    }

    public String RenderContent(HttpServletRequest request, MsgDb md) {
        return doRendContent(request, md);
    }

    public String LoadString(HttpServletRequest request, String key) {
        return SkinUtil.LoadString(request, "res.forum.plugin.DefaultRender", key);
    }

    public String doRendContentCommon(HttpServletRequest request, MsgDb md) {
        String content = md.getContent();
        // 如果是UBB的格式
        if (md.getIsWebedit() == md.WEBEDIT_UBB) {
            content = StrUtil.toHtml(content);
            // UBB代码处理
            if (md.getShowUbbcode() == 1) {
                if (md.getShowSmile() == 1) {
                    content = StrUtil.ubb(request, content, true);
                } else
                    content = StrUtil.ubb(request, content, false);
            }
        } else {
            content = StrUtil.ubbWithoutAutoLink(request, content);
        }
        return content;
    }

    public String doRendContent(HttpServletRequest request, MsgDb md) {
        UserMgr um = new UserMgr();
        if (!md.getName().equals("")) {
            UserDb user = um.getUser(md.getName());
            if (!user.isValid()) {
                showAttachment = false;
                return LoadString(request, "info_user_invalid");
            }
        }

        String content = doRendContentCommon(request, md);

        Privilege privilege = new Privilege();
        String seeUserName = privilege.getUser(request);

        String subStr = LoadString(request, "info_need_reply"); // "===== 以下为隐藏信息 回帖后才能显示 =====";
        String subStrExp; // "===== 以下为隐藏信息 经验值高于$e时才能显示 =====";

        String patternStr = "";
        Pattern pattern;
        Matcher matcher;
        // 处理回复可见, 如果是管理员，则可见
        boolean canEdit = false;
        try {
            canEdit = privilege.canEdit(request, md);
        }
        catch (ErrMsgException e) {
            logger.info("RenderContent:" + e.getMessage());
        }
        if (canEdit) {
            content = replyCanSee(content, true, subStr);
        } else {
            patternStr = "(\\[REPLY\\])(.*?)(\\[\\/REPLY\\])";
            pattern = Pattern.compile(patternStr,
                                      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(content);
            // 如果存在有限制回复可见的UBB代码
            if (matcher.find()) {
                boolean canSee = false;
                // 如果是根贴
                if (md.getReplyid()==-1)
                    canSee = md.isUserReplyerOfMsg(seeUserName);
                else { // 如果是回贴
                    long rootid = md.getRootid();
                    MsgDb rootMsg = md.getMsgDb(rootid);
                    canSee = rootMsg.isUserReplyerOfMsg(seeUserName);
                }
                if (canSee) {
                    content = matcher.replaceAll(subStr + "<BR>" + "$2");
                }
                else {
                    content = matcher.replaceAll(subStr);
                    showAttachment = false;
                }
            }
        }
        if (canEdit) {
            String ownerSubStr = LoadString(request, "info_owner_see"); // "===== 以下为隐藏信息 仅楼主可见 =====";
            content = ownerCanSee(content, true, ownerSubStr);
        } else {
            patternStr = "(\\[OWNER\\])(.*?)(\\[\\/OWNER\\])";
            pattern = Pattern.compile(patternStr,
                                      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(content);
            // 如果存在有限制楼主可见的UBB代码
            if (matcher.find()) {
                boolean canSee = false;
                // 如果是根贴
                if (md.getReplyid()==-1)
                    canSee = true;
                else { // 如果是回贴
                    long rootid = md.getRootid();
                    MsgDb rootMsg = md.getMsgDb(rootid);
                    canSee = rootMsg.getName().equals(seeUserName) || md.getName().equals(seeUserName);
                }
                if (canSee) {
                    content = matcher.replaceAll(subStr + "<BR>" + "$2");
                }
                else {
                    content = matcher.replaceAll(subStr);
                    showAttachment = false;
                }
            }
        }
        if (!md.getName().equals("")) {
            UserDb seeUser = um.getUser(seeUserName);

            // 处理经验值高于某值时可见
            // patternStr = "(\\[HIDE_EXP=([0-9]*)\\])(.[^\\[]*)(\\[\\/HIDE_EXP\\])";
            patternStr =
                    "(\\[HIDE=([a-z|A-Z]*),\\s*([0-9]*)\\])(.*?)(\\[\\/HIDE\\])";
            pattern = Pattern.compile(patternStr,
                                      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(content);
            // 如果存在有限制经验值可见的UBB代码
            if (matcher.find()) {
                String moneyCode = matcher.group(2);
                // logger.info("moneyCode=" + moneyCode);
                if (moneyCode.equals("experience"))
                    subStrExp = LoadString(request, "info_need_exprience"); // "===== 以下为隐藏信息 经验值高于$e时才能显示 =====";
                else
                    subStrExp = LoadString(request, "info_need_credit");

                String exp = matcher.group(3);
                if (!StrUtil.isNumeric(exp))
                    exp = "0";
                subStrExp = subStrExp.replaceFirst("\\$e", exp);
                if (canEdit)
                    content = canSee(content, true, subStrExp);
                else {
                    int e = 0;
                    if (moneyCode.equals("experience"))
                        e = seeUser.getExperience();
                    else
                        e = seeUser.getCredit();
                    int iexp = Integer.parseInt(exp);
                    boolean canSee = e >= iexp ? true : false;
                    if (canSee) {
                        content = matcher.replaceFirst(subStrExp + "<BR>" + "$4");
                    } else {
                        content = matcher.replaceFirst(subStrExp);
                        showAttachment = false;
                    }
                }
            }

            String subStrPoint = LoadString(request, "info_need_fee"); // "===== 以下为隐藏信息 需付费 $f $e $w 才能查看 =====";
            patternStr =
                    "(\\[point=([a-z|A-Z]*),\\s*([0-9]*)\\])(.*?)(\\[\\/point\\])";
            pattern = Pattern.compile(patternStr,
                                      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(content);
            if (matcher.find()) {
                String moneyCode = matcher.group(2);
                String strsum = matcher.group(3);
                int sum = 0;
                String usePoint = ParamUtil.get(request, "usePoint");
                try {
                    sum = StrUtil.toInt(strsum);
                } catch (Exception e) {
                    logger.info("doRenderContent:" + e.getMessage());
                }
                ScoreMgr sm = new ScoreMgr();
                ScoreUnit su = sm.getScoreUnit(moneyCode);

                subStrPoint = subStrPoint.replaceFirst("\\$f", "" + su.getName());
                subStrPoint = subStrPoint.replaceFirst("\\$e", "" + sum);
                subStrPoint = subStrPoint.replaceFirst("\\$w", su.getDanWei());

                boolean canSee = canEdit;
                String reason = "";
                String info = "";
                // 付费查看
                if (usePoint.equals("true")) {
                    // 检查用户是否已登录
                    if (!privilege.isUserLogin(request)) {
                        canSee = false;
                        reason = SkinUtil.LoadString(request, "err_not_login");
                    } else {
                        IPluginScore isc = su.getScore();
                        if (isc != null) {
                            try {
                                isc.pay(seeUserName, isc.SELLER_SYSTEM, sum);
                                String tmp = LoadString(request, "info_fee_detail");
                                tmp = tmp.replaceFirst("\\$f",
                                        su.getName() + sum + su.getDanWei());
                                tmp = tmp.replaceFirst("\\$s",
                                        "" + isc.getUserSum(seeUserName));
                                info += tmp; // info += "<BR><BR><font color=red>您已使用了 " + su.getName() + sum + su.getDanWei() + " 您的余额为：" + isc.getUserSum(seeUserName) + "</font><BR>";
                                canSee = true;
                            } catch (ResKeyException e) {
                                reason = e.getMessage(request);
                                canSee = false;
                            }
                        } else {
                            canSee = false;
                        }
                    }
                }

                if (!canSee) {
                    String action = "[<a href='#' onClick=\"if (window.confirm('" +
                                    LoadString(request, "info_confirm_fee") +
                                    "')) window.location.href='showtopic_tree.jsp?" +
                                    "rootid=" + md.getRootid() + "&showid=" +
                                    md.getId() + "&usePoint=true'\">" +
                                    LoadString(request, "info_click_view") +
                                    "</a>]";
                    action += "<BR><BR><font color=red>" + reason + "</font>";
                    content = matcher.replaceFirst(subStrPoint + "<BR>" + action);
                    showAttachment = false;
                } else {
                    content = matcher.replaceFirst(subStrPoint + "<BR>" + "$4" +
                                                   info);
                }
            }

            String subStrPayme = LoadString(request, "info_fee_to_user"); // "===== 以下为隐藏信息 需付费给 $u $f $e $w 才能查看 =====";

            patternStr =
                    "(\\[payme=([a-z|A-Z]*),\\s*([0-9]*)\\])(.*?)(\\[\\/payme\\])";
            pattern = Pattern.compile(patternStr,
                                      Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(content);
            if (matcher.find()) {
                String moneyCode = matcher.group(2);
                String strsum = matcher.group(3);
                int sum = 0;
                String payme = ParamUtil.get(request, "payme");

                try {
                    sum = StrUtil.toInt(strsum);
                } catch (Exception e) {
                    logger.info("doRenderContent:" + e.getMessage());
                }

                int orgSum = sum;

                // 检查是否超过了单贴最高得分
                ScoreExchangeConfig secfg = new ScoreExchangeConfig();
                int maxEarningThread = secfg.getIntProperty("maxearningthread");
                boolean isExceed = false;
                if (maxEarningThread != 0) {
                    if (sum > maxEarningThread) {
                        sum = maxEarningThread;
                        isExceed = true;
                    }
                }

                ScoreMgr sm = new ScoreMgr();
                ScoreUnit su = sm.getScoreUnit(moneyCode);

                subStrPayme = subStrPayme.replaceFirst("\\$u",
                        um.getUser(md.getName()).getNick());
                subStrPayme = subStrPayme.replaceFirst("\\$f", su.getName());
                subStrPayme = subStrPayme.replaceFirst("\\$e", "" + orgSum);
                subStrPayme = subStrPayme.replaceFirst("\\$w", su.getDanWei());

                if (isExceed) {
                    subStrPayme +=
                            StrUtil.format(LoadString(request,
                            "info_fee_exceed_max_earning_thread"),
                                           new Object[] {"" + maxEarningThread});
                }

                boolean canSee = canEdit;
                String reason = "";
                String info = "";
                // 付费查看
                if (payme.equals("true")) {
                    // 检查用户是否已登录
                    if (!privilege.isUserLogin(request)) {
                        canSee = false;
                        reason = SkinUtil.LoadString(request,
                                                     SkinUtil.ERR_NOT_LOGIN); // "请先登录";
                    } else {
                        IPluginScore isc = su.getScore();
                        if (isc != null) {
                            try {
                                isc.pay(seeUserName, md.getName(), sum);

                                String tmp = LoadString(request,
                                        "info_fee_user_detail");
                                tmp = tmp.replaceFirst("\\$u",
                                        um.getUser(md.getName()).getNick());
                                tmp = tmp.replaceFirst("\\$c", su.getName());
                                tmp = tmp.replaceFirst("\\$s", "" + sum);
                                tmp = tmp.replaceFirst("\\$w", su.getDanWei());
                                tmp = tmp.replaceFirst("\\$y",
                                        "" + isc.getUserSum(seeUserName));
                                info += tmp;
                                // info += "<BR><BR><font color=red>您已支付给 " + md.getName() + " " +  su.getName() +
                                //        sum + su.getDanWei() + " 您的余额为：" +
                                //        isc.getUserSum(seeUserName) + "</font><BR>";
                                canSee = true;
                            } catch (ResKeyException e) {
                                reason = e.getMessage(request);
                                canSee = false;
                            }
                        } else {
                            canSee = false;
                        }
                    }
                }

                if (!canSee) {
                    String tmp = LoadString(request, "info_confirm_user_fee");
                    tmp = tmp.replaceFirst("\\$u", um.getUser(md.getName()).getNick());
                    String action = "[<a href='#' onClick=\"if (window.confirm('" +
                                    tmp +
                                    "')) window.location.href='showtopic_tree.jsp?" +
                                    "rootid=" + md.getRootid() + "&showid=" +
                                    md.getId() +
                                    "&payme=true'\">" +
                                    LoadString(request, "info_click_view") +
                                    "</a>]";
                    action += "<BR><BR><font color=red>" + reason + "</font>";
                    content = matcher.replaceFirst(subStrPayme + "<BR>" + action);
                    showAttachment = false;
                } else {
                    content = matcher.replaceFirst(subStrPayme + "<BR>" + "$4" +
                                                   info);
                }
            }
        }
        return content;
    }

    public String RenderAttachment(HttpServletRequest request, MsgDb md) {
    	
        if (!showAttachment) {
            return "";
        }
        // if (md.getIsWebedit() == md.WEBEDIT_REDMOON) {
            if (md != null) {
                java.util.Vector attachments = md.
                                               getAttachments();
                java.util.Iterator ir = attachments.
                                        iterator();
                String str = "";
                while (ir.hasNext()) {
                    Attachment am = (Attachment)
                                    ir.next();

                    if (md.getIsWebedit()!=MsgDb.WEBEDIT_REDMOON) {
                        // 普通发贴方式
                        String extName = StrUtil.getFileExt(am.getDiskName());
                        if (extName.equalsIgnoreCase("gif") ||
                            extName.equalsIgnoreCase("jpg") ||
                            extName.equalsIgnoreCase("png") || extName.equalsIgnoreCase("bmp"))
                            ; // continue;
                    }
                    str +=
                            "<div><img src='" + Global.getRootPath() + "/netdisk/images/" + am.getIcon() + "'>";
                    // 检查用户所在的组下载是否需付费
                    String groupCode = "";
                    if (Privilege.isUserLogin(request)) {
                        UserDb ud = new UserDb();
                        ud = ud.getUser(Privilege.getUser(request));
                        groupCode = ud.getGroupCode();
                    }
                    // 取得用户所在组
                    if (groupCode.equals(""))
                        groupCode = UserGroupDb.EVERYONE;
                    // 取得用户组在此版块的权限
                    UserGroupPrivDb ugpd = new UserGroupPrivDb();
                    ugpd = ugpd.getUserGroupPrivDb(groupCode, md.getboardcode());
                    String moneyCode = StrUtil.getNullStr(ugpd.getString("money_code"));
                    if (!moneyCode.equals("")) {
                        ScoreMgr sm = new ScoreMgr();
                        ScoreUnit su = sm.getScoreUnit(moneyCode);
                        String sFee = StrUtil.format(LoadString(request, "info_confirm_download_attach_fee"), new Object[] {su.getName(), "" + ugpd.getInt("money_sum")});
                        str +=
                            "    &nbsp; <a href=\"#\" onclick=\"if (window.confirm('" + sFee +"')) window.open('" + request.getContextPath() + "/forum/getfile.jsp?msgId=" + am.getMsgId() + "&attachId=" + am.getId() +
                            "')\">";
                    }
                    else
                        str +=
                            "    &nbsp; <a target=_blank href='" + request.getContextPath() + "/forum/getfile.jsp?msgId=" + am.getMsgId() + "&attachId=" + am.getId() +
                            "'>";
                    str  += am.getName() +
                          "</a>";
                    str += "&nbsp; " + am.getDesc();
                    str += "&nbsp; (" + DateUtil.format(am.getUploadDate(), "yyyy-MM-dd HH:mm") + ", &nbsp;" + NumberUtil.round((double)am.getSize()/1024000, 3) + "&nbsp;M)";
                    String str1 = SkinUtil.LoadString(request, "info_attach_download_count").replaceFirst("\\$count", ""+am.getDownloadCount());
                    str += str1;
                    str += "</div>";
                }
                return str;
            }
        // }

        return "";
    }

    public void setShowAttachment(boolean showAttachment) {
        this.showAttachment = showAttachment;
    }

    public boolean isShowAttachment() {
        return showAttachment;
    }


    /**
     *
     * @param content String
     * @param canSee boolean
     * @param subStr String // 当不能看见时的替代字符串
     * @return String
     */
    public static String replyCanSee(String content, boolean canSee, String subStr) {
        String patternStr = "";
        Pattern pattern;
        Matcher matcher;
        patternStr = "(\\[REPLY\\])(.[^\\[]*)(\\[\\/REPLY\\])";
        pattern = Pattern.compile(patternStr, Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        if (canSee)
            content = matcher.replaceAll(subStr + "<BR>" + "$2");
        else
            content = matcher.replaceAll(subStr);
        return content;
    }

    public static String ownerCanSee(String content, boolean canSee, String subStr) {
        String patternStr = "";
        Pattern pattern;
        Matcher matcher;
        patternStr = "(\\[OWNER\\])(.[^\\[]*)(\\[\\/OWNER\\])";
        pattern = Pattern.compile(patternStr, Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        if (canSee)
            content = matcher.replaceAll(subStr + "<BR>" + "$2");
        else
            content = matcher.replaceAll(subStr);
        return content;
    }

    public static String canSee(String content, boolean canSee, String subStr) {
        String patternStr = "";
        Pattern pattern;
        Matcher matcher;
        // patternStr = "(\\[HIDE_EXP=([0-9]*)\\])(.[^\\[]*)(\\[\\/HIDE_EXP\\])";
        patternStr =
                "(\\[HIDE=([a-z|A-Z]*),\\s*([0-9]*)\\])(.[^\\[]*)(\\[\\/HIDE\\])";
        pattern = Pattern.compile(patternStr, Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        if (canSee)
            content = matcher.replaceFirst(subStr + "<BR>" + "$4");
        else
            content = matcher.replaceFirst(subStr);
        return content;
    }

    private boolean showAttachment = true;

}
