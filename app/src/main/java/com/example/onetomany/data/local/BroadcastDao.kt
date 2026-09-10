package com.example.onetomany.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BroadcastDao {
    @Query("SELECT * FROM broadcast_posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsForUserFlow(userId: String): Flow<List<BroadcastPostEntity>>

    @Query("SELECT * FROM broadcast_posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: String): BroadcastPostEntity?

    @Query("SELECT * FROM broadcast_posts WHERE isSyncedToOnlineDb = 0")
    suspend fun getUnsyncedPosts(): List<BroadcastPostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: BroadcastPostEntity)

    @Update
    suspend fun updatePost(post: BroadcastPostEntity)

    @Query("DELETE FROM broadcast_posts WHERE id = :id")
    suspend fun deletePost(id: String)

    @Query("UPDATE broadcast_posts SET isSyncedToOnlineDb = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}
