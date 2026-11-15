package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Chamado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import com.pfc.thindesk.repository.ChamadoRepository;

@Service
public class ChamadoService {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    @Autowired
    private ChamadoRepository chamadoRepository;

    public Chamado criarChamado(Chamado chamado) {
        chamado.setDataConclusao(DashboardService.concluido(chamado.getStatus()) ? LocalDate.now(ZONA) : null);
        return chamadoRepository.save(chamado);
    }

    public List<Chamado> listarChamados() {
        return chamadoRepository.findAll();
    }

    public Chamado atualizarChamado(String id, Chamado chamadoAtualizado) {
        return chamadoRepository.findById(id)
                .map(chamado -> {
                    chamado.setDescricao(chamadoAtualizado.getDescricao());
                    boolean concluido = DashboardService.concluido(chamadoAtualizado.getStatus());
                    if (!concluido) {
                        chamado.setDataConclusao(null);
                    } else if (!DashboardService.concluido(chamado.getStatus())) {
                        chamado.setDataConclusao(LocalDate.now(ZONA));
                    }
                    chamado.setStatus(chamadoAtualizado.getStatus());
                    chamado.setTipo(chamadoAtualizado.getTipo());
                    chamado.setTecnico(chamadoAtualizado.getTecnico());
                    chamado.setUsuario(chamadoAtualizado.getUsuario());
                    return chamadoRepository.save(chamado);
                })
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado com id: " + id));
    }

}
