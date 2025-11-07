package com.tecsup.metrolimago.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransportLineEntity::class,
        StationEntity::class,
        FavoriteStationEntity::class,
        FavoriteRouteEntity::class // <--- AGREGADO
    ],
    version = 3, // <--- SUBE LA VERSIÓN
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lineDao(): LineDao
    abstract fun stationDao(): StationDao
    abstract fun favoriteStationDao(): FavoriteStationDao
    abstract fun favoriteRouteDao(): FavoriteRouteDao   // <--- AGREGADO

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
                    .fallbackToDestructiveMigration()
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
         * ¡AQUÍ ESTÁ LA CORRECCIÓN DE DATOS!
         * Añadimos coordenadas reales a cada estación.
         */
        suspend fun populateDatabase(lineDao: LineDao, stationDao: StationDao) {
            // --- DATOS DE LÍNEAS ---
            val linea1 = TransportLineEntity("L1", "Línea 1 (Metro)", "#D32F2F", "Operativo")
            val linea2 = TransportLineEntity("L2", "Línea 2 (Metro)", "#FFA000", "En Construcción")
            val corredorAzul = TransportLineEntity("CA", "Corredor Azul", "#303F9F", "Operativo")
            lineDao.insertAll(listOf(linea1, linea2, corredorAzul))

            // --- DATOS DE ESTACIONES (LÍNEA 1 Oficial) ---
            val l1Stations = listOf(
                StationEntity(
                    "L1-VES",
                    "L1",
                    "Villa El Salvador",
                    0,
                    latitude = -12.2155,
                    longitude = -76.9248
                ),
                StationEntity(
                    "L1-PAR",
                    "L1",
                    "Parque Industrial",
                    1,
                    latitude = -12.2036,
                    longitude = -76.9320
                ),
                StationEntity(
                    "L1-PUM",
                    "L1",
                    "Pumacahua",
                    2,
                    latitude = -12.1930,
                    longitude = -76.9387
                ),
                StationEntity(
                    "L1-VMT",
                    "L1",
                    "Villa María",
                    3,
                    latitude = -12.1815,
                    longitude = -76.9453
                ),
                StationEntity(
                    "L1-SJM",
                    "L1",
                    "San Juan",
                    4,
                    latitude = -12.1711,
                    longitude = -76.9535
                ),
                StationEntity(
                    "L1-ATO",
                    "L1",
                    "Atocongo",
                    5,
                    latitude = -12.1613,
                    longitude = -76.9634
                ),
                StationEntity(
                    "L1-JCH",
                    "L1",
                    "Jorge Chávez",
                    6,
                    latitude = -12.1284,
                    longitude = -77.0000
                ),
                StationEntity(
                    "L1-AYA",
                    "L1",
                    "Ayacucho",
                    7,
                    latitude = -12.1158,
                    longitude = -77.0039
                ),
                StationEntity(
                    "L1-ANG",
                    "L1",
                    "Angamos",
                    8,
                    latitude = -12.1065,
                    longitude = -77.0076
                ),
                StationEntity(
                    "L1-SBO",
                    "L1",
                    "San Borja Sur",
                    9,
                    latitude = -12.0968,
                    longitude = -77.0094
                ),
                StationEntity(
                    "L1-CUL",
                    "L1",
                    "La Cultura",
                    10,
                    latitude = -12.0865,
                    longitude = -77.0039
                ),
                StationEntity(
                    "L1-GAM",
                    "L1",
                    "Gamarra",
                    11,
                    latitude = -12.0620,
                    longitude = -77.0135
                ),
                StationEntity(
                    "L1-MGR",
                    "L1",
                    "Miguel Grau",
                    12,
                    latitude = -12.0545,
                    longitude = -77.0205
                ),
                StationEntity(
                    "L1-BAY",
                    "L1",
                    "Bayóvar",
                    13,
                    latitude = -11.9866,
                    longitude = -77.0094
                )
            )

            // --- DATOS DE ESTACIONES (LÍNEA 2 - SIMULADA) ---
            val l2Stations = listOf(
                StationEntity(
                    "L2-ATE",
                    "L2",
                    "Municipalidad de Ate",
                    0,
                    latitude = -12.0520,
                    longitude = -76.9298
                ),
                StationEntity(
                    "L2-MER",
                    "L2",
                    "Mercado Santa Anita",
                    1,
                    latitude = -12.0537,
                    longitude = -76.9534
                ),
                StationEntity(
                    "L2-EVT",
                    "L2",
                    "Evitamiento",
                    2,
                    latitude = -12.0558,
                    longitude = -76.9830
                ),
                StationEntity(
                    "L2-GAM",
                    "L2",
                    "Gamarra",
                    3,
                    latitude = -12.0622,
                    longitude = -77.0138
                )
            )

            // --- DATOS DE ESTACIONES (CORREDOR AZUL - SIMULADO) ---
            val caStations = listOf(
                StationEntity(
                    "CA-AMA",
                    "CA",
                    "Amancaes",
                    0,
                    latitude = -12.0229,
                    longitude = -77.0315
                ),
                StationEntity(
                    "CA-TAC",
                    "CA",
                    "Tacna",
                    1,
                    latitude = -12.0523,
                    longitude = -77.0384
                ),
                StationEntity(
                    "CA-AYA",
                    "CA",
                    "Ayacucho",
                    2,
                    latitude = -12.1160,
                    longitude = -77.0042
                ), // Cerca de L1
                StationEntity(
                    "CA-LAR",
                    "CA",
                    "Larcomar",
                    3,
                    latitude = -12.1332,
                    longitude = -77.0298
                )
            )

            stationDao.insertAll(l1Stations + l2Stations + caStations)
        }
    }
}
