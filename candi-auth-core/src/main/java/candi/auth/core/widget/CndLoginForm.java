package candi.auth.core.widget;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Login form widget for Candi pages.
 *
 * <p>Usage in .jhtml templates:</p>
 * <pre>
 * {{ widget "cnd-login-form" action="/login" showRemember=true }}
 * </pre>
 *
 * <p>Renders a complete login form with username, password, optional remember-me
 * checkbox, and optional forgot-password link.</p>
 *
 * <p>CSS classes:</p>
 * <ul>
 *   <li>{@code cnd-login-form} — form wrapper</li>
 *   <li>{@code cnd-login-form__field} — each input group</li>
 *   <li>{@code cnd-login-form__submit} — submit button</li>
 * </ul>
 */
@Component("cnd-login-form__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndLoginForm implements CandiComponent {

    private String action = "/login";
    private String method = "POST";
    private boolean showRemember = false;
    private boolean showForgotPassword = false;
    private String forgotPasswordUrl = "/forgot-password";
    private String cssClass = "";

    @Override
    public void setParams(Map<String, Object> params) {
        if (params == null) {
            return;
        }
        if (params.containsKey("action")) {
            this.action = String.valueOf(params.get("action"));
        }
        if (params.containsKey("method")) {
            this.method = String.valueOf(params.get("method"));
        }
        if (params.containsKey("showRemember")) {
            this.showRemember = toBoolean(params.get("showRemember"));
        }
        if (params.containsKey("showForgotPassword")) {
            this.showForgotPassword = toBoolean(params.get("showForgotPassword"));
        }
        if (params.containsKey("forgotPasswordUrl")) {
            this.forgotPasswordUrl = String.valueOf(params.get("forgotPasswordUrl"));
        }
        if (params.containsKey("class")) {
            this.cssClass = String.valueOf(params.get("class"));
        }
    }

    @Override
    public void render(HtmlOutput out) {
        String formClass = "cnd-login-form";
        if (!cssClass.isEmpty()) {
            formClass += " " + cssClass;
        }

        out.append("<form class=\"");
        out.appendEscaped(formClass);
        out.append("\" action=\"");
        out.appendEscaped(action);
        out.append("\" method=\"");
        out.appendEscaped(method);
        out.append("\">\n");

        // Username field
        out.append("  <div class=\"cnd-login-form__field\">\n");
        out.append("    <label for=\"username\">Username</label>\n");
        out.append("    <input type=\"text\" id=\"username\" name=\"username\" required autocomplete=\"username\">\n");
        out.append("  </div>\n");

        // Password field
        out.append("  <div class=\"cnd-login-form__field\">\n");
        out.append("    <label for=\"password\">Password</label>\n");
        out.append("    <input type=\"password\" id=\"password\" name=\"password\" required autocomplete=\"current-password\">\n");
        out.append("  </div>\n");

        // Remember me checkbox
        if (showRemember) {
            out.append("  <div class=\"cnd-login-form__field\">\n");
            out.append("    <label>\n");
            out.append("      <input type=\"checkbox\" name=\"remember\" value=\"true\">\n");
            out.append("      Remember me\n");
            out.append("    </label>\n");
            out.append("  </div>\n");
        }

        // Submit button
        out.append("  <div class=\"cnd-login-form__submit\">\n");
        out.append("    <button type=\"submit\">Sign in</button>\n");
        out.append("  </div>\n");

        // Forgot password link
        if (showForgotPassword) {
            out.append("  <div class=\"cnd-login-form__field\">\n");
            out.append("    <a href=\"");
            out.appendEscaped(forgotPasswordUrl);
            out.append("\">Forgot password?</a>\n");
            out.append("  </div>\n");
        }

        out.append("</form>\n");
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof String s) {
            return "true".equalsIgnoreCase(s);
        }
        return false;
    }
}
