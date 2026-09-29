import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Programa principal: lee los archivos de productos, vendedores y ventas, y
 * genera los dos reportes pedidos.
 *
 * <p>Los archivos de entrada deben estar en la carpeta del proyecto (los crea
 * {@link GenerateInfoFiles}). Al ejecutarse se crean:</p>
 * <ul>
 *   <li>{@value #SALESMEN_REPORT_FILE_NAME}: un vendedor por línea con el
 *       formato {@code NombreCompleto;DineroRecaudado}, ordenado de mayor a
 *       menor dinero recaudado.</li>
 *   <li>{@value #PRODUCTS_REPORT_FILE_NAME}: un producto vendido por línea con
 *       el formato {@code Nombre;Precio;CantidadVendida}, ordenado de mayor a
 *       menor cantidad vendida.</li>
 * </ul>
 *
 * <p>Si un vendedor tiene varios archivos de ventas, se suman todos. Las líneas
 * con formato erróneo o información incoherente (un producto que no existe,
 * cantidades o precios negativos, un vendedor desconocido) se descartan y se
 * informan como advertencias, sin detener el programa.</p>
 *
 * <p>El programa no le pide datos al usuario. Al terminar muestra un mensaje
 * de finalización exitosa o un mensaje de error.</p>
 *
 * @author Kevin
 * @version 2.0
 */
public class main {

    /** Nombre del reporte de vendedores ordenado por dinero recaudado. */
    public static final String SALESMEN_REPORT_FILE_NAME = "salesmen_report.csv";

    /** Nombre del reporte de productos ordenado por cantidad vendida. */
    public static final String PRODUCTS_REPORT_FILE_NAME = "products_report.csv";

    /** Advertencias encontradas al leer los archivos de entrada. */
    private static final List<String> warnings = new ArrayList<>();

    /**
     * Constructor privado: esta clase solo tiene métodos estáticos y no se
     * crean objetos de ella.
     */
    private main() {
    }

    /**
     * Punto de entrada: carga los archivos, procesa las ventas y escribe los
     * reportes.
     *
     * @param args no se usan
     */
    public static void main(String[] args) {
        try {
            Map<Long, Product> products = loadProducts(
                    GenerateInfoFiles.DATA_DIRECTORY.resolve(GenerateInfoFiles.PRODUCTS_FILE_NAME));
            Map<Long, Salesman> salesmen = loadSalesmen(
                    GenerateInfoFiles.DATA_DIRECTORY.resolve(GenerateInfoFiles.SALESMEN_FILE_NAME));
            int processedFiles = processSalesFiles(products, salesmen);

            writeSalesmenReport(new ArrayList<>(salesmen.values()));
            writeProductsReport(new ArrayList<>(products.values()));

            System.out.println("Reportes generados exitosamente.");
            System.out.println("  Archivos de ventas procesados: " + processedFiles);
            System.out.println("  " + SALESMEN_REPORT_FILE_NAME + " y " + PRODUCTS_REPORT_FILE_NAME
                    + " creados en la carpeta del proyecto.");
            printWarnings();
        } catch (IOException | RuntimeException e) {
            System.err.println("Error: no se pudieron generar los reportes. " + e.getMessage());
        }
    }

    /**
     * Lee el archivo de productos.
     *
     * <p>Cada línea válida tiene el formato {@code ID;Nombre;Precio}. Se
     * descartan con advertencia las líneas con campos faltantes, números mal
     * escritos, precios negativos o en cero e identificadores repetidos.</p>
     *
     * @param filePath ruta del archivo de productos
     * @return productos leídos, indexados por su identificador
     * @throws IOException           si el archivo no existe o no se puede leer
     * @throws IllegalStateException si el archivo no tiene ningún producto válido
     */
    private static Map<Long, Product> loadProducts(Path filePath) throws IOException {
        List<String> lines = readInputFile(filePath);
        Map<Long, Product> products = new LinkedHashMap<>();

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (line.isEmpty()) {
                continue;
            }

            String location = filePath.getFileName() + ", línea " + (index + 1);
            String[] fields = line.split(GenerateInfoFiles.SEPARATOR);
            if (fields.length < 3) {
                warnings.add(location + ": faltan campos, se esperaba ID;Nombre;Precio.");
                continue;
            }

            try {
                long productId = Long.parseLong(fields[0].trim());
                String productName = fields[1].trim();
                long unitPrice = Long.parseLong(fields[2].trim());

                if (unitPrice <= 0) {
                    warnings.add(location + ": el precio debe ser positivo (" + unitPrice + ").");
                } else if (products.containsKey(productId)) {
                    warnings.add(location + ": el producto " + productId + " está repetido.");
                } else {
                    products.put(productId, new Product(productId, productName, unitPrice));
                }
            } catch (NumberFormatException e) {
                warnings.add(location + ": el ID o el precio no es un número.");
            }
        }

        if (products.isEmpty()) {
            throw new IllegalStateException("El archivo " + filePath.getFileName()
                    + " no tiene productos válidos.");
        }
        return products;
    }

    /**
     * Lee el archivo de información de vendedores.
     *
     * <p>Cada línea válida tiene el formato
     * {@code TipoDocumento;NumeroDocumento;Nombres;Apellidos}. Se descartan con
     * advertencia las líneas con campos faltantes, números de documento mal
     * escritos o documentos repetidos.</p>
     *
     * @param filePath ruta del archivo de vendedores
     * @return vendedores leídos, indexados por su número de documento
     * @throws IOException           si el archivo no existe o no se puede leer
     * @throws IllegalStateException si el archivo no tiene ningún vendedor válido
     */
    private static Map<Long, Salesman> loadSalesmen(Path filePath) throws IOException {
        List<String> lines = readInputFile(filePath);
        Map<Long, Salesman> salesmen = new LinkedHashMap<>();

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (line.isEmpty()) {
                continue;
            }

            String location = filePath.getFileName() + ", línea " + (index + 1);
            String[] fields = line.split(GenerateInfoFiles.SEPARATOR);
            if (fields.length < 4) {
                warnings.add(location
                        + ": faltan campos, se esperaba TipoDocumento;Numero;Nombres;Apellidos.");
                continue;
            }

            try {
                long documentNumber = Long.parseLong(fields[1].trim());
                if (salesmen.containsKey(documentNumber)) {
                    warnings.add(location + ": el documento " + documentNumber + " está repetido.");
                } else {
                    salesmen.put(documentNumber, new Salesman(fields[0].trim(), documentNumber,
                            fields[2].trim(), fields[3].trim()));
                }
            } catch (NumberFormatException e) {
                warnings.add(location + ": el número de documento no es un número.");
            }
        }

        if (salesmen.isEmpty()) {
            throw new IllegalStateException("El archivo " + filePath.getFileName()
                    + " no tiene vendedores válidos.");
        }
        return salesmen;
    }

    /**
     * Recorre todos los archivos de ventas de la carpeta del proyecto y suma
     * cada venta al vendedor y al producto que corresponden.
     *
     * @param products productos disponibles, indexados por identificador
     * @param salesmen vendedores registrados, indexados por número de documento
     * @return cantidad de archivos de ventas procesados
     * @throws IOException           si la carpeta o algún archivo no se puede leer
     * @throws IllegalStateException si no hay ningún archivo de ventas
     */
    private static int processSalesFiles(Map<Long, Product> products,
            Map<Long, Salesman> salesmen) throws IOException {
        int processedFiles = 0;
        String pattern = GenerateInfoFiles.SALES_FILE_PREFIX + "*"
                + GenerateInfoFiles.TEXT_FILE_EXTENSION;

        try (DirectoryStream<Path> salesFiles = Files.newDirectoryStream(
                GenerateInfoFiles.DATA_DIRECTORY, pattern)) {
            for (Path salesFile : salesFiles) {
                processSalesFile(salesFile, products, salesmen);
                processedFiles++;
            }
        }

        if (processedFiles == 0) {
            throw new IllegalStateException("No se encontraron archivos de ventas ("
                    + pattern + "). Ejecute primero GenerateInfoFiles.");
        }
        return processedFiles;
    }

    /**
     * Procesa un archivo de ventas de un vendedor.
     *
     * <p>La primera línea debe ser {@code TipoDocumento;NumeroDocumento} de un
     * vendedor registrado; si no lo es, se descarta el archivo completo. Cada
     * línea siguiente debe ser {@code IDProducto;Cantidad;}. Se descartan con
     * advertencia las ventas de productos que no existen, las cantidades en cero
     * o negativas y las líneas mal escritas.</p>
     *
     * @param salesFile ruta del archivo de ventas
     * @param products  productos disponibles, indexados por identificador
     * @param salesmen  vendedores registrados, indexados por número de documento
     * @throws IOException si el archivo no se puede leer
     */
    private static void processSalesFile(Path salesFile, Map<Long, Product> products,
            Map<Long, Salesman> salesmen) throws IOException {
        List<String> lines = Files.readAllLines(salesFile, StandardCharsets.UTF_8);
        String fileName = salesFile.getFileName().toString();

        if (lines.isEmpty()) {
            warnings.add(fileName + ": el archivo está vacío, se descarta.");
            return;
        }

        Salesman salesman = findSalesman(lines.get(0).trim(), fileName, salesmen);
        if (salesman == null) {
            return;
        }

        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (line.isEmpty()) {
                continue;
            }

            String location = fileName + ", línea " + (index + 1);
            String[] fields = line.split(GenerateInfoFiles.SEPARATOR);
            if (fields.length < 2) {
                warnings.add(location + ": faltan campos, se esperaba IDProducto;Cantidad;");
                continue;
            }

            try {
                long productId = Long.parseLong(fields[0].trim());
                int quantity = Integer.parseInt(fields[1].trim());
                Product product = products.get(productId);

                if (product == null) {
                    warnings.add(location + ": el producto " + productId + " no existe.");
                } else if (quantity <= 0) {
                    warnings.add(location + ": la cantidad debe ser positiva (" + quantity + ").");
                } else {
                    product.addQuantitySold(quantity);
                    salesman.addSale(product.getUnitPrice() * quantity);
                }
            } catch (NumberFormatException e) {
                warnings.add(location + ": el ID del producto o la cantidad no es un número.");
            }
        }
    }

    /**
     * Busca el vendedor indicado en la primera línea de un archivo de ventas.
     *
     * @param headerLine primera línea del archivo: {@code TipoDocumento;NumeroDocumento}
     * @param fileName   nombre del archivo, para las advertencias
     * @param salesmen   vendedores registrados, indexados por número de documento
     * @return el vendedor encontrado, o {@code null} si la línea está mal escrita
     *         o el vendedor no está registrado
     */
    private static Salesman findSalesman(String headerLine, String fileName,
            Map<Long, Salesman> salesmen) {
        String[] fields = headerLine.split(GenerateInfoFiles.SEPARATOR);
        if (fields.length < 2) {
            warnings.add(fileName + ": la primera línea debe ser TipoDocumento;NumeroDocumento,"
                    + " se descarta el archivo.");
            return null;
        }

        try {
            long documentNumber = Long.parseLong(fields[1].trim());
            Salesman salesman = salesmen.get(documentNumber);
            if (salesman == null) {
                warnings.add(fileName + ": el vendedor " + documentNumber
                        + " no está en el archivo de vendedores, se descarta el archivo.");
            } else if (!salesman.getDocumentType().equals(fields[0].trim())) {
                warnings.add(fileName + ": el tipo de documento " + fields[0].trim()
                        + " no coincide con el registrado (" + salesman.getDocumentType() + ").");
            }
            return salesman;
        } catch (NumberFormatException e) {
            warnings.add(fileName + ": el número de documento no es un número, se descarta el archivo.");
            return null;
        }
    }

    /**
     * Escribe el reporte de vendedores ordenado de mayor a menor dinero
     * recaudado. Si dos vendedores recaudaron lo mismo, se ordenan por nombre.
     *
     * @param salesmen vendedores con sus ventas ya sumadas
     * @throws IOException si el reporte no se puede escribir
     */
    private static void writeSalesmenReport(List<Salesman> salesmen) throws IOException {
        salesmen.sort(Comparator.comparingLong(Salesman::getTotalSales).reversed()
                .thenComparing(Salesman::getFullName));

        Path reportPath = GenerateInfoFiles.DATA_DIRECTORY.resolve(SALESMEN_REPORT_FILE_NAME);
        try (BufferedWriter writer = Files.newBufferedWriter(reportPath, StandardCharsets.UTF_8)) {
            for (Salesman salesman : salesmen) {
                writer.write(salesman.getFullName() + GenerateInfoFiles.SEPARATOR
                        + salesman.getTotalSales());
                writer.newLine();
            }
        }
    }

    /**
     * Escribe el reporte de productos vendidos ordenado de mayor a menor
     * cantidad vendida. Los productos que no se vendieron no aparecen. Si dos
     * productos tienen la misma cantidad, se ordenan por nombre.
     *
     * @param products productos con sus cantidades vendidas ya sumadas
     * @throws IOException si el reporte no se puede escribir
     */
    private static void writeProductsReport(List<Product> products) throws IOException {
        List<Product> soldProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.getQuantitySold() > 0) {
                soldProducts.add(product);
            }
        }
        soldProducts.sort(Comparator.comparingInt(Product::getQuantitySold).reversed()
                .thenComparing(Product::getName));

        Path reportPath = GenerateInfoFiles.DATA_DIRECTORY.resolve(PRODUCTS_REPORT_FILE_NAME);
        try (BufferedWriter writer = Files.newBufferedWriter(reportPath, StandardCharsets.UTF_8)) {
            for (Product product : soldProducts) {
                writer.write(product.getName() + GenerateInfoFiles.SEPARATOR
                        + product.getUnitPrice() + GenerateInfoFiles.SEPARATOR
                        + product.getQuantitySold());
                writer.newLine();
            }
        }
    }

    /**
     * Lee todas las líneas de un archivo de entrada obligatorio.
     *
     * @param filePath ruta del archivo
     * @return líneas del archivo
     * @throws IOException si el archivo no existe o no se puede leer
     */
    private static List<String> readInputFile(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            throw new IOException("No existe el archivo " + filePath.getFileName()
                    + ". Ejecute primero GenerateInfoFiles.");
        }
        return Files.readAllLines(filePath, StandardCharsets.UTF_8);
    }

    /**
     * Muestra en consola las advertencias encontradas, si hubo alguna.
     */
    private static void printWarnings() {
        if (warnings.isEmpty()) {
            System.out.println("  Sin datos erróneos en los archivos de entrada.");
            return;
        }

        System.out.println("  Advertencias (" + warnings.size() + " datos descartados):");
        for (String warning : warnings) {
            System.out.println("    - " + warning);
        }
    }
}
