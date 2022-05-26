package lk.ijse.dep8.tasks.service.custome;

import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.service.SuperService;

import javax.servlet.http.Part;
import java.sql.Connection;
import java.sql.SQLException;

public interface UserService extends SuperService{
    boolean existsUser(String emailOrId);

    UserDTO registerUser(Part picture, String appLocation, UserDTO user);

    UserDTO getUser(String emailOrId);

    void deleteUser(String id, String appLocation);

    void updateUser(UserDTO user, Part picture, String appLocation);
}
