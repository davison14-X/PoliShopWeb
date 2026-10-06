# PoliShop — Contexto completo para continuar el desarrollo
# Estado: todos los templates listos, siguiente paso = backend Java

---

## 1. ¿Qué es PoliShop?

Plataforma web universitaria para visibilizar emprendimientos estudiantiles del
Politécnico Colombiano Jaime Isaza Cadavid (PCJIC), Medellín, Colombia.
Los estudiantes publican sus negocios (puestos de comida, tiendas virtuales,
servicios) y la comunidad del Poli los descubre y contacta.

---

## 2. Stack tecnológico

| Capa              | Tecnología                                      |
|-------------------|-------------------------------------------------|
| Backend           | Spring Boot 4.1.1 (Java)                        |
| Frontend          | Thymeleaf + Tailwind CSS CDN + JS vanilla       |
| Base de datos     | MySQL 8                                         |
| Autenticación     | Spring Security (sesiones, NO JWT)              |
| Verificación correo | JavaMailSender + SMTP Gmail                   |
| Imágenes          | Cloudinary (plan gratuito)                      |
| Despliegue        | Railway o Render                                |
| Build             | Maven                                           |
| Control versiones | Git + GitHub                                    |

Package base: `co.edu.pcjic.polishop`

---

## 3. Arquitectura

Monolito Spring Boot con arquitectura por capas:
**Controller → Service → Repository → Model**
combinada con MVC. Sin microservicios.

Rutas públicas: `/`, `/catalogo/**`, `/p/**`, `/auth/**`, `/css/**`, `/js/**`
Rutas privadas: `/panel/**` (requiere autenticación y cuenta verificada)

---

## 4. Estructura completa del proyecto

```
src/main/java/co/edu/pcjic/polishop/
├── config/
│   ├── SecurityConfig.java        ← PENDIENTE
│   ├── CloudinaryConfig.java      ← PENDIENTE
│   └── MailConfig.java            ← PENDIENTE
├── controller/
│   ├── CatalogoController.java    ← PENDIENTE
│   ├── EmprendimientoController.java ← PENDIENTE
│   ├── ProductoController.java    ← PENDIENTE
│   ├── AuthController.java        ← PENDIENTE
│   └── PanelController.java       ← PENDIENTE
├── service/
│   ├── EmprendimientoService.java ← PENDIENTE
│   ├── ProductoService.java       ← PENDIENTE
│   ├── UsuarioService.java        ← PENDIENTE
│   ├── VerificacionService.java   ← PENDIENTE
│   └── CloudinaryService.java     ← PENDIENTE
├── repository/
│   ├── UsuarioRepository.java     ← PENDIENTE
│   ├── EmprendimientoRepository.java ← PENDIENTE
│   ├── ProductoRepository.java    ← PENDIENTE
│   ├── TokenVerificacionRepository.java ← PENDIENTE
│   ├── HorarioRepository.java     ← PENDIENTE
│   └── MeGustaRepository.java     ← PENDIENTE
├── model/                         ← PENDIENTE (9 entidades JPA)
│   ├── Usuario.java
│   ├── Emprendimiento.java
│   ├── Producto.java
│   ├── TokenVerificacion.java
│   ├── Horario.java
│   ├── Categoria.java
│   ├── Contacto.java
│   ├── ImagenProducto.java
│   └── MeGusta.java
├── dto/                           ← PENDIENTE
│   ├── EmprendimientoDTO.java
│   ├── ProductoDTO.java
│   ├── RegistroForm.java
│   ├── LoginForm.java
│   └── EmprendimientoForm.java
├── util/                          ← PENDIENTE
│   ├── SlugUtil.java
│   └── HorarioUtil.java
└── PolishopApplication.java       ← YA EXISTE (generado por Spring Initializr)

src/main/resources/
├── templates/                     ← TODOS LISTOS ✅
│   ├── layout/base.html
│   ├── catalogo/index.html
│   ├── catalogo/detalle.html
│   ├── catalogo/producto.html
│   ├── auth/login.html
│   ├── auth/registro.html
│   ├── auth/verificar.html
│   ├── panel/index.html
│   ├── panel/editar.html
│   ├── panel/producto-form.html
│   └── error/404.html
├── static/
│   ├── css/styles.css             ← LISTO ✅
│   └── js/app.js                  ← LISTO ✅
└── application.properties         ← LISTO ✅
```

---

## 5. Base de datos — esquema MySQL (ya ejecutado)

