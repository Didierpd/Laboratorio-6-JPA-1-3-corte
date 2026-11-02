Laboratorio 6 JPA-1 - 3 corte
Autor: Didier Perilla
Código base: https://github.com/gpedrazag/caso-JPA

EXPLICACIÓN SOLICITADA: @OneToMany Y LA TABLA PRODUCTO
La anotación @OneToMany(cascade=ALL, mappedBy="competitor") en Competitor permite asociar varios productos a un mismo concursante; mappedBy indica que el atributo competitor de Producto es el propietario de la relación. En conjunto con @ManyToOne y @JoinColumn en Producto, este mapeo hace que la tabla PRODUCTO contenga la columna COMPETITOR_ID como clave foránea hacia la clave primaria ID de COMPETITOR, por lo que varios registros de PRODUCTO pueden apuntar al mismo concursante, sin crear una tabla intermedia. Agregar el lado @OneToMany con mappedBy no genera otra clave foránea: reutiliza la relación definida en Producto. CascadeType.ALL propaga las operaciones de persistencia del concursante a sus productos; para mantener coherencia en memoria se actualizan ambos lados mediante addProduct.

REQUISITOS IMPLEMENTADOS
- Unidad CompetitorsPU, EclipseLink, Derby y generación Drop and Create.
- PersistenceManager singleton e inicialización desde Main.
- Entidades Competitor y Producto, ID automático, Serializable y fechas mediante callbacks.
- Consulta JPQL por apellido y registro mediante transacciones con rollback.
- Contraseña asociada al concursante, almacenada como hash PBKDF2 con sal aleatoria.
- Login por correo (campo address del código base) y password; devuelve el concursante sin contraseña. Ante credenciales inválidas lanza javax.ws.rs.NotAuthorizedException (HTTP 401).
- Relación bidireccional OneToMany/ManyToOne.

EJECUCIÓN
Requiere JDK 8 o superior y Maven. Abrir esta carpeta como proyecto Maven en NetBeans.
1. Ejecutar: mvn clean package
2. Iniciar Derby en una terminal desde la carpeta del proyecto:
   java -cp "target/dependency/*" org.apache.derby.drda.NetworkServerControl start -h localhost -p 1527 -noSecurityManager
3. En otra terminal, desde la misma carpeta, iniciar la aplicación:
   java -cp "target/classes:target/dependency/*" com.example.Main
4. Abrir http://localhost:8080 para registrar, consultar e iniciar sesión.
   Puede cambiarse el puerto web con la variable PORT.
5. Para detener, Ctrl+C. Para detener Derby:
   java -cp "target/dependency/*" org.apache.derby.drda.NetworkServerControl shutdown -h localhost -p 1527

Base: jdbc:derby://localhost:1527/CompetitorsDB;create=true
Usuario: app. Contraseña: app.
El servidor Derby debe iniciarse antes de Main. Si ya existe uno en 1527, utilizarlo.
ATENCIÓN: Drop and Create borra y recrea las tablas al iniciar la fábrica, como pide la guía. Los registros se conservan entre peticiones, pero se borran al reiniciar la aplicación.
Se puede conservar la información ejecutando Main con -Declipselink.ddl-generation=create-or-extend-tables antes de -cp.

PRUEBAS CON POSTMAN
Base: http://localhost:8080/rest/competitors
POST /add; Content-Type: application/json
{"name":"Laura","surname":"Gomez","address":"laura.gomez@gmail.com","password":"clave123","age":20,"telephone":"7659675","cellphone":"3002345436","city":"Bogota","country":"Colombia"}
Respuesta HTTP 200: {"competitor_id": ID generado}
GET /get (también GET en la ruta base): lista ordenada por apellido.
POST /login; Content-Type: application/json
{"address":"laura.gomez@gmail.com","password":"clave123"}
Respuesta HTTP 200: concursante autenticado. Con contraseña incorrecta o correo inexistente: HTTP 401.
Correo duplicado: HTTP 409. Registro incompleto: HTTP 400.

PRUEBAS AUTOMÁTICAS
mvn test
Prueba HTTP completa (requiere Python 3, proyecto compilado y puertos locales):
   python3 scripts/verificar_http.py
La prueba crea una base temporal, inicia Jetty y Derby en puertos libres y detiene ambos al terminar. Puede seleccionarse el ejecutable Java con LAB_JAVA.

Las pruebas usan Derby en memoria y no modifican CompetitorsDB. Verifican registro, login correcto, credenciales inválidas, correo duplicado, consulta ordenada, fechas, cascada, clave foránea y ausencia de contraseña en JSON.

COMPATIBILIDAD
Se conserva javax.persistence y el arranque Jetty del caso base. Se actualiza Jersey 1 a Jersey 2 porque NotAuthorizedException pertenece a JAX-RS 2. Las dependencias se resuelven con Maven sin instalar manualmente los JAR del repositorio antiguo. La versión derbyclient 10.10.2.0 coincide con la guía.
Referencia JAX-RS: https://jakarta.ee/specifications/restful-ws/2.1/apidocs/javax/ws/rs/notauthorizedexception

ENTREGA
login.zip contiene el proyecto y este readme.txt. La actividad de Moodle también solicita subir el proyecto a GitHub y entregar allí la URL del repositorio; el enlace del docente es únicamente el código base.
