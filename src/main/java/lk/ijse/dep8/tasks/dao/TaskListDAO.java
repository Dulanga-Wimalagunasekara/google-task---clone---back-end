package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.entity.TaskList;
import java.util.List;
import java.util.Optional;

public interface TaskListDAO {
    TaskList saveTaskList(TaskList list);

    void deleteTaskListById(TaskList list);

    Optional<TaskList> findTaskListTById(int listId);

    boolean existsTaskListById(int listId);

    List<TaskList> findAllTaskLists();

    long countTaskLists();
}
