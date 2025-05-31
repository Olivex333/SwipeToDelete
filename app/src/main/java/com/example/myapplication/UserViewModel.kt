
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.UiState
import com.example.myapplication.User
import com.example.myapplication.UserService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// UserViewModel.kt
class UserViewModel(private val userService: UserService) : ViewModel() {
    private val _uiState = MutableLiveData(UiState())
    val uiState: LiveData<UiState> get() = _uiState

    fun loadUsers() {
        _uiState.value = _uiState.value?.copy(isLoading = true, error = null)
        userService.getUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    _uiState.value = UiState(users = response.body() ?: emptyList())
                } else {
                    _uiState.value = UiState(error = "Błąd: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                _uiState.value = UiState(error = t.message)
            }
        })
    }
    fun deleteUser(user: User) {
        val currentUsers = _uiState.value?.users ?: return
        _uiState.value = _uiState.value?.copy(users = currentUsers - user)
    }
}


