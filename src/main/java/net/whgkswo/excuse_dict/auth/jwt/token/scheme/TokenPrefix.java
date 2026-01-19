package net.whgkswo.excuse_dict.auth.jwt.token.scheme;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TokenPrefix {
    BEARER("Bearer "),
    ;

    String value;
}
