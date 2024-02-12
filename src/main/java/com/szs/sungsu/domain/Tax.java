package com.szs.sungsu.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Getter
@ToString(of = {"id", "totalPayment", "calculatedTax", "insurance", "education", "donation", "medical", "retirement", "scrapStatus"})
public class Tax extends BaseTimeEntity {

    protected Tax(){}

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 총지급액
    private BigDecimal totalPayment;
    // 산출세액
    private BigDecimal calculatedTax;
    // 보험료
    private BigDecimal insurance;
    // 교육비
    private BigDecimal education;
    // 기부금
    private BigDecimal donation;
    // 의료비
    private BigDecimal medical;
    // 퇴직연금
    private BigDecimal retirement;

    // 스크랩 상태
    @Enumerated(EnumType.STRING)
    private ScrapStatus scrapStatus;

    public static Tax createTax(Member member) {
        Tax tax = new Tax();
        tax.totalPayment = BigDecimal.valueOf(0);
        tax.calculatedTax = BigDecimal.valueOf(0);
        tax.insurance = BigDecimal.valueOf(0);
        tax.education = BigDecimal.valueOf(0);
        tax.donation = BigDecimal.valueOf(0);
        tax.medical = BigDecimal.valueOf(0);
        tax.retirement = BigDecimal.valueOf(0);
        tax.scrapStatus = ScrapStatus.INPROGRESS;
        tax.member = member;
        return tax;
    }

    public static void scrapTax(Tax tax, BigDecimal totalPayment, BigDecimal calculatedTax,
                               BigDecimal insurance, BigDecimal education, BigDecimal donation,
                               BigDecimal medical, BigDecimal retirement) {
        tax.totalPayment = totalPayment;
        tax.calculatedTax = calculatedTax;
        tax.insurance = insurance;
        tax.education = education;
        tax.donation = donation;
        tax.medical = medical;
        tax.retirement = retirement;
        tax.scrapStatus = ScrapStatus.DONE;
    }
}
