package com.ajpr00.visumloop.tablet.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AccesLoginType{
    DRIVE,
     LOCAL,
     DROPBOX,
     FTP,
}
