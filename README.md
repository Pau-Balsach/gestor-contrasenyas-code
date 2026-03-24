 (cd "$(git rev-parse --show-toplevel)" && git apply --3way <<'EOF' 
diff --git a/README.md b/README.md
index 5a8cfe2af96a5d45577b6a7e9c6431c3f30e55c9..7bdae9af39be7377238f656fdd84bb2c7d59aa94 100644
--- a/README.md
+++ b/README.md
@@ -11,33 +11,40 @@ Aplicación de escritorio para gestionar de forma segura las credenciales de tus
 - Autenticación de usuarios con Supabase Auth
 - Base de datos en la nube con Row Level Security — cada usuario solo ve sus propios datos
 - Recuperación de contraseña por email
 
 ## 🚀 Descarga e Instalación
 
 La forma más sencilla de usar TrackLectura en Windows es descargando el instalador oficial desde nuestra sección de lanzamientos:
 
 👉 **[Descargar Gestror-Contraseñas v1.2(.exe)](https://github.com/Pau-Balsach/gestor-contrasenas-code/releases/latest/download/GestorContrasenyas-1.2-Setup.exe)**
 
 ## Requisitos
 
 - Java 25 o superior
 
 ## Tecnologías utilizadas
 
 - Java 25
 - Maven
 - Supabase (Auth + PostgreSQL + REST API)
 - AES-256 + PBKDF2 para encriptación
 - Riot Games API (League of Legends)
 - Henrik Dev API (Valorant)
 
 ## Seguridad
 
-- Las contraseñas se encriptan con AES-256 antes de guardarse en la base de datos
-- La clave de encriptación se deriva de tu contraseña maestra con PBKDF2 y nunca se almacena
-- Row Level Security activado en Supabase — ningún usuario puede acceder a los datos de otro
-- La contraseña maestra es independiente de la contraseña de login
+- Las contraseñas se cifran en cliente con **AES-GCM (v2)**, incluyendo integridad autenticada.
+- Compatibilidad con datos legacy **AES-CBC (v1 / sin prefijo)** para lectura durante migración.
+- La clave de cifrado se deriva de tu contraseña maestra con PBKDF2 y nunca se almacena.
+- Row Level Security activado en Supabase — ningún usuario puede acceder a los datos de otro.
+- La contraseña maestra es independiente de la contraseña de login.
+- Protección básica anti brute-force en cliente (backoff y bloqueo temporal).
+
+### Verificaciones de seguridad incluidas en el repo
+
+- Script de prueba de acceso cruzado RLS: `security/rls_cross_access_test.sql`
+- Workflow SCA (OWASP Dependency-Check): `.github/workflows/security-sca.yml`
 
 ## Licencia
 
 MIT License — ver archivo [LICENSE](LICENSE)
 
EOF
)
