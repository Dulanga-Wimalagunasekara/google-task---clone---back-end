package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dao.exception.DataAccessException;
import lk.ijse.dep8.tasks.dao.custome.impl.UserDAOImpl;
import lk.ijse.dep8.tasks.entity.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOImplTest {
    private static Connection connection;
    private static UserDAOImpl userDAOImpl;

    @BeforeAll
    static void setUp() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep8_tasks", "root", "root");
            connection.setAutoCommit(false);
            userDAOImpl = new UserDAOImpl(connection);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static List<User> getDummyUsers(){
        List<User> dummies = new ArrayList<>();
        dummies.add(new User("U0011", "dulanga1231@ijse.lk", "1234", "Dulanga", "picture1"));
        dummies.add(new User("U0012", "dulanga1232@ijse.lk", "1234", "Dulanga", "picture1"));
        dummies.add(new User("U0013", "dulanga1233@ijse.lk", "1234", "Dulanga", null));
        dummies.add(new User("U0014", "dulanga1234@ijse.lk", "1234", "Dulanga", "picture1"));
        dummies.add(new User("U0015", "dulanga1235@ijse.lk", "1234", "Dulanga", null));
        return dummies;
    }

    @Order(1)
    @MethodSource("getDummyUsers")
    @ParameterizedTest()
    void saveUser(User givenUser) {
        System.out.println("saveUser");
        // when
        User savedUser = userDAOImpl.save(givenUser);
        //then
        assertEquals(givenUser, savedUser);
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = {"U0011","U0012","U46r4738"})
        void existsUserById(String userId) {
        System.out.println("existUser");
        boolean result = userDAOImpl.existsById(userId);
        if (userId.equals("U46r4738")){
            assertFalse(result);
        }else {
            assertTrue(result);
        }
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = {"U0011","U0012","U100"})
    void findUserById(String userId) {
        System.out.println("findUser");
        Optional<User> userById = new UserDAOImpl(connection).findById(userId);
        if (userId.equals("U100")){
            assertFalse(userById.isPresent());
        }else {
            assertTrue(userById.isPresent());
        }
    }

    @Order(4)
    @Test
    void findAllUsers() {
        List<User> allUsers = userDAOImpl.findAll();
        assertTrue(allUsers.size()>=5);
    }

    @Order(5)
    @ParameterizedTest
    @ValueSource(strings = {"U0011","U0012","U100"})
    void deleteUserById(String givenUserId) {
        System.out.println("deleteUser");
        if (givenUserId.equals("U100")){
            assertThrows(DataAccessException.class,()-> userDAOImpl.deleteById(givenUserId));
        }else {
            userDAOImpl.deleteById(givenUserId);
        }
        assertFalse(userDAOImpl.existsById(givenUserId));

    }

    @Order(6)
    @Test
    void countUsers() {
        System.out.println("countUser");
        assertTrue(userDAOImpl.count()>=5);
    }
}