package com.vikrambhat.milestonemaster.common.model;

import jakarta.persistence.*;

import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @PrePersist
    void assignPublicId() {
        if(publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }
    protected Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }


}
