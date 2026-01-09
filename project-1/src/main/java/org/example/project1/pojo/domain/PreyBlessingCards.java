package org.example.project1.pojo.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDateTime;

/**
 * TODO
 *
 * @Author djy
 * @Date 2026/1/8 16:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreyBlessingCards {
    @TableId(value = "card_id", type = IdType.AUTO)
    private Long cardId; // 主键，bigint unsigned → Java 中用 Long

    private Long userId; // 用户ID

    private Integer blessingTypeId; // 祈福类型ID

    private String userMessage; // 用户消息（text）

    private String generatedMessage; // 生成的祝福语（text）

    @TableField(fill = FieldFill.DEFAULT)
    private LocalDateTime generatedAt; // 创建时间

    private Integer shareCount; // 分享次数，默认 0

    @TableField(fill = FieldFill.DEFAULT)
    private LocalDateTime savedAt; // 保存时间
}
