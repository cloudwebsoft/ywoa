package com.redmoon.forum.plugin.refer;

import com.redmoon.forum.*;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.plugin.base.IPluginRender;
import org.apache.log4j.Logger;
import com.redmoon.forum.plugin.DefaultRender;
import com.redmoon.forum.person.UserDb;
import cn.js.fan.util.StrUtil;

public class ReferRender extends DefaultRender {
    Logger logger = Logger.getLogger(this.getClass().getName());
    private boolean showAttachment = true;

    public ReferRender() {
    }

    public String RenderTitle(HttpServletRequest request, MsgDb md) {
        return super.RenderTitle(request, md);
    }

    public MsgPollDb RenderVote(HttpServletRequest request, MsgDb md) {
        return super.RenderVote(request, md);
    }

    /**
     * 根据规则判断是否显示content
     * @param request HttpServletRequest
     * @param md MsgDb
     * @return String
     */
    public String RenderContent(HttpServletRequest request, MsgDb md) {
        String str = "";
        String user = Privilege.getUser(request);

        DefaultRender dr = new DefaultRender();
        // 检查看贴人user是否为楼主
        MsgDb rootMsgDb = md.getMsgDb(md.getRootid());
    	Privilege pvg = new Privilege();        
    	// 管理员、版主、楼主全部都能看见
        if (Privilege.isMasterLogin(request) || pvg.isUserHasManagerIdentity(request, md.getboardcode()) || user.equals(rootMsgDb.getName())) {
            return dr.doRendContent(request, md);
        }

        ReferDb rd = new ReferDb();
        rd = rd.getReferDb(md.getId());
        
        // 取得贴子秘级
        switch(rd.getSecretLevel()) {
            case ReferDb.SECRET_LEVEL_PUBLIC:
                str = dr.doRendContent(request, md);
                break;
            case ReferDb.SECRET_LEVEL_MSG_OWNER:
            	// 回复者本人可见
	            if (user.equals(md.getName()))
					return dr.doRendContent(request, md);
				else
					str = ReferSkin.LoadString(request, "MSG_CONTENT_NOTDISPLAY");
                break;
            case ReferDb.SECRET_LEVEL_MANAGER:
            	str = ReferSkin.LoadString(request, "MSG_CONTENT_NOTDISPLAY");
            	break;
            default:
                str = ReferSkin.LoadString(request,
                                "MSG_CONTENT_NOTDISPLAY");
                break;
        }
        return str;
    }


}