```sql
-- Tablas existentes en MySQL local:
categoria          -- id, nombre, icono
usuario            -- id, correo_institucional (UK), nombre, apellido,
                   --   contrasena_hash, verificado, creado_en, actualizado_en
token_verificacion -- id, id_usuario FK, codigo, intentos, expira_en,
                   --   usado, creado_en
emprendimiento     -- id, id_usuario FK, id_categoria FK, nombre, slug (UK),
                   --   descripcion, logo_url, ubicacion, es_virtual, activo,
                   --   disponible_desde, disponible_hasta, creado_en, actualizado_en
horario            -- id, id_emprendimiento FK, dia_semana (1-7), hora_apertura,
                   --   hora_cierre, activo
                   --   UNIQUE(id_emprendimiento, dia_semana)
contacto           -- id, id_emprendimiento FK, tipo (WHATSAPP|INSTAGRAM|TELEFONO|EMAIL|OTRO),
                   --   valor, principal
producto           -- id, id_emprendimiento FK, nombre, slug (UK), descripcion,
                   --   precio DECIMAL(12,2), disponible, creado_en, actualizado_en
imagen_producto    -- id, id_producto FK, url, orden (0 = principal)
me_gusta           -- id, id_usuario FK, id_producto FK (nullable),
                   --   id_emprendimiento FK (nullable), creado_en
                   --   CHECK: exactamente uno de id_producto/id_emprendimiento no nulo
                   --   UNIQUE(id_usuario, id_producto), UNIQUE(id_usuario, id_emprendimiento)
```

Índices FULLTEXT en `producto(nombre, descripcion)` y `emprendimiento(nombre, descripcion)`.
Categorías ya insertadas: Comida y bebidas, Ropa y accesorios, Tecnología, Arte y manualidades,
Belleza y cuidado, Servicios, Papelería y útiles, Otros.

Hibernate ddl-auto = **validate** (no toca la BD, solo verifica).

---

## 6. application.properties (ya configurado, faltan credenciales reales)

```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/polishop?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD_AQUI
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.open-in-view=false
spring.thymeleaf.cache=false
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=TU_CORREO@gmail.com
spring.mail.password=TU_PASSWORD_DE_APP_GMAIL
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
cloudinary.cloud-name=TU_CLOUD_NAME
cloudinary.api-key=TU_API_KEY
cloudinary.api-secret=TU_API_SECRET
cloudinary.carpeta.logos=polishop/logos
cloudinary.carpeta.productos=polishop/productos
spring.servlet.multipart.max-file-size=5MB
server.servlet.session.timeout=30m
polishop.mail.remitente=PoliShop PCJIC <TU_CORREO@gmail.com>
polishop.verificacion.expiracion-minutos=15
polishop.verificacion.max-intentos=3
```

---

## 7. Templates Thymeleaf — variables que cada uno espera del controlador

### `catalogo/index.html` ← CatalogoController GET /
```java
model.addAttribute("emprendimientos", List<EmprendimientoDTO>);
model.addAttribute("categorias", List<Categoria>);
model.addAttribute("totalResultados", int);
model.addAttribute("busqueda", String);          // parámetro ?q=
model.addAttribute("categoriaSeleccionada", Long); // parámetro ?categoria=
model.addAttribute("ubicacionSeleccionada", String); // parámetro ?ubicacion=
model.addAttribute("paginaActual", "catalogo");
```

### `catalogo/detalle.html` ← EmprendimientoController GET /catalogo/{slug}
```java
model.addAttribute("emp", EmprendimientoDTO);
model.addAttribute("productos", List<ProductoDTO>);
model.addAttribute("horarios", List<HorarioDTO>);  // 7 días con: nombre, activo, horaApertura, horaCierre, esHoy
model.addAttribute("contactoWa", String);  // número WhatsApp o null
model.addAttribute("contactoIg", String);  // usuario Instagram o null
model.addAttribute("meGusta", boolean);    // si el usuario logueado ya dio like
model.addAttribute("paginaActual", "catalogo");
```

### `catalogo/producto.html` ← ProductoController GET /p/{slug}
```java
model.addAttribute("prod", ProductoDTO);   // incluye: emprendimientoNombre, emprendimientoSlug, logoEmprendimiento, imagenPrincipal, imagenesAdicionales, totalMeGusta
model.addAttribute("contactoWa", String);
model.addAttribute("meGusta", boolean);
model.addAttribute("urlCanonica", String); // "https://dominio/p/{slug}"
model.addAttribute("paginaActual", "catalogo");
```

### `auth/login.html` ← AuthController GET /auth/login
```java
model.addAttribute("error", String);  // opcional, si viene de ?error
```

### `auth/registro.html` ← AuthController GET /auth/registro
```java
model.addAttribute("registroForm", new RegistroForm());
model.addAttribute("errores", List<String>);  // opcional
```

### `auth/verificar.html` ← AuthController GET /auth/verificar
```java
model.addAttribute("correo", String);   // correo al que se envió el código
model.addAttribute("error", String);    // opcional
```

