package net.snaglobal.trile.wizeye.data.repository

import android.arch.lifecycle.LiveData
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import net.snaglobal.trile.wizeye.AppExecutors
import net.snaglobal.trile.wizeye.data.remote.RemoteDataSource
import net.snaglobal.trile.wizeye.data.remote.model.*
import net.snaglobal.trile.wizeye.data.remote.websocket.WebSocketRequest
import net.snaglobal.trile.wizeye.data.room.RoomDataSource
import net.snaglobal.trile.wizeye.data.room.entity.LoginCredentialEntity
import net.snaglobal.trile.wizeye.utils.Optional
import java.util.*

/**
 * Single Data Source of Truth of the whole Application.
 *
 * @author lmtri
 * @since Oct 16, 2018 at 3:35 PM
 */
class DataRepository(
        private val roomDataSource: RoomDataSource,
        private val remoteDataSource: RemoteDataSource,
        private val executors: AppExecutors
) {

    fun login(domain: String, username: String, password: String): Single<LoginResponse?> =
            Single.defer {
                Single.just(
                        remoteDataSource.login(domain, username, password)
                )
            }.subscribeOn(
                    Schedulers.from(executors.networkIO())
            ).observeOn(
                    Schedulers.from(executors.diskIO())
            ).flatMap {
                roomDataSource.loginCredentialDao()
                        .insertLoginCredential(
                                LoginCredentialEntity(
                                        domain, username, password, it.token, Date(), true
                                )
                        )
                roomDataSource.loginCredentialDao()
                        .updateLoginCredential(
                                LoginCredentialEntity(
                                        domain, username, password, it.token, Date(), true
                                )
                        )
                return@flatMap Single.just(it)
            }

    fun logout(): Single<Boolean> = getLastLoggedInCredential()
            .map {
                it.value?.run {
                    if (isLoggedIn) {
                        val numOfRowsUpdated = roomDataSource.loginCredentialDao()
                                .updateLoginCredential(
                                        LoginCredentialEntity(
                                                domain, userId, password,
                                                token, Date(), false
                                        )
                                )
                        numOfRowsUpdated == 1
                    } else {
                        false
                    }
                } ?: kotlin.run {
                    false
                }
            }

    fun checkIfLoggedInOnAppOpen(): Single<Boolean> =
            Single.defer {
                Single.just(
                        Optional.ofNullable(
                                roomDataSource.loginCredentialDao().getLastLoggedInCredential()
                        )
                )
            }.subscribeOn(
                    Schedulers.from(executors.diskIO())
            ).observeOn(
                    Schedulers.from(executors.diskIO())
            ).flatMap {
                it.value?.let { loginCredentialEntity ->
                    return@flatMap Single.just(loginCredentialEntity.isLoggedIn)
                } ?: kotlin.run {
                    return@flatMap Single.just(false)
                }
            }

    fun getMapList(serverUrl: String, request: WebSocketRequest): LiveData<List<MapListItem>> =
            remoteDataSource.getMapList(serverUrl, request)

    fun getMapDetail(serverUrl: String, request: WebSocketRequest): LiveData<MapDetail?> =
            remoteDataSource.getMapDetail(serverUrl, request)

    fun getVideoList(serverUrl: String, request: WebSocketRequest): LiveData<List<VideoListItem>> =
            remoteDataSource.getVideoList(serverUrl, request)

    fun getVideoDetail(serverUrl: String, request: WebSocketRequest): LiveData<VideoDetail?> =
            remoteDataSource.getVideoDetail(serverUrl, request)

    fun getLastLoggedInCredential(): Single<Optional<LoginCredentialEntity>> =
            Single.defer {
                Single.just(
                        Optional.ofNullable(
                                roomDataSource.loginCredentialDao().getLastLoggedInCredential()
                        )
                )
            }.subscribeOn(
                    Schedulers.from(executors.diskIO())
            ).observeOn(
                    Schedulers.from(executors.diskIO())
            )


    companion object {
        @Volatile private var instance: DataRepository? = null

        fun getInstance(
                roomDataSource: RoomDataSource,
                remoteDataSource: RemoteDataSource,
                executors: AppExecutors
        ): DataRepository =
                instance ?: synchronized(this) {
                    instance ?: DataRepository(roomDataSource, remoteDataSource, executors).also {
                        instance = it
                    }
                }
    }
}