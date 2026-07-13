package com.glazev.playlistmaker.data

import com.glazev.playlistmaker.data.dto.Response

interface NetworkClient {
    fun doRequest(dto: Any): Response
}
