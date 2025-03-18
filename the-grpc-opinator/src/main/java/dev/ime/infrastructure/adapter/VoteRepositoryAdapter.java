package dev.ime.infrastructure.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.ime.application.exception.OnlyOneVotePerUserInReviewException;
import dev.ime.application.exception.ResourceNotFoundException;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.model.Vote;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.infrastructure.entity.ReviewJpaEntity;
import dev.ime.infrastructure.entity.VoteJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;

@Repository
public class VoteRepositoryAdapter implements GenericRepositoryPort<Vote> {

	@PersistenceContext
	private final EntityManager entityManager;
	private final VoteMapper voteMapper;
	
	public VoteRepositoryAdapter(EntityManager entityManager, VoteMapper voteMapper) {
		super();
		this.entityManager = entityManager;
		this.voteMapper = voteMapper;
	}

	@Override
	@Transactional(value = "transactionManager", readOnly = true)
	public List<Vote> findAll() {
		
		String queryString = "SELECT v FROM VoteJpaEntity v";
		TypedQuery<VoteJpaEntity> query = entityManager.createQuery(queryString, VoteJpaEntity.class);

		return voteMapper.fromListJpaToListDomain(query.getResultList());
	}

	@Override
	@Transactional(value = "transactionManager", readOnly = true)
	public List<Vote> findAll(Pageable pageable) {

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	    CriteriaQuery<VoteJpaEntity> cq = cb.createQuery(VoteJpaEntity.class);
	    Root<VoteJpaEntity> root = cq.from(VoteJpaEntity.class);
	    
        List<Order> orders = buildSortOrder(pageable.getSort(), cb, root);
	    cq.orderBy(orders);
	    
        TypedQuery<VoteJpaEntity> query = entityManager.createQuery(cq);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()); 
        query.setMaxResults(pageable.getPageSize());                           
        
        return voteMapper.fromListJpaToListDomain(query.getResultList());
	}

	private List<Order> buildSortOrder(Sort sort, CriteriaBuilder criteriaBuilder, Root<VoteJpaEntity> root) {
	
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
	public Optional<Vote> findById(Long id) {
		
		return Optional.ofNullable(findByIdCommand(id).map(voteMapper::fromJpaToDomain)
				.orElseThrow(() -> new ResourceNotFoundException(
						Map.of(GlobalConstants.VOT_ID, String.valueOf(id)))));
	}

	private Optional<VoteJpaEntity> findByIdCommand(Long id) {

		TypedQuery<VoteJpaEntity> query = createQueryFindById(id);

		try {
			VoteJpaEntity result = query.getSingleResult();
			return Optional.ofNullable(result);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	private TypedQuery<VoteJpaEntity> createQueryFindById(Long id) {
		String queryString = "SELECT v FROM VoteJpaEntity v WHERE voteId = ?1";
		TypedQuery<VoteJpaEntity> query = entityManager.createQuery(queryString, VoteJpaEntity.class);
		query.setParameter(1, id);
		return query;
	}
	
	@Override
	@Transactional("transactionManager")
	public Optional<Vote> save(Vote dom) {
		
		ReviewJpaEntity reviewJpaEntity = findRevById(dom);
		validateOnlyOneVoteRestricction(dom, reviewJpaEntity);		
		
		VoteJpaEntity entity = voteMapper.fromDomainToJpa(dom, reviewJpaEntity);
		entityManager.persist(entity);
		
		return Optional.ofNullable(entity).map(voteMapper::fromJpaToDomain);
	}

	private void validateOnlyOneVoteRestricction(Vote dom, ReviewJpaEntity reviewJpaEntity) {
		Set<VoteJpaEntity> voteList = reviewJpaEntity.getVotes();
		
		boolean result = voteList.stream()
		.anyMatch( v -> dom.getEmail().equals(v.getEmail()));
		
		if (result) {
			throw new OnlyOneVotePerUserInReviewException(Map.of(
					GlobalConstants.REV_ID, String.valueOf(dom.getReview().getReviewId()),
					GlobalConstants.USER_EMAIL, dom.getEmail()
					));
		}
	}
	
	private ReviewJpaEntity findRevById(Vote dom) {
		return findReviewByIdCommand(dom)
				.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.REV_ID, String.valueOf(""))));
	}
	
	private Optional<ReviewJpaEntity> findReviewByIdCommand(Vote dom) {		
		try {
			ReviewJpaEntity reviewJpaEntity = entityManager.find(ReviewJpaEntity.class, dom.getReview().getReviewId());
			return Optional.ofNullable(reviewJpaEntity);
		} catch (Exception e) {
			return Optional.empty();
		}	
	}
	
	@Override
	@Transactional("transactionManager")
	public Optional<Vote> update(Vote dom) {

		Optional<VoteJpaEntity> optV = findByIdCommand(dom.getVoteId());
		optV.orElseThrow(() -> new ResourceNotFoundException(
				Map.of(GlobalConstants.VOT_ID, String.valueOf(dom.getVoteId()))));

		return optV.map(v -> updateAttrs(dom, v))
				.map(entityManager::merge)
				.map(voteMapper::fromJpaToDomain);
		
	}	

	private VoteJpaEntity updateAttrs(Vote dom, VoteJpaEntity entity) {
		
		entity.setUseful(dom.isUseful());
		
		return entity;
	}

	@Override
	@Transactional("transactionManager")
	public boolean deleteById(Long id) {

		VoteJpaEntity entity = findByIdCommand(id)
				.orElseThrow(() -> new ResourceNotFoundException(
						Map.of(GlobalConstants.VOT_ID, String.valueOf(id))));

		return deleteByIdCommand(entity);
	}

	private boolean deleteByIdCommand(VoteJpaEntity entity) {

		entityManager.remove(entity);

		return findByIdCommand(entity.getVoteId()).isEmpty();
	}

}
