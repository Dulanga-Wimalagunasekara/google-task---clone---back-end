package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {
    private Connection connection;


    @BeforeEach
    void setUp() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        try {
           connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep8_tasks", "root", "root");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    void existsUser() throws SQLException {
        boolean b = UserDAO.existsUser(connection, "dulanga10@ijse.lk");
        assertTrue(b);
    }
}