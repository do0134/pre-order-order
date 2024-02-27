package com.example.order_service.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "\"order\"", indexes = {
        @Index(name = "user_id_index", columnList = "user_id"),
        @Index(name = "item_id_index", columnList = "item_id")
})
@SQLDelete(sql = "UPDATE \"order\" SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@Where(clause = "is_deleted = False")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "quantity")
    private Long quantity = 1L;

    @Column(name = "total_price")
    private Long totalPrice = 1000L;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = Boolean.FALSE;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;


    @PrePersist
    void createdAt() {
        this.createdAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    void updatedAt() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

    public static OrderEntity toEntity(Long userId, Long itemId) {
        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setItemId(itemId);
        return order;
    }
}
