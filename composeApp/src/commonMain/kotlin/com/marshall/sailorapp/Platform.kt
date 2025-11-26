package com.marshall.sailorapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform