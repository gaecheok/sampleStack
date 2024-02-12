package com.szs.sungsu.service;

import com.szs.sungsu.config.JwtTokenProvider;
import com.szs.sungsu.domain.Member;
import com.szs.sungsu.repository.MemberRepository;
import com.szs.sungsu.util.TwoWayEncryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberService {
    
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final MemberRepository repository;

    @Transactional
    public Long joinMember(String userId, String password, String name, String regNo) {

        // 아이디 중복 확인
        validateUserId(userId);

        // 주민번호 양방향 암호화
        String encodedRegNo;
        try {
            encodedRegNo = TwoWayEncryptUtil.encrypt(regNo);
        } catch (GeneralSecurityException e) {
            // TODO 맞는 익셉션으로 교체
            throw new IllegalStateException(e);
        }

        // 비번 단방향 해시 암호화
        String encodedPassword = passwordEncoder.encode(password);

        Member member = new Member(userId, encodedPassword, name, encodedRegNo);
        log.info(member.toString());
        repository.save(member);
        return member.getId();
    }

    public String jwtLogin(String userId, String password) {
        // 정보가져오기
        Optional<Member> findMemberOptional = repository.findFirstByUserIdEquals(userId);
        if (findMemberOptional.isEmpty()) {
            throw new IllegalStateException("입력한 정보가 일치하지 않습니다.");
        }

        // 비번 체크
        if (!passwordEncoder.matches(password, findMemberOptional.get().getPassword())) {
            throw new IllegalStateException("입력한 정보가 일치하지 않습니다.");
        }

        // jwt 생성
        return tokenProvider.generateToken(userId);
    }

    private void validateUserId(String userId) {
        Optional<Member> findMemberOptional = repository.findFirstByUserIdEquals(userId);
        if (findMemberOptional.isPresent()) {
            // 중복
            throw new IllegalStateException("이미 존재하는 아이디 입니다");
        }
    }
}
