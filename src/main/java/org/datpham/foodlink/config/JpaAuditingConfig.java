package org.datpham.foodlink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // TODO: Provide an AuditorAware bean if you need createdBy/updatedBy.
}
