package com.redmoon.oa.flow;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.db.PrimaryKey;

public class FormCache extends ObjectCache {
    String FIELDPREFIX = "FIELD_";

    public FormCache() {
        listCachable = false; // 禁止列表缓存，因为当修改表单所属父节点时，在流程属性中选表单时会因缓存而致出问题
    }

    public FormCache(FormDb td) {
        super(td);
        listCachable = false; // 禁止列表缓存，因为当修改表单所属父节点时，在流程属性中选表单时会因缓存而致出问题
    }

}
