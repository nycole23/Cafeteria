package senai499.com.br.expressocertorio.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import senai499.com.br.expressocertorio.model.ItemPedido;
import senai499.com.br.expressocertorio.model.Pedido;
import senai499.com.br.expressocertorio.model.Produto;
import senai499.com.br.expressocertorio.model.Usuario;
import senai499.com.br.expressocertorio.repository.PedidoRepository;
import senai499.com.br.expressocertorio.repository.ProdutoRepository;
import senai499.com.br.expressocertorio.repository.UsuarioRepository;

@Controller
public class PedidosController {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidosController(
            PedidoRepository pedidoRepository,
            ProdutoRepository produtoRepository,
            UsuarioRepository usuarioRepository) {

        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/pedidos")
    public String exibirPedidos(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String sucesso) {

        model.addAttribute(
                "produtos",
                produtoRepository.findByAtivoTrueOrderByCategoriaAscNomeAsc()
        );

        Object usuarioId = session.getAttribute("usuarioId");

        if (usuarioId instanceof Number) {
            Long id = ((Number) usuarioId).longValue();

            usuarioRepository.findById(id).ifPresent(usuario ->
                    model.addAttribute("usuario", usuario)
            );
        }

        if ("true".equals(sucesso)) {
            model.addAttribute("mensagem",
                    "Pedido registrado com sucesso!");
        }

        return "pedidos/pedidos";
    }

    @PostMapping("/pedidos")
    @Transactional
    public String finalizarPedido(
            @RequestParam Map<String, String> parametros,
            RedirectAttributes redirectAttributes) {

        String nome = parametros.getOrDefault("nome", "").trim();
        String telefone = parametros.getOrDefault("telefone", "").trim();
        String observacao = parametros.getOrDefault("observacao", "").trim();
        String pagamento = parametros.getOrDefault("pagamento", "");

        if (nome.isBlank() || telefone.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "erro", "Informe seu nome e telefone."
            );
            return "redirect:/pedidos";
        }

        if (!pagamento.equals("pix")
                && !pagamento.equals("cartao")
                && !pagamento.equals("dinheiro")) {

            redirectAttributes.addFlashAttribute(
                    "erro", "Selecione uma forma de pagamento válida."
            );
            return "redirect:/pedidos";
        }

        // Somente produtos ativos podem ser incluídos.
        Map<Long, Produto> produtosAtivos = new HashMap<>();

        for (Produto produto :
                produtoRepository.findByAtivoTrueOrderByCategoriaAscNomeAsc()) {
            produtosAtivos.put(produto.getId(), produto);
        }

        Pedido pedido = new Pedido();
        pedido.setNomeCliente(nome);
        pedido.setTelefone(telefone);
        pedido.setObservacao(observacao);
        pedido.setPagamento(pagamento);
        pedido.setStatus("PENDENTE");
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setTotal(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;
        int quantidadeTotal = 0;

        for (Map.Entry<String, String> entrada : parametros.entrySet()) {

            String chave = entrada.getKey();

            if (!chave.startsWith("qtd_")) {
                continue;
            }

            try {
                Long produtoId = Long.valueOf(
                        chave.substring("qtd_".length())
                );

                int quantidade = Integer.parseInt(entrada.getValue());

                if (quantidade < 0 || quantidade > 1000) {
                    redirectAttributes.addFlashAttribute(
                            "erro", "Uma das quantidades é inválida."
                    );
                    return "redirect:/pedidos";
                }

                if (quantidade == 0) {
                    continue;
                }

                Produto produto = produtosAtivos.get(produtoId);

                if (produto == null || produto.getPreco() == null
                        || produto.getPreco().compareTo(BigDecimal.ZERO) < 0) {
                    redirectAttributes.addFlashAttribute(
                            "erro", "Um dos produtos selecionados está indisponível."
                    );
                    return "redirect:/pedidos";
                }

                ItemPedido item = new ItemPedido();
                item.setProduto(produto);
                item.setQuantidade(quantidade);
                item.setPrecoUnitario(produto.getPreco());

                BigDecimal subtotal = produto.getPreco()
                        .multiply(BigDecimal.valueOf(quantidade));

                item.setSubtotal(subtotal);
                pedido.adicionarItem(item);

                total = total.add(subtotal);
                quantidadeTotal += quantidade;

            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute(
                        "erro", "Foi informada uma quantidade inválida."
                );
                return "redirect:/pedidos";
            }
        }

        if (quantidadeTotal == 0) {
            redirectAttributes.addFlashAttribute(
                    "erro", "Selecione pelo menos um produto."
            );
            return "redirect:/pedidos";
        }

        pedido.setTotal(total);

        // O pedido e seus itens são salvos na mesma transação.
        pedidoRepository.save(pedido);

        redirectAttributes.addAttribute("sucesso", "true");
        return "redirect:/pedidos";
    }
}