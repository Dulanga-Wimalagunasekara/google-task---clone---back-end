package lk.ijse.dep8.tasks.dao.custome;

import lk.ijse.dep8.tasks.dao.CrudDAO;
import lk.ijse.dep8.tasks.entity.TaskList;

import java.util.List;
import java.util.Optional;

public interface TaskListDAO extends CrudDAO<TaskList,Integer> {
    boolean existTaskListByIdAndUserId(int taskListId,String userId);
    Optional<TaskList> getTaskListByIdAndUserId(int taskListId, String userId);
    Optional<List<TaskList>> findByUserId(String userId);

}
