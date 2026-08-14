package sv.edu.udb.tema6.util;

import jakarta.persistence.EntityManager;
import sv.edu.udb.tema6.model.Categoria;
import sv.edu.udb.tema6.model.Producto;

import java.math.BigDecimal;

// Clase utilitaria que puebla la base de datos vacía con datos de ejemplo
// (3 categorías y 4 productos cada una) para poder probar las consultas del
// servicio sin tener que capturarlos a mano cada vez que se ejecuta el programa.
public class DatosPrueba {

    // Constructor privado: esta clase solo se usa de forma estática, nunca
    // tiene sentido crear una instancia de ella.
    private DatosPrueba() {
    }

    // Crea las categorías y sus productos, y los guarda en la base de datos.
    // Solo se hace em.persist() sobre las categorías: gracias al
    // cascade = CascadeType.ALL definido en Categoria, Hibernate persiste
    // automáticamente también los productos asociados a cada una.
    public static void cargar(EntityManager em) {


        // CATEGORÍAS


        Categoria laptops =
                new Categoria("Laptops");

        Categoria componentes =
                new Categoria("Componentes");

        Categoria perifericos =
                new Categoria("Periféricos");



        // LAPTOPS


        laptops.agregarProducto(
                new Producto(
                        "Lenovo ThinkPad",
                        BigDecimal.valueOf(850.00),
                        10
                )
        );

        laptops.agregarProducto(
                new Producto(
                        "HP Pavilion",
                        BigDecimal.valueOf(720.00),
                        8
                )
        );

        laptops.agregarProducto(
                new Producto(
                        "ASUS Vivobook",
                        BigDecimal.valueOf(680.00),
                        6
                )
        );

        laptops.agregarProducto(
                new Producto(
                        "Acer Aspire",
                        BigDecimal.valueOf(590.00),
                        0
                )
        );



        // COMPONENTES


        componentes.agregarProducto(
                new Producto(
                        "Memoria RAM 16GB",
                        BigDecimal.valueOf(45.00),
                        30
                )
        );

        componentes.agregarProducto(
                new Producto(
                        "SSD NVMe 1TB",
                        BigDecimal.valueOf(80.00),
                        15
                )
        );

        componentes.agregarProducto(
                new Producto(
                        "SSD SATA 512GB",
                        BigDecimal.valueOf(48.00),
                        20
                )
        );

        componentes.agregarProducto(
                new Producto(
                        "Fuente 650W",
                        BigDecimal.valueOf(70.00),
                        5
                )
        );



        // PERIFÉRICOS

        perifericos.agregarProducto(
                new Producto(
                        "Mouse Logitech",
                        BigDecimal.valueOf(25.00),
                        40
                )
        );

        perifericos.agregarProducto(
                new Producto(
                        "Teclado Mecánico",
                        BigDecimal.valueOf(65.00),
                        18
                )
        );

        perifericos.agregarProducto(
                new Producto(
                        "Monitor 24 pulgadas",
                        BigDecimal.valueOf(180.00),
                        12
                )
        );

        perifericos.agregarProducto(
                new Producto(
                        "Webcam Full HD",
                        BigDecimal.valueOf(55.00),
                        7
                )
        );



        // GUARDAR EN LA BD

        em.persist(laptops);
        em.persist(componentes);
        em.persist(perifericos);
    }
}