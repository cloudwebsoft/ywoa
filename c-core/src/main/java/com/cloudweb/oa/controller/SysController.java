package com.cloudweb.oa.controller;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.NumberUtil;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLConfig;
import cn.js.fan.web.Global;
import com.cloudweb.oa.exception.ValidateException;
import com.cloudweb.oa.utils.ResponseUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.SpConfig;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.ui.SkinMgr;
import com.redmoon.oa.util.TwoDimensionCode;
import org.apache.commons.jcs3.access.exception.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class SysController {

    @Autowired
    HttpServletRequest request;

    @Autowired
    ResponseUtil responseUtil;

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/cache")
    public String cache(Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        RMCache rmcache = RMCache.getInstance();
        Runtime runtime = Runtime.getRuntime();

        double freeMemory = (double)runtime.freeMemory()/(1024*1024);
        double totalMemory = (double)runtime.totalMemory()/(1024*1024);
        double maxMemory = Runtime.getRuntime().maxMemory()/(1024*1024);
        double usedMemory = totalMemory - freeMemory;
        double percentFree = (freeMemory/totalMemory)*100.0;
        int free = 100-(int)Math.round(percentFree);

        DecimalFormat mbFormat = new DecimalFormat("#0.00");
        DecimalFormat percentFormat = new DecimalFormat("#0.0");

        model.addAttribute("percentFree", percentFormat.format(percentFree));
        model.addAttribute("usedMemory", percentFormat.format(usedMemory));

        model.addAttribute("totalMemory", mbFormat.format(totalMemory));
        model.addAttribute("maxMemory", mbFormat.format(maxMemory));

        model.addAttribute("maxWebeditFileSize", NumberUtil.round((double) Global.getInstance().getMaxSize()/1024000, 1));
        model.addAttribute("fileSize", NumberUtil.round((double)Global.FileSize/1000, 1));

        model.addAttribute("numBlocks", 50);
        model.addAttribute("canCache", rmcache.getCanCache());

        model.addAttribute("processors", runtime.availableProcessors());

        List<Integer> list = new ArrayList<>();
        int numBlocks = 50;
        int blockSize = 100/numBlocks;

        for (int i = 0; i < numBlocks; i++) {
            if ((i * (100 / numBlocks)) < free) {
                list.add(0);
            }
            else {
                list.add(1);
            }
        }

        model.addAttribute("blockWidth", blockSize);
        model.addAttribute("blockList", list);

        return "th/admin/cache";
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/stopCache", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String stopCache() throws ValidateException {
        RMCache rmcache = RMCache.getInstance();
        rmcache.setCanCache(false);
        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/startCache", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String startCache() throws ValidateException {
        RMCache rmcache = RMCache.getInstance();
        rmcache.setCanCache(true);
        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/clearCache", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String clearCache() throws ValidateException {
        RMCache rmcache = RMCache.getInstance();
        try {
            rmcache.clear();
        } catch (CacheException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/gc", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String gc() throws ValidateException {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/configSys")
    public String configSys(Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        model.addAttribute("global", Global.getInstance());

        return "th/admin/config_sys";
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/editConfigSys", produces = {"text/html;charset=UTF-8;", "application/json;"})
    @ResponseBody
    public String editConfigSys() throws ValidateException {
        XMLConfig cfg = new XMLConfig("config_sys.xml", false, "utf-8");
        Enumeration e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String fieldName = (String) e.nextElement();
            if (fieldName.startsWith("Application")) {
                String value = ParamUtil.get(request, fieldName);
                cfg.set(fieldName, value);
            }
        }
        cfg.writemodify();
        Global.getInstance().init();
        TwoDimensionCode.generate2DCodeByMobileClient();//生成手机端二维码

        return responseUtil.getResultJson(true).toString();
    }

    @PreAuthorize("hasAnyAuthority('admin')")
    @RequestMapping(value = "/license")
    public String license(Model model) throws ValidateException {
        model.addAttribute("skinPath", SkinMgr.getSkinPath(request, false));

        model.addAttribute("license", License.getInstance());

        Config oaCfg = Config.getInstance();
        SpConfig spCfg = new SpConfig();
        String version = StrUtil.getNullStr(oaCfg.get("version"));
        String spVersion = StrUtil.getNullStr(spCfg.get("version"));

        model.addAttribute("version", version);
        model.addAttribute("spVersion", spVersion);

        return "th/admin/license";
    }
}
