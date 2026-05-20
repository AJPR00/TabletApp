# Dependency Injection (DI)

Aquí viven los módulos de Hilt que conectan todas las capas.

## Contiene
- Módulos @Module y @InstallIn
- Bindings entre interfaces y repositorios
- Proveedores de Firebase, Retrofit, CredentialManager, Room…

## Misión
Centralizar la creación de dependencias y evitar instanciaciones manuales.

## No debe contener
- Lógica de negocio
- Lógica de UI
- Repositorios completos
- Modelos

