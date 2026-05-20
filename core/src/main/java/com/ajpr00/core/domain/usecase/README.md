# UseCases
Cada caso de uso representa una única acción de negocio que la app puede realizar.
Define qué se hace, sin importar cómo se implementa.

## Contiene
- Casos de uso (UseCases)

- Lógica de negocio independiente de frameworks

- Reglas que definen qué hace la app, no cómo

## Misión
Encapsular acciones de negocio en unidades pequeñas, claras y testables.
Un UseCase expresa una intención completa, como:

- Iniciar sesión con Google

- Cerrar sesión

- Observar si hay sesión activa

- Obtener datos del usuario

- Guardar un documento

## No debe contener
- Android (Context, Activity, ViewModel, LiveData, Compose…)

- Firebase

- CredentialManager

- DataStore

- Retrofit

- Lógica de UI

- Lógica técnica o de infraestructura