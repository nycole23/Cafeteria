
package senai499.com.br.expressocertorio.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import senai499.com.br.expressocertorio.model.Usuario;
import senai499.com.br.expressocertorio.repository.UsuarioRepository;

@Controller
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Exibe a tela de login.
    @GetMapping("/login")
    public String exibirLogin(
            @RequestParam(required = false) String erro,
            @RequestParam(required = false) String mensagem,
            Model model,
            HttpSession session) {

        // Se já estiver autenticado, direciona para a área correspondente.
        if (session.getAttribute("usuarioId") != null) {
            return redirecionarPorPerfil(
                    (String) session.getAttribute("usuarioPerfil")
            );
        }

        if (erro != null) {
            model.addAttribute(
                    "erro",
                    "E-mail ou senha inválidos, ou conta desativada."
            );
        }

        if (mensagem != null) {
            model.addAttribute("mensagem", mensagem);
        }

        return "admin/login";
    }

    // Processa as credenciais enviadas pelo formulário.
    @PostMapping("/login")
    public String realizarLogin(
            @RequestParam String email,
            @RequestParam String senha,
            HttpServletRequest request) {

        if (email == null || email.isBlank()
                || senha == null || senha.isBlank()) {
            return "redirect:/login?erro";
        }

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email.trim())
                .orElse(null);

        // Confere se a conta existe e está ativa.
        if (usuario == null || !usuario.isAtivo()) {
            return "redirect:/login?erro";
        }

        // Compara a senha digitada com o hash armazenado no banco.
        if (usuario.getSenha() == null
                || !passwordEncoder.matches(senha, usuario.getSenha())) {
            return "redirect:/login?erro";
        }

        // Invalida a sessão anterior, se houver.
        HttpSession sessaoAntiga = request.getSession(false);

        if (sessaoAntiga != null) {
            sessaoAntiga.invalidate();
        }

        // Cria uma nova sessão após a autenticação.
        HttpSession novaSessao = request.getSession(true);

        novaSessao.setAttribute("usuarioId", usuario.getId());
        novaSessao.setAttribute("usuarioNome", usuario.getNome());
        novaSessao.setAttribute("usuarioEmail", usuario.getEmail());
        novaSessao.setAttribute("usuarioPerfil", usuario.getPerfil().name());

        // Direciona conforme o perfil do usuário.
        return redirecionarPorPerfil(usuario.getPerfil().name());
    }

    // Encerra a sessão.
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // Define a página inicial de cada perfil.
    private String redirecionarPorPerfil(String perfil) {

        if (perfil == null) {
            return "redirect:/login";
        }

        switch (perfil) {
            case "ADMIN":
                return "redirect:/administrativo";

            case "FUNCIONARIO":
                return "redirect:/atendimento";

            case "CLIENTE":
                return "redirect:/cliente/minha-conta";

            default:
                return "redirect:/login";
        }
    }
}
