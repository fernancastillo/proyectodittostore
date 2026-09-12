# 🐾 Ditto Store

E-commerce de cartas Pokémon TCG construido con arquitectura de microservicios, autenticación real vía Microsoft Entra External ID, y despliegue en AWS.

**Proyecto académico** — Curso DSY1107 (Desarrollo Cloud Native I).

---

## 📖 Descripción del proyecto

Ditto Store es una tienda en línea de cartas y cajas de Pokémon TCG. El sistema no cuenta con registro propio de usuarios: la autenticación se delega completamente a un tenant de **Microsoft Entra External ID**, y el rol de cada usuario (Cliente o Administrador) se gestiona en el backend propio.

La aplicación está compuesta por:

- Un **backend de microservicios** en Spring Boot, con descubrimiento de servicios, configuración centralizada y un Backend For Frontend (BFF) que valida los tokens JWT.
- Un **frontend en Angular** con dos experiencias diferenciadas por rol: la tienda para clientes y un panel de administración.
- Una capa de **API Manager en AWS** (HTTP API Gateway) que actúa como puerta de entrada pública al backend, con su propio Authorizer JWT.

---

## 🏗️ Arquitectura

```
                         ┌─────────────────────┐
                         │   Frontend Angular   │
                         │   (Netlify / local)  │
                         └──────────┬───────────┘
                                    │ HTTPS
                                    ▼
                         ┌─────────────────────┐
                         │  AWS API Gateway     │
                         │  (JWT Authorizer)    │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   API Gateway        │
                         │   interno (Spring)   │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │     BFF Service      │
                         │  (valida JWT, orquesta)│
                         └──────────┬───────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              ▼                     ▼                     ▼
      ┌───────────────┐   ┌────────────────┐    ┌────────────────┐
      │ producto-      │   │ usuarios-      │    │ carrito /      │
      │ service        │   │ service        │    │ pedidos / pago │
      │                │   │ (sync Azure AD)│    │ / reviews      │
      └───────────────┘   └────────────────┘    └────────────────┘
              │                     │                     │
              └─────────────────────┴─────────────────────┘
                                    │
                          Eureka Server / Config Server
                                    │
                                 MySQL
```

La autenticación corre en paralelo: el frontend usa **MSAL** para autenticar contra Azure AD, obtiene un token JWT, y ese mismo token viaja en cada request hasta el BFF, que lo valida (firma, issuer, audience y vigencia) antes de dejar pasar la petición.

---

## 🛠️ Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 17, Spring Boot 4.0.8, Spring Cloud (Eureka, Config Server, Gateway, OpenFeign) |
| Base de datos | MySQL 8 |
| Frontend | Angular (standalone components), MSAL v4 |
| Autenticación | Microsoft Entra External ID (Azure AD, tipo CIAM) |
| Nube | AWS (EC2, API Gateway) |
| Build | Maven (backend), Angular CLI (frontend) |

---

## 📁 Estructura del repositorio

```
DittoStore/
├── backend/
│   ├── pom.xml                      # POM padre (dependencias compartidas)
│   ├── infrastructure/
│   │   ├── pom.xml
│   │   ├── eureka-server/
│   │   ├── config-server/
│   │   ├── api-gateway/
│   │   └── bff-service/
│   ├── businessdomain/
│   │   ├── pom.xml
│   │   ├── producto-service/
│   │   ├── usuarios-service/
│   │   ├── carrito-service/
│   │   ├── pedidos-service/
│   │   ├── pago-service/
│   │   └── reviews-service/
│   └── config-repo/                 # Configuración centralizada (Config Server)
└── frontend/
    ├── src/app/
    │   ├── core/                    # Servicios, guards, modelos compartidos
    │   ├── features/
    │   │   └── admin/               # Panel de administración
    │   ├── shared/                  # Navbar, footer, componentes reutilizables
    │   └── pages/                   # Páginas públicas de la tienda
    └── environments/
```

---

## ⚙️ Requisitos previos

- Java 17
- Maven (o usar el wrapper `./mvnw` incluido)
- Node.js 18+ y npm
- MySQL (local, ej. vía Laragon/XAMPP, o accesible remotamente)
- Angular CLI (`npm install -g @angular/cli`)
- Una cuenta de Azure con un tenant de Microsoft Entra External ID configurado (ver sección de Autenticación)

