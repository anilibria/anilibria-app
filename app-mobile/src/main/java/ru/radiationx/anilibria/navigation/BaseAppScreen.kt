package ru.radiationx.anilibria.navigation

import com.github.terrakok.cicerone.androidx.ActivityScreen
import com.github.terrakok.cicerone.androidx.FragmentScreen
import java.io.Serializable

abstract class BaseFragmentScreen : FragmentScreen, Serializable

abstract class BaseActivityScreen : ActivityScreen, Serializable