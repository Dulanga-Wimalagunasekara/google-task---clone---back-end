package lk.ijse.dep8.tasks.dao.custome;

import lk.ijse.dep8.tasks.dao.crudDAO;
import lk.ijse.dep8.tasks.entity.User;

import java.util.Optional;

public interface UserDAO extends crudDAO<User,String> {
    Optional<User> findUserByIdOrEmail(String userIdOrEmail);
    boolean existsUserEmailOrUserId(String emailOrId);

}
