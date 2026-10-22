
package senai499.com.br.expressocertorio.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Validator;
import senai499.com.br.expressocertorio.model.PerfilUsuario;
import senai499.com.br.expressocertorio.model.Usuario;
import senai499.com.br.expressocertorio.repository.UsuarioRepository;

@Controller
public class CadastroController {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Validator validator;

    public CadastroController(
            UsuarioRepository usuarioRepository,
            Validator validator) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.validator = validator;
    }

    @GetMapping("/criar-conta")
    public String exibirCadastro() {
        return "admin/criar-conta";
    }

    @PostMapping("/criar-conta")
    public String cadastrarUsuario(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) String senha,
            @RequestParam(required = false) String confirmarSenha,
            @RequestParam(required = false) String cep,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) String rua,
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) String complemento,
            @RequestParam(defaultValue = "false") boolean termos,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Mantém os dados preenchidos caso ocorra algum erro.
        model.addAttribute("nome", nome);
        model.addAttribute("email", email);
        model.addAttribute("telefone", telefone);
        model.addAttribute("cep", cep);
        model.addAttribute("estado", estado);
        model.addAttribute("cidade", cidade);
        model.addAttribute("bairro", bairro);
        model.addAttribute("rua", rua);
        model.addAttribute("numero", numero);
        model.addAttribute("complemento", complemento);

        // Verifica os campos obrigatórios.
        if (vazio(nome) || vazio(email) || vazio(senha)
                || vazio(confirmarSenha) || vazio(cep)
                || vazio(estado) || vazio(cidade) || vazio(bairro)
                || vazio(rua) || vazio(numero)) {

            model.addAttribute("erro",
                    "Preencha todos os campos obrigatórios.");

            return "admin/criar-conta";
        }

        nome = nome.trim();
        email = email.trim();
        cep = cep.trim();
        estado = estado.trim();
        cidade = cidade.trim();
        bairro = bairro.trim();
        rua = rua.trim();
        numero = numero.trim();

        if (senha.length() < 8) {
            model.addAttribute("erro",
                    "A senha deve ter pelo menos 8 caracteres.");

            return "admin/criar-conta";
        }

        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("erro",
                    "As senhas não coincidem.");

            return "admin/criar-conta";
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            model.addAttribute("erro",
                    "Informe um endereço de e-mail válido.");

            return "admin/criar-conta";
        }

        if (!termos) {
            model.addAttribute("erro",
                    "Você precisa aceitar os termos para continuar.");

            return "admin/criar-conta";
        }

        // Impede o cadastro de dois usuários com o mesmo e-mail.
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            model.addAttribute("erro",
                    "Este e-mail já está cadastrado.");

            return "admin/criar-conta";
        }

        // Cria o usuário com os dados informados.
        Usuario usuario = new Usuario();

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setTelefone(telefone == null ? null : telefone.trim());
        usuario.setCep(cep);
        usuario.setEstado(estado);
        usuario.setCidade(cidade);
        usuario.setBairro(bairro);
        usuario.setRua(rua);
        usuario.setNumero(numero);
        usuario.setComplemento(
                complemento == null || complemento.isBlank()
                        ? null
                        : complemento.trim());

        // O cadastro público sempre cria um cliente ativo.
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        usuario.setAtivo(true);

        // Valida as regras definidas nas anotações do modelo.
        var violacoes = validator.validate(usuario);

        if (!violacoes.isEmpty()) {
            model.addAttribute("erro",
                    violacoes.iterator().next().getMessage());

            return "admin/criar-conta";
        }

        // Salva no banco de dados.
        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute(
                "mensagem",
                "Conta criada com sucesso! Faça login para continuar.");

        return "redirect:/login";
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
