package ru.radiationx.shared_app.controllers.actionmulti

fun <ARG, RESULT> MultiActionExecutor<ARG, ARG, RESULT>.executeAsKey(arg: ARG) {
    execute(arg, arg)
}