package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET

interface UserService {
    @GET("users")
    fun getUsers(): Call<List<User>>
}
