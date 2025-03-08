package dev.ime.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.UserMapper;
import dev.ime.domain.model.User;
import dev.ime.domain.port.outbound.UserRepository;
import dev.ime.infrastructure.entity.UserJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    @PersistenceContext
	private final EntityManager entityManager;
    private final UserMapper userMapper;    

	public UserRepositoryAdapter(EntityManager entityManager, UserMapper userMapper) {
		super();
		this.entityManager = entityManager;
		this.userMapper = userMapper;
	}

	@Override
	public List<User> findAll() {
		
		String queryString = "SELECT u FROM UserJpaEntity u";
        TypedQuery<UserJpaEntity> query = entityManager.createQuery(queryString, UserJpaEntity.class);
        
        return userMapper.fromListJpaToListDomain(query.getResultList());
	}

	@Override
	public List<User> findAll(Pageable pageable) {
		
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	    CriteriaQuery<UserJpaEntity> cq = cb.createQuery(UserJpaEntity.class);
	    Root<UserJpaEntity> root = cq.from(UserJpaEntity.class);	  
	    
        List<Order> orders = buildSortOrder(pageable.getSort(), cb, root);
	    cq.orderBy(orders);
	    
        TypedQuery<UserJpaEntity> query = entityManager.createQuery(cq);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()); 
        query.setMaxResults(pageable.getPageSize());                           
        
        return userMapper.fromListJpaToListDomain(query.getResultList());
	}

	private List<Order> buildSortOrder(Sort sort, CriteriaBuilder criteriaBuilder, Root<UserJpaEntity> root) {
	
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
	public Optional<User> findByEmail(String email) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserJpaEntity> cq = cb.createQuery(UserJpaEntity.class);        
        Root<UserJpaEntity> root = cq.from(UserJpaEntity.class);        
        Predicate emailPredicate = cb.equal(root.get(GlobalConstants.USER_EMAIL), email);
        cq.where(emailPredicate);
        List<UserJpaEntity> result = entityManager.createQuery(cq).getResultList();
        List<User> userList = userMapper.fromListJpaToListDomain(result);
        
        return userList.isEmpty() ? Optional.empty() : Optional.of(userList.get(0));
	}

	@Override
	@Transactional("transactionManager")
    public User save(User user) {
		
		UserJpaEntity userJpaEntity = userMapper.fromDomainToJpa(user);
		if (userJpaEntity.getUserId() == null) {
			entityManager.persist(userJpaEntity);
		} else {
			userJpaEntity = entityManager.merge(userJpaEntity);  
		}
		return userMapper.fromJpaToDomain(userJpaEntity);
	}

}
