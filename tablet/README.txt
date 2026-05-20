# Módulo: mobile

## Descripción general

El módulo `mobile` contiene la implementación completa de la aplicación para dispositivos móviles. Aquí se define la UI final adaptada a pantallas pequeñas, utilizando los módulos compartidos para la lógica y componentes reutilizables.

Este módulo es responsable de ensamblar la experiencia de usuario específica de móvil.

---

## Propósito

Construir la versión móvil de la aplicación, adaptando la interfaz y la navegación a las limitaciones y patrones de uso propios de dispositivos móviles.

* Pantallas optimizadas para espacio reducido
* Interacciones simplificadas
* Layouts adaptados a móvil

---

## Contenido del módulo

Incluye todos los elementos necesarios para la app móvil:

* Pantallas finales de móvil
  Implementación concreta de cada pantalla adaptada a dispositivos móviles

* Navegación
  Definición de flujos y rutas entre pantallas

* Integración con `presentation_common`
  Uso de ViewModels y estados compartidos

* Uso de `ui_common`
  Composición de la UI mediante componentes reutilizables

* Recursos específicos de móvil
  Assets, dimensiones, estilos o configuraciones propias de esta plataforma

---

## Rol en la arquitectura

Este módulo se sitúa en la capa de UI específica de plataforma:

mobile
↓
presentation_common
↓
core

Además, reutiliza componentes visuales de `ui_common`.

---

## Responsabilidades

* Implementar la UI final para móvil
* Adaptar la experiencia a pantallas pequeñas
* Conectar la UI con la lógica de presentación
* Definir la navegación de la app móvil

---

## Qué NO incluye

Para mantener la separación de responsabilidades, este módulo no debe contener:

* Lógica de negocio (pertenece a `core`)
* Lógica de presentación compleja (pertenece a `presentation_common`)
* Componentes reutilizables genéricos (pertenecen a `ui_common`)

---

## Beneficios

* Permite personalizar la experiencia para móvil
* Mantiene desacoplada la lógica compartida
* Facilita evolución independiente de otras plataformas

---

## Resumen

`mobile` es la capa donde se construye la experiencia final para dispositivos móviles, reutilizando la lógica y componentes comunes, pero adaptando la UI a su contexto específico.
