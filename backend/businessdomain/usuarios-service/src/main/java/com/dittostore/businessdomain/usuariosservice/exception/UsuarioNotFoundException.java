package com.dittostore.businessdomain.usuariosservice.exception;

public class UsuarioNotFoundException extends RuntimeException {

    public UsuarioNotFoundException(String mensaje) {
        super(mensaje);
    }
}