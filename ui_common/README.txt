# Módulo: ui_common

## Descripción general

El módulo `ui_common` agrupa todos los componentes visuales reutilizables de la aplicación. Funciona como una biblioteca de UI compartida entre las plataformas tablet y mobile.

Su responsabilidad es exclusivamente visual: define cómo se ve la interfaz, no cómo se comporta.

---

## Propósito

Evitar la duplicación de componentes visuales y mantener consistencia en el diseño de la aplicación.

Este módulo permite:

* Reutilizar UI entre distintas plataformas
* Mantener un diseño homogéneo
* Separar claramente la presentación visual de la lógica

---

## Contenido del módulo

Incluye elementos puramente visuales y reutilizables:

* Composables reutilizables
  Componentes independientes que pueden usarse en múltiples pantallas

* Componentes de UI
  Botones, cards, inputs, listas, etc.

* Layouts
  Estructuras base para organizar la UI

* Animaciones
  Transiciones y efectos visuales

* Pantallas base
  Estructuras genéricas reutilizables (scaffolds, containers, etc.)

* Recursos gráficos
  Drawables, iconos y otros assets visuales

---

## Rol en la arquitectura

Este módulo se ubica en la capa más externa, siendo consumido por las implementaciones de UI específicas:

UI (mobile / tablet)
↓
ui_common

Puede ser utilizado junto con `presentation_common`, pero no depende de él.

---

## Responsabilidades

* Proveer componentes visuales reutilizables
* Garantizar consistencia en el diseño
* Centralizar estilos y elementos gráficos

---

## Qué NO incluye

Para mantener una arquitectura limpia, este módulo no debe contener:

* ViewModels
* Lógica de negocio
* Navegación
* Acceso a contexto o dependencias de plataforma
* Código específico de tablet o mobile

---

## Beneficios

* Evita duplicación de UI
* Mejora la mantenibilidad del diseño
* Facilita cambios globales en la interfaz
* Refuerza la separación entre lógica y presentación visual

---

## Resumen

`ui_common` es la capa encargada de definir la apariencia de la aplicación mediante componentes reutilizables, sin incluir lógica ni dependencias de plataforma.
