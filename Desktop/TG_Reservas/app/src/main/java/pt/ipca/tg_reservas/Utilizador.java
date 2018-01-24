package pt.ipca.tg_reservas;

/**
 * Created by angelasilva on 04/01/18.
 */

public class Utilizador {
    String id;
    String nome;
    String email;

    public Utilizador(String id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    public Utilizador() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
}
