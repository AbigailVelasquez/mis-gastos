# Mis Gastos

App Android sencilla (Java, sin dependencias externas, sin Internet).

## Generar el APK
1. Instala Android Studio (https://developer.android.com/studio).
2. File > Open y elige esta carpeta (MisGastos). Espera a que termine "Gradle Sync".
3. Menú Build > Build Bundle(s) / APK(s) > Build APK(s).
4. El APK queda en: app/build/outputs/apk/debug/app-debug.apk

## Estructura
- app/src/main/java/com/example/misgastos/MainActivity.java  -> toda la lógica
- app/src/main/res/layout/activity_main.xml                  -> pantalla principal
- app/src/main/res/layout/item_gasto.xml                     -> una fila de la lista
- app/src/main/res/values/colors.xml                         -> colores
