package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dto.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class oldUserDAOTest {
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

    @ParameterizedTest
    @ValueSource(strings = {"dulanga10@ijse.lk", "gihara111@ijse.lk", "c7d35a06-2689-40ff-ba9b-ef90068df29f"})
    void existsUser(String arg) throws SQLException {
        boolean b = new oldUserDAO().existsUser(connection, arg);
        assertTrue(b);
    }

    @Test
    void saveUser() throws SQLException {
        UserDTO user = new oldUserDAO().saveUser(connection, userDTO);
        assertEquals(this.userDTO, user);
        boolean b = new oldUserDAO().existsUser(connection, user.getEmail());
        assertTrue(b);
    }

    @ParameterizedTest
    @ValueSource(strings = {"dulanga10@ijse.lk", "gihara111@ijse.lk", "c7d35a06-2689-40ff-ba9b-ef90068df29f"})
    void getUser(/*Given*/String val) throws SQLException {
        //When
        UserDTO user = new oldUserDAO().getUser(connection, val);

        //Then
        assertNotNull(user);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
        connection.close();
    }

    @Test
    void deleteUser() throws SQLException {
        //Given
        String id = "d6348360-04fa-4931-9e64-99d84fb34531";

        //When
        new oldUserDAO().deleteUser(connection, id);

        //Then
        assertThrows(AssertionFailedError.class, () -> existsUser(id));
    }

    @Test
    void updateUser() throws SQLException {
        //Given
        UserDTO user = new oldUserDAO().getUser(connection, "bd3f1de4-b188-43b4-b0ee-1ef205502149");
        user.setName("Gihara plus");

        //When
        new oldUserDAO().updateUser(connection, user);

        //Then
        UserDTO user1 = new oldUserDAO().getUser(connection, "newone222@ijse.lk");
        assertEquals(user, user1);
    }
}