### `panel/index.html` ← PanelController GET /panel
```java
model.addAttribute("emp", EmprendimientoDTO);
model.addAttribute("usuario", UsuarioDTO);
model.addAttribute("productos", List<ProductoDTO>);
model.addAttribute("horarios", List<HorarioDTO>);
model.addAttribute("categorias", List<Categoria>);
model.addAttribute("contactoWa", String);
model.addAttribute("contactoIg", String);
model.addAttribute("tab", "perfil");  // o "productos" según ?tab=
model.addAttribute("paginaActual", "panel");
model.addAttribute("exito", String);  // opcional, mensaje de éxito
model.addAttribute("error", String);  // opcional
```

### `panel/editar.html` ← PanelController GET /panel/editar
```java
model.addAttribute("emprendimientoForm", EmprendimientoForm);  // relleno con datos actuales
model.addAttribute("categorias", List<Categoria>);
model.addAttribute("horarios", List<HorarioDTO>);
model.addAttribute("errores", List<String>);  // opcional
model.addAttribute("paginaActual", "panel");
```

### `panel/producto-form.html` ← PanelController GET /panel/producto/nuevo y GET /panel/producto/{id}/editar
```java
model.addAttribute("productoForm", ProductoForm);
model.addAttribute("modoEdicion", boolean);
model.addAttribute("errores", List<String>);  // opcional
model.addAttribute("paginaActual", "panel");
```

---

## 8. Flujo de registro y verificación de correo

1. Usuario llena `registro.html` con correo `@elpoli.edu.co`
2. `AuthController` valida que el correo termine en `@elpoli.edu.co`; si no → error
3. Se crea `Usuario` con `verificado = false` y contraseña hasheada con BCrypt
4. `VerificacionService` genera código de 6 dígitos aleatorio
5. Se guarda en `token_verificacion`: `codigo`, `expira_en = now + 15min`, `intentos = 0`, `usado = false`
6. `JavaMailSender` envía el código al correo del usuario
7. Redirige a `/auth/verificar` con el correo en sesión
8. Usuario digita el código en `verificar.html`
9. `AuthController POST /auth/verificar` valida:
   - Token no usado + no expirado + intentos < 3
   - Código coincide
10. Si válido: `usuario.verificado = true`, `token.usado = true`, login automático, redirige a `/panel`
11. Si inválido: `token.intentos++`; si llega a 3 → token invalidado, mensaje de error

---

## 9. Lógica "Abierto ahora" (HorarioUtil)

Un emprendimiento está abierto si:
- `disponible_hasta` no es null Y `disponible_hasta > now()` → **Flash activo** (prioridad)
- O bien: el día de hoy tiene una fila en `horario` con `activo = true`
  Y `hora_apertura <= hora_actual <= hora_cierre`

El resultado es el boolean `emp.abierto` que usan los templates.

---

## 10. Endpoint de Me Gusta (API mínima)

```
POST /api/me-gusta/{tipo}/{id}
tipo: "producto" | "emprendimiento"
Requiere autenticación (401 si no está logueado)
Responde JSON: { "likes": int, "liked": boolean }
```

El `app.js` ya lo consume. Si responde 401, abre el modal de login.

---

## 11. SlugUtil

Genera slugs únicos a partir del nombre:
- "Empanadas Poli 3x2" → "empanadas-poli-3x2"
- Si ya existe en BD → "empanadas-poli-3x2-2", "empanadas-poli-3x2-3", etc.
- Caracteres especiales y tildes se normalizan (Normalizer.NFD + regex)

---

## 12. Cloudinary — flujo de subida de imágenes

1. Usuario selecciona imagen en el form (MultipartFile)
2. `CloudinaryService.subirImagen(file, carpeta)` sube a Cloudinary
3. Cloudinary devuelve URL pública
4. Se guarda la URL en `emprendimiento.logo_url` o `imagen_producto.url`
5. Nunca se guarda el archivo en el servidor

Carpetas: `polishop/logos` y `polishop/productos`

---

## 13. Spring Security — configuración esperada

```java
// Rutas públicas (sin autenticación):
"/", "/catalogo/**", "/p/**",
"/auth/login", "/auth/registro", "/auth/verificar", "/auth/reenviar-codigo",
"/css/**", "/js/**", "/img/**", "/favicon.ico"

// Rutas privadas:
"/panel/**", "/api/**"

// Login page: /auth/login
// Logout: POST /auth/logout → redirige a /
// Usuario cargado por: UsuarioService implements UserDetailsService
// Contraseñas: BCryptPasswordEncoder
// Sesiones: por defecto Spring Security (no JWT)
```

---

## 14. Lo que sigue — orden de desarrollo

