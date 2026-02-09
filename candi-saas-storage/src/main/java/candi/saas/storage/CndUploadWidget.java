package candi.saas.storage;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Candi widget that renders a file upload input with optional drag-and-drop zone
 * and image preview support.
 *
 * Usage in templates:
 * <pre>
 * {{ widget "cnd-upload" name="avatar" label="Profile Photo" accept="image/*" maxSize="5MB" preview=true }}
 * </pre>
 *
 * Params:
 *   name       - form field name (required)
 *   label      - display label
 *   accept     - MIME type filter (e.g., "image/*", "image/png,image/jpeg")
 *   maxSize    - human-readable max size (e.g., "10MB", "500KB")
 *   multiple   - allow multiple files (boolean)
 *   preview    - show image preview for selected file (boolean)
 *   currentUrl - URL of currently uploaded file (for edit forms)
 *   class      - additional CSS classes
 */
@Component("cnd-upload__Widget")
@Scope("prototype")
public class CndUploadWidget implements CandiComponent {

    private String name = "file";
    private String label = "";
    private String accept = "";
    private String maxSize = "";
    private boolean multiple = false;
    private boolean preview = false;
    private String currentUrl = "";
    private String cssClass = "";

    @Override
    public void setParams(Map<String, Object> params) {
        if (params.containsKey("name")) {
            this.name = String.valueOf(params.get("name"));
        }
        if (params.containsKey("label")) {
            this.label = String.valueOf(params.get("label"));
        }
        if (params.containsKey("accept")) {
            this.accept = String.valueOf(params.get("accept"));
        }
        if (params.containsKey("maxSize")) {
            this.maxSize = String.valueOf(params.get("maxSize"));
        }
        if (params.containsKey("multiple")) {
            this.multiple = toBoolean(params.get("multiple"));
        }
        if (params.containsKey("preview")) {
            this.preview = toBoolean(params.get("preview"));
        }
        if (params.containsKey("currentUrl")) {
            this.currentUrl = String.valueOf(params.get("currentUrl"));
        }
        if (params.containsKey("class")) {
            this.cssClass = String.valueOf(params.get("class"));
        }
    }

    @Override
    public void render(HtmlOutput out) {
        String widgetId = "cnd-upload-" + name;
        String containerClass = "cnd-upload" + (cssClass.isEmpty() ? "" : " " + cssClass);

        out.append("<div class=\"");
        out.appendEscaped(containerClass);
        out.append("\" id=\"");
        out.appendEscaped(widgetId);
        out.append("\">");

        // Label
        if (!label.isEmpty()) {
            out.append("<label class=\"cnd-upload__label\" for=\"");
            out.appendEscaped(widgetId + "-input");
            out.append("\">");
            out.appendEscaped(label);
            out.append("</label>");
        }

        // Current file preview (for edit forms)
        if (!currentUrl.isEmpty()) {
            out.append("<div class=\"cnd-upload__current\">");
            if (isImageUrl(currentUrl)) {
                out.append("<img src=\"");
                out.appendEscaped(currentUrl);
                out.append("\" alt=\"Current file\" class=\"cnd-upload__current-img\">");
            } else {
                out.append("<a href=\"");
                out.appendEscaped(currentUrl);
                out.append("\" target=\"_blank\" class=\"cnd-upload__current-link\">Current file</a>");
            }
            out.append("</div>");
        }

        // Dropzone
        out.append("<div class=\"cnd-upload__dropzone\" id=\"");
        out.appendEscaped(widgetId + "-dropzone");
        out.append("\">");
        out.append("<p class=\"cnd-upload__dropzone-text\">Drag and drop file here, or click to browse</p>");

        // File input
        out.append("<input type=\"file\" class=\"cnd-upload__input\" id=\"");
        out.appendEscaped(widgetId + "-input");
        out.append("\" name=\"");
        out.appendEscaped(name);
        out.append("\"");

        if (!accept.isEmpty()) {
            out.append(" accept=\"");
            out.appendEscaped(accept);
            out.append("\"");
        }

        if (multiple) {
            out.append(" multiple");
        }

        out.append(">");
        out.append("</div>"); // dropzone

        // Size hint
        if (!maxSize.isEmpty()) {
            out.append("<p class=\"cnd-upload__hint\">Maximum file size: ");
            out.appendEscaped(maxSize);
            out.append("</p>");
        }

        // Preview container
        if (preview) {
            out.append("<div class=\"cnd-upload__preview\" id=\"");
            out.appendEscaped(widgetId + "-preview");
            out.append("\"></div>");
        }

        out.append("</div>"); // cnd-upload

        // Inline script for drag-and-drop and preview
        renderScript(out, widgetId);
    }

