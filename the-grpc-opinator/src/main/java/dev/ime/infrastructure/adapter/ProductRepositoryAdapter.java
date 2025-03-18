package dev.ime.infrastructure.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.ime.application.exception.EntityAssociatedException;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.application.exception.UniqueValueException;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.model.Product;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.infrastructure.entity.CategoryJpaEntity;
import dev.ime.infrastructure.entity.ProductJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

@Repository
public class ProductRepositoryAdapter implements GenericRepositoryPort<Product> {

	@PersistenceContext
	private final EntityManager entityManager;
	private final ProductMapper productMapper;
	
	public ProductRepositoryAdapter(EntityManager entityManager, ProductMapper productMapper) {
		super();
		this.entityManager = entityManager;
		this.productMapper = productMapper;
	}
	
	@Override
	@Transactional(value = "transactionManager", readOnly = true)
	public List<Product> findAll() {
		
		String queryString = "SELECT p FROM ProductJpaEntity p LEFT JOIN FETCH p.reviews";
		TypedQuery<ProductJpaEntity> query = entityManager.createQuery(queryString, ProductJpaEntity.class);

		return productMapper.fromListJpaToListDomain(query.getResultList());
	}

	@Override
	@Transactional(value = "transactionManager", readOnly = true)
	public List<Product> findAll(Pageable pageable) {

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	    CriteriaQuery<ProductJpaEntity> cq = cb.createQuery(ProductJpaEntity.class);
	    Root<ProductJpaEntity> root = cq.from(ProductJpaEntity.class);		  
	    root.fetch("reviews", JoinType.LEFT);  
	    
        List<Order> orders = buildSortOrder(pageable.getSort(), cb, root);
	    cq.orderBy(orders);
	    
        TypedQuery<ProductJpaEntity> query = entityManager.createQuery(cq);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()); 
        query.setMaxResults(pageable.getPageSize());                           
        
        return productMapper.fromListJpaToListDomain(query.getResultList());
	}

	private List<Order> buildSortOrder(Sort sort, CriteriaBuilder criteriaBuilder, Root<ProductJpaEntity> root) {
	
		return sort.stream()
		.map( order ->{
			if (order.isAscending()) {
				return criteriaBuilder.asc(root.get(order.getProperty()));
			} else {
				return criteriaBuilder.desc(root.get(order.getProperty()));
			}
		})
		.toList();		
	}
	
	@Override
	@Transactional(value = "transactionManager", readOnly = true)
	public Optional<Product> findById(Long id) {
		
		return Optional.ofNullable(findByIdCommand(id).map(productMapper::fromJpaToDomain)
				.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.PROD_ID, String.valueOf(id)))));
	}
	
	private Optional<ProductJpaEntity> findByIdCommand(Long id) {

		TypedQuery<ProductJpaEntity> query = createQueryFindById(id);

		try {
			ProductJpaEntity result = query.getSingleResult();
			return Optional.ofNullable(result);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	private TypedQuery<ProductJpaEntity> createQueryFindById(Long id) {
		String queryString = "SELECT p FROM ProductJpaEntity p LEFT JOIN FETCH p.reviews WHERE productId = ?1";
		TypedQuery<ProductJpaEntity> query = entityManager.createQuery(queryString, ProductJpaEntity.class);
		query.setParameter(1, id);
		return query;
	}
	
	@Override
	@Transactional("transactionManager")
	public Optional<Product> save(Product dom) {
		
		validateNameInSaveAction(dom.getProductName());
		CategoryJpaEntity categoryJpaEntity = findCatById(dom);		
		ProductJpaEntity entity = productMapper.fromDomainToJpa(dom, categoryJpaEntity);
		entityManager.persist(entity);

		return Optional.ofNullable(entity).map(productMapper::fromJpaToDomain);
	}

	private void validateNameInSaveAction(String name) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
	    Root<ProductJpaEntity> root = cq.from(ProductJpaEntity.class);
	    
	    cq.select(cb.count(root))
	      .where(cb.equal(root.get(GlobalConstants.PROD_NAME), name));
	    
	    int result =  entityManager.createQuery(cq).getSingleResult().intValue();
		
	    if (result > 0) {
	    	throw new UniqueValueException(
	    			Map.of(GlobalConstants.OBJ_FIELD, GlobalConstants.PROD_NAME,
	    					GlobalConstants.OBJ_VALUE, name));
	    }    
	}

	private CategoryJpaEntity findCatById(Product dom) {
		return findCatByIdCommand(dom)
				.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.CAT_ID, String.valueOf(""))));
	}
	
	private Optional<CategoryJpaEntity> findCatByIdCommand(Product pro) {		
		try {
			CategoryJpaEntity categoryJpaEntity = entityManager.find(CategoryJpaEntity.class, pro.getCategory().getCategoryId());
			return Optional.ofNullable(categoryJpaEntity);
		} catch (Exception e) {
			return Optional.empty();
		}	
	}
	
	@Override
	@Transactional("transactionManager")
	public Optional<Product> update(Product dom) {

		Optional<ProductJpaEntity> optPro = findByIdCommand(dom.getProductId());
		optPro.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.PROD_ID, String.valueOf(dom.getProductId()))));
		validateNameInUpdateAction(dom);

		CategoryJpaEntity categoryJpaEntity = findCatById(dom);		
		
		return optPro.map(p -> updateAttrs(dom, p, categoryJpaEntity))
				.map(entityManager::merge)
				.map(productMapper::fromJpaToDomain);		
	}

	private void validateNameInUpdateAction(Product dom) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
	    Root<ProductJpaEntity> root = cq.from(ProductJpaEntity.class);
	    
	    cq.select(cb.count(root))
	      .where(cb.equal(root.get(GlobalConstants.PROD_NAME), dom.getProductName()))
	      .where(cb.notEqual(root.get(GlobalConstants.PROD_ID), dom.getProductId()));
	    
	    int result =  entityManager.createQuery(cq).getSingleResult().intValue();
		
	    if (result > 0) {
	    	throw new UniqueValueException(
	    			Map.of(GlobalConstants.OBJ_FIELD, GlobalConstants.PROD_NAME,
	    					GlobalConstants.OBJ_VALUE, dom.getProductName()));
	    }	    
	}

	private ProductJpaEntity updateAttrs(Product dom, ProductJpaEntity entity, CategoryJpaEntity categoryJpaEntity) {

		entity.setProductName(dom.getProductName());
		entity.setProductDescription(dom.getProductDescription());
		entity.setCategory(categoryJpaEntity);
		
		return entity;
	}
	
	@Override
	@Transactional("transactionManager")
	public boolean deleteById(Long id) {
		
		ProductJpaEntity entity = findByIdCommand(id)
				.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.PROD_ID, String.valueOf(id))));
		
		if (entity.getReviews().isEmpty()) {

			return deleteByIdCommand(entity);

		} else {
			throw new EntityAssociatedException(
					Map.of(GlobalConstants.PROD_ID, String.valueOf(id)));
		}		
	}

	private boolean deleteByIdCommand(ProductJpaEntity entity) {

		entityManager.remove(entity);

		return findByIdCommand(entity.getProductId()).isEmpty();
	}
	
}
