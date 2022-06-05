package lk.ijse.dep8.tasks.dao.custome;

import lk.ijse.dep8.tasks.dao.SuperDAO;
import lk.ijse.dep8.tasks.entity.SuperEntity;
import lk.ijse.dep8.tasks.entity.Task;

public interface QueryDAO<T extends SuperEntity> extends SuperDAO{
    T getTask(int taskId,int taskListId,String userId);
}
