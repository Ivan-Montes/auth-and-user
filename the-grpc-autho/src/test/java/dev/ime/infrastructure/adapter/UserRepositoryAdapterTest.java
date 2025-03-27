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

import dev.ime.common.mapper.UserMapper;
import dev.ime.domain.model.Role;
import dev.ime.domain.model.User;
import dev.ime.infrastructure.entity.UserJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

	@Mock
	private UserMapper userMapper;
	@Mock
	private EntityManager entityManager;

	@InjectMocks
	private UserRepositoryAdapter userRepositoryAdapter;

	private User user;
	private UserJpaEntity userJpaEntity;
	private UserJpaEntity userJpaEntityNew;

	private final Long userId = 1L;
	private final String email = "email@email.tk";
	private final String password = "god";
	private final Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());

	@BeforeEach
	private void setUp() {

		user = new User(userId, email, password, Role.USER);
		userJpaEntity = UserJpaEntity.builder().userId(userId).email(email).password(password).role(Role.USER).build();
		userJpaEntityNew = UserJpaEntity.builder().userId(null).email(email).password(password).role(Role.USER).build();
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_shouldReturnList() {

		TypedQuery<UserJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.createQuery(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(userJpaEntity));
		Mockito.when(userMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(user));

		List<User> list = userRepositoryAdapter.findAll();

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(list).isNotNull(),
				() -> Assertions.assertThat(list).hasSize(1));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findAll_withPageable_shouldReturnPagedList() {

		CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
		CriteriaQuery<UserJpaEntity> cq = Mockito.mock(CriteriaQuery.class);
		Root<UserJpaEntity> root = Mockito.mock(Root.class);
		TypedQuery<UserJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		Mockito.when(cb.createQuery(UserJpaEntity.class)).thenReturn(cq);
		Mockito.when(cq.from(UserJpaEntity.class)).thenReturn(root);
		Mockito.when(cq.orderBy(Mockito.anyList())).thenReturn(cq);
		Mockito.when(entityManager.createQuery(cq)).thenReturn(query);
		Mockito.when(query.setFirstResult(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.setMaxResults(Mockito.anyInt())).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(userJpaEntity));
		Mockito.when(userMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(user));

		List<User> result = userRepositoryAdapter.findAll(pageable);

		org.junit.jupiter.api.Assertions.assertAll(
				() -> Assertions.assertThat(result).isNotNull(),
				() -> Assertions.assertThat(result).hasSize(1), 
				() -> Mockito.verify(query).setFirstResult(0),
				() -> Mockito.verify(query).setMaxResults(10));
	}

	@SuppressWarnings("unchecked")
	@Test
	void findByEmail_shouldReturnOptUser() {

		CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
		CriteriaQuery<UserJpaEntity> cq = Mockito.mock(CriteriaQuery.class);
		Root<UserJpaEntity> root = Mockito.mock(Root.class);
		TypedQuery<UserJpaEntity> query = Mockito.mock(TypedQuery.class);
		Mockito.when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		Mockito.when(cb.createQuery(UserJpaEntity.class)).thenReturn(cq);
		Mockito.when(cq.from(UserJpaEntity.class)).thenReturn(root);
		
		Mockito.when(entityManager.createQuery(cq)).thenReturn(query);
		Mockito.when(query.getResultList()).thenReturn(List.of(userJpaEntity));
		Mockito.when(userMapper.fromListJpaToListDomain(Mockito.anyList())).thenReturn(List.of(user));

		Optional<User> optResult = userRepositoryAdapter.findByEmail(email);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().getUserId()).isEqualTo(userId)
				);
	}

	@Test
	void save_shouldReturnSavedUser() {
		
		Mockito.when(userMapper.fromDomainToJpa(Mockito.any(User.class))).thenReturn(userJpaEntityNew);
		Mockito.doNothing().when(entityManager).persist(Mockito.any(UserJpaEntity.class));
		Mockito.when(userMapper.fromJpaToDomain(Mockito.any(UserJpaEntity.class))).thenReturn(user);
		
		User userSaved = userRepositoryAdapter.save(user);

		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(userSaved).isNotNull()
				);		
	}
	
	@Test
	void save_shouldReturnUpdatedUser() {
		
		Mockito.when(userMapper.fromDomainToJpa(Mockito.any(User.class))).thenReturn(userJpaEntity);
		Mockito.when(entityManager.merge(Mockito.any(UserJpaEntity.class))).thenReturn(userJpaEntity);
		Mockito.when(userMapper.fromJpaToDomain(Mockito.any(UserJpaEntity.class))).thenReturn(user);
		
		User userUpdated = userRepositoryAdapter.save(user);

		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(userUpdated).isNotNull(),
				()-> Assertions.assertThat(userUpdated.getUserId()).isEqualTo(userId)
				);		
	}
	
}
