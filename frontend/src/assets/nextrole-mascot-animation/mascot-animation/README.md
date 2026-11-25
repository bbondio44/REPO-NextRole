# NextRole — recursos de animación
Copiar los PNG a frontend/src/assets/brand/mascot-animation/. Cada pose y fotograma tiene 320×320 px RGBA; línea inferior de apoyo y=300. Sprite horizontal de 2560×320, ocho celdas. Reutilizar ../mascot-wave.png para celebrar.

Reproducir con CSS: background-size: 800% 100%; animación de background-position-x de 0% a 100% con steps(7,end) en 880ms, o alternar los ocho PNG cada 110ms. Usar un contenedor separado para mover la mascota sobre un path SVG según el progreso guardado. Detener la caminata al llegar a la posición real y mostrar idle. Respetar prefers-reduced-motion. No modificar backend ni matching.

Los fotogramas fueron generados por IA y recortados/normalizados; no son un rig 3D. Puede haber pequeñas variaciones entre poses y el primer y último fotograma son similares. Validar el bucle en el navegador antes de producción. No reemplazar los assets originales.
