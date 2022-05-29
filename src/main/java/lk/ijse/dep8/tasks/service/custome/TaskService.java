package lk.ijse.dep8.tasks.service.custome;

import lk.ijse.dep8.tasks.dto.TaskDTO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.service.SuperService;

import javax.servlet.http.Part;
import java.util.List;
import java.util.Optional;

public interface TaskService extends SuperService {
    boolean existsTask(String emailOrId);

    UserDTO saveTask(Part picture, String appLocation, UserDTO user);

    Optional<List<TaskDTO>> getTask(int taskListId, String userId);

    void deleteTask(String id, String appLocation);

    void updateTask(UserDTO user, Part picture, String appLocation);


    /*==================================================================================*/
    UserDTO saveTaskList(Part picture, String appLocation, UserDTO user);

    UserDTO getTaskList(int taskListId,String userId);

    void deleteTaskList(String id, String appLocation);

    void updateTaskList(UserDTO user, Part picture, String appLocation);



}
