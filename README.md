<p align="center">
  <img src="frontend/public/branding/dittostore-logo.jpg" alt="Ditto Store logo" width="460"/>
</p>
<h1 align="center">Ditto Store</h1>
 
E-commerce de cartas Pokémon TCG construido con arquitectura de microservicios, autenticación real vía Microsoft Entra External ID, mensajería asíncrona con RabbitMQ y despliegue en AWS.
 
---

## Descripción del proyecto

Ditto Store es una tienda en línea de cartas y cajas de Pokémon TCG. El sistema no cuenta con registro propio de usuarios: la autenticación se delega completamente a un tenant de **Microsoft Entra External ID**, y el rol de cada usuario (Cliente o Administrador) se gestiona en el backend propio.

La aplicación está compuesta por:

- Un **backend de microservicios** en Spring Boot, con descubrimiento de servicios, configuración centralizada y un Backend For Frontend (BFF) que valida los tokens JWT.
- Un **frontend en Angular** con dos experiencias diferenciadas por rol: la tienda para clientes y un panel de administración.
- Una capa de **API Manager en AWS** (HTTP API Gateway) que actúa como puerta de entrada pública al backend, con su propio Authorizer JWT.
- Un **clúster RabbitMQ de 2 nodos** para la comunicación asíncrona entre microservicios, con colas, exchanges, DLQ y un microservicio administrador (`rabbit-admin-service`).

---

## Arquitectura
![Diagrama de arquitectura de Ditto Store](frontend/public/branding/diagramadearquitectura.jpg)

La autenticación corre en paralelo: el frontend usa **MSAL** para autenticar contra Azure AD, obtiene un token JWT, y ese mismo token viaja en cada request hasta el BFF (o directo a `producto-service`/`reviews-service` en las rutas que van sin pasar por el BFF), que lo valida (firma, issuer, audience y vigencia) antes de dejar pasar la petición.

