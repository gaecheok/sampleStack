package com.szs.sungsu.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

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

    @Length(max = 20)
    private String userId;
    private String password;
    @Length(max = 20)
    private String name;
    private String regNo;

}
