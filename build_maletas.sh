#!/usr/bin/env bash
# ============================================================
#  Versão Linux do build_maletas.bat
#  Gera um APK release para cada maleta listada em maletas.csv
#  Formato do CSV: flavor,nome,erroNominal,erroTransicao,erroMinima
#    flavor = escoamento | comparativo
#  Ex.: comparativo,M-003,0.42,0.40,0.55
#
#  Os APKs finais ficam na pasta  maletas_apks/
#
#  Uso:  ./build_maletas.sh
# ============================================================
set -uo pipefail
cd "$(dirname "$0")"

# Se o JAVA_HOME não estiver configurado no sistema, usa o JDK instalado em
# ~/.local/share/android-build (mesma ideia do .bat, que cai no JDK do Android Studio).
if [ -z "${JAVA_HOME:-}" ] && [ -x "$HOME/.local/share/android-build/jdk/bin/java" ]; then
    export JAVA_HOME="$HOME/.local/share/android-build/jdk"
fi
if [ -z "${ANDROID_HOME:-}" ] && [ -d "$HOME/.local/share/android-build/sdk" ]; then
    export ANDROID_HOME="$HOME/.local/share/android-build/sdk"
fi

if [ ! -f "maletas.csv" ]; then
    echo "[ERRO] Arquivo maletas.csv nao encontrado nesta pasta."
    exit 1
fi

OUTDIR=maletas_apks
mkdir -p "$OUTDIR"

# IFS=, lê os 5 campos; o || [ -n "$FLAVOR" ] garante a última linha sem \n no fim
while IFS=, read -r FLAVOR NOME ERRN ERRT ERRM || [ -n "${FLAVOR:-}" ]; do
    # tira espaços/CR (o CSV pode vir do Windows, com terminador \r\n)
    FLAVOR=$(echo "${FLAVOR:-}" | tr -d ' \r')
    NOME=$(echo "${NOME:-}" | tr -d '\r' | sed 's/^ *//; s/ *$//')
    ERRN=$(echo "${ERRN:-}" | tr -d ' \r')
    ERRT=$(echo "${ERRT:-}" | tr -d ' \r')
    ERRM=$(echo "${ERRM:-}" | tr -d ' \r')

    # ignora linhas em branco e comentários (#)
    [ -z "$FLAVOR" ] && continue
    case "$FLAVOR" in \#*) continue ;; esac

    case "$(echo "$FLAVOR" | tr '[:upper:]' '[:lower:]')" in
        escoamento)  TASK=assembleEscoamentoRelease ;;
        comparativo) TASK=assembleComparativoRelease ;;
        *) echo "[AVISO] Flavor desconhecido \"$FLAVOR\" (use escoamento ou comparativo). Pulando."
           continue ;;
    esac

    echo
    echo "=========================================================="
    echo " Gerando: $FLAVOR  |  Maleta $NOME  |  Erros N=$ERRN T=$ERRT M=$ERRM"
    echo "=========================================================="
    if ! sh ./gradlew "$TASK" \
            -PmaletaNome="$NOME" \
            -PerroNominal="$ERRN" \
            -PerroTransicao="$ERRT" \
            -PerroMinima="$ERRM"; then
        echo "[ERRO] Falha ao gerar a maleta $NOME. Abortando."
        exit 1
    fi

    SRC="app/build/outputs/apk/$FLAVOR/release/app-$FLAVOR-release.apk"
    DST="$OUTDIR/SANIBLUE_${FLAVOR}_${NOME// /_}.apk"
    cp -f "$SRC" "$DST"
    echo "APK salvo em: $DST"
done < maletas.csv

echo
echo "Concluido. APKs prontos na pasta: $OUTDIR"
