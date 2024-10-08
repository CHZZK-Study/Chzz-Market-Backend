package org.chzz.market.domain.image.repository;

import java.util.Set;
import org.chzz.market.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImageRepository extends JpaRepository<Image, Long> {
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Image i "
            + " WHERE i.product.id = :productId"
            + " AND i.id NOT IN :ids")
    void deleteImagesNotContainsIdsOf(@Param("productId") Long productId,@Param("ids") Set<Long> ids);

    long countByProductId(Long productId);
}
