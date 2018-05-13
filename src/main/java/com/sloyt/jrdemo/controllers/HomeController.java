package com.sloyt.jrdemo.controllers;

import com.sloyt.jrdemo.reporting.Report;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index(@RequestParam(value = "page", required = false) Integer _page,  Model model) {
        Integer page = ((_page == null) || (_page < 1)) ? 1 : _page;

        Report report = new Report("hosts");

        Map<String, Object> reportParams = new HashMap<String, Object>() {{
            put("page", page);
            put("items_per_page", 20);
        }};
        report.fill("jdbc:postgresql://10.210.12.203:5432/depo?user=django&password=123123", reportParams);

        model.addAttribute("page", page);
        model.addAttribute("reportHtml", report.getHtml());

        return "index";
    }
}
