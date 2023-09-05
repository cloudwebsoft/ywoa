package com.cloudweb.oa.cache;

import cn.js.fan.cache.jcs.RMCache;
import org.springframework.stereotype.Component;

@Component
public class FlowShowRuleCache {

    private final String GROUP = "flow_show_rule";

    public void put(String typeCode, String showRuleScripts) {
        RMCache.getInstance().putInGroup(typeCode, GROUP, showRuleScripts);
    }

    public String get(String typeCode) {
        return (String)RMCache.getInstance().getFromGroup(typeCode, GROUP);
    }

    public void refresh(String typeCode) {
        RMCache.getInstance().remove(typeCode, GROUP);
    }

    public void refreshAll() {
        RMCache.getInstance().invalidateGroup(GROUP);
    }
}
