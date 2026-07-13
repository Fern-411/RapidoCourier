# RapidoCourier S.A.C. - Microservicios

Este repositorio contiene la implementación del ecosistema de microservicios para RapidoCourier S.A.C.

## 1. Mapa de Microservicios

El sistema consta de la siguiente infraestructura y 5 microservicios de negocio:

### Infraestructura
- **eureka-server** (8761): Registro y descubrimiento de servicios.
- **api-gateway** (8080): Punto de entrada único. Enrutamiento, validación de JWT, y filtro de trazabilidad global.
- **config-server** (8888): Configuración centralizada. Lee de un repositorio Git local (`~/rapidocourier-config`).
- **Vault**: Gestión de secretos (JWT secret key).

### Microservicios de Negocio
1. **servicio-auth** (8081)
   - *Bounded Context*: Gestión de Identidad y Accesos.
   - *Entidades*: `Usuario`, `Rol`.
   - *Requerimientos*: RF-08 (Roles).
   - *Base de Datos*: PostgreSQL (`db_auth`). Elegida por la necesidad de integridad relacional fuerte entre usuarios y roles, y facilidad de transacciones ACID para autenticación.
   - *Dependencias*: Ninguna (proveedor de tokens).

2. **servicio-clientes** (8082)
   - *Bounded Context*: Gestión de Perfiles de Cliente.
   - *Entidades*: `Cliente`.
   - *Requerimientos*: RF-01 (Registro RENIEC).
   - *Base de Datos*: PostgreSQL (`db_clientes`). Elegida porque el esquema de cliente es estructurado y no requiere flexibilidad NoSQL.
   - *Dependencias*: Comunicación sincrónica con RENIEC (vía Feign con Resilience4j CircuitBreaker).

3. **servicio-paquetes** (8083)
   - *Bounded Context*: Logística y Seguimiento de Paquetes.
   - *Entidades*: `Paquete`, `Categoria`, `HistorialEstado`.
   - *Requerimientos*: RF-02, RF-03, RF-04, RF-05, RF-06, RF-07, RF-09.
   - *Base de Datos*: PostgreSQL (`db_paquetes`). Elegida para manejar las relaciones `ManyToMany` con categorías y relaciones `OneToMany` con el historial de estados eficientemente, además de permitir consultas complejas con `JOIN` para filtros (RF-06).
   - *Dependencias*: Consulta sincrónica a `servicio-clientes` para validar existencia. Sincrónica a `servicio-pagos` para validación de estado.

4. **servicio-pagos** (8084)
   - *Bounded Context*: Gestión Financiera.
   - *Entidades*: `Pago`.
   - *Requerimientos*: Procesar el pago de la tarifa (Extensión funcional).
   - *Base de Datos*: PostgreSQL (`db_pagos`). Elegida estrictamente por transacciones ACID.
   - *Dependencias*: Ninguna.

5. **servicio-notificaciones** (8085)
   - *Bounded Context*: Comunicación al cliente.
   - *Entidades*: `Notificacion`.
   - *Requerimientos*: Registro de notificaciones enviadas.
   - *Base de Datos*: PostgreSQL (`db_notificaciones`).
   - *Dependencias*: Recibe peticiones asincrónicas/fire-and-forget desde `servicio-paquetes` cuando cambia un estado.

## 2. Decisiones de Diseño y Arquitectura

### Comunicación Inter-Servicio
- **Sincrónica (Feign)**: Utilizada entre `servicio-paquetes` y `servicio-clientes` para validar que el DNI del remitente/destinatario exista en el momento exacto del registro del paquete. Es necesaria una respuesta inmediata para proceder o rechazar la transacción.
- **Circuit Breaker**: Implementado en la llamada a RENIEC dentro de `servicio-clientes` porque dependemos de una API externa que puede ser inestable.

### Justificación de la Descomposición
Se optó por separar **Clientes**, **Paquetes** y **Auth** para aislar el dominio de seguridad del dominio logístico y del perfilamiento. Una **decisión difícil** fue si incluir el cálculo de tarifas y pagos en el mismo microservicio de paquetes. Optamos por extraer `servicio-pagos` como un microservicio independiente para que en un futuro pueda integrar pasarelas de pago externas sin acoplar esa lógica al ciclo de vida central del paquete.

