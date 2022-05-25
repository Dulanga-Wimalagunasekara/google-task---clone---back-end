package lk.ijse.dep8.tasks.dao;

import lk.ijse.dep8.tasks.entity.Task;
import java.util.List;
import java.util.Optional;

public interface TaskDAO {
    Task saveTask(Task task);

    void deleteTaskById(int taskId);

    Optional<Task> findTaskById(int taskId);

    boolean existsTaskById(int taskId);

    List<Task> findAllTasks(String taskId);

    long countTasks();
}
