# Ciclo de Vida del Desarrollo de Software (SDLC)

---

## ¿Qué es el SDLC?

Es el proceso estructurado que sigue un equipo para planificar, construir, probar y entregar software de forma ordenada y repetible.

```
Planificación → Análisis → Diseño → Desarrollo → Pruebas → Despliegue → Mantenimiento
```

Cada fase tiene una entrada, un proceso y una salida. La salida de una fase es la entrada de la siguiente.

---

## Las 7 fases

---

### 1. Planificación
**¿Vale la pena construirlo?**

Se evalúa la viabilidad del proyecto antes de invertir tiempo y dinero.

- Se define el alcance (qué entra y qué no)
- Se estiman tiempos, costos y recursos
- Se identifican riesgos técnicos y de negocio

**Resultado:** documento de viabilidad, cronograma, asignación de roles

---

### 2. Análisis de Requisitos
**¿Qué debe hacer el sistema exactamente?**

Se recopilan y documentan las necesidades del cliente y los usuarios finales.

| Tipo de requisito | Qué define | Ejemplo |
|---|---|---|
| **Funcional** | qué hace el sistema | registrar un usuario, buscar productos, generar un reporte |
| **No funcional** | cómo lo hace | responder en menos de 2s, soportar 1000 usuarios simultáneos, cifrar contraseñas |

**Técnicas de recopilación:** entrevistas, encuestas, análisis de sistemas existentes, casos de uso.

**Resultado:** documento de requisitos, historias de usuario, casos de uso

---

### 3. Diseño
**¿Cómo se va a construir?**

Se traduce el "qué" (requisitos) en el "cómo" (arquitectura y estructura técnica).

**Niveles de diseño:**

| Nivel | Qué define |
|---|---|
| **Arquitectura** | estructura general del sistema (capas, microservicios, monolito) |
| **Base de datos** | entidades, relaciones, tipos de datos (modelo ER) |
| **API** | endpoints, contratos de request/response, códigos HTTP |
| **Seguridad** | autenticación, autorización, manejo de sesiones |
| **Infraestructura** | servidores, contenedores, servicios cloud |

**Resultado:** diagrama de arquitectura, modelo ER, especificación de API, decisiones técnicas documentadas

---

### 4. Desarrollo
**Se escribe el código.**

El equipo implementa el sistema siguiendo el diseño. Se aplican buenas prácticas:

- **Separación de responsabilidades** — cada clase/módulo tiene una sola razón para cambiar
- **Control de versiones** — Git con ramas por feature (`feature/`) y correcciones (`fix/`)
- **Code review** — otro desarrollador revisa el código antes de integrarlo
- **Principios SOLID** — código mantenible y extensible

**Resultado:** código fuente versionado en el repositorio

---

### 5. Pruebas (Testing)
**¿El sistema funciona correctamente?**

Se verifica que el software cumple los requisitos antes de llegar a producción.

| Tipo | Qué verifica | Alcance |
|---|---|---|
| **Unitarias** | un método o función de forma aislada | muy pequeño |
| **Integración** | la interacción entre componentes (ej: servicio + BD) | medio |
| **End-to-End (E2E)** | el flujo completo desde la interfaz hasta la BD | grande |
| **Regresión** | que los cambios nuevos no rompieron lo que ya funcionaba | variable |
| **Carga / Estrés** | comportamiento bajo alto volumen de peticiones | sistema completo |

> **Regla fundamental:** si un test falla localmente, fallará en producción. Nunca desplegar con tests rotos.

**Resultado:** reporte de cobertura, bugs documentados y corregidos

---

### 6. Despliegue (Deployment)
**¿Cómo llega el software al usuario final?**

El proceso de llevar el código del repositorio a un servidor donde los usuarios pueden accederlo.

**Entornos típicos:**

| Entorno | Propósito |
|---|---|
| **Desarrollo (local)** | el desarrollador trabaja y prueba en su máquina |
| **Staging / QA** | réplica de producción para pruebas finales antes del lanzamiento |
| **Producción** | el sistema real que usan los usuarios finales |

**Estrategias de despliegue:**

| Estrategia | Cómo funciona | Ventaja |
|---|---|---|
| **Big Bang** | se reemplaza todo de una vez | simple |
| **Blue/Green** | dos entornos idénticos, se alterna entre ellos | rollback inmediato |
| **Canary** | el cambio llega al 5% de usuarios primero, luego al 100% | detecta errores con impacto mínimo |
| **Rolling** | se actualiza instancia por instancia sin downtime | sin interrupciones |

**Herramientas modernas de despliegue:**

| Herramienta | Para qué |
|---|---|
| **Docker** | empaqueta la app en un contenedor portable — corre igual en cualquier máquina |
| **Docker Compose** | orquesta múltiples contenedores (app + base de datos) localmente |
| **CI/CD** | automatiza build, tests y deploy en cada push (GitHub Actions, Jenkins) |
| **PaaS (Render, Railway, Heroku)** | plataforma que gestiona servidores, redes y deploys automáticamente |
| **IaC (render.yaml, terraform)** | define la infraestructura como código — versionable y reproducible |

**Resultado:** sistema accesible en producción con URL pública

---

### 7. Mantenimiento
**El sistema está vivo — hay que sostenerlo.**

Después del lanzamiento, el sistema necesita atención continua.

| Tipo de mantenimiento | Descripción |
|---|---|
| **Correctivo** | corregir bugs encontrados en producción |
| **Adaptativo** | ajustar el sistema a cambios en el entorno (nuevo SO, nueva versión de BD) |
| **Perfectivo** | mejorar rendimiento, usabilidad o agregar nuevas funcionalidades |
| **Preventivo** | refactorizar código para reducir deuda técnica antes de que cause problemas |

**Monitoreo en producción:**
- **Health checks** — endpoint `/actuator/health` verificado cada N segundos
- **Logs** — registro de errores y eventos importantes
- **Alertas** — notificaciones automáticas cuando algo falla
- **Métricas** — CPU, memoria, tiempo de respuesta, tasa de errores

---

## Modelos de proceso del SDLC

No todos los proyectos siguen el SDLC de la misma forma:

| Modelo | Enfoque | Cuándo usarlo |
|---|---|---|
| **Cascada (Waterfall)** | fases secuenciales, no se vuelve atrás | requisitos muy estables y bien definidos |
| **Iterativo** | ciclos cortos que refinan el producto | requisitos que evolucionan |
| **Ágil (Scrum, Kanban)** | entregas frecuentes, feedback continuo | proyectos con cambios constantes |
| **DevOps** | desarrollo + operaciones integrados, deploy continuo | equipos que despliegan varias veces al día |

---

## Resumen

```
PLANIFICACIÓN  → ¿vale la pena? ¿qué alcance? ¿qué riesgos?
ANÁLISIS       → ¿qué hace el sistema? requisitos funcionales y no funcionales
DISEÑO         → ¿cómo se construye? arquitectura, BD, API, seguridad
DESARROLLO     → código, control de versiones, buenas prácticas
PRUEBAS        → unitarias, integración, E2E — nunca desplegar con tests rotos
DESPLIEGUE     → Docker, CI/CD, PaaS, IaC — del repo al usuario final
MANTENIMIENTO  → health checks, logs, alertas, correcciones, nuevas features
```

---

