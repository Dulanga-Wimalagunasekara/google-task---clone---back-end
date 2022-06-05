package lk.ijse.dep8.tasks.util;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@FunctionalInterface
public interface ExecutionContext {
    Logger logger = Logger.getLogger(ExecutionContext.class.getName());

    void context() throws SQLException;
    static void execute(ExecutionContext e){
        try {
            e.context();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE,ex.getMessage(),ex);
        }
    }
}
