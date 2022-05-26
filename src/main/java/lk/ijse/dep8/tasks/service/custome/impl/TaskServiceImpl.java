package lk.ijse.dep8.tasks.service.custome.impl;

import lk.ijse.dep8.tasks.service.custome.TaskService;
import lk.ijse.dep8.tasks.util.JNDIConnectionPool;

import javax.sql.DataSource;
import java.sql.Connection;

public class TaskServiceImpl implements TaskService {
    private DataSource dataSource;

    public TaskServiceImpl() {
        dataSource= JNDIConnectionPool.getInstance().getPool();
    }

}

