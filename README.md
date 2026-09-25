# Nosotros — Amor a distancia (Android)

Aplicación nativa de Android desarrollada en **Kotlin** y **Jetpack Compose** para parejas en relaciones a distancia. Diseñada con una estética oscura íntima con acentos en oro rosa, tipografía editorial y componentes interactivos en tiempo real.

## Características Principales

1. **Hogar (Espacio Compartido)**:
   - Tarjetas de estado con zonas horarias en vivo (Madrid y Tokio), clima, nivel de batería y disponibilidad.
   - Contador regresivo hacia el próximo reencuentro (*18 oct*).
   - Botón interactivo central con vibración háptica, animaciones de ondas luminosas y latido en directo.
   - Pizarrón de notas rápidas compartidas con marcado de tareas, autor y eliminación.

2. **Conexión (Preguntas & Retos)**:
   - **Pregunta diaria en doble ciega**: la respuesta de la pareja se mantiene borrosa hasta que tú respondes; al revelarse, se habilita el hilo de conversación.
   - **Retos cooperativos**: seguimiento bilateral de actividades conjuntas a distancia (cocinar la misma receta, fotografía espontánea, playlist cruzada, etc.).
   - **Preguntas secretas**: formulario para crear preguntas privadas y tarjetas de estado.

3. **Sala de Cine (Watch Together)**:
   - Reproductor de vídeo sincronizado con Media3 ExoPlayer.
   - Lluvia de reacciones emoji flotantes animadas en pantalla (❤️, 🍿, 😂, 😭, 😍, 🔥).
   - Control de canal de voz.
   - Selector de biblioteca de vídeos (.mp4) y campo para enlaces personalizados.
   - Chat de sala en directo con respuestas simuladas de pareja.

4. **Nuestro Feed (Recuerdos para Dos)**:
   - Bandeja superior de historias efímeras con visor modal animado a pantalla completa y barra de progreso.
   - Feed privado con soporte de fotos, doble toque para dar me gusta (*1/1 ❤️*), notas de voz con visor de onda de audio interactivo y cajón de comentarios.
   - Conmutador entre vista Feed vertical y Cuadrícula (Grid 3x3).

5. **Bóveda Íntima (Espacio Protegido)**:
   - Pantalla de desbloqueo con teclado de PIN de 4 dígitos (demo: `1402`) y lector de huella dactilar biométrico.
   - **Desire Match**: cartas de deseos deslizables (*swipe left/right* o botones Sí/No) en doble ciega; genera efecto visual de *¡Es un match!* cuando coinciden y los guarda en el historial.
   - **Recuerdos de Voz**: audios cifrados y cuadrícula de recuerdos privados.

## Arquitectura y Tecnologías

- **Lenguaje**: Kotlin 2.0+
- **UI**: Jetpack Compose con Material 3 y Material You theming
- **Arquitectura**: MVVM con StateFlow y Kotlin Coroutines
- **Reproducción Multimedia**: AndroidX Media3 ExoPlayer
- **Imágenes**: Coil Compose y drawables vectoriales adaptativos
- **Build System**: Gradle Kotlin DSL con Version Catalog (`libs.versions.toml`)
