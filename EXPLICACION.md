# Explicación del proyecto: Tema 6 — Consultas JPA

## ¿Qué es este proyecto?

Antes que nada, una aclaración importante: **este proyecto NO usa Spring Boot**. Si revisas el archivo `pom.xml`, no hay ninguna dependencia de Spring. Lo que sí tiene es:

- **JPA** (Jakarta Persistence API): una especificación de Java que define cómo mapear clases Java a tablas de una base de datos (esto se llama **ORM**, Object-Relational Mapping).
- **Hibernate**: la implementación concreta de JPA que hace el trabajo real (traducir tus consultas Java a SQL).
- **H2**: una base de datos ligera que se guarda en un archivo local, ideal para practicar sin instalar un motor de base de datos completo.

Es una aplicación de **consola** (no web): se ejecuta, muestra un menú de texto, el usuario elige una opción con el teclado, y el programa responde en la terminal.

El objetivo del proyecto es **practicar JPQL** (el lenguaje de consultas de JPA, muy parecido a SQL pero que trabaja sobre objetos Java en vez de tablas) mediante 4 operaciones típicas: filtrar, ordenar, hacer un join y paginar resultados.

---

## Mapa del proyecto

```
tema6-consultas-jpa/
├── pom.xml                                  → configuración de Maven y dependencias
└── src/main/
    ├── java/sv/edu/udb/tema6/
    │   ├── Main.java                        → punto de entrada, menú de consola
    │   ├── model/
    │   │   ├── Categoria.java               → entidad "Categoría"
    │   │   └── Producto.java                → entidad "Producto"
    │   ├── service/
    │   │   └── ProductoConsultaService.java → las 4 consultas JPQL
    │   └── util/
    │       └── DatosPrueba.java             → datos de ejemplo para probar
    └── resources/META-INF/
        └── persistence.xml                  → configuración de la conexión a la BD
```

La organización sigue un patrón muy común en proyectos Java:

| Paquete | Responsabilidad |
|---|---|
| `model` | Las "cosas" del dominio (entidades): qué es un Producto, qué es una Categoría. |
| `service` | La lógica de negocio: qué consultas se pueden hacer sobre esas entidades. |
| `util` | Herramientas de apoyo que no son el núcleo de la app (aquí, cargar datos de prueba). |
| raíz (`Main.java`) | El "director de orquesta": arranca todo y conecta al usuario con el servicio. |

---

## 1. `pom.xml` — la receta del proyecto

Maven usa este archivo para saber qué librerías descargar y cómo compilar el código. Las dependencias importantes son:

```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>7.4.5.Final</version>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.4.240</version>
    <scope>runtime</scope>
</dependency>
```

- **`hibernate-core`**: el motor ORM. Convierte tus objetos Java (`Producto`, `Categoria`) en filas de tablas SQL, y viceversa.
- **`h2`** con `scope=runtime`: el driver de la base de datos. `runtime` significa que solo se necesita cuando el programa **se ejecuta**, no cuando se **compila** — el código Java nunca importa clases de H2 directamente, solo habla con JPA.

También fija Java 21 como versión de compilación.

> **Idea clave**: tu código Java (en `model` y `service`) nunca menciona SQL ni H2 directamente. Solo usa JPA (interfaces como `EntityManager`). Esto significa que, en teoría, podrías cambiar H2 por MySQL o PostgreSQL sin tocar una sola línea de `Producto.java` o del servicio — solo cambiarías la configuración de conexión.

---

## 2. `persistence.xml` — el manual de conexión

Ubicado en `src/main/resources/META-INF/persistence.xml`, este archivo es **obligatorio** en JPA "puro" (sin Spring): le dice a JPA cómo se llama la conexión, qué entidades existen y cómo conectarse a la BD.

```xml
<persistence-unit name="Tema6PU" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    <class>sv.edu.udb.tema6.model.Categoria</class>
    <class>sv.edu.udb.tema6.model.Producto</class>
    <properties>
        <property name="jakarta.persistence.jdbc.url"
                  value="jdbc:h2:file:./data/tema6db"/>
        <property name="hibernate.hbm2ddl.auto" value="create"/>
        <property name="hibernate.show_sql" value="true"/>
        ...
    </properties>
</persistence-unit>
```

Puntos clave para entender:

