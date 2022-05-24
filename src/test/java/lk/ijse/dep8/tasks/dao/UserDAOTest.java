package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {
    private Connection connection;
    UserDTO userDTO;


    @BeforeEach
    void setUp() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try {
           connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep8_tasks", "root", "root");
           connection.setAutoCommit(false);
            userDTO = new UserDTO(UUID.randomUUID().toString(), "Dulanga", "dulanga11@ijse.lk", "1234", null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    void existsUser() throws SQLException {
        boolean b = UserDAO.existsUser(connection, "dulanga10@ijse.lk");
        assertTrue(b);
    }

    @Test
    void saveUser() throws SQLException{
        UserDTO user = UserDAO.saveUser(connection, userDTO);
        assertEquals(this.userDTO,user);
        boolean b = UserDAO.existsUser(connection, user.getEmail());
        assertTrue(b);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
        connection.close();
    }
}