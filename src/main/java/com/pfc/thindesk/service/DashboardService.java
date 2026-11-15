package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Chamado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final ChamadoService chamados;

    public DashboardService(ChamadoService chamados) {
        this.chamados = chamados;
    }

    public record Result(long total, long atendidos, long atendidosSemData,
                         Map<String, Long> situacoes, Map<LocalDate, Long> atendidosPorDia,
                         List<TipoTotal> tiposAtendidos) {}
    public record TipoTotal(String tipo, long total) {}

    public Result summary(LocalDate inicio, LocalDate fim, String status, String tipo, String tecnico) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data inicial deve anteceder ou coincidir com a final.");
        }
        var selecionados = chamados.listarChamados().stream()
                .filter(c -> matches(status, situacao(c)) && matches(tipo, label(c.getTipo()))
                        && matches(tecnico, label(c.getTecnico())))
                .filter(c -> inicio == null && fim == null || within(c.getDataConclusao(), inicio, fim))
                .toList();
        var atendidos = selecionados.stream().filter(c -> concluido(c.getStatus())).toList();
        var porDia = atendidos.stream().filter(c -> c.getDataConclusao() != null)
                .collect(Collectors.groupingBy(Chamado::getDataConclusao, TreeMap::new, Collectors.counting()));
        var ranking = count(atendidos, c -> label(c.getTipo())).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .map(e -> new TipoTotal(e.getKey(), e.getValue())).toList();
        long semData = atendidos.stream().filter(c -> c.getDataConclusao() == null).count();
        return new Result(selecionados.size(), atendidos.size(), semData,
                count(selecionados, DashboardService::situacao), porDia, ranking);
    }

    public Map<String, List<String>> filters() {
        var todos = chamados.listarChamados();
        return Map.of("status", options(todos, DashboardService::situacao),
                "tipos", options(todos, c -> label(c.getTipo())),
                "tecnicos", options(todos, c -> label(c.getTecnico())));
    }

    static boolean concluido(String status) {
        return "CONCLUIDO".equals(normalize(status));
    }

    private static String situacao(Chamado chamado) {
        return switch (normalize(chamado.getStatus())) {
            case "ABERTO" -> "Aberto";
            case "EM_ANDAMENTO" -> "Em andamento";
            case "CONCLUIDO" -> "Concluído";
            default -> label(chamado.getStatus());
        };
    }

    private static boolean within(LocalDate date, LocalDate inicio, LocalDate fim) {
        return date != null && (inicio == null || !date.isBefore(inicio)) && (fim == null || !date.isAfter(fim));
    }

    private static String label(String value) {
        return value == null || value.isBlank() ? "Não informado" : value.strip();
    }

    private static String normalize(String value) {
        return Normalizer.normalize(label(value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private static boolean matches(String filter, String value) {
        return filter == null || filter.isBlank() || normalize(filter).equals(normalize(value));
    }

    private static Map<String, Long> count(List<Chamado> list, Function<Chamado, String> key) {
        return list.stream().collect(Collectors.groupingBy(key, TreeMap::new, Collectors.counting()));
    }

    private static List<String> options(List<Chamado> list, Function<Chamado, String> key) {
        return list.stream().map(key).distinct().sorted().toList();
    }
}
