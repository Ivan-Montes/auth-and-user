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

import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.ReviewMapper;
import dev.ime.domain.model.Product;
import dev.ime.domain.model.Review;
import dev.ime.infrastructure.entity.ProductJpaEntity;
import dev.ime.infrastructure.entity.ReviewJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class ReviewRepositoryAdapterTest {

	@Mock
	private EntityManager entityManager;
	@Mock
	private ReviewMapper reviewMapper;	

	@InjectMocks
	private ReviewRepositoryAdapter reviewRepositoryAdapter;

	private Review review;
	private ReviewJpaEntity reviewJpaEntity;
	private Product product;
	private ProductJpaEntity productJpaEntity;
	
	private final Long reviewId = 1L;
	private final String email = "email@email.tk";
	private final Long productId = 1L;
	private final String reviewText = "Excellent";
	private final Integer rating = 5;

	private final Pageable pageable = PageRequest.of(0, 10, Sort.by(GlobalConstants.REV_ID).ascending());
		
	@BeforeEach
	private void setUp(){
		
		productJpaEntity = ProductJpaEntity.builder().productId(productId).build();
		reviewJpaEntity = ReviewJpaEntity.builder().reviewId(reviewId).email(email).reviewText(reviewText).rating(rating).product(productJpaEntity).build();
		
		product = new Product();
		product.setProductId(productId);
		
		review = new Review();
		review.setReviewId(reviewId);
		review.setEmail(email);
		review.setReviewText(reviewText);
		review.setRating(rating);
		review.setProduct(product);
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnList() {

		TypedQuery<ReviewJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(reviewJpaEntity));
		Mockito.when(reviewMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(review));

		List<Review> list = reviewRepositoryAdapter.findAll();

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(list).isNotNull(),
				() -> Assertions.assertThat(list).hasSize(1));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_withPageable_shouldReturnPagedList() {

		CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
		CriteriaQuery<ReviewJpaEntity> cq = Mockito.mock(CriteriaQuery.class);
		Root<ReviewJpaEntity> root = Mockito.mock(Root.class);
		TypedQuery<ReviewJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		Mockito.when(cb.createQuery(ReviewJpaEntity.class)).thenReturn(cq);
		Mockito.when(cq.from(ReviewJpaEntity.class)).thenReturn(root);
		Mockito.when(cq.orderBy(Mockito.anyList())).thenReturn(cq);
		Mockito.when(entityManager.createQuery(cq)).thenReturn(query);
		Mockito.when(query.setFirstResult(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.setMaxResults(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(reviewJpaEntity));
		Mockito.when(reviewMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(review));

		List<Review> result = reviewRepositoryAdapter.findAll(pageable);

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(result).isNotNull(),
				() -> Assertions.assertThat(result).hasSize(1), 
				() -> Mockito.verify(query).setFirstResult(0),
				() -> Mockito.verify(query).setMaxResults(10));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findById_shouldReturnOpt() {
		
		TypedQuery<ReviewJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(reviewJpaEntity);
		Mockito.when(reviewMapper.fromJpaToDomain(Mockito.any(ReviewJpaEntity.class))).thenReturn(review);

		Optional<Review> optResult = reviewRepositoryAdapter.findById(reviewId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getReviewId()).isEqualTo(reviewId)
				);
	}

	@Test
	@SuppressWarnings("unchecked")
	void save_shouldReturnOpt() {

		Mockito.when(entityManager.find(Mockito.any(Class.class),Mockito.anyLong())).thenReturn(productJpaEntity);
		Mockito.when(reviewMapper.fromDomainToJpa(Mockito.any(Review.class),Mockito.any(ProductJpaEntity.class))).thenReturn(reviewJpaEntity);
		Mockito.doNothing().when(entityManager).persist(Mockito.any(ReviewJpaEntity.class));
		Mockito.when(reviewMapper.fromJpaToDomain(Mockito.any(ReviewJpaEntity.class))).thenReturn(review);

		Optional<Review> optResult = reviewRepositoryAdapter.save(review);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getReviewId()).isEqualTo(reviewId)
				);
	}

	@Test
	@SuppressWarnings("unchecked")
	void update_shouldReturnOpt() {

		TypedQuery<ReviewJpaEntity> queryFindBy = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(queryFindBy);
		Mockito.when(queryFindBy.getSingleResult()).thenReturn(reviewJpaEntity);
		Mockito.when(entityManager.merge(Mockito.any(ReviewJpaEntity.class))).thenReturn(reviewJpaEntity);
		Mockito.when(reviewMapper.fromJpaToDomain(Mockito.any(ReviewJpaEntity.class))).thenReturn(review);

		Optional<Review> optResult = reviewRepositoryAdapter.update(review);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getReviewId()).isEqualTo(reviewId)
				);		
	}

	@Test
	@SuppressWarnings("unchecked")
	void deleteById_shouldReturnTrue() {
		
		TypedQuery<ReviewJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(reviewJpaEntity).thenReturn(null);
		Mockito.doNothing().when(entityManager).remove(Mockito.any(ReviewJpaEntity.class));
		
		
		boolean result = reviewRepositoryAdapter.deleteById(reviewId);

		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
				);		
	}
	
}
