# Sabores en Línea
Tu compañero Culinario Digital

## Trabajo de Fin de Grado - Grado Superior DAM
**Autora**: María Aránzazu Esquide López


## Descripción del Proyecto

Sabores en Línea es una aplicación de escritorio de recetas desarrollada en Java con interfaz gráfica Swing. Permite al usuario crear, buscar, valorar y compartir recetas culinarias, así como gestionar categorías, usuarios y generar reportes estadísticos.El objetivo principal es proporcionar una plataforma intuitiva y completa que me permita guardar recetas de cocina de una manera fácil y atractiva, con la posibilidad de compartirla a usuarios descubrir.

## Características Implementadas

- **Sistema de Usuarios**: Registro, autenticación y gestión de usuarios con roles (administrador/usuario regular)
- **Gestión de Recetas**: Creación, edición, búsqueda y visualización de recetas
- **Categorización**: Organización de recetas por categorías personalizables
- **Multimedia**: Soporte para fotos y videos (archivos locales y URLs)
- **Interfaz Gráfica**: Diseño intuitivo con navegación entre pantallas

## Arquitectura

El proyecto sigue una arquitectura de tres capas con patrón MVC (Modelo-Vista-Controlador):

- **Capa de Presentación (Vista)**: Interfaces gráficas desarrolladas con Swing
- **Capa de Lógica de Negocio (Controlador)**: Clases que gestionan la lógica de la aplicación
- **Capa de Datos (Modelo)**: Clases de entidad y acceso a datos

## Tecnologías Utilizadas

- **Java 23**: Lenguaje de programación principal con características preview
- **Swing**: Biblioteca para la interfaz gráfica de usuario
- **Hibernate 6.5**: Framework de persistencia para el mapeo objeto-relacional
- **MySQL 8.2**: Sistema de gestión de base de datos
- **BIRT 4.4.2**: Business Intelligence and Reporting Tools para la generación de informes
- **iText 5.5**: Biblioteca para generación de documentos PDF
- **Maven**: Gestión de dependencias y construcción del proyecto

## Requisitos del Sistema

- Java 23 o superior
- MySQL 8.2 o superior
- Mínimo 4GB de RAM
- 100MB de espacio en disco (sin contar la base de datos)

## Instalación

1. Clone el repositorio o descargue el código fuente
2. Configure la conexión a la base de datos en `hibernate.cfg.xml`
3. Compile el proyecto con Maven: `mvn clean package`
4. Ejecute la aplicación: `java -jar target/sabores-en-linea-0.0.1-SNAPSHOT.jar`

## Configuración de la Base de Datos

```sql
CREATE DATABASE sabores_en_linea;
USE sabores_en_linea;

-- Las tablas se crearán automáticamente mediante Hibernate