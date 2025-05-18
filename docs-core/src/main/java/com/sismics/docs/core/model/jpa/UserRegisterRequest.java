package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * User registration request entity.
 * 
 * @author claude
 */
@Entity
@Table(name = "T_USER_REGISTER_REQUEST")
public class UserRegisterRequest implements Loggable {
    /**
     * Request ID.
     */
    @Id
    @Column(name = "URR_ID_C", length = 36)
    private String id;
    
    /**
     * User's username.
     */
    @Column(name = "URR_USERNAME_C", nullable = false, length = 50)
    private String username;
    
    /**
     * User's password.
     */
    @Column(name = "URR_PASSWORD_C", nullable = false, length = 60)
    private String password;
    
    /**
     * Email address.
     */
    @Column(name = "URR_EMAIL_C", nullable = false, length = 100)
    private String email;
    
    /**
     * Creation date.
     */
    @Column(name = "URR_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Processing date.
     */
    @Column(name = "URR_PROCESSDATE_D")
    private Date processDate;
    
    /**
     * User ID who processed the request.
     */
    @Column(name = "URR_IDUSER_C", length = 36)
    private String userId;
    
    /**
     * Status.
     */
    @Column(name = "URR_STATUS_C", nullable = false, length = 20)
    private String status;
    
    /**
     * Request message.
     */
    @Column(name = "URR_MESSAGE_C", length = 1000)
    private String message;
    
    /**
     * Admin response message.
     */
    @Column(name = "URR_RESPONSE_C", length = 1000)
    private String response;
    
    /**
     * Deletion date.
     */
    @Column(name = "URR_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public UserRegisterRequest setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserRegisterRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserRegisterRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRegisterRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public UserRegisterRequest setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public UserRegisterRequest setProcessDate(Date processDate) {
        this.processDate = processDate;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public UserRegisterRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UserRegisterRequest setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public UserRegisterRequest setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getResponse() {
        return response;
    }

    public UserRegisterRequest setResponse(String response) {
        this.response = response;
        return this;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    public UserRegisterRequest setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
        return this;
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

    @Override
    public String toMessage() {
        return username;
    }
} 