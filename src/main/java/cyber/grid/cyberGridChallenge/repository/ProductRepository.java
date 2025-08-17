package cyber.grid.cyberGridChallenge.repository;

import cyber.grid.cyberGridChallenge.entity.Product;
import cyber.grid.cyberGridChallenge.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds products with price above minimum threshold, excluding deleted products.
     */
    @Query(value = "SELECT * FROM products WHERE price > :minPrice AND status != :deletedStatus", nativeQuery = true)
    List<Product> findExpensiveProducts(@Param("minPrice") BigDecimal minPrice, @Param("deletedStatus") String deletedStatus);

    /**
     * Finds all active products with pagination and sorting, excluding deleted products.
     */
    @Query(value = "SELECT * FROM products WHERE status != :deletedStatus", nativeQuery = true)
    Page<Product> findAllActive(@Param("deletedStatus") String deletedStatus, Pageable pageable);

    /**
     * Finds a product by ID that doesn't have the specified status.
     */
    Optional<Product> findByIdAndStatusNot(Long id, ProductStatus status);
}
