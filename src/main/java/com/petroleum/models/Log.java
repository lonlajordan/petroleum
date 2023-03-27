package com.petroleum.models;

import com.petroleum.enums.Level;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Enumerated(EnumType.STRING)
    private Level level = Level.INFO;
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String message = "";
    @Column(columnDefinition = "LONGTEXT")
    private String details = "";
    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime date = LocalDateTime.now();
}