    private void renderScript(HtmlOutput out, String widgetId) {
        out.append("<script>");
        out.append("(function(){");
        out.append("var dz=document.getElementById('");
        out.appendEscaped(widgetId + "-dropzone");
        out.append("');");
        out.append("var inp=document.getElementById('");
        out.appendEscaped(widgetId + "-input");
        out.append("');");

        // Click to open file dialog
        out.append("dz.addEventListener('click',function(){inp.click();});");

        // Drag-and-drop events
        out.append("dz.addEventListener('dragover',function(e){e.preventDefault();dz.classList.add('cnd-upload__dropzone--active');});");
        out.append("dz.addEventListener('dragleave',function(e){e.preventDefault();dz.classList.remove('cnd-upload__dropzone--active');});");
        out.append("dz.addEventListener('drop',function(e){e.preventDefault();dz.classList.remove('cnd-upload__dropzone--active');inp.files=e.dataTransfer.files;inp.dispatchEvent(new Event('change'));});");

        // Image preview
        if (preview) {
            out.append("inp.addEventListener('change',function(){");
            out.append("var pv=document.getElementById('");
            out.appendEscaped(widgetId + "-preview");
            out.append("');");
            out.append("pv.innerHTML='';");
            out.append("Array.from(inp.files).forEach(function(f){");
            out.append("if(f.type.startsWith('image/')){");
            out.append("var r=new FileReader();");
            out.append("r.onload=function(e){var img=document.createElement('img');img.src=e.target.result;img.className='cnd-upload__preview-img';pv.appendChild(img);};");
            out.append("r.readAsDataURL(f);");
            out.append("}});});");
        }

        out.append("})();");
        out.append("</script>");

        // Inline styles
        renderStyles(out);
    }

    private void renderStyles(HtmlOutput out) {
        out.append("<style>");
        out.append(".cnd-upload{margin-bottom:1rem;}");
        out.append(".cnd-upload__label{display:block;margin-bottom:0.5rem;font-weight:600;}");
        out.append(".cnd-upload__dropzone{border:2px dashed #cbd5e1;border-radius:8px;padding:2rem;text-align:center;cursor:pointer;transition:border-color 0.2s,background-color 0.2s;}");
        out.append(".cnd-upload__dropzone:hover,.cnd-upload__dropzone--active{border-color:#3b82f6;background-color:#eff6ff;}");
        out.append(".cnd-upload__dropzone-text{margin:0;color:#64748b;}");
        out.append(".cnd-upload__input{display:none;}");
        out.append(".cnd-upload__hint{margin-top:0.25rem;font-size:0.875rem;color:#94a3b8;}");
        out.append(".cnd-upload__preview{margin-top:0.5rem;display:flex;gap:0.5rem;flex-wrap:wrap;}");
        out.append(".cnd-upload__preview-img{max-width:120px;max-height:120px;border-radius:4px;object-fit:cover;}");
        out.append(".cnd-upload__current{margin-bottom:0.5rem;}");
        out.append(".cnd-upload__current-img{max-width:200px;max-height:200px;border-radius:4px;object-fit:cover;}");
        out.append(".cnd-upload__current-link{color:#3b82f6;text-decoration:underline;}");
        out.append("</style>");
    }

    private boolean isImageUrl(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".svg");
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean b) return b;
        return "true".equalsIgnoreCase(String.valueOf(value));
    }
}
