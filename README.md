# Generación y clasificación de datos

Proyecto del módulo Conceptos Fundamentales de Programación (Politécnico Grancolombiano).
Proyecto de Eclipse, Java 8.

## Qué hace

El proyecto tiene dos (y solo dos) clases con método `main`:

1. **GenerateInfoFiles**: genera los archivos planos pseudoaleatorios de entrada.
2. **main**: lee esos archivos y genera los dos reportes.

Ninguno de los dos le pide datos al usuario. Los dos muestran un mensaje de finalización exitosa o un mensaje de error.

## Cómo ejecutarlo

En Eclipse: `File > Import > General > Existing Projects into Workspace`, escoger esta carpeta, y luego clic derecho sobre `GenerateInfoFiles.java > Run As > Java Application`. Después, lo mismo con `main.java`.

En la terminal, desde esta carpeta:

```
javac -encoding UTF-8 -d bin src/*.java
java -cp bin GenerateInfoFiles
java -cp bin main
```

## Archivos de entrada (los crea GenerateInfoFiles)

| Archivo | Formato de cada línea |
|---|---|
| `products.txt` | `IDProducto;NombreProducto;PrecioPorUnidad` |
| `salesmen.txt` | `TipoDocumento;NumeroDocumento;Nombres;Apellidos` |
| `sales_<nombre>_<documento>_<n>.txt` | Primera línea `TipoDocumento;NumeroDocumento`, luego una venta por línea: `IDProducto;Cantidad;` |

Métodos de generación pedidos en el enunciado:

- `createSalesMenFile(int randomSalesCount, String name, long id)`
- `createProductsFile(int productsCount)`
- `createSalesManInfoFile(int salesmanCount)`

## Reportes (los crea main)

| Archivo | Contenido |
|---|---|
| `salesmen_report.csv` | `NombreCompleto;DineroRecaudado`, de mayor a menor dinero |
| `products_report.csv` | `Nombre;Precio;CantidadVendida`, de mayor a menor cantidad vendida |

## Extras implementados

- **Más de un archivo por vendedor**: GenerateInfoFiles crea un segundo archivo de ventas para algunos vendedores y main suma todos los archivos del mismo vendedor.
- **Detección de datos erróneos**: main descarta y reporta como advertencia los productos inexistentes, las cantidades y precios negativos o en cero, los vendedores que no están registrados, los archivos vacíos y las líneas mal escritas, sin detener el programa.

## Estructura

```
src/
  GenerateInfoFiles.java   genera los archivos de entrada
  main.java                genera los reportes
  Product.java             un producto: id, nombre, precio y unidades vendidas
  Salesman.java            un vendedor: documento, nombres, apellidos y total recaudado
```
