
package senai499.com.br.expressocertorio.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import senai499.com.br.expressocertorio.model.PerfilUsuario;
import senai499.com.br.expressocertorio.model.Usuario;
import senai499.com.br.expressocertorio.repository.UsuarioRepository;

@Controller
public class PrimeiroAdminController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public PrimeiroAdminController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Abre a configuração inicial somente se ainda não houver ADMIN.
    @GetMapping("/configurar-admin")
    public String exibirFormulario(Model model) {

        if (jaExisteAdmin()) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", new Usuario());

        return "admin/primeiro-admin";
    }

    // Cria o primeiro administrador.
    @PostMapping("/configurar-admin")
    public String criarPrimeiroAdmin(
            Usuario usuario,
            String confirmarSenha,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Impede a criação de outro ADMIN por esta rota.
        if (jaExisteAdmin()) {
            return "redirect:/login";
        }

        // Confere os campos obrigatórios do cadastro.
        if (vazio(usuario.getNome())
                || vazio(usuario.getEmail())
                || vazio(usuario.getSenha())
                || vazio(usuario.getCep())
                || vazio(usuario.getEstado())
                || vazio(usuario.getCidade())
                || vazio(usuario.getBairro())
                || vazio(usuario.getRua())
                || vazio(usuario.getNumero())) {

            model.addAttribute(
                    "erro",
                    "Preencha todos os campos obrigatórios."
            );
            model.addAttribute("usuario", usuario);
            return "admin/primeiro-admin";
        }

        usuario.setNome(usuario.getNome().trim());
        usuario.setEmail(usuario.getEmail().trim());

        if (usuario.getNome().length() > 120
                || usuario.getEmail().length() > 160
                || usuario.getCep().length() > 9
                || usuario.getEstado().length() > 100
                || usuario.getCidade().length() > 100
                || usuario.getBairro().length() > 100
                || usuario.getRua().length() > 150
                || usuario.getNumero().length() > 15) {

            model.addAttribute("erro", "Um ou mais campos excedem o limite permitido.");
            model.addAttribute("usuario", usuario);
            return "admin/primeiro-admin";
        }

        // Valida formato básico do e-mail.
        if (!usuario.getEmail().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("erro", "Informe um e-mail válido.");
            model.addAttribute("usuario", usuario);
            return "admin/primeiro-admin";
        }

        // Verifica se o e-mail já está cadastrado.
        if (usuarioRepository.existsByEmailIgnoreCase(usuario.getEmail())) {
            model.addAttribute("erro", "Este e-mail já está cadastrado.");
            model.addAttribute("usuario", usuario);
            return "admin/primeiro-admin";
        }

        // Exige senha segura e confirmação.
        if (usuario.getSenha().length() < 8
                || confirmarSenha == null
                || !usuario.getSenha().equals(confirmarSenha)) {

            model.addAttribute(
                    "erro",
                    "A senha deve ter pelo menos 8 caracteres e as senhas devem ser iguais."
            );
            model.addAttribute("usuario", usuario);
            return "admin/primeiro-admin";
        }

        // Define o perfil no servidor. Nunca confia no formulário.
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);

        // Salva somente o hash da senha.
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        Usuario salvo = usuarioRepository.save(usuario);

        // Cria uma nova sessão depois do cadastro.
        HttpSession sessaoAntiga = request.getSession(false);

        if (sessaoAntiga != null) {
            sessaoAntiga.invalidate();
        }

        HttpSession sessao = request.getSession(true);
        sessao.setAttribute("usuarioId", salvo.getId());
        sessao.setAttribute("usuarioNome", salvo.getNome());
        sessao.setAttribute("usuarioEmail", salvo.getEmail());
        sessao.setAttribute("usuarioPerfil", salvo.getPerfil().name());

        redirectAttributes.addFlashAttribute(
                "mensagem",
                "Administrador criado com sucesso!"
        );

        return "redirect:/administrativo";
    }

    private boolean jaExisteAdmin() {
        return usuarioRepository.countByPerfil(PerfilUsuario.ADMIN) > 0;
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
