package com.condingfly.sql;

import java.io.Serializable;
import java.util.Objects;

/**
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
    in("ne"),
    notIn("ne"),
    Null("Null"),
    notNull("null"),
    like("like"),
    notLike("notLike"),
    prefixLike("prefixLike"),
    notPrefixLike("notPrefixLike"),
    suffixLike("suffixLike"),
    notSuffixLike("notSuffixLike"),
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
