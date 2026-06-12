package org.example.yci_web.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "documents")
public class DocumentsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocument;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @Lob
    @Column(name = "image_front", columnDefinition = "LONGBLOB")
    private byte[] imageFront;

    @Lob
    @Column(name = "image_back", columnDefinition = "LONGBLOB")
    private byte[] imageBack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_booking")
    private BookingEntity bookingEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status")
    private StatusEntity statusEntity;
}
