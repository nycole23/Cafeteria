
package senai499.com.br.expressocertorio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import senai499.com.br.expressocertorio.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca um usuário pelo e-mail, ignorando maiúsculas e minúsculas
    Optional<Usuario> findByEmailIgnoreCase(String email);

    // Verifica se já existe uma conta cadastrada com esse e-mail
    boolean existsByEmailIgnoreCase(String email);
}
