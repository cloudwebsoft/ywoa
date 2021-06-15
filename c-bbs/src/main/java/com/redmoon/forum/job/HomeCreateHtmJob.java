package com.redmoon.forum.job;

import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import com.cloudwebsoft.framework.util.*;
import org.quartz.*;
import cn.js.fan.util.StrUtil;

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
public class HomeCreateHtmJob implements Job {
    public HomeCreateHtmJob() {
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
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        try {
            String filePath = Global.realPath + "doc/template/community_index.htm";
            // System.out.println(getClass() + " filePath=" + filePath);
            TemplateLoader tl = new TemplateLoader(null, filePath);
            FileUtil fu = new FileUtil();
            fu.WriteFile(Global.getRealPath() + "forum/" +
                         "index.htm",
                         tl.toString(), "UTF-8");
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error("execute:" + e.getMessage());
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        }
    }
}
