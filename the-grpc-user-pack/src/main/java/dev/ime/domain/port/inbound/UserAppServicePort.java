package dev.ime.domain.port.inbound;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

public interface UserAppServicePort<T> {

	List<T> findAll();
	List<T> findAll(Pageable pageable);
	boolean create(T dto);
	Optional<T> update(T dto);
}
