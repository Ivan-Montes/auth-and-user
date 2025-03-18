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
import dev.ime.common.mapper.CategoryMapper;
import dev.ime.domain.model.Category;
import dev.ime.infrastructure.entity.CategoryJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryAdapterTest {

	@Mock
	private EntityManager entityManager;
	@Mock
	private CategoryMapper categoryMapper;

	@InjectMocks
	private CategoryRepositoryAdapter categoryRepositoryAdapter;

	private Category category;
	private CategoryJpaEntity categoryJpaEntity;

	private final Long categoryId = 1L;
	private final String categoryName = "Vegetables";
	
	private final Pageable pageable = PageRequest.of(0, 10, Sort.by(GlobalConstants.CAT_ID).ascending());
	
	@BeforeEach
	private void setUp() {

		category = new Category();
		category.setCategoryId(categoryId);
		category.setCategoryName(categoryName);
		
		categoryJpaEntity = CategoryJpaEntity.builder().categoryId(categoryId).categoryName(categoryName).build();
			
	}	

	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnList() {

		TypedQuery<CategoryJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(categoryJpaEntity));
		Mockito.when(categoryMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(category));

		List<Category> list = categoryRepositoryAdapter.findAll();

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(list).isNotNull(),
				() -> Assertions.assertThat(list).hasSize(1));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_withPageable_shouldReturnPagedList() {

		CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
		CriteriaQuery<CategoryJpaEntity> cq = Mockito.mock(CriteriaQuery.class);
		Root<CategoryJpaEntity> root = Mockito.mock(Root.class);
		TypedQuery<CategoryJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		Mockito.when(cb.createQuery(CategoryJpaEntity.class)).thenReturn(cq);
		Mockito.when(cq.from(CategoryJpaEntity.class)).thenReturn(root);
		Mockito.when(cq.orderBy(Mockito.anyList())).thenReturn(cq);
		Mockito.when(entityManager.createQuery(cq)).thenReturn(query);
		Mockito.when(query.setFirstResult(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.setMaxResults(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(categoryJpaEntity));
		Mockito.when(categoryMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(category));

		List<Category> result = categoryRepositoryAdapter.findAll(pageable);

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(result).isNotNull(),
				() -> Assertions.assertThat(result).hasSize(1), 
				() -> Mockito.verify(query).setFirstResult(0),
				() -> Mockito.verify(query).setMaxResults(10));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findById_shouldReturnOpt() {
		
		TypedQuery<CategoryJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(categoryJpaEntity);
		Mockito.when(categoryMapper.fromJpaToDomain(Mockito.any(CategoryJpaEntity.class))).thenReturn(category);

		Optional<Category> optResult = categoryRepositoryAdapter.findById(categoryId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getCategoryId()).isEqualTo(categoryId)
				);
	}

	@Test
	@SuppressWarnings("unchecked")
	void save_shouldReturnOpt() {
		
		CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
		CriteriaQuery<Long> cq = Mockito.mock(CriteriaQuery.class);
		Root<CategoryJpaEntity> root = Mockito.mock(Root.class);
		TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		Mockito.when(cb.createQuery(Long.class)).thenReturn(cq);
		Mockito.when(cq.from(CategoryJpaEntity.class)).thenReturn(root);
		Mockito.when(cq.select(Mockito.any())).thenReturn(cq);
		Mockito.when(entityManager.createQuery(cq)).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(0L);
		Mockito.when(categoryMapper.fromDomainToJpa(Mockito.any(Category.class))).thenReturn(categoryJpaEntity);
		Mockito.doNothing().when(entityManager).persist(Mockito.any(CategoryJpaEntity.class));
		Mockito.when(categoryMapper.fromJpaToDomain(Mockito.any(CategoryJpaEntity.class))).thenReturn(category);

		Optional<Category> optResult = categoryRepositoryAdapter.save(category);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getCategoryId()).isEqualTo(categoryId)
				);
	}

	@Test
	@SuppressWarnings("unchecked")
	void update_shouldReturnOpt() {

		TypedQuery<CategoryJpaEntity> queryFindBy = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(queryFindBy);
		Mockito.when(queryFindBy.getSingleResult()).thenReturn(categoryJpaEntity);
		CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
		CriteriaQuery<Long> cq = Mockito.mock(CriteriaQuery.class);
		Root<CategoryJpaEntity> root = Mockito.mock(Root.class);
		TypedQuery<Long> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		Mockito.when(cb.createQuery(Long.class)).thenReturn(cq);
		Mockito.when(cq.from(CategoryJpaEntity.class)).thenReturn(root);
		Mockito.when(cq.select(Mockito.any())).thenReturn(cq);

		Path<Object> path = Mockito.mock(Path.class);
		Mockito.when(root.get(Mockito.anyString())).thenReturn(path);
		Predicate predicate = Mockito.mock(Predicate.class);
		Mockito.when(cb.equal(Mockito.any(Path.class), Mockito.anyString())).thenReturn(predicate);
		Mockito.when(cb.notEqual(Mockito.any(Path.class), Mockito.anyLong())).thenReturn(predicate);
	    Mockito.when(cq.where(Mockito.any(Expression.class))).thenReturn(cq);
		
		Mockito.when(entityManager.createQuery(cq)).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(0L);
		Mockito.when(entityManager.merge(Mockito.any(CategoryJpaEntity.class))).thenReturn(categoryJpaEntity);
		Mockito.when(categoryMapper.fromJpaToDomain(Mockito.any(CategoryJpaEntity.class))).thenReturn(category);

		Optional<Category> optResult = categoryRepositoryAdapter.update(category);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getCategoryId()).isEqualTo(categoryId)
				);		
	}

	@Test
	@SuppressWarnings("unchecked")
	void deleteById_shouldReturnTrue() {
		
		TypedQuery<CategoryJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(categoryJpaEntity).thenReturn(null);
		Mockito.doNothing().when(entityManager).remove(Mockito.any(CategoryJpaEntity.class));
		
		boolean result = categoryRepositoryAdapter.deleteById(categoryId);

		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
				);		
	}
		
}
