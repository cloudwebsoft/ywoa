package com.redmoon.oa.address;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;

public class UprightLineNode {
    int layer = 1;
    int count = 0;

    /**
     * 位置处在第layer层，需画count次的垂直线段
     * @param layer int
     * @param count int
     */
    public UprightLineNode(int layer, int count) {
        this.layer = layer;
        this.count = count;
    }

    public void show(JspWriter out, String src) throws IOException {
        count--;
        out.print("<img src="+src+" align='absmiddle'>");
    }

    public int getLayer() {
        return layer;
    }

    public int getCount() {
        return count;
    }

    public String toString() {
        return "Node layer=" + layer + "," + "count=" + count;
    }
}
