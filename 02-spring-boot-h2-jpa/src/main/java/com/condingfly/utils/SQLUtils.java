package com.condingfly.utils;

import com.condingfly.sql.Condition;
import com.condingfly.sql.Predicate;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hgp
 * @date 20-8-5
 */
public class SQLUtils {
    public static String insertSql(String tableName, List<String> fields) {
        Assert.isTrue(StringUtils.hasText(tableName), "表名不允许为空");
        Assert.isTrue(ObjectUtils.isEmpty(fields)==false, "字段不允许为空");
        StringBuffer sb = new StringBuffer("INSERT INTO "+tableName+"(");
        StringBuffer value = new StringBuffer("VALUES(");
        int i = 0;
        for (i = 0; i < fields.size()-1; i++) {
            sb.append(fields.get(i)+",");
            value.append("?,");
        }
        sb.append(fields.get(i)+") ");
        value.append("?);");
        return sb.toString() + value.toString();
    }

    public static String updateSql(String tableName, List<String> fields) {
        return updateSql(tableName, fields, null, null);
    }

    public static String updateSql(String tableName, List<String> fields, List<Predicate> predicates, Map<String,Object> paramMap) {
        Assert.isTrue(StringUtils.hasText(tableName), "表名不允许为空");
        Assert.isTrue(ObjectUtils.isEmpty(fields)==false, "字段不允许为空");
        StringBuffer sb = new StringBuffer("UPDATE "+tableName+" SET ");
        int i =0;
        for (i = 0; i < fields.size()-1; i++) {
            sb.append(fields.get(i)+"=?,");
        }
        sb.append(fields.get(i)+"=? ");
        if (!ObjectUtils.isEmpty(predicates) && !ObjectUtils.isEmpty(paramMap)) {
            List<Predicate> newPredicates = new ArrayList();
            predicates = predicates.stream().filter(item->ObjectUtils.isEmpty(paramMap.get(item.getField()))==false).collect(Collectors.toList());

        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String tableName = "sys_user";
        List<String> fields = Arrays.asList("id","username","nickname");
        System.out.println(updateSql(tableName, fields));

        tableName = "sys_user";
        fields = Arrays.asList("id");
        System.out.println(updateSql(tableName, fields));
    }
}
