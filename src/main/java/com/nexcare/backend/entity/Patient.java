package com.nexcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Table(name="patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId;

    @Setter
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;


    @Setter
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Setter
    private LocalDate dateOfBirth;

    @Setter
    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;

    @Setter
    private String address;

    @Setter
    private String emergencyContact;

    @Setter
    private Double height;

    @Setter
    private Double weight;

}
