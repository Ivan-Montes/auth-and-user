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
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.domain.model.Category;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.infrastructure.entity.CategoryJpaEntity;
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
public class CategoryRepositoryAdapter implements GenericRepositoryPort<Category> {

	@PersistenceContext
	private final EntityManager entityManager;
	private final CategoryMapper categoryMapper;

	public CategoryRepositoryAdapter(EntityManager entityManager, CategoryMapper categoryMapper) {
		super();
		this.entityManager = entityManager;
		this.categoryMapper = categoryMapper;
	}

	@Override
	@Transactional(value = "transactionManager", readOnly = true)
	public List<Category> findAll() {

		String queryString = "SELECT c FROM CategoryJpaEntity c LEFT JOIN FETCH c.products";
		TypedQuery<CategoryJpaEntity> query = entityManager.createQuery(queryString, CategoryJpaEntity.class);

		return categoryMapper.fromListJpaToListDomain(query.getResultList());

	}

	@Override
	@Transactional(value = "transactionManager", readOnly = true)
	public List<Category> findAll(Pageable pageable) {

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	    CriteriaQuery<CategoryJpaEntity> cq = cb.createQuery(CategoryJpaEntity.class);
	    Root<CategoryJpaEntity> root = cq.from(CategoryJpaEntity.class);	  
	    root.fetch("products", JoinType.LEFT);

        List<Order> orders = buildSortOrder(pageable.getSort(), cb, root);
	    cq.orderBy(orders);
	    
        TypedQuery<CategoryJpaEntity> query = entityManager.createQuery(cq);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()); 
        query.setMaxResults(pageable.getPageSize());                           
        
        return categoryMapper.fromListJpaToListDomain(query.getResultList());
	}

	private List<Order> buildSortOrder(Sort sort, CriteriaBuilder criteriaBuilder, Root<CategoryJpaEntity> root) {
	
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
	public Optional<Category> findById(Long id) {

		return Optional.ofNullable(findByIdCommand(id)
				.map(categoryMapper::fromJpaToDomain)
				.orElseThrow(() -> new ResourceNotFoundException(
						Map.of(GlobalConstants.CAT_ID, String.valueOf(id)))));
	}

	private Optional<CategoryJpaEntity> findByIdCommand(Long id) {

		TypedQuery<CategoryJpaEntity> query = createQueryFindById(id);

		try {
			CategoryJpaEntity result = query.getSingleResult();
			return Optional.ofNullable(result);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	private TypedQuery<CategoryJpaEntity> createQueryFindById(Long id) {
		String queryString = "SELECT c FROM CategoryJpaEntity c LEFT JOIN FETCH c.products WHERE categoryId = ?1 ";
		TypedQuery<CategoryJpaEntity> query = entityManager.createQuery(queryString, CategoryJpaEntity.class);
		query.setParameter(1, id);
		return query;
	}

	@Override
	@Transactional("transactionManager")
	public Optional<Category> save(Category dom) {

		validateNameInSaveAction(dom.getCategoryName());
		CategoryJpaEntity entity = categoryMapper.fromDomainToJpa(dom);
		entityManager.persist(entity);

		return Optional.ofNullable(entity).map(categoryMapper::fromJpaToDomain);
	}
	
	private void validateNameInSaveAction(String name) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
	    Root<CategoryJpaEntity> root = cq.from(CategoryJpaEntity.class);
	    
	    cq.select(cb.count(root))
	      .where(cb.equal(root.get(GlobalConstants.CAT_NAME), name));
	    
	    int result =  entityManager.createQuery(cq).getSingleResult().intValue();
		
	    if (result > 0) {
	    	throw new UniqueValueException(
	    			Map.of(GlobalConstants.OBJ_FIELD, GlobalConstants.CAT_NAME,
	    					GlobalConstants.OBJ_VALUE, name));
	    }    
	}

	@Override
	@Transactional("transactionManager")
	public Optional<Category> update(Category dom) {

		Optional<CategoryJpaEntity> optCat = findByIdCommand(dom.getCategoryId());
		optCat.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.CAT_ID, String.valueOf(dom.getCategoryId()))));
		validateNameInUpdateAction(dom);
		
		return optCat.map(c -> updateAttrs(dom, c))
				.map(entityManager::merge)
				.map(categoryMapper::fromJpaToDomain);
	}

	private void validateNameInUpdateAction(Category dom) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
	    Root<CategoryJpaEntity> root = cq.from(CategoryJpaEntity.class);
	    
	    cq.select(cb.count(root))
	      .where(cb.equal(root.get(GlobalConstants.CAT_NAME), dom.getCategoryName()))
	      .where(cb.notEqual(root.get(GlobalConstants.CAT_ID), dom.getCategoryId()));
	    
	    int result =  entityManager.createQuery(cq).getSingleResult().intValue();
		
	    if (result > 0) {
	    	throw new UniqueValueException(
	    			Map.of(GlobalConstants.OBJ_FIELD, GlobalConstants.CAT_NAME,
	    					GlobalConstants.OBJ_VALUE, dom.getCategoryName()));
	    }	    
	}

	private CategoryJpaEntity updateAttrs(Category dom, CategoryJpaEntity entity) {

		entity.setCategoryName(dom.getCategoryName());

		return entity;
	}

	@Override
	@Transactional("transactionManager")
	public boolean deleteById(Long id) {

		CategoryJpaEntity entity = findByIdCommand(id)
				.orElseThrow(() -> new ResourceNotFoundException(
						Map.of(GlobalConstants.CAT_ID, String.valueOf(id))));

		if (entity.getProducts().isEmpty()) {

			return deleteByIdCommand(entity);

		} else {
			throw new EntityAssociatedException(
					Map.of(GlobalConstants.CAT_ID, String.valueOf(id)));
		}
	}

	private boolean deleteByIdCommand(CategoryJpaEntity entity) {

		entityManager.remove(entity);

		return findByIdCommand(entity.getCategoryId()).isEmpty();
	}

}
