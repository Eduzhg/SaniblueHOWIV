# ============================================================================
# Regras de ofuscação (R8) — aplicadas apenas no build de RELEASE.
#
# Objetivo: ofuscar a lógica (use cases, ViewModels, PdfGenerator, util, etc.)
# mantendo apenas o que NÃO pode ser renomeado sem quebrar o app:
#   - Entidades Room (mapeadas por reflexão/código gerado)
#   - Enums persistidos no banco por nome (name()/valueOf)
#   - Modelos de domínio (data classes (de)serializadas)
#
# Room, Hilt e Compose já trazem suas próprias regras (consumer rules),
# então não é preciso "keep" amplo para eles.
# ============================================================================

# --- Entidades Room (segurança contra renomeação) ---------------------------
-keep class com.saniblue.app.data.local.entity.** { *; }

# --- Modelos de domínio (data classes usadas em mapeamento/serialização) ----
-keep class com.saniblue.app.domain.model.** { *; }

# --- Enums persistidos via name()/valueOf (não renomear os constantes) -------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    *;
}

# --- Dagger/Hilt: anotações de compilação (errorprone) ausentes em runtime --
# São anotações usadas só pelo compilador; o Dagger as referencia mas elas não
# existem em runtime. -dontwarn evita o R8 tratar "classe ausente" como erro.
-dontwarn com.google.errorprone.annotations.**

# --- ZXing (QR Code) --------------------------------------------------------
-dontwarn com.google.zxing.**
-keep class com.google.zxing.** { *; }

# --- Remove logs em produção (Log.d/v/i) ------------------------------------
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
    public static int i(...);
}
