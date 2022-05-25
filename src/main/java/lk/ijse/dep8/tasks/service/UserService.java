package lk.ijse.dep8.tasks.service;

import lk.ijse.dep8.tasks.dao.impl.UserDAOImpl;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.entity.User;
import lk.ijse.dep8.tasks.util.ResponseStatusException;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class UserService {
    private  final Logger logger = Logger.getLogger(UserService.class.getName());
    public  UserDTO registerUser(Connection connection, Part picture,String appLocation,UserDTO user) throws SQLException {
        try {
            connection.setAutoCommit(false);
            user.setId(UUID.randomUUID().toString());
            if (picture!=null){
                user.setPicture(user.getPicture() + user.getId());
            }
            user.setPassword(DigestUtils.sha256Hex(user.getPassword()));
            UserDAOImpl userDAOImpl = new UserDAOImpl(connection);
            User userEntity = new User(user.getId(), user.getEmail(), user.getPassword(), user.getName(), user.getPicture());
            User savedUser = userDAOImpl.saveUser(userEntity);
            user=new UserDTO(savedUser.getId(),savedUser.getFullName(),savedUser.getEmail(),savedUser.getPassword(),savedUser.getProfilePic());
            if (picture != null){
                Path path = Paths.get(appLocation, "uploads");
                if (Files.notExists(path)) {
                    Files.createDirectory(path);
                }
                String picturePath = path.resolve(user.getId()).toAbsolutePath().toString();
                picture.write(picturePath);
            }
            connection.commit();
            return user;
        } catch (Throwable t) {
            connection.rollback();
            throw new RuntimeException(t);
        }finally {
            connection.setAutoCommit(true);
        }
    }

    public  void updateUser(Connection connection,UserDTO user,Part picture,String appLocation)throws SQLException{
        try {
            connection.setAutoCommit(false);
            UserDAOImpl userDAOImpl = new UserDAOImpl(connection);
            Optional<User> userWrapper = userDAOImpl.findUserById(user.getId());
            User oldUserEntity = userWrapper.get();

            oldUserEntity.setPassword(DigestUtils.sha256Hex(user.getPassword()));

            oldUserEntity.setFullName(user.getName());
            oldUserEntity.setProfilePic(user.getPicture());

            userDAOImpl.saveUser(oldUserEntity);

            Path path = Paths.get(appLocation, "uploads");
            Path picturePath = path.resolve(user.getId());
            if (picture != null) {
                if (Files.notExists(path)) {
                    Files.createDirectory(path);
                }

                Files.deleteIfExists(picturePath);
                picture.write(picturePath.toAbsolutePath().toString());

                if (Files.notExists(picturePath)) {
                    throw new ResponseStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save the picture");
                }
            } else {
                Files.deleteIfExists(picturePath);
            }
            connection.commit();
        } catch (SQLException | IOException e) {
            connection.rollback();
            throw new RuntimeException(e);
        }finally {
            connection.setAutoCommit(true);
        }

    }

    public  void deleteUser(Connection connection,String id,String appLocation) throws SQLException {
        UserDAOImpl userDAOImpl = new UserDAOImpl(connection);
        userDAOImpl.deleteUserById(id);
        new Thread(() -> {
            Path filePath = Paths.get(appLocation, "uploads", id);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                logger.warning("Failed to delete the image" + filePath.toAbsolutePath());
            }
        }).start();

    }
    public  UserDTO getUser(Connection connection, String emailOrId) throws SQLException {
        UserDAOImpl userDAOImpl = new UserDAOImpl(connection);
        Optional<User> userWrapper = userDAOImpl.findUserByIdOrEmail(emailOrId);
        return userWrapper.map(user -> new UserDTO(user.getId(),user.getFullName(),user.getEmail(),user.getPassword(),user.getProfilePic()))
                .orElse(null);
    }

    public  boolean existsUser(Connection connection, String emailOrId) throws SQLException {
        UserDAOImpl userDAOImpl = new UserDAOImpl(connection);
        return userDAOImpl.existsUserEmailOrUserId(emailOrId);
    }
}
