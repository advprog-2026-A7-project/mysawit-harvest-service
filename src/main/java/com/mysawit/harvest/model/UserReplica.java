package com.mysawit.harvest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_replicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReplica {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "role", length = 32)
    private String role;

    @Column(name = "mandor_id")
    private UUID mandorId;
}
