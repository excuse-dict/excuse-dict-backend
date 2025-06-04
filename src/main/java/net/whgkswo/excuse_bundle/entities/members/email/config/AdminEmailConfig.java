package net.whgkswo.excuse_bundle.entities.members.email.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "admin")
@Data
public class AdminEmailConfig {
    private List<String> emails;
}
