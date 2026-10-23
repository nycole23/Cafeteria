
package senai499.com.br.expressocertorio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import senai499.com.br.expressocertorio.model.Usuario;
import senai499.com.br.expressocertorio.model.PerfilUsuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca o usuário pelo e-mail, sem diferenciar maiúsculas e minúsculas.
    Optional<Usuario> findByEmailIgnoreCase(String email);

    // Verifica se o e-mail já está cadastrado.
    boolean existsByEmailIgnoreCase(String email);

    // Conta os usuários de determinado perfil.
    long countByPerfil(PerfilUsuario perfil);

    // Conta somente os usuários ativos de determinado perfil.
    long countByPerfilAndAtivoTrue(PerfilUsuario perfil);
}


