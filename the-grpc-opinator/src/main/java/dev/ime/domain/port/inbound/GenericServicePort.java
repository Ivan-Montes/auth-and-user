package dev.ime.domain.port.inbound;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

public interface GenericServicePort<U> {

	List<U>findAll();
	List<U>findAll(Pageable pageable);
	Optional<U>findById(Long id);
	Optional<U>save(U dto);
	Optional<U>update(U dto);
	boolean deleteById(Long id);
	
}
