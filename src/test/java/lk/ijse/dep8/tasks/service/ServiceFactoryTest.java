package lk.ijse.dep8.tasks.service;

import lk.ijse.dep8.tasks.service.custome.impl.TaskServiceImpl;
import lk.ijse.dep8.tasks.service.custome.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class ServiceFactoryTest {

    @Mock
    private Connection connection;
    AutoCloseable autoCloseable;

    @RepeatedTest(2)
    void getInstance() {
        ServiceFactory instance1 = ServiceFactory.getInstance();
        ServiceFactory instance2 = ServiceFactory.getInstance();
        assertEquals(instance1,instance2);
    }

    @Test
    void getService() {
        SuperService task = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.TASK);
        SuperService user = ServiceFactory.getInstance().getService(ServiceFactory.ServiceTypes.USER);
        assertTrue(task instanceof TaskServiceImpl);
        assertTrue(user instanceof UserServiceImpl);
    }

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }
}