### Paso 1: Entidades JPA (model/)
Orden sugerido (menor a mayor dependencia):
`Categoria` → `Usuario` → `TokenVerificacion` → `Emprendimiento`
→ `Horario` → `Contacto` → `Producto` → `ImagenProducto` → `MeGusta`

Anotaciones clave: `@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy=IDENTITY)`,
`@Column`, `@ManyToOne`, `@OneToMany(mappedBy, cascade, fetch)`,
`@JoinColumn`, `@CreationTimestamp`, `@UpdateTimestamp`

### Paso 2: Repositorios (repository/)
Interfaces que extienden `JpaRepository<Entidad, Long>`.
Consultas personalizadas necesarias:
- `findBySlug(String slug)` en Emprendimiento y Producto
- `findByCorreoInstitucional(String correo)` en Usuario
- `findByIdUsuarioAndUsadoFalseAndExpiresAfter(Long id, LocalDateTime now)` en TokenVerificacion
- Búsqueda FULLTEXT en Emprendimiento (con `@Query` nativa)
- `existsByIdUsuarioAndIdProducto(Long, Long)` en MeGusta

### Paso 3: DTOs
- `EmprendimientoDTO` — datos para las vistas (incluye `abierto`, `totalMeGusta`, `flashActivo`)
- `ProductoDTO` — incluye `imagenPrincipal`, `imagenesAdicionales`, `totalMeGusta`, datos del emprendimiento
- `HorarioDTO` — incluye `nombre` (Lunes…Domingo), `diaSemana`, `activo`, `horaApertura`, `horaCierre`, `esHoy`
- `RegistroForm` — nombre, apellido, correoInstitucional, contrasena, confirmarContrasena
- `EmprendimientoForm` — nombre, categoriaId, descripcion, esVirtual, ubicacion, whatsapp, instagram, logoActualUrl
- `ProductoForm` — id (nullable), nombre, descripcion, precio, disponible, imagenActualUrl

### Paso 4: Utilidades (util/)
- `SlugUtil.java` — normaliza y genera slugs únicos consultando el repositorio
- `HorarioUtil.java` — calcula si un emprendimiento está abierto ahora

### Paso 5: Servicios (service/)
- `UsuarioService` — registro, `loadUserByUsername` (para Spring Security)
- `VerificacionService` — generar código, enviar correo, validar código, reenviar
- `EmprendimientoService` — CRUD, búsqueda + filtros fulltext, calcular `abierto`
- `ProductoService` — CRUD, generar slug, obtener imágenes
- `CloudinaryService` — `subirImagen(MultipartFile, carpeta)`, `eliminarImagen(publicId)`

### Paso 6: Configuración (config/)
- `SecurityConfig` — rutas públicas/privadas, login, logout, BCrypt
- `CloudinaryConfig` — bean de Cloudinary con credenciales de application.properties
- `MailConfig` — bean de JavaMailSender (ya lo autoconfigura Spring Boot con las properties)

### Paso 7: Controladores (controller/)
- `CatalogoController` — `GET /` con parámetros `q`, `categoria`, `ubicacion`
- `EmprendimientoController` — `GET /catalogo/{slug}`
- `ProductoController` — `GET /p/{slug}` con meta OG en el modelo
- `AuthController` — `GET/POST /auth/registro`, `login`, `verificar`, `reenviar-codigo`
- `PanelController` — `GET /panel`, `POST /panel/perfil`, `POST /panel/flash`,
  `GET/POST /panel/producto/nuevo`, `GET/POST /panel/producto/{id}/editar`,
  `POST /panel/producto/{id}/eliminar`

### Paso 8: Endpoint Me Gusta
`POST /api/me-gusta/{tipo}/{id}` en un `MeGustaController` o en `PanelController`.

---

## 15. Notas importantes

- **Correo válido:** solo `@elpoli.edu.co`. Rechazar en registro si no cumple.
- **Imágenes:** nunca se guardan en el servidor. Solo URLs de Cloudinary en BD.
- **Slug único:** se verifica contra BD antes de guardar. Sufijo numérico si ya existe.
- **Flash:** `disponible_hasta > now()` tiene prioridad sobre el horario semanal.
- **ddl-auto=validate:** Hibernate NO crea ni modifica tablas. El esquema está creado
  manualmente con el script SQL ya ejecutado.
- **Spring Security usa sesiones.** El usuario autenticado disponible en controladores
  vía `@AuthenticationPrincipal UserDetails userDetails`.
- **CSRF:** todos los forms POST ya tienen `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>`.
- **Me gusta:** la tabla tiene CHECK que garantiza que exactamente uno de
  `id_producto` / `id_emprendimiento` sea no nulo.
- **dia_semana en horario:** 1 = lunes … 7 = domingo (ISO 8601).
  Mapear con `DayOfWeek.getValue()` en Java.
