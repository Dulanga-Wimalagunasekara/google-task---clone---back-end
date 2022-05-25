package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO extends SuperDAO<User,String> {
    Optional<User> findUserByIdOrEmail(String userIdOrEmail);
    boolean existsUserEmailOrUserId(String emailOrId);

}
