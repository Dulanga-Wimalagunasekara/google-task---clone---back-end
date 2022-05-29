package lk.ijse.dep8.tasks.util;

import lk.ijse.dep8.tasks.dto.TaskDTO;
import lk.ijse.dep8.tasks.dto.TaskListDTO;
import lk.ijse.dep8.tasks.dto.UserDTO;
import lk.ijse.dep8.tasks.entity.Task;
import lk.ijse.dep8.tasks.entity.TaskList;
import lk.ijse.dep8.tasks.entity.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;

public class EntityDTOMapper {

    public static UserDTO getUserDTO(User user){
        ModelMapper modelMapper = new ModelMapper();
        TypeMap<User, UserDTO> typeMap = modelMapper.typeMap(User.class, UserDTO.class);
        typeMap.addMapping(s -> s.getProfilePic(),(d,value)->d.setPicture((String) value));
        return modelMapper.map(user,UserDTO.class);
    }

    public static User getUser(UserDTO user){
        ModelMapper modelMapper = new ModelMapper();
        TypeMap<UserDTO, User> typeMap = modelMapper.typeMap(UserDTO.class,User.class);
        return typeMap.map(user);
//        typeMap.addMapping(UserDTO::getName, User::setFullName);
//        typeMap.addMapping(s -> s.getPicture(),(d,value)->d.setProfilePic((String) value));
//        return modelMapper.map(user,User.class);

    }


    public static TaskListDTO getTaskListDTO(TaskList taskList){
        ModelMapper modelMapper = new ModelMapper();
        TypeMap<TaskList, TaskListDTO> typeMap = modelMapper.typeMap(TaskList.class, TaskListDTO.class);
        typeMap.addMapping(taskList1 -> taskList1.getName(),(d,value)->d.setTitle((String) value));
        return modelMapper.map(taskList,TaskListDTO.class);
    }

    public static TaskList getTaskList(TaskListDTO taskList){
        ModelMapper modelMapper = new ModelMapper();
        TypeMap<TaskListDTO, TaskList> typeMap = modelMapper.typeMap(TaskListDTO.class,TaskList.class);
        typeMap.addMapping(taskList1 -> taskList1.getTitle(),(d,value)->d.setName((String) value));
        return modelMapper.map(taskList,TaskList.class);
    }



    public static TaskDTO getTaskDTO(Task task){
        ModelMapper modelMapper = new ModelMapper();
        TypeMap<Task, TaskDTO> typeMap = modelMapper.typeMap(Task.class, TaskDTO.class);
        typeMap.addMapping(task1 -> task1.getDetails(),(d,value)->d.setNotes((String) value));
        return modelMapper.map(task,TaskDTO.class);
    }

    public static Task getTask(TaskDTO task){
        ModelMapper modelMapper = new ModelMapper();
        TypeMap<TaskDTO, Task> typeMap = modelMapper.typeMap(TaskDTO.class,Task.class);
        typeMap.addMapping(task1 -> task1.getNotes(),(d,value)->d.setDetails((String) value));
        return modelMapper.map(task,Task.class);
    }


}
