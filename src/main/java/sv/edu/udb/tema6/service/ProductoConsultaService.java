package sv.edu.udb.tema6.service;

import jakarta.persistence.EntityManager;
import sv.edu.udb.tema6.model.Producto;

import java.math.BigDecimal;
import java.util.List;

// Agrupa las consultas JPQL disponibles sobre la entidad Producto: filtrar,
// ordenar, buscar por categoría (JOIN) y paginar. Cada método arma su propia
// consulta y la ejecuta a través del EntityManager recibido por constructor.
public class ProductoConsultaService {

    private final EntityManager em;

    public ProductoConsultaService(EntityManager em) {
        this.em = em;
    }

    // FILTRO: devuelve los productos cuyo precio es mayor o igual al mínimo
    // indicado, ordenados de menor a mayor precio. El valor se pasa como
    // parámetro nombrado (:precioMinimo) en vez de concatenarlo en el texto
    // de la consulta, evitando así inyección SQL.
    public List<Producto> filtrarPorPrecio(BigDecimal precioMinimo) {

        String jpql = """
                SELECT p
                FROM Producto p
                WHERE p.precio >= :precioMinimo
                ORDER BY p.precio ASC
                """;

        return em.createQuery(jpql, Producto.class)
                .setParameter("precioMinimo", precioMinimo)
                .getResultList();
    }

    // JOIN: devuelve los productos que pertenecen a la categoría cuyo nombre
    // coincide exactamente con el recibido. "JOIN p.categoria c" recorre la
    // relación @ManyToOne definida en Producto, no una tabla directamente.
    public List<Producto> buscarPorCategoria(String nombreCategoria) {

        String jpql = """
            SELECT p
            FROM Producto p
            JOIN p.categoria c
            WHERE c.nombre = :nombreCategoria
            ORDER BY p.nombre ASC
            """;

        return em.createQuery(jpql, Producto.class)
                .setParameter("nombreCategoria", nombreCategoria)
                .getResultList();
    }

    // PAGINACIÓN: devuelve solo una "página" de productos, ordenados por id.
    // setFirstResult calcula cuántos resultados saltar (equivale a OFFSET) y
    // setMaxResults limita cuántos traer como máximo (equivale a LIMIT).
    public List<Producto> obtenerPagina(int numeroPagina, int tamanioPagina) {

        String jpql = """
            SELECT p
            FROM Producto p
            ORDER BY p.id ASC
            """;

        return em.createQuery(jpql, Producto.class)
                .setFirstResult((numeroPagina - 1) * tamanioPagina)
                .setMaxResults(tamanioPagina)
                .getResultList();
    }

    // ORDENAMIENTO: devuelve todos los productos ordenados de menor a mayor
    // precio, sin ningún filtro.
    public List<Producto> ordenarPorPrecio() {

        String jpql = """
            SELECT p
            FROM Producto p
            ORDER BY p.precio ASC
            """;

        return em.createQuery(jpql, Producto.class)
                .getResultList();
    }
}