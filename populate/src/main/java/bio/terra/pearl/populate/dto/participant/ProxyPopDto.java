package bio.terra.pearl.populate.dto.participant;

import lombok.Getter;

@Getter
public class ProxyPopDto {
    String username;
    String shortcode;
    boolean enrollAsProxy = true;
}
