package com.sloyt.jrdemo.reporting;

import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.NonNull;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.UUID;

public class Report {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String reportsFolder = "reports";

    private JasperReport report;
    private JasperPrint print;

    public Report(@NonNull String reportName) {
        report = this.compile(reportName);
    }

    private JasperReport compile(@NonNull String reportName) {
        String reportFileName = reportsFolder + "/" + reportName;

        if (!reportName.endsWith(".jrxml")) {
            reportFileName += ".jrxml";
        }

        ClassPathResource sourceFile = new ClassPathResource(reportFileName);

        if (sourceFile.exists()) {
            try {
                return JasperCompileManager.compileReport(sourceFile.getFile().getAbsolutePath());
            } catch(Exception ex) {
                logger.error("couldn't compile report \"" + reportName + "\": " + ex.getMessage());
            }
        } else {
            logger.error(".jrxml file for specified report \"" + reportName + "\" wasn't found");
        }

        return null;
    }

    public boolean fill(String connectionUrl, Map<String, Object> params) {
        boolean fillStatus = true;

        Connection connection = null;

        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(connectionUrl);
        } catch(Exception ex) {
            fillStatus = false;
            logger.error("couldn't connect to the datasource: " + ex.getMessage());
        }

        if (fillStatus) {
            try {
                print = JasperFillManager.fillReport(report, params, connection);
            } catch (Exception ex) {
                fillStatus = false;
                logger.error("couldn't fill report: " + ex.getMessage());
            }
        }

        return fillStatus;
    }

    public boolean fill(String connectionUrl) {
        return fill(connectionUrl, null);
    }

    public String getHtml() {
        String reportHtml  = "";

        if (report == null) {
            logger.warn("no report found");
        } else if (print == null) {
            logger.warn("no filled report found");
        } else {
            try {
                File htmlFile = File.createTempFile(UUID.randomUUID().toString().replace("-", ""), ".tmp");

//                logger.info("temp html file: " + htmlFile.getAbsolutePath());

                JasperExportManager.exportReportToHtmlFile(print, htmlFile.getAbsolutePath());

                byte[] htmlFileContent = Files.readAllBytes(Paths.get(htmlFile.getAbsolutePath()));
                reportHtml = new String(htmlFileContent, Charset.defaultCharset());

                htmlFile.delete();
            } catch (Exception ex) {
                logger.error("couldn't export report to html: " + ex.getMessage());
            }
        }

        return reportHtml;
    }
}
