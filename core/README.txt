# Módulo: core

## Descripción general

El módulo `core` representa el núcleo de la aplicación. Contiene toda la lógica que es completamente independiente de la plataforma y de la interfaz de usuario.

Es la capa donde reside la lógica de negocio y las reglas fundamentales del sistema.

---

## Propósito

Definir una base sólida, reutilizable y desacoplada sobre la que se apoyan el resto de módulos.

Este módulo está diseñado para ser:

* Independiente de Android y cualquier framework de UI
* Reutilizable en distintos entornos (mobile, web, desktop, etc.)
* Fácil de testear
* Estable frente a cambios en la interfaz

---

## Contenido del módulo

El módulo incluye los siguientes componentes:

* Modelos de dominio
  Representan las entidades principales del negocio.

* Casos de uso
  Encapsulan la lógica de negocio y definen las acciones que se pueden realizar en la aplicación.

* Repositorios

  * Interfaces: definen contratos de acceso a datos
  * Implementaciones: gestionan la obtención y persistencia de datos

* DataSources

  * Remotos (API / cloud)
  * Locales (base de datos, almacenamiento)

* Base de datos (Room)

  * DAO (Data Access Objects)
  * Entities

* DTOs (Data Transfer Objects)
  Modelos utilizados para comunicación externa (API, almacenamiento, etc.)

* Mappers
  Transforman datos entre DTOs, Entities y modelos de dominio

* Preferencias
  Gestión de configuración persistente simple

* Excepciones
  Definición de errores del dominio

* Validaciones
  Reglas de validación puras sin dependencias externas

* Utilidades
  Funciones auxiliares independientes de cualquier framework

---

## Rol en la arquitectura

Este módulo es la base sobre la que se construyen las demás capas:

UI / presentation
↓
core

El módulo `core`:

* No depende de ninguna otra capa superior
* Puede ser utilizado por múltiples plataformas
* Define las reglas del sistema

---

## Responsabilidades

* Centralizar la lógica de negocio
* Definir contratos de acceso a datos
* Asegurar consistencia en las reglas del dominio
* Proveer una base desacoplada y testeable

---

## Qué NO incluye

Para mantener la separación de responsabilidades, este módulo no debe contener:

* Código de UI (Views, ViewModels, etc.)
* Dependencias de Android o frameworks de presentación
* Lógica específica de plataforma

---

## Beneficios

* Alta reutilización del código
* Independencia tecnológica
* Facilidad de testing (unit tests puros)
* Mayor estabilidad ante cambios en UI o frameworks

---

## Resumen

`core` es la capa donde vive la lógica real de la aplicación. Todo lo demás depende de ella, pero ella no depende de nada externo.
