package com.redmoon.oa.questionnaire;

import cn.js.fan.base.ObjectCache;

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
public class QuestionnaireFormSubitemCache extends ObjectCache {
    public QuestionnaireFormSubitemCache() {
        listCachable = false;
    }

    public QuestionnaireFormSubitemCache(QuestionnaireFormSubitemDb questionnaireFormSubitemDb) {
        super(questionnaireFormSubitemDb);
        listCachable = false;
    }
}
