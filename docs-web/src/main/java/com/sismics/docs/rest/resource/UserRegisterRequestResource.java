package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.UserRegisterRequestDao;
import com.sismics.docs.core.dao.dto.UserRegisterRequestDto;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserRegisterRequest;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.JsonUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * User registration request REST resources.
 * 
 * @author claude
 */
@Path("/user/register")
public class UserRegisterRequestResource extends BaseResource {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(UserRegisterRequestResource.class);
    
    /**
     * Creates a new user registration request.
     *
     * @api {put} /user/register Create a new user registration request
     * @apiName PutUserRegisterRequest
     * @apiGroup UserRegisterRequest
     * @apiParam {String{3..50}} username Username
     * @apiParam {String{8..50}} password Password
     * @apiParam {String{1..100}} email E-mail
     * @apiParam {String{0..1000}} message Message to the administrator
     * @apiSuccess {String} status Status OK
     * @apiError (client) ValidationError Validation error
     * @apiError (client) AlreadyExistingUsername Username already exists
     * @apiError (server) UnknownError Unknown server error
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @param username User's username
     * @param password Password
     * @param email E-Mail
     * @param message Message to the administrator
     * @return Response
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email,
            @FormParam("message") String message) {
        
        // 基本长度验证
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 1, 100);
        message = ValidationUtil.validateLength(message, "message", 0, 1000, true);
        
        // 创建注册请求
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername(username);
        
        // 直接使用UserDao的hashPassword方法
        UserDao userDao = new UserDao();
        request.setPassword(userDao.hashPassword(password));
        
        request.setEmail(email);
        request.setMessage(message);
        
        // 创建请求
        UserRegisterRequestDao requestDao = new UserRegisterRequestDao();
        requestDao.create(request);
        
        // 返回成功
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns all pending user registration requests.
     *
     * @api {get} /user/register/pending Get all pending user registration requests
     * @apiName GetUserRegisterRequestPending
     * @apiGroup UserRegisterRequest
     * @apiSuccess {Object[]} requests List of pending requests
     * @apiSuccess {String} requests.id ID
     * @apiSuccess {String} requests.username Username
     * @apiSuccess {String} requests.email E-mail
     * @apiSuccess {Number} requests.create_date Create date (timestamp)
     * @apiSuccess {String} requests.message Message from the requester
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    @Path("pending")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPendingRequests() {
        if (!authenticate() || !hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ForbiddenClientException();
        }
        
        UserRegisterRequestDao requestDao = new UserRegisterRequestDao();
        List<UserRegisterRequestDto> requestDtoList = requestDao.findAllPending();
        
        JsonArrayBuilder requests = Json.createArrayBuilder();
        for (UserRegisterRequestDto requestDto : requestDtoList) {
            requests.add(Json.createObjectBuilder()
                    .add("id", requestDto.getId())
                    .add("username", requestDto.getUsername())
                    .add("email", requestDto.getEmail())
                    .add("create_date", requestDto.getCreateTimestamp())
                    .add("message", JsonUtil.nullable(requestDto.getMessage())));
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("requests", requests);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Approves a user registration request.
     *
     * @api {post} /user/register/approve/:id Approve a user registration request
     * @apiName PostUserRegisterRequestApprove
     * @apiGroup UserRegisterRequest
     * @apiParam {String} id Request ID
     * @apiParam {String{0..1000}} response Response message to the user
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) RequestNotFound Request not found
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param id Request ID
     * @param responseMessage Response message to the user
     * @return Response
     */
    @POST
    @Path("approve/{id: [a-zA-Z0-9-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response approve(
            @PathParam("id") String id,
            @FormParam("response") String responseMessage) {
        if (!authenticate() || !hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ForbiddenClientException();
        }
        
        // 验证输入数据
        responseMessage = ValidationUtil.validateLength(responseMessage, "response", 0, 1000, true);
        
        // 获取注册请求
        UserRegisterRequestDao requestDao = new UserRegisterRequestDao();
        UserRegisterRequest request = requestDao.getById(id);
        if (request == null || request.getDeleteDate() != null) {
            throw new ClientException("RequestNotFound", "Request not found");
        }
        
        // 检查请求状态
        if (!"PENDING".equals(request.getStatus())) {
            throw new ClientException("RequestAlreadyProcessed", "This request has already been processed");
        }
        
        // 创建用户
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 使用已经哈希过的密码
        user.setEmail(request.getEmail());
        user.setRoleId("user"); // 默认角色
        user.setCreateDate(new Date());
        user.setPrivateKey(EncryptionUtil.generatePrivateKey());
        user.setStorageCurrent(0L);
        user.setStorageQuota(0L);
        user.setOnboarding(true);
        
        // 保存用户
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(user);
        
        // 更新请求状态
        requestDao.process(id, "APPROVED", principal.getId(), responseMessage);
        
        // 创建审计日志
        AuditLogUtil.create(user, AuditLogType.CREATE, principal.getId());
        
        // 返回成功
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Rejects a user registration request.
     *
     * @api {post} /user/register/reject/:id Reject a user registration request
     * @apiName PostUserRegisterRequestReject
     * @apiGroup UserRegisterRequest
     * @apiParam {String} id Request ID
     * @apiParam {String{0..1000}} response Response message to the user
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) RequestNotFound Request not found
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param id Request ID
     * @param responseMessage Response message to the user
     * @return Response
     */
    @POST
    @Path("reject/{id: [a-zA-Z0-9-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reject(
            @PathParam("id") String id,
            @FormParam("response") String responseMessage) {
        if (!authenticate() || !hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ForbiddenClientException();
        }
        
        // Validate the input data
        responseMessage = ValidationUtil.validateLength(responseMessage, "response", 0, 1000, true);
        
        // Get the request
        UserRegisterRequestDao requestDao = new UserRegisterRequestDao();
        UserRegisterRequest request = requestDao.getById(id);
        if (request == null || request.getDeleteDate() != null) {
            throw new ClientException("RequestNotFound", "Request not found");
        }
        
        // Check if the request is already processed
        if (!"PENDING".equals(request.getStatus())) {
            throw new ClientException("RequestAlreadyProcessed", "This request has already been processed");
        }
        
        // Update the request status
        requestDao.process(id, "REJECTED", principal.getId(), responseMessage);
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns all user registration requests.
     *
     * @api {get} /user/register Get all user registration requests
     * @apiName GetUserRegisterRequest
     * @apiGroup UserRegisterRequest
     * @apiParam {Number} offset Offset
     * @apiParam {Number} limit Limit
     * @apiSuccess {Object[]} requests List of requests
     * @apiSuccess {String} requests.id ID
     * @apiSuccess {String} requests.username Username
     * @apiSuccess {String} requests.email E-mail
     * @apiSuccess {String} requests.status Status (PENDING, APPROVED, REJECTED)
     * @apiSuccess {Number} requests.create_date Create date (timestamp)
     * @apiSuccess {Number} requests.process_date Process date (timestamp)
     * @apiSuccess {String} requests.message Message from the requester
     * @apiSuccess {String} requests.response Response from the admin
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param offset Offset
     * @param limit Limit
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequests(
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit) {
        if (!authenticate() || !hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ForbiddenClientException();
        }
        
        // Validate the input data
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = 50;
        }
        
        UserRegisterRequestDao requestDao = new UserRegisterRequestDao();
        List<UserRegisterRequestDto> requestDtoList = requestDao.findAll(offset, limit);
        
        JsonArrayBuilder requests = Json.createArrayBuilder();
        for (UserRegisterRequestDto requestDto : requestDtoList) {
            JsonObjectBuilder request = Json.createObjectBuilder()
                    .add("id", requestDto.getId())
                    .add("username", requestDto.getUsername())
                    .add("email", requestDto.getEmail())
                    .add("status", requestDto.getStatus())
                    .add("create_date", requestDto.getCreateTimestamp());
            
            // Add optional fields
            if (requestDto.getProcessTimestamp() != null) {
                request.add("process_date", requestDto.getProcessTimestamp());
            } else {
                request.add("process_date", JsonValue.NULL);
            }
            
            if (requestDto.getMessage() != null) {
                request.add("message", requestDto.getMessage());
            } else {
                request.add("message", JsonValue.NULL);
            }
            
            if (requestDto.getResponse() != null) {
                request.add("response", requestDto.getResponse());
            } else {
                request.add("response", JsonValue.NULL);
            }
            
            requests.add(request);
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("requests", requests);
        return Response.ok().entity(response.build()).build();
    }
} 