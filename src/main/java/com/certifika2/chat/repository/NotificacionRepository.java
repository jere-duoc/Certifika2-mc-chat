package com.certifika2.chat.repository;
import com.certifika2.chat.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, String> {
    @Query("select n.eventoId as eventId, count(n) as delivered, sum(case when n.leida = true then 1 else 0 end) as readCount from Notificacion n where n.eventoId in :ids group by n.eventoId")
    List<DeliveryCount> deliveryCounts(@Param("ids") List<String> ids);
    interface DeliveryCount {
        String getEventId();
        long getDelivered();
        long getReadCount();
    }
    List<Notificacion> findTop50ByUsuarioIdOrderByFechaCreacionDesc(String usuarioId);
    long countByUsuarioIdAndLeidaFalse(String usuarioId);
    Optional<Notificacion> findByIdAndUsuarioId(String id, String usuarioId);
}
