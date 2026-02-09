package candi.web.seo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Candi SEO plugin.
 *
 * <pre>
 * candi:
 *   seo:
 *     site-name: My App
 *     base-url: https://myapp.com
 *     title-suffix: " | My App"
 * </pre>
 */
@ConfigurationProperties(prefix = "candi.seo")
public class SeoProperties {

    private String siteName = "";
    private String baseUrl = "http://localhost:8080";
    private String defaultImage = "";
    private String titleSuffix = "";

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(String defaultImage) {
        this.defaultImage = defaultImage;
    }

    public String getTitleSuffix() {
        return titleSuffix;
    }

    public void setTitleSuffix(String titleSuffix) {
        this.titleSuffix = titleSuffix;
    }
}
