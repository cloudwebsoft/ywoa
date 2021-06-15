package cn.js.fan.db;

import java.util.*;

public class ListResult {
    public ListResult() {
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setResult(Vector result) {
        this.result = result;
    }

    public long getTotal() {
        return total;
    }

    public Vector getResult() {
        return result;
    }

    /**
     * 总记录数
     */
    private long total = -1;
    private Vector result;

}
