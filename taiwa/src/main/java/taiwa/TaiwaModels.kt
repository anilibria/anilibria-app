package taiwa


sealed interface TaiwaAnchor {
    data object Root : TaiwaAnchor
    data class Id(val any: Any) : TaiwaAnchor
}

sealed interface TaiwaEvent {
    data object Close : TaiwaEvent
    data class Anchor(val anchor: TaiwaAnchor) : TaiwaEvent
}

sealed interface TaiwaAction {
    data object Close : TaiwaAction
    data object Root : TaiwaAction
    data class Anchor(val anchor: TaiwaAnchor.Id) : TaiwaAction
}