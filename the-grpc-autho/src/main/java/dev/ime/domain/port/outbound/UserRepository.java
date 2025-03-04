package dev.ime.domain.port.outbound;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import dev.ime.domain.model.User;

public interface UserRepository {

	List<User> findAll();
	List<User> findAll(Pageable pageable);
	Optional<User> findByEmail(String email); 
	User save(User user);
	
}
