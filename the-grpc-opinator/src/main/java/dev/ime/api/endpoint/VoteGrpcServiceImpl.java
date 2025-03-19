package dev.ime.api.endpoint;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.google.protobuf.Empty;

import dev.ime.api.validation.VoteRequestValidator;
import dev.ime.application.dto.PaginationDto;
import dev.ime.application.dto.VoteDto;
import dev.ime.application.exception.EmptyResponseException;
import dev.ime.common.constants.GlobalConstants;
import dev.ime.common.mapper.VoteMapper;
import dev.ime.domain.port.inbound.GenericServicePort;
import dev.proto.CreateVoteRequest;
import dev.proto.DeleteVoteRequest;
import dev.proto.DeleteVoteResponse;
import dev.proto.GetVoteRequest;
import dev.proto.ListVotesResponse;
import dev.proto.PaginationRequest;
import dev.proto.UpdateVoteRequest;
import dev.proto.VoteGrpcServiceGrpc;
import dev.proto.VoteProto;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class VoteGrpcServiceImpl extends VoteGrpcServiceGrpc.VoteGrpcServiceImplBase{

	private final GenericServicePort<VoteDto> voteServiceAdapter;
	private final VoteMapper voteMapper;
	private final VoteRequestValidator requestValidator;
	private static final Logger logger = LoggerFactory.getLogger(VoteGrpcServiceImpl.class);
	
	public VoteGrpcServiceImpl(GenericServicePort<VoteDto> voteServiceAdapter, VoteMapper voteMapper,
			VoteRequestValidator requestValidator) {
		super();
		this.voteServiceAdapter = voteServiceAdapter;
		this.voteMapper = voteMapper;
		this.requestValidator = requestValidator;
	}

	@Override
	public void createVote(CreateVoteRequest request, StreamObserver<VoteProto> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.CREATE_VOT, request);
		
		requestValidator.validateCreateRequest(request);

		VoteProto voteProto = Optional.ofNullable(request)
				.map(voteMapper::fromCreateToDto)
				.flatMap(voteServiceAdapter::save)
				.map(voteMapper::fromDtoToProto)
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.VOT_CREATED, item);
					return item;
				}).orElseThrow(
						() -> new EmptyResponseException(Map.of(GlobalConstants.VOT_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(voteProto);
		responseObserver.onCompleted();
	}

	@Override
	public void updateVote(UpdateVoteRequest request, StreamObserver<VoteProto> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.UPDATE_VOT, request);
		requestValidator.validateUpdateRequest(request);
		VoteDto dto = voteMapper.fromUpdateToDto(request);
		Optional<VoteDto> opt = voteServiceAdapter.update(dto);
		VoteProto voteProto = opt.map(voteMapper::fromDtoToProto).orElseThrow(
				() -> new EmptyResponseException(Map.of(GlobalConstants.VOT_CAT, GlobalConstants.MSG_NODATA)));
		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.VOT_UPDATED, voteProto);

		responseObserver.onNext(voteProto);
		responseObserver.onCompleted();
	}

	@Override
	public void deleteVote(DeleteVoteRequest request, StreamObserver<DeleteVoteResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.DELETE_VOT, request);
		
		requestValidator.validateDeleteRequest(request);
		
		DeleteVoteResponse deleteVoteResponse = Optional.ofNullable(request.getVoteId())
				.map(voteServiceAdapter::deleteById)
				.map(b -> DeleteVoteResponse.newBuilder().setSuccess(b).build())
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.VOT_DELETED, item);
					return item;
				}).orElseThrow(
						() -> new EmptyResponseException(Map.of(GlobalConstants.VOT_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(deleteVoteResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void listVotes(Empty request, StreamObserver<ListVotesResponse> responseObserver) {

		List<VoteProto> protoList = voteMapper.fromListDtoToListProto(voteServiceAdapter.findAll());
		ListVotesResponse listResponse = ListVotesResponse.newBuilder().addAllVotes(protoList).build();

		responseObserver.onNext(listResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void listVotesPaginated(PaginationRequest request, StreamObserver<ListVotesResponse> responseObserver) {

		logger.info(GlobalConstants.MSG_PATTERN_INFO, request.getClass().getSimpleName(), request);
		
		PaginationDto paginationDto = createPaginationDto(request);
		PageRequest pageRequest = createPageable(paginationDto);
		List<VoteProto> protoList = voteMapper.fromListDtoToListProto(voteServiceAdapter.findAll(pageRequest));
		ListVotesResponse listResponse = ListVotesResponse.newBuilder().addAllVotes(protoList).build();

		responseObserver.onNext(listResponse);
		responseObserver.onCompleted();
	}

	private PaginationDto createPaginationDto(PaginationRequest request) {
		
		Set<String> attrsSet = Set.of(GlobalConstants.VOT_ID,GlobalConstants.USER_EMAIL,GlobalConstants.VOT_US);
		Integer page = Optional.ofNullable(request.getPage()).filter( i -> i >= 0).orElse(0);
    	Integer size = Optional.ofNullable(request.getSize()).filter( i -> i > 0).orElse(100);
        String sortBy = Optional.ofNullable(request.getSortBy()).filter(attrsSet::contains).orElse(GlobalConstants.VOT_ID);
        String sortDir = Optional.ofNullable(request.getSortDir())
        		.map(String::toUpperCase)
        		.filter( sorting -> sorting.equals(GlobalConstants.PS_A) || sorting.equals(GlobalConstants.PS_D))
        		.orElse(GlobalConstants.PS_A);
        
        return new PaginationDto(page, size, sortBy, sortDir);
	}
	
	private PageRequest createPageable(PaginationDto paginationDto) {

		Sort sort = Sort.by(Sort.Direction.fromString(paginationDto.sortDir()), paginationDto.sortBy());
		return PageRequest.of(paginationDto.page(), paginationDto.size(), sort);
	}

	@Override
	public void getVote(GetVoteRequest request, StreamObserver<VoteProto> responseObserver) {

		requestValidator.validateGetRequest(request);
		
		VoteProto voteProto = Optional.ofNullable(request.getVoteId())
				.flatMap(voteServiceAdapter::findById)
				.map(voteMapper::fromDtoToProto)
				.map(item -> {
					logger.info(GlobalConstants.MSG_PATTERN_INFO, GlobalConstants.VOT_CAT, item);
					return item;
				})
				.orElseThrow(
				() -> new EmptyResponseException(Map.of(GlobalConstants.VOT_CAT, GlobalConstants.MSG_NODATA)));

		responseObserver.onNext(voteProto);
		responseObserver.onCompleted();
	}
	
}
