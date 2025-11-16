# Implementación de totalUnidades y precioTotal en Carrito

## ✅ Cambios Implementados

Se han agregado dos campos nuevos en la tabla `carrito` para llevar un registro de totales:
- **totalUnidades**: Suma total de cantidades de todos los productos
- **precioTotal**: Suma total del precio de todos los productos en el carrito

---

## 📋 Modificaciones Realizadas

### **1. Modelo Cart.java**

**Campos Agregados:**
```java
@Column("total_unidades")
private Long totalUnidades;

@Column("precio_total")
private Integer precioTotal;
```

**Estado:** ✅ COMPLETADO  
**Capa:** Acceso a Datos (Modelo)

---

### **2. DTO CartWithProductsDto.java**

**Campos Agregados:**
```java
@Schema(description = "Total de unidades de todos los productos (suma de cantidades)", example = "5")
private Long totalUnidades;

@Schema(description = "Precio total del carrito (suma de todos los precios)", example = "2500")
private Integer precioTotal;
```

**Estado:** ✅ COMPLETADO  
**Capa:** Presentación (DTO)

---

### **3. CartCustomerService.java - Métodos Actualizados**

#### **addProductToCart()**
**Cambio Principal:**
```java
// ANTES: Solo contaba productos diferentes
cartDetailRepository.countByCarritoId(cart.getId())
    .flatMap(count -> {
        cart.setNumeroProductos(count);
        return cartRepository.save(cart);
    })

// AHORA: Calcula totales completos
cartDetailRepository.findAllByCarritoId(cart.getId())
    .collectList()
    .flatMap(allDetails -> {
        // Número de productos diferentes
        Long numeroProductos = (long) allDetails.size();
        
        // Total de unidades (suma de cantidades)
        Long totalUnidades = allDetails.stream()
                .mapToLong(CartDetail::getCantidad)
                .sum();
        
        // Precio total (suma de precios)
        Integer precioTotal = allDetails.stream()
                .mapToInt(CartDetail::getPrecioTotal)
                .sum();
        
        cart.setNumeroProductos(numeroProductos);
        cart.setTotalUnidades(totalUnidades);
        cart.setPrecioTotal(precioTotal);
        
        return cartRepository.save(cart);
    })
```

**Beneficio:** Cada vez que se agrega un producto, se recalculan todos los totales automáticamente.

---

#### **removeProductFromCart()**
**Cambio:**
```java
// Recalcular totales después de eliminar
cartDetailRepository.findAllByCarritoId(cart.getId())
    .collectList()
    .flatMap(allDetails -> {
        Long numeroProductos = (long) allDetails.size();
        
        Long totalUnidades = allDetails.stream()
                .mapToLong(CartDetail::getCantidad)
                .sum();
        
        Integer precioTotal = allDetails.stream()
                .mapToInt(CartDetail::getPrecioTotal)
                .sum();
        
        cart.setNumeroProductos(numeroProductos);
        cart.setTotalUnidades(totalUnidades);
        cart.setPrecioTotal(precioTotal);
        
        return cartRepository.save(cart);
    })
```

**Beneficio:** Los totales se actualizan correctamente al eliminar productos.

---

#### **clearCart()**
**Cambio:**
```java
// Resetear todos los totales a 0
cart.setNumeroProductos(0L);
cart.setTotalUnidades(0L);
cart.setPrecioTotal(0);
```

**Beneficio:** Carrito vacío tiene todos los contadores en 0.

---

#### **getCartWithProducts()**
**Cambio:**
```java
// Mapear nuevos campos al DTO
cartDto.setNumeroProductos(cart.getNumeroProductos());
cartDto.setTotalUnidades(cart.getTotalUnidades());
cartDto.setPrecioTotal(cart.getPrecioTotal());
```

**Beneficio:** El DTO retorna los totales precalculados sin necesidad de calcularlos cada vez.

---

### **4. CartAdminService.java**

#### **getCartWithProductsIdCart()**
**Cambio:** Mismo que getCartWithProducts()
```java
cartDto.setNumeroProductos(cart.getNumeroProductos());
cartDto.setTotalUnidades(cart.getTotalUnidades());
cartDto.setPrecioTotal(cart.getPrecioTotal());
```

**Estado:** ✅ COMPLETADO  
**Capa:** Lógica de Negocio

---

### **5. Swagger Actualizado**

**Ejemplos de Response Actualizados en 4 Endpoints:**

