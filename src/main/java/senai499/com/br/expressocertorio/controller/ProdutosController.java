package senai499.com.br.expressocertorio.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProdutosController {

    @GetMapping("/produtos")
    public String produtos() {
        return "produtos/produtos";
    }

}