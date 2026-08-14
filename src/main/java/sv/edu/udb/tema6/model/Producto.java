package sv.edu.udb.tema6.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;


    // Constructor vacío requerido por JPA para poder instanciar la entidad
    // internamente (por ejemplo, al reconstruirla desde la base de datos).
    public Producto() {
    }


    // Constructor de conveniencia para crear un producto nuevo. La categoría
    // no se recibe aquí: se asigna aparte con setCategoria() o mediante
    // Categoria.agregarProducto(), que hace ambas cosas a la vez.
    public Producto(String nombre, BigDecimal precio, Integer stock) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }


    // Devuelve el id autogenerado del producto (clave primaria).
    public Long getId() {
        return id;
    }


    // Devuelve el nombre del producto.
    public String getNombre() {
        return nombre;
    }


    // Devuelve el precio del producto como BigDecimal (exacto, sin errores
    // de redondeo, a diferencia de float/double).
    public BigDecimal getPrecio() {
        return precio;
    }


    // Devuelve la cantidad disponible en inventario.
    public Integer getStock() {
        return stock;
    }


    // Devuelve la categoría a la que pertenece el producto. Como la relación
    // es LAZY, la primera vez que se llama puede disparar una consulta a la
    // base de datos si la categoría aún no estaba cargada en memoria.
    public Categoria getCategoria() {
        return categoria;
    }


    // Asigna la categoría del producto. Normalmente no se llama directo,
    // sino a través de Categoria.agregarProducto() para mantener sincronizados
    // ambos lados de la relación.
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }


    // Da formato de tabla (columnas alineadas) al producto para imprimirlo
    // en consola, incluyendo el nombre de su categoría.
    @Override
    public String toString() {

        return String.format(
                "%-3d | %-25s | $%8.2f | Stock: %-3d | %s",
                id,
                nombre,
                precio,
                stock,
                categoria.getNombre()
        );
    }
}