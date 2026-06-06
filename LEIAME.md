# SANIBLUE Metrologia — App Android

Sistema de Verificação Metrológica de Hidrômetros para uso em campo.

---

## Como compilar no Android Studio

### Pré-requisitos

- Android Studio Hedgehog (2023.1.1) ou superior
- JDK 17
- SDK Android 34 instalado

### Passos

1. Abrir Android Studio
2. **File → Open** e selecionar esta pasta
3. Aguardar o Gradle sync finalizar (~2-5 min na primeira vez)
4. Selecionar dispositivo/emulador
5. Clicar em **Run ▶**

---

## Acesso padrão

| Usuário    | Senha       | Perfil           |
|------------|-------------|------------------|
| admin      | admin123    | Administrador    |
| tecnico    | tecnico123  | Técnico de Campo |

---

## Arquitetura

```
app/
├── data/
│   ├── local/
│   │   ├── dao/          ← Interfaces Room (consultas SQL)
│   │   ├── database/     ← SaniblueDatabase, pré-populate
│   │   ├── entity/       ← Tabelas Room (5 entidades)
│   │   └── relations/    ← Joins Room (@Relation)
│   └── repository/       ← Implementações dos repositórios
├── domain/
│   ├── model/            ← Modelos de domínio puros (Kotlin data classes)
│   ├── repository/       ← Interfaces de repositório
│   └── usecase/          ← Lógica de negócio isolada
├── presentation/
│   ├── navigation/       ← NavGraph + Sealed Screen
│   ├── screens/          ← Uma pasta por tela (Screen + ViewModel)
│   ├── components/       ← Composables reutilizáveis
│   └── theme/            ← Material 3 (cores, tipografia, formas)
├── di/                   ← Módulos Hilt (Database, Repository, App)
└── util/                 ← PdfGenerator, QrCodeGenerator, HashUtils
```

---

## Banco de Dados (Room)

### Diagrama de Entidades

```
USUARIO ────────────────────────────────
 id (PK)   nome   login   senha_hash
 cargo     email  ativo

HIDROMETRO_MODELOS ─────────────────────
 id (PK)   nome   descricao
 vazao_nominal   vazao_transicao   vazao_minima
 limite_nominal_min/max
 limite_transicao_min/max
 limite_minima_min/max

ENSAIOS ───────────────────────────────
 id (PK)   hidrometro_modelo_id (FK)
 numero_hidrometro   cliente   matricula
 endereco   cidade   bairro
 data_ensaio   tecnico_responsavel
 idade_hidrometro   temperatura_agua
 observacoes   resultado_final
 sync_status   created_at   updated_at

VAZAO_ENSAIOS ─────────────────────────
 id (PK)   ensaio_id (FK → CASCADE)
 tipo_vazao  [NOMINAL | TRANSICAO | MINIMA]
 m1/m2/m3_escoamento
 m1/m2/m3_leitura_inicial
 m1/m2/m3_leitura_final
 erro_1/2/3   erro_medio   aprovado

FOTOS_ENSAIO ──────────────────────────
 id (PK)   ensaio_id (FK → CASCADE)
 tipo_foto  [HIDROMETRO | LOCAL | LEITURA | ASSINATURA]
 caminho_arquivo   data_captura
```

---

## Regras de Negócio

### Cálculo do Erro

```
Totalizado = Leitura Final - Leitura Inicial
Erro (%) = ((Totalizado - Escoamento) / Escoamento) × 100
```

### Limites de Aprovação

| Vazão        | Limite Mín | Limite Máx |
|--------------|-----------|-----------|
| Nominal      | -5%       | +5%       |
| Transição    | -5%       | +5%       |
| Mínima       | -10%      | +10%      |

### Resultado Final

- **APROVADO**: todas as 3 vazões aprovadas
- **REPROVADO**: qualquer vazão fora do limite
- **PENDENTE**: dados incompletos

---

## Modelos Pré-cadastrados

| Modelo           | Nominal (L/h) | Transição (L/h) | Mínima (L/h) |
|------------------|--------------|----------------|--------------|
| Hidrômetro 3 m³/h  | 1500         | 120            | 30           |
| Hidrômetro 5 m³/h  | 2500         | 200            | 50           |
| Hidrômetro 10 m³/h | 5000         | 400            | 100          |

Novos modelos podem ser adicionados pelo cadastro em **Configurações → Hidrômetros** sem alterar código.

---

## Telas

| Tela                    | Descrição                                           |
|-------------------------|-----------------------------------------------------|
| Splash Screen           | Animação de entrada com logo SANIBLUE               |
| Login Local             | Autenticação SHA-256 contra banco local             |
| Dashboard               | Indicadores: total, aprovados, reprovados, pendentes |
| Lista de Ensaios        | Busca por hidrômetro, cliente ou data               |
| Novo Ensaio             | Formulário completo com cálculo em tempo real       |
| Detalhes do Ensaio      | Visualização + geração de PDF                       |
| Cadastro de Hidrômetros | CRUD de modelos de hidrômetros                      |
| Configurações           | Informações do sistema                              |

---

## Geração de PDF

O laudo PDF é gerado com a API nativa Android (`android.graphics.pdf.PdfDocument`) e inclui:

- Cabeçalho SANIBLUE com cores da marca
- Dados do cliente e do ensaio
- Tabela de medições com erros individuais e médios
- Resultado final destacado (verde/vermelho)
- QR Code para validação online futura
- Rodapé com técnico e data de emissão

---

## Estratégia de Sincronização Futura

O campo `sync_status` na tabela `ensaios` suporta os estados:

- `PENDING` — criado offline, aguardando sync
- `SYNCED` — sincronizado com servidor
- `ERROR` — erro na sincronização

Para implementar a sync:
1. Criar `RemoteDataSource` com interface Retrofit/API
2. Criar `SyncWorker` (WorkManager) que envia `PENDING` ao servidor
3. Atualizar `sync_status = SYNCED` após confirmação
4. O `RepositoryModule` vincula a implementação remota

---

## Dependências Principais

| Biblioteca                    | Versão    | Uso                          |
|-------------------------------|-----------|------------------------------|
| Jetpack Compose BOM           | 2024.02   | UI declarativa               |
| Material Design 3             | —         | Componentes e theming        |
| Room                          | 2.6.1     | Banco de dados local         |
| Hilt                          | 2.50      | Injeção de dependência       |
| Navigation Compose            | 2.7.7     | Navegação entre telas        |
| ZXing Core                    | 3.5.3     | Geração de QR Code           |
| Coil Compose                  | 2.5.0     | Carregamento de imagens      |
| Coroutines + StateFlow        | 1.7.3     | Programação assíncrona       |
| DataStore Preferences         | 1.0.0     | Preferências persistidas     |
| AndroidX SplashScreen         | 1.0.1     | Splash Screen API            |

---

## Testes

```bash
./gradlew test              # Testes unitários
./gradlew connectedCheck    # Testes instrumentados (requer dispositivo)
```

Testes unitários cobrem:
- `CalcularErroUseCaseTest` — 10 casos de teste para cálculo de erro e aprovação
- `HashUtilsTest` — validação de hashing SHA-256

---

## Estrutura de Pastas (resumo)

```
App Saniblue/
├── gradle/libs.versions.toml
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/saniblue/app/
│       │   ├── SaniblueApp.kt
│       │   ├── MainActivity.kt
│       │   ├── data/...
│       │   ├── domain/...
│       │   ├── presentation/...
│       │   ├── di/...
│       │   └── util/...
│       └── res/
│           ├── drawable/
│           ├── values/
│           └── xml/
└── LEIAME.md
```
