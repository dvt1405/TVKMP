package tv.iptv.tun.tviptv

import org.junit.Assert.assertTrue
import org.junit.Test
import tv.iptv.tun.tviptv.ui.Greeting

class AndroidGreetingTest {

    @Test
    fun testExample() {
        assertTrue("Check Android is mentioned", Greeting().greet().contains("Android"))
    }
}