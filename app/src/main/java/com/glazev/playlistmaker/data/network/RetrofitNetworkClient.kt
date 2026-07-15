package com.glazev.playlistmaker.data.network

import com.glazev.playlistmaker.data.NetworkClient
import com.glazev.playlistmaker.data.dto.Response
import com.glazev.playlistmaker.data.dto.TracksSearchRequest
import java.io.IOException

class RetrofitNetworkClient(private val iTunesService: ITunesApi) : NetworkClient {

    override fun doRequest(dto: Any): Response {
        if (dto is TracksSearchRequest) {
            return try {
                val resp = iTunesService.search(dto.term).execute()
                val body = resp.body() ?: Response()
                body.apply { resultCode = resp.code() }
            } catch (e: IOException) {
                Response().apply { resultCode = -1 }
            }
        } else {
            return Response().apply { resultCode = 400 }
        }
    }
}
