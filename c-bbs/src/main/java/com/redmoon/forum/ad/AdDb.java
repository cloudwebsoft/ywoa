package com.redmoon.forum.ad;

import com.cloudwebsoft.framework.base.QObjectDb;
import cn.js.fan.util.StrUtil;
import java.util.Vector;
import com.cloudwebsoft.framework.base.QObjectBlockIterator;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.DateUtil;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.Leaf;

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
public class AdDb extends QObjectDb {
    public static final int TYPE_HEADER = 0; // 一个
    public static final int TYPE_FOOTER = 1; // 一个
    public static final int TYPE_TEXT = 2; // 多个
    public static final int TYPE_FLOAT = 3; // 一个
    public static final int TYPE_TOPIC_BOTTOM = 4; // 多个
    public static final int TYPE_COUPLE = 5; // 门联广告左
    public static final int TYPE_TOPIC_RIGHT = 6; // 一个
    public static final int TYPE_COUPLE_RIGHT = 7; // 门联广告右
    public static final int TYPE_TOPIC_AFTER = 8; // 一、二楼间广告

    public static final int KIND_HTML = 0;
    public static final int KIND_TEXT = 1;
    public static final int KIND_IMAGE = 2;
    public static final int KIND_FLASH = 3;

    public AdDb() {
    }

    public boolean create(ParamChecker paramChecker) throws ResKeyException,
            ErrMsgException {
        int ad_kind = paramChecker.getInt("ad_kind");
        if (ad_kind == KIND_FLASH) {
            int width = paramChecker.getInt("width");
            int height = paramChecker.getInt("height");
            if (width == 0 || height == 0) {
                throw new ResKeyException("res.forum.ad.AdDb", "need_width_height");
            }
        }
        // LogUtil.getLog(getClass()).info("kind_type=" + kind_type + " width=" + paramChecker.getInt("width"));
        return super.create(paramChecker);
    }

    public boolean save(ParamChecker paramChecker) throws ResKeyException,
            ErrMsgException {
        int ad_kind = paramChecker.getInt("ad_kind");
        if (ad_kind == KIND_FLASH) {
            int width = paramChecker.getInt("width");
            int height = paramChecker.getInt("height");
            if (width == 0 || height == 0) {
                throw new ResKeyException("res.forum.ad.AdDb", "need_width_height");
            }
        }
        // LogUtil.getLog(getClass()).info("ad_type=" + kind_type + " width=" + paramChecker.getInt("width"));
        return super.save(paramChecker);
    }

    public boolean isOnBoard(String boardCode) {
        // 检查日期
        // LogUtil.getLog(getClass()).info("beginDate=" + getDate("begin_date") + " endDate=" + getDate("end_date"));
        java.util.Date beginDate = getDate("begin_date");
        if (beginDate != null) {
            if (DateUtil.compare(new java.util.Date(), beginDate) == 2)
                return false;
        }

        java.util.Date endDate = getDate("end_date");
        if (endDate != null) {
            if (DateUtil.compare(new java.util.Date(), endDate) == 1)
                return false;
        }

        String[] boards = StrUtil.split(getString("boardcodes"), ",");
        if (boards != null) {
            int len = boards.length;
            for (int i = 0; i < len; i++) {
                if (boardCode.equals(boards[i]))
                    return true;
            }
        }

        return false;
    }

    /**
     * 根据boardCode及位置获取广告
     * @param boardCode String 为空时表示默认的广告，用于forum/index.jsp页面的显示
     * @param position int
     * @return AdDb
     */
    public static Vector getADOnBoard(String boardCode, int type) {
        AdDb ad = new AdDb();
        String sql = "select id from " + ad.table.getName() + " where ad_type=" +
                     type;
        int count = (int) ad.getQObjectCount(sql);
        // LogUtil.getLog(AdDb.class).info("sql=" + sql + " count=" + count);

        QObjectBlockIterator qir = ad.getQObjects(sql, 0, count);
        Vector v = new Vector();
        while (qir.hasNext()) {
            ad = (AdDb) qir.next();
            if (ad.isOnBoard(boardCode))
                v.addElement(ad);
        }
        return v;
    }

    public String render(HttpServletRequest request) {
        int kind = getInt("ad_kind");
        String str = "";
        if (kind == KIND_HTML) {
            str = StrUtil.getNullStr(getString("content"));
        } else if (kind == KIND_TEXT) {
            String url = StrUtil.getNullStr(getString("url"));
            String fontSize = StrUtil.getNullStr(getString("font_size"));
            String text = StrUtil.toHtml(getString("content"));
            if (!fontSize.equals("")) {
                str = "<font size='" + fontSize + "'>" + text + "</font>";
            } else
                str = text;
            if (!url.equals("")) {
                str = "<a target='_blank' href='" + url + "'>" + str + "</a>";
            }
        } else if (kind == KIND_IMAGE) {
            String img = StrUtil.getNullStr(getString("content"));
            String url = StrUtil.getNullStr(getString("url"));
            int width = getInt("width");
            int height = getInt("height");
            String image_alt = StrUtil.getNullStr(getString("image_alt"));
            String w = "", h = "";
            if (width!=0)
                w = "width=" + width;
            if (height!=0)
                h = "height=" + height;
            str = "<img " + w + " " + h + " alt='" + image_alt +
                  "' src='" + img + "' style='cursor:hand' onClick=\"window.open('" + url + "')\"/>";
        } else if (kind == KIND_FLASH) {
            String flash = StrUtil.getNullStr(getString("content"));
            int width = getInt("width");
            int height = getInt("height");
            str = "<OBJECT codeBase=http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=4,0,2,0 classid=clsid:D27CDB6E-AE6D-11cf-96B8-444553540000 width='" +
                  width + "' height='" + height +
                    "'><PARAM NAME=movie VALUE=\"" + flash + "\"><PARAM NAME=quality VALUE=high><param name=\"Wmode\" value=\"transparent\"><embed src=\"" +
                  flash + "\" quality=high pluginspage='hhttttpp://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash' type='application/x-shockwave-flash' width='" +
                  width + "' height='" + height + "'></embed>" +
                  "</OBJECT><BR>";
        }
        return str;
    }

}

