package com.phonebid.app.member.repository;

import com.phonebid.app.member.domain.SellerDocument;
import com.phonebid.app.member.domain.Seller;
import com.phonebid.app.member.domain.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 판매자 문서 Repository
 * 판매자 문서 데이터 접근을 위한 인터페이스
 */
@Repository
public interface SellerDocumentRepository extends JpaRepository<SellerDocument, UUID> {

    /**
     * 판매자별 문서 목록 조회
     */
    List<SellerDocument> findBySellerOrderByCreatedAtDesc(Seller seller);

    /**
     * 판매자와 문서 타입으로 문서 조회
     */
    Optional<SellerDocument> findBySellerAndType(Seller seller, DocumentType type);

    /**
     * 판매자와 문서 타입으로 문서 존재 여부 확인
     */
    boolean existsBySellerAndType(Seller seller, DocumentType type);

    /**
     * 판매자별 특정 타입의 문서 목록 조회
     */
    List<SellerDocument> findBySellerAndTypeOrderByCreatedAtDesc(Seller seller, DocumentType type);

    /**
     * 판매자 ID로 문서 목록 조회
     */
    @Query("SELECT sd FROM SellerDocument sd WHERE sd.seller.id = :sellerId")
    List<SellerDocument> findBySellerId(@Param("sellerId") UUID sellerId);

    /**
     * 판매자 ID와 문서 타입으로 문서 조회
     */
    @Query("SELECT sd FROM SellerDocument sd WHERE sd.seller.id = :sellerId AND sd.type = :documentType")
    Optional<SellerDocument> findBySellerIdAndType(@Param("sellerId") UUID sellerId, @Param("documentType") DocumentType documentType);
}
