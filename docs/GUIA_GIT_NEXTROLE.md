# Cómo subir archivos a GitHub — NextRole

Esta guía muestra cómo clonar el repositorio **NextRole** y publicar cambios desde la terminal (Git Bash, PowerShell o la terminal de VS Code).

> **Importante:** el repositorio se clona una sola vez. Después, los pasos de actualización, trabajo, commit y publicación se repiten cada vez que se quieran subir cambios.
>
> **Flujo del equipo:** trabajamos en ramas `feature/...` o `fix/...` creadas a partir de `develop`, y enviamos un **Pull Request hacia `develop`**. La rama `main` se reserva para versiones estables.

## 1. Clonar el repositorio

Descargamos una copia del proyecto en nuestra computadora.

```bash
git clone https://github.com/bbondio44/REPO-NextRole.git
```

> También se puede usar SSH si ya está configurada una clave SSH en GitHub: `git clone git@github.com:bbondio44/REPO-NextRole.git`.

## 2. Entrar al proyecto

Nos movemos a la carpeta creada al clonar el repositorio.

```bash
cd REPO-NextRole
```

## 3. Descargar los últimos cambios

Antes de comenzar una tarea, nos ubicamos en `develop` y actualizamos la rama:

```bash
git switch develop
git pull origin develop
```

> Si todavía no existe `develop`, primero hay que crearla desde `main` y publicarla. Este paso se realiza **una sola vez** por el equipo:
>
> ```bash
> git switch main
> git pull origin main
> git switch -c develop
> git push -u origin develop
> ```
>
> Una vez creada en GitHub, los demás integrantes pueden obtenerla con `git fetch origin` y `git switch develop`.

## 4. Crear o cambiar de rama

Consultamos las ramas disponibles:

```bash
git branch
```

Creamos una rama nueva **a partir de `develop` actualizado**:

```bash
git switch -c feature/hu-03-perfil
```

Para cambiar a una rama existente:

```bash
git switch nombre-de-la-rama
```

Para volver a la rama de integración:

```bash
git switch develop
```

**Convenciones sugeridas:**

- `feature/hu-03-perfil`: funcionalidad nueva.
- `fix/error-login`: corrección de error.
- `docs/guia-git`: cambios en documentación.

## 5. Comprobar el estado inicial

Verificamos la rama actual y si existen cambios pendientes:

```bash
git status
```

## 6. Identificar los cambios

Después de agregar o modificar archivos, revisamos qué detectó Git:

```bash
git status
```

Podemos revisar las diferencias antes de prepararlas:

```bash
git diff
```

## 7. Preparar los archivos

Indicamos qué archivo queremos incluir en el próximo commit:

```bash
git add ruta/del/archivo
```

O agregamos todos los cambios de la carpeta actual, **después de revisarlos**:

```bash
git add .
```

Comprobamos qué quedó preparado:

```bash
git diff --staged
```

> No subir archivos `.env`, contraseñas, tokens ni otras credenciales.

## 8. Crear el commit

Guardamos los cambios con un mensaje breve que explique lo realizado:

```bash
git commit -m "feat: agregar formulario de perfil"
```

Otros ejemplos:

```bash
git commit -m "fix: corregir validacion de login"
git commit -m "docs: agregar guia de Git"
```

## 9. Publicar los cambios

La primera vez que publicamos una rama nueva usamos:

```bash
git push -u origin feature/hu-03-perfil
```

En las siguientes publicaciones de esa misma rama basta con:

```bash
git push
```

## 10. Crear un Pull Request

Una vez publicada la rama:

1. Abrimos el repositorio: [REPO-NextRole](https://github.com/bbondio44/REPO-NextRole).
2. Seleccionamos **Compare & pull request** (o **Pull requests → New pull request**).
3. Elegimos **base: `develop`** y **compare: `feature/hu-03-perfil`**.
4. Describimos el cambio, la HU/Issue relacionada y las pruebas realizadas.
5. Solicitamos revisión y esperamos la aprobación y las pruebas acordadas.
6. Fusionamos el PR en `develop` cuando esté listo.

> No hacer `push` directo a `main`. Para publicar una versión estable, se crea un PR de `develop` hacia `main`.

## 11. Comenzar otra tarea

Después de que el PR se integre, volvemos a actualizar `develop` antes de crear otra rama:

```bash
git switch develop
git pull origin develop
git switch -c feature/nueva-tarea
```

---

## Resumen rápido

```bash
# Desde el repositorio ya clonado
git switch develop
git pull origin develop
git switch -c feature/nombre-tarea

# Después de trabajar
git status
git add .
git commit -m "feat: descripcion del cambio"
git push -u origin feature/nombre-tarea

# Finalmente: crear Pull Request hacia develop en GitHub
```

**Repositorio:** https://github.com/bbondio44/REPO-NextRole.git
