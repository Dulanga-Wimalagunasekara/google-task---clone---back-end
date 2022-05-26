package lk.ijse.dep8.tasks.util;

import lk.ijse.dep8.tasks.dto.TaskDTO;
import lk.ijse.dep8.tasks.dto.TaskListDTO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.entity.Task;
import lk.ijse.dep8.tasks.entity.TaskList;
import lk.ijse.dep8.tasks.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityDTOMapperTest {

    @Test
    void getUserDTO() {
        //given
        User user = new User("C001", "dulanga@ijse.lk", "1234", "Dulanga", "null");

        //when
        UserDTO userDTO = EntityDTOMapper.getUserDTO(user);

        //Then
        assertEquals(user.getId(),userDTO.getId());
        assertEquals(user.getEmail(),userDTO.getEmail());
        assertEquals(user.getFullName(),userDTO.getName());
        assertEquals(user.getPassword(),userDTO.getPassword());
        assertEquals(user.getProfilePic(),userDTO.getPicture());
    }

    @Test
    void getTaskListDTO() {
        TaskList taskList = new TaskList(10, "Dulanga", "U001");
        TaskListDTO taskListDTO = EntityDTOMapper.getTaskListDTO(taskList);

        assertEquals(taskListDTO.getId(),taskList.getId());
        assertEquals(taskListDTO.getTitle(),taskList.getName());
        assertEquals(taskListDTO.getUserID(),taskList.getUserId());
    }

    @Test
    void getTaskDTO() {
        Task task = new Task(10, "New", "SimpleTask", 1, Task.Status.needsAction, 1);
        TaskDTO taskDTO = EntityDTOMapper.getTaskDTO(task);
        assertEquals(taskDTO.getId(),task.getId());
        assertEquals(task.getTaskListId(),taskDTO.getTaskListId());
        assertEquals(task.getTitle(),taskDTO.getTitle());
        assertEquals(task.getPosition(),taskDTO.getPosition());
        assertEquals(task.getStatus().toString(),taskDTO.getStatus());
        assertEquals(task.getDetails(),taskDTO.getNotes());
    }

    @Test
    void getUser() {
        //given
        UserDTO userdto = new UserDTO("C001","Dulanga","dulanga@ijse.lk","1234","something");

        //when
        User user = EntityDTOMapper.getUser(userdto);

        //Then
        assertEquals(user.getId(),userdto.getId());
        assertEquals(user.getEmail(),userdto.getEmail());
//        assertEquals(userdto.getName(), user.getFullName());
        assertEquals(user.getPassword(),userdto.getPassword());
        assertEquals(user.getProfilePic(),userdto.getPicture());
    }

    @Test
    void getTaskList() {
        TaskListDTO taskListDTO = new TaskListDTO(10, "Dulanga", "U001");
        TaskList taskList = EntityDTOMapper.getTaskList(taskListDTO);

        assertEquals(taskListDTO.getId(),taskList.getId());
        assertEquals(taskListDTO.getTitle(),taskList.getName());
        assertEquals(taskListDTO.getUserID(),taskList.getUserId());
    }

    @Test
    void getTask() {
        TaskDTO taskDTO = new TaskDTO(10, "New",10,"Something","completed");
        Task task = EntityDTOMapper.getTask(taskDTO);

        assertEquals(taskDTO.getId(),taskDTO.getId());
        assertEquals(taskDTO.getTaskListId(),task.getTaskListId());
        assertEquals(taskDTO.getTitle(),task.getTitle());
        assertEquals(taskDTO.getPosition(),task.getPosition());
        assertEquals(taskDTO.getStatus(),task.getStatus().toString());
        assertEquals(taskDTO.getNotes(),task.getDetails());
    }
}