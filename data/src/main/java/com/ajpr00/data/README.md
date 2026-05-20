# Data Layer

Esta capa se encarga de obtener, guardar y transformar datos desde cualquier fuente.

## Contiene
- Implementaciones de repositorios (`repository/`)
- DataSources locales (`datasource/local/`)
- DataSources remotos (`datasource/cloud/`)
- DTOs y mappers
- Integraciones reales: Firebase, CredentialManager, FTP, Drive…

## Misión
Convertir datos externos en modelos de dominio y entregarlos al repositorio.

## No debe contener
- ViewModels
- Estados de UI
- Composables
- Navegación
- Lógica de negocio (solo implementación técnica)

