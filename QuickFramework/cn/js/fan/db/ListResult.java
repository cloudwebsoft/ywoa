package cn.js.fan.db;

import java.util.*;

public class ListResult {
    public ListResult() {
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setResult(Vector result) {
        this.result = result;
    }

    public int getTotal() {
        return total;
    }

    public Vector getResult() {
        return result;
    }

    /**
     * ×Ü¼ÇÂ¼Êý
     */
    private int total = -1;
    private Vector result;

}
