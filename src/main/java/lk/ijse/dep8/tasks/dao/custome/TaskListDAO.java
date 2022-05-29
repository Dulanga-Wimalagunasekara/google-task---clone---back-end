package lk.ijse.dep8.tasks.dao.custome;

import lk.ijse.dep8.tasks.dao.crudDAO;
import lk.ijse.dep8.tasks.entity.TaskList;

import java.util.Optional;

public interface TaskListDAO extends crudDAO<TaskList,Integer> {
    boolean existTaskListByIdAndUserId(int taskListId,String userId);
    Optional<TaskList> getTaskListByIdAndUserId(int taskListId, String userId);

}
