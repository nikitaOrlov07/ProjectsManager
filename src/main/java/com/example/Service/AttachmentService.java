package com.example.Service;

import com.example.Model.Attachment;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    Attachment saveAttachment(MultipartFile file, Project project, UserEntity user) throws Exception;

    Attachment getAttachment(Long fileId) throws Exception;

    @Transactional
    List<Attachment> findAllByProject(Project project);
}