- **`name="Tema6PU"`**: es el identificador que usa `Main.java` para decir "quiero conectarme usando esta configuración". PU = *Persistence Unit* (Unidad de Persistencia).
- **`transaction-type="RESOURCE_LOCAL"`**: las transacciones las maneja el propio programa (con `em.getTransaction().begin()/commit()`), a diferencia de un entorno con servidor de aplicaciones que las gestionaría por ti. Esto es típico cuando no usas un framework como Spring.
- **`<class>`**: hay que **listar manualmente** cada entidad que Hibernate debe conocer. Si creas una entidad nueva y no la agregas aquí, JPA no sabrá que existe.
- **`jdbc:h2:file:./data/tema6db`**: le dice a H2 que guarde la base de datos en un archivo dentro de la carpeta `data/`, relativa a donde se ejecuta el programa.
- **`hibernate.hbm2ddl.auto=create`**: esta es la propiedad más importante para entender el comportamiento del programa. `create` significa **"borra las tablas si existen y créalas de nuevo desde cero cada vez que arranca la aplicación"**. Por eso el proyecto siempre carga datos de prueba al inicio: la base de datos nace vacía en cada ejecución. (Otros valores comunes que verás en otros proyectos: `update` conserva los datos y solo ajusta el esquema, `validate` no toca nada y solo verifica que coincida.)
- **`hibernate.show_sql=true`** y **`format_sql=true`**: hacen que, cada vez que el programa ejecuta una consulta JPQL, Hibernate imprima en la consola el **SQL real** que generó. Es una herramienta de aprendizaje excelente: te permite ver la traducción de JPQL a SQL en tiempo real.

---

## 3. Paquete `model` — las entidades

Una **entidad** es una clase Java anotada con `@Entity` que representa una tabla de la base de datos. Cada instancia de la clase representa una fila.

### `Categoria.java`

```java
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL)
    private List<Producto> productos = new ArrayList<>();
    ...
}
```

- `@Entity` + `@Table(name = "categorias")`: le dice a Hibernate "esta clase es una tabla llamada `categorias`".
- `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`: el campo `id` es la clave primaria, y la base de datos la genera automáticamente (autoincremental), no el programa.
- `@Column(nullable = false, unique = true)`: el nombre de la categoría no puede ser nulo y no se puede repetir.
- `@OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL)`: **una** categoría tiene **muchos** productos. `mappedBy = "categoria"` apunta al nombre del campo en `Producto` que es "dueño" de la relación (más abajo se explica qué significa "dueño"). `cascade = CascadeType.ALL` significa: si guardo o borro una categoría, que se propague automáticamente a sus productos asociados.

El método `agregarProducto()` es un pequeño "ayudante" muy importante en relaciones bidireccionales:

```java
public void agregarProducto(Producto producto) {
    productos.add(producto);
    producto.setCategoria(this);
}
```

Como la relación existe en **ambos sentidos** (`Categoria` tiene una lista de `Producto`, y `Producto` tiene una referencia a `Categoria`), si solo actualizas un lado, el otro queda desincronizado en memoria (aunque en la BD esté bien). Este método actualiza los dos lados a la vez, evitando bugs sutiles.

### `Producto.java`

```java
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
    ...
}
```

- `precision = 10, scale = 2` en el precio: hasta 10 dígitos en total, 2 de ellos decimales (ej. `12345678.90`). Se usa `BigDecimal` en vez de `double` porque **nunca se deben usar decimales binarios (`float`/`double`) para dinero** — pueden introducir errores de redondeo. `BigDecimal` es exacto.
- `@ManyToOne`: **muchos** productos pertenecen a **una** categoría. Este es el lado "dueño" de la relación (el que tiene la clave foránea real en su tabla).
- `@JoinColumn(name = "categoria_id")`: crea físicamente la columna `categoria_id` en la tabla `productos`, que apunta al `id` de `categorias`. Es la clásica **llave foránea**.
- `fetch = FetchType.LAZY`: esto es clave para entender el rendimiento en JPA. Significa que cuando cargas un `Producto`, Hibernate **no** trae automáticamente su `Categoria` de la base de datos — solo la trae en el momento en que realmente accedes a ella (por ejemplo, con `producto.getCategoria().getNombre()`). Esto evita cargar datos que quizás no necesitas. La alternativa es `FetchType.EAGER`, que trae todo de inmediato.

El método `toString()` da formato tabular a la impresión en consola:

```java
return String.format(
    "%-3d | %-25s | $%8.2f | Stock: %-3d | %s",
    id, nombre, precio, stock, categoria.getNombre()
);
```

Nota que aquí se llama a `categoria.getNombre()`: como el fetch es `LAZY`, este es justo el momento en que Hibernate va a la base de datos a buscar la categoría (si no la tenía ya en memoria).

---

## 4. Paquete `service` — las consultas JPQL

