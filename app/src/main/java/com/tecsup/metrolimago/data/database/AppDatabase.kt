package com.tecsup.metrolimago.data.database // Asegúrate que coincida con tu paquete

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// --- ¡VERSIÓN ACTUALIZADA A 6! ---
// Esto forzará la migración destructiva y recargará los datos.
@Database(entities = [TransportLineEntity::class, StationEntity::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lineDao(): LineDao
    abstract fun stationDao(): StationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "metrolimago_db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration() // Borra la DB v5 y crea la v6
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.lineDao(), database.stationDao())
                }
            }
        }

        /**
         * ¡DATOS CORREGIDOS!
         * Coordenadas ajustadas al CENTRO DE LA ESTACIÓN (plataforma).
         */
        suspend fun populateDatabase(lineDao: LineDao, stationDao: StationDao) {
            // --- 1. DATOS DE LÍNEAS ---
            val linea1 = TransportLineEntity("L1", "Línea 1 (Metro)", "#D32F2F", "Operativo")
            val linea2 = TransportLineEntity("L2", "Línea 2 (Metro)", "#FFA000", "En Construcción")
            val corredorAzul = TransportLineEntity("CA", "Corredor Azul", "#303F9F", "Operativo")
            val metropolitano = TransportLineEntity("METRO", "Metropolitano (Troncal)", "#808080", "Operativo")

            lineDao.insertAll(listOf(linea1, linea2, corredorAzul, metropolitano))

            // --- 2. ESTACIONES LÍNEA 1 (SOBRE LA PLATAFORMA) ---
            val l1Stations = listOf(
                StationEntity("L1-VES", "L1", "Villa El Salvador", 0, latitude = -12.207198, longitude = -76.933328), // Centro de la plataforma
                StationEntity("L1-PAR", "L1", "Parque Industrial", 1, latitude = -12.2036, longitude = -76.9320), // Centro de la plataforma
                StationEntity("L1-PUM", "L1", "Pumacahua", 2, latitude = -12.1930, longitude = -76.9387), // Centro de la plataforma
                StationEntity("L1-VMT", "L1", "Villa María", 3, latitude = -12.1814, longitude = -76.9450), // Centro de la plataforma
                StationEntity("L1-MIG", "L1", "Miguel Iglesias", 4, latitude = -12.1660, longitude = -76.9589), // Centro de la plataforma
                StationEntity("L1-SJM", "L1", "San Juan", 5, latitude = -12.1708, longitude = -76.9534), // Centro de la plataforma
                StationEntity("L1-ATO", "L1", "Atocongo", 6, latitude = -12.1611, longitude = -76.9634), // Centro de la plataforma
                StationEntity("L1-JCH", "L1", "Jorge Chávez", 7, latitude = -12.1283, longitude = -77.0000), // Centro de la plataforma
                StationEntity("L1-AYA", "L1", "Ayacucho", 8, latitude = -12.1158, longitude = -77.0039), // Centro de la plataforma
                StationEntity("L1-CAB", "L1", "Cabitos", 9, latitude = -12.1106, longitude = -77.0057), // Centro de la plataforma
                StationEntity("L1-ANG", "L1", "Angamos", 10, latitude = -12.1064, longitude = -77.0076), // Centro de la plataforma
                StationEntity("L1-SBO", "L1", "San Borja Sur", 11, latitude = -12.0967, longitude = -77.0094), // Centro de la plataforma
                StationEntity("L1-CUL", "L1", "La Cultura", 12, latitude = -12.0864, longitude = -77.0034), // Centro de la plataforma
                StationEntity("L1-ARR", "L1", "Arriola", 13, latitude = -12.0734, longitude = -77.0090), // Centro de la plataforma
                StationEntity("L1-GAM", "L1", "Gamarra", 14, latitude = -12.0620, longitude = -77.0135), // Centro de la plataforma
                StationEntity("L1-MGR", "L1", "Miguel Grau", 15, latitude = -12.0545, longitude = -77.0205), // Centro de la plataforma
                StationEntity("L1-ELA", "L1", "El Ángel", 16, latitude = -12.0445, longitude = -77.0198), // Centro de la plataforma
                StationEntity("L1-PRE", "L1", "Presbítero Maestro", 17, latitude = -12.0378, longitude = -77.0185), // Centro de la plataforma
                StationEntity("L1-CAJ", "L1", "Caja de Agua", 18, latitude = -12.0238, longitude = -77.0105), // Centro de la plataforma
                StationEntity("L1-PIR", "L1", "Pirámide del Sol", 19, latitude = -12.0157, longitude = -77.0095), // Centro de la plataforma
                StationEntity("L1-JAR", "L1", "Los Jardines", 20, latitude = -12.0083, longitude = -77.0094), // Centro de la plataforma
                StationEntity("L1-POS", "L1", "Los Postes", 21, latitude = -12.0019, longitude = -77.0093), // Centro de la plataforma
                StationEntity("L1-SCA", "L1", "San Carlos", 22, latitude = -11.9950, longitude = -77.0091), // Centro de la plataforma
                StationEntity("L1-SMA", "L1", "San Martín", 23, latitude = -11.9899, longitude = -77.0094), // Centro de la plataforma
                StationEntity("L1-SRO", "L1", "Santa Rosa", 24, latitude = -11.9825, longitude = -77.0096), // Centro de la plataforma
                StationEntity("L1-BAY", "L1", "Bayóvar", 25, latitude = -11.9765, longitude = -77.0105)  // Centro de la plataforma
            )

            // --- 3. ESTACIONES LÍNEA 2 (Entradas subterráneas) ---
            val l2Stations = listOf(
                StationEntity("L2-ATE", "L2", "Municipalidad de Ate", 0, latitude = -12.0520, longitude = -76.9298),
                StationEntity("L2-MER", "L2", "Mercado Santa Anita", 1, latitude = -12.0537, longitude = -76.9534),
                StationEntity("L2-EVT", "L2", "Evitamiento", 2, latitude = -12.0558, longitude = -76.9830),
                StationEntity("L2-GAM", "L2", "Gamarra", 3, latitude = -12.0622, longitude = -77.0138)
            )

            // --- 4. ESTACIONES CORREDOR AZUL (Paraderos de calle) ---
            val caStations = listOf(
                StationEntity("CA-AMA", "CA", "Amancaes", 0, latitude = -12.0229, longitude = -77.0315),
                StationEntity("CA-TAC", "CA", "Tacna", 1, latitude = -12.0523, longitude = -77.0384),
                StationEntity("CA-AYA", "CA", "Ayacucho", 2, latitude = -12.1160, longitude = -77.0042),
                StationEntity("CA-LAR", "CA", "Larcomar", 3, latitude = -12.1332, longitude = -77.0298)
            )

            // --- 5. ESTACIONES METROPOLITANO (Plataformas centrales) ---
            val metroStations = listOf(
                StationEntity("METRO-NAR", "METRO", "Naranjal (Terminal)", 0, latitude = -11.9904, longitude = -77.0592), // Plataforma
                StationEntity("METRO-UNI", "METRO", "UNI", 1, latitude = -12.0225, longitude = -77.0494), // Plataforma
                StationEntity("METRO-EST", "METRO", "Estación Central", 2, latitude = -12.0592, longitude = -77.0365), // Plataforma
                StationEntity("METRO-JAV", "METRO", "Javier Prado", 3, latitude = -12.0886, longitude = -77.0308), // Plataforma
                StationEntity("METRO-CAN", "METRO", "Canaval y Moreyra", 4, latitude = -12.0991, longitude = -77.0275), // Plataforma
                StationEntity("METRO-ANG", "METRO", "Angamos", 5, latitude = -12.1127, longitude = -77.0235), // Plataforma
                StationEntity("METRO-MAT", "METRO", "Matellini (Terminal)", 6, latitude = -12.1868, longitude = -77.0093) // Plataforma
            )

            stationDao.insertAll(l1Stations + l2Stations + caStations + metroStations)
        }
    }
}