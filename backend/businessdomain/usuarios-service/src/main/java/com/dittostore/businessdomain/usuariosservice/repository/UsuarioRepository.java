package com.dittostore.businessdomain.usuariosservice.repository;

import com.dittostore.businessdomain.usuariosservice.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByAzureAdObjectId(String azureAdObjectId);

    Optional<Usuario> findByEmail(String email);

    boolean existsByAzureAdObjectId(String azureAdObjectId);
}