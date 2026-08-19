package com.hermex.di

import android.content.Context
import com.hermex.data.gateway.ConnectionStore
import com.hermex.data.gateway.HermesGatewayTransport
import com.hermex.data.gateway.NativeHermesAgentGateway
import com.hermex.domain.chat.AgentGateway
import com.hermex.security.SecureStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OptimusModule {
    @Provides
    @Singleton
    fun provideSecureStore(@ApplicationContext context: Context): SecureStore = SecureStore(context)

    @Provides
    @Singleton
    fun provideConnectionStore(
        @ApplicationContext context: Context,
        secureStore: SecureStore
    ): ConnectionStore = ConnectionStore(context, secureStore)

    @Provides
    @Singleton
    fun provideHermesTransport(): HermesGatewayTransport = HermesGatewayTransport()

    @Provides
    @Singleton
    fun provideAgentGateway(
        connectionStore: ConnectionStore,
        transport: HermesGatewayTransport
    ): AgentGateway = NativeHermesAgentGateway(connectionStore, transport)
}
