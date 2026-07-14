@echo off
setlocal enabledelayedexpansion
REM ============================================================
REM  Gera um APK release para cada maleta listada em maletas.csv
REM  Formato do CSV: flavor,nome,erroNominal,erroTransicao,erroMinima
REM    flavor = escoamento | comparativo
REM  Ex.: comparativo,M-003,0.42,0.40,0.55
REM
REM  Os APKs finais ficam na pasta  maletas_apks\
REM ============================================================

cd /d "%~dp0"

REM Se JAVA_HOME nao estiver configurado no sistema, usa o JDK que vem junto
REM com o Android Studio (evita o erro "JAVA_HOME is not set" ao rodar fora
REM do Android Studio, ex.: pelo Code Runner do VS Code).
if "%JAVA_HOME%"=="" (
    if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe" (
        set "JAVA_HOME=%ProgramFiles%\Android\Android Studio\jbr"
    )
)

if not exist "maletas.csv" (
    echo [ERRO] Arquivo maletas.csv nao encontrado nesta pasta.
    exit /b 1
)

set OUTDIR=maletas_apks
if not exist "%OUTDIR%" mkdir "%OUTDIR%"

for /f "usebackq tokens=1-5 delims=," %%A in ("maletas.csv") do (
    set "FLAVOR=%%A"
    set "NOME=%%B"
    set "ERRN=%%C"
    set "ERRT=%%D"
    set "ERRM=%%E"

    REM ignora linhas em branco e comentarios (#)
    if not "!FLAVOR!"=="" if not "!FLAVOR:~0,1!"=="#" (
        set "TASK="
        if /I "!FLAVOR!"=="escoamento"  set "TASK=assembleEscoamentoRelease"
        if /I "!FLAVOR!"=="comparativo" set "TASK=assembleComparativoRelease"

        if "!TASK!"=="" (
            echo [AVISO] Flavor desconhecido "!FLAVOR!" ^(use escoamento ou comparativo^). Pulando.
        ) else (
            echo.
            echo ==========================================================
            echo  Gerando: !FLAVOR!  ^|  Maleta !NOME!  ^|  Erros N=!ERRN! T=!ERRT! M=!ERRM!
            echo ==========================================================
            call "%~dp0gradlew.bat" !TASK! -PmaletaNome="!NOME!" -PerroNominal=!ERRN! -PerroTransicao=!ERRT! -PerroMinima=!ERRM!
            if errorlevel 1 (
                echo [ERRO] Falha ao gerar a maleta !NOME!. Abortando.
                exit /b 1
            )
            set "SRC=app\build\outputs\apk\!FLAVOR!\release\app-!FLAVOR!-release.apk"
            set "NOMEFILE=!NOME: =_!"
            set "DST=%OUTDIR%\SANIBLUE_!FLAVOR!_!NOMEFILE!.apk"
            copy /Y "!SRC!" "!DST!" >nul
            echo APK salvo em: !DST!
        )
    )
)

echo.
echo Concluido. APKs prontos na pasta: %OUTDIR%
endlocal
