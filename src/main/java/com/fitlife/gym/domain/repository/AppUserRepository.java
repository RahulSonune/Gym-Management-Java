package com.fitlife.gym.domain.repository;

import com.fitlife.gym.domain.entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    @EntityGraph(attributePaths = {"organization", "roles"})
    Optional<AppUser> findByEmailAndDeletedAtIsNull(String email);

    @EntityGraph(attributePaths = {"organization", "roles"})
    Optional<AppUser> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = {"organization", "roles"})
    Optional<AppUser> findByIdAndOrganizationIdAndDeletedAtIsNull(Long id, Long organizationId);

    @EntityGraph(attributePaths = {"roles"})
    List<AppUser> findByOrganizationIdAndDeletedAtIsNullOrderByFullNameAsc(Long organizationId);

    boolean existsByOrganizationIdAndEmailAndDeletedAtIsNull(Long organizationId, String email);

    boolean existsByOrganizationIdAndEmailAndDeletedAtIsNullAndIdNot(
            Long organizationId, String email, Long excludeUserId);
}
