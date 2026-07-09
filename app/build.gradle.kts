plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.saniblue.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.saniblue.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room schema export
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        // === Configuração da maleta (embutida no build) ===
        // Nome e erros padrão (um por vazão) são passados ao compilar cada maleta:
        //   ./gradlew assembleComparativoRelease -PmaletaNome="M-003" ^
        //       -PerroNominal=0.42 -PerroTransicao=0.40 -PerroMinima=0.55
        // Sem parâmetros, usa os valores de teste abaixo (desenvolvimento).
        val maletaNome = (project.findProperty("maletaNome") as String?) ?: "Maleta de Teste"
        val erroNominal = (project.findProperty("erroNominal") as String?) ?: "0.5"
        val erroTransicao = (project.findProperty("erroTransicao") as String?) ?: "0.4"
        val erroMinima = (project.findProperty("erroMinima") as String?) ?: "0.6"
        buildConfigField("String", "MALETA_NOME", "\"$maletaNome\"")
        buildConfigField("double", "ERRO_PADRAO_NOMINAL", erroNominal)
        buildConfigField("double", "ERRO_PADRAO_TRANSICAO", erroTransicao)
        buildConfigField("double", "ERRO_PADRAO_MINIMA", erroMinima)
    }

    // === Tipo de ensaio (uma variante por tipo) ===
    // Cada maleta é de UM tipo só. O flavor trava o tipo no build e esconde o outro.
    //   assembleEscoamento*  → app de escoamento direto
    //   assembleComparativo* → app de comparativo por leitura
    flavorDimensions += "tipoEnsaio"
    productFlavors {
        create("escoamento") {
            dimension = "tipoEnsaio"
            applicationIdSuffix = ".escoamento"
            buildConfigField("String", "TIPO_ENSAIO", "\"ESCOAMENTO_DIRETO\"")
            resValue("string", "app_name", "Saniblue ED")
        }
        create("comparativo") {
            dimension = "tipoEnsaio"
            applicationIdSuffix = ".comparativo"
            buildConfigField("String", "TIPO_ENSAIO", "\"COMPARATIVO_LEITURA\"")
            resValue("string", "app_name", "Saniblue CL")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Assinado com a chave de debug para permitir instalar o APK release
            // (otimizado pelo R8) direto nos tablets de teste — Compose em release
            // é muito mais rápido que em debug. Trocar por keystore próprio se o
            // app for distribuído por loja.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.animation)

    // Navigation
    implementation(libs.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Coroutines
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.core)

    // ZXing - QR Code
    implementation(libs.zxing.core)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coil — carregamento de imagens (foto do ensaio não realizado)
    implementation(libs.coil.compose)

    // Debug
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Espresso 3.6+ exige tracing >= 1.1; a resolução consistente do AGP força os testes
    // à versão do app, então o upgrade precisa ficar aqui (sem isto: NoSuchMethodError
    // forceEnableAppTracing e o teste não enxerga a árvore do Compose)
    implementation("androidx.tracing:tracing:1.2.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
