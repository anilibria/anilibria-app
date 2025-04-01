// MyGuidanceStylist.kt
package ru.radiationx.anilibria.screen.auth.otp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.leanback.widget.GuidanceStylist
import ru.radiationx.anilibria.R

/**
 * Кастомный стилист, чтобы использовать my_guidance.xml вместо lb_guidance,
 * и иметь доступ к своему qrImageView.
 */
class MyGuidanceStylist : GuidanceStylist() {

    // Ссылка на ImageView, чтобы фрагмент мог поставить в неё bitmap
    var qrImageView: ImageView? = null

    // Подменяем стандартный layout (lb_guidance) на наш (my_guidance)
    override fun onProvideLayoutId(): Int {
        return R.layout.my_guidance
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        guidance: Guidance
    ): View {
        // Вызовем базовый метод, чтобы он "надул" (inflate) мой макет
        val view = super.onCreateView(inflater, container, guidance)

        // Находим по ID наш ImageView (qr_image)
        qrImageView = view.findViewById(R.id.qr_image)

        // Всё готово, возвращаем результат
        return view
    }
}
