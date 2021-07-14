package com.redmoon.forum.plugin2.alipay;

import com.redmoon.forum.plugin2.base.IRender;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.forum.MsgDb;
import cn.js.fan.web.Global;
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
public class AlipayRender implements IRender {
    public String rend(HttpServletRequest request, MsgDb msgDb) {
        AlipayDb ad = new AlipayDb();
        ad = ad.getAlipaydDb(msgDb.getId());
        String str = "";
        // str += "" + ad.getSeller() + "<BR>";
        str += "<p style='line-height:300%'><b>商品名称：</b>&nbsp;" + ad.getSubject() + "<BR>";
        str += "<b>商品价格：</b> " + ad.getPrice() + "元<BR>";
        str += "<b>邮递信息：</b> " + ad.getTransportDesc(request) + "<BR>";
        String tmp = "<a href='http://amos1.taobao.com/msg.ww?v=2&amp;uid=" + ad.getWw() + "&amp;s=1'><img src='http://amos1.taobao.com/online.ww?v=2&amp;uid=555555&amp;s=1' border=0></a>";
        tmp += "&nbsp;&nbsp;<a href='http://wpa.qq.com/msgrd?V=1&amp;Uin=" + ad.getQq() + "&amp;Site=" + Global.server + "&amp;Menu=no' target='_blank'><img alt='联系我' src='http://wpa.qq.com/pa?p=1:" + ad.getQq() + ":10' border='0'></a>";
        str += "<b>联系方法：</b>&nbsp;" + tmp + "<BR>";
        str += "<b>演示网址：</b>&nbsp;<a href='" + ad.getDemo() + "'>" + ad.getDemo() + "</a><BR>";
        try {
        str += "<a href='https://www.alipay.com/payto:" + ad.getSeller() + "?subject=" + StrUtil.UrlEncode(ad.getSubject(), "gb2312") + "&body=" + StrUtil.UrlEncode(msgDb.getContent(), "gb2312") + "&price=" + ad.getPrice() + "&amp;type=1&amp;transport=" + ad.getTransport() + "&amp;ordinary_fee=" + ad.getOrdinary() + "&amp;express_fee2=" + ad.getExpress() + "&amp;url=" + ad.getDemo() + "&amp;readonly=true&amp;readonly=true' target='_blank'><img src='" + Global.getRootPath() + "/images/alipay-trade.gif' border=0 target=_blank width=180 height=60></a>" + "<BR>";
        }
        catch (Exception e){}
        str += "</p>";
        str += "<form action='plugin2/alipay/search.jsp' method=post target=_blank><input name='what'>&nbsp;<input type=submit value='搜索支付宝商品'></form>";
        return str;
    }

}
