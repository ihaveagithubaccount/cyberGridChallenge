package cyber.grid.cyberGridChallenge.controller;

import cyber.grid.cyberGridChallenge.dto.ProductDTO;
import cyber.grid.cyberGridChallenge.dto.ProductCreateDTO;
import cyber.grid.cyberGridChallenge.dto.ProductUpdateDTO;
import cyber.grid.cyberGridChallenge.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product", description = "Create a new product with the provided details")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductCreateDTO productCreateDTO) {
        return ResponseEntity.ok(productService.createProduct(productCreateDTO));
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all active products with pagination and sorting")
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "ASC") String direction
    ) {
        // Note: Using explicit @RequestParam instead of @ParameterObject Pageable due to Swagger UI issues
        // Standard approach would be: @ParameterObject Pageable pageable (no manual Pageable creation needed)
        Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    } 
  
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    public ResponseEntity<ProductDTO> getProductById(@Parameter(description = "Product ID") @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/expensive")
    @Operation(summary = "Get expensive products", description = "Retrieve products with price above the specified minimum")
    public ResponseEntity<List<ProductDTO>> getExpensiveProducts(
            @Parameter(description = "Minimum price threshold", example = "100.0") @RequestParam Double minPrice) {
        return ResponseEntity.ok(productService.getExpensiveProducts(minPrice));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product", description = "Update an existing product with the provided details")
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id, 
            @Valid @RequestBody ProductUpdateDTO productUpdateDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productUpdateDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product", description = "Soft delete a product by setting its status to DELETED")
    public ResponseEntity<Void> deleteProduct(@Parameter(description = "Product ID") @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
