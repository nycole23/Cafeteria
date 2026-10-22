
package senai499.com.br.expressocertorio.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import senai499.com.br.expressocertorio.model.Usuario;
import senai499.com.br.expressocertorio.repository.UsuarioRepository;

@Controller
public class ClienteController {

    private final UsuarioRepository usuarioRepository;

    // Injeta o repositório no controller
    public ClienteController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/cliente/minha-conta")
    public String minhaConta(HttpSession session, Model model) {

        Object id = session.getAttribute("usuarioId");
        Object perfil = session.getAttribute("usuarioPerfil");

        // Verifica se o usuário está logado e é cliente
        if (!(id instanceof Long) || !"CLIENTE".equals(perfil)) {
            return "redirect:/login";
        }

        // Busca o usuário no banco de dados
        Usuario usuario = usuarioRepository.findById((Long) id)
                .orElse(null);

        // Verifica se o usuário existe e está ativo
        if (usuario == null || !usuario.isAtivo()) {
            session.invalidate();
            return "redirect:/login";
        }

        // Envia os dados para o Thymeleaf
        model.addAttribute("usuario", usuario);

        return "cliente/minha-conta";
    }
}
