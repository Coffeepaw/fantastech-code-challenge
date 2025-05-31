package com.coffeepaw.smsapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sms")
public class Sms extends AuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "to_number")
    private String to;

    @Column(name = "from_number")
    private String from;

    @Column(name = "size")
    private int size;

    @Column(name = "parts")
    private int parts;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;

    @ElementCollection
    @CollectionTable(name = "sms_content", joinColumns = @JoinColumn(name = "sms_id"))
    @Column(name = "content_part")
    private List<String> content;


}
