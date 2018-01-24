package pt.ipca.tg_reservas;

/**
 * Created by angelasilva on 04/01/18.
 */

public class Reserva {
    String id;
    String authUtilizador;
    String nome;
    String numeroPessoas;
    String data;
    String hora;
    String listaPratos;
    int aceite; // 0 - Nao aceite / 1 - Aceite / 2 - Em espera
    int activo; // 0 - Removido / 1 - Activo

    public Reserva(String id, String authUtilizador, String nome, String numeroPessoas, String data, String hora, String listaPratos, int aceite, int activo) {
        this.id = id;
        this.authUtilizador = authUtilizador;
        this.nome = nome;
        this.numeroPessoas = numeroPessoas;
        this.data = data;
        this.hora = hora;
        this.listaPratos = listaPratos;
        this.aceite = aceite;
        this.activo = activo;
    }

    public Reserva() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthUtilizador() {
        return authUtilizador;
    }

    public void setAuthUtilizador(String authUtilizador) {
        this.authUtilizador = authUtilizador;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNumeroPessoas() {
        return numeroPessoas;
    }

    public void setNumeroPessoas(String numeroPessoas) {
        this.numeroPessoas = numeroPessoas;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getListaPratos() {
        return listaPratos;
    }

    public void setListaPratos(String listaPratos) {
        this.listaPratos = listaPratos;
    }

    public int getAceite() {
        return aceite;
    }

    public void setAceite(int aceite) {
        this.aceite = aceite;
    }

    public int getActivo() {
        return activo;
    }

    public void setActivo(int activo) {
        this.activo = activo;
    }
}
