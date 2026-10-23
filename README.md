# Procesos - Concurrencia en JavaFX

## Descripción del proyecto

Este proyecto corresponde al laboratorio de Programación de Aplicaciones de Escritorio, donde se trabaja con programación concurrente utilizando Java, JavaFX, Thread y Runnable.

El objetivo es desarrollar una aplicación que permita procesar solicitudes desde dos ventanillas al mismo tiempo, evitando bloquear la interfaz gráfica.

El proyecto se desarrolla en equipo utilizando GitHub y ramas individuales para organizar las contribuciones.


## Preguntas y respuestas - Estación 1

### 1. Si una tarea duerme 500 ms cinco veces, ¿se bloqueará la interfaz al invocarla desde el controlador? ¿Qué cambiaría al ejecutarla en un hilo independiente?

Sí. Si ejecutamos directamente esa tarea en el hilo principal de JavaFX, la interfaz se bloquearía aproximadamente 2.5 segundos, porque tendría que esperar a que termine el proceso.

En cambio, al ejecutarla mediante un hilo independiente con `start()`, el procesamiento se realiza en segundo plano y la interfaz puede seguir respondiendo.

### 2. ¿Por qué el orden de los mensajes cambia entre ejecuciones?

Porque los hilos se ejecutan concurrentemente y el sistema operativo decide cuándo dar tiempo de procesamiento a cada uno.

Por esta razón, algunas veces la ventanilla A puede mostrar primero sus mensajes y otras veces puede hacerlo la ventanilla B. El orden no está garantizado.

### 3. ¿Es correcto asumir que start() termina el trabajo antes de devolver el control?

No. El método `start()` inicia la ejecución de un nuevo hilo, pero no espera a que este termine.

Esto permite que el programa principal continúe ejecutándose mientras los otros hilos realizan sus tareas.

## Diferencia entre run() y start()

| run() | start() |
|---|---|
| Ejecuta el método en el hilo que lo llama. | Inicia un nuevo hilo de ejecución. |
| No genera concurrencia por sí mismo. | Permite ejecutar tareas concurrentemente. |
| Si se llama directamente desde JavaFX, puede bloquear la interfaz. | Permite realizar trabajo en segundo plano. |

## Pruebas realizadas

Se comprobó el funcionamiento de las dos ventanillas mediante la ejecución de las clases desarrolladas.

La comparación permitió observar que, utilizando `run()`, las operaciones se ejecutan secuencialmente desde el hilo principal.

Al utilizar `start()`, las dos ventanillas trabajan concurrentemente y sus mensajes pueden aparecer intercalados.

Para completar la evidencia del laboratorio, se deben registrar los nombres de los hilos y el orden observado en tres ejecuciones.

## Instrucciones de ejecución

1. Abrir el proyecto `HilosJava` en IntelliJ IDEA.
2. Verificar que Maven haya cargado las dependencias.
3. Utilizar JDK 17 o superior.
4. Ejecutar `PruebaVentanillas.java` para comprobar el procesamiento concurrente.
5. Ejecutar `ComparacionHilos.java` para comparar `run()` y `start()`.
6. Revisar los resultados en la consola.

## Organización en GitHub

El proyecto utiliza las siguientes ramas:

- `main`: versión estable del proyecto.
- `Loaisiga`: Thread y Runnable.
- `Davis`: estados y monitoreo de hilos.
- `feature-Gean`: sincronización del contador.
- `Valverde-branch`: cancelación e integración JavaFX.

Los cambios se integran mediante Pull Requests revisados por el líder.

## Conclusión

Con esta primera estación comprendimos cómo utilizar `Thread` y `Runnable` para ejecutar varias tareas de manera concurrente.

También aprendimos que llamar directamente a `run()` no inicia un nuevo hilo, mientras que `start()` permite ejecutar procesos independientes.

Estos conceptos son importantes para desarrollar aplicaciones JavaFX que realicen tareas en segundo plano sin bloquear la interfaz gráfica.
