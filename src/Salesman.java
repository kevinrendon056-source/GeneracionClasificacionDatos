/**
 * Representa a un vendedor de la empresa.
 *
 * <p>Guarda los datos personales que vienen en el archivo de información de
 * vendedores y acumula el dinero total que el vendedor recaudó según sus
 * archivos de ventas.</p>
 *
 * @author Kevin
 * @version 2.0
 */
public class Salesman {

    /** Tipo de documento de identidad (por ejemplo CC, CE, TI o PA). */
    private final String documentType;

    /** Número del documento de identidad; identifica al vendedor. */
    private final long documentNumber;

    /** Nombres del vendedor. */
    private final String firstNames;

    /** Apellidos del vendedor. */
    private final String lastNames;

    /** Dinero total recaudado por el vendedor, en pesos. */
    private long totalSales;

    /**
     * Crea un vendedor sin ventas registradas.
     *
     * @param documentType   tipo de documento de identidad
     * @param documentNumber número de documento de identidad
     * @param firstNames     nombres del vendedor
     * @param lastNames      apellidos del vendedor
     */
    public Salesman(String documentType, long documentNumber,
            String firstNames, String lastNames) {
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.totalSales = 0;
    }

    /**
     * Suma el valor de una venta al total recaudado por el vendedor.
     *
     * @param amount valor de la venta, en pesos
     */
    public void addSale(long amount) {
        totalSales += amount;
    }

    /**
     * Devuelve el tipo de documento del vendedor.
     *
     * @return tipo de documento de identidad
     */
    public String getDocumentType() {
        return documentType;
    }

    /**
     * Devuelve el número de documento del vendedor.
     *
     * @return número de documento de identidad
     */
    public long getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Devuelve los nombres del vendedor.
     *
     * @return nombres del vendedor
     */
    public String getFirstNames() {
        return firstNames;
    }

    /**
     * Devuelve los apellidos del vendedor.
     *
     * @return apellidos del vendedor
     */
    public String getLastNames() {
        return lastNames;
    }

    /**
     * Devuelve el nombre completo: nombres seguidos de apellidos.
     *
     * @return nombre completo del vendedor
     */
    public String getFullName() {
        return firstNames + " " + lastNames;
    }

    /**
     * Devuelve el dinero total recaudado por el vendedor.
     *
     * @return total de ventas, en pesos
     */
    public long getTotalSales() {
        return totalSales;
    }

    /**
     * Devuelve el vendedor en el formato del archivo de información de
     * vendedores: {@code TipoDocumento;NumeroDocumento;Nombres;Apellidos}.
     *
     * @return línea lista para escribir en el archivo de vendedores
     */
    @Override
    public String toString() {
        return documentType + ";" + documentNumber + ";" + firstNames + ";" + lastNames;
    }
}
