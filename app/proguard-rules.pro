# R8 em full mode (padrao no AGP 8). Regras abaixo cobrem o que a analise
# estatica nao consegue enxergar sozinha.

# Modelos de dominio e classes geradas pelo Apollo sao serializados por nome.
-keep class com.mej.rickmorty.domain.model.** { *; }
-keep class com.mej.rickmorty.graphql.** { *; }
-dontwarn com.apollographql.apollo.**

# Koin resolve por reflexao os construtores registrados nos modulos.
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keepclassmembers class * { public <init>(...); }

# Kotlin coroutines: campos usados internamente por atomic field updaters.
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Metadados de Kotlin necessarios para reflexao de data class e enums.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepclassmembers enum * { *; }

# Remove log de debug do binario de producao.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Facilita ler crashes ofuscados junto do mapping.txt.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
