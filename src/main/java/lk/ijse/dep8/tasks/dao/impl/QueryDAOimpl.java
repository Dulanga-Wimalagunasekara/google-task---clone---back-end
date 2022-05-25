package lk.ijse.dep8.tasks.dao.impl;

import java.sql.Connection;

public class QueryDAOimpl implements QueryDAO{

    private Connection connection;

    public QueryDAOimpl(Connection connection) {
        this.connection = connection;
    }
}
