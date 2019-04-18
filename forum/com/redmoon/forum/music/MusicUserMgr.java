package com.redmoon.forum.music;

import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.Config;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.plugin.score.Gold;
import cn.js.fan.util.ResKeyException;
import com.redmoon.forum.message.MessageMgr;
import cn.js.fan.web.SkinUtil;
import com.redmoon.forum.person.UserDb;
import cn.js.fan.util.StrUtil;
import com.redmoon.forum.message.MessageDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.forum.plugin.base.IPluginScore;

/**
 * <p>Title: </p>
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
public class MusicUserMgr {
    public MusicUserMgr() {
    }

    /**
     * 点歌
     * @param request HttpServletRequest
     * @param userName String
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean orderMusicForUser(HttpServletRequest request,
                                     String userName, long musicId) throws
            ErrMsgException {
        Privilege pvg = new Privilege();
        if (!pvg.isUserLogin(request)) {
            throw new ErrMsgException("请先登录！");
        }
        Config cfg = Config.getInstance();
        int gold = cfg.getIntProperty("forum.orderMusicGold");

        Gold gd = new Gold();
        boolean re = false;

        // 扣费
        try {
            re = gd.pay(pvg.getUser(request), IPluginScore.SELLER_SYSTEM, gold);
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        // 扣费成功，则点歌
        if (re) {
            MusicUserDb mud = new MusicUserDb();
            long createId = mud.create(userName, pvg.getUser(request), musicId);
            if (createId!=-1) {
                UserDb ud = new UserDb();
                ud = ud.getUser(pvg.getUser(request));
                String nick = ud.getNick();

                MusicFileDb mfd = new MusicFileDb();
                mfd = mfd.getMusicFileDb(musicId);
                mfd.setDownloadCount(mfd.getDownloadCount() + 1);
                mfd.save(new JdbcTemplate());

                MessageDb shortmsg = new MessageDb();
                shortmsg.setTitle(nick + " 点歌-" + mfd.getName());
                String content = "[URL=../forum/music_listen.jsp?orderId=" + createId + "]请点击此处，收听歌曲！[/URL]";
                shortmsg.setContent(content);
                shortmsg.setSender(shortmsg.USER_SYSTEM);
                shortmsg.setReceiver(userName);
                shortmsg.setIp(request.getRemoteAddr());
                shortmsg.setType(shortmsg.TYPE_SYSTEM);
                shortmsg.create();
            }
            else
                re = false;
        }
        return re;
    }
}
