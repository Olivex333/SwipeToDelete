package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    @SerializedName("address") val address: Address,
    val phone: String,
    val website: String,
    val company: Company
)

data class Address(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String,
    val geo: Geo
)

data class Geo(
    val lat: String,
    val lng: String
)

data class Company(
    val name: String,
    val catchPhrase: String,
    val bs: String
)
