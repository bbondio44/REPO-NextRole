# Git y GitHub para NextRole V2

La rama de integración actual es `develop-v2`. `main` conserva la versión V1; `develop` es histórica. Esta guía no autoriza por sí misma un commit o una publicación: ambos requieren aprobación explícita del usuario que solicita el trabajo.

## Antes de empezar

```powershell
git status --short --branch
git log -5 --oneline --decorate
git diff
git diff --cached
git fetch origin
```

Revisá cada archivo modificado, no rastreado o local antes de continuar. Compará la rama local con `origin/develop-v2` y revisá los commits remotos pendientes antes de integrar. No sobrescribas ni descartes cambios preexistentes.

## Rama de trabajo

Para una tarea aislada, partí de una referencia actualizada de `develop-v2` y creá una rama propia:

```powershell
git switch develop-v2
git pull --ff-only origin develop-v2
git switch -c feature/nombre-breve
```

Si la tarea debe continuar directamente en `develop-v2` por una instrucción explícita, verificá primero que el árbol esté entendido y mantené ese alcance. No cambies a otra rama por iniciativa propia.

## Revisar y preparar un commit autorizado

```powershell
git status --short
git diff --check
git diff
git diff --cached
```

Prepará rutas específicas después de revisarlas:

```powershell
git add ruta/al/archivo
git diff --cached
```

Confirmá que no se incluyan `.env`, credenciales, copias locales, archivos temporales ni cambios ajenos. Usá un mensaje breve y descriptivo, en español cuando corresponda. No hagas amend ni commits adicionales salvo que la autorización los incluya.

## Integrar y publicar

La integración habitual es mediante Pull Request hacia `develop-v2`. Antes de fusionar, incorporá de forma segura los cambios remotos, resolvé conflictos solo cuando la intención sea inequívoca y repetí las validaciones afectadas. Consultá al usuario si un conflicto admite varias interpretaciones.

Push solo con autorización explícita:

```powershell
git push -u origin feature/nombre-breve
```

Al finalizar una sincronización autorizada, ejecutá `git fetch origin` y compará `git status -sb` o `git rev-list --left-right --count HEAD...origin/develop-v2`. No uses force push, `reset`, `restore`, `clean` ni cambies de rama sin autorización.
