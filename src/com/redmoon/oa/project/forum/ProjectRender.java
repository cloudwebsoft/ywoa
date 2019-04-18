package com.redmoon.oa.project.forum;

import com.redmoon.forum.*;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.plugin.base.IPluginRender;
import org.apache.log4j.Logger;
import com.redmoon.forum.plugin.DefaultRender;
import com.redmoon.forum.person.UserDb;
import cn.js.fan.util.StrUtil;

public class ProjectRender extends DefaultRender {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public ProjectRender() {
    }

    public String RenderTitle(HttpServletRequest request, MsgDb md) {
        return super.RenderTitle(request, md);
    }

    public MsgPollDb RenderVote(HttpServletRequest request, MsgDb md) {
        return super.RenderVote(request, md);
    }


}
