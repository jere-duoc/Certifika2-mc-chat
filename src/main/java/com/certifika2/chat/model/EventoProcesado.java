package com.certifika2.chat.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.Getter;

@Entity @Table(name = "eventos_curso_procesados") @NoArgsConstructor @Getter
public class EventoProcesado {
    @Id private String id;
    public EventoProcesado(String id) { this.id = id; }
}
