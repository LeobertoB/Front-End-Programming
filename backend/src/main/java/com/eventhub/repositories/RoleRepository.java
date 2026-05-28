package com.eventhub.repositories;

import java.util.Optional;

import com.eventhub.domain.entities.Role;
import com.eventhub.domain.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
