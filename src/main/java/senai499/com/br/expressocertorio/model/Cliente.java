package senai499.com.br.expressocertorio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "clientes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_clientes_email",
                        columnNames = "email"
                )
        }
)
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório.")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 160)
    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres.")
    @Column(nullable = false, length = 255)
    private String senha;

    @NotBlank(message = "O telefone é obrigatório.")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String telefone;

    @NotBlank(message = "O CEP é obrigatório.")
    @Size(max = 9)
    @Column(nullable = false, length = 9)
    private String cep;

    @NotBlank(message = "O estado é obrigatório.")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String estado;

    @NotBlank(message = "A cidade é obrigatória.")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String cidade;

    @NotBlank(message = "O bairro é obrigatório.")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String bairro;

    @NotBlank(message = "A rua é obrigatória.")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String rua;

    @NotBlank(message = "O número é obrigatório.")
    @Size(max = 15)
    @Column(nullable = false, length = 15)
    private String numero;

    @Size(max = 100)
    @Column(length = 100)
    private String complemento;

    // Construtor vazio exigido pelo JPA
    public Cliente() {
    }

    // Getters e setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }
}