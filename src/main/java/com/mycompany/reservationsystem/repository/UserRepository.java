package com.mycompany.reservationsystem.repository;

import com.mycompany.reservationsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author formentera
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsernameAndPassword(String username, String password);
    void deleteByUsername(String username);
    boolean existsByPosition(User.Position position);
}
