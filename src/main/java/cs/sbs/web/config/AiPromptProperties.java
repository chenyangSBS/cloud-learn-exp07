package cs.sbs.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiPromptProperties {

    private String supportSystemPrompt;

    private String adminSystemPrompt;

    public String getSupportSystemPrompt() {
        return supportSystemPrompt;
    }

    public void setSupportSystemPrompt(String supportSystemPrompt) {
        this.supportSystemPrompt = supportSystemPrompt;
    }

    public String getAdminSystemPrompt() {
        return adminSystemPrompt;
    }

    public void setAdminSystemPrompt(String adminSystemPrompt) {
        this.adminSystemPrompt = adminSystemPrompt;
    }
}
