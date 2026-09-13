package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SongEntity::class, PlaylistEntity::class, PlaylistItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RaagaDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: RaagaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): RaagaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RaagaDatabase::class.java,
                    "raaga_music_database"
                )
                .addCallback(RaagaDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class RaagaDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.musicDao())
                }
            }
        }

        suspend fun populateInitialData(dao: MusicDao) {
            val defaultSongs = DefaultMusicCatalog.getInitialSongs()
            dao.insertSongs(defaultSongs)

            // Default curated playlists
            val p1Id = dao.insertPlaylist(
                PlaylistEntity(
                    name = "Morning Awakening Ragas",
                    description = "Soulful dawn melodies to center the mind and elevate spirits",
                    colorIndex = 0
                )
            )
            val p2Id = dao.insertPlaylist(
                PlaylistEntity(
                    name = "Deep Focus & Sitar Flow",
                    description = "Continuous meditative acoustic rhythms for deep study and work",
                    colorIndex = 1
                )
            )
            val p3Id = dao.insertPlaylist(
                PlaylistEntity(
                    name = "Evening Golden Sunset",
                    description = "Warm acoustic Yaman and Bhupali twilight harmonies",
                    colorIndex = 2
                )
            )

            // Seed songs into default playlists
            defaultSongs.take(4).forEach { song ->
                dao.addSongToPlaylist(PlaylistItemEntity(p1Id, song.id))
            }
            defaultSongs.drop(2).take(4).forEach { song ->
                dao.addSongToPlaylist(PlaylistItemEntity(p2Id, song.id))
            }
            defaultSongs.takeLast(4).forEach { song ->
                dao.addSongToPlaylist(PlaylistItemEntity(p3Id, song.id))
            }
        }
    }
}
