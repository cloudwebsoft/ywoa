package com.redmoon.forum;

import java.sql.*;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.security.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.blog.*;
import com.redmoon.forum.err.*;
import com.redmoon.forum.life.prision.*;
import com.redmoon.forum.person.*;
import com.redmoon.forum.plugin.*;
import com.redmoon.forum.plugin.base.*;
import com.redmoon.forum.security.*;
import com.redmoon.kit.util.*;
import org.apache.log4j.*;
import cn.js.fan.db.Conn;

/**
 *
 * <p>Title: 论坛权限管理</p>
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
public class Privilege {
    Logger logger = Logger.getLogger(Privilege.class.getName());
    boolean debug = false;

    public static final String MASTER = "sq_master";

    public static final String USER_SYSTEM = "system";

    public static final String COOKIE_CWBBS_AUTH = "cwbbs.auth";
    public static final String SESSION_CWBBS_AUTH = "cwbbs.auth";

    public static final int LOGIN_SAVE_NONE = 0;
    public static final int LOGIN_SAVE_DAY = 1;
    public static final int LOGIN_SAVE_MONTH = 2;
    public static final int LOGIN_SAVE_YEAR = 3;

    private final static int ENCODE_XORMASK = 0x5A;
    private final static char ENCODE_DELIMETER = '\002';
    private final static char ENCODE_CHAR_OFFSET1 = 'A';
    private final static char ENCODE_CHAR_OFFSET2 = 'h';

    public Privilege() {
    }

    public boolean isRequestValid(HttpServletRequest request) throws
            SQLException {
        if (request.getRequestURL().indexOf(request.getServerName()) == -1)
            return false;
        else
            return true;
    }


    /**
     * 填充跳转时需要的登录信息
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean JumpToCommunity(HttpServletRequest req,
                                   HttpServletResponse res, String name) throws
            ErrMsgException {
        boolean isvalid = false;
        // 判断是否已登录,即重复登录
        if (isUserLogin(req)) {
            return true;
        }

        if (name.equals("")) {
            throw new ErrMsgException("用户名不能为空！");
        }

        com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
        ud = ud.getUserDb(name);
        if (!ud.isLoaded()) {
            throw new ErrMsgException("OA用户不存在！");
        }
        if (!ud.isValid()) {
            throw new ErrMsgException("帐号已被禁用！");
        }

        UserDb user = new UserDb();
        user = user.getUser(name);
        if (!user.isLoaded()) {
            throw new ErrMsgException("论坛中用户 " + name + " 不存在！");
        }

        if (!user.isValid()) {
            throw new ErrMsgException("对不起，您已被屏蔽！");
        }

        // 检查是否被关进了监狱
        if (Prision.isUserArrested(name)) {
            Calendar cal = Prision.getReleaseDate(name);
            throw new ErrMsgException("您已被关押在社区监狱中，释放日期为" +
                                      DateUtil.format(cal, "yy-MM-dd") +
                                      "，不能登录！");
        }

        return doLogin(req, res, user);
    }

    /**
     * 是否拥有管理员权限
     * @param request HttpServletRequest
     * @return boolean
     */
    public static boolean isMasterLogin(HttpServletRequest request) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        return pvg.isUserPrivValid(request, "admin");
    }

    public boolean isMasterPrivValid(HttpServletRequest request, String priv) {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (pvg.isUserPrivValid(request, "admin"))
            return true;
        if (pvg.isUserPrivValid(request, priv))
            return true;
        return false;
    }

    public String getMaster(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return (String) session.getAttribute(MASTER);
    }

    public boolean canWebEditRedMoon(HttpServletRequest request,
                                     String boardCode) {
        if (boardCode.equals(Leaf.CODE_BLOG))
            return true;
        // 检查版块是否允许高级发贴方式
        Leaf lf = new Leaf();
        lf = lf.getLeaf(boardCode);
        if (lf == null || !lf.isLoaded())
            return false;
        if (lf.getWebeditAllowType() ==
            lf.WEBEDIT_ALLOW_TYPE_UBB_NORMAL_REDMOON ||
            lf.getWebeditAllowType() == lf.WEBEDIT_ALLOW_TYPE_REDMOON_FIRST) {
            return true;
        } else {
            // 如果版块不允许WebEdit控件发贴方式，则只有版主和总管理员才可用
            if (isManager(request, boardCode) || isMasterLogin(request))
                return true;
        }
        return false;
    }

    public String LoadString(HttpServletRequest request, String key) {
        return SkinUtil.LoadString(request, "res.forum.Privilege", key);
    }

    /**
     * 检查用户能否进入版块
     * @param request HttpServletRequest
     * @param boardCode String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean checkCanEnterBoard(HttpServletRequest request,
                                      String boardCode) throws ErrMsgException {
        Leaf curleaf = new Leaf();
        curleaf = curleaf.getLeaf(boardCode);
        if (curleaf == null || !curleaf.isLoaded()) {
            throw new ErrMsgException(LoadString(request, "err_board_lost")); // "版块 " + boardCode + " 不存在！");
        }

        // 版块被锁定
        if (curleaf.isLocked()) {
            if (isMasterLogin(request) || isManager(request, boardCode)) {
                return true;
            }
            throw new ErrMsgException(LoadString(request, "err_board_locked"));
        }

        // 如果通行证检查可以进入版块，则允许进入，如果通行证不允许进入，则会抛出异常
        // 当版块挂有通行证时，以通行证为依据，否则检查用户组是否有进入的权限
        boolean isEntrancePluginBoard = false;
        boolean isEntranceAllowed = false;
        EntranceMgr em = new EntranceMgr();
        Vector vEntrancePlugin = em.getAllEntranceUnitOfBoard(boardCode);
        if (vEntrancePlugin.size() > 0) {
            isEntrancePluginBoard = true;
            Iterator irpluginentrance = vEntrancePlugin.iterator();
            while (irpluginentrance.hasNext()) {
                EntranceUnit eu = (EntranceUnit) irpluginentrance.next();
                IPluginEntrance ipe = eu.getEntrance();
                ipe.canEnter(request, boardCode);
            }
            isEntranceAllowed = true;
        }

        if (isEntrancePluginBoard) {
            if (isEntranceAllowed)
                return true;
            else {
                // 如果通行证不允许进入
                if (isMasterLogin(request) || isManager(request, boardCode)) {
                    return true;
                }
            }
        } else {
            // 版块未挂有通行证，检查用户组是否有进入的权限
            // canUserDo中对是否为master作了检查，所以如果用户组没有进入的权限，则检查是否为版主
            if (!canUserDo(request, boardCode, "enter_board")) {
                // 如果是版主，则允许进入
                if (isManager(request, boardCode)) {
                    return true;
                } else
                    throw new ErrMsgException(SkinUtil.LoadString(
                            request,
                            "pvg_invalid"));
            }
        }

        return true;
    }

    /**
     * 能否发主题贴或回贴，时间限制检查，用于canAddNew canAddQuickReply canAddReply
     * @param request HttpServletRequest
     * @param boardCode String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean checkCanPost(HttpServletRequest request, String boardCode) throws
            ErrMsgException {
        // 如果不是管理员或版主，则检查是否为新用户
        if (isMasterLogin(request) || isManager(request, boardCode)) {
            return true;
        } else {
            RegConfig rgc = new RegConfig();
            int timeLimit = rgc.getIntProperty("newUserAddTopicTimeLimit");
            // 对于新用户有发贴时间限制
            if (timeLimit > 0) {
                UserDb ud = new UserDb();
                ud = ud.getUser(getUser(request));
                // logger.info(DateUtil.datediffMinute(new java.util.Date(), ud.getRegDate()) + "--" + timeLimit);
                if (DateUtil.datediffMinute(new java.util.Date(), ud.getRegDate()) <
                    timeLimit) {
                    throw new ErrMsgException(StrUtil.format(LoadString(request,
                            "err_new_user_add_topic_timelimit"),
                            new Object[] {"" + timeLimit}));
                }
            }

            // 检查是否处于可发贴时间段
            TimeConfig tc = new TimeConfig();
            if (tc.isPostForbidden(request)) {
                throw new ErrMsgException(StrUtil.format(LoadString(request,
                        "time_forbid_post"),
                        new Object[] {tc.getProperty("forbidPostTime1"),
                        tc.getProperty("forbidPostTime2")}));
            }
        }
        return true;
    }

    /**
     * 用户能否投票
     * @param request HttpServletRequest
     * @param boardCode String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean canVote(HttpServletRequest request, String boardCode) throws
            ErrMsgException {
        if (!canUserDo(request, boardCode, "vote"))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    SkinUtil.PVG_INVALID));
        return true;
    }

    public boolean canAddNew(HttpServletRequest request, String boardCode,
                             FileUpload fu) throws ErrMsgException {
        checkCanEnterBoard(request, boardCode);
        checkCanPost(request, boardCode);

        Config cfg = Config.getInstance();
        if (cfg.getBooleanProperty("forum.addUseValidateCode")) {
            if (isValidateCodeRight(request, fu))
                return true;
            else
                throw new ErrMsgException(LoadString(request,
                        "err_validate_code"));
        }

        // 如果通行证检查可以回贴，则允许，如果通行证不允许，则会抛出异常
        // 当版块挂有通行证时，以通行证为依据，否则检查用户组是否有回贴的权限
        EntranceMgr em = new EntranceMgr();
        boolean isPluginEntranceValid = false;
        Vector vEntrancePlugin = em.getAllEntranceUnitOfBoard(boardCode);
        if (vEntrancePlugin.size() > 0) {
            Iterator irpluginentrance = vEntrancePlugin.iterator();
            while (irpluginentrance.hasNext()) {
                EntranceUnit eu = (EntranceUnit) irpluginentrance.next();
                IPluginEntrance ipe = eu.getEntrance();
                ipe.canAddNew(request, boardCode);
            }
            isPluginEntranceValid = true;
        }
        if (isPluginEntranceValid)
            return true;

        if (!canUserDo(request, boardCode, "add_topic"))
            throw new ErrMsgException(StrUtil.UrlEncode(SkinUtil.LoadString(
                    request, "pvg_invalid")));

        // 防止因addtopic_new.jsp发贴时，原来在同城交易中发贴，但却选了其它版块
        // 而后者无插件功能，所以在下面的检查中，取得版块的所有插件，能防止发贴信息的混乱

        // 当为贴子型插件时，会传递pluginCode
        String pluginCode = StrUtil.getNullString(fu.getFieldValue("pluginCode"));
        boolean isPluginValid = false;
        // 插件的权限检查
        PluginMgr pm = new PluginMgr();
        Vector vplugin = pm.getAllPluginUnitOfBoard(boardCode);
        if (vplugin.size() > 0) {
            Iterator irplugin = vplugin.iterator();
            while (irplugin.hasNext()) {
                PluginUnit pu = (PluginUnit) irplugin.next();
                IPluginPrivilege ipp = pu.getPrivilege();
                // logger.info("plugin name:" + pu.getName(request));
                if (!ipp.canAddNew(request, boardCode, fu)) {
                    String str = SkinUtil.LoadString(request,
                            "res.forum.MsgMgr", "err_pvg_plugin");
                    str = str.replaceFirst("\\$p", pu.getName(request));
                    throw new ErrMsgException(str);
                }
                // 检查指定的pluginCode是否被允许
                if (!pluginCode.equals("")) {
                    if (pu.getCode().equals(pluginCode)) {
                        isPluginValid = true;
                    }
                }
            }
        }

        // 如果指定的pluginCode不被允许，则报错
        if (!pluginCode.equals("") && !isPluginValid) {
            throw new ErrMsgException(LoadString(request, "err_plugin_invalid"));
        }

        return true;
    }

    public boolean canAddReply(HttpServletRequest request, String boardCode,
                               FileUpload fu) throws
            ErrMsgException {
        checkCanEnterBoard(request, boardCode);
        checkCanPost(request, boardCode);

        Config cfg = Config.getInstance();
        if (cfg.getBooleanProperty("forum.addUseValidateCode")) {
            if (!isValidateCodeRight(request, fu))
                throw new ErrMsgException(LoadString(request,
                        "err_validate_code"));
        }

        String strreplyid = StrUtil.getNullStr(fu.getFieldValue("replyid"));
        long replyid = StrUtil.toInt(strreplyid, -1);
        if (replyid == -1)
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "res.forum.MsgMgr", "err_need_reply_id")); // "缺少回贴标识！");
        MsgDb msgReplied = new MsgDb();
        msgReplied = msgReplied.getMsgDb(replyid);

        int topicReplyMaxDay = cfg.getIntProperty("forum.topicReplyMaxDay");
        if (topicReplyMaxDay>0) {
        	// 检查是否已超出可回复的天数范围
        	if (DateUtil.datediff(new java.util.Date(), msgReplied.getAddDate())>topicReplyMaxDay) {
                throw new ErrMsgException(StrUtil.format(LoadString(request, "err_reply_exceed_max_day"), new Object[]{""+topicReplyMaxDay})); // "缺少回贴标识！");
        	}
        }

        // 如果通行证检查可以回贴，则允许，如果通行证不允许，则会抛出异常
        // 当版块挂有通行证时，以通行证为依据，否则检查用户组是否有回贴的权限
        EntranceMgr em = new EntranceMgr();
        boolean isPluginEntranceValid = false;
        Vector vEntrancePlugin = em.getAllEntranceUnitOfBoard(boardCode);
        if (vEntrancePlugin.size() > 0) {
            Iterator irpluginentrance = vEntrancePlugin.iterator();
            while (irpluginentrance.hasNext()) {
                EntranceUnit eu = (EntranceUnit) irpluginentrance.next();
                IPluginEntrance ipe = eu.getEntrance();
                ipe.canAddReply(request, boardCode, msgReplied.getRootid());
            }
            isPluginEntranceValid = true;
        }
        if (isPluginEntranceValid)
            return true;

        if (!canUserDo(request, boardCode, "reply_topic"))
            throw new ErrMsgException(StrUtil.UrlEncode(SkinUtil.LoadString(
                    request, "pvg_invalid")));

        // 插件的权限检查
        PluginMgr pm = new PluginMgr();
        Vector vplugin = pm.getAllPluginUnitOfBoard(boardCode);
        if (vplugin.size() > 0) {
            Iterator irplugin = vplugin.iterator();
            while (irplugin.hasNext()) {
                PluginUnit pu = (PluginUnit) irplugin.next();
                IPluginPrivilege ipp = pu.getPrivilege();
                // logger.info("plugin name:" + pu.getName(request) + " rootid=" + rootid + " replyid=" + replymsg.getId());
                if (!ipp.canAddReply(request, boardCode, msgReplied.getRootid())) {
                    String s = LoadString(request, "err_pvg_plugin");
                    s = s.replaceFirst("\\$p", pu.getName(request));
                    throw new ErrMsgException(s);
                }
            }
        }
        return true;
    }

    public boolean canAddQuickReply(HttpServletRequest request,
                                    String boardcode, MsgDb remsg) throws
            ErrMsgException {
        checkCanEnterBoard(request, boardcode);
        checkCanPost(request, boardcode);

        Config cfg = Config.getInstance();
        if (cfg.getBooleanProperty("forum.addUseValidateCode")) {
            if (isValidateCodeRight(request))
                ;
            else
                throw new ErrMsgException(LoadString(request, "err_validate_code"));
        }

        int topicReplyMaxDay = cfg.getIntProperty("forum.topicReplyMaxDay");
        if (topicReplyMaxDay>0) {
        	// 检查是否已超出可回复的天数范围
        	if (DateUtil.datediff(new java.util.Date(), remsg.getAddDate())>topicReplyMaxDay) {
                throw new ErrMsgException(StrUtil.format(LoadString(request, "err_reply_exceed_max_day"), new Object[]{""+topicReplyMaxDay})); // "缺少回贴标识！");
        	}
        }

        // 如果通行证检查可以回贴，则允许，如果通行证不允许，则会抛出异常
        // 当版块挂有通行证时，以通行证为依据，否则检查用户组是否有回贴的权限
        EntranceMgr em = new EntranceMgr();
        boolean isPluginEntranceValid = false;
        Vector vEntrancePlugin = em.getAllEntranceUnitOfBoard(boardcode);
        if (vEntrancePlugin.size() > 0) {
            Iterator irpluginentrance = vEntrancePlugin.iterator();
            while (irpluginentrance.hasNext()) {
                EntranceUnit eu = (EntranceUnit) irpluginentrance.next();
                IPluginEntrance ipe = eu.getEntrance();
                ipe.canAddReply(request, boardcode, remsg.getRootid());
            }
            isPluginEntranceValid = true;
        }
        if (isPluginEntranceValid)
            return true;

        if (!canUserDo(request, boardcode, "reply_topic"))
            throw new ErrMsgException(StrUtil.UrlEncode(SkinUtil.LoadString(
                    request, "pvg_invalid")));

        // 插件的权限检查
        PluginMgr pm = new PluginMgr();
        Vector vplugin = pm.getAllPluginUnitOfBoard(boardcode);
        if (vplugin.size() > 0) {
            Iterator irplugin = vplugin.iterator();
            while (irplugin.hasNext()) {
                PluginUnit pu = (PluginUnit) irplugin.next();
                IPluginPrivilege ipp = pu.getPrivilege();
                if (ipp != null && !ipp.canAddQuickReply(request, remsg)) {
                    String s = LoadString(request, "err_plugin");
                    s = s.replaceFirst("\\$p", pu.getName(request));
                    throw new ErrMsgException(s);
                }
            }
        }
        return true;
    }

    /**
     * 是否可以删除贴子
     * @param request HttpServletRequest
     * @param id long
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean canDel(HttpServletRequest request, long id) throws
            ErrMsgException {
        // 如果是管理员身份
        if (isMasterPrivValid(request, "admin"))
            return true;
        if (!isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "err_not_login"));

        MsgDb md = new MsgDb();
        md = md.getMsgDb(id);
        if (!md.isLoaded())
            return false;

        if (md.isBlog()) {
            // 如果是团队博客,检查是否具有团队博客中的权限
            UserConfigDb ucd = new UserConfigDb();
            ucd = ucd.getUserConfigDb(md.getBlogId());
            if (ucd.getType() == UserConfigDb.TYPE_GROUP) {
                if (BlogGroupUserDb.canUserDo(request, md.getBlogId(),
                                              BlogGroupUserDb.PRIV_TOPIC)) {
                    return true;
                }
            } else {
                // 验证是否为该贴的博主
                if (ucd.getUserName().equals(getUser(request)))
                    return true;
            }
        }

        // 发贴者本人
        if (Config.getInstance().getBooleanProperty("forum.canUserDelTopicSelf")) {
            if (md.getName().equals(getUser(request)))
                return true;
        }
        return isUserHasManagerIdentity(request, md.getboardcode());
    }

    /**
     * 是否可以置顶，转移版块，更改贴子颜色等操作
     * @param request HttpServletRequest
     * @param id long
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean canManage(HttpServletRequest request, long id) throws
            ErrMsgException {
        // 如果是管理员身份
        if (isMasterPrivValid(request, "admin"))
            return true;
        if (!isUserLogin(request))
            return false;

        MsgDb md = new MsgDb();
        md = md.getMsgDb(id);
        if (!md.isLoaded())
            return false;

        return isUserHasManagerIdentity(request, md.getboardcode());
    }

    /**
     * 判别对于版块是否有管理权，用于在显示版主管理按钮，“回复可见”等选项
     * @param request HttpServletRequest
     * @param boardCode String
     * @return boolean
     */
    public boolean isManager(HttpServletRequest request, String boardCode) {
        if (isMasterPrivValid(request, "admin"))
            return true;
        if (!isUserLogin(request))
            return false;

        return isUserHasManagerIdentity(request, boardCode);
    }

    /**
     * 验证用户是否有boardCode版块的版主身份
     * @param request HttpServletRequest
     * @param boardCode String
     * @return boolean
     */
    public static boolean isUserHasManagerIdentity(HttpServletRequest request,
                                            String boardCode) {
    	if (boardCode.equals(""))
    		return false;

        Directory dir = new Directory();
        Leaf lf = dir.getLeaf(boardCode);
        if (lf==null)
        	return false;

        // 验证是否为版主
        BoardManagerDb bm = new BoardManagerDb();
        bm = bm.getBoardManagerDb(boardCode, getUser(request));
        if (bm.isLoaded()) {
            return true;
        }

        // 验证是否为总版
        bm = bm.getBoardManagerDb(Leaf.CODE_ROOT, getUser(request));
        if (bm.isLoaded()) {
            return true;
        }

        // 验证是否为祖先节点的版主
        String parentCode = lf.getParentCode();
        while (!parentCode.equals(Leaf.CODE_ROOT)) {
            lf = dir.getLeaf(parentCode);
            if (lf == null)
                break;
            bm = bm.getBoardManagerDb(lf.getCode(), getUser(request));
            if (bm.isLoaded())
                return true;
            parentCode = lf.getParentCode();
        }
        return false;
    }

    /**
     * 检查用户能否审核版块中的贴子
     * @param request
     * @param boardCode
     * @return
     */
    public static boolean isUserCanCheck(HttpServletRequest request, String boardCode) {
		if (boardCode.equals(""))
			return false;

		Directory dir = new Directory();
		Leaf lf = dir.getLeaf(boardCode);
		if (lf == null)
			return false;

		if (isMasterLogin(request))
			return true;

		// 验证是否为版主
		BoardManagerDb bm = new BoardManagerDb();
		bm = bm.getBoardManagerDb(boardCode, getUser(request));
		if (bm.isLoaded()) {
			return bm.isCanCheck();
		}

		// 验证是否为总版
		bm = bm.getBoardManagerDb(Leaf.CODE_ROOT, getUser(request));
		if (bm.isLoaded()) {
			if (bm.isCanCheck())
				return true;
		}

		// 验证是否为祖先节点的版主
		String parentCode = lf.getParentCode();
		while (!parentCode.equals(Leaf.CODE_ROOT)) {
			lf = dir.getLeaf(parentCode);
			if (lf == null)
				break;
			bm = bm.getBoardManagerDb(lf.getCode(), getUser(request));
			if (bm.isLoaded()) {
				if (bm.isCanCheck())
					return true;
			}
			parentCode = lf.getParentCode();
		}
		return false;
	}

    /**
     * 获取管理版块时的管理身份
     * @param request HttpServletRequest
     * @param boardCode String
     * @return BoardManagerDb 结果可能是本版版主，也可能是上级版主
     */
    public BoardManagerDb getUserManagerIdentityOfBoard(HttpServletRequest
            request,
            String boardCode) {
        // 验证是否为版主
        BoardManagerDb bm = new BoardManagerDb();
        bm = bm.getBoardManagerDb(boardCode, getUser(request));
        if (bm.isLoaded()) {
            return bm;
        }

        // 验证是否为总版
        bm = bm.getBoardManagerDb(Leaf.CODE_ROOT, getUser(request));
        if (bm.isLoaded()) {
            return bm;
        }

        // 验证是否为祖先节点的版主
        Directory dir = new Directory();
        Leaf lf = dir.getLeaf(boardCode);
        String parentCode = lf.getParentCode();
        while (!parentCode.equals(Leaf.CODE_ROOT)) {
            lf = dir.getLeaf(parentCode);
            if (lf == null)
                break;
            bm = bm.getBoardManagerDb(lf.getCode(), getUser(request));
            if (bm.isLoaded())
                return bm;
            parentCode = lf.getParentCode();
        }
        return null;
    }

    public boolean canEdit(HttpServletRequest request, MsgDb md) throws
            ErrMsgException {
        // 先验证是否为会员
        if (!isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request,
                    "err_not_login"));

        String boardcode = md.getboardcode();

        checkCanEnterBoard(request, boardcode);

        String name = getUser(request);
        String username = md.getName();

        boolean valid = false;

        // 验证是否有版主身份
        if (isUserHasManagerIdentity(request, boardcode) ||
            isMasterLogin(request)) {
            valid = true;
        } else {
            if (md.isBlog()) {
                if (username.equals(name)) {
                    valid = true;
                } else {
                    // 如果是团队博客,检查是否具有团队博客中的权限
                    UserConfigDb ucd = new UserConfigDb();
                    ucd = ucd.getUserConfigDb(md.getBlogId());
                    if (ucd.getType() == UserConfigDb.TYPE_GROUP) {
                        if (BlogGroupUserDb.canUserDo(request, md.getBlogId(),
                                BlogGroupUserDb.PRIV_TOPIC)) {
                            valid = true;
                        }
                    }
                }
            } else {
                // 发贴者本人
                Config cfg = Config.getInstance();
                if (cfg.getBooleanProperty("forum.canUserEditTopicSelf")) {
                    if (username.equals(name)) {
                        // 如果未超过限定的编辑时间
                        int minute = cfg.getIntProperty("forum.topicEditExpireMinute");
                        if (minute >= 0) {
                            if (DateUtil.datediffMinute(new java.util.Date(),
                                    md.getAddDate()) < minute) {
                                valid = true;
                            } else {
                                throw new ErrMsgException(StrUtil.format(
                                        LoadString(request,
                                        "info_topic_edit_expire"),
                                        new Object[] {"" + minute}));
                            }
                        } else
                            valid = true;
                    }

                }
            }
        }

        // 插件的权限检查
        PluginMgr pm = new PluginMgr();
        Vector vplugin = pm.getAllPluginUnitOfBoard(boardcode);
        if (vplugin.size() > 0) {
            Iterator irplugin = vplugin.iterator();
            while (irplugin.hasNext()) {
                PluginUnit pu = (PluginUnit) irplugin.next();
                IPluginPrivilege ipp = pu.getPrivilege();
                if (ipp != null && !ipp.canEdit(request, md)) {
                    String s = LoadString(request, "err_plugin");
                    s = s.replaceFirst("\\$p", pu.getName(request));
                    throw new ErrMsgException(s);
                }
            }
        }

        return valid;
    }

    public static boolean isUserLogin(HttpServletRequest request) {
        // 如果从session中直接取Authorization（JIVE），速度快，但是需耗session资源
        // 而从cookie中取出值之后，需从缓存中取user的帐号判断COOKIE是否合法以及用户是否被关入监狱
        // 效率上前者快一些，后者所耗费的session资源无，但是cookie中的信息需加密和解密，这样一来每资带来的资源消耗就比较大
        // 因此相比之下，JIVE更合适一些，另外，因为系统中在别处使用到了session（SkinUtil），所以决定还是采用session来进行登录处理
        // 而以cookie作为一种辅助手段
        HttpSession session = request.getSession(true);
        Authorization auth = (Authorization) session.getAttribute(SESSION_CWBBS_AUTH);

        boolean isValid = false;
        if (auth != null) {
            isValid = !auth.isGuest();
            if (isValid) {
                return true;
            }
        }

        // 用保存的cookie登录
        String c = CookieBean.getCookieValue(request, COOKIE_CWBBS_AUTH);

        if (c.equals("")) {
            return false;
        }

        String[] ck = decodeCookie(c);
        String userName = ck[0];
        String pwdMD5 = ck[1];

        com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
        Logger.getLogger(Privilege.class.getName()).info("isUserLogin cookie name=" + userName);
        ud = ud.getUserDb(userName);

        if (ud.isLoaded()) {
            if (ud.getPwdMD5().equals(pwdMD5)) {
                // 检查是否被关进了监狱
                if (Prision.isUserArrested(userName)) {
                    return false;
                }

                auth = new Authorization(userName, false);
                session.setAttribute(SESSION_CWBBS_AUTH, auth);

                /*
                                 // 取得用户的locale
                                 String mylocale = ud.getLocale();
                                 if (!mylocale.equals("")) {
                    String[] ary = StrUtil.split(mylocale, "_");
                    if (ary!=null && ary.length==2) {
                        Locale locale = new Locale(ary[0], ary[1]);
                        session.setAttribute(SkinUtil.SESSION_LOCALE, locale);
                    }
                                 }
                 */

                OnlineUserDb ou = new OnlineUserDb();
                ou = ou.getOnlineUserDb(userName);
                ou.setStayTime(new java.util.Date());
                // 如果用户在线
                if (ou.isLoaded()) {
                    ou.save();
                } else {
                    // 如果不在线，即超时被刷新掉了，则再加入在线列表
                    int isguest = 0;
                    ou.setName(auth.getName());
                    ou.setIp(StrUtil.getIp(request));
                    ou.setGuest(isguest == 1 ? true : false);
                    try {
                        ou.create();
                    } catch (ErrMsgException e) {
                        Logger.getLogger(Privilege.class.getName()).error(
                                "isUserLogin:" + e.getMessage());
                    }
                }

                return true;
            }
        }
        // 如果帐号验证不合法或者被关进了监狱，则清除其COOKIE
        // enrolGuest中作了这样的相应处理

        return false;
    }

    /**
     * 是否为访客，即已登记过并赋予了随机用户名，是则返回true;如果已登录用户，则返回false，未登记用户，也返回false
     * @param request HttpServletRequest
     * @return boolean
     */
    public static boolean isGuest(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Authorization auth = (Authorization) session.getAttribute(
                SESSION_CWBBS_AUTH);
        if (auth == null)
            return false;
        return auth.isGuest();
    }

    public static String getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Authorization auth = (Authorization) session.getAttribute(
                SESSION_CWBBS_AUTH);
        if (auth == null)
            return "";
        else
            return auth.getName();
    }

    /**
     * 高级发贴及断点续传时用以判断上传文件大小是否超出了允许的空间大小，对于效率不太好，因为当用户上传很大文件时，需等待文件上传完毕后才能判断
     * 可以参考博客歌曲、视频发布的判断方法加以改进
     * @param request HttpServletRequest
     * @param fileSize long 上传文件的大小
     * @return boolean
     */
    public static boolean canUploadAttachment(HttpServletRequest request, long fileSize) {
        // 未登录
        if (!isUserLogin(request))
            return false;
        String userName = getUser(request);
        UserDb ud = new UserDb();
        ud = ud.getUser(userName);
        if (getDiskSpaceAllowed(ud) >= ud.getDiskSpaceUsed() + fileSize)
            return true;
        else
            return false;
    }

    /**
     * 取得用户允许的空间大小
     * @param user UserDb
     * @return long
     */
    public static long getDiskSpaceAllowed(UserDb user) {
        String groupCode = user.getUserGroupDb().getCode();
        UserGroupPrivDb ugpd = new UserGroupPrivDb();
        ugpd = ugpd.getUserGroupPrivDb(groupCode, UserGroupPrivDb.ALLBOARD);
        // 以用户组的允许空间和用户指定空间大小两者中大的为准
        long space = ugpd.getLong("disk_space_allowed");
        if (user.getDiskSpaceAllowed()>space)
            space = user.getDiskSpaceAllowed();
        return space;
    }


    public static boolean doLogout(HttpServletRequest req,
                                   HttpServletResponse res,
                                   String userName) {
        HttpSession session = req.getSession(true);
        session.removeAttribute(SESSION_CWBBS_AUTH);

        CookieBean cookiebean = new CookieBean();
        cookiebean.delCookie(res, COOKIE_CWBBS_AUTH, "/");

        // 从在线列表中删除
        OnlineUserDb ou = new OnlineUserDb();
        ou = ou.getOnlineUserDb(userName);
        if (ou.isLoaded()) {
            return ou.del();
        }
        return false;
    }

    /**
     * 退出
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean logout(HttpServletRequest req, HttpServletResponse res) throws
            ErrMsgException {
        return doLogout(req, res, getUser(req));
    }

    public boolean doLogin(HttpServletRequest req, HttpServletResponse res,
                           UserDb user) throws ErrMsgException {
        // 取得登录前的用户名
        String oldname = getUser(req);

        boolean isvalid = false;

        String strcovered = ParamUtil.get(req, "covered");
        int covered = 0;
        if (strcovered.equals("")) {
            strcovered = "0";
        }
        covered = Integer.parseInt(strcovered);

        // 保存用户上次登录时间
        user.setLastTime(user.getCurTime());
        user.setCurTime();
        user.setIp(StrUtil.getIp(req));
        isvalid = user.save();

        if (isvalid) {
            OnlineUserDb oud = new OnlineUserDb();
            // 如果用户原来未登录,是访客（已被系统登记，随机赋予过用户名）
            if (Privilege.isGuest(req)) {
                // 查询该访客是否已在线
                oud = oud.getOnlineUserDb(oldname);
                if (oud.isLoaded()) {
                    // 删除原来作为访客的在线记录
                    oud.del();
                }
            }
            // 检查用户name是否在线
            oud = oud.getOnlineUserDb(user.getName());
            // 如果该用户已处于在线记录中
            if (oud.isLoaded()) {
                oud.setCovered(covered == 1 ? true : false);
                oud.save();
            } else {
                // 如果在线记录中没有该用户，则创建在线记录
                oud.setName(user.getName());
                oud.setIp(req.getRemoteAddr());
                oud.setCovered(covered == 1 ? true : false);
                oud.setGuest(false);
                oud.create();
            }

            ScoreMgr sm = new ScoreMgr();
            Vector vatt = sm.getAllScore();
            Iterator iratt = vatt.iterator();
            while (iratt.hasNext()) {
                ScoreUnit su = (ScoreUnit) iratt.next();
                IPluginScore ips = su.getScore();
                if (ips != null) {
                    ips.login(user);
                }
            }

            // 保存session
            HttpSession session = req.getSession(true);
            Authorization auth = new Authorization(user.getName(), false);
            session.setAttribute(SESSION_CWBBS_AUTH, auth);
            // 取得用户的locale
            String mylocale = user.getLocale();
            if (!mylocale.equals("")) {
                String[] ary = StrUtil.split(mylocale, "_");
                if (ary != null && ary.length == 2) {
                    Locale locale = new Locale(ary[0], ary[1]);
                    session.setAttribute(SkinUtil.SESSION_LOCALE, locale);
                }
            }
            // 保存cookie，根据loginSaveDate置cookie时间
            int loginSaveDate = LOGIN_SAVE_NONE;
            try {
                loginSaveDate = ParamUtil.getInt(req, "loginSaveDate");
            } catch (Exception e) {
            }
            int maxAge = -1;
            if (loginSaveDate == LOGIN_SAVE_NONE) {
                maxAge = -1;
            } else if (loginSaveDate == LOGIN_SAVE_DAY) {
                maxAge = 60 * 60 * 24;
            } else if (loginSaveDate == LOGIN_SAVE_MONTH) {
                maxAge = 60 * 60 * 24 * 30;
            } else if (loginSaveDate == LOGIN_SAVE_YEAR) {
                maxAge = 60 * 60 * 24 * 365;
            }
            // COOKIE都有一个有效期,有效期默认值为-1,这表示没有保存该COOKIE,当该浏览器退出时,该COOKIE立即失效.
            String c = encodeCookie(user.getName(), user.getPwdMd5());
            CookieBean.addCookie(res, COOKIE_CWBBS_AUTH, c, "/", maxAge);
            // 使用cookiebean.setCookieMaxAge不会产生效果，因为setCookieMaxAge从request中取COOKIE，然后设其到期值，但是此时request中尚没有发送过来的cookie
            // cookiebean.setCookieMaxAge(req, res, NAME, maxAge);
        }
        return isvalid;
    }

    /**
     * 验证码是否合法
     * @param request HttpServletRequest
     * @return boolean
     */
    public boolean isValidateCodeRight(HttpServletRequest request) {
        // 检测验证码
        HttpSession session = request.getSession(true);
        String sessionCode = StrUtil.getNullStr((String) session.getAttribute(
                "validateCode"));
        if (sessionCode.equals(""))
            return false;
        String validateCode = ParamUtil.get(request, "validateCode");
        if (!validateCode.equals(sessionCode))
            return false;
        else
            return true;
    }

    public boolean isValidateCodeRight(HttpServletRequest request,
                                       FileUpload fu) {
        HttpSession session = request.getSession(true);
        String sessionCode = StrUtil.getNullStr((String) session.getAttribute(
                "validateCode"));
        if (sessionCode.equals(""))
            return false;
        // 检测验证码
        String validateCode = StrUtil.getNullString(fu.getFieldValue(
                "validateCode"));
        if (!validateCode.equals(sessionCode))
            return false;
        else
            return true;
    }

    /**
     * 登录时会检查验证码，如果配置文件中设置了检查，则检查
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @return boolean
     * @throws WrongPasswordException
     * @throws InvalidNameException
     * @throws ErrMsgException
     */
    public boolean login(HttpServletRequest req, HttpServletResponse res) throws
            WrongPasswordException, InvalidNameException, ErrMsgException {
        return login(req, res, true);
    }

    /**
     * 登录
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @param isCheckValidateCode boolean　是否检查验证码
     * @return boolean
     * @throws WrongPasswordException
     * @throws InvalidNameException
     * @throws ErrMsgException
     */
    public boolean login(HttpServletRequest req, HttpServletResponse res,
                         boolean isCheckValidateCode) throws
            WrongPasswordException, InvalidNameException, ErrMsgException {
        Config cfg = Config.getInstance();
        // 检测验证码
        if (isCheckValidateCode) {
            if (cfg.getBooleanProperty("forum.loginUseValidateCode")) {
                if (!isValidateCodeRight(req)) {
                    throw new ErrMsgException(LoadString(req, "err_validate_code"));
                }
            }
        }
        // 验证IP
        IPMonitor im = new IPMonitor();
        if (!im.isValid(req, StrUtil.getIp(req))) {
            throw new ErrMsgException(im.getMessage());
        }

        boolean isvalid = false;
        String nick = ParamUtil.get(req, "name");
        if (nick.equals("")) {
            throw new InvalidNameException(req);
        }
        String pwd = req.getParameter("pwd");
        if (pwd == null) {
            throw new WrongPasswordException(req);
        }

        UserDb user = new UserDb();
        user = user.getUserDbByNick(nick);
        if (user == null || !user.isLoaded())
            throw new InvalidNameException(req);
        // 检查密码是否相符
        String MD5pwd = "";
        try {
            MD5pwd = SecurityUtil.MD5(pwd);
        } catch (Exception e) {
            logger.error("login MD5 exception: " + e.getMessage());
        }

        com.redmoon.oa.person.UserDb ud = new com.redmoon.oa.person.UserDb();
        ud = ud.getUserDb(nick);

        // 配置文件中设置支持DVBBS登录是否支持
        if (cfg.getBooleanProperty("forum.loginCompatibleDVBBS")) {
            if (ud.getPwdMD5().length() == 16) { // 如果数据库中密码为16位长度，则表示为DVBBS导入用户
                String pwd16 = MD5pwd.substring(8, 24); // 取MD5的中间16位
                if (!ud.getPwdMD5().equals(pwd16)) {
                    throw new WrongPasswordException(req);
                }
            } else {
                if (!ud.getPwdMD5().equals(MD5pwd)) {
                    throw new WrongPasswordException(req);
                }
            }
        } else {
            if (!user.getPwdMd5().equals(MD5pwd)) {
                throw new WrongPasswordException(req);
            }
        }

        if (!user.isValid()) {
            throw new ErrMsgException(LoadString(req, "err_invalid"));
        }
        // 检查用户是否在审核中
        if (user.getCheckStatus() != UserDb.CHECK_STATUS_PASS) {
            RegConfig rc = new RegConfig();
            int regVerify = rc.getIntProperty("regVerify");
            if (regVerify == RegConfig.REGIST_VERIFY_MANUAL) {
                throw new ErrMsgException(LoadString(req, "info_need_check_manual"));
            } else if (regVerify == RegConfig.REGIST_VERIFY_EMAIL) {
                throw new ErrMsgException(LoadString(req, "info_need_check_email"));
            }
            // 如果已设置为无需审核，则用户可以登录
        }
        // 检查是否被关进了监狱
        if (Prision.isUserArrested(user.getName())) {
            Calendar cal = Prision.getReleaseDate(user.getName());
            String s = LoadString(req, "err_prision");
            s = s.replaceFirst("\\$d", ForumSkin.formatDate(req, cal.getTime()));
            throw new ErrMsgException(s); // "您已被关押在社区监狱中，释放日期为" + DateUtil.format(cal, "yy-MM-dd") + "，不能登录！");
        }

        // 取得登录前的用户名
        String oldname = getUser(req);

        // 判断是否已登录,即重复登录
        if (oldname.equals(user.getName())) {
            return true;
        }

        isvalid = doLogin(req, res, user);

        return isvalid;
    }

    private static String encodeCookie(String username, String password) {
        StringBuffer buf = new StringBuffer();
        if (username != null && password != null) {
            byte[] bytes = (username + ENCODE_DELIMETER + password).getBytes();
            int b;

            for (int n = 0; n < bytes.length; n++) {
                b = bytes[n] ^ (ENCODE_XORMASK + n);
                buf.append((char) (ENCODE_CHAR_OFFSET1 + (b & 0x0F)));
                buf.append((char) (ENCODE_CHAR_OFFSET2 + ((b >> 4) & 0x0F)));
            }
        }
        return buf.toString();
    }

    private static String[] decodeCookie(String cookieVal) {
        // check that the cookie value isn't null or zero-length
        if (cookieVal == null || cookieVal.length() <= 0) {
            return null;
        }

        // unrafel the cookie value
        char[] chars = cookieVal.toCharArray();
        byte[] bytes = new byte[chars.length / 2];
        int b;
        for (int n = 0, m = 0; n < bytes.length; n++) {
            b = chars[m++] - ENCODE_CHAR_OFFSET1;
            b |= (chars[m++] - ENCODE_CHAR_OFFSET2) << 4;
            bytes[n] = (byte) (b ^ (ENCODE_XORMASK + n));
        }
        cookieVal = new String(bytes);
        int pos = cookieVal.indexOf(ENCODE_DELIMETER);
        String username = (pos < 0) ? "" : cookieVal.substring(0, pos);
        String password = (pos < 0) ? "" : cookieVal.substring(pos + 1);

        return new String[] {username, password};
    }

    /**
     * 登记访客,只放在listtopic.jsp及index.jsp，其余页面不放
     * @param request 请求.
     * @param res 响应.
     * @return void
     */
    public void enrolGuest(HttpServletRequest request, HttpServletResponse res) throws
            ErrMsgException, UserArrestedException {
        /**
         * zjrj.cn/index.jsp登录---->login.jsp--->/forum/index.jsp---->enrolGuest() refreshStayTime()
         * 王长江登录时发现 登录成功后，用户名在listtopic.jsp中看时变成了随机用户名，并且测试后发现
         * 该随机用户名是在refreshStayTime()时被create的，并且该随机用户于在线列表中还不是游客身份，说明"islogin"这个
         * cookie应该是被写入了，怀疑在login.jsp中因为<html><body>头的存在，可能使cookie未能及时写入
         * 而被重定向至index.jsp后，enrolGuest认为未被登记，而将其登记，而当5分钟后，refreshStayTime()时，islogin这个cookie
         * 已被写入，造成随机名称用户有非游客的身份出现在线列中有
         * 解决方法：将login.jsp中多余的<html><body>头去掉，将%> <%之间的换行及空格也去掉
         * 经检查，原来有可能是login.jsp中sendRedirect的问题 见http://dev.csdn.net/develop/article/6/6435.shtm
         */
        // 已经用会员身份登录了
        if (isUserLogin(request)) {
            HttpSession session = request.getSession(true);
            Authorization auth = (Authorization) session.getAttribute(
                    SESSION_CWBBS_AUTH);
            if (!auth.isArrestChecked()) { // 如果未检查过是否被捕
                // 检查其是否被捕，如果是的话，则强制其退出登录
                // 此检查只进行一次
                auth.setArrestChecked(true);
                String userName = getUser(request);
                if (Prision.isUserArrested(userName)) {
                    // 如果被捕，则撤销以前保存的登录信息
                    logout(request, res);

                    Calendar cal = Prision.getReleaseDate(userName);

                    String s = LoadString(request, "err_prision");
                    s = s.replaceFirst("\\$d",
                                       ForumSkin.formatDate(request,
                            cal.getTime()));
                    throw new ErrMsgException(s); // "您已被关押在社区监狱中，释放日期为" + DateUtil.format(cal, "yy-MM-dd") + "，不能登录！");
                } else // 未被捕，则退出函数
                {
                    return;
                }
            } else {
                // 已检查过是否被捕
                return;
            }
        }

        HttpSession session = request.getSession(true);
        Authorization auth = (Authorization) session.getAttribute(
                SESSION_CWBBS_AUTH);
        // 如果用户未登录,则检查是否已随机赋予name值
        if (auth != null)
            return; // name已记录则表示已被登记过
        String guestname = FileUpload.getRandName(); // "" + System.currentTieMillis();
        String boardcode = StrUtil.getNullString(ParamUtil.get(request,
                "boardcode"));

        // 在数据库中插入在线记录，置游客在位时间
        OnlineUserDb ou = new OnlineUserDb();
        int k = 0;
        boolean isGuestNameUsed = true;
        while (k < 10) {
            // 检查该用户名是否已被使用，防止重复
            ou = ou.getOnlineUserDb(guestname);
            // 未被使用，则退出
            if (!ou.isLoaded()) {
                isGuestNameUsed = false;
                break;
            } else {
                isGuestNameUsed = true;
                guestname = FileUpload.getRandName(); // "" + System.currentTimeMillis() + "f";
            }
            k++;
        }

        // 原来在forum/index.jsp中之所以不能写入cookie，可能与userservice.enrolGuest(request,response);
        // 在index.jsp中的位置有关，当位于网页的正文部分时，会不起作用，但listtopic.jsp放在body后一开始处却也是可以的
        // 将其移至index.jsp的首部时，cookie就能被写入了
        if (!isGuestNameUsed) {
            auth = new Authorization(guestname, true);
            session.setAttribute(SESSION_CWBBS_AUTH, auth);
            ou.setName(guestname);
            ou.setBoardCode(boardcode);
            ou.setGuest(true);
            ou.setIp(request.getRemoteAddr());
            ou.setCovered(false);
            ou.create();
        }
    }

    public Authorization getAuthorization(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return (Authorization) session.getAttribute(SESSION_CWBBS_AUTH);
    }

    /**
     * 判别用户是否具有版块中的权限
     * @param boardCode String
     * @param doWhat String
     * @return boolean
     */
    public boolean canUserDo(HttpServletRequest request, String boardCode,
                             String doWhat) {
        // 管理员具有所有的权限
        if (isMasterLogin(request))
            return true;

        // 版主具有该版的所有权限
        if (isUserHasManagerIdentity(request, boardCode))
        	return true;

        boolean isDefaultLoaded = false;
        boolean defaultPriv = false;

        String groupCode = "";

        // 如果是注册用户
        if (isUserLogin(request)) {
            String userName = getUser(request);

            UserDb ud = new UserDb();
            ud = ud.getUser(userName);
            if (!ud.isLoaded())
                return false;
            UserPrivDb upd = new UserPrivDb();
            upd = upd.getUserPrivDb(userName);
            // 使用默认新用户设定的参数
            boolean userPriv = false;
            if (upd.getBoolean("is_default")) {
                defaultPriv = getDefaultPriv(doWhat);
                isDefaultLoaded = true;
                userPriv = defaultPriv;
            } else {
                // sq_user_priv表中没有字段enter_board view_topic view_listmember view_userinfo
                if (!doWhat.equals("view_online") &&
                    !doWhat.equals("enter_board") &&
                    !doWhat.equals("view_topic") &&
                    !doWhat.equals("view_listmember") &&
                    !doWhat.equals("view_userinfo")) {
                    userPriv = upd.getBoolean(doWhat);
                    if (!userPriv) // 如果用户不使用默认设置，且被设置为false，则说明已被禁用此项功能
                        return false;
                    // else
                    //    return true; // 当不使用默认权限时，则说明强制使用该权限
                }
            }

            // 当默认权限允许或者用户权限允许，则以用户组权限为准
            groupCode = ud.getUserGroupDb().getCode();
        } else {
            // 游客
            // groupCode = UserGroupDb.GUEST;
            UserGroupDb ug = new UserGroupDb();
            groupCode = ug.getGuestGroupCodeByIP(StrUtil.getIp(request));
        }

        // 检查用户所在组是否具有相应权限
        UserGroupPrivDb ugpd = new UserGroupPrivDb();
        String groupBoardCode = boardCode;
        if (boardCode.equals(""))
        	groupBoardCode = UserGroupPrivDb.ALLBOARD; // 在forum/search.jsp中用到
        ugpd = ugpd.getUserGroupPrivDb(groupCode, groupBoardCode);
        boolean groupPriv = false;

        // 如果不是游客
        if (!groupCode.equals(UserGroupDb.GUEST)) {
            // 使用默认新用户的设置
            if (ugpd.getBoolean("is_default")) {
                if (!isDefaultLoaded) {
                    defaultPriv = getDefaultPriv(doWhat);
                    isDefaultLoaded = true;
                }
                groupPriv = defaultPriv;
            } else {
                groupPriv = ugpd.getBoolean(doWhat);
            }
        } else {
            groupPriv = ugpd.getBoolean(doWhat);
            // LogUtil.getLog(getClass()).info("groupPriv=" + groupPriv);
        }
        // LogUtil.getLog(getClass()).info("groupCode=" + groupCode + " defaultPriv=" + defaultPriv + " groupPriv=" + groupPriv + " doWhat=" + doWhat);

        return groupPriv;
    }

    public boolean getDefaultPriv(String doWhat) {
        boolean defaultPriv = true;
        Config cfg = Config.getInstance();
        if (doWhat.equals("add_topic")) {
            defaultPriv = cfg.getProperty("forum.canUserAddTopic").equals("true");
        } else if (doWhat.equals("attach_upload")) {
            defaultPriv = cfg.getProperty("forum.canUserUploadAttach").equals("true");
        } else if (doWhat.equals("reply_topic")) {
            defaultPriv = cfg.getProperty("forum.canUserReplyTopic").equals("true");
        } else if (doWhat.equals("vote")) {
            defaultPriv = cfg.getProperty("forum.canUserVote").equals("true");
        } else if (doWhat.equals("search")) {
            defaultPriv = cfg.getProperty("forum.canUserSearch").equals(
                    "true");
        }
        return defaultPriv;
    }

    /**
     * 判断用户能否在版块中上传文件
     * @param boardCode String
     * @return boolean
     */
    public boolean canUserUpload(HttpServletRequest request, String boardCode) {
        String groupCode = "";

        boolean defaultPriv = false;
        boolean isDefalutLoaded = false;
        UserPrivDb upd = new UserPrivDb();

        if (isUserLogin(request)) {
            String userName = getUser(request);
            UserDb ud = new UserDb();
            ud = ud.getUser(userName);
            if (!ud.isLoaded())
                return false;
            groupCode = ud.getUserGroupDb().getCode();

            upd = upd.getUserPrivDb(userName);
            if (upd.getBoolean("is_default")) {
                defaultPriv = getDefaultPriv("attach_upload");
                isDefalutLoaded = true;
            } else {
                if (!upd.getBoolean("attach_upload"))
                    return false;
            }
        } else
            groupCode = UserGroupDb.GUEST;

        UserGroupPrivDb ugpd = new UserGroupPrivDb();
        ugpd = ugpd.getUserGroupPrivDb(groupCode, boardCode);
        boolean groupPriv = false;
        if (ugpd.getBoolean("is_default")) {
            if (!isDefalutLoaded) {
                defaultPriv = getDefaultPriv("attach_upload");
            }
            groupPriv = defaultPriv;
        } else {
            groupPriv = ugpd.getBoolean("attach_upload");
        }
        // 没有组权限，则返回false
        if (!groupPriv) {
            return false;
        }

        if (isUserLogin(request)) {
            Config cfg = Config.getInstance();
            int uploadCount = cfg.getIntProperty("forum.maxAttachDayCount");
            if (upd.getBoolean("is_default")) {
                if (upd.getInt("attach_day_count") > uploadCount) {
                    uploadCount = upd.getInt("attach_day_count");
                }
            }
            if (upd.getAttachTodayUploadCount() < uploadCount)
                return true;
            else
                return false;
        } else
            return groupPriv;
        // LogUtil.getLog(getClass()).info(getClass() + " " + upd.get("attach_today_upload_count").getClass() + " v=" + upd.get("attach_today_upload_count"));

    }


    public boolean regist(HttpServletRequest req, HttpServletResponse res) throws
            ErrMsgException {
        com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.
                                                 Privilege();
        if (!privilege.isUserLogin(req))
            throw new ErrMsgException("请先登录！");
        String RegName = ParamUtil.get(req, "RegName").trim();
        if (RegName == null || RegName.trim().equals("")) {
            throw new ErrMsgException("用户名不能为空!");
        }

        // 检查是否有重名
        String sql = "select name from users where nick=" +
                     StrUtil.sqlstr(RegName);
        Conn conn = null;
        try {
            conn = new Conn(Global.getDefaultDB());
            ResultSet rs = conn.executeQuery(sql);
            if (rs.next()) {
                throw new ErrMsgException("该用户名已被使用，请选用别的名称!");
            }
        } catch (SQLException e) {
            logger.error("regist:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        // ForumDb fd = new ForumDb();
        // fd = fd.getForumDb();
        // fd.FilterUserName(req, RegName);

        boolean re = false;

        String userName = privilege.getUser(req);

        UserDb user = new UserDb();

        String Password = "";
        String Password2 = "";
        String Answer = "";
        String Question = "";

        String RealName = ParamUtil.get(req, "RealName");
        String Career = ParamUtil.get(req, "Career");
        String Gender = ParamUtil.get(req, "Gender");
        String Job = ParamUtil.get(req, "Job");
        String BirthYear = ParamUtil.get(req, "BirthYear");
        String BirthMonth = ParamUtil.get(req, "BirthMonth");
        String BirthDay = ParamUtil.get(req, "BirthDay");
        String Birthday = BirthYear + "-" + BirthMonth + "-" + BirthDay;
        int Marriage = ParamUtil.getInt(req, "Marriage");
        String Phone = ParamUtil.get(req, "Phone");
        String Mobile = ParamUtil.get(req, "Mobile");
        String State = ParamUtil.get(req, "State");
        String City = ParamUtil.get(req, "City");
        String Address = ParamUtil.get(req, "Address");
        String PostCode = ParamUtil.get(req, "PostCode");
        String IDCard = ParamUtil.get(req, "IDCard");
        String RealPic = ParamUtil.get(req, "RealPic");
        String Hobbies = ParamUtil.get(req, "Hobbies");
        String Email = ParamUtil.get(req, "Email");
        String OICQ = ParamUtil.get(req, "OICQ");
        String sign = ParamUtil.get(req, "Content");
        boolean secret = ParamUtil.getBoolean(req, "isSecret", false);

        user.setName(RegName);
        user.setPwdMd5(Password);
        user.setRawPwd(Password2);
        user.setQuestion(Question);
        user.setAnswer(Answer);
        user.setRealName(RealName);
        user.setCareer(Career);
        user.setGender(Gender);
        user.setJob(Job);
        java.util.Date d = DateUtil.parse(Birthday, "yyyy-MM-dd");
        user.setBirthday(d);
        user.setMarriage(Marriage);
        user.setPhone(Phone);
        user.setMobile(Mobile);
        user.setState(State);
        user.setCity(City);
        user.setAddress(Address);
        user.setPostCode(PostCode);
        user.setIDCard(IDCard);
        user.setRealPic(RealPic);
        user.setHobbies(Hobbies);
        user.setEmail(Email);
        user.setOicq(OICQ);
        user.setRegDate(new java.util.Date());
        user.setSign(sign);
        user.setSecret(secret);
        user.setNick(RegName);

        // TimeZone tz = TimeZone.getDefault();
        user.setTimeZone(Global.timeZone);

        // 置经验、信用、金币等初值
        ScoreMgr sm = new ScoreMgr();
        Vector v = sm.getAllScore();
        Iterator ir = v.iterator();
        while (ir.hasNext()) {
            ScoreUnit su = (ScoreUnit) ir.next();
            IPluginScore ips = su.getScore();
            // 判别类是否存在
            if (ips != null)
                ips.regist(user);
        }
        re = user.create();

        if (re) {
            doLogin(req, res, user.getUser(RegName));
        }
        return re;
    }
}

