package com.mysawit.harvest.repository;

import com.mysawit.harvest.model.UserReplica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserReplicaRepository extends JpaRepository<UserReplica, UUID> {
}
