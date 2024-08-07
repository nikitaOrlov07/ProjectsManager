package com.example.Controller;

import com.example.Dto.TaskDto;
import com.example.Model.Attachment;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Model.Task;
import com.example.Security.SecurityUtil;
import com.example.Service.AttachmentService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.example.Service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {
    private ProjectService projectService; private UserService userService;
    private AttachmentService attachmentService;
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    public MainController(ProjectService projectService, UserService userService,AttachmentService attachmentService) {
        this.projectService = projectService;
        this.userService = userService;
        this.attachmentService = attachmentService;

    }



    @GetMapping("/home")
    public String mainPage(Model model) {
        List<Project> allProjects = projectService.findAllProjects();
        List<Project> userProjects = projectService.findUsersProjects();

        UserEntity user = userService.findByUsername(SecurityUtil.getSessionUser());
        List<UserEntity> userFriends=null;
        if(user != null && user.getUserFriends()!=null) {
            userFriends = user.getUserFriends(); }

        List<UserEntity> allUsers = userService.findAllUsers();

        // Create map collection to store common projects
        Map<UserEntity, List<Project>> commonProjectsMap = new HashMap<>();
        if (user != null) {
            for (UserEntity userF : allUsers) {
                List<Project> commonProjects = userService.findCommonProjects(user, userF); // find common projects
                commonProjectsMap.put(userF, commonProjects != null ? commonProjects : new ArrayList<>()); // add value into map (even if there are no shared projects, add the user as a key and empty array list as value)
            }
            allUsers.remove(user); // the user will not see himself in the list of users

        }
        // Friends invitation
        List<UserEntity> friendsInvitation = null;
        if(user !=null && user.getUserFriendsInvitations()!=null)
        {
            friendsInvitation = user.getUserFriendsInvitations();
        }
        model.addAttribute("userProjects", userProjects);
        model.addAttribute("allProjects", allProjects);
        model.addAttribute("userFriends", userFriends);
        model.addAttribute("user", user);
        model.addAttribute("commonProjectsMap", commonProjectsMap);
        model.addAttribute("userList", allUsers);
        model.addAttribute("friendsInvitations",friendsInvitation);


        return "home-page";
    }


    // Detail page
    @GetMapping("/projects/{projectId}")
    public String detailPage(@PathVariable("projectId") Long projectId
                             , Model model)
    {
        Project project = projectService.findById(projectId);
        UserEntity user = userService.findByUsername(SecurityUtil.getSessionUser());
        List<Attachment> attachments = attachmentService.findAllByProject(project);
        Long remainingDays = projectService.getRemainingDays(project.getEndDate());
        attachments.forEach(attachment -> logger.info("File name: " + attachment.getFileName()));
        System.out.println("user is Admin : " + user.hasAdminRole());
        model.addAttribute("user", user);
        model.addAttribute("project", project);
        model.addAttribute("tasks", project.getTasks());
        model.addAttribute("taskDto",new TaskDto());
        model.addAttribute("remainingDays",remainingDays);
        model.addAttribute("files", attachments);

        return "detail-page";
    }
    //------------------------------------------------------------ Search projects-----------------------------------------------------------------
    @GetMapping("/home/find")
    public String search(@RequestParam(value = "query", defaultValue = " ") String query,
                             Model model,
                             @RequestParam(value = "type", defaultValue = " ") String type) {
        UserEntity user = userService.findByUsername(SecurityUtil.getSessionUser());
        if (type.equals("allProjects")) {
            model.addAttribute("allProjects", projectService.search(query, "allProjects"));
            logger.info("search logic are working for all projects");
            return "home-page :: allProjects"; // allProjects fragments
        } else if (type.equals("userProjects")) {
            model.addAttribute("userProjects", projectService.search(query, "userProjects"));
            logger.info("search logic are working for users projects");
            return "home-page :: userProjects"; // userProjects fragments
        } else if (type.equals("allUsers")) {
            List<UserEntity> users = userService.search(query, type);
            // common projects
            Map<UserEntity, List<Project>> commonProjectsMap = new HashMap<>();
            if (user != null) {
                for (UserEntity userF : users) {
                    List<Project> commonProjects = userService.findCommonProjects(user, userF); // find common projects
                    commonProjectsMap.put(userF, commonProjects != null ? commonProjects : new ArrayList<>()); // add value into map
                }
                users.remove(user); // the user will not see himself in the list of users
            }
            model.addAttribute("userList", users);
            model.addAttribute("user", user);
            model.addAttribute("commonProjectsMap", commonProjectsMap);
            logger.info("search logic are working for users projects");
            return "home-page :: userList"; // allUser fragment
        } else if (type.equals("userFriends")) {
            List<UserEntity> usersFriends = userService.search(query, type);
            // common projects
            Map<UserEntity, List<Project>> commonProjectsMap = new HashMap<>();
            if (user != null) {
                for (UserEntity userF : usersFriends) {
                    List<Project> commonProjects = userService.findCommonProjects(user, userF); // find common projects
                    commonProjectsMap.put(userF, commonProjects != null ? commonProjects : new ArrayList<>()); // add value into map
                }
                usersFriends.remove(user); // the user will not see himself in the list of users
            }
            model.addAttribute("userFriends",usersFriends);
            model.addAttribute("user", user);
            model.addAttribute("commonProjectsMap", commonProjectsMap);
            logger.info("search logic are working for users projects");
            return "home-page :: userFriends"; // userFriends fragment
        } else if (type.equals("invitations")) {
            List<UserEntity> users = userService.search(query,type);
            Map<UserEntity, List<Project>> commonProjectsMap = new HashMap<>();
            if (user != null) {
                for (UserEntity userF : users) {
                    List<Project> commonProjects = userService.findCommonProjects(user, userF); // find common projects
                    commonProjectsMap.put(userF, commonProjects != null ? commonProjects : new ArrayList<>()); // add value into map
                }
                users.remove(user); // the user will not see himself in the list of users
            }
            model.addAttribute("friendsInvitations", userService.search(query,type));
            model.addAttribute("commonProjectsMap", commonProjectsMap);
            model.addAttribute("user", user);
            return  "home-page :: userFriendsInvList";
        }
        return "redirect:/home";
    }
}
