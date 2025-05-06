package com.example.forexproject.coordinator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to load and reload dynamic calculation formulas from scripts.
 */
@Component
@ConfigurationProperties(prefix = "calculation.formulas")
public class FormulaService {
    private static final Logger logger = LoggerFactory.getLogger(FormulaService.class);

    /**
     * Resource path pattern for formula scripts (e.g., classpath:formulas/*.groovy).
     */
    private String path = "classpath:formulas/*.groovy";

    private final Map<String, String> formulas = new ConcurrentHashMap<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getFormulas() {
        return formulas;
    }

    @PostConstruct
    public void loadFormulas() {
        reloadFormulas();
    }

    /**
     * Load or reload all formula scripts matching the configured path.
     */
    public void reloadFormulas() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(path);
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null && filename.endsWith(".groovy")) {
                    String name = filename.replaceFirst("\\.groovy$", "");
                    try (InputStream in = resource.getInputStream()) {
                        String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        formulas.put(name, content);
                        logger.info("Loaded formula script: {}", name);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error loading formulas from {}: {}", path, e.getMessage(), e);
        }
    }

    /**
     * Retrieve the script content for the given formula name.
     */
    public String getFormula(String name) {
        return formulas.get(name);
    }
}
