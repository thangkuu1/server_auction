package com.main.auc.repsitory;

import com.main.auc.models.ERole;
import com.main.auc.models.Role;
import com.main.auc.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Transactional
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndLoginType(String username, String loginType);
    Optional<User> findByUsernameAndIdAuth(String username, String idAuth);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
