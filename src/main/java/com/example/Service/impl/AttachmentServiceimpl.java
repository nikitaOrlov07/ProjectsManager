package com.example.Service.impl;

import com.example.Model.Attachment;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Repository.AttachmentRepository;
import com.example.Security.SecurityUtil;
import com.example.Service.AttachmentService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AttachmentServiceimpl implements AttachmentService {
    private ProjectService projectService;
    private UserService userService;
    private AttachmentRepository attachmentRepository;

    public AttachmentServiceimpl(ProjectService projectService, UserService userService, AttachmentRepository attachmentRepository) {
        this.projectService = projectService;
        this.userService = userService;
        this.attachmentRepository =  attachmentRepository;
    }

    @Override
    public Attachment saveAttachment(MultipartFile file,Project project,  UserEntity user) throws Exception {
     String fileName = StringUtils.cleanPath(file.getOriginalFilename());//returns the name of the uploaded file as it was on the client (user) side, including the file path if one was specified.
        try
        {
           if(fileName.contains(".."))
           {
               throw  new Exception("Filename contains invalid path sequence" + fileName);
           }



            Attachment attachment = new Attachment(fileName,
                                                   file.getContentType(), // The MIME type of a file -> is metadata that describes the type of file, e.g. text file, image, PDF, etc.
                                                   file.getBytes()); // returns the file contents as a byte array
            attachment.setCreator(user.getUsername());
            attachment.setProject(project);
            return attachmentRepository.save(attachment);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            throw  new Exception("Could not save File: "+fileName);
        }
    }

    @Override
    public Attachment getAttachment(Long fileId) throws Exception {
        return attachmentRepository.findById(fileId).orElseThrow(()-> new Exception("File not found with id "+ fileId));

    }
    @Transactional
    @Override
    public List<Attachment> findAllByProject(Project project) {
        return attachmentRepository.findAllByProject(project);
    }
}
