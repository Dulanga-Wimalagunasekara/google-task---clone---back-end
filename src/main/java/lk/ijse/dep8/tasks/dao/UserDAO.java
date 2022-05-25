package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    User saveUser(User user);

    void deleteUserById(String userId);

    Optional<User> findUserById(String userId);

    Optional<User> findUserByIdOrEmail(String userIdOrEmail);

    boolean existsUserById(String userId);

    boolean existsUserEmailOrUserId(String emailOrId);

    List<User> findAllUsers();

    long countUsers();
}
