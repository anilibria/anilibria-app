package ru.radiationx.data.analytics.profile

import android.content.Context
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import ru.radiationx.shared.ktx.coRunCatching
import javax.inject.Inject

class AnalyticsInstallerDataSource @Inject constructor(
    private val context: Context
) {

    fun getAttributes(): List<ProfileAttribute.String> {
        val installerInfo = coRunCatching {
            getInstallerInfo()
        }.getOrElse {
            InstallerInfo()
        }
        return listOf(
            installerInfo.installing.toNullableAttr(ProfileConstants.installer_installing),
            installerInfo.source.toNullableAttr(ProfileConstants.installer_source),
            installerInfo.initiating.toNullableAttr(ProfileConstants.installer_initiating),
            installerInfo.originating.toNullableAttr(ProfileConstants.installer_originating),
            installerInfo.updateOwner.toNullableAttr(ProfileConstants.installer_update_owner),
        )
    }

    private fun String?.toNullableAttr(name: String): ProfileAttribute.String {
        return ProfileAttribute.String(name, this ?: "<unknown>")
    }

    private fun getInstallerInfo(): InstallerInfo {
        val packageManager: PackageManager = context.packageManager

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val installer = packageManager.getInstallerPackageName(context.packageName)
            return InstallerInfo(installing = installer)
        }

        val sourceInfo = packageManager.getInstallSourceInfo(context.packageName)

        val packageSource = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mapPackageSource(sourceInfo.packageSource)
        } else {
            null
        }
        val updateOwner = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            sourceInfo.updateOwnerPackageName
        } else {
            null
        }
        return InstallerInfo(
            installing = sourceInfo.installingPackageName,
            source = packageSource,
            initiating = sourceInfo.initiatingPackageName,
            originating = sourceInfo.originatingPackageName,
            updateOwner = updateOwner,
        )
    }

    private data class InstallerInfo(
        val installing: String? = null,
        val source: String? = null,
        val initiating: String? = null,
        val originating: String? = null,
        val updateOwner: String? = null,
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun mapPackageSource(value: Int): String? = when (value) {
        PackageInstaller.PACKAGE_SOURCE_UNSPECIFIED -> "unspecified"
        PackageInstaller.PACKAGE_SOURCE_STORE -> "store"
        PackageInstaller.PACKAGE_SOURCE_LOCAL_FILE -> "local_file"
        PackageInstaller.PACKAGE_SOURCE_DOWNLOADED_FILE -> "downloaded_file"
        PackageInstaller.PACKAGE_SOURCE_OTHER -> "other"
        else -> null
    }
}