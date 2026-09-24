package bio.terra.pearl.api.admin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rejects requests with ';' path parameters (e.g. /api;x/portals/...). Tomcat strips path
 * parameters before routing, so the path this app routes on can differ from the path the auth proxy
 * matched its rules against -- which would let a request skip the proxy's token check. Nothing in
 * the app uses path parameters.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RejectPathParametersFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (request.getRequestURI().contains(";")) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    filterChain.doFilter(request, response);
  }
}
