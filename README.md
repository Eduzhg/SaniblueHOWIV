# SANIBLUE — App de Metrologia de Hidrômetros

Aplicativo Android para **verificação metrológica de hidrômetros em campo**, com
geração de laudo em PDF. Funciona offline (banco local Room) e foi pensado para o
técnico preencher o ensaio direto no celular.

> Documentação detalhada da arquitetura e do banco está em [LEIAME.md](LEIAME.md).

---

## Funcionalidades

- **Login local** (SHA-256) com perfis de administrador e técnico.
- **Dashboard** com total de ensaios aprovados/reprovados/pendentes.
- **Novo ensaio** com cálculo de erro em tempo real para as 3 vazões.
- **Seleção de norma** do ensaio:
  - **Portaria 246** — QN (nominal) / QT (transição) / QM (mínima)
  - **Portaria 155** — Q3 / Q2 / Q1
  - A norma define os rótulos das vazões **e os limites de erro** aceitáveis.
- **Dois métodos de ensaio**:
  - **Escoamento direto** — o volume escoado é informado diretamente.
  - **Comparativo por leitura** — para a maleta de verificação que não zera:
    informa-se a leitura inicial/final do **padrão ultrassônico** e o volume
    escoado é calculado automaticamente.
- **Idade do hidrômetro** preenchida automaticamente pelo nº de série
  (os 2 primeiros dígitos = ano de fabricação, ex.: `Y20B` → 2020).
- **Dados de substituição** quando o hidrômetro é reprovado (leitura final do
  reprovado, nº de série e leitura inicial do novo).
- **Laudo em PDF** com cabeçalho, dados do cliente/companhia, tabela de medições,
  resultado final e QR Code de validação.

---

## Como compilar

### Pré-requisitos

- **Android Studio** Hedgehog (2023.1.1) ou superior
- **JDK 17 ou 21** (o JBR que acompanha o Android Studio funciona)
- **SDK Android 34** instalado

> ⚠️ **Não use o JDK 25.** O Kotlin 1.9.22 / KSP usado no projeto não consegue
> interpretar a string de versão do Java 25 e a build falha com
> `IllegalArgumentException: 25.0.1`. Use JDK 17 ou 21.

### Passos

1. **File → Open** e selecione a pasta do projeto.
2. Aguarde o Gradle sync.
3. Selecione um dispositivo/emulador e clique em **Run ▶**.

### Pela linha de comando

```bash
./gradlew :app:assembleDebug        # gera o APK de debug
./gradlew :app:testDebugUnitTest    # roda os testes unitários
```

Para forçar um JDK específico (caso o padrão da máquina seja o 25):

```bash
./gradlew :app:assembleDebug -Dorg.gradle.java.home="C:/Program Files/Android/Android Studio/jbr"
```

---

## Acesso padrão

| Usuário  | Senha       | Perfil           |
|----------|-------------|------------------|
| admin    | admin123    | Administrador    |
| tecnico  | tecnico123  | Técnico de Campo |

---

## Stack

Kotlin · Jetpack Compose (Material 3) · Room · Hilt · Navigation Compose · ZXing
(QR Code) · Coroutines/StateFlow. Arquitetura em camadas
(`data` / `domain` / `presentation`).

---

## Configuração local

O arquivo `local.properties` (com o caminho do SDK) **não é versionado**. Ao clonar,
o Android Studio o gera automaticamente, ou crie manualmente:

```properties
sdk.dir=C\:\\Caminho\\Para\\Android\\Sdk
```
