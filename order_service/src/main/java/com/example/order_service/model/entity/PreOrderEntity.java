package com.example.order_service.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@Table(name = "\"pre_order\"")
@SQLDelete(sql = "UPDATE \"pre_order\" SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@Where(clause = "is_deleted = False")
public class PreOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salesItemId")
    private Long salesItemId;

    @Column(name = "userId")
    private Long userId;


}
