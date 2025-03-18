package dev.ime.api.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;

import dev.ime.api.validation.VoteRequestValidator;
import dev.ime.application.dto.VoteDto;
import dev.ime.common.config.GlobalConstants;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.proto.CreateVoteRequest;
import dev.proto.DeleteVoteRequest;
import dev.proto.DeleteVoteResponse;
import dev.proto.GetVoteRequest;
import dev.proto.ListVotesResponse;
import dev.proto.PaginationRequest;
import dev.proto.UpdateVoteRequest;
import dev.proto.VoteProto;
import io.grpc.internal.testing.StreamRecorder;

@ExtendWith(MockitoExtension.class)
class VoteGrpcServiceImplTest {

	@Mock
	private GenericServicePort<VoteDto> voteServiceAdapter;
	@Mock
	private VoteMapper voteMapper;
	@Mock
	private VoteRequestValidator requestValidator;

	@InjectMocks
	private VoteGrpcServiceImpl voteGrpcServiceImpl;

	private VoteDto voteDto;
	private VoteProto voteProto;
	
	private final Long voteId = 1L;
	private final String email = "email@email.tk";
	private final Long reviewId = 1L;
	private final boolean useful = true;
	
	@BeforeEach
	private void setUp(){
		
		voteDto = new VoteDto(voteId,email,reviewId,useful);
		voteProto = VoteProto.newBuilder()
				.setVoteId(voteId)
				.setEmail(email)
				.setReviewId(reviewId)
				.setUseful(BoolValue.of(useful))
				.build();				
	}

	@Test
	void createVote_shouldReturnVote() throws Exception {

		CreateVoteRequest request = CreateVoteRequest.newBuilder()
				.setReviewId(reviewId)
				.setUseful(useful)
				.build();
		Mockito.doNothing().when(requestValidator).validateCreateRequest(Mockito.any(CreateVoteRequest.class));
		Mockito.when(voteMapper.fromCreateToDto(Mockito.any(CreateVoteRequest.class))).thenReturn(voteDto);
		Mockito.when(voteServiceAdapter.save(Mockito.any(VoteDto.class))).thenReturn(Optional.of(voteDto));
		Mockito.when(voteMapper.fromDtoToProto(Mockito.any(VoteDto.class))).thenReturn(voteProto);
		StreamRecorder<VoteProto> responseObserver = StreamRecorder.create();
		
		voteGrpcServiceImpl.createVote(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<VoteProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        VoteProto votP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(votP).isNotNull()
				);
	}

	@Test
	void updateVote_shouldReturnVote() throws Exception {

		UpdateVoteRequest request = UpdateVoteRequest.newBuilder()
				.setVoteId(voteId)
				.setUseful(useful)
				.build();
		Mockito.doNothing().when(requestValidator).validateUpdateRequest(Mockito.any(UpdateVoteRequest.class));
		Mockito.when(voteMapper.fromUpdateToDto(Mockito.any(UpdateVoteRequest.class))).thenReturn(voteDto);
		Mockito.when(voteServiceAdapter.update(Mockito.any(VoteDto.class))).thenReturn(Optional.of(voteDto));
		Mockito.when(voteMapper.fromDtoToProto(Mockito.any(VoteDto.class))).thenReturn(voteProto);
		StreamRecorder<VoteProto> responseObserver = StreamRecorder.create();
		
		voteGrpcServiceImpl.updateVote(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<VoteProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        VoteProto votP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(votP).isNotNull()
				);
	}
	
	@Test
	void deleteVote_shouldReturnTrue() throws Exception {

		DeleteVoteRequest request = DeleteVoteRequest.newBuilder()
				.setVoteId(voteId)
				.build();
		Mockito.doNothing().when(requestValidator).validateDeleteRequest(Mockito.any(DeleteVoteRequest.class));
		Mockito.when(voteServiceAdapter.deleteById(Mockito.anyLong())).thenReturn(true);
		StreamRecorder<DeleteVoteResponse> responseObserver = StreamRecorder.create();
		
		voteGrpcServiceImpl.deleteVote(request, responseObserver);		

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }

        assertNull(responseObserver.getError());
        List<DeleteVoteResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        DeleteVoteResponse response = results.get(0);
		boolean result = response.getSuccess();
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(result).isTrue()
				);		
	}

	@Test
	void listVotes_shouldReturnList() throws Exception {
		
		Mockito.when(voteServiceAdapter.findAll()).thenReturn(List.of(voteDto));
		Mockito.when(voteMapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(voteProto));
		StreamRecorder<ListVotesResponse> responseObserver = StreamRecorder.create();
		
		voteGrpcServiceImpl.listVotes(Empty.getDefaultInstance(), responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListVotesResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListVotesResponse response = results.get(0);
        List<VoteProto> list = response.getVotesList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);	
	}	

	@Test
	void listVotesPaginated_shouldReturnList() throws Exception {
		PaginationRequest paginationRequest = PaginationRequest.newBuilder()
				.setPage(0)
				.setSize(2)
				.setSortBy(GlobalConstants.VOT_ID)
				.setSortDir("DESC")
				.build();
		Mockito.when(voteServiceAdapter.findAll(Mockito.any(Pageable.class))).thenReturn(List.of(voteDto));
		Mockito.when(voteMapper.fromListDtoToListProto(Mockito.anyList())).thenReturn(List.of(voteProto));
		StreamRecorder<ListVotesResponse> responseObserver = StreamRecorder.create();
		
		voteGrpcServiceImpl.listVotesPaginated(paginationRequest, responseObserver);
        
		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
			fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<ListVotesResponse> results = responseObserver.getValues();
        assertEquals(1, results.size());
        ListVotesResponse response = results.get(0);
        List<VoteProto> list = response.getVotesList();    	
		org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(list).isNotNull(),
				()-> Assertions.assertThat(list).hasSize(1)
				);	
	}		

	@Test
	void getVote_shouldReturnVote() throws Exception {

		GetVoteRequest request = GetVoteRequest.newBuilder()
				.setVoteId(voteId)
				.build();
		Mockito.doNothing().when(requestValidator).validateGetRequest(Mockito.any(GetVoteRequest.class));
		Mockito.when(voteServiceAdapter.findById(Mockito.anyLong())).thenReturn(Optional.of(voteDto));
		Mockito.when(voteMapper.fromDtoToProto(Mockito.any(VoteDto.class))).thenReturn(voteProto);
		StreamRecorder<VoteProto> responseObserver = StreamRecorder.create();
		
		voteGrpcServiceImpl.getVote(request, responseObserver);

		if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail(GlobalConstants.MSG_TESTGRPC_NOTINTIME);
        }
        assertNull(responseObserver.getError());
        List<VoteProto> results = responseObserver.getValues();
        assertEquals(1, results.size());
        VoteProto votP = results.get(0);
        org.junit.jupiter.api.Assertions.assertAll(
				()-> Assertions.assertThat(votP).isNotNull()
				);
	}			
	

}
