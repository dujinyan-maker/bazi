package org.example.project1.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

/**
 * TODO
 *
 * @Author djy
 * @Date 2026/1/8 18:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendPreyBlessingRequest {
    private String content;
    private Integer typeId;
}
