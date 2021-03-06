package net.snaglobal.trile.wizeye.data.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import net.snaglobal.trile.wizeye.data.room.dao.LoginCredentialDao
import net.snaglobal.trile.wizeye.data.room.entity.LoginCredentialEntity
import net.snaglobal.trile.wizeye.data.room.entity.RoomDateConverter
import net.snaglobal.trile.wizeye.utils.SingletonHolder

/**
 * Provides an API for handling all local database operations.
 *
 * @author lmtri
 * @since Oct 19, 2018 at 11:23 AM
 */
@Database(
        entities = [LoginCredentialEntity::class],
        version = 1
)
@TypeConverters(value = [RoomDateConverter::class])
abstract class RoomDataSource : RoomDatabase() {

    abstract fun loginCredentialDao(): LoginCredentialDao

    companion object : SingletonHolder<RoomDataSource, Context>({
        Room.databaseBuilder(
                it.applicationContext,
                RoomDataSource::class.java,
                "wizeye.db"
        ).build()
    })
}