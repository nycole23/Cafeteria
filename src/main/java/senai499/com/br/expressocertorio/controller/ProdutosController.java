
package senai499.com.br.expressocertorio.controller;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import senai499.com.br.expressocertorio.model.Produto;
import senai499.com.br.expressocertorio.repository.ProdutoRepository;

@Controller
public class ProdutosController {

    private final ProdutoRepository produtoRepository;

    public ProdutosController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping("/produtos")
    public String produtos(Model model) {

        List<Produto> produtos = produtoRepository
                .findByAtivoTrueOrderByCategoriaAscNomeAsc();

        List<String> categorias = Arrays.asList(
                "Bebidas quentes",
                "Bebidas geladas",
                "Padaria e confeitaria",
                "Lanches salgados",
                "Sobremesas",
                "Opções saudáveis"
        );

        Map<String, List<Produto>> produtosPorCategoria =
                new LinkedHashMap<>();

        for (String categoria : categorias) {
            List<Produto> produtosCategoria = produtos.stream()
                    .filter(p -> categoria.equals(p.getCategoria()))
                    .collect(Collectors.toList());

            produtosPorCategoria.put(categoria, produtosCategoria);
        }

        model.addAttribute("produtosPorCategoria", produtosPorCategoria);
        model.addAttribute("temProdutos", !produtos.isEmpty());

        return "produtos/produtos";
    }
}