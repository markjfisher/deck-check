package io.github.nerd.discordkt.discord.util

// formatting
fun String.bold() = "**$this**"

fun String.italic() = "*$this*"
fun String.code() = "`$this`"
fun String.codeBlock(language: String = "") = "```$language\n$this```"
fun String.underline() = "__${this}__"
fun String.strikeout() = "~~$this~~"