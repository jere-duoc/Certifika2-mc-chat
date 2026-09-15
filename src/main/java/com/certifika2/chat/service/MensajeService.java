package com.certifika2.chat.service;

import com.certifika2.chat.model.Mensaje;
import com.certifika2.chat.repository.MensajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MensajeService {

    private final MensajeRepository mensajeRepository;

    public Mensaje guardarMensaje(String idCurso, String idUsuarioEmisor, String nombreEmisor, String contenido) {
        Mensaje mensaje = new Mensaje();
        mensaje.setIdCurso(idCurso);
        mensaje.setIdUsuarioEmisor(idUsuarioEmisor);
        mensaje.setNombreEmisor(nombreEmisor);
        mensaje.setContenido(contenido);
        mensaje.setFechaEnvio(new Date());
        return mensajeRepository.save(mensaje);
    }

    public List<Mensaje> obtenerHistorial(String idCurso) {
        return mensajeRepository.findByIdCursoOrderByFechaEnvioAsc(idCurso);
    }
}
