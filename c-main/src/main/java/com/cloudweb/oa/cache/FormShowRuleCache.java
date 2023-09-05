package com.cloudweb.oa.cache;

import cn.js.fan.cache.jcs.RMCache;
import org.springframework.stereotype.Component;

@Component
public class FormShowRuleCache {

    private final String GROUP = "form_show_rule";

    public void put(String formCode, String showRuleScripts) {
        RMCache.getInstance().putInGroup(formCode, GROUP, showRuleScripts);
    }

    public String get(String formCode) {
        return (String)RMCache.getInstance().getFromGroup(formCode, GROUP);
    }

    public void refresh(String formCode) {
        RMCache.getInstance().remove(formCode, GROUP);
    }

    public void refreshAll() {
        RMCache.getInstance().invalidateGroup(GROUP);
    }
}
