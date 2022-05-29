package lk.ijse.dep8.tasks.dao.custome;

import lk.ijse.dep8.tasks.dao.crudDAO;
import lk.ijse.dep8.tasks.entity.Task;

import java.util.List;
import java.util.Optional;

public interface TaskDAO extends crudDAO<Task,Integer>{
    Optional<List<Task>> findByTaskListId(Integer taskListId);
}
