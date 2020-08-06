package com.condingfly.sql;

import java.io.Serializable;
import java.util.Objects;

/**
 * between 拼接SQL语句时用 ge 和 le ,因为前端可能只传了一个值
 * eq ne gt ge lt le between in notIn notNull null like notLike prefixLike notPrefixLike suffixLike notSuffixLike regex notRegex
 * @author hgp
 * @date 20-8-5
 */
public enum Condition implements Serializable {
    eq("eq"),
    ne("ne"),
    gt("gt"),
    ge("ge"),
    lt("lt"),
    le("le"),
    between("between"),
    in("in"),
    notIn("notIn"),
    Null("Null"), // null是Java关键字, 所以改成首字母大写
    notNull("notNull"),
    like("like"),
    notLike("notLike"),
    prefixLike("prefixLike"),       // 应该比like用的还少
    notPrefixLike("notPrefixLike"), // 应该比like用的还少
    suffixLike("suffixLike"),       // 应该比like用的还少
    notSuffixLike("notSuffixLike"), // 应该比like用的还少
    regex("regex"),
    notRegex("notRegex");

    private String value;

    Condition(String eq) {
        this.value = eq;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static Condition getEnum(String value) {
        for (Condition operate:values()) {
            if (Objects.equals(operate.getValue(), value)) {
                return operate;
            }
        }
        return null;
    }
}
