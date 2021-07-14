package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Privilege extends Model<Privilege> {

    private static final long serialVersionUID = 1L;

    private String priv;

    private String description;

    @TableField("isSystem")
    private Boolean isSystem;

    private Integer isAdmin;

    private Integer kind;

    private Integer layer;

    private Integer orders;


    @Override
    protected Serializable pkVal() {
        return this.priv;
    }

}
