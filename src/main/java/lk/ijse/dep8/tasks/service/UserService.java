package lk.ijse.dep8.tasks.service;

import lk.ijse.dep8.tasks.dao.oldUserDAO;
import lk.ijse.dep8.tasks.dto.UserDTO;
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
            UserDTO savedUser = new oldUserDAO().saveUser(connection, user);
            if (picture != null){
                Path path = Paths.get(appLocation, "uploads");
                if (Files.notExists(path)) {
                    Files.createDirectory(path);
                }
                String picturePath = path.resolve(user.getId()).toAbsolutePath().toString();
                picture.write(picturePath);
            }
            connection.commit();
            return savedUser;
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
            user.setPassword(DigestUtils.sha256Hex(user.getPassword()));
            new oldUserDAO().updateUser(connection,user);

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
        new oldUserDAO().deleteUser(connection,id);
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
        return new oldUserDAO().getUser(connection,emailOrId);
    }

    public  boolean existsUser(Connection con, String emailOrId) throws SQLException {
        return new oldUserDAO().existsUser(con,emailOrId);
    }
}
