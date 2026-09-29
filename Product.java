/**
 * Representa un producto disponible para la venta.
 *
 * <p>Cada producto tiene un identificador único, un nombre y un precio por
 * unidad. Además lleva la cuenta de cuántas unidades se han vendido, dato que
 * se usa para construir el reporte de productos ordenado por cantidad.</p>
 *
 * @author Kevin
 * @version 2.0
 */
public class Product {

    /** Identificador único del producto. */
    private final long id;

    /** Nombre del producto. */
    private final String name;

    /** Precio de una unidad del producto, en pesos. */
    private final long unitPrice;

    /** Total de unidades vendidas de este producto entre todos los vendedores. */
    private int quantitySold;

    /**
     * Crea un producto sin unidades vendidas.
     *
     * @param id        identificador único del producto
     * @param name      nombre del producto
     * @param unitPrice precio por unidad, en pesos; debe ser mayor que cero
     */
    public Product(long id, String name, long unitPrice) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantitySold = 0;
    }

    /**
     * Suma unidades vendidas a este producto.
     *
     * @param quantity cantidad de unidades vendidas en una venta
     */
    public void addQuantitySold(int quantity) {
        quantitySold += quantity;
    }

    /**
     * Devuelve el identificador del producto.
     *
     * @return identificador único
     */
    public long getId() {
        return id;
    }

    /**
     * Devuelve el nombre del producto.
     *
     * @return nombre del producto
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve el precio por unidad.
     *
     * @return precio por unidad, en pesos
     */
    public long getUnitPrice() {
        return unitPrice;
    }

    /**
     * Devuelve cuántas unidades se han vendido de este producto.
     *
     * @return total de unidades vendidas
     */
    public int getQuantitySold() {
        return quantitySold;
    }

    /**
     * Devuelve el producto en el formato del archivo de productos:
     * {@code ID;Nombre;Precio}.
     *
     * @return línea lista para escribir en el archivo de productos
     */
    @Override
    public String toString() {
        return id + ";" + name + ";" + unitPrice;
    }
}
