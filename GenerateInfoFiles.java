import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Genera los archivos planos pseudoaleatorios que sirven de entrada al
 * programa {@code main}.
 *
 * <p>Al ejecutarse crea, en la carpeta del proyecto:</p>
 * <ul>
 *   <li>{@value #PRODUCTS_FILE_NAME}: un producto por línea con el formato
 *       {@code IDProducto;NombreProducto;PrecioPorUnidad}.</li>
 *   <li>{@value #SALESMEN_FILE_NAME}: un vendedor por línea con el formato
 *       {@code TipoDocumento;NumeroDocumento;Nombres;Apellidos}.</li>
 *   <li>Uno o más archivos de ventas por vendedor, cuyo nombre empieza por
 *       {@value #SALES_FILE_PREFIX}. La primera línea es
 *       {@code TipoDocumento;NumeroDocumento} y cada línea siguiente es una venta
 *       con el formato {@code IDProducto;Cantidad;}.</li>
 * </ul>
 *
 * <p>El programa no le pide datos al usuario. Al terminar muestra un mensaje
 * de finalización exitosa o un mensaje de error.</p>
 *
 * @author Kevin
 * @version 2.0
 */
public class GenerateInfoFiles {

    /** Nombre del archivo con la información de los productos. */
    public static final String PRODUCTS_FILE_NAME = "products.txt";

    /** Nombre del archivo con la información de los vendedores. */
    public static final String SALESMEN_FILE_NAME = "salesmen.txt";

    /** Prefijo con el que empieza el nombre de cada archivo de ventas. */
    public static final String SALES_FILE_PREFIX = "sales_";

    /** Extensión de los archivos planos de entrada. */
    public static final String TEXT_FILE_EXTENSION = ".txt";

    /** Separador de campos usado en todos los archivos. */
    public static final String SEPARATOR = ";";

    /** Carpeta donde se escriben los archivos: la carpeta del proyecto. */
    public static final Path DATA_DIRECTORY = Paths.get(".");

    /** Cantidad de productos que se generan al ejecutar el programa. */
    private static final int PRODUCTS_TO_GENERATE = 25;

    /** Cantidad de vendedores que se generan al ejecutar el programa. */
    private static final int SALESMEN_TO_GENERATE = 12;

    /** Mínimo de ventas que puede tener un archivo de ventas generado. */
    private static final int MIN_SALES_PER_FILE = 5;

    /** Máximo de ventas que puede tener un archivo de ventas generado. */
    private static final int MAX_SALES_PER_FILE = 20;

    /** Máximo de unidades de un producto en una sola venta. */
    private static final int MAX_QUANTITY_PER_SALE = 10;

    /** Probabilidad de que un vendedor tenga un segundo archivo de ventas. */
    private static final double SECOND_FILE_PROBABILITY = 0.3;

    /** Primer identificador que se asigna a los productos generados. */
    private static final long FIRST_PRODUCT_ID = 1001;

    /** Variación máxima del precio generado respecto al de referencia (20 %). */
    private static final double PRICE_VARIATION = 0.2;

    /** Los precios generados son múltiplos de este valor, en pesos. */
    private static final int PRICE_STEP = 100;

    /** Menor número de documento que se puede generar. */
    private static final int MIN_DOCUMENT_NUMBER = 10_000_000;

    /** Rango de números de documento que se pueden generar. */
    private static final int DOCUMENT_NUMBER_RANGE = 1_190_000_000;

    /** Tipos de documento de identidad usados en Colombia. */
    private static final String[] DOCUMENT_TYPES = {"CC", "CC", "CC", "CE", "PA"};

    /** Nombres reales de hombres para generar vendedores coherentes. */
    private static final String[] MALE_FIRST_NAMES = {
        "Juan", "Carlos", "Andrés", "Santiago", "Sebastián", "Felipe",
        "Alejandro", "Daniel", "Mateo", "Camilo"
    };

    /** Nombres reales de mujeres para generar vendedores coherentes. */
    private static final String[] FEMALE_FIRST_NAMES = {
        "María", "Laura", "Valentina", "Daniela", "Camila", "Paula",
        "Sofía", "Natalia", "Juliana", "Mariana"
    };

    /** Apellidos reales de personas para generar vendedores coherentes. */
    private static final String[] LAST_NAMES = {
        "García", "Rodríguez", "Martínez", "López", "González", "Hernández",
        "Pérez", "Sánchez", "Ramírez", "Torres", "Gómez", "Díaz", "Vargas",
        "Rojas", "Castro", "Moreno", "Jiménez", "Ruiz", "Ospina", "Restrepo"
    };

    /** Nombres de productos reales para generar el catálogo. */
    private static final String[] PRODUCT_NAMES = {
        "Arroz 500 g", "Aceite 1 L", "Azúcar 1 kg", "Café 250 g", "Leche 1 L",
        "Huevos x 12", "Pan tajado", "Panela 500 g", "Frijol 500 g",
        "Lenteja 500 g", "Chocolate 250 g", "Atún en lata", "Pasta 500 g",
        "Sal 1 kg", "Harina de maíz 1 kg", "Queso campesino 500 g",
        "Mantequilla 250 g", "Jabón de loza", "Detergente 1 kg",
        "Papel higiénico x 4", "Crema dental", "Champú 400 ml",
        "Galletas x 6", "Gaseosa 1.5 L", "Jugo de naranja 1 L",
        "Avena 500 g", "Salchichas x 10", "Yogur 1 L", "Cereal 400 g",
        "Agua 600 ml"
    };

    /**
     * Precio de referencia de cada producto de {@link #PRODUCT_NAMES}, en pesos,
     * en el mismo orden. El precio generado varía alrededor de este valor.
     */
    private static final int[] PRODUCT_BASE_PRICES = {
        3_000, 12_000, 4_500, 9_000, 4_200, 14_000, 6_500, 3_500, 5_800,
        4_000, 6_000, 7_500, 3_800, 2_200, 3_600, 11_000, 8_500, 5_000,
        12_500, 9_800, 6_200, 16_000, 4_800, 6_500, 7_200, 5_500, 9_500,
        10_500, 14_500, 1_800
    };

    /** Generador de números pseudoaleatorios usado por todos los métodos. */
    private static final Random RANDOM = new Random();

    /** Identificadores de los productos creados por {@link #createProductsFile(int)}. */
    private static final List<Long> generatedProductIds = new ArrayList<>();

    /** Vendedores creados por {@link #createSalesManInfoFile(int)}. */
    private static final List<Salesman> generatedSalesmen = new ArrayList<>();

    /** Tipo de documento de cada vendedor generado, según su número de documento. */
    private static final Map<Long, String> documentTypeByNumber = new HashMap<>();

    /** Cuántos archivos de ventas se han creado para cada vendedor. */
    private static final Map<Long, Integer> salesFilesCountByNumber = new HashMap<>();

    /**
     * Constructor privado: esta clase solo tiene métodos estáticos y no se
     * crean objetos de ella.
     */
    private GenerateInfoFiles() {
    }

    /**
     * Punto de entrada: genera todos los archivos de prueba.
     *
     * <p>Primero borra los archivos de ventas de una ejecución anterior, luego
     * crea el archivo de productos, el de vendedores y los archivos de ventas.
     * Algunos vendedores reciben un segundo archivo de ventas para probar que
     * el programa principal suma varios archivos del mismo vendedor.</p>
     *
     * @param args no se usan
     */
    public static void main(String[] args) {
        try {
            int deletedFiles = deleteOldSalesFiles();
            createProductsFile(PRODUCTS_TO_GENERATE);
            createSalesManInfoFile(SALESMEN_TO_GENERATE);

            int salesFilesCreated = 0;
            for (Salesman salesman : generatedSalesmen) {
                createSalesMenFile(randomSalesCount(), salesman.getFullName(),
                        salesman.getDocumentNumber());
                salesFilesCreated++;

                if (RANDOM.nextDouble() < SECOND_FILE_PROBABILITY) {
                    createSalesMenFile(randomSalesCount(), salesman.getFullName(),
                            salesman.getDocumentNumber());
                    salesFilesCreated++;
                }
            }

            System.out.println("Archivos generados exitosamente.");
            System.out.println("  Archivos de ventas anteriores borrados: " + deletedFiles);
            System.out.println("  Productos: " + PRODUCTS_TO_GENERATE
                    + " (" + PRODUCTS_FILE_NAME + ")");
            System.out.println("  Vendedores: " + SALESMEN_TO_GENERATE
                    + " (" + SALESMEN_FILE_NAME + ")");
            System.out.println("  Archivos de ventas: " + salesFilesCreated);
        } catch (IOException | RuntimeException e) {
            System.err.println("Error: no se pudieron generar los archivos. " + e.getMessage());
        }
    }

    /**
     * Crea un archivo pseudoaleatorio de ventas para un vendedor.
     *
     * <p>La primera línea tiene el tipo y el número de documento del vendedor.
     * Cada línea siguiente es una venta de un producto existente, con una
     * cantidad entre 1 y {@value #MAX_QUANTITY_PER_SALE}. Si el mismo vendedor
     * ya tiene archivos, se crea uno nuevo con el siguiente número de secuencia,
     * sin borrar los anteriores.</p>
     *
     * <p>Requiere haber llamado antes a {@link #createProductsFile(int)}, porque
     * las ventas solo usan productos que existen.</p>
     *
     * @param randomSalesCount cantidad de ventas (líneas) que tendrá el archivo;
     *                         debe ser mayor que cero
     * @param name             nombre completo del vendedor; se usa en el nombre
     *                         del archivo
     * @param id               número de documento del vendedor
     * @throws IOException              si el archivo no se puede escribir
     * @throws IllegalArgumentException si {@code randomSalesCount} no es positivo
     * @throws IllegalStateException    si todavía no se han generado productos
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id)
            throws IOException {
        if (randomSalesCount <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de ventas debe ser mayor que cero: " + randomSalesCount);
        }
        if (generatedProductIds.isEmpty()) {
            throw new IllegalStateException(
                    "Primero hay que generar los productos con createProductsFile.");
        }

        String documentType = documentTypeByNumber.getOrDefault(id, "CC");
        int fileNumber = salesFilesCountByNumber.getOrDefault(id, 0) + 1;
        salesFilesCountByNumber.put(id, fileNumber);

        String fileName = SALES_FILE_PREFIX + toFileNamePart(name) + "_" + id
                + "_" + fileNumber + TEXT_FILE_EXTENSION;
        Path filePath = DATA_DIRECTORY.resolve(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(documentType + SEPARATOR + id);
            writer.newLine();

            for (int sale = 0; sale < randomSalesCount; sale++) {
                long productId = generatedProductIds.get(RANDOM.nextInt(generatedProductIds.size()));
                int quantity = 1 + RANDOM.nextInt(MAX_QUANTITY_PER_SALE);
                writer.write(productId + SEPARATOR + quantity + SEPARATOR);
                writer.newLine();
            }
        }
    }

    /**
     * Crea el archivo {@value #PRODUCTS_FILE_NAME} con información
     * pseudoaleatoria de productos.
     *
     * <p>Los identificadores son consecutivos desde {@value #FIRST_PRODUCT_ID}
     * para que nunca se repitan. Los nombres salen de una lista de productos
     * reales; si se piden más productos que nombres en la lista, se les agrega
     * un número para distinguirlos. Cada precio varía hasta un 20 % alrededor
     * de un precio de referencia realista.</p>
     *
     * @param productsCount cantidad de productos a generar; debe ser mayor que cero
     * @throws IOException              si el archivo no se puede escribir
     * @throws IllegalArgumentException si {@code productsCount} no es positivo
     */
    public static void createProductsFile(int productsCount) throws IOException {
        if (productsCount <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de productos debe ser mayor que cero: " + productsCount);
        }

        generatedProductIds.clear();
        Path filePath = DATA_DIRECTORY.resolve(PRODUCTS_FILE_NAME);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            for (int index = 0; index < productsCount; index++) {
                long productId = FIRST_PRODUCT_ID + index;
                int catalogIndex = index % PRODUCT_NAMES.length;
                String productName = PRODUCT_NAMES[catalogIndex];
                int round = index / PRODUCT_NAMES.length;
                if (round > 0) {
                    productName = productName + " #" + (round + 1);
                }

                long unitPrice = randomPrice(PRODUCT_BASE_PRICES[catalogIndex]);
                Product product = new Product(productId, productName, unitPrice);
                writer.write(product.toString());
                writer.newLine();
                generatedProductIds.add(productId);
            }
        }
    }

    /**
     * Crea el archivo {@value #SALESMEN_FILE_NAME} con información
     * pseudoaleatoria y coherente de vendedores.
     *
     * <p>Cada vendedor recibe un tipo de documento, un número de documento
     * único, uno o dos nombres reales y dos apellidos reales.</p>
     *
     * @param salesmanCount cantidad de vendedores a generar; debe ser mayor que cero
     * @throws IOException              si el archivo no se puede escribir
     * @throws IllegalArgumentException si {@code salesmanCount} no es positivo
     */
    public static void createSalesManInfoFile(int salesmanCount) throws IOException {
        if (salesmanCount <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de vendedores debe ser mayor que cero: " + salesmanCount);
        }

        generatedSalesmen.clear();
        documentTypeByNumber.clear();
        salesFilesCountByNumber.clear();
        Set<Long> usedDocumentNumbers = new HashSet<>();
        Path filePath = DATA_DIRECTORY.resolve(SALESMEN_FILE_NAME);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            for (int index = 0; index < salesmanCount; index++) {
                long documentNumber;
                do {
                    documentNumber = MIN_DOCUMENT_NUMBER + RANDOM.nextInt(DOCUMENT_NUMBER_RANGE);
                } while (!usedDocumentNumbers.add(documentNumber));

                String documentType = randomElement(DOCUMENT_TYPES);
                Salesman salesman = new Salesman(documentType, documentNumber,
                        randomFirstNames(), randomElement(LAST_NAMES) + " " + randomElement(LAST_NAMES));

                writer.write(salesman.toString());
                writer.newLine();
                generatedSalesmen.add(salesman);
                documentTypeByNumber.put(documentNumber, documentType);
            }
        }
    }

    /**
     * Borra los archivos de ventas que quedaron de una ejecución anterior, para
     * que no se mezclen con los nuevos.
     *
     * @return cantidad de archivos borrados
     * @throws IOException si la carpeta no se puede leer o un archivo no se puede borrar
     */
    private static int deleteOldSalesFiles() throws IOException {
        int deletedFiles = 0;
        String pattern = SALES_FILE_PREFIX + "*" + TEXT_FILE_EXTENSION;

        try (DirectoryStream<Path> oldFiles = Files.newDirectoryStream(DATA_DIRECTORY, pattern)) {
            for (Path oldFile : oldFiles) {
                Files.delete(oldFile);
                deletedFiles++;
            }
        }
        return deletedFiles;
    }

    /**
     * Devuelve una cantidad aleatoria de ventas para un archivo, entre
     * {@value #MIN_SALES_PER_FILE} y {@value #MAX_SALES_PER_FILE}.
     *
     * @return cantidad de ventas
     */
    private static int randomSalesCount() {
        return MIN_SALES_PER_FILE + RANDOM.nextInt(MAX_SALES_PER_FILE - MIN_SALES_PER_FILE + 1);
    }

    /**
     * Devuelve un precio aleatorio cercano al precio de referencia: hasta un
     * 20 % más barato o más caro, redondeado a un
     * múltiplo de {@value #PRICE_STEP}.
     *
     * @param basePrice precio de referencia del producto, en pesos
     * @return precio en pesos, nunca menor que {@value #PRICE_STEP}
     */
    private static long randomPrice(int basePrice) {
        double factor = 1 - PRICE_VARIATION + RANDOM.nextDouble() * 2 * PRICE_VARIATION;
        long price = Math.round(basePrice * factor / PRICE_STEP) * PRICE_STEP;
        return Math.max(price, PRICE_STEP);
    }

    /**
     * Devuelve uno o dos nombres de pila distintos, separados por un espacio.
     * Los dos nombres son del mismo género para que el resultado sea coherente.
     *
     * @return nombres del vendedor
     */
    private static String randomFirstNames() {
        String[] namesOfOneGender = RANDOM.nextBoolean() ? MALE_FIRST_NAMES : FEMALE_FIRST_NAMES;
        String firstName = randomElement(namesOfOneGender);
        if (RANDOM.nextBoolean()) {
            return firstName;
        }

        String secondName;
        do {
            secondName = randomElement(namesOfOneGender);
        } while (secondName.equals(firstName));
        return firstName + " " + secondName;
    }

    /**
     * Devuelve un elemento al azar de un arreglo.
     *
     * @param options arreglo del que se escoge; no puede estar vacío
     * @return elemento escogido
     */
    private static String randomElement(String[] options) {
        return options[RANDOM.nextInt(options.length)];
    }

    /**
     * Convierte un nombre en un texto seguro para usar en un nombre de archivo:
     * quita las tildes y cambia los espacios y demás símbolos por guiones bajos.
     *
     * @param text texto original, por ejemplo {@code "José Pérez"}
     * @return texto seguro, por ejemplo {@code "Jose_Perez"}
     */
    private static String toFileNamePart(String text) {
        String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.replaceAll("[^A-Za-z0-9]+", "_");
    }
}
