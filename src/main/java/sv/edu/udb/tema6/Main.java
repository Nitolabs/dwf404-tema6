package sv.edu.udb.tema6;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import sv.edu.udb.tema6.model.Producto;
import sv.edu.udb.tema6.service.ProductoConsultaService;
import sv.edu.udb.tema6.util.DatosPrueba;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

// Punto de entrada de la aplicación. Arranca JPA, carga datos de prueba y
// muestra un menú de consola que permite probar las 4 consultas de
// ProductoConsultaService (filtro, ordenamiento, JOIN y paginación).
public class Main {
	private static final Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {

		// Crea la fábrica de EntityManager a partir de la unidad de
		// persistencia "Tema6PU" definida en persistence.xml, y luego
		// el EntityManager que se usará durante toda la ejecución.
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("Tema6PU");

		EntityManager em = emf.createEntityManager();

		// CARGAR DATOS DE PRUEBA
		// Toda escritura en la base de datos debe ir dentro de una
		// transacción (begin/commit). Como hbm2ddl.auto=create recrea
		// las tablas vacías en cada ejecución, hay que volver a cargar
		// los datos de ejemplo cada vez.

		em.getTransaction().begin();
		DatosPrueba.cargar(em);
		em.getTransaction().commit();

		ProductoConsultaService service = new ProductoConsultaService(em);

		// MENÚ

		int opcion;

		do {
			mostrarMenu();
			try {
				opcion = Integer.parseInt(scanner.nextLine());

				switch (opcion) {
					case 1 ->
						demostrarFiltro(service);

					case 2 ->
						demostrarOrdenamiento(service);

					case 3 ->
						demostrarJoin(service);

					case 4 ->
						demostrarPaginacion(service);

					case 0 ->
						System.out.println("\nFinalizando demostración...");

					default ->
						System.out.println("\nOpción no válida.");
				}
			} catch (NumberFormatException e) {
				System.out.println("\nDebe ingresar un número.");

				opcion = -1;
			}

		} while (opcion != 0);

		// Libera los recursos abiertos: conexión a la BD (EntityManager
		// y su fábrica) y el lector de teclado.
		em.close();
		emf.close();

		scanner.close();
	}

	// Imprime el menú de opciones en consola.
	private static void mostrarMenu() {

		System.out.println("""


				   DEMOSTRACIÓN JPA E HIBERNATE
						   TEMA 6

				1. Filtrar productos por precio
				2. Ordenar productos por precio
				3. Buscar productos por categoría (JOIN)
				4. Mostrar productos paginados

				0. Salir


				Seleccione una opción:
				""");
	}

	// Pide un precio mínimo al usuario y muestra los productos que lo
	// cumplen, usando ProductoConsultaService.filtrarPorPrecio().
	private static void demostrarFiltro(
			ProductoConsultaService service) {

		System.out.print(
				"\nIngrese el precio mínimo: $");

		try {
			BigDecimal precio = new BigDecimal(scanner.nextLine());

			List<Producto> productos = service.filtrarPorPrecio(precio);

			System.out.println();
			System.out.println("PRODUCTOS CON PRECIO >= $" + precio);
			System.out.println("------------------------------------------");

			mostrarProductos(productos);

		} catch (NumberFormatException e) {

			System.out.println("\nPrecio no válido.");
		}
	}

	// Muestra todos los productos ordenados por precio, usando
	// ProductoConsultaService.ordenarPorPrecio().
	private static void demostrarOrdenamiento(
			ProductoConsultaService service) {

		System.out.println();
		System.out.println("PRODUCTOS ORDENADOS POR PRECIO");
		System.out.println("------------------------------------------");

		List<Producto> productos = service.ordenarPorPrecio();

		mostrarProductos(productos);
	}

	// Pide el nombre de una categoría y muestra los productos que
	// pertenecen a ella, usando ProductoConsultaService.buscarPorCategoria()
	// (consulta con JOIN).
	private static void demostrarJoin(ProductoConsultaService service) {

		System.out.println("""

				Categorías disponibles:

				- Laptops
				- Componentes
				- Periféricos
				""");

		System.out.print("Ingrese la categoría: ");

		String categoria = scanner.nextLine();

		List<Producto> productos = service.buscarPorCategoria(categoria);

		System.out.println();
		System.out.println("PRODUCTOS DE LA CATEGORÍA: " + categoria);

		System.out.println("------------------------------------------");

		mostrarProductos(productos);
	}

	// Pide un número de página y muestra ese bloque de productos, usando
	// ProductoConsultaService.obtenerPagina(). El tamaño de página es fijo
	// (4), por eso solo se permiten las páginas 1 a 3 (12 productos en total).
	private static void demostrarPaginacion(ProductoConsultaService service) {

		final int tamanioPagina = 4;

		System.out.print("\nIngrese número de página (1 - 3): ");

		try {
			int pagina = Integer.parseInt(scanner.nextLine());

			if (pagina < 1 || pagina > 3) {

				System.out.println("\nLa página debe estar entre 1 y 3.");

				return;
			}

			List<Producto> productos = service.obtenerPagina(
					pagina,
					tamanioPagina);

			System.out.println();
			System.out.println("PÁGINA " + pagina);

			System.out.println("------------------------------------------");

			mostrarProductos(productos);

		} catch (NumberFormatException e) {
			System.out.println("\nNúmero de página no válido.");
		}
	}

	// Imprime la lista de productos recibida (o un aviso si está vacía)
	// seguida del total de resultados encontrados. Se reutiliza en las
	// 4 opciones del menú para no repetir la lógica de impresión.
	private static void mostrarProductos(
			List<Producto> productos) {

		if (productos.isEmpty()) {
			System.out.println("No se encontraron productos.");

			return;
		}

		productos.forEach(System.out::println);

		System.out.println();
		System.out.println("Total encontrados: " + productos.size());
	}
}
