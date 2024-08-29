package reserve.store.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reserve.store.domain.Store;
import reserve.store.dto.response.StoreInfoResponse;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    boolean existsByIdAndUserId(Long storeId, Long userId);

    Optional<Store> findByIdAndUserId(Long storeId, Long userId);

    @Query("""
           SELECT new reserve.store.dto.response.StoreInfoResponse(
               store.id, store.user.username, store.name, store.address, store.description
           )
           FROM Store store
           WHERE store.id = :storeId
           """)
    Optional<StoreInfoResponse> findResponseById(@Param("storeId") Long storeId);

    @Override
    @Modifying
    @Query("UPDATE Store store SET store.status = 'DELETED' WHERE store.id = :storeId")
    void deleteById(@Param("storeId") Long storeId);

}
