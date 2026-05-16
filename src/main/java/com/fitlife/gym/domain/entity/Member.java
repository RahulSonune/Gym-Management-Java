package com.fitlife.gym.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "member_code", nullable = false, length = 512)
    private String memberCode;

    /** May hold FLENC1: client ciphertext or legacy plaintext. */
    @Column(name = "first_name", nullable = false, length = 512)
    private String firstName;

    @Column(name = "last_name", length = 512)
    private String lastName;

    @Column(length = 512)
    private String gender;

    /** ISO date plaintext or FLENC1: ciphertext from the client. */
    @Column(name = "date_of_birth", length = 512)
    private String dateOfBirth;

    @Column(length = 512)
    private String email;

    @Column(nullable = false, length = 512)
    private String phone;

    @Column(name = "emergency_contact_name", length = 512)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 512)
    private String emergencyContactPhone;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "joined_at")
    private LocalDate joinedAt;

    @Column(length = 512)
    private String source;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getFullName() {
        if (firstName == null) {
            return null;
        }
        if (lastName == null || lastName.isBlank()) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
}
