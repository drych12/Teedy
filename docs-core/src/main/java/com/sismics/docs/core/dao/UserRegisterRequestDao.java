package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.dto.UserRegisterRequestDto;
import com.sismics.docs.core.model.jpa.UserRegisterRequest;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * User Registration Request DAO.
 * 
 * @author claude
 */
public class UserRegisterRequestDao {
    /**
     * Creates a new user registration request.
     * 
     * @param userRegisterRequest User registration request
     * @return New ID
     */
    public String create(UserRegisterRequest userRegisterRequest) {
        // Create the UUID
        userRegisterRequest.setId(UUID.randomUUID().toString());
        
        // Create the request
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        userRegisterRequest.setCreateDate(new Date());
        userRegisterRequest.setStatus("PENDING");
        em.persist(userRegisterRequest);
        
        return userRegisterRequest.getId();
    }
    
    /**
     * Returns a user registration request by ID.
     * 
     * @param id User registration request ID
     * @return User registration request
     */
    public UserRegisterRequest getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(UserRegisterRequest.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Returns a request by username.
     * 
     * @param username Username
     * @return User registration request
     */
    public UserRegisterRequest getByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select r from UserRegisterRequest r where r.username = :username and r.deleteDate is null");
            q.setParameter("username", username);
            return (UserRegisterRequest) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Process a user registration request (approve or reject).
     * 
     * @param id User registration request ID
     * @param status New status (APPROVED or REJECTED)
     * @param userId User ID who processed the request
     * @param response Admin response message
     */
    public void process(String id, String status, String userId, String response) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the request
        Query q = em.createQuery("select r from UserRegisterRequest r where r.id = :id and r.deleteDate is null");
        q.setParameter("id", id);
        UserRegisterRequest request = (UserRegisterRequest) q.getSingleResult();
        
        // Verify that the user exists
        UserDao userDao = new UserDao();
        User user = userDao.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        // Update the request
        request.setStatus(status);
        request.setUserId(userId);
        request.setProcessDate(new Date());
        request.setDeleteDate(new Date());
        if (response != null) {
            request.setResponse(response);
        }
        
        // Create audit log
        AuditLogUtil.create(request, AuditLogType.UPDATE, userId);
    }
    
    /**
     * Delete a user registration request.
     * 
     * @param id User registration request ID
     * @param userId User ID who deletes the request
     */
    public void delete(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the request
        Query q = em.createQuery("select r from UserRegisterRequest r where r.id = :id and r.deleteDate is null");
        q.setParameter("id", id);
        UserRegisterRequest request = (UserRegisterRequest) q.getSingleResult();
        
        // Delete the request
        request.setDeleteDate(new Date());
        
        // Create audit log
        AuditLogUtil.create(request, AuditLogType.DELETE, userId);
    }
    
    /**
     * Returns all pending user registration requests.
     * 
     * @return List of user registration requests
     */
    public List<UserRegisterRequestDto> findAllPending() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the requests
        Query q = em.createQuery("select r from UserRegisterRequest r where r.status = 'PENDING' and r.deleteDate is null and r.processDate is null order by r.createDate asc");
        @SuppressWarnings("unchecked")
        List<UserRegisterRequest> requestList = q.getResultList();
        
        // Assemble results
        List<UserRegisterRequestDto> requestDtoList = new ArrayList<>();
        for (UserRegisterRequest request : requestList) {
            UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
            requestDto.setId(request.getId());
            requestDto.setUsername(request.getUsername());
            requestDto.setEmail(request.getEmail());
            requestDto.setCreateTimestamp(request.getCreateDate().getTime());
            requestDto.setMessage(request.getMessage());
            requestDto.setStatus(request.getStatus());
            requestDtoList.add(requestDto);
        }
        
        return requestDtoList;
    }
    
    /**
     * Returns all user registration requests.
     * 
     * @param offset Offset
     * @param limit Limit
     * @return List of user registration requests
     */
    public List<UserRegisterRequestDto> findAll(int offset, int limit) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the requests
        Query q = em.createQuery("select r from UserRegisterRequest r where r.deleteDate is null order by r.createDate desc");
        q.setFirstResult(offset);
        q.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<UserRegisterRequest> requestList = q.getResultList();
        
        // Assemble results
        List<UserRegisterRequestDto> requestDtoList = new ArrayList<>();
        for (UserRegisterRequest request : requestList) {
            UserRegisterRequestDto requestDto = new UserRegisterRequestDto();
            requestDto.setId(request.getId());
            requestDto.setUsername(request.getUsername());
            requestDto.setEmail(request.getEmail());
            requestDto.setCreateTimestamp(request.getCreateDate().getTime());
            requestDto.setProcessTimestamp(request.getProcessDate() != null ? request.getProcessDate().getTime() : null);
            requestDto.setAdminId(request.getUserId());
            requestDto.setMessage(request.getMessage());
            requestDto.setResponse(request.getResponse());
            requestDto.setStatus(request.getStatus());
            requestDtoList.add(requestDto);
        }
        
        return requestDtoList;
    }
} 