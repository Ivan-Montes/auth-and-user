package dev.ime.application.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import dev.ime.application.dto.ProductDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.domain.port.outbound.PublisherPort;

@ExtendWith(MockitoExtension.class)
class ProductServiceAdapterTest {

	@Mock
	private GenericRepositoryPort<Product> productRepositoryAdapter;
	@Mock
	private ProductMapper productMapper;
	@Mock
	private EventMapper eventMapper;
	@Mock
	private PublisherPort publisherAdapter;

	@InjectMocks
	private ProductServiceAdapter productServiceAdapter;

	private ProductDto productDto;
	private Product product;
	private Event event;

	private final Long productId = 1L;
	private final String productName = "Tomatoes";
	private final String productDescription = "full of red";
	private final Long categoryId = 1L;

	private UUID eventId;
	private final String eventCategory = GlobalConstants.CAT_CAT;
	private final String eventType = GlobalConstants.CAT_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();

	private final Pageable pageable = PageRequest.of(0, 10, Sort.by(GlobalConstants.PROD_ID).ascending());
		
	@BeforeEach
	private void setUp() {
		
		productDto = new ProductDto(productId,productName,productDescription,categoryId);
		product = new Product();
		product.setProductId(productId);
		product.setProductName(productName);
		product.setProductDescription(productDescription);
		product.setCategory(new Category());
		
		eventId = UUID.randomUUID();
		
		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);			
	}

	@Test
	void findAll_shouldReturnList() {
		
		Mockito.when(productRepositoryAdapter.findAll()).thenReturn(List.of(product));
		Mockito.when(productMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(productDto));
		
		List<ProductDto> list = productServiceAdapter.findAll();
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void findAll_withPageable_shouldReturnList() {
		
		Mockito.when(productRepositoryAdapter.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(product));
		Mockito.when(productMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(productDto));
		
		List<ProductDto> list = productServiceAdapter.findAll(pageable);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void findById_shouldReturnOptDto() {
		
		Mockito.when(productRepositoryAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(product));
		Mockito.when(productMapper.fromDomainToDto(Mockito.any(Product.class))).thenReturn(productDto);
		
		Optional<ProductDto> optResult =  productServiceAdapter.findById(categoryId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().categoryId()).isEqualTo(categoryId)
		);
	}

	@Test
	void save_shouldReturnOptDto() {
		
		Mockito.when(productMapper.fromDtoToDomain(Mockito.any(ProductDto.class))).thenReturn(product);
		Mockito.when(productRepositoryAdapter.save(Mockito.any(Product.class))).thenReturn(Optional.of(product));
		Mockito.when(productMapper.fromDomainToDto(Mockito.any(Product.class))).thenReturn(productDto);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(ProductDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));

		Optional<ProductDto> optResult =  productServiceAdapter.save(productDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().categoryId()).isEqualTo(categoryId)
		);
	}

	@Test
	void update_shouldReturnOptDto() {
		
		Mockito.when(productMapper.fromDtoToDomain(Mockito.any(ProductDto.class))).thenReturn(product);
		Mockito.when(productRepositoryAdapter.update(Mockito.any(Product.class))).thenReturn(Optional.of(product));
		Mockito.when(productMapper.fromDomainToDto(Mockito.any(Product.class))).thenReturn(productDto);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(ProductDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));

		Optional<ProductDto> optResult =  productServiceAdapter.update(productDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().categoryId()).isEqualTo(categoryId)
		);
	}

	@Test
	void deleteById_shouldReturnTrue() {
		
		Mockito.when(productRepositoryAdapter.deleteById(Mockito.anyLong())).thenReturn(true);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(ProductDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));
		
		boolean result = productServiceAdapter.deleteById(categoryId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
		);		
	}

}
