package com.financialapp.investments.infrastructure.persistence.entity;

import com.financialapp.commons.messaging.infrastructure.persistence.entity.OutboxRecordEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "outbox_event", schema = "investments")
@Getter
@Setter
public class OutboxEventEntity extends OutboxRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
