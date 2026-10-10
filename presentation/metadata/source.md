## Título: Migración de servicios legacy con agentes Kiro de AWS

## Participantes:
- Osmond Pomare, Lead Platform Engineer en Nequi
- Cristhian Castillo, Lead Platform Engineer en Nequi

## Contexto:
Como organización sólida en el mercado, que nació de una idea y evolucionó a un Neobanco, la mantenibilidad de proyectos de código legacy es inevitable. En su momento, para experimentar y probar, no se priorizaron prácticas de mantenibilidad a largo plazo, lo que produjo código legado escrito en tecnologías que hoy no tienen soporte y versionadas de forma extraña. Un ejemplo de esto: un proyecto que tiene diferentes versiones de la misma funcionalidad separadas por ramas.

Algunas de las causas para resaltar:
- Proyecto evolutivo que nació de una idea.
- Adopción de una sola tecnología para trabajar todo.
- Pasar por alto la evolución de la nube durante los últimos 10 años.
- El tiempo, como factor natural de adquisición de deuda técnica.

## Motivación
Los costos de mantenibilidad aumentan cada vez más por varias razones. Por ejemplo: al tratarse de tecnologías legacy, encontrar developers con el conocimiento y la experiencia técnica necesarios se vuelve complejo. Las tecnologías utilizadas pierden soporte, lo que dificulta cerrar vulnerabilidades y obliga a buscar otra dependencia y reemplazarla.

El time to market del producto disminuye. A pesar de tener un legacy de años —que en principio se pensaría debería acelerar la entrega, porque todo ya está escrito en el legacy—, la realidad es que la disminuye. Esto se debe a que, para agregar una nueva funcionalidad, es necesario validar y verificar que las demás funcionalidades que en principio no se modifican no se vean impactadas por los cambios realizados.

El crecimiento de la empresa también frena la evolución de estos proyectos, debido a que el número de developers aumenta. Se vuelve más complejo trabajar sobre los mismos componentes sin que los developers se pisen entre sí y generen conflictos en el código que tomen tiempo en solucionar.

Implementar nuevas estrategias de resiliencia, escalabilidad y elasticidad se vuelve más complejo, debido a que nacen nuevos frameworks y dependencias —como librerías— que no cuentan con soporte sobre las tecnologías ya utilizadas.

## Pregunta
¿Qué hacemos con el legado? ¿Cómo lo migramos?

## Alternativas
Hace un par de años, sin la IA, ya realizábamos procesos de migración. ¿Cómo los efectuábamos? Bajo demanda y de acuerdo con la prioridad y la necesidad del negocio. En muchos casos, iniciábamos con los servicios más críticos para el negocio, en función de la disponibilidad y por temas regulatorios; en otros, por temas de evolución, negociábamos con el producto iniciar con una fase de migración de la funcionalidad actual, llevarla a producción, estabilizarla y luego acelerar el proceso de evolución del producto.

Desde la perspectiva más técnica, inicialmente se revisaba un framework de desarrollo como Spring Framework, se realizaban algunas pruebas de concepto y se tomaba la decisión de migrar las funcionalidades a esa tecnología. Este proceso se conoce mejor como actualización del stack tecnológico. No solo a nivel de desarrollo, sino también de pruebas de calidad, pruebas de integración y procesos de DevOps para desplegar los nuevos artefactos.

Desde la perspectiva del desarrollador se adoptaba una estrategia de "divide y vencerás". Por un lado, inicialmente era necesario ir a revisar el código del componente de software legado y entenderlo; luego crear el proyecto destino e iniciar con la migración. En este proceso puntual era donde se aplicaba esta técnica: migra primero los objetos que transportan los datos, luego revisa las integraciones con terceros —como bases de datos y consumo de APIs— e implementa la lógica de negocio, y cuida la mensajería para que no se vea afectada. Superada esta etapa, se llevaba el componente a ambientes de prueba, donde se validaba la correcta ejecución del componente, y luego se llevaba al ambiente productivo, donde se implementaba una técnica de despliegue como la de canary deployment: 10% del tráfico por el servicio migrado y 90% por el camino antiguo.

## Solución Propuesta
La solución propuesta busca automatizar el proceso descrito anteriormente con IA. En vez de delegar un prompt indicando "por favor, migra el componente X", se optó por realizar una descripción del proceso que normalmente un developer implementaba hace un par de años, para que, con el uso de Skills, Prompts y MCPs, este proceso se automatice siguiendo una secuencia de pasos cuidadosamente diseñados y en el orden correcto, buscando un resultado con un alto grado de probabilidad de éxito y no una "apuesta".

Esto lo vimos factible de implementar usando Kiro, una herramienta agéntica que permite realizar esta configuración de una manera rápida, sencilla y, sobre todo, clara para el developer.

### Claves de la Propuesta
- Automatizar un proceso y no apostar.
- La generación de proyectos es una tarea que, por diferentes medios, se puede automatizar sin el uso de IA, garantizando de entrada un resultado exacto en función de la necesidad de la base del proyecto.
- La generación de módulos y piezas de código ya estandarizadas: este código o validación lo tenemos replicado 20 veces en muchos proyectos, funciona... entonces, ¿por qué dejar que la IA lo haga diferente? ¿Por qué no indicarle a la IA cómo lo debe hacer?
- La generación de contexto es clave en este proceso. El contexto que normalmente un equipo de desarrollo adquiere a lo largo de la interacción con los proyectos, en este caso, a través de agentes y Skills, se genera como un contexto del proyecto a migrar con una estructura ya preestablecida y en función de la migración.

## Taller
- Instalen Kiro.
- Activen la licencia.
- Descarguen el repositorio.
- Ejecuten los siguientes pasos.
- Evidencien un resultado en sus IDEs.

## Trabajo Futuro
- Cómo centralizar el agente.
- Cómo almacenar ese contexto que se acumula en la máquina del developer.
- Kiro WorkFlows
