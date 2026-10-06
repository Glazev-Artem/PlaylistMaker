package com.glazev.playlistmaker.data

import com.glazev.playlistmaker.data.dto.Response

interface NetworkClient {
    suspend fun doRequest(dto: Any): Response
}
