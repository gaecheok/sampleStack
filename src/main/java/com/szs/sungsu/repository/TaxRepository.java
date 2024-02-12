package com.szs.sungsu.repository;

import com.szs.sungsu.domain.Member;
import com.szs.sungsu.domain.Tax;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaxRepository extends CrudRepository<Tax, Long> {

    Optional<Tax> findFirstByMemberEquals(Member member);
}
