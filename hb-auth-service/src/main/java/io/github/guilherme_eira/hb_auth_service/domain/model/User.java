package io.github.guilherme_eira.hb_auth_service.domain.model;

public record User(
        String username,
        String email,
        String taxId,
        String firstName,
        String lastName,
        String password
){
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
