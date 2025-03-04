package dev.ime.common.config;

public class GlobalConstants {

	private GlobalConstants() {
		super();
	}

	// Values
	public static final Long EXPIRATION_TIME = 3600L;

	// Patterns
	public static final String PATTERN_NAME_FULL = "^[a-zA-Z0-9ñÑáéíóúÁÉÍÓÚ][a-zA-Z0-9ñÑáéíóúÁÉÍÓÚ\\s\\-\\.&,:]{1,49}$";
	public static final String PATTERN_DESC_FULL = "^[a-zA-Z0-9ñÑáéíóúÁÉÍÓÚ][a-zA-Z0-9ñÑáéíóúÁÉÍÓÚ\\s\\-\\.&,:]{1,127}$";
	public static final String PATTERN_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

	// Messages
	public static final String MSG_EMPTYTOKEN = "WTF Empty Token";
	public static final String MSG_PATTERN_SEVERE = "### [** eXception **] -> [{}] : [{}] ###";
	public static final String MSG_PATTERN_INFO = "### [{}] : [{}] ###";
	public static final String MSG_NODATA = "No data available";
	public static final String MSG_ENTITY_ALREADY = "Entity already in db";
	public static final String MSG_JWTNOTFOUND = "No JWT authentication found";
	public static final String MSG_CONRESULT = "Conection to client result";
	public static final String MSG_UNSUP_REQ = "Unsupported request type";
	public static final String MSG_PUBLISH_EVENT = "Publishing Event";	
	public static final String MSG_PUBLISH_OK = "Publish Event Succesfully";	
	public static final String MSG_PUBLISH_FAIL = "Publish Event Failed";		
	public static final String MSG_TESTGRPC_NOTINTIME = "The call did not terminate in time";				

	// Models
	public static final String USER_CAT = "User";
	public static final String USER_CAT_DB = "users";
	public static final String USER_ID = "userId";
	public static final String USER_ID_DB = "user_id";
	public static final String USER_EMAIL = "email";
	public static final String USER_PASS = "password";
	public static final String USER_ROLE = "role";
	public static final String USERAPP_CAT = "UserApp";
	public static final String USERAPP_CAT_DB = "usersapp";
	public static final String USERAPP_ID = "userAppId";
	public static final String USERAPP_ID_DB = "user_app_id";
	public static final String USERAPP_EMAIL = "email";
	public static final String USERAPP_NAME = "name";
	public static final String USERAPP_LASTNAME = "lastname";
	public static final String CAT_CAT = "Category";
	public static final String CAT_CAT_DB = "categories";
	public static final String CAT_ID = "categoryId";
	public static final String CAT_ID_DB = "category_id";
	public static final String CAT_NAME = "categoryName";
	public static final String CAT_NAME_DB = "category_name";
	public static final String PROD_CAT = "Product";
	public static final String PROD_CAT_DB = "products";
	public static final String PROD_ID = "productId";
	public static final String PROD_ID_DB = "product_id";
	public static final String PROD_NAME = "productName";
	public static final String PROD_NAME_DB = "product_name";
	public static final String PROD_DESC = "productDescription";
	public static final String PROD_DESC_DB = "product_description";
	public static final String REV_CAT = "Review";
	public static final String REV_CAT_DB = "reviews";
	public static final String REV_ID = "reviewId";
	public static final String REV_ID_DB = "review_id";
	public static final String REV_TXT = "reviewText";
	public static final String REV_TXT_DB = "review_text";
	public static final String REV_RAT = "rating";
	public static final String VOT_CAT = "Vote";
	public static final String VOT_CAT_DB = "votes";
	public static final String VOT_ID = "voteId";
	public static final String VOT_ID_DB = "vote_id";
	public static final String VOT_US = "useful";
	
	// Orders
	public static final String CREATE_USER = "create.user";
	public static final String UPDATED_USER = "update.user";
	public static final String DELETE_USER = "delete.user";
	public static final String CREATE_USERAPP = "create.userapp";
	public static final String UPDATE_USERAPP = "update.userapp";
	public static final String DELETE_USERAPP = "delete.userapp";
	public static final String CREATE_CAT = "create.category";
	public static final String UPDATE_CAT = "update.category";
	public static final String DELETE_CAT = "delete.category";
	public static final String CREATE_PROD = "create.product";
	public static final String UPDATE_PROD = "update.product";
	public static final String DELETE_PROD = "delete.product";
	public static final String CREATE_REV = "create.review";
	public static final String UPDATE_REV = "update.review";
	public static final String DELETE_REV = "delete.review";
	public static final String CREATE_VOT = "create.vote";
	public static final String UPDATE_VOT = "update.vote";
	public static final String DELETE_VOT = "delete.vote";
	
