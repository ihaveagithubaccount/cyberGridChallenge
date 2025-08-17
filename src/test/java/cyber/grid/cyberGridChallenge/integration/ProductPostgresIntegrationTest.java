package cyber.grid.cyberGridChallenge.integration;

import cyber.grid.cyberGridChallenge.entity.Product;
import cyber.grid.cyberGridChallenge.entity.ProductStatus;
import cyber.grid.cyberGridChallenge.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Testcontainers
class ProductPostgresIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProductRepository productRepository;

    private MockMvc mockMvc;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        testProduct = Product.builder()
                .name("PostgreSQL Integration Test Product")
                .description("Product for PostgreSQL integration testing")
                .price(BigDecimal.valueOf(199.99))
                .status(ProductStatus.ACTIVE)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void getProductById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", testProduct.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.name").value("PostgreSQL Integration Test Product"))
                .andExpect(jsonPath("$.description").value("Product for PostgreSQL integration testing"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getProductById_NotFound() throws Exception {
        Long nonExistentId = 99999L;
        
        mockMvc.perform(get("/api/v1/products/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductById_DeletedProduct_NotFound() throws Exception {
        testProduct.setStatus(ProductStatus.DELETED);
        productRepository.save(testProduct);
        
        mockMvc.perform(get("/api/v1/products/{id}", testProduct.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(testProduct.getId()))
                .andExpect(jsonPath("$.content[0].name").value("PostgreSQL Integration Test Product"));
    }

    @Test
    void createProduct_Success() throws Exception {
        String productJson = """
            {
                "name": "New Integration Test Product",
                "description": "Product created via integration test",
                "price": 299.99
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New Integration Test Product"))
                .andExpect(jsonPath("$.description").value("Product created via integration test"))
                .andExpect(jsonPath("$.price").value(299.99))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createProduct_InvalidData_ReturnsBadRequest() throws Exception {
        String invalidProductJson = """
            {
                "name": "",
                "description": "Invalid product",
                "price": -100
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidProductJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_Success() throws Exception {
        String updateJson = """
            {
                "name": "Updated Integration Test Product",
                "description": "Updated description",
                "price": 399.99
            }
            """;

        mockMvc.perform(put("/api/v1/products/{id}", testProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Integration Test Product"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.price").value(399.99))
                .andExpect(jsonPath("$.id").value(testProduct.getId()));
    }

    @Test
    void updateProduct_NotFound_ReturnsNotFound() throws Exception {
        String updateJson = """
            {
                "name": "Updated Product",
                "description": "Updated description",
                "price": 399.99
            }
            """;

        mockMvc.perform(put("/api/v1/products/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", testProduct.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/products/{id}", testProduct.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_NotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExpensiveProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/expensive")
                .param("minPrice", "100.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testProduct.getId()))
                .andExpect(jsonPath("$[0].price").value(199.99));
    }

    @Test
    void getExpensiveProducts_NoResults_ReturnsEmptyArray() throws Exception {
        mockMvc.perform(get("/api/v1/products/expensive")
                .param("minPrice", "1000.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
} 