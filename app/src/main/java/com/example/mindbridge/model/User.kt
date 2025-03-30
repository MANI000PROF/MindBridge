package com.example.mindbridge.model

data class User(
    var uid: String? = "",
    var name: String? = "",
    var phoneNumber: String? = "",
    var profileImage: String? = "",
    var age: String? = "",                // Added age
    var qualification: String? = "",      // Added qualification
    var interestedIn: String? = ""        // Added interests
) {
    // Default constructor for Firebase
    constructor() : this("", "", "", "", "", "")
}
