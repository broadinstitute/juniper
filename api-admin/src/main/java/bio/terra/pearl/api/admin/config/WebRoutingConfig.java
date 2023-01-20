package bio.terra.pearl.api.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebRoutingConfig implements WebMvcConfigurer {
  /**
   * Ensure client-side paths redirect to index.html because client handles routing. NOTE: Do NOT
   * use @EnableWebMvc or it will break this.
   */
  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    //    registry.addViewController("/{spring:\\w+}").setViewName("forward:/");
    //    registry.addViewController("/*/**").setViewName("forward:/");
  }
}