### Tabla de Dependencias Inter-Servicio
| Servicio Origen | Servicio Destino | Tipo de Llamada | Propósito | Fallback (Resilience4j) |
| --- | --- | --- | --- | --- |
| `servicio-clientes` | `reniec` (API Externa)| Sincrónica (Feign) | Obtener nombre del cliente | Sí (Circuit Breaker) |
| `servicio-paquetes` | `servicio-clientes` | Sincrónica (Feign) | Validar remitente/destinatario | Sí (Circuit Breaker) |
| `servicio-paquetes` | `servicio-pagos` | Sincrónica (Feign) | Validar estado de pago | No (Manejo de Exception) |
| `servicio-paquetes` | `servicio-notif.` | Asíncrona (@Async) | Notificar cambio de estado | No |

### Dashboard Eureka
A continuación, una previsualización del Eureka Dashboard con los servicios registrados:
*(Inserta aquí la captura de pantalla de http://localhost:8761)*
![Eureka Dashboard Placeholder](https://via.placeholder.com/800x400.png?text=Dashboard+Eureka+Local)

### Modelo de Datos (Diagramas Lógicos)
- **db_auth**: `usuarios (id, nombre, email, password, rol_id)` -> `roles (id, nombre)`
- **db_clientes**: `clientes (id, dni, nombre_completo, email, telefono)`
- **db_paquetes**: 
  - `paquetes (id, codigo_rastreo, peso_kg, valor_declarado, tarifa, sucursal_origen, sucursal_destino, remitente_id, destinatario_id, estado_actual)`
  - `categorias (id, nombre)`
  - `paquetes_categorias (paquete_id, categoria_id)`
  - `historial_estados (id, paquete_id, estado, usuario, fecha)`
- **db_pagos**: `pagos (id, paquete_id, monto, estado_pago)`
- **db_notificaciones**: `notificaciones (id, paquete_id, email_destino, mensaje, fecha)`

## 3. Lógica de Negocio Documentada

### Cálculo de Tarifa (RF-03)
La tarifa en soles se calcula automáticamente en base al peso y al valor declarado:
- **Tarifa Base**: 10.00 Soles.
- **Costo por Peso**: 5.00 Soles por cada Kg adicional.
- **Seguro (Valor Declarado)**: 2% del valor declarado si supera los 100 Soles.
- **Fórmula**: `10.00 + (Peso * 5.00) + (ValorDeclarado > 100 ? ValorDeclarado * 0.02 : 0)`

### Transiciones de Estado del Paquete (RF-04)
Secuencia estricta permitida:
1. `REGISTRADO` (Estado inicial al crearse).
2. `EN_TRANSITO` (Solo desde REGISTRADO, asume que el pago fue validado).
3. `EN_SUCURSAL_DESTINO` (Solo desde EN_TRANSITO).
4. `ENTREGADO` (Solo desde EN_SUCURSAL_DESTINO).

Intentar saltar un estado (ej. `REGISTRADO` -> `ENTREGADO`) arrojará un error 409 Conflict.

## 4. Instrucciones para Ejecutar Localmente

### Prerrequisitos
1. Docker (para base de datos y Vault).
2. Repositorio de configuración local creado: `~/rapidocourier-config` inicializado con git y con los archivos `*.yaml`.

### Paso 1: Levantar Dependencias
```bash
# Levantar HashiCorp Vault en modo dev
docker run --cap-add=IPC_LOCK -e 'VAULT_DEV_ROOT_TOKEN_ID=root' -e 'VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200' -p 8200:8200 -d hashicorp/vault

# Registrar secreto JWT en Vault
docker exec -it <container_id> vault kv put secret/rapidocourier/auth jwt.secret="SuperSecretaClaveLargaParaJWTSignature2026!"
```

### Paso 2: Orden de Arranque de Microservicios
1. `eureka-server`
2. `config-server`
3. `api-gateway`
4. `servicio-auth`
5. `servicio-clientes`
6. `servicio-paquetes`
7. `servicio-pagos`
8. `servicio-notificaciones`

### Paso 3: Refrescar Configuración (Demostración de Actuator)
Si se modifica una propiedad en Git, refrescar así:
```bash
curl -X POST http://localhost:8083/actuator/refresh
```

