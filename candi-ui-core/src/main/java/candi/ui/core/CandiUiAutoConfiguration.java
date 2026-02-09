package candi.ui.core;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for Candi UI Core widgets.
 * Registers all widget components in the candi.ui.core package.
 */
@AutoConfiguration
@ComponentScan("candi.ui.core")
public class CandiUiAutoConfiguration {
}
