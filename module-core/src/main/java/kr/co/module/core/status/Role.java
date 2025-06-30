package kr.co.module.core.status;

public enum Role {
    USER, ADMIN;

    public String getKey() {
        return "ROLE_" + this.name();
    }
}