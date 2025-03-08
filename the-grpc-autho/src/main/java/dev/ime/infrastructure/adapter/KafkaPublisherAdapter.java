package dev.ime.infrastructure.adapter;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.ime.common.config.GlobalConstants;
import dev.ime.domain.model.Event;
import dev.ime.domain.port.outbound.PublisherPort;

@Service
public class KafkaPublisherAdapter implements PublisherPort {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private static final Logger logger = LoggerFactory.getLogger(KafkaPublisherAdapter.class);

	public KafkaPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
		super();
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	@Transactional("kafkaTransactionManager")
	public void publishEvent(Event event) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.MSG_PUBLISH_EVENT, event);

		CompletableFuture<SendResult<String, Object>> completableFuture = kafkaTemplate
				.send(new ProducerRecord<>(event.getEventType(), event));
		completableFuture.whenComplete((result, ex) -> {
			if (ex == null) {
				handleSuccess(result);
			} else {
				handleFailure(result, ex);
			}
		});
	}

	private void handleSuccess(SendResult<String, Object> result) {
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.MSG_PUBLISH_OK,
				result.getProducerRecord().value());
	}

	private void handleFailure(SendResult<String, Object> result, Throwable ex) {
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.MSG_PUBLISH_FAIL,
				" [" + result.getProducerRecord().value() + "]:[" + ex.getMessage()+ "]");
	}

}
