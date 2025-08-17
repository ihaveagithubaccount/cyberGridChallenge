package cyber.grid.cyberGridChallenge.service;

import cyber.grid.cyberGridChallenge.dto.ProductDTO;
import cyber.grid.cyberGridChallenge.dto.ProductCreateDTO;
import cyber.grid.cyberGridChallenge.dto.ProductUpdateDTO;
import cyber.grid.cyberGridChallenge.entity.Product;
import cyber.grid.cyberGridChallenge.entity.ProductStatus;
import cyber.grid.cyberGridChallenge.exception.ProductNotFoundException;
import cyber.grid.cyberGridChallenge.mapper.ProductMapper;
import cyber.grid.cyberGridChallenge.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;
    private ProductCreateDTO testProductCreateDTO;
    private ProductUpdateDTO testProductUpdateDTO;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100.00))
                .status(ProductStatus.ACTIVE)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        testProductDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(100.00)
                .status(ProductStatus.ACTIVE)
                .build();

        testProductCreateDTO = ProductCreateDTO.builder()
                .name("Test Product")
                .description("Test Description")
                .price(100.00)
                .build();

        testProductUpdateDTO = ProductUpdateDTO.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(150.00)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void createProduct_Success() {
        when(productMapper.toEntity(testProductCreateDTO)).thenReturn(testProduct);
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        ProductDTO result = productService.createProduct(testProductCreateDTO);

        assertNotNull(result);
        assertEquals(testProductDTO.getName(), result.getName());
        verify(productMapper).toEntity(testProductCreateDTO);
        verify(productRepository).save(testProduct);
        verify(productMapper).toDTO(testProduct);
    }

    @Test
    void getAllProducts_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct));
        when(productRepository.findAllActive(ProductStatus.DELETED.name(), pageable)).thenReturn(productPage);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        Page<ProductDTO> result = productService.getAllProducts(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testProductDTO.getName(), result.getContent().get(0).getName());
        verify(productRepository).findAllActive(ProductStatus.DELETED.name(), pageable);
    }

    @Test
    void getProductById_Success() {
        when(productRepository.findByIdAndStatusNot(1L, ProductStatus.DELETED))
                .thenReturn(Optional.of(testProduct));
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(testProductDTO.getName(), result.getName());
        verify(productRepository).findByIdAndStatusNot(1L, ProductStatus.DELETED);
        verify(productMapper).toDTO(testProduct);
    }

    @Test
    void getProductById_ProductNotFound_ThrowsException() {
        when(productRepository.findByIdAndStatusNot(1L, ProductStatus.DELETED))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(1L));
        verify(productRepository).findByIdAndStatusNot(1L, ProductStatus.DELETED);
        verify(productMapper, never()).toDTO(any());
    }

    @Test
    void updateProduct_Success() {
        when(productRepository.findByIdAndStatusNot(1L, ProductStatus.DELETED))
                .thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        ProductDTO result = productService.updateProduct(1L, testProductUpdateDTO);

        assertNotNull(result);
        verify(productRepository).findByIdAndStatusNot(1L, ProductStatus.DELETED);
        verify(productMapper).updateFromDto(testProductUpdateDTO, testProduct);
        verify(productRepository).save(testProduct);
        verify(productMapper).toDTO(testProduct);
    }

    @Test
    void updateProduct_ProductNotFound_ThrowsException() {
        when(productRepository.findByIdAndStatusNot(1L, ProductStatus.DELETED))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(1L, testProductUpdateDTO));
        verify(productRepository).findByIdAndStatusNot(1L, ProductStatus.DELETED);
        verify(productMapper, never()).updateFromDto(any(), any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_Success() {
        when(productRepository.findByIdAndStatusNot(1L, ProductStatus.DELETED)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);

        productService.deleteProduct(1L);

        assertEquals(ProductStatus.DELETED, testProduct.getStatus());
        verify(productRepository).findByIdAndStatusNot(1L, ProductStatus.DELETED);
        verify(productRepository).save(testProduct);
    }

    @Test
    void deleteProduct_ProductNotFound_ThrowsException() {
        when(productRepository.findByIdAndStatusNot(1L, ProductStatus.DELETED)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(1L));
        verify(productRepository).findByIdAndStatusNot(1L, ProductStatus.DELETED);
        verify(productRepository, never()).save(any());
    }

    @Test
    void getExpensiveProducts_Success() {
        Double minPrice = 50.0;
        when(productRepository.findExpensiveProducts(BigDecimal.valueOf(minPrice), ProductStatus.DELETED.name()))
                .thenReturn(List.of(testProduct));
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        List<ProductDTO> result = productService.getExpensiveProducts(minPrice);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProductDTO.getName(), result.get(0).getName());
        verify(productRepository).findExpensiveProducts(BigDecimal.valueOf(minPrice), ProductStatus.DELETED.name());
    }
}