package lk.ijse.dep8.tasks.service.custome.impl;

import lk.ijse.dep8.tasks.dao.DAOFactory;
import lk.ijse.dep8.tasks.dao.custome.UserDAO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.entity.User;
import lk.ijse.dep8.tasks.service.custome.UserService;
import lk.ijse.dep8.tasks.service.exception.FailedExecutionException;
import lk.ijse.dep8.tasks.util.EntityDTOMapper;
import lk.ijse.dep8.tasks.util.ExecutionContext;
import lk.ijse.dep8.tasks.util.JNDIConnectionPool;
import lk.ijse.dep8.tasks.util.ResponseStatusException;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.sql.ConnectionEvent;
import javax.sql.DataSource;
import javax.xml.crypto.Data;
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

    private DataSource dataSource;

    public UserServiceImpl() {
        dataSource= JNDIConnectionPool.getInstance().getPool();
    }

    private Logger logger = Logger.getLogger(UserServiceImpl.class.getName());
    public UserDTO registerUser(Part picture, String appLocation, UserDTO user){
        Connection connection=null;
        try {
            connection = JNDIConnectionPool.getInstance().getPool().getConnection();
            connection.setAutoCommit(false);
            user.setId(UUID.randomUUID().toString());
            if (picture != null) {
                user.setPicture(user.getPicture() + user.getId());
            }
            user.setPassword(DigestUtils.sha256Hex(user.getPassword()));
            UserDAO userDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.USER);
            User userEntity = EntityDTOMapper.getUser(user);
//            User userEntity = new User(user.getId(), user.getEmail(), user.getPassword(), user.getName(), user.getPicture());
            User savedUser = userDAOImpl.save(userEntity);
            user = EntityDTOMapper.getUserDTO(savedUser);
//            user = new UserDTO(savedUser.getId(), savedUser.getFullName(), savedUser.getEmail(), savedUser.getPassword(), savedUser.getProfilePic());
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
            if (connection!=null){
                ExecutionContext.execute(connection::rollback);
            }
            throw new FailedExecutionException("Failed to save the user",t);
        } finally {
            if (connection!=null){
                Connection tempConnection=connection;
                ExecutionContext.execute(()->tempConnection.setAutoCommit(true));
            }
        }
    }

    public void updateUser(UserDTO user, Part picture, String appLocation) {
        Connection connection=null;
        try {
            connection=JNDIConnectionPool.getInstance().getPool().getConnection();
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
            if (connection!=null){
                ExecutionContext.execute(connection::rollback);
            }
            throw new RuntimeException(e);
        } finally {
            if (connection!=null){
                Connection tempConnection=connection;
                ExecutionContext.execute(()->tempConnection.setAutoCommit(true));
            }
        }

    }

    public void deleteUser(String id, String appLocation) {
        try {
            Connection connection= JNDIConnectionPool.getInstance().getPool().getConnection();
            UserDAO userDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.USER);
            userDAOImpl.deleteById(id);
            new Thread(() -> {
                Path filePath = Paths.get(appLocation, "uploads", id);
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    logger.warning("Failed to delete the image" + filePath.toAbsolutePath());
                }
            }).start();
        } catch (SQLException e) {
            throw new FailedExecutionException("Failed t delete the user",e);
        }
    }

    public UserDTO getUser(String emailOrId) {
        try {
            Connection connection = JNDIConnectionPool.getInstance().getPool().getConnection();
            UserDAO userDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.USER);
            Optional<User> userWrapper = userDAOImpl.findUserByIdOrEmail(emailOrId);
            return EntityDTOMapper.getUserDTO(userWrapper.orElse(null));
        } catch (SQLException e) {
            throw new FailedExecutionException("Failed to fetch the user",e);
        }
//        return userWrapper.map(user -> new UserDTO(user.getId(), user.getFullName(), user.getEmail(), user.getPassword(), user.getProfilePic()))
//                .orElse(null);
    }

    public boolean existsUser(String emailOrId){
        try (Connection connection = dataSource.getConnection()) {

                UserDAO userDAOImpl = DAOFactory.getInstance().getDAO(connection, DAOFactory.DAOTypes.USER);
                return userDAOImpl.existsUserEmailOrUserId(emailOrId);

        }catch (SQLException t){
            throw new FailedExecutionException("Failed to check the existence",t);
        }
    }

}
