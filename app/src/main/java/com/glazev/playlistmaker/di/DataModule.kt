package com.glazev.playlistmaker.di

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import com.glazev.playlistmaker.App
import com.glazev.playlistmaker.R
import com.glazev.playlistmaker.data.NetworkClient
import com.glazev.playlistmaker.data.network.ITunesApi
import com.glazev.playlistmaker.data.network.RetrofitNetworkClient
import com.glazev.playlistmaker.data.sharing.ExternalNavigatorImpl
import com.glazev.playlistmaker.domain.api.ExternalNavigator
import com.glazev.playlistmaker.domain.models.EmailData
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val SHARE_APP_LINK_QUALIFIER = "share_app_link"
const val TERMS_LINK_QUALIFIER = "terms_link"

val dataModule = module {

    single<ITunesApi> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApi::class.java)
    }

    single<SharedPreferences> {
        androidContext().getSharedPreferences(
            App.PLAYLIST_MAKER_PREFERENCES,
            Context.MODE_PRIVATE
        )
    }

    factory { Gson() }

    single<NetworkClient> {
        RetrofitNetworkClient(get())
    }

    factory { MediaPlayer() }

    single<ExternalNavigator> {
        ExternalNavigatorImpl(androidContext())
    }

    factory(named(SHARE_APP_LINK_QUALIFIER)) {
        androidContext().getString(R.string.share_app_link)
    }

    factory(named(TERMS_LINK_QUALIFIER)) {
        androidContext().getString(R.string.agreement_link)
    }

    factory {
        EmailData(
            email = androidContext().getString(R.string.support_email),
            subject = androidContext().getString(R.string.support_subject),
            body = androidContext().getString(R.string.support_body)
        )
    }
}
