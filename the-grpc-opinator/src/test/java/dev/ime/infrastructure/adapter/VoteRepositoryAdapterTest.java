package dev.ime.infrastructure.adapter;

import java.util.List;
import java.util.Optional;

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

import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.model.Review;
import dev.ime.domain.model.Vote;
import dev.ime.infrastructure.entity.ReviewJpaEntity;
import dev.ime.infrastructure.entity.VoteJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class VoteRepositoryAdapterTest {

	@Mock
	private EntityManager entityManager;
	@Mock
	private VoteMapper voteMapper;

	@InjectMocks
	private VoteRepositoryAdapter voteRepositoryAdapter;
	
	private Vote vote;
	private VoteJpaEntity voteJpaEntity;
	private Review review;
	private ReviewJpaEntity reviewJpaEntity;
	
	private final Long voteId = 1L;
	private final String email = "email@email.tk";
	private final Long reviewId = 1L;
	private final boolean useful = true;

	private final Pageable pageable = PageRequest.of(0, 10, Sort.by(GlobalConstants.REV_ID).ascending());
		
	@BeforeEach
	private void setUp(){
		
		reviewJpaEntity = ReviewJpaEntity.builder().reviewId(reviewId).email(email).build();
		voteJpaEntity = VoteJpaEntity.builder().voteId(voteId).email(email).review(reviewJpaEntity).useful(useful).build();
		
		review = new Review();
		review.setReviewId(reviewId);
		
		vote = new Vote();
		vote.setVoteId(voteId);
		vote.setEmail(email);
		vote.setReview(review);
		vote.setUseful(useful);
				
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnList() {

		TypedQuery<VoteJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(voteJpaEntity));
		Mockito.when(voteMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(vote));

		List<Vote> list = voteRepositoryAdapter.findAll();

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(list).isNotNull(),
				() -> Assertions.assertThat(list).hasSize(1));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_withPageable_shouldReturnPagedList() {

		CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
		CriteriaQuery<VoteJpaEntity> cq = Mockito.mock(CriteriaQuery.class);
		Root<VoteJpaEntity> root = Mockito.mock(Root.class);
		TypedQuery<VoteJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		Mockito.when(cb.createQuery(VoteJpaEntity.class)).thenReturn(cq);
		Mockito.when(cq.from(VoteJpaEntity.class)).thenReturn(root);
		Mockito.when(cq.orderBy(Mockito.anyList())).thenReturn(cq);
		Mockito.when(entityManager.createQuery(cq)).thenReturn(query);
		Mockito.when(query.setFirstResult(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.setMaxResults(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(voteJpaEntity));
		Mockito.when(voteMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(vote));

		List<Vote> result = voteRepositoryAdapter.findAll(pageable);

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(result).isNotNull(),
				() -> Assertions.assertThat(result).hasSize(1), 
				() -> Mockito.verify(query).setFirstResult(0),
				() -> Mockito.verify(query).setMaxResults(10));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findById_shouldReturnOpt() {
		
		TypedQuery<VoteJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(voteJpaEntity);
		Mockito.when(voteMapper.fromJpaToDomain(Mockito.any(VoteJpaEntity.class))).thenReturn(vote);

		Optional<Vote> optResult = voteRepositoryAdapter.findById(voteId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getVoteId()).isEqualTo(voteId)
				);
	}

	@Test
	@SuppressWarnings("unchecked")
	void save_shouldReturnOpt() {
		
		Mockito.when(entityManager.find(Mockito.any(Class.class),Mockito.anyLong())).thenReturn(reviewJpaEntity);
		Mockito.when(voteMapper.fromDomainToJpa(Mockito.any(Vote.class),Mockito.any(ReviewJpaEntity.class))).thenReturn(voteJpaEntity);
		Mockito.doNothing().when(entityManager).persist(Mockito.any(VoteJpaEntity.class));
		Mockito.when(voteMapper.fromJpaToDomain(Mockito.any(VoteJpaEntity.class))).thenReturn(vote);

		Optional<Vote> optResult = voteRepositoryAdapter.save(vote);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getVoteId()).isEqualTo(voteId)
				);
	}

	@Test
	@SuppressWarnings("unchecked")
	void update_shouldReturnOpt() {

		TypedQuery<VoteJpaEntity> queryFindBy = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(queryFindBy);
		Mockito.when(queryFindBy.getSingleResult()).thenReturn(voteJpaEntity);
		Mockito.when(entityManager.merge(Mockito.any(VoteJpaEntity.class))).thenReturn(voteJpaEntity);
		Mockito.when(voteMapper.fromJpaToDomain(Mockito.any(VoteJpaEntity.class))).thenReturn(vote);

		Optional<Vote> optResult = voteRepositoryAdapter.update(vote);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getVoteId()).isEqualTo(voteId)
				);		
	}

	@Test
	@SuppressWarnings("unchecked")
	void deleteById_shouldReturnTrue() {
		
		TypedQuery<VoteJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(voteJpaEntity).thenReturn(null);
		Mockito.doNothing().when(entityManager).remove(Mockito.any(VoteJpaEntity.class));
		
		boolean result = voteRepositoryAdapter.deleteById(voteId);

		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
				);		
	}
	
}
