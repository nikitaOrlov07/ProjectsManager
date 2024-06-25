package com.example.Controller;

import com.example.Dto.ProjectDto;
import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Security.SecurityUtil;
import com.example.Service.ChatService;
import com.example.Service.MessageService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.example.mappers.ProjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class ActionController {
    private ProjectService projectService; private UserService userService; private MessageService messageService; private ChatService chatService;
    private static final Logger logger = LoggerFactory.getLogger(ActionController.class);

    @Autowired
    public ActionController(ProjectService projectService, UserService userService,MessageService messageService,ChatService chatService) {
        this.projectService = projectService;
        this.userService = userService;
        this.messageService=messageService;
        this.chatService=chatService;
    }
    //----------------------------------------------------------------Projects----------------------------------------------------------------

    @PostMapping("/projects/addProject/{projectId}")
    public String addProject(@PathVariable("projectId") Long projectId)
    {
      UserEntity user = userService.findByUsername(SecurityUtil.getSessionUser());
      Project project = projectService.findById(projectId);
      user.getCurrentProjects().add(projectService.findById(projectId));
      project.getInvolvedUsers().add(user);
      logger.info("adding project logic working");
      userService.save(user); projectService.save(project);
      return "redirect:/projects/" +projectId;
    }
    @PostMapping("/projects/removeProject/{projectId}")
    public String removeProject(@PathVariable("projectId") Long projectId)
    {
        UserEntity user = userService.findByUsername(SecurityUtil.getSessionUser());
        Project project = projectService.findById(projectId);
        user.getCurrentProjects().remove(project);
        project.getInvolvedUsers().remove(user);
        userService.save(user); projectService.save(project);
        return "redirect:/projects/" +projectId;
    }
    // Create project
    @GetMapping("/projects/create")
    public String createProject(RedirectAttributes redirectAttributes,
                                Model model)
    {
        UserEntity user = userService.findByUsername(SecurityUtil.getSessionUser());
        if(user == null)
        {
            redirectAttributes.addFlashAttribute("loginError", "You must be logged in");
            return "redirect:/login";
        }
        ProjectDto projectDto = new ProjectDto();
        model.addAttribute("projectDto", projectDto);
        return "create-project";
    }
    @PostMapping("/projects/create/save")
    public String createProject(@ModelAttribute("projectDto") @Valid ProjectDto projectDto
                                , BindingResult result
                                , RedirectAttributes redirectAttributes
                                , Model model)
    {
        UserEntity user = userService.findByUsername(SecurityUtil.getSessionUser());
        if(user == null)
        {
            redirectAttributes.addFlashAttribute("loginError", "You must be logged in");
            return "redirect:/login";
        }
        if(result.hasErrors())
        {
            model.addAttribute("projectDto", projectDto);
            return "create-project";
        }
        ProjectDto project = projectService.createProject(projectDto,user);
        return "redirect:/projects/" + project.getId();
    }
    // update project
    @GetMapping("/projects/{projectId}/update")
    public String updateNews(Model model, @PathVariable("projectId") Long projectId) {
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        Project project = projectService.findById(projectId);
        if (currentUser == null || !project.getInvolvedUsers().contains(currentUser))
        {
            return "redirect:/home?operationError";
        }
        ProjectDto projectDto = ProjectMapper.projectToProjectDto(project);
        model.addAttribute("projectDto", projectDto);
        return "project-update";
    }


    @PostMapping(value = "/news/update/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String updateNews(@Valid @RequestBody ProjectDto projectDto) {
        String username = SecurityUtil.getSessionUser();
        if (username == null || !userService.findByUsername(username).hasAdminRole()) // if the user is not authorized and don`t have admin role
        {
            return "redirect:/news";
        }
        projectService.save(ProjectMapper.projectDtotoProject(projectDto));
        return "redirect:/projects/"+projectDto.getId();
    }
    // delete project
    @PostMapping("/projects/{projectId}/delete")
    public String deleteProject(@PathVariable("projectId") Long projectId)
    {
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        Project project = projectService.findById(projectId);
        if(currentUser == null || (currentUser !=null && !project.getInvolvedUsers().contains(currentUser)))
        {
         return "redirect:/home";
        }
        projectService.delete(project);
        return "redirect:/home?projectSuccessfulDelete";
    }
    //-------------------------------------------------Users------------------------------------------------
    @PostMapping("/users/delete/{userId}")
    public String deleteUser(@PathVariable("userId") Long userId)
    {
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        UserEntity deletedUser = userService.findById(userId);
        if(currentUser == null ||  !currentUser.equals(deletedUser))
        {
            return "redirect:/home";
        }
        userService.delete(deletedUser);
        return "redirect:/logout";
    }
    @PostMapping("/users/{action}Friend/{friendId}")
    public String handleFriendAction(@PathVariable("action") String action,
                                     @PathVariable("friendId") Long friendId) {
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        UserEntity friendUser = userService.findById(friendId);

        if (currentUser == null || friendUser == null) {
            return "redirect:/home?operationError";
        }

        switch (action.toLowerCase()) {
            case "add":
                userService.friendsLogic(currentUser, friendUser, "add");
                return "redirect:/home?friendAdded";
            case "remove":
                userService.friendsLogic(currentUser, friendUser, "remove");
                return "redirect:/home?friendRemoved";
            default:
                return "redirect:/home?operationError";
        }
    }
    @PostMapping("/users/{action}FriendInvitation/{friendId}")
    public String handleFriendInvitation(@PathVariable("action") String action,
                                         @PathVariable("friendId") Long friendId) {
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        UserEntity friendUser = userService.findById(friendId);

        if (currentUser == null || friendUser == null) {
            return "redirect:/home?operationError";
        }

        switch (action) {
            case "remove":
                friendUser.getUserFriendsInvitations().remove(currentUser);
                break;
            case "decline":
                currentUser.getUserFriendsInvitations().remove(friendUser);
                break;
            case "add":
                friendUser.getUserFriendsInvitations().add(currentUser);
                break;
            default:
                return "redirect:/home?operationError";
        }

        userService.save(currentUser); userService.save(friendUser);
        return "redirect:/home?friendInvitation"+action+"Success";
    }


}
