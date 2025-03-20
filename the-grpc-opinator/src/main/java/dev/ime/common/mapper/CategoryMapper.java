package dev.ime.common.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.ime.application.dto.CategoryDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Product;
import dev.ime.infrastructure.entity.CategoryJpaEntity;
import dev.ime.infrastructure.entity.ProductJpaEntity;
import dev.proto.CategoryProto;
import dev.proto.CreateCategoryRequest;
import dev.proto.UpdateCategoryRequest;

@Component
public class CategoryMapper {

	private final ObjectMapper objectMapper;
	
	public CategoryMapper(ObjectMapper objectMapper) {
		super();
		this.objectMapper = objectMapper;
	}

	public CategoryDto fromCreateToDto(CreateCategoryRequest request) {
	
		return new CategoryDto(
				null,
				request.getCategoryName()
				);		
	}	

	public CategoryDto fromUpdateToDto(UpdateCategoryRequest request) {
		
		return new CategoryDto(
				request.getCategoryId(),
				request.getCategoryName()
				);		
	}

	public Category fromDtoToDomain(CategoryDto dto) {
		
		return new Category(
				dto.categoryId(),
				dto.categoryName(),
				new HashSet<>()
				); 			
	}
	
	public CategoryJpaEntity fromDomainToJpa(Category domain) {
		
		return new CategoryJpaEntity(
				domain.getCategoryId(),
				domain.getCategoryName(),
				new HashSet<>()				
				);			
	}

	public Category fromJpaToDomain(CategoryJpaEntity entity) {

		Category category =  new Category(
				entity.getCategoryId(),
				entity.getCategoryName(),
				null
				); 	
		
		Set<ProductJpaEntity> productsJpa = entity.getProducts();
		Set<Product> products = fromSetProductJpaToSetProductDomain(productsJpa, category);
		category.setProducts(products);
		
		return category;
	}

	private Product fromProductJpaToProductDomain(ProductJpaEntity productJpa, Category category) {
				
		return new Product(
				productJpa.getProductId(),
				productJpa.getProductName(),
				productJpa.getProductDescription(),
				category,
				new HashSet<>()
				);
	}

	private Set<Product> fromSetProductJpaToSetProductDomain(Set<ProductJpaEntity> productsJpa, Category category) {
		
		if ( productsJpa == null ) {
			return new HashSet<>();
		}

		return productsJpa.stream()
				.map( p -> fromProductJpaToProductDomain(p, category))
				.collect(Collectors.toSet());	
	}

	public List<Category> fromListJpaToListDomain(List<CategoryJpaEntity> listJpa) {
		
		if ( listJpa == null ) {
			return new ArrayList<>();
		}

		return listJpa.stream()
				.map(this::fromJpaToDomain)
				.toList();	
	}
	
	public CategoryDto fromDomainToDto(Category dom) {
		
		return new CategoryDto(
				dom.getCategoryId(),
				dom.getCategoryName()
				);
		
	}
	
	public List<CategoryDto> fromListDomainToListDto(List<Category> list) {

		if ( list == null ) {
			return new ArrayList<>();
		}

		return list.stream()
				.map(this::fromDomainToDto)
				.toList();	
	}

	public CategoryProto fromDtoToProto(CategoryDto dto) {
		
		return CategoryProto.newBuilder()
				.setCategoryId(dto.categoryId())
				.setCategoryName(dto.categoryName())
				.build();
	}

	public List<CategoryProto> fromListDtoToListProto(List<CategoryDto> list) {

		if ( list == null ) {
			return new ArrayList<>();
		}

		return list.stream()
				.map(this::fromDtoToProto)
				.toList();	
	}

	public Event fromDtoToEvent(String eventType, CategoryDto dto) {		
		
		return new Event(
				GlobalConstants.CAT_CAT,
				eventType,
				createEventData(dto)
				);		
	}
	
	private Map<String, Object> createEventData(CategoryDto dto) {

		return objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {
		});

	}
	
}
