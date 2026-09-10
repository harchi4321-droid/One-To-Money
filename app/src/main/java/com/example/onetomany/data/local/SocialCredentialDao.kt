package com.example.onetomany.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialCredentialDao {
    @Query("SELECT * FROM social_credentials WHERE userId = :userId")
    fun getCredentialsForUserFlow(userId: String): Flow<List<SocialCredentialEntity>>

    @Query("SELECT * FROM social_credentials WHERE userId = :userId")
    suspend fun getCredentialsForUser(userId: String): List<SocialCredentialEntity>

    @Query("SELECT * FROM social_credentials WHERE userId = :userId AND platformId = :platformId LIMIT 1")
    suspend fun getCredential(userId: String, platformId: String): SocialCredentialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: SocialCredentialEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(credentials: List<SocialCredentialEntity>)

    @Query("DELETE FROM social_credentials WHERE userId = :userId AND platformId = :platformId")
    suspend fun deleteCredential(userId: String, platformId: String)
}
