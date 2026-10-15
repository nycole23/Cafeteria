package senai499.com.br.expressocertorio.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/administrativo")
    public String admin() {
        return "admin/administrativo";
    }

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/criar-conta")
    public String criarConta() {
        return "admin/criar-conta";
    }

}