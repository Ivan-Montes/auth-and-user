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

import dev.ime.common.mapper.UserAppMapper;
import dev.ime.domain.model.UserApp;
import dev.ime.infrastructure.entity.UserAppJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class UserAppRepositoryAdapterTest {

	@Mock
	private EntityManager entityManager;
	@Mock
	private UserAppMapper userAppMapper;

	@InjectMocks
	private UserAppRepositoryAdapter userAppRepositoryAdapter;	

	private UserApp userApp;
	private UserAppJpaEntity userAppJpaEntity;
	private UserAppJpaEntity userAppJpaEntityNew;
	
	private final Long userAppId = 1L;
	private final String email = "email@email.tk";
	private final String name = "name";
	private final String lastname = "lastname";
	private final Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());
	
	
	@BeforeEach
	private void setUp(){
		
		userApp = new UserApp(userAppId,email,name,lastname);
		userAppJpaEntity = UserAppJpaEntity.builder().userAppId(userAppId).email(email).name(name).lastname(lastname).build();
		userAppJpaEntityNew = UserAppJpaEntity.builder().userAppId(null).email(email).name(name).lastname(lastname).build();

	}	

	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnList() {

		TypedQuery<UserAppJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(userAppJpaEntity));
		Mockito.when(userAppMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(userApp));

		List<UserApp> list = userAppRepositoryAdapter.findAll();

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(list).isNotNull(),
				() -> Assertions.assertThat(list).hasSize(1));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_withPageable_shouldReturnPagedList() {

		CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
		CriteriaQuery<UserAppJpaEntity> cq = Mockito.mock(CriteriaQuery.class);
		Root<UserAppJpaEntity> root = Mockito.mock(Root.class);
		TypedQuery<UserAppJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		Mockito.when(cb.createQuery(UserAppJpaEntity.class)).thenReturn(cq);
		Mockito.when(cq.from(UserAppJpaEntity.class)).thenReturn(root);
		Mockito.when(cq.orderBy(Mockito.anyList())).thenReturn(cq);
		Mockito.when(entityManager.createQuery(cq)).thenReturn(query);
		Mockito.when(query.setFirstResult(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.setMaxResults(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(userAppJpaEntity));
		Mockito.when(userAppMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(userApp));

		List<UserApp> result = userAppRepositoryAdapter.findAll(pageable);

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(result).isNotNull(),
				() -> Assertions.assertThat(result).hasSize(1), 
				() -> Mockito.verify(query).setFirstResult(0),
				() -> Mockito.verify(query).setMaxResults(10));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findById_shouldReturnOptUserApp() {
		
		TypedQuery<UserAppJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getSingleResult()).thenReturn(userAppJpaEntity);
		Mockito.when(userAppMapper.fromJpaToDomain(Mockito.any(UserAppJpaEntity.class))).thenReturn(userApp);

		Optional<UserApp> optResult = userAppRepositoryAdapter.findById(userAppId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getUserAppId()).isEqualTo(userAppId)
				);
	}

	@Test
	void save_shouldReturnSavedUser() {
		
		Mockito.when(userAppMapper.fromDomainToJpa(Mockito.any(UserApp.class))).thenReturn(userAppJpaEntityNew);
		Mockito.doNothing().when(entityManager).persist(Mockito.any(UserAppJpaEntity.class));
		Mockito.when(userAppMapper.fromJpaToDomain(Mockito.any(UserAppJpaEntity.class))).thenReturn(userApp);
		
		Optional<UserApp> optResult = userAppRepositoryAdapter.save(userApp);

		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getUserAppId()).isEqualTo(userAppId)
				);	
	}
	
	@Test
	void save_shouldReturnUpdatedUser() {
		
		Mockito.when(userAppMapper.fromDomainToJpa(Mockito.any(UserApp.class))).thenReturn(userAppJpaEntity);
		Mockito.when(entityManager.merge(Mockito.any(UserAppJpaEntity.class))).thenReturn(userAppJpaEntity);
		Mockito.when(userAppMapper.fromJpaToDomain(Mockito.any(UserAppJpaEntity.class))).thenReturn(userApp);
		
		Optional<UserApp> optResult = userAppRepositoryAdapter.save(userApp);

		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getUserAppId()).isEqualTo(userAppId)
				);	
	}
	
}
