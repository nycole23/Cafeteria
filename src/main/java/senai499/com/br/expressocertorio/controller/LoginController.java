
package senai499.com.br.expressocertorio.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import senai499.com.br.expressocertorio.model.Usuario;
import senai499.com.br.expressocertorio.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public LoginController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Exibe a tela de login
    @GetMapping("/login")
    public String exibirLogin(
            @RequestParam(required = false) String erro,
            @RequestParam(required = false) String mensagem,
            Model model,
            HttpSession session) {

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

    // Processa o login

    @PostMapping("/login")
    public String realizarLogin(
            @RequestParam String email,
            @RequestParam String senha,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email.trim())
                .orElse(null);

        // Verifica se o usuário existe e está ativo.
        if (usuario == null || !usuario.isAtivo()) {
            return "redirect:/login?erro";
        }

        // Compara a senha informada com a senha criptografada.
        if (usuario.getSenha() == null
                || !passwordEncoder.matches(senha, usuario.getSenha())) {
            return "redirect:/login?erro";
        }

        // Invalida a sessão anterior, se existir.
        HttpSession sessaoAntiga = request.getSession(false);

        if (sessaoAntiga != null) {
            sessaoAntiga.invalidate();
        }

        // Cria uma nova sessão válida.
        HttpSession novaSessao = request.getSession(true);

        // Armazena os dados do usuário autenticado.
        novaSessao.setAttribute("usuarioId", usuario.getId());
        novaSessao.setAttribute("usuarioNome", usuario.getNome());
        novaSessao.setAttribute("usuarioEmail", usuario.getEmail());
        novaSessao.setAttribute("usuarioPerfil", usuario.getPerfil().name());

        // Redireciona conforme o perfil.
        return redirecionarPorPerfil(usuario.getPerfil().name());
    }


    // Encerra a sessão
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // Direciona cada perfil para sua área
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

    /*
     * O container cria uma nova sessão quando getSession()
     * é chamado após invalidar a sessão anterior.
     */
    private HttpSession criarNovaSessao(HttpSession sessaoAnterior) {
        return sessaoAnterior.getServletContext() == null
                ? sessaoAnterior
                : obterNovaSessao(sessaoAnterior);
    }

    private HttpSession obterNovaSessao(HttpSession sessaoAnterior) {
        // Este método será substituído pela criação correta da sessão.
        return sessaoAnterior;
    }
}
