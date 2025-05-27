# ProyectoISD

**Autores:** Juan Luis Ardila, Simón Díaz y Melissa Ruíz

## Autores

- [@Juan Luis Ardila](https://github.com/jardila20)
- [@Simon Diaz](https://github.com/SDM30)
- [@Melissa Ruiz](https://github.com/mfruiz1025)

## Descripción

Este proyecto implementa un sistema de gestión de recursos académicos utilizando el patrón *Load Balancing Broker* y *Request Reply Asíncrono*. Las funcionalidades principales incluyen:

##Primera entrega

1. **Facultades** envían solicitudes de recursos (aulas y laboratorios) al **Servidor Central**.
2. **Servidor Central concurrente** procesa las solicitudes, asigna recursos disponibles y responde a las facultades.
3. **Facultades** confirman o rechazan las asignaciones propuestas por el servidor.

##Segunda entrega

1. **Programas** envían solicitudes de recursos al servidor de **Facultad**
2. **Facultad** reciben las solcitudes y envian las solicitudes a **ServidorCentral**
3. **Healthcheck** proceso ejecutado aparte para revisar el fallo de **ServidorCentral**
4. **ServidorReplica** replica y actualiza el contenido del **ServidorCentral** en caso de fallo
5. **Cassandra** uso de para la persistencia 

---

## Cómo ejecutar el programa cliente (Facultad)

### Ejecutar desde IDE
1. Clonar el repositorio y abrir el proyecto en su IDE.
2. Ejecutar la clase `MainFacultad.java`.

### Ejecutar como JAR
1. Compilar el proyecto y ubicar el archivo JAR generado (ej: `Facultad-1.0-SNAPSHOT-jar-with-dependencies.jar`).
2. Ejecutar con los siguientes argumentos:
   ```bash
   java -jar Facultad.jar <nombre_facultad> <IP_servidor> <puerto> [semestre] [archivo_programas]
   ```
   **Ejemplos:**
   - Valores por defecto:
     ```bash
     java -jar Facultad.jar
     ```
   - Parámetros personalizados:
     ```bash
     java -jar Facultad.jar "Facultad de Ciencias" 192.168.1.100 5555 2 programas_custom.txt
     ```

**Argumentos:**
- `nombre_facultad`: Nombre identificador de la facultad.
- `IP_servidor`: Dirección IP del servidor central.
- `puerto`: Puerto de conexión al servidor.
- `semestre` (opcional, default=1): Semestre académico.
- `archivo_programas` (opcional): Ruta de archivo con programas académicos (formato: `Nombre,salones,laboratorios`).

---

## Cómo ejecutar el programa servidor central

### Ejecutar desde IDE
1. Ejecutar la clase `MainServidorCentral.java`.

### Ejecutar con configuración personalizada
1. Crear un archivo `configServidor.properties` con los siguientes parámetros:
   ```properties
   max.salones=380
   max.laboratorios=60
   server.ip=0.0.0.0
   server.port=5555
   inproc.address=backend
   ```
2. Ejecutar el servidor especificando la ruta del archivo:

El nombre del jar por defecto para el servidor  `ServidorCentral-1.0-SNAPSHOT-jar-with-dependencies.jar`, Para colocar una configuración
personalizada ingresa a `src/main/resources/configServidor.properties`
 
   ```bash
   java -jar ServidorCentral.jar
   ```

### Ejecutar con valores por defecto
```bash
java -jar ServidorCentral.jar
```
**Valores por defecto:**
- Máximo de salones: 380
- Máximo de laboratorios: 60
- IP: `0.0.0.0`
- Puerto: `5555`

---

## Cómo ejecutar el programa al ServidorRespaldo

### Ejecutar desde IDE
1. Clonar el repositorio y abrir el proyecto en su IDE.
2. Ejecutar la clase `MainServidorRespaldo.java`.

### Ejecutar como JAR
1. Compilar el proyecto y ubicar el archivo JAR generado (ej: `SevidorRespaldo-1.0-SNAPSHOT-jar-with-dependencies.jar`).
2. Ejecutar con los siguientes argumentos:
   ```bash
   java -jar ServidorRespaldo.jar <max_salones> <max_labs> <aulas_moviles> <ip_servidor> <puerto> <ipproc> <healthcheck_ip> <healthcheck_puerto> <cassandra_ip> <cassandra_puerto>
   ```
    **Ejemplos:**
   - Valores por defecto:
     ```bash
     java -jar ServidorRespaldo.jar
     ```
   - Parámetros personalizados:
     ```bash
     java -jar ServidorRespaldo.jar 380 60 10 0.0.0.0 5555 backend 0.0.0.0 5554 localhost 9042
     ```

**Argumentos:**
- `max_salones`: Número máximo de salones
- `max_labs`: Número máximo de laboratorios
- `aulas_moviles`: Número de aulas móviles
- `ip_servidor`: Ip del servidor central
- `puerto`: puerto del servidor central
- `ipproc`:
- `healthcheck_ip`: ip conexion healthcheck
- `healthcheck_puerto`: puerto conexion healthcheck
- `cassandra_ip`: ip cassandra
- `cassandra_puerto`: puerto conexion cassandra

---

## Cómo ejecutar el programa cliente (FacultadAsincrona)

### Ejecutar desde IDE
1. Clonar el repositorio y abrir el proyecto en su IDE.
2. Ejecutar la clase `MainFacultadAsincrona.java`.

### Ejecutar como JAR
1. Compilar el proyecto y ubicar el archivo JAR generado (ej: `FacultadAsincrona-1.0-SNAPSHOT-jar-with-dependencies.jar`).
2. Ejecutar con los siguientes argumentos:
   ```bash
   java -jar FacultadAsincrona.jar <nombre> <ip> <puerto> <ip_healtcheck> <puerto_healthcheack> [semestre] [archivo_programas]
   ```
    **Ejemplos:**
   - Valores por defecto:
     ```bash
     java -jar FacultadAsincrona.jar
     ```
   - Parámetros personalizados:
     ```bash
     java -jar FacultadAsincrona.jar "Facultad de Medicina" 127.0.0.1 5556 127.0.0.1 5553 2 misProgramas.txt
     ```

**Argumentos:**
- `nombre`: Nombre identificador de la facultad.
- `ip`: Dirección IP del servidor central.
- `puerto`: Puerto de conexión al servidor.
- `ip_healthcheck`: Ip del servidor healthchec
- `puerto_healthcheck`: puerto conexion healthcheck
- `semestre` (opcional, default=1): Semestre académico.
- `archivo_programas` (opcional): Ruta de archivo con programas académicos (formato: `Nombre,salones,laboratorios`).

---

## Cómo ejecutar el programa PatronHealthcheck

### Ejecutar desde IDE
1. Clonar el repositorio y abrir el proyecto en su IDE.
2. Ejecutar la clase `PatronHealthcheck.java`.

### Ejecutar como JAR
1. Compilar el proyecto y ubicar el archivo JAR generado (ej: `PatronHealthcheck-1.0-SNAPSHOT-jar-with-dependencies.jar`).
2. Ejecutar con los siguientes argumentos:
   ```bash
   java -jar PatronHealthcheck.jar <ip> <puerto> <backup_ip> <puerto_backup>
   ```
    **Ejemplos:**
   - Valores por defecto:
     ```bash
     java -jar PatronHealthcheck.jar
     ```
   - Parámetros personalizados:
     ```bash
     java -jar PatronHealthcheck.jar 0.0.0.0 5555 0.0.0.0 5553 5554
     ```

**Argumentos:**
- `ip`: Direccion ip servidor central.
- `puerto`: Puerto de conexión al servidor.
- `backup_ip`: Ip del servidor respaldo
- `puerto_backup`: Puerto publicador

---

## Cómo ejecutar el programa al ServidorAsincronoReplica

### Ejecutar desde IDE
1. Clonar el repositorio y abrir el proyecto en su IDE.
2. Ejecutar la clase `ServidorAsincronoReplica.java`.

### Ejecutar como JAR
1. Compilar el proyecto y ubicar el archivo JAR generado (ej: `ServidorAsincronoReplica-1.0-SNAPSHOT-jar-with-dependencies.jar`).
2. Ejecutar con los siguientes argumentos:
   ```bash
   java -jar ServidorAsincronoReplica.jar <max_salones> <max_labs> <aulas_moviles> <ip_servidor> <puerto> <ipproc> <healthcheck_ip> <healthcheck_puerto> <cassandra_ip> <cassandra_puerto>
   ```
    **Ejemplos:**
   - Valores por defecto:
     ```bash
     java -jar ServidorAsincronoReplica.jar
     ```
   - Parámetros personalizados:
     ```bash
     java -jar ServidorAsincronoReplica.jar 380 60 10 0.0.0.0 5555 backend 0.0.0.0 5554 localhost 9042
     ```

**Argumentos:**
- `max_salones`: Número máximo de salones
- `max_labs`: Número máximo de laboratorios
- `aulas_moviles`: Número de aulas móviles
- `ip_servidor`: Ip del servidor central
- `puerto`: puerto del servidor central
- `ipproc`:
- `healthcheck_ip`: ip conexion healthcheck
- `healthcheck_puerto`: puerto conexion healthcheck
- `cassandra_ip`: ip cassandra
- `cassandra_puerto`: puerto conexion cassandra

---


## Cómo ejecutar el programa al ServidorCentralAsincrono

### Ejecutar desde IDE
1. Clonar el repositorio y abrir el proyecto en su IDE.
2. Ejecutar la clase `ServidorAsincronoReplica.java`.

### Ejecutar como JAR
1. Compilar el proyecto y ubicar el archivo JAR generado (ej: `ServidorCentralAsincrono-1.0-SNAPSHOT-jar-with-dependencies.jar`).
2. Ejecutar con los siguientes argumentos:
   ```bash
   java -jar ServidorCentralAsincrono.jar <max_salones> <max_labs> <aulas_moviles> <ip_servidor> <puerto> <ipproc> <healthcheck_ip> <healthcheck_puerto> <cassandra_ip> <cassandra_puerto>
   ```
    **Ejemplos:**
   - Valores por defecto:
     ```bash
     java -jar ServidorCentralAsincrono.jar
     ```
   - Parámetros personalizados:
     ```bash
     java -jar ServidorCentralAsincrono.jar 380 60 10 0.0.0.0 5555 backend 0.0.0.0 5554 localhost 9042
     ```

**Argumentos:**
- `max_salones`: Número máximo de salones
- `max_labs`: Número máximo de laboratorios
- `aulas_moviles`: Número de aulas móviles
- `ip_servidor`: Ip del servidor central
- `puerto`: puerto del servidor central
- `ipproc`:
- `healthcheck_ip`: ip conexion healthcheck
- `healthcheck_puerto`: puerto conexion healthcheck
- `cassandra_ip`: ip cassandra
- `cassandra_puerto`: puerto conexion cassandra

---

## Requisitos
- **Java 17** o superior.
- **Conexión de red** entre cliente y servidor.
- **Servidor Central** debe estar en ejecución antes de iniciar los clientes. Se tiene para el patrón LoadBalancer y Request/Reply Asíncrono 
- Archivo `configCliente.properties` (opcional para cliente) con formato:
  ```properties
  server.ip=localhost
  server.port=5555
  server.healthcheck = localhost
  server.healthcheck.port = 5553
  ```
- Archivo `programasDefecto.txt` (para carga inicial de programas académicos en cliente).

---

## Notas adicionales
- El cliente cargará automáticamente una **solicitud de emergencia** si no se especifica un archivo de programas.
- Para depuración, revise los mensajes de consola en cliente y servidor
