package candi.web.routes;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for Candi Web Routes.
 * Registers the route registry, template helper, and supporting beans.
 */
@AutoConfiguration
@ComponentScan("candi.web.routes")
public class CandiWebRoutesAutoConfiguration {
}
