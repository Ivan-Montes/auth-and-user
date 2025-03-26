package dev.ime.infrastructure.adapter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import dev.ime.common.config.GlobalConstants;
import dev.ime.domain.model.Event;

@ExtendWith(MockitoExtension.class)
class KafkaPublisherAdapterTest {

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	@InjectMocks
	private KafkaPublisherAdapter kafkaPublisherAdapter;

	private Event event;
	private final UUID eventId = UUID.randomUUID();
	private final String eventCategory = GlobalConstants.USER_CAT;
	private final String eventType = GlobalConstants.USER_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();
	private final ProducerRecord<String, Object> producerRecord = new ProducerRecord<>("topic", "key", "value");

	@BeforeEach
	private void setUp() {

		event = new Event(eventId, eventCategory, eventType, eventTimestamp, eventData);
	}

	@Test
	@SuppressWarnings("unchecked")
	void publishEvent_shouldCompleteProcess() {

		CompletableFuture<SendResult<String, Object>> completableFuture = new CompletableFuture<>();
		SendResult<String, Object> sendResult = Mockito.mock(SendResult.class);
		completableFuture.complete(sendResult);
		Mockito.when(kafkaTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(completableFuture);
		Mockito.when(sendResult.getProducerRecord()).thenReturn(producerRecord);

		kafkaPublisherAdapter.publishEvent(event);

		Mockito.verify(kafkaTemplate).send(Mockito.any(ProducerRecord.class));
		Mockito.verify(sendResult).getProducerRecord();
	}

}