	// Topics
	public static final String USER_CREATED = "user.created";
	public static final String USER_UPDATED = "user.updated";
	public static final String USER_DELETED = "user.deleted";
	public static final String USERAPP_CREATED = "userapp.created";
	public static final String USERAPP_UPDATED = "userapp.updated";
	public static final String USERAPP_DELETED = "userapp.deleted";
	public static final String CAT_CREATED = "category.created";
	public static final String CAT_UPDATED = "category.updated";
	public static final String CAT_DELETED = "category.deleted";
	public static final String PROD_CREATED = "product.created";
	public static final String PROD_UPDATED = "product.updated";
	public static final String PROD_DELETED = "product.deleted";
	public static final String REV_CREATED = "review.created";
	public static final String REV_UPDATED = "review.updated";
	public static final String REV_DELETED = "review.deleted";
	public static final String VOT_CREATED = "vote.created";
	public static final String VOT_UPDATED = "vote.updated";
	public static final String VOT_DELETED = "vote.deleted";

	// Exceptions
	public static final String EX_BASIC = "BasicException";
	public static final String EX_USERNOTFOUND = "UsernameNotFoundException";
	public static final String EX_VALIDATION = "ValidationException";
	public static final String EX_VALIDATION_DESC = "Kernel Panic in validation process";
	public static final String EX_PLAIN = "Exception";
	public static final String EX_PLAIN_DESC = "Exception friendly";
	public static final String EX_EMPTYRESPONSE = "EmptyResponseException";
	public static final String EX_EMPTYRESPONSE_DESC = "No freak out, just an Empty Response";
	public static final String EX_EMAILUSED = "EmailUsedException";
	public static final String EX_EMAILUSED_DESC = "Email already in use";
	public static final String EX_EMAILNOTCHANGE = "EmailNotChageException";
	public static final String EX_EMAILNOTCHANGE_DESC = "Email change unvailable";
	public static final String EX_NORESOURCE = "NoResourceFoundException";
	public static final String EX_NORESOURCE_DESC = "No Resource Found Exception";
	public static final String EX_GRPCCLIENTCOM = "GrpcClientCommunicationException";
	public static final String EX_GRPCCLIENTCOM_DESC = "Grpc Client Communication Exception";
	public static final String EX_CREATEJPAENTITY = "CreateJpaEntityException";
	public static final String EX_CREATEJPAENTITY_DESC = "Exception while creation a JPA entity for saving to sql db";
	public static final String EX_RESOURCENOTFOUND = "ResourceNotFoundException";
	public static final String EX_RESOURCENOTFOUND_DESC = "The resource has not been found.";
	public static final String EX_JWTTOKENEMAILRESTRICTION = "JwtTokenEmailRestriction";
	public static final String EX_JWTTOKENEMAILRESTRICTION_DESC = "Jwt Token Email different";
	public static final String EX_ENTITYASSOCIATED = "EntityAssociatedException";
	public static final String EX_ENTITYASSOCIATED_DESC = "Some entity is still associated in the element";
	public static final String EX_UNIQUEVALUE = "UniqueValueException";
	public static final String EX_UNIQUEVALUE_DESC = "Unique Value constraint infringed";
	public static final String EX_ONLYONEVOTE = "OnlyOneVotePerUserInReviewException";
	public static final String EX_ONLYONEVOTE_DESC = "Only One Vote Per User In Review constraint exceed";
	public static final String EX_ONLYONEREV = "OnlyOneReviewPerUserInProductException";
	public static final String EX_ONLYONEREV_DESC = "Only One Review Per User In Product constraint exceed";

	// Paging and Sorting
	public static final String PS_PAGE = "page";
	public static final String PS_SIZE = "size";
	public static final String PS_BY = "sortBy";
	public static final String PS_DIR = "sortDir";
	public static final String PS_A = "ASC";
	public static final String PS_D = "DESC";
	
	// Others
	public static final String JWT_USER = "sub";
	public static final String OBJ_FIELD = "field";
	public static final String OBJ_VALUE = "value";
	public static final String OBJ_R = "reason";

}
