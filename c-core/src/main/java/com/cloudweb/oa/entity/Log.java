package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author fgf
 * @since 2020-02-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Log extends Model<Log> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("USER_NAME")
    private String userName;

    @TableField("LOG_DATE")
    private Date logDate;

    @TableField("LOG_TYPE")
    private Integer logType;

    @TableField("IP")
    private String ip;

    @TableField("ACTION")
    private String action;

    private Integer device;

    private String unitCode;

    private String remark;

    private Integer level;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
