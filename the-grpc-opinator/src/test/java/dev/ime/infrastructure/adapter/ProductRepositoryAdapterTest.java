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
import dev.ime.common.mapper.ProductMapper;
import dev.ime.domain.model.Category;
import dev.ime.domain.model.Product;
import dev.ime.infrastructure.entity.CategoryJpaEntity;
import dev.ime.infrastructure.entity.ProductJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

	@Mock
	private EntityManager entityManager;
	@Mock
	private ProductMapper productMapper;

	@InjectMocks
	private ProductRepositoryAdapter productRepositoryAdapter;

	private Product product;
	private ProductJpaEntity productJpaEntity;
	private Category category;

	private final Long productId = 1L;
	private final String productName = "Tomatoes";
	private final String productDescription = "full of red";
	private final Long categoryId = 1L;

	private final Pageable pageable = PageRequest.of(0, 10, Sort.by(GlobalConstants.PROD_ID).ascending());
		
	@BeforeEach
	private void setUp() {
		
		productJpaEntity = ProductJpaEntity.builder().productId(productId).productName(productName).productDescription(productDescription).category(new CategoryJpaEntity()).build();
		
		category = new Category();
		category.setCategoryId(categoryId);
		
		product = new Product();
		product.setProductId(productId);
		product.setProductName(productName);
		product.setProductDescription(productDescription);
		product.setCategory(category);
		
	
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnList() {

		TypedQuery<ProductJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(productJpaEntity));
		Mockito.when(productMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(product));

		List<Product> list = productRepositoryAdapter.findAll();

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(list).isNotNull(),
				() -> Assertions.assertThat(list).hasSize(1));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_withPageable_shouldReturnPagedList() {

		CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
		CriteriaQuery<ProductJpaEntity> cq = Mockito.mock(CriteriaQuery.class);
		Root<ProductJpaEntity> root = Mockito.mock(Root.class);
		TypedQuery<ProductJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		Mockito.when(cb.createQuery(ProductJpaEntity.class)).thenReturn(cq);
		Mockito.when(cq.from(ProductJpaEntity.class)).thenReturn(root);
		Mockito.when(cq.orderBy(Mockito.anyList())).thenReturn(cq);
		Mockito.when(entityManager.createQuery(cq)).thenReturn(query);
		Mockito.when(query.setFirstResult(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.setMaxResults(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(productJpaEntity));
		Mockito.when(productMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(product));

		List<Product> result = productRepositoryAdapter.findAll(pageable);

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(result).isNotNull(),
				() -> Assertions.assertThat(result).hasSize(1), 
				() -> Mockito.verify(query).setFirstResult(0),
				() -> Mockito.verify(query).setMaxResults(10));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findById_shouldReturnOpt() {
		
		TypedQuery<ProductJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(productJpaEntity);
		Mockito.when(productMapper.fromJpaToDomain(Mockito.any(ProductJpaEntity.class))).thenReturn(product);

		Optional<Product> optResult = productRepositoryAdapter.findById(productId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getProductId()).isEqualTo(productId)
				);
	}

	@Test
	@SuppressWarnings("unchecked")
	void save_shouldReturnOpt() {
		
		CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
		CriteriaQuery<Long> cq = Mockito.mock(CriteriaQuery.class);
		Root<ProductJpaEntity> root = Mockito.mock(Root.class);
		TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		Mockito.when(cb.createQuery(Long.class)).thenReturn(cq);
		Mockito.when(cq.from(ProductJpaEntity.class)).thenReturn(root);
		Mockito.when(cq.select(Mockito.any())).thenReturn(cq);
		Mockito.when(entityManager.createQuery(cq)).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(0L);
		Mockito.when(entityManager.find(Mockito.any(Class.class),Mockito.anyLong())).thenReturn(new CategoryJpaEntity());
		Mockito.when(productMapper.fromDomainToJpa(Mockito.any(Product.class),Mockito.any(CategoryJpaEntity.class))).thenReturn(productJpaEntity);
		Mockito.doNothing().when(entityManager).persist(Mockito.any(ProductJpaEntity.class));
		Mockito.when(productMapper.fromJpaToDomain(Mockito.any(ProductJpaEntity.class))).thenReturn(product);

		Optional<Product> optResult = productRepositoryAdapter.save(product);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getProductId()).isEqualTo(productId)
				);
	}

	@Test
	@SuppressWarnings("unchecked")
	void update_shouldReturnOpt() {

		TypedQuery<ProductJpaEntity> queryFindBy = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(queryFindBy);
		Mockito.when(queryFindBy.getSingleResult()).thenReturn(productJpaEntity);
		CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
		CriteriaQuery<Long> cq = Mockito.mock(CriteriaQuery.class);
		Root<ProductJpaEntity> root = Mockito.mock(Root.class);
		TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		Mockito.when(cb.createQuery(Long.class)).thenReturn(cq);
		Mockito.when(cq.from(ProductJpaEntity.class)).thenReturn(root);
		Mockito.when(cq.select(Mockito.any())).thenReturn(cq);

		Path<Object> path = Mockito.mock(Path.class);
		Mockito.when(root.get(Mockito.anyString())).thenReturn(path);
		Predicate predicate = Mockito.mock(Predicate.class);
		Mockito.when(cb.equal(Mockito.any(Path.class), Mockito.anyString())).thenReturn(predicate);
		Mockito.when(cb.notEqual(Mockito.any(Path.class), Mockito.anyLong())).thenReturn(predicate);
	    Mockito.when(cq.where(Mockito.any(Expression.class))).thenReturn(cq);
		
		Mockito.when(entityManager.createQuery(cq)).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(0L);
		Mockito.when(entityManager.find(Mockito.any(Class.class),Mockito.anyLong())).thenReturn(new CategoryJpaEntity());
		Mockito.when(entityManager.merge(Mockito.any(ProductJpaEntity.class))).thenReturn(productJpaEntity);
		Mockito.when(productMapper.fromJpaToDomain(Mockito.any(ProductJpaEntity.class))).thenReturn(product);

		Optional<Product> optResult = productRepositoryAdapter.update(product);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getProductId()).isEqualTo(productId)
				);		
	}

	@Test
	@SuppressWarnings("unchecked")
	void deleteById_shouldReturnTrue() {
		
		TypedQuery<ProductJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(productJpaEntity).thenReturn(null);
		Mockito.doNothing().when(entityManager).remove(Mockito.any(ProductJpaEntity.class));
		
		boolean result = productRepositoryAdapter.deleteById(productId);

		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
				);		
	}
		

}
