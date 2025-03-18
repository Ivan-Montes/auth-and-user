package dev.ime.domain.port.outbound;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

public interface GenericRepositoryPort<T> {

	List<T>findAll();
	List<T>findAll(Pageable pageable);
	Optional<T>findById(Long id);
	Optional<T>save(T dom);
	Optional<T>update(T dom);
	boolean deleteById(Long id);
	
}
