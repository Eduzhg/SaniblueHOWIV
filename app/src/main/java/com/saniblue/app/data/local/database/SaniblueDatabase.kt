package com.saniblue.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.saniblue.app.data.local.dao.EnsaioDao
import com.saniblue.app.data.local.dao.FotoEnsaioDao
import com.saniblue.app.data.local.dao.HidrometroModeloDao
import com.saniblue.app.data.local.dao.UsuarioDao
import com.saniblue.app.data.local.dao.VazaoEnsaioDao
import com.saniblue.app.data.local.entity.EnsaioEntity
import com.saniblue.app.data.local.entity.FotoEnsaioEntity
import com.saniblue.app.data.local.entity.HidrometroModeloEntity
import com.saniblue.app.data.local.entity.UsuarioEntity
import com.saniblue.app.data.local.entity.VazaoEnsaioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        HidrometroModeloEntity::class,
        EnsaioEntity::class,
        VazaoEnsaioEntity::class,
        FotoEnsaioEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class SaniblueDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun hidrometroModeloDao(): HidrometroModeloDao
    abstract fun ensaioDao(): EnsaioDao
    abstract fun vazaoEnsaioDao(): VazaoEnsaioDao
    abstract fun fotoEnsaioDao(): FotoEnsaioDao

    companion object {
        const val DATABASE_NAME = "saniblue_metrologia.db"
    }
}