#### **POST /api/v1/carts/agregarproducto**
```json
{
  "cartId": 1,
  "userId": 123,
  "nombreUsuario": "Juan Pérez García",
  "estadoCarrito": "activo",
  "numeroProductos": 2,
  "totalUnidades": 5,        ← NUEVO
  "precioTotal": 2500,       ← NUEVO
  "products": [
    {
      "productoId": 100,
      "cantidad": 1,
      "precioTotal": 1500
    },
    {
      "productoId": 200,
      "cantidad": 4,
      "precioTotal": 1000
    }
  ]
}
```

#### **DELETE /api/v1/carts/vaciarcarrito**
```json
{
  "cartId": 1,
  "numeroProductos": 0,
  "totalUnidades": 0,        ← NUEVO (resetado)
  "precioTotal": 0,          ← NUEVO (resetado)
  "products": []
}
```

**Estado:** ✅ COMPLETADO  
**Capa:** Presentación (Documentación)

---

## 🏛️ Arquitectura de Tres Capas - RESPETADA

```
┌──────────────────────────────────────────────┐
│  CAPA DE PRESENTACIÓN                        │
│  ✅ MODIFICADA                               │
├──────────────────────────────────────────────┤
│  • CartWithProductsDto (+2 campos)           │
│  • Controllers (Swagger actualizado)         │
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  CAPA DE LÓGICA DE NEGOCIO                   │
│  ✅ MEJORADA                                 │
├──────────────────────────────────────────────┤
│  • CartCustomerService                       │
│    - addProductToCart() → Calcula totales    │
│    - removeProductFromCart() → Recalcula     │
│    - clearCart() → Resetea a 0               │
│    - getCartWithProducts() → Mapea totales   │
│                                               │
│  • CartAdminService                          │
│    - getCartWithProductsIdCart() → Mapea     │
└──────────────────────────────────────────────┘
                    ↓
┌──────────────────────────────────────────────┐
│  CAPA DE ACCESO A DATOS                      │
│  ✅ MODIFICADA (Modelo)                      │
├──────────────────────────────────────────────┤
│  • Cart (+2 campos en modelo)                │
│  • Repositories (sin cambios)                │
│  • Clients (sin cambios)                     │
└──────────────────────────────────────────────┘
```

---

## 📊 Ejemplo de Cálculo de Totales

### **Escenario: Carrito con 2 productos**

| Producto | Cantidad | Precio Unitario | Precio Total |
|----------|----------|-----------------|--------------|
| Laptop Dell XPS 15 | 1 | 1500 | 1500 |
| Mouse Logitech | 4 | 250 | 1000 |

**Cálculos:**
```java
numeroProductos = 2                  // Productos diferentes
totalUnidades = 1 + 4 = 5           // Suma de cantidades
precioTotal = 1500 + 1000 = 2500    // Suma de precios totales
```

**Response del Carrito:**
```json
{
  "cartId": 1,
  "numeroProductos": 2,
  "totalUnidades": 5,
  "precioTotal": 2500,
  "products": [...]
}
```

---

## 🔄 Flujo de Actualización de Totales

### **Al Agregar Producto**
```
1. Usuario agrega producto (productId=100, quantity=2)
2. Se guarda/actualiza CartDetail
3. Se consultan TODOS los CartDetail del carrito
4. Se calcula:
   - numeroProductos = count de detalles
   - totalUnidades = sum(cantidad)
   - precioTotal = sum(precioTotal)
5. Se actualiza Cart con los nuevos totales
6. Se retorna CartWithProductsDto con totales actualizados
```

### **Al Eliminar Producto**
```
1. Usuario elimina producto (productId=100)
2. Se elimina CartDetail
3. Se consultan los CartDetail restantes
4. Se recalculan todos los totales
5. Se actualiza Cart
6. Se retorna CartWithProductsDto actualizado
```

### **Al Vaciar Carrito**
```
1. Usuario vacía el carrito
2. Se eliminan todos los CartDetail
3. Se resetean totales a 0:
   - numeroProductos = 0
   - totalUnidades = 0
   - precioTotal = 0
4. Se actualiza Cart
5. Se retorna CartWithProductsDto con totales en 0
```

---

## 📝 SQL para Actualizar la Base de Datos

