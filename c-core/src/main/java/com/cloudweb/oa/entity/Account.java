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
 * @since 2020-02-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Account extends Model<Account> {

    private static final long serialVersionUID = 1L;

    private String name;

    @TableField("userName")
    private String userName;

    private String unitCode;


    @Override
    protected Serializable pkVal() {
        return this.name;
    }

}
