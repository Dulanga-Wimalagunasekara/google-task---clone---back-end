package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.dao.custome.QueryDAO;
import lk.ijse.dep8.tasks.dao.custome.TaskDAO;
import lk.ijse.dep8.tasks.dao.custome.TaskListDAO;
import lk.ijse.dep8.tasks.dao.custome.UserDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DAOFactoryTest {
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getDAO() {
        Connection mockConnection = mock(Connection.class);
        TaskListDAO taskList = DAOFactory.getInstance().getDAO(mockConnection, DAOFactory.DAOTypes.TASK_LIST);
        UserDAO user = DAOFactory.getInstance().getDAO(mockConnection, DAOFactory.DAOTypes.USER);
        TaskDAO task = DAOFactory.getInstance().getDAO(mockConnection, DAOFactory.DAOTypes.TASK);
        QueryDAO query = DAOFactory.getInstance().getDAO(mockConnection, DAOFactory.DAOTypes.QUERY);

        assertNotNull(taskList);
        assertNotNull(user);
        assertNotNull(task);
        assertNotNull(query);
    }

    @Test
    void getInstance() {
    }
}