package io.github.nerd.discordkt.discord.util

import io.github.nerd.discordkt.discord.auth.Authentication
import io.github.nerd.discordkt.discord.model.response.ModelRateLimitResponse
import io.github.nerd.discordkt.discordLogger
import io.github.nerd.discordkt.mapper
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.ConcurrentHashMap

/**
 * @author ashley
 * @since 6/2/17 11:41 AM
 */
internal class DiscordInterceptor(val auth: Authentication) : Interceptor {
	val map = ConcurrentHashMap<String, RateLimitData>()
	val majorParamsRegex = Regex("(guilds|channels)/[0-9]*")

	override fun intercept(p0: Interceptor.Chain?): Response {
		synchronized(map) {
			p0?.let {
				val authenticatedRequest = it.request().newBuilder().addHeader("Authorization", auth.token).build()
				val major = majorParamsRegex.find(it.request().url().toString())?.value
				if (null != major && null != map[major]) {
					var requestsRemaining = map[major]!!.callsRemaining
					var msToSleep = map[major]!!.reset - System.currentTimeMillis()
					while (requestsRemaining <= 1 && msToSleep > 0) {
						Thread.sleep(msToSleep)
						msToSleep = map[major]!!.reset - System.currentTimeMillis()
						requestsRemaining = map[major]!!.callsRemaining
					}
				}

				val response = it.proceed(authenticatedRequest)
				if (response.code() == 429) {
					try {
						val rateLimitResponse = mapper.readValue(response.body()!!.string(), ModelRateLimitResponse::class.java)
						discordLogger.debug("Rate limited / Retrying after ${rateLimitResponse.retryAfter}ms")
						Thread.sleep(rateLimitResponse.retryAfter)
						return it.proceed(authenticatedRequest)
					} catch (e: Exception) {
						discordLogger.error("Error handling rate limits", e)
					}
				}

				if (null != major
						&& null != response.headers()[rateLimitRequestsRemainingHeader]
						&& null != response.headers()[rateLimitResetHeader]) {
					val requestsLeft = response.headers()[rateLimitRequestsRemainingHeader]!!.toInt()
					val rateLimitReset = response.headers()[rateLimitResetHeader]!!
					val rateLimitResetMs = rateLimitReset.toLong() * 1000
					map.put(major, RateLimitData(major, rateLimitResetMs, requestsLeft))
				}
				return response
			}
		}
		return p0!!.proceed(p0.request())
	}
}

internal const val rateLimitHeader = "X-RateLimit-Limit"
internal const val rateLimitRequestsRemainingHeader = "X-RateLimit-Remaining"
internal const val rateLimitResetHeader = "X-RateLimit-Reset"

internal data class RateLimitData(val endpoint: String, var reset: Long, var callsRemaining: Int)