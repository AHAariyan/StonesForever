package com.hady.stonesforever

import android.content.Context
import com.hady.stonesforever.data.Repository.GoogleAuthRepositoryImpl
import com.hady.stonesforever.domain.Repository.GoogleAuthRepository
import com.hady.stonesforever.domain.use_cases.SignInWithGoogleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    fun provideGoogleAuthRepository(
        @ApplicationContext context: Context
    ): GoogleAuthRepository = GoogleAuthRepositoryImpl(
        context = context,
        webClientId = "167639734033-s24rie3nmu5jc87539j729gioi8pl79k.apps.googleusercontent.com"
    )

    @Provides
    fun provideSignInUseCase(repo: GoogleAuthRepository): SignInWithGoogleUseCase =
        SignInWithGoogleUseCase(repo)
}
