package lk.ijse.dep8.tasks.dao.custome;

import lk.ijse.dep8.tasks.dao.CrudDAO;
import lk.ijse.dep8.tasks.entity.User;

import java.util.Optional;

public interface UserDAO extends CrudDAO<User,String> {
    Optional<User> findUserByIdOrEmail(String userIdOrEmail);
    boolean existsUserEmailOrUserId(String emailOrId);

}
