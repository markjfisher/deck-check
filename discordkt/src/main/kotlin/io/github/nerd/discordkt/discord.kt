package io.github.nerd.discordkt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.nerd.discordkt.discord.Discord
import org.apache.tika.Tika
import org.slf4j.LoggerFactory

/**
 * @author ashley
 * @since 5/14/17 5:09 PM
 * whoa it's the name of the library!!!
 */
const val PROJ_NAME = "discord.kt"
const val PROJ_VER = 0.3
const val PROJ_AUTHOR = "Ashley Null (nerd)"
const val API_BASEURL = "https://discordapp.com/api/v6/"

val discordLogger = LoggerFactory.getLogger(Discord::class.java)
internal val mapper = ObjectMapper().registerModules(Jdk8Module(),
		JavaTimeModule(),
		KotlinModule())

internal val tika = Tika()