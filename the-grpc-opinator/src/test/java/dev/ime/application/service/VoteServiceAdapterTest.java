package dev.ime.application.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import dev.ime.application.dto.VoteDto;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.EventMapper;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.model.Event;
import dev.ime.domain.model.Vote;
import dev.ime.domain.port.inbound.AuthorizationServicePort;
import dev.ime.domain.port.outbound.GenericRepositoryPort;
import dev.ime.domain.port.outbound.PublisherPort;

@ExtendWith(MockitoExtension.class)
class VoteServiceAdapterTest {

	@Mock
	private GenericRepositoryPort<Vote> voteRepositoryAdapter;
	@Mock
	private VoteMapper voteMapper;
	@Mock
	private EventMapper eventMapper;
	@Mock
	private AuthorizationServicePort authorizationServiceAdapter;
	@Mock
	private PublisherPort publisherAdapter;

	@InjectMocks
	private VoteServiceAdapter voteServiceAdapter;

	private VoteDto voteDto;
	private Vote vote;
	private Event event;	
	
	private final Long voteId = 1L;
	private final String email = "email@email.tk";
	private final Long reviewId = 1L;
	private final boolean useful = true;

	private UUID eventId;
	private final String eventCategory = GlobalConstants.REV_CAT;
	private final String eventType = GlobalConstants.REV_DELETED;
	private final Instant eventTimestamp = Instant.now();
	private final Map<String, Object> eventData = new HashMap<>();

	private final Pageable pageable = PageRequest.of(0, 10, Sort.by(GlobalConstants.REV_ID).ascending());
		
	@BeforeEach
	private void setUp(){
		
		voteDto = new VoteDto(voteId,email,reviewId,useful);
		
		vote = new Vote();
		vote.setVoteId(voteId);
		vote.setEmail(email);
		vote.setReview(null);
		vote.setUseful(useful);
		
		eventId = UUID.randomUUID();
		
		event = new Event(
				eventId,
				eventCategory,
				eventType,
				eventTimestamp,
				eventData);				
	}

	@Test
	void findAll_shouldReturnList() {
		
		Mockito.when(voteRepositoryAdapter.findAll()).thenReturn(List.of(vote));
		Mockito.when(voteMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(voteDto));
		
		List<VoteDto> list = voteServiceAdapter.findAll();
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void findAll_withPageable_shouldReturnList() {
		
		Mockito.when(voteRepositoryAdapter.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(vote));
		Mockito.when(voteMapper.fromListDomainToListDto(Mockito.anyList())).thenReturn(List.of(voteDto));
		
		List<VoteDto> list = voteServiceAdapter.findAll(pageable);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);		
	}

	@Test
	void findById_shouldReturnOptDto() {
		
		Mockito.when(voteRepositoryAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(vote));
		Mockito.when(voteMapper.fromDomainToDto(Mockito.any(Vote.class))).thenReturn(voteDto);
		
		Optional<VoteDto> optResult =  voteServiceAdapter.findById(voteId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().voteId()).isEqualTo(voteId)
		);
	}

	@Test
	void save_shouldReturnOptDto() {
		
		Mockito.when(voteMapper.fromDtoToDomain(Mockito.any(VoteDto.class))).thenReturn(vote);
		Mockito.when(authorizationServiceAdapter.getJwtTokenEmail()).thenReturn(email);
		Mockito.when(voteRepositoryAdapter.save(Mockito.any(Vote.class))).thenReturn(Optional.of(vote));
		Mockito.when(voteMapper.fromDomainToDto(Mockito.any(Vote.class))).thenReturn(voteDto);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(VoteDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));

		Optional<VoteDto> optResult =  voteServiceAdapter.save(voteDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().voteId()).isEqualTo(voteId)
		);
	}

	@Test
	void update_shouldReturnOptDto() {
		
		Mockito.when(voteRepositoryAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(vote));
		Mockito.doNothing().when(authorizationServiceAdapter).checkJwtTokenOwner(Mockito.anyString());
		Mockito.when(voteMapper.fromDtoToDomain(Mockito.any(VoteDto.class))).thenReturn(vote);
		Mockito.when(voteRepositoryAdapter.update(Mockito.any(Vote.class))).thenReturn(Optional.of(vote));
		Mockito.when(voteMapper.fromDomainToDto(Mockito.any(Vote.class))).thenReturn(voteDto);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(VoteDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));

		Optional<VoteDto> optResult =  voteServiceAdapter.update(voteDto);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(optResult).isNotNull(),
				()-> Assertions.assertThat(optResult.get().voteId()).isEqualTo(voteId)
		);
	}

	@Test
	void deleteById_shouldReturnTrue() {
		
		Mockito.when(voteRepositoryAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(vote));
		Mockito.doNothing().when(authorizationServiceAdapter).checkJwtTokenOwner(Mockito.anyString());

		Mockito.when(voteRepositoryAdapter.deleteById(Mockito.anyLong())).thenReturn(true);
		Mockito.when(eventMapper.createEvent(Mockito.anyString(),Mockito.anyString(),Mockito.any(VoteDto.class))).thenReturn(event);
		Mockito.doNothing().when(publisherAdapter).publishEvent(Mockito.any(Event.class));
		
		boolean result = voteServiceAdapter.deleteById(voteId);
		
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
		);		
	}

}
