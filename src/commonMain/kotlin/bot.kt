package io.github.dragneelfps.kbot

import io.github.dragneelfps.kbot.Poller.Companion.DEFAULT_POLL_DELAY_SECONDS
import io.github.dragneelfps.kbot.network.TClient

class Bot(private val poller: Poller?) {

    suspend fun startPolling() {
        poller?.startPolling()
    }

    fun stopPolling() {
        poller?.stopPolling()
    }
}

class BotConfig {
    lateinit var token: String
    var usePolling = true

    // TODO: 9/19/2020 Switch to Duration when kotlinx.time becomes stable
    var pollDelaySeconds = DEFAULT_POLL_DELAY_SECONDS

    private var listenerConfig: ListenerConfig.() -> Unit = {}

    fun listeners(config: ListenerConfig.() -> Unit) {
        listenerConfig = config
    }

    fun build(): Bot {
        val tClient = TClient(
            token = token
        )
        val listeners = ListenerConfig(tClient = tClient).apply(listenerConfig).build()
        return Bot(
            poller = if (usePolling) {
                Poller(
                    tClient = tClient,
                    pollDelaySeconds = pollDelaySeconds,
                    listeners = listeners
                )
            } else null
        )
    }
}

class ListenerConfig(val tClient: TClient) {
    private val listeners = mutableListOf<Listener>()

    fun addListener(listener: Listener) {
        listeners += listener
    }

    fun build() = listeners
}

fun bot(block: BotConfig.() -> Unit): Bot {
    return BotConfig().apply(block).build()
}
