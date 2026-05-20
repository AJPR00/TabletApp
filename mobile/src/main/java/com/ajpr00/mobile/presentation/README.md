# Presentation Layer

Capa que conecta la UI con el dominio.  
Aquí viven los ViewModels y los estados de pantalla.

## Contiene
- ViewModels
- StateFlow / MutableStateFlow
- Clases de estado (state/)

## Misión
- Recibir eventos de la UI
- Pedir datos al repositorio
- Transformar datos en estados de UI

## No debe contener
- Firebase
- CredentialManager
- DataStore
- Lógica de autenticación
- Acceso directo a APIs o bases de datos

