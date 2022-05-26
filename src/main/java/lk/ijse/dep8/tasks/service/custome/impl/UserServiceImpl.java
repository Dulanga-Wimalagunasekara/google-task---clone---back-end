package lk.ijse.dep8.tasks.service.custome.impl;

import lk.ijse.dep8.tasks.dao.DAOFactory;
import lk.ijse.dep8.tasks.dao.custome.UserDAO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.entity.User;
import lk.ijse.dep8.tasks.service.custome.UserService;
import lk.ijse.dep8.tasks.service.exception.FailedExecutionException;
import lk.ijse.dep8.tasks.util.ExecutionContext;
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

public class UserServiceImpl implements UserService {

    private Connection connection;

    private Logger logger = Logger.getLogger(UserServiceImpl.class.getName());
    public UserServiceImpl(Connection connection) {
        this.connection = connection;
    }

    public UserDTO registerUser(Part picture, String appLocation, UserDTO user){
        try {
            connection.setAutoCommit(false);
            user.setId(UUID.randomUUID().toString());
            if (picture != null) {
                user.setPicture(user.getPicture() + user.getId());
            }
            user.setPassword(DigestUtils.sha256Hex(user.getPassword()));
            UserDAO userDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.USER);
            User userEntity = new User(user.getId(), user.getEmail(), user.getPassword(), user.getName(), user.getPicture());
            User savedUser = userDAOImpl.save(userEntity);
            user = new UserDTO(savedUser.getId(), savedUser.getFullName(), savedUser.getEmail(), savedUser.getPassword(), savedUser.getProfilePic());
            if (picture != null) {
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
            ExecutionContext.execute(connection::rollback);
            throw new FailedExecutionException("Failed to save the user",t);
        } finally {
            ExecutionContext.execute(()->connection.setAutoCommit(true));
        }
    }

    public void updateUser(UserDTO user, Part picture, String appLocation) {
        try {
            connection.setAutoCommit(false);
            UserDAO userDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.USER);
            Optional<User> userWrapper = userDAOImpl.findById(user.getId());
            User oldUserEntity = userWrapper.get();

            oldUserEntity.setPassword(DigestUtils.sha256Hex(user.getPassword()));

            oldUserEntity.setFullName(user.getName());
            oldUserEntity.setProfilePic(user.getPicture());

            userDAOImpl.save(oldUserEntity);

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
            ExecutionContext.execute(connection::rollback);
            throw new RuntimeException(e);
        } finally {
            ExecutionContext.execute(()->connection.setAutoCommit(true));
        }

    }

    public void deleteUser(String id, String appLocation) {
        UserDAO userDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.USER);
        ;
        userDAOImpl.deleteById(id);
        new Thread(() -> {
            Path filePath = Paths.get(appLocation, "uploads", id);
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                logger.warning("Failed to delete the image" + filePath.toAbsolutePath());
            }
        }).start();

    }

    public UserDTO getUser(String emailOrId) {
        UserDAO userDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.USER);
        Optional<User> userWrapper = userDAOImpl.findUserByIdOrEmail(emailOrId);
        return userWrapper.map(user -> new UserDTO(user.getId(), user.getFullName(), user.getEmail(), user.getPassword(), user.getProfilePic()))
                .orElse(null);
    }

    public boolean existsUser(String emailOrId) {
        UserDAO userDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.USER);
        return userDAOImpl.existsUserEmailOrUserId(emailOrId);
    }

}