**JPQL** (Jakarta Persistence Query Language) es un lenguaje de consultas muy parecido a SQL, pero con una diferencia fundamental: **en JPQL escribes sobre clases y campos Java (entidades), no sobre tablas y columnas SQL**. Hibernate se encarga de traducirlo al SQL real.

`ProductoConsultaService.java` recibe un `EntityManager` (el objeto central de JPA para hacer consultas y persistir datos) y expone 4 métodos, cada uno mostrando una técnica distinta:

### a) Filtro con parámetro

```java
String jpql = """
        SELECT p
        FROM Producto p
        WHERE p.precio >= :precioMinimo
        ORDER BY p.precio ASC
        """;

return em.createQuery(jpql, Producto.class)
        .setParameter("precioMinimo", precioMinimo)
        .getResultList();
```

- `FROM Producto p`: nota que dice `Producto` (el nombre de la **clase** Java), no `productos` (el nombre de la tabla).
- `:precioMinimo` es un **parámetro nombrado**. Se rellena después con `.setParameter(...)`. Esto es importante por seguridad: evita la inyección SQL, ya que nunca concatenas el valor directamente en el texto de la consulta.
- `.getResultList()` ejecuta la consulta y devuelve una `List<Producto>`.

### b) Join entre entidades

```java
String jpql = """
    SELECT p
    FROM Producto p
    JOIN p.categoria c
    WHERE c.nombre = :nombreCategoria
    ORDER BY p.nombre ASC
    """;
```

Aquí `JOIN p.categoria c` no es un "JOIN de tablas" como en SQL puro — es un **join a través de la relación de objetos** que ya definiste con `@ManyToOne` en `Producto`. Le dices a JPQL "sigue la relación `categoria` de cada producto y llámala `c`", y luego filtras por `c.nombre`. Hibernate se encarga de traducir esto al `JOIN` SQL correspondiente usando la columna `categoria_id`.

### c) Paginación

```java
String jpql = """
    SELECT p
    FROM Producto p
    ORDER BY p.id ASC
    """;

return em.createQuery(jpql, Producto.class)
        .setFirstResult((numeroPagina - 1) * tamanioPagina)
        .setMaxResults(tamanioPagina)
        .getResultList();
```

- `setFirstResult(n)`: cuántos resultados **saltar** desde el principio (equivale al `OFFSET` de SQL).
- `setMaxResults(n)`: cuántos resultados traer como máximo (equivale al `LIMIT` de SQL).
- La fórmula `(numeroPagina - 1) * tamanioPagina` es el cálculo estándar de paginación: si quieres la página 2 con tamaño 4, saltas `(2-1)*4 = 4` resultados y traes 4 más (del 5 al 8).

### d) Ordenamiento simple

```java
String jpql = """
    SELECT p
    FROM Producto p
    ORDER BY p.precio ASC
    """;
```

El caso más básico: sin filtro, solo ordenar todos los resultados.

> **Para recordar**: `SELECT`, `FROM`, `WHERE`, `JOIN`, `ORDER BY` en JPQL se ven casi idénticos a SQL, pero siempre trabajan sobre **entidades y sus campos**, nunca sobre tablas y columnas directamente. Esa es la diferencia conceptual más importante entre JPQL y SQL.

---

## 5. Paquete `util` — `DatosPrueba.java`

Esta clase solo tiene un método estático, `cargar(EntityManager em)`, cuyo trabajo es poblar la base de datos vacía con datos de ejemplo (3 categorías, 4 productos cada una = 12 productos en total).

```java
Categoria laptops = new Categoria("Laptops");

laptops.agregarProducto(
        new Producto("Lenovo ThinkPad", BigDecimal.valueOf(850.00), 10)
);
// ... más productos

em.persist(laptops);
```

Puntos a notar:

- El **constructor privado** (`private DatosPrueba() {}`) evita que alguien intente crear una instancia de esta clase — solo tiene sentido usarla de forma estática, como una caja de herramientas.
- Solo se llama `em.persist(laptops)` (y lo mismo para las otras 2 categorías) — **nunca** se hace `em.persist()` sobre cada producto individualmente. Esto funciona gracias al `cascade = CascadeType.ALL` que vimos en `Categoria`: al persistir la categoría, Hibernate detecta la lista de productos asociados y los persiste automáticamente también. Es un ejemplo práctico de por qué el cascade es útil.

---

## 6. `Main.java` — el punto de entrada

Este archivo conecta todas las piezas y le da al usuario una interfaz de consola. Su flujo, paso a paso:

