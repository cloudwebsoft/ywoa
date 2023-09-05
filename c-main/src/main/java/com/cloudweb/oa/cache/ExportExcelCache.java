package com.cloudweb.oa.cache;

import cn.js.fan.cache.jcs.RMCache;
import com.cloudweb.oa.bean.ExportExcelItem;
import org.springframework.stereotype.Component;

@Component
public class ExportExcelCache {

    private final String GROUP = "export_excel";

    public void put(String uid, ExportExcelItem exportExcelItem) {
        RMCache.getInstance().putInGroup(uid, GROUP, exportExcelItem);
    }

    public ExportExcelItem get(String uid) {
        return (ExportExcelItem)RMCache.getInstance().getFromGroup(uid, GROUP);
    }

    public void refresh(String uid) {
        RMCache.getInstance().remove(uid, GROUP);
    }

    public void refreshAll() {
        RMCache.getInstance().invalidateGroup(GROUP);
    }
}
