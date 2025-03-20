package dev.ime.infrastructure.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.ime.application.exception.EntityAssociatedException;
import dev.ime.application.exception.OnlyOneReviewPerUserInProductException;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.model.Review;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.infrastructure.entity.ProductJpaEntity;
import dev.ime.infrastructure.entity.ReviewJpaEntity;
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
public class ReviewRepositoryAdapter implements GenericRepositoryPort<Review> {

	@PersistenceContext
	private final EntityManager entityManager;
	private final ReviewMapper reviewMapper;	
	
	public ReviewRepositoryAdapter(EntityManager entityManager, ReviewMapper reviewMapper) {
		super();
		this.entityManager = entityManager;
		this.reviewMapper = reviewMapper;
	}

	@Override
	@Transactional(value = "transactionManager", readOnly = true)
	public List<Review> findAll() {
		
		String queryString = "SELECT r FROM ReviewJpaEntity r LEFT JOIN FETCH r.votes";
		TypedQuery<ReviewJpaEntity> query = entityManager.createQuery(queryString, ReviewJpaEntity.class);

		return reviewMapper.fromListJpaToListDomain(query.getResultList());
	}

	@Override
	@Transactional(value = "transactionManager", readOnly = true)
	public List<Review> findAll(Pageable pageable) {

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	    CriteriaQuery<ReviewJpaEntity> cq = cb.createQuery(ReviewJpaEntity.class);
	    Root<ReviewJpaEntity> root = cq.from(ReviewJpaEntity.class);	  
	    root.fetch("votes", JoinType.LEFT);	  
	    
        List<Order> orders = buildSortOrder(pageable.getSort(), cb, root);
	    cq.orderBy(orders);
	    
        TypedQuery<ReviewJpaEntity> query = entityManager.createQuery(cq);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()); 
        query.setMaxResults(pageable.getPageSize());                           
        
        return reviewMapper.fromListJpaToListDomain(query.getResultList());
	}

	private List<Order> buildSortOrder(Sort sort, CriteriaBuilder criteriaBuilder, Root<ReviewJpaEntity> root) {
	
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
	public Optional<Review> findById(Long id) {
		
		return Optional.ofNullable(findByIdCommand(id).map(reviewMapper::fromJpaToDomain)
				.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.REV_ID, String.valueOf(id)))));
	}
	
	private Optional<ReviewJpaEntity> findByIdCommand(Long id) {

		TypedQuery<ReviewJpaEntity> query = createQueryFindById(id);

		try {
			ReviewJpaEntity result = query.getSingleResult();
			return Optional.ofNullable(result);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	private TypedQuery<ReviewJpaEntity> createQueryFindById(Long id) {
		String queryString = "SELECT r FROM ReviewJpaEntity r LEFT JOIN FETCH r.votes WHERE reviewId = ?1";
		TypedQuery<ReviewJpaEntity> query = entityManager.createQuery(queryString, ReviewJpaEntity.class);
		query.setParameter(1, id);
		return query;
	}
	
	@Override
	@Transactional("transactionManager")
	public Optional<Review> save(Review dom) {
		
		ProductJpaEntity productJpaEntity = findProdById(dom.getProduct().getProductId());
		validateOnlyOneReviewRestricction(dom, productJpaEntity);
		ReviewJpaEntity entity = reviewMapper.fromDomainToJpa(dom, productJpaEntity);
		entityManager.persist(entity);
		
		return Optional.ofNullable(entity).map(reviewMapper::fromJpaToDomain);
	}

	private void validateOnlyOneReviewRestricction(Review dom, ProductJpaEntity productJpaEntity) {
		Set<ReviewJpaEntity> revList = productJpaEntity.getReviews();
		
		boolean result = revList.stream()
		.anyMatch( r -> dom.getEmail().equals(r.getEmail()));
		
		if (result) {
			throw new OnlyOneReviewPerUserInProductException(Map.of(
					GlobalConstants.PROD_ID, String.valueOf(dom.getProduct().getProductId()),
					GlobalConstants.USER_EMAIL, dom.getEmail()
					));
		}
	}
	
	private ProductJpaEntity findProdById(Long id) {
		return findProdByIdCommand(id)
				.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.PROD_ID, String.valueOf(""))));
	}
	
	private Optional<ProductJpaEntity> findProdByIdCommand(Long id) {		
		try {
			ProductJpaEntity productJpaEntity = entityManager.find(ProductJpaEntity.class, id);
			return Optional.ofNullable(productJpaEntity);
		} catch (Exception e) {
			return Optional.empty();
		}	
	}
	
	@Override
	@Transactional("transactionManager")
	public Optional<Review> update(Review dom) {

		Optional<ReviewJpaEntity> optRev = findByIdCommand(dom.getReviewId());
		optRev.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.REV_ID, String.valueOf(dom.getReviewId()))));

		return optRev.map(r -> updateAttrs(dom, r))
				.map(entityManager::merge)
				.map(reviewMapper::fromJpaToDomain);
		
	}

	private ReviewJpaEntity updateAttrs(Review dom, ReviewJpaEntity entity) {

		entity.setReviewText(dom.getReviewText());
		entity.setRating(dom.getRating());
		
		return entity;
	}

	@Override
	@Transactional("transactionManager")
	public boolean deleteById(Long id) {
		
		ReviewJpaEntity entity = findByIdCommand(id)
				.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.REV_ID, String.valueOf(id))));
		
		if (entity.getVotes().isEmpty()) {

			return deleteByIdCommand(entity);

		} else {
			throw new EntityAssociatedException(
					Map.of(GlobalConstants.REV_ID, String.valueOf(id)));
		}	
	}

	private boolean deleteByIdCommand(ReviewJpaEntity entity) {

		entityManager.remove(entity);

		return findByIdCommand(entity.getReviewId()).isEmpty();
	}

}
