# Módulo: tablet

## Descripción general

El módulo `tablet` contiene la implementación de la aplicación adaptada a dispositivos tipo tablet. Define la UI final aprovechando el mayor espacio disponible y patrones de interacción más ricos que en móvil.

Este módulo construye la experiencia específica de tablet reutilizando la lógica y componentes comunes.

---

## Propósito

Desarrollar una interfaz optimizada para pantallas grandes, permitiendo mayor densidad de información y flujos más complejos.

* Uso eficiente del espacio disponible
* Mayor número de acciones visibles
* Layouts más complejos y flexibles

---

## Contenido del módulo

Incluye los elementos específicos de la aplicación en tablet:

* Pantallas finales de tablet
  Implementación concreta de cada pantalla adaptada a tablet

* Navegación
  Definición de flujos y estructuras de navegación específicas

* Integración con `presentation_common`
  Uso de ViewModels y estados compartidos

* Uso de `ui_common`
  Construcción de la UI mediante componentes reutilizables

* Recursos específicos de tablet
  Assets, estilos, dimensiones y configuraciones propias

---

## Rol en la arquitectura

Este módulo forma parte de la capa de UI específica de plataforma:

tablet
↓
presentation_common
↓
core

También reutiliza componentes visuales de `ui_common`.

---

## Responsabilidades

* Implementar la UI final para tablet
* Aprovechar el espacio adicional para mejorar la experiencia
* Conectar la UI con la lógica de presentación
* Definir la navegación en tablet

---

## Qué NO incluye

Para mantener la arquitectura desacoplada, este módulo no debe contener:

* Lógica de negocio (pertenece a `core`)
* Lógica de presentación compleja (pertenece a `presentation_common`)
* Componentes reutilizables genéricos (pertenecen a `ui_common`)

---

## Beneficios

* Experiencia optimizada para pantallas grandes
* Mejor aprovechamiento del espacio y funcionalidades
* Separación clara entre plataformas
* Evolución independiente de la UI de móvil

---

## Resumen

`tablet` es la capa donde se construye la experiencia específica para dispositivos de pantalla grande, reutilizando lógica común pero adaptando la interfaz a un contexto más amplio y complejo.
