package lk.ijse.dep8.tasks.api;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.util.HttpServlet2;
import lk.ijse.dep8.tasks.util.ResponseStatusException;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

@MultipartConfig(location = "/tmp",maxFileSize = 10*1023*1024)
@WebServlet(name = "UserServlet", value = "/v1/users/*")
public class UserServlet extends HttpServlet2 {

    private final Logger logger = Logger.getLogger(UserServlet.class.getName());
    @Resource(name = "java:comp/env/jdbc/pool")
    private volatile DataSource pool;

    private UserDTO getUser(HttpServletRequest request) {
        if (!(request.getPathInfo() != null &&
                (request.getPathInfo().replaceAll("/", "").length() == 36))) {
            throw new ResponseStatusException(404, "Not found");
        }

        String userId = request.getPathInfo().replaceAll("/", "");
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM user WHERE id=?");
            stm.setString(1, userId);
            ResultSet rst = stm.executeQuery();
            if (!rst.next()) {
                throw new ResponseStatusException(HttpServletResponse.SC_NOT_FOUND, "Invalid User");
            } else {
                String name = rst.getString("full_name");
                String email = rst.getString("email");
                String password = rst.getString("password");
                String picture = rst.getString("profile_pic");
                return new UserDTO(userId, name, email, password, picture);

            }

        } catch (SQLException e) {
            throw new ResponseStatusException(500, "Failed to fetch the user info", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserDTO userDTO = getUser(request);
        Jsonb jsonb = JsonbBuilder.create();
        jsonb.toJson(userDTO, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getContentType() == null || !request.getContentType().startsWith("multipart/form-data")) {
            throw new ResponseStatusException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Invalid Request");
        }

        if (request.getPathInfo() != null && !request.getPathInfo().equals("/")) {
            throw new ResponseStatusException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Invalid Request");
        }

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        Part picture = request.getPart("picture");

        if (name == null || !name.matches("[A-Za-z ]+")) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid name");
        } else if (email == null || !email.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid email");
        } else if (password == null || password.trim().isEmpty()) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Password can not be empty");
        } else if (picture != null && !picture.getContentType().startsWith("image")) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid picture");
        }

        String realPath = getServletContext().getRealPath("/");
        Path path = Paths.get(realPath, "uploads");
        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }

        Connection connection = null;
        try {
            connection = pool.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM user WHERE email=?");
            statement.setString(1, email);
            ResultSet que = statement.executeQuery();
            if (que.next()) {
                throw new ResponseStatusException(HttpServletResponse.SC_CONFLICT, "User already exists");
            }
            connection.setAutoCommit(false);
            PreparedStatement stm = connection.prepareStatement("INSERT INTO user (id,email, password, full_name,profile_pic) VALUES (?,?,?,?,?)");
            String id = UUID.randomUUID().toString();
            stm.setString(1, id);
            stm.setString(2, email);
            stm.setString(3, DigestUtils.sha256Hex(password));
            stm.setString(4, name);

            String pictureUrl = null;
            if (picture != null) {
                pictureUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + getServletContext().getContextPath();
                pictureUrl += "/uploads/" + id;
            }

            stm.setString(5, pictureUrl);
            int i = stm.executeUpdate();
            if (i != 1) {
                throw new SQLException("Failed to register the user");
            }

            if (picture != null) {
                String picturePath = path.resolve(id).toAbsolutePath().toString();
                picture.write(picturePath);
                if (Files.notExists(Paths.get(picturePath))) {
                    connection.rollback();
                    throw new ResponseStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Can not save the picture");
                }
            }
            connection.commit();
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/json");
            UserDTO userDTO = new UserDTO(id, name, email, password, pictureUrl);
            Jsonb jsonb = JsonbBuilder.create();
            jsonb.toJson(userDTO, response.getWriter());
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Can not register the user");
        } finally {
            try {
                if (!connection.getAutoCommit()){
                    connection.rollback();
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserDTO user = getUser(req);
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("DELETE FROM user WHERE id=?");
            stm.setString(1, user.getId());
            int i = stm.executeUpdate();
            if (i != 1) {
                throw new SQLException();
            }
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            new Thread(() -> {
                Path filePath = Paths.get(getServletContext().getRealPath("/"), "uploads", user.getId());
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    logger.warning("Failed to delete the image" + filePath.toAbsolutePath());
                }
            }).start();
        } catch (SQLException e) {
            throw new ResponseStatusException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete the user", e);
        }
    }

    @Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        if (request.getContentType() == null || !request.getContentType().startsWith("multipart/form-data")) {
            throw new ResponseStatusException(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Invalid content type or no content type is provided");
        }

        UserDTO user = getUser(request);

        String name = request.getParameter("name");
        String password = request.getParameter("password");
        Part picture = request.getPart("picture");

        if (name == null || !name.matches("[A-Za-z ]+")) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid name or name is empty");
        } else if (password == null || password.trim().isEmpty()) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Password can't be empty");
        } else if (picture != null && !picture.getContentType().startsWith("image")) {
            throw new ResponseStatusException(HttpServletResponse.SC_BAD_REQUEST, "Invalid picture");
        }

        Connection connection = null;
        try {
            connection = pool.getConnection();
            connection.setAutoCommit(false);

            PreparedStatement stm = connection.
                    prepareStatement("UPDATE user SET full_name=?, password=?, profile_pic=? WHERE id=?");
            stm.setString(1, name);
            stm.setString(2, DigestUtils.sha256Hex(password));

            String pictureUrl = null;
            if (picture != null) {
                pictureUrl = request.getScheme() + "://" + request.getServerName() + ":"
                        + request.getServerPort() + request.getContextPath();
                pictureUrl += "/uploads/" + user.getId();
            }
            stm.setString(3, pictureUrl);
            stm.setString(4, user.getId());

            if (stm.executeUpdate() != 1) {
                throw new SQLException("Failed to update the user");
            }

            String appLocation = getServletContext().getRealPath("/");
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
            resp.setStatus(204);
        } catch (SQLException e) {
            throw new ResponseStatusException(500, e.getMessage(), e);
        } finally {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
