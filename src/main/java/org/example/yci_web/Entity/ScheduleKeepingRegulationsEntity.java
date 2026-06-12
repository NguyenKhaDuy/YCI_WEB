package org.example.yci_web.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "schedule_keeping_regulations_entity")
public class ScheduleKeepingRegulationsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idScheduleKeepingRegulations;

    @Column(name = "schedule_keeping_regulation")
    private String scheduleKeepingRegulation;

    @Column(name = "created_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updatedAt;
}
