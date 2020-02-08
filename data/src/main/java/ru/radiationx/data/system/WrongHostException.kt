package ru.radiationx.data.system

class WrongHostException(host: String) : Exception("Неверный IP адрес сервера: '$host'")