# TabletApp
Aplicación marco de fotos y videos destinado a Tablet.

# Arquitectura del Proyecto

Este módulo sigue una arquitectura limpia pensada para que el código sea fácil de mantener, escalar y entender.  
La idea es separar claramente **qué hace la app** de **cómo lo hace**.

## Capas principales

### 1. `ui/`
Todo lo que el usuario ve: pantallas, navegación, componentes y temas.

### 2. `presentation/`
Aquí viven los ViewModels y los estados de UI.  
Conectan la UI con el dominio, pero no contienen lógica de datos.

### 3. `domain/`
La capa más pura. Define modelos, reglas de negocio e interfaces de repositorios.  
No depende de Android ni de librerías externas.

### 4. `data/`
La capa que obtiene datos de verdad: Firebase, CredentialManager, APIs, Room, FTP, Drive…  
Implementa las interfaces definidas en `domain`.

### 5. `di/`
Módulos de Hilt. Conectan las capas entre sí.

### 6. `util/`
Funciones auxiliares puras y utilidades generales.

## Flujo general


