package candi.ui.forms;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Auto-configuration for Candi UI Forms plugin.
 * Registers all form widgets (CndForm, CndInput, CndSelect) as Spring beans.
 */
@AutoConfiguration
@ComponentScan("candi.ui.forms")
public class CandiUiFormsAutoConfiguration {
}
