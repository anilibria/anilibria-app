package tv.aniliberty.next

/** Nested drawer pages share one pause; only leaving the drawer resumes playback. */
internal class PanelPlayback {
    private var opened = false

    fun open(pause: () -> Unit) {
        if (!opened) {
            opened = true
            pause()
        }
    }

    fun close(resume: Boolean = true, play: () -> Unit) {
        if (opened) {
            opened = false
            if (resume) play()
        }
    }
}
