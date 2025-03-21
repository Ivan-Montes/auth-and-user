package dev.ime.application.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import dev.ime.application.dto.ProductDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.domain.port.outbound.PublisherPort;

@Service
public class ProductServiceAdapter implements GenericServicePort<ProductDto> {

	private final GenericRepositoryPort<Product> productRepositoryAdapter;
	private final ProductMapper productMapper;
	private final EventMapper eventMapper;
	private final PublisherPort publisherAdapter;
	private static final Logger logger = LoggerFactory.getLogger(ProductServiceAdapter.class);

	public ProductServiceAdapter(GenericRepositoryPort<Product> productRepositoryAdapter, ProductMapper productMapper,
			EventMapper eventMapper, PublisherPort publisherAdapter) {
		super();
		this.productRepositoryAdapter = productRepositoryAdapter;
		this.productMapper = productMapper;
		this.eventMapper = eventMapper;
		this.publisherAdapter = publisherAdapter;
	}

	@Override
	public List<ProductDto> findAll() {
		
		return productMapper.fromListDomainToListDto(productRepositoryAdapter.findAll());
	}

	@Override
	public List<ProductDto> findAll(Pageable pageable) {
		
		return productMapper.fromListDomainToListDto(productRepositoryAdapter.findAll(pageable));
	}

	@Override
	public Optional<ProductDto> findById(Long id) {

		return productRepositoryAdapter.findById(id)
				.map(productMapper::fromDomainToDto)
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.PROD_CAT, item);
		            return item;
		        });
	}

	@Override
	public Optional<ProductDto> save(ProductDto dto) {

		return  Optional.of(dto)
				.map(productMapper::fromDtoToDomain)
				.flatMap(productRepositoryAdapter::save)
				.map(productMapper::fromDomainToDto)
				.map( item -> {
					publisherAdapter.publishEvent(eventMapper.createEvent(GlobalConstants.PROD_CAT, GlobalConstants.PROD_CREATED, item));
					return item;
				})
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.PROD_CREATED, item);
		            return item;
		        });
	}

	@Override
	public Optional<ProductDto> update(ProductDto dto) {
		
		return  Optional.of(dto)
				.map(productMapper::fromDtoToDomain)
				.flatMap(productRepositoryAdapter::update)
				.map(productMapper::fromDomainToDto)
				.map( item -> {
					publisherAdapter.publishEvent(eventMapper.createEvent(GlobalConstants.PROD_CAT, GlobalConstants.PROD_UPDATED, item));
					return item;
				})
		        .map(item -> {
		            logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.PROD_UPDATED, item);
		            return item;
		        });
	}

	@Override
	public boolean deleteById(Long id) {

		return Optional.of(productRepositoryAdapter.deleteById(id))
		.map( b -> processDelete(b,id))
		.orElse(false);
	}

	private boolean processDelete(boolean result, Long id) {

		Optional.ofNullable(id)
		.map( i -> new ProductDto(id,"","",-1L))
		.map(item -> eventMapper.createEvent(GlobalConstants.PROD_CAT, GlobalConstants.PROD_DELETED, item))
		.ifPresent(publisherAdapter::publishEvent);

		return result;
	}

}
