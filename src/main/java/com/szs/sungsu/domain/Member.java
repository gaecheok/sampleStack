package com.szs.sungsu.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@Entity
@Getter
@ToString(of = {"id", "userId", "password", "name", "regNo"})
public class Member extends BaseTimeEntity {

    protected Member() {}
    public Member(String userId, String password, String name, String regNo) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.regNo = regNo;
    }

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "member_id")
    private Long id;

    private String userId;
    private String password;
    private String name;
    private String regNo;

}
