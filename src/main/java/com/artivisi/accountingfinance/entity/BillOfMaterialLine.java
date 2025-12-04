package com.artivisi.accountingfinance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bill of Materials Line - a single component in the BOM recipe.
 */
@Entity
@Table(name = "bill_of_material_lines")
@Getter
@Setter
@NoArgsConstructor
public class BillOfMaterialLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bill_of_material", nullable = false)
    private BillOfMaterial billOfMaterial;

    @NotNull(message = "Komponen wajib dipilih")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_component", nullable = false)
    private Product component;

    @NotNull(message = "Jumlah komponen wajib diisi")
    @Column(name = "quantity", precision = 15, scale = 4, nullable = false)
    private BigDecimal quantity;

    @Size(max = 255, message = "Catatan maksimal 255 karakter")
    @Column(name = "notes", length = 255)
    private String notes;

    @Column(name = "line_order", nullable = false)
    private int lineOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
