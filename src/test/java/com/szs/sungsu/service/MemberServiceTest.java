package com.szs.sungsu.service;

import com.szs.sungsu.config.jwt.JwtTokenProvider;
import com.szs.sungsu.domain.Member;
import com.szs.sungsu.exception.MemberException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
public class MemberServiceTest {

    @Autowired private EntityManager em;
    @Autowired private MemberService memberService;
    @Autowired private JwtTokenProvider tokenProvider;


    @Test
    public void 회원가입_정상케이스() {
        // given
        String userId = "hong";
        String password = "1234";
        String name = "홍길동";
        String regNo = "860824-1655068";

        // when
        Long memberId = memberService.joinMember(userId, password, name, regNo);
        Member member = em.find(Member.class, memberId);

        // then
        assertThat(member.getUserId()).isEqualTo(userId);
        assertThat(member.getName()).isEqualTo(name);
    }

    // 회원가입-있는 아이디
    @Test
    public void 회원가입_중복아이디() {
        // given
        Member memberA = new Member("hong", "1234", "홍길동", "860824-1655068");
        em.persist(memberA);

        String userId = "hong";
        String password = "1234";
        String name = "홍길동";
        String regNo = "860824-1655068";

        // then
        assertThatThrownBy(() -> memberService.joinMember(userId, password, name, regNo))
                .isInstanceOf(MemberException.class);
    }

    // 로그인+토큰검증
    @Test
    public void 로그인_토큰검증() {
        // given
        String userId = "hong";
        String password = "1234";
        String name = "홍길동";
        String regNo = "860824-1655068";

        // when
        memberService.joinMember(userId, password, name, regNo);
        String token = memberService.jwtLogin(userId, password);

        // then
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    // 로그인-잘못된비번
    @Test
    public void 로그인_잘못된비번() {
        // given
        String userId = "hong";
        String password = "1234";
        String name = "홍길동";
        String regNo = "860824-1655068";

        String wrongPassword = "aaaa";

        // when
        memberService.joinMember(userId, password, name, regNo);
        assertThatThrownBy(() -> memberService.jwtLogin(userId, wrongPassword))
                .isInstanceOf(MemberException.class);
    }

    // 로그인-없는아이디
    @Test
    public void 로그인_없는아이디() {
        // given
        String userId = "hong";
        String password = "1234";
        String name = "홍길동";
        String regNo = "860824-1655068";

        String wrongUserId = "aaaa";

        // when
        memberService.joinMember(userId, password, name, regNo);
        assertThatThrownBy(() -> memberService.jwtLogin(wrongUserId, password))
                .isInstanceOf(MemberException.class);
    }


}
