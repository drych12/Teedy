package com.sismics.docs.core.dao.dto;

import com.google.common.base.MoreObjects;

/**
 * User Registration Request DTO.
 *
 * @author claude
 */
public class UserRegisterRequestDto {
    /**
     * Request ID.
     */
    private String id;
    
    /**
     * Username.
     */
    private String username;
    
    /**
     * Email address.
     */
    private String email;
    
    /**
     * Creation date of this request.
     */
    private Long createTimestamp;
    
    /**
     * Processing date of this request.
     */
    private Long processTimestamp;
    
    /**
     * Admin user ID who processed the request.
     */
    private String adminId;
    
    /**
     * Request message.
     */
    private String message;
    
    /**
     * Admin response message.
     */
    private String response;
    
    /**
     * Status.
     */
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Long getProcessTimestamp() {
        return processTimestamp;
    }

    public void setProcessTimestamp(Long processTimestamp) {
        this.processTimestamp = processTimestamp;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("email", email)
                .add("status", status)
                .toString();
    }
} 