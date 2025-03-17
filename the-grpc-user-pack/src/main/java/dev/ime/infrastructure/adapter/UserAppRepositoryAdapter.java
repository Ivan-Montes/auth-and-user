package dev.ime.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.model.UserApp;
import dev.ime.domain.port.outbound.UserAppRepositoryPort;
import dev.ime.infrastructure.entity.UserAppJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

@Repository
public class UserAppRepositoryAdapter implements UserAppRepositoryPort{

    @PersistenceContext
	private final EntityManager entityManager;
	private final UserAppMapper userAppMapper;
    
	public UserAppRepositoryAdapter(EntityManager entityManager, UserAppMapper userAppMapper) {
		super();
		this.entityManager = entityManager;
		this.userAppMapper = userAppMapper;
	}

	@Override
	public List<UserApp> findAll() {
		
		String queryString = "SELECT u FROM UserAppJpaEntity u";
        TypedQuery<UserAppJpaEntity> query = entityManager.createQuery(queryString, UserAppJpaEntity.class);
        
        return userAppMapper.fromListJpaToListDomain(query.getResultList());
	}

	@Override
	public List<UserApp> findAll(Pageable pageable) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	    CriteriaQuery<UserAppJpaEntity> cq = cb.createQuery(UserAppJpaEntity.class);
	    Root<UserAppJpaEntity> root = cq.from(UserAppJpaEntity.class);	  
	    
        List<Order> orders = buildSortOrder(pageable.getSort(), cb, root);
	    cq.orderBy(orders);
	    
        TypedQuery<UserAppJpaEntity> query = entityManager.createQuery(cq);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()); 
        query.setMaxResults(pageable.getPageSize());                           
        
        return userAppMapper.fromListJpaToListDomain(query.getResultList());
	}

	private List<Order> buildSortOrder(Sort sort, CriteriaBuilder criteriaBuilder, Root<UserAppJpaEntity> root) {
	
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
	public Optional<UserApp> findById(Long userAppId) {
		
	    String queryString = "SELECT u FROM UserAppJpaEntity u WHERE userAppId = ?1";
	    TypedQuery<UserAppJpaEntity> query = entityManager.createQuery(queryString, UserAppJpaEntity.class);
	    query.setParameter(1, userAppId);
	    
	    try {
	        UserAppJpaEntity result = query.getSingleResult();
	        return Optional.ofNullable(userAppMapper.fromJpaToDomain(result));
	    } catch (NoResultException e) {
	        return Optional.empty();
	    }
	}

	
	@Override
	@Transactional("transactionManager")
	public Optional<UserApp> save(UserApp userApp) {
		
		UserAppJpaEntity userAppJpaEntity = userAppMapper.fromDomainToJpa(userApp);
		if (userAppJpaEntity.getUserAppId() == null) {
			entityManager.persist(userAppJpaEntity);
		} else {
			userAppJpaEntity = entityManager.merge(userAppJpaEntity);  
		}
		return Optional.ofNullable(userAppJpaEntity)
				.map(userAppMapper::fromJpaToDomain);
	}

	@Override
	public int countByEmail(String email) {
		
	    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
	    Root<UserAppJpaEntity> root = cq.from(UserAppJpaEntity.class);
	    
	    cq.select(cb.count(root))
	      .where(cb.equal(root.get(GlobalConstants.USERAPP_EMAIL), email));
	    
	    return entityManager.createQuery(cq).getSingleResult().intValue();
	}

}
