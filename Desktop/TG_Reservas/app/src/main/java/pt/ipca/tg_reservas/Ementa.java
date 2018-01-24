package pt.ipca.tg_reservas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by angelasilva on 04/01/18.
 */

public class Ementa {
    String id;
    String data;
    List<Prato> pratos = new ArrayList<>();

    public Ementa(String id, String data, List<Prato> pratos) {
        this.id = id;
        this.data = data;
        this.pratos = pratos;
    }

    public Ementa() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<Prato> getPratos() {
        return pratos;
    }

    public void setPratos(List<Prato> pratos) {
        this.pratos = pratos;
    }

    @Override
    public String toString() {
        return "Ementa{" +
                "id='" + id + '\'' +
                ", data=" + data +
                ", pratos=" + pratos +
                '}';
    }
}
