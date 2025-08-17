package cyber.grid.cyberGridChallenge.service;

import cyber.grid.cyberGridChallenge.dto.ProductDTO;
import cyber.grid.cyberGridChallenge.dto.ProductCreateDTO;
import cyber.grid.cyberGridChallenge.dto.ProductUpdateDTO;
import cyber.grid.cyberGridChallenge.entity.Product;
import cyber.grid.cyberGridChallenge.mapper.ProductMapper;
import cyber.grid.cyberGridChallenge.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import cyber.grid.cyberGridChallenge.exception.ProductNotFoundException;
import cyber.grid.cyberGridChallenge.entity.ProductStatus;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Creates a new product with the provided details.
     */
    public ProductDTO createProduct(ProductCreateDTO productCreateDTO) {
        log.info("Creating new product: {}", productCreateDTO.getName());
        Product product = productMapper.toEntity(productCreateDTO);
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        return productMapper.toDTO(savedProduct);
    }

    /**
     * Retrieves all active products with pagination and sorting.
     */
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAllActive(ProductStatus.DELETED.name(), pageable);
        return products.map(productMapper::toDTO);
    }

    /**
     * Retrieves a product by its ID, excluding deleted products.
     */
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product with id: {}", id);
        Product product = productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        log.debug("Product found: {}", product.getName());
        return productMapper.toDTO(product);
    }

    /**
     * Updates an existing product with new details.
     */
    @CacheEvict(value = "expensiveProducts", allEntries = true)
    public ProductDTO updateProduct(Long id, ProductUpdateDTO productUpdateDTO) {
        Product existingProduct = productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productMapper.updateFromDto(productUpdateDTO, existingProduct);

        Product updatedProduct = productRepository.save(existingProduct);

        return productMapper.toDTO(updatedProduct);
    }

    /**
     * Soft deletes a product by setting its status to DELETED.
     */
    @CacheEvict(value = "expensiveProducts", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Soft deleting product with id: {}", id);
        Product product = productRepository.findByIdAndStatusNot(id, ProductStatus.DELETED)
                .orElseThrow(() -> new ProductNotFoundException(id));
        
        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
        log.info("Product soft deleted successfully");
    }

    /**
     * Retrieves products with price above the specified minimum.
     */
    @Cacheable(value = "expensiveProducts", key = "#minPrice")
    public List<ProductDTO> getExpensiveProducts(Double minPrice) {
        List<Product> products = productRepository.findExpensiveProducts(BigDecimal.valueOf(minPrice), ProductStatus.DELETED.name());
        return products.stream()
                .map(productMapper::toDTO)
                .toList();
    }
}
