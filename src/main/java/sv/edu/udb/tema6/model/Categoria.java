package sv.edu.udb.tema6.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @OneToMany(
            mappedBy = "categoria",
            cascade = CascadeType.ALL
    )
    private List<Producto> productos = new ArrayList<>();


    // Constructor vacío requerido por JPA para poder instanciar la entidad
    // internamente (por ejemplo, al reconstruirla desde la base de datos).
    public Categoria() {
    }


    // Constructor de conveniencia para crear una categoría nueva solo con su nombre.
    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    // Añade un producto a esta categoría y, a la vez, actualiza el lado
    // inverso de la relación (producto.setCategoria(this)) para mantener
    // ambos objetos sincronizados en memoria.
    public void agregarProducto(Producto producto) {
        productos.add(producto);
        producto.setCategoria(this);
    }

    // Devuelve el id autogenerado de la categoría (clave primaria).
    public Long getId() {
        return id;
    }

    // Devuelve el nombre de la categoría (ej. "Laptops").
    public String getNombre() {
        return nombre;
    }

    // Devuelve la lista de productos que pertenecen a esta categoría.
    public List<Producto> getProductos() {
        return productos;
    }


    // Representación en texto de la categoría: simplemente su nombre.
    @Override
    public String toString() {
        return nombre;
    }
}