package ru.radiationx.anilibria.model.data.remote

class NetworkResponse(url: String) {
    var code = 0
    var message = ""
    var url = ""
    var redirect = url
    var body = ""

    init {
        this.url = url
    }

    override fun toString(): String {
        return "NetworkResponse{" + code + ", " + message + ", " + url + ", " + redirect + ", " + body.length + "}"
    }
}
