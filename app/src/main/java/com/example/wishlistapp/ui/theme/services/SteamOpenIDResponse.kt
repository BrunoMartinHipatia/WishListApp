import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call
import okhttp3.OkHttpClient
import okhttp3.Request

// Modelo de datos para la respuesta de OpenID
class SteamOpenIDResponse(
    val steamid: String
)

// Interfaz para manejar la autenticación con Steam OpenID

interface SteamOpenIDService {
    companion object {
        private const val STEAM_OPENID_URL = "https://steamcommunity.com/openid/login"

        fun getSteamLoginUrl(returnTo: String): String {
            return "https://steamcommunity.com/openid/login" +
                    "?openid.ns=http://specs.openid.net/auth/2.0" +
                    "&openid.mode=checkid_setup" +
                    "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select" +
                    "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
                    "&openid.return_to=$returnTo" +
                    "&openid.realm=$returnTo"
        }


        fun verifySteamResponse(openidParams: Map<String, String>): Boolean {
            val client = OkHttpClient()
            val requestUrl = STEAM_OPENID_URL + "?openid.mode=check_authentication" +
                    openidParams.entries.joinToString("&") { "&${it.key}=${it.value}" }

            val request = Request.Builder()
                .url(requestUrl)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                return responseBody?.contains("is_valid:true") == true
            }
        }

        fun extractSteamID(openidClaimedId: String): String? {
            return openidClaimedId.substringAfterLast("/")
        }
    }
}
