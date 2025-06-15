package com.dzaky3022.asesment1.ui.model

data class User(
    var id: String? = null,
    var nama: String? = null,
    var email: String? = null,
    var photoUrl: String? = null,
) {
    constructor() : this(null)
}