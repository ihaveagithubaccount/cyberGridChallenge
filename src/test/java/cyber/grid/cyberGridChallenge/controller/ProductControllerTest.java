package cyber.grid.cyberGridChallenge.controller;

import cyber.grid.cyberGridChallenge.dto.ProductDTO;
import cyber.grid.cyberGridChallenge.dto.ProductCreateDTO;
import cyber.grid.cyberGridChallenge.dto.ProductUpdateDTO;
import cyber.grid.cyberGridChallenge.entity.ProductStatus;
import cyber.grid.cyberGridChallenge.exception.ProductNotFoundException;
import cyber.grid.cyberGridChallenge.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private ProductDTO testProductDTO;
    private ProductCreateDTO testProductCreateDTO;
    private ProductUpdateDTO testProductUpdateDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new cyber.grid.cyberGridChallenge.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

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
    void createProduct_Success() throws Exception {
        when(productService.createProduct(any(ProductCreateDTO.class))).thenReturn(testProductDTO);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(100.0));

        verify(productService).createProduct(any(ProductCreateDTO.class));
    }

    @Test
    void createProduct_ValidationError() throws Exception {
        ProductCreateDTO invalidProduct = ProductCreateDTO.builder()
                .name("")
                .price(-10.0)
                .build();

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any());
    }

    @Test
    void getAllProducts_Success() throws Exception {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Page<ProductDTO> productPage = new PageImpl<>(List.of(testProductDTO), pageable, 1);
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(productPage);

        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(productService).getAllProducts(any(Pageable.class));
    }

    @Test
    void getProductById_Success() throws Exception {
        when(productService.getProductById(1L)).thenReturn(testProductDTO);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(100.0));

        verify(productService).getProductById(1L);
    }

    @Test
    void getProductById_NotFound() throws Exception {
        when(productService.getProductById(1L)).thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isNotFound());

        verify(productService).getProductById(1L);
    }

    @Test
    void getExpensiveProducts_Success() throws Exception {
        when(productService.getExpensiveProducts(50.0)).thenReturn(List.of(testProductDTO));

        mockMvc.perform(get("/api/v1/products/expensive")
                .param("minPrice", "50.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService).getExpensiveProducts(50.0);
    }

    @Test
    void updateProduct_Success() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductUpdateDTO.class))).thenReturn(testProductDTO);

        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService).updateProduct(eq(1L), any(ProductUpdateDTO.class));
    }

    @Test
    void updateProduct_NotFound() throws Exception {
        when(productService.updateProduct(eq(1L), any(ProductUpdateDTO.class)))
                .thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductUpdateDTO)))
                .andExpect(status().isNotFound());

        verify(productService).updateProduct(eq(1L), any(ProductUpdateDTO.class));
    }

    @Test
    void updateProduct_ValidationError_BlankName() throws Exception {
        ProductUpdateDTO invalidUpdate = ProductUpdateDTO.builder()
                .name("")
                .description("Valid description")
                .price(100.0)
                .status(ProductStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    void updateProduct_ValidationError_NegativePrice() throws Exception {
        ProductUpdateDTO invalidUpdate = ProductUpdateDTO.builder()
                .name("Valid Name")
                .description("Valid description")
                .price(-10.0)
                .status(ProductStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    void updateProduct_ValidationError_MissingRequiredFields() throws Exception {
        ProductUpdateDTO invalidUpdate = ProductUpdateDTO.builder()
                .description("Only description provided")
                .status(ProductStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).updateProduct(any(), any());
    }

    @Test
    void deleteProduct_Success() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isOk());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void deleteProduct_NotFound() throws Exception {
        doThrow(new ProductNotFoundException(1L)).when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNotFound());

        verify(productService).deleteProduct(1L);
    }
}
