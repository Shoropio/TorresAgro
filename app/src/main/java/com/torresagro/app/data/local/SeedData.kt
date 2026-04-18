package com.torresagro.app.data.local

import androidx.room.withTransaction
import com.torresagro.app.data.local.entity.ActivityRecordEntity
import com.torresagro.app.data.local.entity.CropObservationEntity
import com.torresagro.app.data.local.entity.CropTaskEntity
import com.torresagro.app.data.local.entity.HarvestRecordEntity
import com.torresagro.app.data.local.entity.InventoryItemEntity
import com.torresagro.app.data.local.entity.ParcelEntity
import com.torresagro.app.data.local.entity.SyncQueueEntity

object SeedData {
    suspend fun populateIfEmpty(database: AgroDatabase) {
        if (database.agroDao().parcelCount() > 0) return

        database.withTransaction {
            val dao = database.agroDao()
            dao.upsertParcels(
                listOf(
                    ParcelEntity("p1", "Lote La Esperanza", "San Juan de la Maguana", 1.8, "Cassava", "Valencia", "2026-02-10", "2026-12-10", null, null, null, true),
                    ParcelEntity("p2", "Parcela El Mango", "Moca", 1.2, "SweetPotato", "Beauregard", "2026-03-05", "2026-08-02", null, null, null, false),
                    ParcelEntity("p3", "Loma Verde", "La Vega", 2.0, "Corn", "Maiz amarillo ICTA", "2026-01-20", "2026-05-25", null, null, null, false),
                    ParcelEntity("p4", "Las Palmas", "Bonao", 0.9, "Yam", "Diamantes", "2026-02-28", "2026-10-25", null, null, null, false)
                )
            )
            dao.upsertTasks(
                listOf(
                    CropTaskEntity("t1", "p1", "Aplicar deshierbe temprano", "2026-04-17", "Weeding", false, "Alta", true),
                    CropTaskEntity("t2", "p2", "Revisar humedad del surco", "2026-04-18", "Irrigation", false, "Alta", true),
                    CropTaskEntity("t3", "p3", "Fertilizacion de cobertura", "2026-04-20", "Fertilization", false, "Media", true),
                    CropTaskEntity("t4", "p4", "Monitoreo de hojas amarillas", "2026-04-21", "Monitoring", false, "Media", true),
                    CropTaskEntity("t5", "p1", "Preparar plan de cosecha", "2026-11-20", "Harvest", false, "Baja", true)
                )
            )
            dao.upsertActivities(
                listOf(
                    ActivityRecordEntity("a1", "p1", "Sowing", "2026-02-10", 180.0, "1200 estacas", "Siembra con surco alto.", null),
                    ActivityRecordEntity("a2", "p2", "Irrigation", "2026-04-15", 25.0, "4 horas", "Riego liviano por falta de lluvia.", null),
                    ActivityRecordEntity("a3", "p3", "Fertilization", "2026-04-11", 140.0, "3 quintales", "Se aplico mezcla NPK al voleo.", null),
                    ActivityRecordEntity("a4", "p4", "Labor", "2026-04-13", 70.0, "2 jornales", "Aporque y limpieza de pasillos.", null)
                )
            )
            dao.upsertObservations(
                listOf(
                    CropObservationEntity("o1", "p1", "2026-04-16", "Desarrollo vegetativo", "Bueno", "Sin sintomas graves", "Mantener control de maleza y revisar drenaje.", null),
                    CropObservationEntity("o2", "p2", "2026-04-15", "Formacion de guias", "Regular", "Hojas algo caidas", "Verificar humedad del suelo y evitar encharcamiento.", null),
                    CropObservationEntity("o3", "p4", "2026-04-14", "Desarrollo inicial", "Alerta", "Hojas amarillas", "Revisar fertilidad, drenaje y danos en raiz o tallo.", null)
                )
            )
            dao.upsertInventory(
                listOf(
                    InventoryItemEntity("i1", "Estacas de yuca", "Material de siembra", 350.0, "unidad", 200.0),
                    InventoryItemEntity("i2", "Fertilizante NPK 15-15-15", "Fertilizante", 6.0, "sacos", 3.0),
                    InventoryItemEntity("i3", "Machete", "Herramienta", 4.0, "unidad", 2.0),
                    InventoryItemEntity("i4", "Bioinsumo foliar", "Proteccion", 1.0, "galon", 2.0)
                )
            )
            dao.upsertHarvests(
                listOf(
                    HarvestRecordEntity("h1", "p1", "Cassava", 0.0, 420.0, 0.0),
                    HarvestRecordEntity("h2", "p2", "SweetPotato", 0.0, 260.0, 0.0),
                    HarvestRecordEntity("h3", "p3", "Corn", 1250.0, 510.0, 920.0),
                    HarvestRecordEntity("h4", "p4", "Yam", 0.0, 390.0, 0.0)
                )
            )
            dao.enqueueSync(
                listOf(
                    SyncQueueEntity("s1", "parcel", "p1", "UPSERT", "2026-04-17T08:00:00"),
                    SyncQueueEntity("s2", "task", "t1", "UPSERT", "2026-04-17T08:05:00")
                )
            )
        }
    }
}
