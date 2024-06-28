package com.example.Model;

import com.example.Model.Security.UserEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String fileType;
    private String creator;
    private String downloadUrl;
    private String viewUrl;
    private String timestamp;


    @Lob // that a class property must be mapped to a larger object in the database.
    private byte[] data ;

    public Attachment(String fileName, String fileType, byte[] data, String downloadUrl ,String viewUrl) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
        this.downloadUrl=downloadUrl;
        this.viewUrl= viewUrl;
    }
    @ToString.Exclude
    @JsonBackReference // to eliminate recursion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Project project;

}
