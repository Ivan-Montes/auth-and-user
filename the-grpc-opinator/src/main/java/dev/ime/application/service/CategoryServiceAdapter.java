package dev.ime.application.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.CategoryDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.common.mapper.EventMapper;
import dev.ime.domain.model.Category;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.domain.port.outbound.PublisherPort;

@Service
public class CategoryServiceAdapter implements GenericServicePort<CategoryDto>{

	private final GenericRepositoryPort<Category> categoryRepositoryAdapter;
	private final CategoryMapper categoryMapper;
	private final EventMapper eventMapper;
	private final PublisherPort publisherAdapter;
	private static final Logger logger = LoggerFactory.getLogger(CategoryServiceAdapter.class);

	public CategoryServiceAdapter(GenericRepositoryPort<Category> categoryRepositoryAdapter,
			CategoryMapper categoryMapper, EventMapper eventMapper, PublisherPort publisherAdapter) {
		super();
		this.categoryRepositoryAdapter = categoryRepositoryAdapter;
		this.categoryMapper = categoryMapper;
		this.eventMapper = eventMapper;
		this.publisherAdapter = publisherAdapter;
	}

	@Override
	public List<CategoryDto> findAll() {
		
		return categoryMapper.fromListDomainToListDto(categoryRepositoryAdapter.findAll());
	}

	@Override
	public List<CategoryDto> findAll(Pageable pageable) {
		
		return categoryMapper.fromListDomainToListDto(categoryRepositoryAdapter.findAll(pageable));
	}

	@Override
	public Optional<CategoryDto> findById(Long id) {
		
		return categoryRepositoryAdapter.findById(id)
				.map(categoryMapper::fromDomainToDto)
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CAT_CAT, item);
		            return item;
		        });
	}

	@Override
	public Optional<CategoryDto> save(CategoryDto dto) {

		return  Optional.of(dto)
				.map(categoryMapper::fromDtoToDomain)
				.flatMap(categoryRepositoryAdapter::save)
				.map(categoryMapper::fromDomainToDto)
				.map( item -> {
					publisherAdapter.publishEvent(eventMapper.createEvent(GlobalConstants.CAT_CAT, GlobalConstants.CAT_CREATED, item));
					return item;
				})
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CAT_CREATED, item);
		            return item;
		        });
	}

	@Override
	public Optional<CategoryDto> update(CategoryDto dto) {

		return  Optional.of(dto)
				.map(categoryMapper::fromDtoToDomain)
				.flatMap(categoryRepositoryAdapter::update)
				.map(categoryMapper::fromDomainToDto)
				.map( itemUpdated -> {
					publisherAdapter.publishEvent(eventMapper.createEvent(GlobalConstants.CAT_CAT, GlobalConstants.CAT_UPDATED, itemUpdated));
					return itemUpdated;
				})
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CAT_UPDATED, item);
		            return item;
		        });
	}

	@Override
	public boolean deleteById(Long id) {
		
		return Optional.of(categoryRepositoryAdapter.deleteById(id))
				.map( b -> processDelete(b,id))
				.orElse(false);
	}
	
	private boolean processDelete(boolean result, Long id) {
		
		Optional.ofNullable(id)
		.map( i -> new CategoryDto(id,""))
		.map(item -> eventMapper.createEvent(GlobalConstants.CAT_CAT, GlobalConstants.CAT_DELETED, item))
		.ifPresent(publisherAdapter::publishEvent);
		
		return result;
	}
	
}
