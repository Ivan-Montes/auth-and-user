package dev.ime.domain.port.inbound;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

public interface AuthGrpcServicePort<T,U> {

	List<T> findAll();
	List<T> findAll(Pageable pageable);
	Optional<T> register(U dto);
}
