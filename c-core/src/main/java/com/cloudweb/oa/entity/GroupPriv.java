package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
 * @since 2020-02-15
 */
@Data
@TableName(value="user_group_priv")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GroupPriv extends Model<GroupPriv> {

    private static final long serialVersionUID = 1L;

    @TableField("groupCode")
    private String groupCode;

    private String priv;


    @Override
    protected Serializable pkVal() {
        return null;
    }

}
