package com.cloudweb.oa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2020-02-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value="user_group_of_role")
@Accessors(chain = true)
public class GroupOfRole extends Model<GroupOfRole> {

    private static final long serialVersionUID = 1L;

    @TableId("userGroupCode")
    private String userGroupCode;

    @TableField("roleCode")
    private String roleCode;


    @Override
    protected Serializable pkVal() {
        return this.userGroupCode;
    }

}
