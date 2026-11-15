package com.pfc.thindesk.controller;

import com.pfc.thindesk.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {
    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/dashboard")
    public String page() {
        return "dashboard";
    }

    @GetMapping("/api/dashboard")
    @ResponseBody
    public DashboardService.Result summary(
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "tipo", required = false) String tipo,
            @RequestParam(name = "tecnico", required = false) String tecnico) {
        return dashboard.summary(inicio, fim, status, tipo, tecnico);
    }

    @GetMapping("/api/dashboard/filtros")
    @ResponseBody
    public Map<String, List<String>> filters() {
        return dashboard.filters();
    }
}