> Hay **dos capas de CORS** independientes en el camino: la del AWS API Gateway (a nivel de API, configurable en la consola) y la del API Gateway interno de Spring (`CorsConfig.java`). Si se agrega un nuevo dominio de frontend, hay que agregarlo **en ambas**, o el backend interno rechazará la petición con `403` aunque AWS ya la haya dejado pasar (ver [Problemas conocidos](#-problemas-conocidos-y-sus-soluciones)).

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 17, Spring Boot 4.0.8, Spring Cloud (Eureka, Config Server, Gateway, OpenFeign) |
| Base de datos | MySQL 8 |
| Mensajería | RabbitMQ 4.2 (clúster de 2 nodos con Docker Compose), Spring AMQP |
| Frontend | Angular (standalone components), MSAL v4 |
| Autenticación | Microsoft Entra External ID (Azure AD, tipo CIAM) |
| Nube | AWS (EC2, API Gateway) — cuenta de laboratorio estudiantil |
| Hosting frontend | Netlify (build vía variable de entorno, ver más abajo) |
| Build | Maven (backend), Angular CLI (frontend) |

---

## Estructura del repositorio

```
DittoStore/
├── backend/
│   ├── pom.xml                      # POM padre (dependencias compartidas)
│   ├── infrastructure/
│   │   ├── pom.xml
│   │   ├── eureka-server/
│   │   ├── config-server/
│   │   ├── api-gateway/             # Gateway interno Spring Cloud (CORS + rutas)
│   │   ├── bff-service/
│   │   └── rabbit-admin-service/    # Administrador REST de colas, exchanges y bindings
│   └── businessdomain/
│       ├── pom.xml
│       ├── producto-service/
│       ├── usuarios-service/
│       ├── carrito-service/
│       ├── pedidos-service/
│       ├── pago-service/
│       └── reviews-service/
├── config-repo/                     # Configuración centralizada (Config Server)
├── docker-compose.yml               # Clúster RabbitMQ de 2 nodos
├── rabbitmq/
│   └── cluster.conf                 # Formación automática del clúster
└── frontend/
    ├── set-env.js                   # Genera environment.prod.ts en cada build (ver más abajo)
    ├── src/app/
    │   ├── core/                    # Servicios, guards, modelos compartidos
    │   ├── features/
    │   │   └── admin/               # Panel de administración
    │   ├── shared/                  # Navbar, footer, componentes reutilizables
    │   ├── pages/                   # Páginas públicas de la tienda
    │   └── msal.config.ts           # protectedResourceMap: qué rutas llevan el Bearer token
    └── src/environments/
```

---

## Requisitos previos

- Java 17
- Maven (o usar el wrapper `./mvnw` incluido)
- Node.js 18+ y npm
- MySQL (local, ej. vía Laragon/XAMPP, o accesible remotamente)
- Docker y Docker Compose (para RabbitMQ)
- Angular CLI (`npm install -g @angular/cli`)
- Una cuenta de Azure con un tenant de Microsoft Entra External ID configurado (ver sección de Autenticación)

---

## Levantar el backend en local

1. **Configura MySQL**: crea las 6 bases de datos vacías (se autogeneran las tablas vía JPA):
```sql
   CREATE DATABASE dittostore_producto;
   CREATE DATABASE dittostore_usuarios;
   CREATE DATABASE dittostore_carrito;
   CREATE DATABASE dittostore_pedidos;
   CREATE DATABASE dittostore_pago;
   CREATE DATABASE dittostore_reviews;
```
   Por defecto, cada microservicio se conecta con el usuario `root` sin contraseña (ajusta en cada `application.properties` si el entorno usa otras credenciales).

2. **Compila el backend completo**:
```bash
   cd backend
   ./mvnw clean package -DskipTests
```

3. **Levanta RabbitMQ** (desde la raíz del repositorio; sin archivo `.env` usa `guest/guest`):
```bash
   docker compose up -d
```
   Dashboard en `http://localhost:15672`.

4. **Levanta los servicios en este orden** (cada uno en su propia terminal, o usando Spring Boot Dashboard):
```bash
   # 1. Eureka Server (registro de servicios)
   java -jar infrastructure/eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar

   # 2. Config Server (configuración centralizada, vía config-repo/ en este mismo repo)
   java -jar infrastructure/config-server/target/config-server-0.0.1-SNAPSHOT.jar

   # 3. API Gateway, BFF y administrador de RabbitMQ
   java -jar infrastructure/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
   java -jar infrastructure/bff-service/target/bff-service-0.0.1-SNAPSHOT.jar
   java -jar infrastructure/rabbit-admin-service/target/rabbit-admin-service-0.0.1-SNAPSHOT.jar

   # 4. Los 6 microservicios de negocio (orden indistinto entre ellos)
   java -jar businessdomain/producto-service/target/producto-service-0.0.1-SNAPSHOT.jar
   java -jar businessdomain/usuarios-service/target/usuarios-service-0.0.1-SNAPSHOT.jar
   java -jar businessdomain/carrito-service/target/carrito-service-0.0.1-SNAPSHOT.jar
   java -jar businessdomain/pedidos-service/target/pedidos-service-0.0.1-SNAPSHOT.jar
   java -jar businessdomain/pago-service/target/pago-service-0.0.1-SNAPSHOT.jar
   java -jar businessdomain/reviews-service/target/reviews-service-0.0.1-SNAPSHOT.jar
```

5. **Verifica**: entra a `http://localhost:8761` — deberías ver los 11 servicios registrados como `UP`.
---
### Puertos de referencia
---
| Servicio | Puerto |
|---|---|
| Eureka Server | 8761 |
| Config Server | 8888 |
| API Gateway (interno) | 8080 |
| BFF Service | 8081 |
| producto-service | 8082 |
| usuarios-service | 8083 |
| carrito-service | 8084 |
| pedidos-service | 8085 |
| pago-service | 8086 |
| reviews-service | 8087 |
| rabbit-admin-service | 8088 |
| RabbitMQ AMQP (nodo 1 / nodo 2) | 5672 / 5673 |
| RabbitMQ Dashboard (nodo 1 / nodo 2) | 15672 / 15673 |

---

## Levantar el frontend en local

```bash
cd frontend
npm install --legacy-peer-deps
ng serve
```

Abrir `http://localhost:4200`. Por defecto, el `environment.ts` (development) apunta al backend local (`http://localhost:8081` / `http://localhost:8080`) — este archivo no se toca nunca, a diferencia de `environment.prod.ts` (ver sección siguiente).

> **Nota**: el proyecto usa detección de cambios *zoneless*. Si se construye un componente nuevo que actualiza datos dentro de un `.subscribe()` (HTTP, MSAL, etc.), la vista no se refresca sola — inyecta `ChangeDetectorRef` y llama a `detectChanges()` manualmente, o usa Angular Signals.

---

## Despliegue en AWS

### Instancias EC2

El backend está desplegado en **3 instancias EC2** (se dividió por las limitaciones de memoria de una cuenta de AWS Academy — una sola instancia `t3.small` de 2 GB de RAM no soporta los 11 servicios de Java más RabbitMQ simultáneamente):

| Instancia | Contenido | IP elástica |
|---|---|---|
| `DittoStore` | MySQL, Eureka, Config Server, API Gateway, BFF, usuarios-service | *(ver configuración privada del equipo)* |
| `DittoStore-Negocio` | producto, carrito, pedidos, pago, reviews-service | *(ver configuración privada del equipo)* |
| `DittoStore-Rabbit` | Clúster RabbitMQ (docker-compose) y rabbit-admin-service | *(ver configuración privada del equipo)* |

Los microservicios de la segunda instancia y el `rabbit-admin-service` usan un perfil de Spring separado (`application-prod.properties`) que apunta a la IP **privada** de la primera instancia — el `application.properties` base sigue intacto en `localhost` para que el desarrollo local no se vea afectado.

---
### Levantar las instancias (consola AWS)
---
1. Entrar a la consola de AWS (o al portal del lab estudiantil, ej. AWS Academy Learner Lab) y arranca la sesión/lab.
2. Ir a **EC2 → Instancias**, seleccionar `DittoStore`, `DittoStore-Negocio` y `DittoStore-Rabbit`, y presionar **Iniciar instancia** en las tres (si el lab no las levantó solo).
3. Esperar a que el estado quede en `Running` y anotar las IPs (pública/privada) — en un lab que se destruye y recrea, estas pueden cambiar.
---
### Levantar los servicios (dentro de cada instancia, por SSH)
---
Conéctate por SSH (o usa el acceso que te dé el lab) y ejecuta, en orden, en `DittoStore (ssh -i db_dittostore.pem ubuntu@35.171.64.101)`:

```bash
# MySQL normalmente arranca solo como servicio; si no, en Ubuntu:
sudo systemctl start mysql

# Servicios de infraestructura, cada uno en su propia sesión 
nohup java -Xms64m -Xmx192m -jar infrastructure/eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar > eureka.log 2>&1 &

nohup java -Xms64m -Xmx192m -jar infrastructure/config-server/target/config-server-0.0.1-SNAPSHOT.jar > config-server.log 2>&1 &

nohup java -Xms64m -Xmx192m -jar infrastructure/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar > api-gateway.log 2>&1 &

nohup java -Xms64m -Xmx192m -jar infrastructure/bff-service/target/bff-service-0.0.1-SNAPSHOT.jar > bff-service.log 2>&1 &

nohup java -Xms64m -Xmx192m -jar businessdomain/usuarios-service/target/usuarios-service-0.0.1-SNAPSHOT.jar > usuarios-service.log 2>&1 &
```

Antes de levantar los microservicios de la segunda instancia, el clúster RabbitMQ de la tercera debe estar arriba (ver [Despliegue de RabbitMQ en AWS](#despliegue-de-rabbitmq-en-aws)). Luego, en `DittoStore-Negocio (ssh -i db_dittostore.pem ubuntu@54.166.52.100)`:

```bash
nohup java -Xms64m -Xmx192m -jar businessdomain/producto-service/target/producto-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > producto.log 2>&1 &

nohup java -Xms64m -Xmx192m -jar businessdomain/carrito-service/target/carrito-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > carrito.log 2>&1 &

nohup java -Xms64m -Xmx192m -jar businessdomain/pedidos-service/target/pedidos-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > pedidos.log 2>&1 &

nohup java -Xms64m -Xmx192m -jar businessdomain/pago-service/target/pago-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > pago.log 2>&1 &

nohup java -Xms64m -Xmx192m -jar businessdomain/reviews-service/target/reviews-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > reviews.log 2>&1 &
```

Conectárse a MySQL directamente en la instancia con:
```bash
mysql -u root
```
---
### API Manager de AWS (HTTP API Gateway)

Rutas configuradas en el API Gateway (visibles en **API Gateway → tu API → Routes**):

| Ruta | Métodos | Autorización |
|---|---|---|
| `/api/productos` | `GET`, `POST` | Sin autorizador (pública) |
| `/api/productos/{id}` | `GET`, `PUT`, `DELETE` | Sin autorizador (pública) |
| `/{proxy+}` | `ANY` | JWT Authorizer (tenant de Azure) |
| `/{proxy+}` | `OPTIONS` | **Sin autorizador** — necesaria para que el preflight de CORS no choque con el JWT Authorizer de la ruta `ANY` |

El endpoint `GET` de productos quedan públicos porque no debería requerirse login solo para navegar el catálogo; todo lo demás (`/bff/*`, `/api/carritos/*`, `/api/pedidos/*`, `/api/pagos/*`, `/api/reviews/*`, `/api/usuarios/*`) cae en el catch-all `ANY /{proxy+}` y exige un JWT válido.

**CORS a nivel de API** (API Gateway → tu API → CORS):

- **Access-Control-Allow-Origin**: `http://localhost:4200`, `https://dittostore.netlify.app`
- **Access-Control-Allow-Headers**: `content-type`, `authorization`
- **Access-Control-Allow-Methods**: `GET`, `POST`, `PATCH`, `PUT`, `DELETE`, `OPTIONS`
- **Access-Control-Allow-Credentials**: No

> Esta configuración de CORS y las rutas anteriores viven **únicamente en el recurso de AWS**, no en el código — si el API Gateway se recrea (ver más abajo), hay que rehacerlas a mano.

### Cuando el API Gateway cambia de ID

Si AWS genera el ID del API Gateway automáticamente y no se puede fijar, cada vez que el lab se recrea hay que hacer:

1. **Recrear las rutas y el CORS** de la tabla de arriba en el API Gateway nuevo (y volver a asociar el JWT Authorizer a `ANY /{proxy+}`).
2. **Copiar el nuevo Invoke URL** del API Gateway (algo como `https://<nuevo-id>.execute-api.us-east-1.amazonaws.com/dev`).
3. **Actualizar la variable de entorno `API_GATEWAY_URI` en Netlify** (Site configuration → Environment variables) con ese nuevo valor — **no** se edita `environment.prod.ts` a mano.
4. **Disparar un nuevo deploy en Netlify** (push, o *Trigger deploy* manual). El build corre `npm run build:prod`, que ejecuta `frontend/set-env.js` y regenera `environment.prod.ts` con la URL nueva antes de compilar.

Este flujo (variable de entorno + `set-env.js`) existe justamente para no tener que editar código ni hacer commits cada vez que el lab se reinicia — solo se toca el valor en el panel de Netlify.

---

## Mensajería asíncrona con RabbitMQ

Los microservicios se comunican de forma desacoplada mediante eventos sobre **RabbitMQ**, sin cambiar la lógica HTTP existente. Las operaciones principales siguen siendo síncronas; RabbitMQ se usa para efectos secundarios (notificaciones, comprobantes, reembolsos, vaciado del carrito, avisos a reseñas).

### Convenciones

- Los nombres de colas, exchanges y routing keys **no están escritos en el código**: se definen en el `application.properties` de cada servicio bajo `dittostore.rabbitmq.*` y se leen con un `@ConfigurationProperties` (`RabbitMQProperties`).
- Cada servicio declara sus `Queue`, `Exchange` y `Binding` como beans en `messaging/config/RabbitMQConfig`.
- Estructura de paquetes en cada servicio: `messaging/config`, `producer`, `consumer`, `event` (los mensajes) y `support` (manejo de ACK).
- Todas las colas son durables y tienen una **DLQ** asociada vía un exchange de dead letter propio de cada servicio.

### Topología

Exchanges `topic` de eventos:
- `pedido.topic.exchange`: lo publica `pedidos-service` con la routing key `pedido.estado.<estado>` (ej. `pedido.estado.confirmado`).
- `pago.topic.exchange`: lo publica `pago-service` con la routing key `pago.estado.<estado>` (ej. `pago.estado.aprobado`).

Exchange `direct`: `pago.direct.exchange`, para la solicitud de reembolsos (`pago.reembolso.solicitar`).

Colas por servicio (cada una con su `.dlq`):
- **pedidos-service** consume `pedidos.pago.queue` (`pago.estado.*`): aplica el resultado del pago al pedido. DLX: `pedidos.dlx.exchange`.
- **pago-service** consume tres colas: `pago.notificacion.queue` (`pago.estado.*`, registra la notificación), `pago.comprobante.queue` (`pago.estado.aprobado`, genera el comprobante) y `pago.reembolso.queue` (direct, procesa el reembolso). DLX: `pago.dlx.exchange`.
- **reviews-service** consume `reviews.pedido.queue` (`pedido.estado.*`) y `reviews.pago.queue` (`pago.estado.*`). DLX: `reviews.dlx.exchange`.
- **carrito-service** consume `carrito.pedido.queue` (`pedido.estado.*`): al confirmarse un pedido vacía el carrito y lo marca `CONVERTIDO`. DLX: `carrito.dlx.exchange`.

En total son 7 colas de negocio, cada una con su DLQ.

Los eventos se publican **después del commit** de la transacción, para no anunciar cambios que luego se reviertan. Para probar el flujo de reembolso: `POST /api/pagos/{id}/reembolso` responde `202` y encola la solicitud.

### Confirmación de mensajes y DLQ

Los consumidores usan **ACK manual** a través de `ManualAckHandler`:

1. Mensaje procesado correctamente → `ACK`.
2. JSON inválido, cuerpo vacío o error no recuperable (mensaje con datos faltantes, entidad inexistente, argumentos inválidos) → `NACK` sin reencolar, el exchange de dead letter lo envía a la **DLQ** y se registra en el log (`[DLQ]`).
3. Error recuperable (ej. base de datos caída) → el mensaje se reinserta en su cola con el header `x-retry-count`, con espera creciente (`backoff-ms` × intento), hasta `max-intentos` (3). Al agotarse, va a la DLQ (`[REINTENTO]` en el log).

`carrito-service` y `reviews-service` incluyen además un listener de DLQ que registra los mensajes muertos (`[DLQ-CARRITO]`, `[DLQ-REVIEWS]`) y se puede desactivar con `dittostore.rabbitmq.dlq-listener.enabled=false`.

### Clúster RabbitMQ (docker-compose)

El `docker-compose.yml` de la raíz levanta **2 nodos** (`rabbit1` y `rabbit2`, imagen `rabbitmq:4.2-management`) que forman un clúster con `rabbitmq/cluster.conf` (peer discovery `classic_config`). `rabbit2` espera a que `rabbit1` esté sano. Los servicios se conectan con ambos nodos mediante `spring.rabbitmq.addresses`.

Variables, definidas en un archivo `.env` junto al compose (ignorado por Git):

```dotenv
RABBITMQ_USER=<usuario>
RABBITMQ_PASS=<contraseña>
RABBITMQ_ERLANG_COOKIE=<cadena aleatoria, igual para ambos nodos>
```

Verificar el clúster:

```bash
docker compose ps
docker exec rabbit1 rabbitmqctl cluster_status
```

Dashboards: `http://<host>:15672` (nodo 1) y `http://<host>:15673` (nodo 2).

### rabbit-admin-service

Microservicio de infraestructura (puerto 8088) para administrar RabbitMQ por REST. Las peticiones requieren un JWT válido del mismo tenant de Azure y la lógica de RabbitMQ está encapsulada en `RabbitAdminService` (el controlador no usa la librería de RabbitMQ directamente).

- `POST /rabbit/admin/queues` y `DELETE /rabbit/admin/queues/{nombre}`
- `POST /rabbit/admin/exchanges` y `DELETE /rabbit/admin/exchanges/{nombre}`
- `POST /rabbit/admin/bindings` y `DELETE /rabbit/admin/bindings?cola=&exchange=&routingKey=`

Ejemplos:

```bash
curl -X POST http://localhost:8088/rabbit/admin/queues \
  -H "Authorization: Bearer <JWT>" -H "Content-Type: application/json" \
  -d '{"nombre":"demo.queue","durable":true}'

curl -X POST http://localhost:8088/rabbit/admin/exchanges \
  -H "Authorization: Bearer <JWT>" -H "Content-Type: application/json" \
  -d '{"nombre":"demo.topic","tipo":"topic"}'

curl -X POST http://localhost:8088/rabbit/admin/bindings \
  -H "Authorization: Bearer <JWT>" -H "Content-Type: application/json" \
  -d '{"cola":"demo.queue","exchange":"demo.topic","routingKey":"demo.*"}'
```

Validaciones: los nombres admiten solo letras, números, `.`, `_` y `-` (máximo 255) y no pueden empezar con `amq.`; el tipo de exchange debe ser `direct`, `topic`, `fanout` o `headers`. Los datos inválidos responden `400` con un mensaje claro y los fallos del broker responden `502`.

### Despliegue de RabbitMQ en AWS

RabbitMQ y `rabbit-admin-service` corren en una tercera instancia, `DittoStore-Rabbit` (`t3.small`, Ubuntu, IP privada `172.31.20.162`). Los microservicios de las otras dos instancias se conectan a ella por IP privada usando `RABBITMQ_HOST` (valor por defecto en sus `application-prod.properties`).

Preparar la instancia:

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu          
sudo apt install -y git openjdk-17-jre-headless
```

Levantar el clúster (con el `.env` creado junto al compose) y el administrador:

```bash
docker compose up -d
docker compose ps
docker exec rabbit1 rabbitmqctl cluster_status

nohup java -Xms64m -Xmx256m -jar rabbit-admin-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod > rabbit-admin.log 2>&1 &
```

Grupo de seguridad de la instancia:
- `5672` y `5673` (AMQP): solo desde el grupo de seguridad de las otras dos instancias.
- `15672` (dashboard) y `8088` (administrador): solo desde la IP del equipo.
- `22` (SSH). Ningún otro puerto.

Orden de arranque: primero el clúster RabbitMQ, luego los microservicios de negocio (`--spring.profiles.active=prod`) y por último `rabbit-admin-service`. Si se definen credenciales propias, los servicios deben arrancar con las mismas `RABBITMQ_USER` y `RABBITMQ_PASS`.

Para verificar el despliegue en el dashboard: **Overview** muestra los 2 nodos del clúster, **Queues** las 7 colas de negocio con sus 7 DLQ, y **Exchanges** los de tipo `topic` y `direct` (más los de dead letter).

---

## Autenticación con Azure AD

El sistema usa **Microsoft Entra External ID** (un tenant tipo CIAM, pensado para aplicaciones de cara a clientes). Se registraron dos aplicaciones dentro del mismo tenant:

- **`DittoStore-Backend-API`**: expone el scope `Store.Access` que el frontend solicita al hacer login. Requiere `requestedAccessTokenVersion: 2` en su manifiesto (sin esto, el token emitido usa un formato de audience distinto al que espera Spring Security).
- **`DittoStore-Frontend`**: aplicación tipo SPA, con los redirect URIs de cada entorno donde corra el frontend (local y el hosting final).

Como no hay registro propio de usuarios, **los usuarios se crean directamente en el tenant de Azure** (Microsoft Entra ID → Usuarios → Nuevo usuario). El rol de negocio (`CLIENTE`/`ADMIN`) se gestiona por separado en `usuarios-service`, y se asigna manualmente vía la API (`PUT /api/usuarios/{id}`) o directo en MySQL (ver [Conectar el usuario a rol Admin](#conectar-el-usuario-a-rol-admin-para-pruebas)) — Azure no tiene ningún concepto de "rol de tienda".

Al iniciar sesión por primera vez, el BFF sincroniza automáticamente el perfil del usuario en `usuarios-service` (JIT provisioning), usando los datos disponibles en el token.

> Importante: el **Issuer** real de este tipo de tenant usa el Tenant ID como subdominio (`https://<tenant-id>.ciamlogin.com/<tenant-id>/v2.0`) — no el dominio `.onmicrosoft.com` ni el dominio "vanity" del tenant.

### `protectedResourceMap` (MSAL, frontend)

`frontend/src/app/msal.config.ts` define a qué peticiones el interceptor de MSAL le agrega el header `Authorization: Bearer <token>`, usando patrones con wildcard (`*`). Ojo con la diferencia entre:

- `/algo/*` → exige algo **después** de la barra final; no matchea una llamada a la ruta base `/algo` sin subpath.
- `/algo*` → matchea tanto `/algo` como `/algo/lo-que-sea`.

Si agregas un endpoint nuevo que se llama a veces en su ruta base (ej. un `POST` de creación) y a veces con subpath (ej. `GET /algo/{id}`), usa el segundo patrón (`/algo*`) para cubrir ambos casos.

---

## Pruebas

- Cada microservicio incluye tests unitarios con **Mockito** sobre su capa de servicio.
- Los consumidores de RabbitMQ se prueban con `ManualAckHandlerTest` (pedidos, pago, reviews y carrito): ACK en caso exitoso, DLQ ante JSON inválido o error no recuperable, reintento ante error recuperable y DLQ al agotarse los reintentos.
- `rabbit-admin-service` tiene tests unitarios del servicio (`RabbitAdminServiceImplTest`) y del controlador con sus validaciones (`RabbitAdminControllerTest`).
- Las pruebas de integración de los endpoints se hicieron manualmente con **Postman**, incluyendo:
  - CRUD completo de cada microservicio.
  - Validación de JWT en el BFF (firma, issuer, audience, vigencia).
  - Caso de **token vencido**, confirmando que el sistema rechaza correctamente el acceso una vez expirado.
- El flujo de mensajería se verificó manualmente con el dashboard de RabbitMQ (colas, DLQ y exchanges) y los logs de cada servicio.

---

## Decisiones de diseño relevantes

- **MySQL con usuario `root`** en los 6 microservicios, en vez de un usuario dedicado por servicio — decisión de equipo para simplificar, ya que la pauta no exige aislamiento de credenciales por base de datos.
- **`usuarios-service` no valida JWT por sí mismo** — solo el BFF lo hace. Los microservicios internos confían en que únicamente el BFF puede alcanzarlos (patrón de "zona de confianza interna").
- **Los endpoints `GET` de productos son públicos** — no se exige autenticación para navegar el catálogo, solo para acciones que impliquen al usuario (carrito, pago, reviews).
- **3 instancias EC2** en vez de una sola, por restricciones de memoria de la cuenta de AWS Academy: infraestructura, microservicios de negocio y RabbitMQ.
- **Reintentos por reencolado en vez de requeue infinito**: un error recuperable reinserta el mensaje en su misma cola con un contador (`x-retry-count`) y, al agotar los intentos, lo envía a la DLQ. Así un mensaje defectuoso no bloquea la cola.
- **La URL del API Gateway se inyecta en build-time vía Netlify (`API_GATEWAY_URI` → `set-env.js` → `environment.prod.ts`)**, en vez de quedar hardcodeada en el código, para no depender de un commit cada vez que el lab estudiantil se reinicia y el API Gateway cambia de ID.

---

## Problemas conocidos y sus soluciones

| Problema | Causa | Solución |
|---|---|---|
| MySQL rechaza conexiones remotas entre instancias EC2 | Ubuntu configura `bind-address=127.0.0.1` por defecto | Cambiar a `0.0.0.0` en `mysqld.cnf` y otorgar el usuario con `@'%'` |
| Feign lanza `Invalid HTTP method: PATCH` | El cliente HTTP por defecto de Feign (`HttpURLConnection`) no soporta PATCH | Agregar la dependencia `feign-okhttp` y declarar un bean de tipo `feign.Client` respaldado por `feign.okhttp.OkHttpClient` |
| Componentes de Angular no reflejan datos tras un `.subscribe()` | El proyecto usa detección de cambios zoneless | Inyectar `ChangeDetectorRef` y llamar `detectChanges()`, o usar Signals |
| Frontend en Netlify: `net::ERR_NAME_NOT_RESOLVED` hacia el API Gateway | El lab estudiantil se cerró/reinició, el API Gateway se recreó con un ID nuevo y el frontend seguía apuntando al ID viejo | Actualizar la variable `API_GATEWAY_URI` en Netlify con el nuevo Invoke URL y redeployar (ver [Cuando el lab se reinicia](#cuando-el-lab-se-reinicia-y-el-api-gateway-cambia-de-id)) |
| `403 Forbidden` en rutas públicas (ej. `GET /api/productos`) aunque no tengan Authorizer en AWS | El `CorsFilter` del API Gateway **interno** de Spring (`CorsConfig.java`) rechaza con 403 cualquier request cuyo `Origin` no esté en su lista de orígenes permitidos — es una capa de CORS aparte de la de AWS | Agregar el dominio de producción (`https://dittostore.netlify.app`) a `allowedOrigins` en `CorsConfig.java`, recompilar y redesplegar el jar de `api-gateway` |
| Preflight de CORS falla en rutas protegidas (ej. `/bff/perfil`), mensaje "doesn't pass access control check: It does not have HTTP ok status" | El preflight `OPTIONS` no lleva header `Authorization`; si cae en la misma ruta que exige el JWT Authorizer (`ANY /{proxy+}`), el Authorizer lo rechaza con status no-2xx | Crear una ruta explícita `OPTIONS /{proxy+}` **sin** autorizador en el API Gateway de AWS |
| `POST` a una ruta protegida no lleva el token (`401 Unauthorized`), aunque otras peticiones a rutas "hermanas" sí funcionan | El patrón en `protectedResourceMap` (ej. `/api/reviews/*`) exige un segmento después de la barra final; una llamada a la ruta base sin subpath (ej. `POST /api/reviews`) no matchea, así que MSAL no le agrega el Bearer token | Cambiar el patrón a `/api/reviews*` (sin la barra antes del asterisco) para que cubra tanto la ruta base como las rutas con subpath |
| Los microservicios no conectan a RabbitMQ: `Connection refused` | El compose no está levantado en la instancia de RabbitMQ, o los nodos no están `healthy` | `docker compose ps` y `docker compose logs rabbit1` en esa instancia. Si el `nc` hacia el puerto se queda colgado en vez de rechazar, el problema es el grupo de seguridad (puertos 5672/5673) |
| `rabbit-admin-service` no arranca en la instancia | Falta la propiedad `issuer-uri`: el Config Server la lee de `config-repo/` en la rama `main` de GitHub | Mergear a `main`, o definir `spring.security.oauth2.resourceserver.jwt.issuer-uri` en su `application-prod.properties` |
| Las DLQ aparecen vacías en el dashboard aunque hubo errores | `CarritoDlqListener` y `ReviewsDlqListener` consumen y confirman los mensajes de la DLQ | Poner `dittostore.rabbitmq.dlq-listener.enabled=false` mientras se inspeccionan las DLQ |
| Los nodos de RabbitMQ no forman clúster | Cookie Erlang distinta entre nodos, o un volumen con una cookie antigua | Usar el mismo `RABBITMQ_ERLANG_COOKIE` y, si ya había volúmenes, `docker compose down -v` y volver a subir |

---