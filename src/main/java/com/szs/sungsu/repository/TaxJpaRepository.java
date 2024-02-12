package com.szs.sungsu.repository;

import com.szs.sungsu.domain.Tax;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TaxJpaRepository {

    private final EntityManager em;

    public Tax findTaxByUserId(String userId) {
        return em.createQuery("select t from Tax t " +
                " join fetch t.member m" +
                " where m.userId = :userId", Tax.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }
}
