
package senai499.com.br.expressocertorio.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import senai499.com.br.expressocertorio.model.PerfilUsuario;
import senai499.com.br.expressocertorio.model.Produto;
import senai499.com.br.expressocertorio.repository.ProdutoRepository;
import senai499.com.br.expressocertorio.repository.UsuarioRepository;

@Controller
public class AdminController {

    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public AdminController(
            ProdutoRepository produtoRepository,
            UsuarioRepository usuarioRepository) {

        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping({"/dashboard", "/administrativo"})
    public String dashboard(HttpSession session, Model model) {

        // Verifica se existe um usuário autenticado.
        Object usuarioId = session.getAttribute("usuarioId");
        Object perfil = session.getAttribute("usuarioPerfil");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        // Somente ADMIN pode acessar o painel administrativo.
        if (!PerfilUsuario.ADMIN.name().equals(perfil)) {

            // Redireciona os outros perfis para suas respectivas áreas.
            if (PerfilUsuario.CLIENTE.name().equals(perfil)) {
                return "redirect:/cliente/minha-conta";
            }

            if (PerfilUsuario.FUNCIONARIO.name().equals(perfil)) {
                return "redirect:/atendimento";
            }

            // Sessão inválida ou perfil desconhecido.
            session.invalidate();
            return "redirect:/login";
        }

        // Nome do administrador conectado.
        model.addAttribute(
                "usuarioNome",
                session.getAttribute("usuarioNome")
        );

        // Busca os produtos ativos, ordenados por categoria e nome.
        List<Produto> produtos =
                produtoRepository.findByAtivoTrueOrderByCategoriaAscNomeAsc();

        model.addAttribute("produtos", produtos);
        model.addAttribute("totalProdutos", produtos.size());

        // Quantidade de funcionários ativos cadastrados.
        long totalFuncionarios =
                usuarioRepository.countByPerfilAndAtivoTrue(
                        PerfilUsuario.FUNCIONARIO
                );

        model.addAttribute("totalFuncionarios", totalFuncionarios);

        return "admin/dashboard";
    }
}
