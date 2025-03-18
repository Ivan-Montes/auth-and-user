package dev.ime.domain.port.outbound;

import dev.ime.domain.model.Event;

public interface PublisherPort {
	
	void publishEvent(Event event);
}
