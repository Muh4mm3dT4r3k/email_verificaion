package com.mohamed.emailVerification.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    ACTIVATION_CODE("activation-code");

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
