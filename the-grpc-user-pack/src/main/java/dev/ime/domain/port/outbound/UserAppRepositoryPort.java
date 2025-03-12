package dev.ime.domain.port.outbound;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import dev.ime.domain.model.UserApp;

public interface UserAppRepositoryPort {

	List<UserApp> findAll();
	List<UserApp> findAll(Pageable pageable);
	Optional<UserApp> findById(Long userAppId);
	Optional<UserApp> save(UserApp userApp);
	int countByEmail(String email);

}
