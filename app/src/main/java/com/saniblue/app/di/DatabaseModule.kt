package com.saniblue.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.saniblue.app.data.local.database.DatabasePrePopulate
import com.saniblue.app.data.local.database.SaniblueDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SaniblueDatabase {
        var db: SaniblueDatabase? = null

        db = Room.databaseBuilder(
            context,
            SaniblueDatabase::class.java,
            SaniblueDatabase.DATABASE_NAME
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(database: SupportSQLiteDatabase) {
                    super.onCreate(database)
                    // Pre-populate com dados iniciais
                    CoroutineScope(Dispatchers.IO).launch {
                        val database = db ?: return@launch
                        // Inserir hidrômetros padrão
                        DatabasePrePopulate.getModelosHidrometro().forEach { modelo ->
                            database.hidrometroModeloDao().insert(modelo)
                        }
                        // Inserir usuários padrão
                        DatabasePrePopulate.getUsuariosDefault().forEach { usuario ->
                            database.usuarioDao().insert(usuario)
                        }
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()

        return db
    }

    @Provides
    fun provideUsuarioDao(db: SaniblueDatabase) = db.usuarioDao()

    @Provides
    fun provideHidrometroModeloDao(db: SaniblueDatabase) = db.hidrometroModeloDao()

    @Provides
    fun provideEnsaioDao(db: SaniblueDatabase) = db.ensaioDao()

    @Provides
    fun provideVazaoEnsaioDao(db: SaniblueDatabase) = db.vazaoEnsaioDao()

    @Provides
    fun provideFotoEnsaioDao(db: SaniblueDatabase) = db.fotoEnsaioDao()
}
