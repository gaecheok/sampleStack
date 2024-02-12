package com.szs.sungsu.repository;

import com.szs.sungsu.domain.Member;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends CrudRepository<Member, Long> {
    Optional<Member> findFirstByUserIdEquals(String userId);

}
