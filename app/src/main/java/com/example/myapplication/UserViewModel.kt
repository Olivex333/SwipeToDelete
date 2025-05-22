
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.User
import com.example.myapplication.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UserViewModel : ViewModel() {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val logTag = "UserViewModel"

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val userService = retrofit.create(UserService::class.java)

    init {
        loadUsers()
    }

    private fun loadUsers() {
        userService.getUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(
                call: Call<List<User>>,
                response: Response<List<User>>
            ) {
                if (response.isSuccessful) {
                    _users.value = response.body() ?: emptyList()
                    Log.d(logTag, "Załadowano ${_users.value?.size} użytkowników")
                } else {
                    Log.e(logTag, "Nieudana odpowiedź: ${response.errorBody()}")
                    _users.value = emptyList()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e(logTag, "Błąd połączenia: ${t.message}", t)
                _users.value = emptyList()
            }
        })
    }


    fun deleteUser(user: User) {
        try {
            val initialSize = _users.value?.size ?: 0
            _users.value = _users.value?.toMutableList()?.apply {
                remove(user)
            }
            val finalSize = _users.value?.size ?: 0
            Log.d(logTag, "Rozmiar listy przed usunięciem: $initialSize, po usunięciu: $finalSize")
        } catch (e: Exception) {
            Log.e(logTag, "Błąd podczas usuwania użytkownika: ${e.message}", e)
        }
    }
}