---

## 🚀 Levantar el backend en local

1. **Configura MySQL**: crea las 6 bases de datos vacías (se autogeneran las tablas vía JPA):
   ```sql
   CREATE DATABASE dittostore_producto;
   CREATE DATABASE dittostore_usuarios;
   CREATE DATABASE dittostore_carrito;
   CREATE DATABASE dittostore_pedidos;
   CREATE DATABASE dittostore_pago;
   CREATE DATABASE dittostore_reviews;
   ```
   Por defecto, cada microservicio se conecta con el usuario `root` sin contraseña (ajusta en cada `application.properties` si tu entorno usa otras credenciales).

2. **Compila el backend completo**:
   ```bash
   cd backend
   ./mvnw clean package -DskipTests
   ```

3. **Levanta los servicios en este orden** (cada uno en su propia terminal, o usando Spring Boot Dashboard):
   ```bash
   # 1. Eureka Server (registro de servicios)
   java -jar infrastructure/eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar

   # 2. Config Server (configuración centralizada, vía config-repo/ en este mismo repo)
   java -jar infrastructure/config-server/target/config-server-0.0.1-SNAPSHOT.jar

   # 3. API Gateway y BFF
   java -jar infrastructure/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar
   java -jar infrastructure/bff-service/target/bff-service-0.0.1-SNAPSHOT.jar

   # 4. Los 6 microservicios de negocio (orden indistinto entre ellos)
   java -jar businessdomain/producto-service/target/producto-service-0.0.1-SNAPSHOT.jar
   java -jar businessdomain/usuarios-service/target/usuarios-service-0.0.1-SNAPSHOT.jar
   java -jar businessdomain/carrito-service/target/carrito-service-0.0.1-SNAPSHOT.jar
   java -jar businessdomain/pedidos-service/target/pedidos-service-0.0.1-SNAPSHOT.jar
   java -jar businessdomain/pago-service/target/pago-service-0.0.1-SNAPSHOT.jar
   java -jar businessdomain/reviews-service/target/reviews-service-0.0.1-SNAPSHOT.jar
   ```

4. **Verifica**: entra a `http://localhost:8761` — deberías ver los 10 servicios registrados como `UP`.

### Puertos de referencia

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

---

## 🖥️ Levantar el frontend en local

```bash
cd frontend
npm install --legacy-peer-deps
ng serve
```

Abre `http://localhost:4200`. Por defecto, el `environment.ts` apunta al backend local (`http://localhost:8081`).

> **Nota**: el proyecto usa detección de cambios *zoneless*. Si construyes un componente nuevo que actualiza datos dentro de un `.subscribe()` (HTTP, MSAL, etc.), la vista no se refresca sola — inyecta `ChangeDetectorRef` y llama a `detectChanges()` manualmente, o usa Angular Signals.

---

## ☁️ Despliegue en AWS

El backend está desplegado en **2 instancias EC2** (se dividió en dos por las limitaciones de memoria de una cuenta de AWS Academy — una sola instancia `t3.small` de 2 GB de RAM no soporta los 10 servicios de Java simultáneamente):

| Instancia | Contenido | IP elástica |
|---|---|---|
| `DittoStore` | MySQL, Eureka, Config Server, API Gateway, BFF, usuarios-service | *(ver configuración privada del equipo)* |
| `DittoStore-Negocio` | producto, carrito, pedidos, pago, reviews-service | *(ver configuración privada del equipo)* |

Los 5 microservicios de la segunda instancia usan un perfil de Spring separado (`application-prod.properties`) que apunta a la IP **privada** de la primera instancia — el `application.properties` base sigue intacto en `localhost` para que el desarrollo local no se vea afectado.

Para levantarlos en la instancia:
```bash
java -Xms64m -Xmx192m -jar <servicio>.jar --spring.profiles.active=prod
```

> Los servicios se levantan manualmente (no vía `systemd`) — hay que volver a ejecutarlos cada vez que la instancia se reinicia.

