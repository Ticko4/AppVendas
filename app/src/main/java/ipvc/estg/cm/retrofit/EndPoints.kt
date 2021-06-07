package ipvc.estg.cm.retrofit


import ipvc.estg.cm.entities.Product
import ipvc.estg.cm.entities.User
import retrofit2.Call
import retrofit2.http.*


interface EndPoints {

    @FormUrlEncoded
    @POST("/api/user/login")
    fun loginUser(@Field("payload") payload: String): Call<User>

    @FormUrlEncoded
    @POST("/api/products/recommended")
    fun getRecommendedProducts(@Field("payload") payload: String): Call<List<Product>>
}