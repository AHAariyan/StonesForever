package com.hady.stonesforever

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.hady.stonesforever.data.Repository.GoogleAuthRepositoryImpl
import com.hady.stonesforever.domain.Repository.GoogleAuthRepository
import com.hady.stonesforever.domain.use_cases.GetCurrentUserUseCase
import com.hady.stonesforever.domain.use_cases.SignInWithGoogleUseCase
import com.hady.stonesforever.domain.use_cases.SignOutUseCase
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

    @Provides
    fun provideSignOutUseCase(repo: GoogleAuthRepository): SignOutUseCase =
        SignOutUseCase(repo)

    @Provides
    fun provideGetCurrentUserUseCase(repo: GoogleAuthRepository): GetCurrentUserUseCase =
        GetCurrentUserUseCase(repo)
}

@Composable
fun getActivity(): Activity? {
    val context = LocalContext.current
    return remember(context) {
        generateSequence(context) {
            when (it) {
                is ContextWrapper -> it.baseContext
                else -> null
            }
        }.filterIsInstance<Activity>().firstOrNull()
    }
}
