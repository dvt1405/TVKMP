package tv.iptv.tun.tviptv.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

actual object AssetReader {
    @OptIn(ExperimentalForeignApi::class)
    actual fun readTextFile(fileName: String): String {
        val bundle = NSBundle.mainBundle
        val path = bundle.pathForResource(fileName, ofType = null) ?: return ""
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as String
    }

}