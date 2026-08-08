package com.glazev.playlistmaker.domain.impl

import com.glazev.playlistmaker.domain.api.Resource
import com.glazev.playlistmaker.domain.api.TracksInteractor
import com.glazev.playlistmaker.domain.api.TracksRepository
import java.util.concurrent.ExecutorService

class TracksInteractorImpl(
    private val repository: TracksRepository,
    private val executor: ExecutorService
) : TracksInteractor {

    override fun searchTracks(expression: String, consumer: TracksInteractor.TracksConsumer) {
        executor.execute {
            when (val resource = repository.searchTracks(expression)) {
                is Resource.Success -> {
                    consumer.consume(resource.data, null)
                }
                is Resource.Error -> {
                    consumer.consume(null, resource.message)
                }
            }
        }
    }
}