### Paso 1 — Arrancar JPA

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("Tema6PU");
EntityManager em = emf.createEntityManager();
```

- `EntityManagerFactory`: se crea **una sola vez** por aplicación. Es "pesado" de construir (lee la configuración, prepara el pool de conexiones, etc.), por eso no se crea uno por cada consulta.
- `EntityManager`: es el objeto que realmente usas para consultar y guardar datos. En aplicaciones más grandes normalmente se crea un `EntityManager` nuevo por cada operación/transacción; aquí, al ser un programa simple de consola, se reutiliza uno solo durante toda la ejecución.

### Paso 2 — Cargar datos de prueba dentro de una transacción

```java
em.getTransaction().begin();
DatosPrueba.cargar(em);
em.getTransaction().commit();
```

Cualquier operación que **modifique** la base de datos (persistir, actualizar, borrar) en JPA debe ocurrir dentro de una transacción: se abre con `begin()` y se confirma con `commit()`. Si algo fallara a mitad de camino, se podría llamar a `rollback()` para deshacer los cambios (aquí no se maneja ese caso porque es un ejemplo simple).

### Paso 3 — El menú interactivo

```java
do {
    mostrarMenu();
    opcion = Integer.parseInt(scanner.nextLine());
    switch (opcion) {
        case 1 -> demostrarFiltro(service);
        case 2 -> demostrarOrdenamiento(service);
        case 3 -> demostrarJoin(service);
        case 4 -> demostrarPaginacion(service);
        case 0 -> System.out.println("Finalizando...");
        default -> System.out.println("Opción no válida.");
    }
} while (opcion != 0);
```

- Es un bucle `do-while`: se ejecuta al menos una vez (para mostrar el menú) y sigue repitiéndose hasta que el usuario elige `0`.
- El `switch` usa la sintaxis moderna de Java (flechas `->`, disponible desde Java 14), más compacta que el `switch` clásico con `case: ... break;`.
- Cada opción llama a un método privado (`demostrarFiltro`, `demostrarOrdenamiento`, etc.) que pide los datos necesarios al usuario (por ejemplo, el precio mínimo) y llama al método correspondiente del `service`.
- Los errores de formato (si el usuario escribe letras en vez de números) se capturan con `try/catch (NumberFormatException e)`, evitando que el programa se caiga por una mala entrada.

### Paso 4 — Cerrar recursos

```java
em.close();
emf.close();
scanner.close();
```

Al terminar, se cierran el `EntityManager`, el `EntityManagerFactory` y el `Scanner`. Es una buena práctica liberar siempre los recursos que abriste (conexiones, lectores, etc.), especialmente en aplicaciones que no usan un framework que lo haga por ti automáticamente (como sí ocurre en Spring).

---

## El flujo completo, de principio a fin

1. Arranca el programa → JPA se conecta a H2 usando la configuración de `persistence.xml`.
2. Como `hbm2ddl.auto=create`, las tablas `categorias` y `productos` se **recrean vacías**.
3. `DatosPrueba.cargar(em)` llena la base de datos con 3 categorías y 12 productos.
4. Se muestra el menú en consola.
5. El usuario elige una opción → se ejecuta el método correspondiente en `ProductoConsultaService`, que arma una consulta **JPQL**.
6. Hibernate traduce esa consulta JPQL a **SQL real** y la ejecuta contra H2 (puedes ver el SQL generado en consola gracias a `show_sql=true`).
7. Los resultados (objetos `Producto`) se imprimen usando su `toString()`.
8. El ciclo se repite hasta que el usuario elige `0` y el programa cierra todo correctamente.

---

## Conceptos clave para repasar

| Concepto | En una frase |
|---|---|
| **Entidad** (`@Entity`) | Una clase Java que representa una tabla de la BD. |
| **JPQL** | Un lenguaje tipo SQL que consulta sobre entidades/campos Java, no sobre tablas/columnas. |
| **`EntityManager`** | El objeto que usas para leer y escribir datos vía JPA. |
| **Transacción** (`begin`/`commit`) | Un bloque de operaciones que se confirman (o deshacen) como una unidad. |
| **`@OneToMany` / `@ManyToOne`** | Cómo se modela una relación 1-a-muchos entre dos entidades. |
| **`cascade`** | Qué operaciones (persistir, borrar...) se propagan automáticamente a las entidades relacionadas. |
| **`FetchType.LAZY` vs `EAGER`** | Si una relación se carga "cuando se necesita" o "siempre de inmediato". |
| **Parámetro nombrado (`:param`)** | Forma segura de pasar valores a una consulta, evitando inyección SQL. |
| **`hibernate.hbm2ddl.auto`** | Controla si Hibernate crea, actualiza, valida o ignora el esquema de la BD al arrancar. |
