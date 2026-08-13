package com.nexcare.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;

@Entity
@Table(name="users")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String firstName;
    private String lastName;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(name="password_hash")
    private String passwordHash;

    @Column(name="phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name= "role")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private UserStatus status;

    @CreationTimestamp
    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    //setters

    public void setEmail(String email){
        this.email = email;
    }
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    public void  setLastName(String  lastName){
        this.lastName = lastName;
    }
    public void setPasswordHash(String passwordHash){
        this.passwordHash = passwordHash;
    }
    public void setRole(Role role){
        this.role = role;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber =  phoneNumber;
    }
    public void setStatus(UserStatus status){
        this.status = status;
    }
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
    }
    public void setDeletedAt(LocalDateTime deletedAt){
        this.deletedAt = deletedAt;
    }

    //getters
    public Long getId(){
        return id;
    }
    public String getFirstName(){
        return firstName;
    }

    public String getLastName(){
        return lastName;
    }
    public String getPasswordHash(){
        return  passwordHash;
    }
    public String getEmail(){
        return email;
    }
    public Role getRole(){
        return role;
    }
    public UserStatus getStatus(){
        return status;
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public LocalDateTime getUpdatedAt(){
        return updatedAt;
    }
    public LocalDateTime getDeletedAt(){
        return deletedAt;
    }

}