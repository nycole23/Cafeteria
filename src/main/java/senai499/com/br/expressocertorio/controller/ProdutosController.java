
package senai499.com.br.expressocertorio.controller;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import senai499.com.br.expressocertorio.model.PerfilUsuario;
import senai499.com.br.expressocertorio.model.Produto;
import senai499.com.br.expressocertorio.repository.ProdutoRepository;

@Controller
public class ProdutosController {

    private final ProdutoRepository produtoRepository;

    private static final List<String> CATEGORIAS = Arrays.asList(
            "Bebidas quentes",
            "Bebidas geladas",
            "Padaria e confeitaria",
            "Lanches salgados",
            "Sobremesas",
            "Opções saudáveis"
    );

    public ProdutosController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    // =====================================================
    // CARDÁPIO PÚBLICO
    // =====================================================

    @GetMapping("/produtos")
    public String produtos(Model model) {

        List<Produto> produtos = produtoRepository
                .findByAtivoTrueOrderByCategoriaAscNomeAsc();

        Map<String, List<Produto>> produtosPorCategoria =
                new LinkedHashMap<>();

        for (String categoria : CATEGORIAS) {
            List<Produto> produtosCategoria = produtos.stream()
                    .filter(p -> categoria.equals(p.getCategoria()))
                    .collect(Collectors.toList());

            produtosPorCategoria.put(categoria, produtosCategoria);
        }

        model.addAttribute("produtosPorCategoria", produtosPorCategoria);
        model.addAttribute("temProdutos", !produtos.isEmpty());

        return "produtos/produtos";
    }

    // =====================================================
    // LISTAGEM ADMINISTRATIVA
    // =====================================================

    @GetMapping("/administrativo/produtos")
    public String listarProdutos(
            HttpSession session,
            Model model) {

        if (!ehAdmin(session)) {
            return redirecionarAcesso(session);
        }

        // Inclui produtos ativos e inativos.
        model.addAttribute("produtos", produtoRepository.findAll());

        return "admin/produtos";
    }

    // =====================================================
    // FORMULÁRIO DE CADASTRO
    // =====================================================

    @GetMapping("/administrativo/produtos/novo")
    public String novoProduto(
            HttpSession session,
            Model model) {

        if (!ehAdmin(session)) {
            return redirecionarAcesso(session);
        }

        model.addAttribute("produto", new Produto());
        model.addAttribute("categorias", CATEGORIAS);

        return "admin/produto-form";
    }

    // =====================================================
    // FORMULÁRIO DE EDIÇÃO
    // =====================================================

    @GetMapping("/administrativo/produtos/editar/{id}")
    public String editarProduto(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!ehAdmin(session)) {
            return redirecionarAcesso(session);
        }

        Produto produto = produtoRepository.findById(id).orElse(null);

        if (produto == null) {
            redirectAttributes.addFlashAttribute(
                    "erro", "Produto não encontrado."
            );

            return "redirect:/administrativo/produtos";
        }

        model.addAttribute("produto", produto);
        model.addAttribute("categorias", CATEGORIAS);

        return "admin/produto-form";
    }

    // =====================================================
    // SALVAR NOVO PRODUTO OU ATUALIZAR PRODUTO EXISTENTE
    // =====================================================

    @PostMapping("/administrativo/produtos/salvar")
    public String salvarProduto(
            @ModelAttribute("produto") Produto formulario,
            @RequestParam(required = false) Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!ehAdmin(session)) {
            return redirecionarAcesso(session);
        }

        // Validação básica dos campos.
        if (formulario.getNome() == null
                || formulario.getNome().isBlank()
                || formulario.getCategoria() == null
                || !CATEGORIAS.contains(formulario.getCategoria())
                || formulario.getPreco() == null
                || formulario.getPreco().signum() <= 0) {

            model.addAttribute(
                    "erro",
                    "Informe um nome, uma categoria válida e um preço maior que zero."
            );

            model.addAttribute("categorias", CATEGORIAS);
            model.addAttribute("produto", formulario);

            return "admin/produto-form";
        }

        Produto produto;

        if (id != null) {
            // Busca o registro real para não criar outro produto
            // quando a intenção é editar.
            produto = produtoRepository.findById(id).orElse(null);

            if (produto == null) {
                redirectAttributes.addFlashAttribute(
                        "erro", "Produto não encontrado."
                );

                return "redirect:/administrativo/produtos";
            }
        } else {
            produto = new Produto();
            produto.setAtivo(true);
        }

        // Atualiza os dados permitidos do produto.
        produto.setNome(formulario.getNome().trim());
        produto.setDescricao(formulario.getDescricao());
        produto.setPreco(formulario.getPreco());
        produto.setImagem(formulario.getImagem());
        produto.setCategoria(formulario.getCategoria());

        produtoRepository.save(produto);

        redirectAttributes.addFlashAttribute(
                "mensagem",
                id == null
                        ? "Produto cadastrado com sucesso!"
                        : "Produto atualizado com sucesso!"
        );

        return "redirect:/administrativo/produtos";
    }

    // =====================================================
    // ATIVAR OU DESATIVAR PRODUTO
    // =====================================================

    @PostMapping("/administrativo/produtos/alternar-status/{id}")
    public String alternarStatus(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!ehAdmin(session)) {
            return redirecionarAcesso(session);
        }

        Produto produto = produtoRepository.findById(id).orElse(null);

        if (produto == null) {
            redirectAttributes.addFlashAttribute(
                    "erro", "Produto não encontrado."
            );

            return "redirect:/administrativo/produtos";
        }

        produto.setAtivo(!produto.isAtivo());
        produtoRepository.save(produto);

        redirectAttributes.addFlashAttribute(
                "mensagem",
                produto.isAtivo()
                        ? "Produto ativado com sucesso!"
                        : "Produto desativado com sucesso!"
        );

        return "redirect:/administrativo/produtos";
    }

    // =====================================================
    // VERIFICAÇÃO DE ACESSO ADMINISTRATIVO
    // =====================================================

    private boolean ehAdmin(HttpSession session) {

        Object usuarioId = session.getAttribute("usuarioId");
        Object perfil = session.getAttribute("usuarioPerfil");

        return usuarioId != null
                && PerfilUsuario.ADMIN.name().equals(perfil);
    }

    private String redirecionarAcesso(HttpSession session) {

        Object usuarioId = session.getAttribute("usuarioId");
        Object perfil = session.getAttribute("usuarioPerfil");

        if (usuarioId == null || perfil == null) {
            return "redirect:/login";
        }

        if (PerfilUsuario.CLIENTE.name().equals(perfil)) {
            return "redirect:/cliente/minha-conta";
        }

        if (PerfilUsuario.FUNCIONARIO.name().equals(perfil)) {
            return "redirect:/atendimento";
        }

        session.invalidate();

        return "redirect:/login";
    }
}
