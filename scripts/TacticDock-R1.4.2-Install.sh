#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail
# Authenticate with: gh auth login; ADB must already be paired/authorized.
REPO="Jorgeprdz/TacticDock"
BRANCH="feat/tactic-dock-r1-floating"
PKG="mx.tactic.dock"
SERVICE="$PKG/cu.axel.smartdock.services.DockService"
APK="TacticDock-R1.4.2.apk"
ARTIFACT="TacticDock-R1.4.2-APK"
log() { printf '[TacticDock R1.4.2] %s\n' "$*"; }
fail() { printf '[ERROR] %s\n' "$*" >&2; exit 1; }
for c in gh adb sha256sum mktemp; do
  command -v "$c" >/dev/null || fail "Falta $c. Termux: pkg install gh android-tools coreutils"
done
gh auth status >/dev/null 2>&1 || fail "Primero ejecuta gh auth login"
HEAD="$(gh api "repos/$REPO/branches/$BRANCH" --jq '.commit.sha')" || fail "No se pudo consultar la rama"
RUN="$(gh run list -R "$REPO" -w tactic-dock-r1.yml -b "$BRANCH" -c "$HEAD" -s success -L 10 --json databaseId --jq '.[0].databaseId // empty')" || fail "Error consultando workflows"
[[ -n "$RUN" ]] || fail "No hay compilacion exitosa para el HEAD actual $HEAD. No se usa APK antigua."
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT
log "Workflow=$RUN, commit=$HEAD"
gh run download "$RUN" -R "$REPO" -n "$ARTIFACT" -D "$TMP" || fail "No se pudo descargar artifact"
[[ -s "$TMP/$APK" && -s "$TMP/$APK.sha256" ]] || fail "El artifact no contiene APK y SHA256"
(cd "$TMP" && sha256sum -c "$APK.sha256") || fail "Checksum invalido, instalacion cancelada"
log "SHA256: $(sha256sum "$TMP/$APK" | cut -d' ' -f1)"
unset LD_LIBRARY_PATH
adb start-server >/dev/null || fail "Fallo al iniciar ADB"
adb devices -l
mapfile -t DEVICES < <(adb devices | awk '$2=="device" {print $1}')
(( ${#DEVICES[@]} == 1 )) || fail "Conecta y autoriza exactamente un dispositivo ADB"
SERIAL="${DEVICES[0]}"
ADB=(adb -s "$SERIAL")
"${ADB[@]}" shell getprop ro.build.version.release >/dev/null || fail "ADB no autorizado"
log "Instalando en $SERIAL"
if OUTPUT="$("${ADB[@]}" install -r "$TMP/$APK" 2>&1)"; then
  log "$OUTPUT"
else
  log "$OUTPUT"
  if [[ "$OUTPUT" == *INSTALL_FAILED_UPDATE_INCOMPATIBLE* ]]; then
    log "Firma incompatible: DESINSTALAR BORRA TODOS LOS DATOS DE TACTIC DOCK"
    read -r -p "Escribe DESINSTALAR para borrar los datos y continuar: " CONFIRM
    [[ "$CONFIRM" == "DESINSTALAR" ]] || fail "No se desinstalo nada"
    "${ADB[@]}" uninstall "$PKG" || fail "No se pudo desinstalar"
    "${ADB[@]}" install "$TMP/$APK" || fail "Reinstalacion fallida"
  else
    fail "Fallo instalacion APK"
  fi
fi
"${ADB[@]}" shell pm path "$PKG" | grep -q 'package:' || fail "Paquete no verificable"
log "Paquete confirmado: $PKG"
# Only app-ops associated with permissions in AndroidManifest.
"${ADB[@]}" shell cmd appops set "$PKG" SYSTEM_ALERT_WINDOW allow || log "PENDIENTE overlay; Ajustes del sistema"
"${ADB[@]}" shell cmd appops set "$PKG" GET_USAGE_STATS allow || log "PENDIENTE estadisticas de uso"
for OP in SYSTEM_ALERT_WINDOW GET_USAGE_STATS; do
  STATE="$("${ADB[@]}" shell cmd appops get "$PKG" "$OP" 2>&1 || true)"
  log "$OP: $STATE"
  [[ "$STATE" == *allow* ]] || log "PENDIENTE: $OP requiere intervencion"
done
# Do not change secure settings or existing accessibility services.
ACC="$("${ADB[@]}" shell settings get secure enabled_accessibility_services 2>/dev/null | tr -d '\r')"
if [[ ":$ACC:" == *":$SERVICE:"* ]]; then
  log "Accesibilidad: habilitada"
else
  log "PENDIENTE: habilita Tactic Dock en Ajustes > Accesibilidad"
fi
N="$("${ADB[@]}" shell settings get secure enabled_notification_listeners 2>/dev/null | tr -d '\r')"
if [[ "$N" == *"$PKG/"* ]]; then
  log "Notificaciones: acceso autorizado"
else
  log "PENDIENTE: habilita acceso a notificaciones en Ajustes"
fi
log "Ajustes restringidos: verificar y permitir manualmente en Info de la aplicacion > menu"
if [[ -d "$HOME/storage/downloads" && -w "$HOME/storage/downloads" ]]; then
  cp "$TMP/$APK" "$HOME/storage/downloads/$APK" || fail "No se pudo guardar en Descargas"
  log "APK guardada en Descargas de Android"
else
  cp "$TMP/$APK" "$HOME/$APK" || fail "No se pudo guardar APK"
  log "Sin acceso a Descargas; APK guardada en $HOME/$APK"
fi
log "Instalacion verificada. Pruebas fisicas de DeX, displays y hover siguen PENDIENTES."
