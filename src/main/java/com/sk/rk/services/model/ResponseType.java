package com.sk.rk.services.model;

public enum ResponseType {
    SUCCESS,
    ERROR,
    EXCEPTION,
    AUTHORIZED,
    UNAUTHROIZED;

    private ResponseType () {
    }
}
