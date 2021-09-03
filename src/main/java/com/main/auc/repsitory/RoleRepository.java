package com.main.auc.repsitory;

import com.main.auc.models.ERole;
import com.main.auc.models.Role;
import com.main.auc.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.Optional;

@Transactional
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);
}