Sobre el **API Manager de AWS**: se creó una HTTP API con una ruta `ANY /{proxy+}` genérica que reenvía todo el tráfico al API Gateway interno, más rutas específicas por método HTTP (GET/POST/PUT/DELETE) para `producto-service`, protegidas con un **JWT Authorizer** configurado contra el tenant de Azure. Los endpoints `GET` de productos quedan públicos (sin exigir token), ya que no debería requerirse login solo para navegar el catálogo.

---

## 🔐 Autenticación con Azure AD

El sistema usa **Microsoft Entra External ID** (un tenant tipo CIAM, pensado para aplicaciones de cara a clientes). Se registraron dos aplicaciones dentro del mismo tenant:

- **`DittoStore-Backend-API`**: expone el scope `Store.Access` que el frontend solicita al hacer login. Requiere `requestedAccessTokenVersion: 2` en su manifiesto (sin esto, el token emitido usa un formato de audience distinto al que espera Spring Security).
- **`DittoStore-Frontend`**: aplicación tipo SPA, con los redirect URIs de cada entorno donde corra el frontend (local y el hosting final).

Como no hay registro propio de usuarios, **los usuarios se crean directamente en el tenant de Azure** (Microsoft Entra ID → Usuarios → Nuevo usuario). El rol de negocio (`CLIENTE`/`ADMIN`) se gestiona por separado en `usuarios-service`, y se asigna manualmente vía la API (`PUT /api/usuarios/{id}`) — Azure no tiene ningún concepto de "rol de tienda".

Al iniciar sesión por primera vez, el BFF sincroniza automáticamente el perfil del usuario en `usuarios-service` (JIT provisioning), usando los datos disponibles en el token.

> Importante: el **Issuer** real de este tipo de tenant usa el Tenant ID como subdominio (`https://<tenant-id>.ciamlogin.com/<tenant-id>/v2.0`) — no el dominio `.onmicrosoft.com` ni el dominio "vanity" del tenant.

---

## 🧪 Pruebas

- Cada microservicio incluye tests unitarios con **Mockito** sobre su capa de servicio.
- Las pruebas de integración de los endpoints se hicieron manualmente con **Postman**, incluyendo:
  - CRUD completo de cada microservicio.
  - Validación de JWT en el BFF (firma, issuer, audience, vigencia).
  - Caso de **token vencido**, confirmando que el sistema rechaza correctamente el acceso una vez expirado.

---

## 📌 Decisiones de diseño relevantes

- **MySQL con usuario `root`** en los 6 microservicios, en vez de un usuario dedicado por servicio — decisión de equipo para simplificar, ya que la pauta no exige aislamiento de credenciales por base de datos.
- **`usuarios-service` no valida JWT por sí mismo** — solo el BFF lo hace. Los microservicios internos confían en que únicamente el BFF puede alcanzarlos (patrón de "zona de confianza interna").
- **Los endpoints `GET` de productos son públicos** — no se exige autenticación para navegar el catálogo, solo para acciones que impliquen al usuario (carrito, pago, reviews).
- **2 instancias EC2** en vez de una sola, por restricciones de memoria de la cuenta de AWS Academy.

---

## 🐞 Problemas conocidos y sus soluciones

| Problema | Causa | Solución |
|---|---|---|
| MySQL rechaza conexiones remotas entre instancias EC2 | Ubuntu configura `bind-address=127.0.0.1` por defecto | Cambiar a `0.0.0.0` en `mysqld.cnf` y otorgar el usuario con `@'%'` |
| Feign lanza `Invalid HTTP method: PATCH` | El cliente HTTP por defecto de Feign (`HttpURLConnection`) no soporta PATCH | Agregar la dependencia `feign-okhttp` y declarar un bean de tipo `feign.Client` respaldado por `feign.okhttp.OkHttpClient` |
| Componentes de Angular no reflejan datos tras un `.subscribe()` | El proyecto usa detección de cambios zoneless | Inyectar `ChangeDetectorRef` y llamar `detectChanges()`, o usar Signals |

---

## 👥 Equipo

Proyecto desarrollado en equipo de 3 personas para el curso DSY1107 (Desarrollo Cloud Native I).