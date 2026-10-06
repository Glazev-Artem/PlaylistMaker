package com.glazev.playlistmaker.data.network

import com.glazev.playlistmaker.data.NetworkClient
import com.glazev.playlistmaker.data.dto.Response
import com.glazev.playlistmaker.data.dto.TracksSearchRequest
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

class RetrofitNetworkClient(private val iTunesService: ITunesApi) : NetworkClient {

    override suspend fun doRequest(dto: Any): Response {
        if (dto !is TracksSearchRequest) {
            return Response().apply { resultCode = 400 }
        }

        return try {
            iTunesService.search(dto.term).apply { resultCode = 200 }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Response().apply { resultCode = -1 }
        } catch (e: HttpException) {
            Response().apply { resultCode = e.code() }
        } catch (e: Exception) {
            Response().apply { resultCode = 500 }
        }
    }
}
