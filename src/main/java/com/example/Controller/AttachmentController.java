package com.example.Controller;

import com.example.Dto.ResponseData;
import com.example.Model.Attachment;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Security.SecurityUtil;
import com.example.Service.AttachmentService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.HttpStatus;

import java.net.URI;

@RestController
public class AttachmentController {
    private AttachmentService attachmentService; private ProjectService projectService;private UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(AttachmentController.class);

    public AttachmentController(AttachmentService attachmentService, ProjectService projectService,UserService userService) {
        this.attachmentService = attachmentService;
        this.projectService = projectService;
        this.userService = userService;
    }
    @PostMapping("/upload/{projectId}")
    public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file,
                                   @PathVariable("projectId") Long projectId) throws Exception {
        Project project = projectService.findById(projectId);
        UserEntity user = userService.findByUsername(SecurityUtil.getSessionUser());
        Attachment attachment = null;
        if(user == null || !project.getInvolvedUsers().contains(user))
        {
            String redirectUrl = "/projects/" + projectId;
            URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path(redirectUrl)
                    .build()
                    .toUri();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(location)
                    .build();
        }
        attachment = attachmentService.saveAttachment(file,project,user);
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath() //This means that the initial URL will include the schema (http or https), the domain, and the root context of the application (if any).
                                                 .path("/download/") // then to path add "/download/" path
                                                 .path(String.valueOf(attachment.getId())) // and add attachment id to the path
                                                 .toUriString(); // converts the constructed URI object into a string -> building a URL that can be used to download a file.
        logger.info("Download URL: "+ downloadUrl);

        String redirectUrl = "/projects/" + projectId;
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(redirectUrl)
                .build()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .build();
    }
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileId") Long fileId) throws Exception // method will return file content and file metadata
    {
        Attachment attachment = null;
        attachment = attachmentService.getAttachment(fileId);
        return ResponseEntity.ok() // request was successful
                .contentType(MediaType.parseMediaType(attachment.getFileType())) // This sets the Content-Type of the file using the fileType stored in the attachment object. MediaType.parseMediaType() converts the string representation of the file type into a MediaType object.
                .header(HttpHeaders.CONTENT_DISPOSITION ,
                        "attachment;filename=\"" + attachment.getFileName()+"\"") // Sets the Content-Disposition header. The value "attachment;filename=\"" indicates to the browser that the file should be displayed as an attachment and not opened in the browser. attachment.getFileName() is used to set the file name.
                .body( new ByteArrayResource(attachment.getData())); // we return the contents of the file as a resource of type ByteArrayResource. This allows Spring to treat the byte array as a data stream for the HTTP response.
    }
}
