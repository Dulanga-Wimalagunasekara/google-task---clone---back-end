package lk.ijse.dep8.tasks.service.custome.impl;

import lk.ijse.dep8.tasks.service.custome.TaskService;

import java.sql.Connection;

public class TaskServiceImpl implements TaskService {
    private Connection connection;
    public TaskServiceImpl(Connection connection) {
        this.connection=connection;
    }
}

