package reserve.menu.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reserve.menu.domain.Menu;
import reserve.menu.dto.response.MenuInfoResponse;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("""
           SELECT new reserve.menu.dto.response.MenuInfoResponse(
               menu.id, menu.store.id, menu.name, menu.price, menu.description
           )
           FROM Menu menu
           WHERE menu.id = :menuId
           """)
    Optional<MenuInfoResponse> findResponseById(@Param("menuId") Long menuId);

    @Query("""
           SELECT new reserve.menu.dto.response.MenuInfoResponse(
               menu.id, menu.store.id, menu.name, menu.price, menu.description
           )
           FROM Menu menu
           WHERE menu.store.id = :storeId
           """)
    List<MenuInfoResponse> findResponsesByStoreId(@Param("storeId") Long storeId);

    @Override
    @Modifying
    @Query("UPDATE Menu menu SET menu.status = 'DELETED' WHERE menu.id = :menuId")
    void deleteById(@Param("menuId") Long menuId);

}
