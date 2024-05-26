package com.tugasakhir.udmrputra.data

class Users {
    var id: String? = null
    var nama: String? = null
    var email: String? = null
    var noHp: String? = null

    constructor() {}
    constructor(id: String?, nama: String?, email: String?, noHp: String?) {
        this.id = id
        this.nama = nama
        this.noHp = noHp
        this.email = email

    }
}
