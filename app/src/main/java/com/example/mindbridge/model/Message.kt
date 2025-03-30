package com.example.mindbridge.model

class Message {
    var messageId: String? = null
    var senderId: String? = null
    var receiverId: String? = null
    var message: String? = null
    var timestamp: Long? = 0
    var imageUrl: String? = null
    var replyToId: String? = null // New field for tracking replies

    constructor() {}

    constructor(
        message: String?,
        senderId: String?,
        timestamp: Long?,
        replyToId: String? = null // Initialize this field as well
    ) {
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp
        this.replyToId = replyToId // Set the replyToId if provided
    }
}
