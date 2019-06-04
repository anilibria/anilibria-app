package ru.radiationx.anilibria.model.system

class WrongHostException(host: String) : Exception("Неверный IP адрес сервера: '$host'")