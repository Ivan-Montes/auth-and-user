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

import dev.ime.application.dto.CategoryDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.domain.port.outbound.PublisherPort;

@ExtendWith(MockitoExtension.class)
class CategoryServiceAdapterTest {

	@Mock
	private GenericRepositoryPort<Category> categoryRepositoryAdapter;
	@Mock
	private CategoryMapper categoryMapper;
	@Mock
	private EventMapper eventMapper;
	@Mock
	private PublisherPort publisherAdapter;

	@InjectMocks
	private CategoryServiceAdapter categoryServiceAdapter;

	private CategoryDto categoryDto;
	private Category category;
	private Event event;
	
	private final Long categoryId = 1L;
	private final String categoryName = "Vegetables";
	
	private UUID eventId;
	private final String eventCategory = GlobalConstants.CAT_CAT;
	private final String eventType = GlobalConstants.CAT_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();

	private final Pageable pageable = PageRequest.of(0, 10, Sort.by(GlobalConstants.CAT_ID).ascending());
		
	@BeforeEach
	private void setUp() {

		categoryDto = new CategoryDto(categoryId,categoryName);
		category = new Category();
		category.setCategoryId(categoryId);
		category.setCategoryName(categoryName);
		
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
		
		Mockito.when(categoryRepositoryAdapter.findAll()).thenReturn(List.of(category));
		Mockito.when(categoryMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(categoryDto));
		
		List<CategoryDto> list = categoryServiceAdapter.findAll();
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void findAll_withPageable_shouldReturnList() {
		
		Mockito.when(categoryRepositoryAdapter.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(category));
		Mockito.when(categoryMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(categoryDto));
		
		List<CategoryDto> list = categoryServiceAdapter.findAll(pageable);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void findById_shouldReturnOptDto() {
		
		Mockito.when(categoryRepositoryAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(category));
		Mockito.when(categoryMapper.fromDomainToDto(Mockito.any(Category.class))).thenReturn(categoryDto);
		
		Optional<CategoryDto> optResult =  categoryServiceAdapter.findById(categoryId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().categoryId()).isEqualTo(categoryId)
		);
	}

	@Test
	void save_shouldReturnOptDto() {
		
		Mockito.when(categoryMapper.fromDtoToDomain(Mockito.any(CategoryDto.class))).thenReturn(category);
		Mockito.when(categoryRepositoryAdapter.save(Mockito.any(Category.class))).thenReturn(Optional.of(category));
		Mockito.when(categoryMapper.fromDomainToDto(Mockito.any(Category.class))).thenReturn(categoryDto);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(CategoryDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));

		Optional<CategoryDto> optResult =  categoryServiceAdapter.save(categoryDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().categoryId()).isEqualTo(categoryId)
		);
	}

	@Test
	void update_shouldReturnOptDto() {
		
		Mockito.when(categoryMapper.fromDtoToDomain(Mockito.any(CategoryDto.class))).thenReturn(category);
		Mockito.when(categoryRepositoryAdapter.update(Mockito.any(Category.class))).thenReturn(Optional.of(category));
		Mockito.when(categoryMapper.fromDomainToDto(Mockito.any(Category.class))).thenReturn(categoryDto);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(CategoryDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));

		Optional<CategoryDto> optResult =  categoryServiceAdapter.update(categoryDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().categoryId()).isEqualTo(categoryId)
		);
	}

	@Test
	void deleteById_shouldReturnTrue() {
		
		Mockito.when(categoryRepositoryAdapter.deleteById(Mockito.anyLong())).thenReturn(true);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(CategoryDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));
		
		boolean result = categoryServiceAdapter.deleteById(categoryId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
		);		
	}
	
}
