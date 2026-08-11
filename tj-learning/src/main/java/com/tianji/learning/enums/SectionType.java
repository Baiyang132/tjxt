package com.tianji.learning.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tianji.common.enums.BaseEnum;
import lombok.Getter;

<<<<<<< Updated upstream
=======
/**
 * 小节类型
 */
>>>>>>> Stashed changes
@Getter
public enum SectionType implements BaseEnum {
    VIDEO(1, "视频"),
    EXAM(2, "考试"),
    ;
    @JsonValue
    @EnumValue
    int value;
    String desc;

    SectionType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

<<<<<<< Updated upstream

=======
>>>>>>> Stashed changes
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SectionType of(Integer value){
        if (value == null) {
            return null;
        }
<<<<<<< Updated upstream
        for (SectionType status : values()) {
            if (status.equalsValue(value)) {
                return status;
=======
        for (SectionType type : values()) {
            if (type.equalsValue(value)) {
                return type;
>>>>>>> Stashed changes
            }
        }
        return null;
    }
}
