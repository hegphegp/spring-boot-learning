package com.condingfly.entity;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "sys_user",
       indexes = {@Index(name="sys_user_username_index", columnList = "username", unique=false),
                  @Index(name="sys_user_del_index", columnList = "del", unique=false),
                  @Index(name="sys_user_create_at_index", columnList = "createAt", unique=false),
                  @Index(name="sys_user_update_at_index", columnList = "updateAt", unique=false)})
public class UserEntity {
    @Id
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @GeneratedValue(generator = "system-uuid")
    @Column(length = 32)
    private String id;
    private String username;
    private String nickname;
    private String phone;
    private Timestamp createAt;
    private Timestamp updateAt;
    private Boolean del;
    public UserEntity() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public Timestamp getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Timestamp updateAt) {
        this.updateAt = updateAt;
    }

    public Boolean getDel() {
        return del;
    }

    public void setDel(Boolean del) {
        this.del = del;
    }
}
