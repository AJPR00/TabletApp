# Módulo: presentation_common

## Descripción general

El módulo `presentation_common` contiene la lógica de presentación compartida entre las aplicaciones de tablet y mobile. Su objetivo es centralizar el comportamiento de la UI que es común a ambas plataformas, evitando duplicación de código.

Este módulo no define cómo se ve la interfaz, sino cómo se comporta.

---

## Propósito

Permitir que distintas plataformas (tablet y mobile) reutilicen la misma lógica de presentación, manteniendo implementaciones de UI independientes.

* Misma lógica
* Diferente interfaz visual

---

## Contenido del módulo

Este módulo incluye los siguientes componentes:

* ViewModels
  Encargados de gestionar el estado y coordinar la lógica de presentación.

* Estados de UI
  Representan la información necesaria para renderizar la pantalla.
  Ejemplos: LoginState, RegisterState.

* Eventos
  Acciones generadas por la UI (interacciones del usuario).
  Ejemplo: EstadoEvento.

* Lógica de presentación
  Incluye validaciones, transformación de datos y control del flujo de la UI.

* Manejo de loading y errores
  Controla estados de carga y expone errores de forma consumible por la UI.

* Interacción con la capa de dominio
  Realiza llamadas a los casos de uso definidos en el módulo core.

---

## Rol en la arquitectura

Este módulo actúa como una capa intermedia entre la UI y la lógica de negocio:

UI (mobile / tablet)
↓
presentation_common
↓
core (lógica de negocio, casos de uso)

---

## Responsabilidades

* Centralizar la lógica de presentación compartida
* Mantener consistencia de comportamiento entre plataformas
* Servir como puente entre la UI y la capa de dominio

---

## Qué NO incluye

Para mantener una arquitectura limpia, este módulo no debe contener:

* Componentes visuales (Views, XML, Composables, etc.)
* Código específico de plataforma
* Lógica de negocio compleja (pertenece a core)

---

## Beneficios

* Evita duplicación de código
* Facilita el mantenimiento
* Mejora la consistencia entre plataformas
* Permite testing aislado de la lógica de presentación

---

## Resumen

`presentation_common` define el comportamiento de la UI de forma reutilizable, desacoplada de la implementación visual y de la plataforma.


