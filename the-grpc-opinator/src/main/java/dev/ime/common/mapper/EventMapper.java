package dev.ime.common.mapper;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.ime.domain.model.Event;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventMapper {

	private final ObjectMapper objectMapper;
	
	public <T> Event createEvent(String eventCategory, String eventType, T dto) {		
		
		return new Event(
				eventCategory,
				eventType,
				createEventData(dto)
				);		
	}
	
	private <T> Map<String, Object> createEventData(T dto) {
	    return objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {});
	}

}
