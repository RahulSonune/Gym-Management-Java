package com.fitlife.gym.domain.repository;

import com.fitlife.gym.domain.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