```sql
-- Agregar columnas a la tabla carrito
ALTER TABLE carrito 
ADD COLUMN total_unidades BIGINT DEFAULT 0,
ADD COLUMN precio_total INTEGER DEFAULT 0;

-- Actualizar registros existentes (calcular totales)
UPDATE carrito c
SET 
    total_unidades = (
        SELECT COALESCE(SUM(cd.cantidad), 0) 
        FROM carrito_detalle cd 
        WHERE cd.carrito_id = c.id
    ),
    precio_total = (
        SELECT COALESCE(SUM(cd.precio_total), 0) 
        FROM carrito_detalle cd 
        WHERE cd.carrito_id = c.id
    );
```

---

## 📊 Comparación Antes vs Ahora

### **ANTES ❌**
```json
{
  "cartId": 1,
  "numeroProductos": 2,
  "products": [
    {"cantidad": 1, "precioTotal": 1500},
    {"cantidad": 4, "precioTotal": 1000}
  ]
}
```
**Problema:** Para saber el total de unidades y precio total, el frontend tenía que sumar manualmente.

### **AHORA ✅**
```json
{
  "cartId": 1,
  "numeroProductos": 2,
  "totalUnidades": 5,      ← Precalculado
  "precioTotal": 2500,     ← Precalculado
  "products": [
    {"cantidad": 1, "precioTotal": 1500},
    {"cantidad": 4, "precioTotal": 1000}
  ]
}
```
**Beneficio:** Totales precalculados y listos para usar.

---

## 🎯 Beneficios Implementados

### **1. Performance**
✅ Totales precalculados en base de datos  
✅ No se calculan en cada consulta  
✅ Frontend recibe datos listos para mostrar  

### **2. Consistencia**
✅ Totales siempre sincronizados con productos  
✅ Actualización automática en cada operación  
✅ Un solo punto de verdad (la base de datos)  

### **3. UX Mejorada**
✅ Frontend puede mostrar "X productos (Y unidades)"  
✅ "Total: $2500" sin cálculos adicionales  
✅ Resumen del carrito instantáneo  

### **4. Analytics**
✅ Fácil saber el valor promedio de carritos  
✅ Consultas SQL más simples  
✅ Reportes de ventas más rápidos  

---

## 📁 Archivos Modificados

```
✅ src/main/java/.../model/Cart.java
✅ src/main/java/.../dto/CartWithProductsDto.java
✅ src/main/java/.../service/CartCustomerService.java
✅ src/main/java/.../service/CartAdminService.java
✅ src/main/java/.../controller/CartCustomerController.java
✅ src/main/java/.../controller/CardAdminController.java
```

**Total:** 6 archivos modificados  
**Campos agregados:** 2 (totalUnidades, precioTotal)  
**Métodos actualizados:** 5

---

## ✅ Validaciones Realizadas

### **Código sin Deprecar**
- ✅ `Stream.mapToLong()` - Java 8+ estándar
- ✅ `Stream.sum()` - Java 8+ estándar
- ✅ `collectList()` - Reactor estándar
- ✅ Sin warnings de deprecación

### **Arquitectura Limpia**
- ✅ Modelo con nuevos campos
- ✅ Lógica de cálculo en Services
- ✅ DTO actualizado para presentación
- ✅ Swagger documentado

---

## 🔧 Ejemplo de Uso

### **Request: Agregar Producto**
```http
POST /api/v1/carts/agregarproducto
Headers:
  X-Auth-User-Id: 123
Body:
{
  "productId": 100,
  "quantity": 2
}
```

### **Response:**
```json
{
  "cartId": 1,
  "userId": 123,
  "nombreUsuario": "Juan Pérez García",
  "estadoCarrito": "activo",
  "numeroProductos": 1,
  "totalUnidades": 2,        ← Total de unidades
  "precioTotal": 3000,       ← Total en pesos
  "products": [
    {
      "productoId": 100,
      "nombre": "Laptop",
      "cantidad": 2,
      "precioUnitario": 1500,
      "precioTotal": 3000
    }
  ]
}
```

---

## 📚 Documentación Swagger Actualizada

**Acceder a:** `http://localhost:8080/swagger-ui.html`

**Cambios visibles:**
- ✅ Schema de CartWithProductsDto con 2 campos nuevos
- ✅ Ejemplos de response con totalUnidades y precioTotal
- ✅ Descripciones claras de cada campo

---

**Fecha de Implementación:** 15 de Noviembre de 2025  
**Estado:** ✅ COMPLETADO  
**Arquitectura:** ✅ Tres Capas Respetada  
**Código Deprecado:** ✅ Ninguno  
**Campos Agregados:** totalUnidades, precioTotal  
**Base de Datos:** ⚠️ Requiere ALTER TABLE (SQL incluido arriba)

