package cyber.grid.cyberGridChallenge.mapper;

import cyber.grid.cyberGridChallenge.dto.ProductDTO;
import cyber.grid.cyberGridChallenge.dto.ProductCreateDTO;
import cyber.grid.cyberGridChallenge.dto.ProductUpdateDTO;
import cyber.grid.cyberGridChallenge.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {
    
    ProductDTO toDTO(Product product);
    
    @org.mapstruct.Mapping(target = "status", defaultExpression = "java(cyber.grid.cyberGridChallenge.entity.ProductStatus.ACTIVE)")
    Product toEntity(ProductDTO productDTO);

    @org.mapstruct.Mapping(target = "status", defaultExpression = "java(cyber.grid.cyberGridChallenge.entity.ProductStatus.ACTIVE)")
    Product toEntity(ProductCreateDTO productCreateDTO);
    
    void updateFromDto(ProductUpdateDTO dto, @MappingTarget Product entity);
}
