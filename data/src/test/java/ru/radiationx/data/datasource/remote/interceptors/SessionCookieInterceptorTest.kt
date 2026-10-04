package ru.radiationx.data.datasource.remote.interceptors

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class SessionCookieInterceptorTest {

    private val expires = "Thu, 03 Dec 2037 19:15:39 GMT"
    private val foldedSession =
        "PHPSESSID=test-session; expires=$expires; path=/;; " +
            "PHPSESSID=test-session; expires=$expires; path=/; domain=.anilibria.tv;"

    @Test
    fun `folded session is rejected before normalization`() {
        val headers = Headers.Builder().add("Set-Cookie", foldedSession).build()
        assertTrue(Cookie.parseAll("https://wwnd.space/".toHttpUrl(), headers).isEmpty())
    }

    @Test
    fun `folded session is saved and sent to the next request`() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().addHeader("Set-Cookie", foldedSession))
            server.enqueue(MockResponse())
            val jar = TestCookieJar()
            val client = client(jar)

            client.newCall(Request.Builder().url(server.url("/public/api/index.php")).build())
                .execute().use { response ->
                    assertEquals(2, response.headers("Set-Cookie").size)
                }
            val session = jar.cookies.single()
            assertEquals("PHPSESSID", session.name)
            assertEquals("test-session", session.value)
            assertTrue(session.hostOnly)
            assertEquals(server.hostName, session.domain)
            assertEquals("/", session.path)
            assertTrue(session.persistent)
            assertTrue(session.expiresAt > System.currentTimeMillis())

            client.newCall(Request.Builder().url(server.url("/public/api/index.php?query=user")).build())
                .execute().close()
            assertNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            val profileRequest = server.takeRequest(5, TimeUnit.SECONDS)
            assertNotNull(profileRequest)
            assertEquals("PHPSESSID=test-session", profileRequest!!.getHeader("Cookie"))
        }
    }

    @Test
    fun `normal headers and cookie attributes are preserved`() {
        MockWebServer().use { server ->
            val session = "PHPSESSID=normal; Path=/public; Secure; HttpOnly; SameSite=Lax"
            val other = "theme=dark; Path=/; HttpOnly"
            server.enqueue(MockResponse().addHeader("Set-Cookie", session).addHeader("Set-Cookie", other))
            client(TestCookieJar()).newCall(Request.Builder().url(server.url("/")).build())
                .execute().use { response ->
                    assertEquals(listOf(session, other), response.headers("Set-Cookie"))
                    val cookie = Cookie.parse(server.url("/"), response.headers("Set-Cookie").first())!!
                    assertTrue(cookie.secure)
                    assertTrue(cookie.httpOnly)
                    assertEquals("/public", cookie.path)
                }
        }
    }

    @Test
    fun `standalone cookie for a foreign domain is still rejected`() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().addHeader("Set-Cookie", "PHPSESSID=foreign; Domain=.anilibria.tv; Path=/"))
            val jar = TestCookieJar()
            client(jar).newCall(Request.Builder().url(server.url("/")).build()).execute().close()
            assertTrue(jar.cookies.isEmpty())
        }
    }

    @Test
    fun `folded deletion retains expiry and deletion value`() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().addHeader("Set-Cookie",
                "PHPSESSID=deleted; Max-Age=0; Path=/;; PHPSESSID=deleted; Max-Age=0; Domain=.anilibria.tv; Path=/;"))
            val jar = TestCookieJar()
            client(jar).newCall(Request.Builder().url(server.url("/")).build()).execute().close()
            val cookie = jar.cookies.single()
            assertEquals("deleted", cookie.value)
            assertTrue(cookie.expiresAt < System.currentTimeMillis())
        }
    }

    private fun client(jar: CookieJar) = OkHttpClient.Builder()
        .cookieJar(jar)
        .addNetworkInterceptor(SessionCookieInterceptor())
        .build()

    private class TestCookieJar : CookieJar {
        var cookies = emptyList<Cookie>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            this.cookies = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> =
            cookies.filter { it.matches(url) && it.expiresAt > System.currentTimeMillis() }
    }
}
