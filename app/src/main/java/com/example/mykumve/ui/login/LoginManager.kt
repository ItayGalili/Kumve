import android.content.Context
import android.util.Base64
import com.example.mykumve.data.repository.RepositoryProvider
import com.example.mykumve.data.repository.UserRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Manages login logic and operations.
 * Handles user authentication.
 *
 * TODO: Implement more robust error handling and logging.
 */
class LoginManager(private val context: Context) {

    private val userRepository: UserRepository by lazy {
        RepositoryProvider.getUserRepository(context)
    }

    fun loginUser(username: String, password: String): Boolean {
        var isValid = false
        GlobalScope.launch {
            val user = userRepository.getUserByUsername(username)
            if (user != null) {
                val passwordHash = hashPassword(password, user.salt)
                if (passwordHash == user.passwordHash) {
                    // Handle successful login
                    isValid = true
                }
            }
            // Handle invalid login
        }
        return isValid
    }

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(Base64.decode(salt, Base64.DEFAULT))
        val hashedBytes = digest.digest(password.toByteArray())
        return Base64.encodeToString(hashedBytes, Base64.DEFAULT)
    }
}
