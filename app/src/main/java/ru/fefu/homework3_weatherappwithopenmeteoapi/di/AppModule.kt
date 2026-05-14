package ru.fefu.homework3_weatherappwithopenmeteoapi.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.AppDatabase
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.localDb.FavouriteDao
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.GeocodingApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.data.remote.WeatherApi
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepository
import ru.fefu.homework3_weatherappwithopenmeteoapi.domain.repository.WeatherRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "weather_db").build()

    @Provides
    @Singleton
    fun provideFavouriteDao(db: AppDatabase): FavouriteDao = db.favouriteDao()

    @Provides
    @Singleton
    fun provideGeocodingApi(): GeocodingApi = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeocodingApi::class.java)

    @Provides
    @Singleton
    fun provideWeatherApi(): WeatherApi = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    @Provides
    @Singleton
    fun provideWeatherRepository(
        geocodingApi: GeocodingApi,
        weatherApi: WeatherApi,
        favouriteDao: FavouriteDao
    ): WeatherRepository = WeatherRepositoryImpl(geocodingApi, weatherApi, favouriteDao)
}