package com.cloudweb.oa.bean;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExportExcelItem implements Serializable {
    String uid;
    int curRow;
    int rows;
    /**
     * ft_export_excel中记录的id
     */
    long id;
    boolean finished = false;
}
