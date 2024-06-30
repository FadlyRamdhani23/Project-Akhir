package com.tugasakhir.udmrputra.data


data class ChatRoomModel (
    var chatroomId: String,
    var userIdBuyer: String,
    var userIdSeller: String,
    var usernameBuyer: String,
    var usernameSeller: String,
    var userImageBuyer: String,
    var userImageSeller: String,
    var lastMessage: String,
    var lastMessageTimestamp: String,
)