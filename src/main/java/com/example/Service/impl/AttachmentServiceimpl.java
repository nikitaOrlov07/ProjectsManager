package com.example.Service.impl;

import com.example.Model.Attachment;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Repository.AttachmentRepository;
import com.example.Service.AttachmentService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        public Attachment saveAttachment(MultipartFile file, Project project, UserEntity user) throws Exception {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            try {
                if(fileName.contains("..")) {
                    throw new Exception("Filename contains invalid path sequence" + fileName);
                }

                Attachment attachment = new Attachment(fileName,
                        file.getContentType(),
                        file.getBytes(),
                        null,
                        null
                );
                attachment.setCreator(user.getUsername());
                attachment.setProject(project);

                LocalDateTime currentDateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                attachment.setTimestamp(currentDateTime.format(formatter));

                return attachmentRepository.save(attachment);
            } catch (Exception exception) {
                exception.printStackTrace();
                throw new Exception("Could not save File: "+fileName);
            }
        }
    @Transactional
    @Override
    public void updateAttachmentUrls(Long id, String downloadUrl, String viewUrl) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
        attachment.setDownloadUrl(downloadUrl);
        attachment.setViewUrl(viewUrl);
        attachmentRepository.save(attachment);

    }
    @Transactional
    @Override
    public Attachment getAttachment(Long fileId) throws Exception {
        return attachmentRepository.findById(fileId).orElseThrow(()-> new Exception("File not found with id "+ fileId));

    }
    @Transactional
    @Override
    public List<Attachment> findAllByProject(Project project) {
        return attachmentRepository.findAllByProject(project);
    }

    @Override
    @Transactional // operations with large objects must be transactional
    public void deleteAttachment(Long projectId, Long fileId) throws Exception {
        Project project = projectService.findById(projectId);
        Attachment attachment = getAttachment(fileId);
        project.getAttachments().remove(attachment);
        attachmentRepository.delete(attachment);
    }

}
