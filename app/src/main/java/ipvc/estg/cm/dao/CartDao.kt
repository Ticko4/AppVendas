package ipvc.estg.cm.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ipvc.estg.cm.entities.Cart
import androidx.lifecycle.Observer

@Dao
interface CartDao  {

    @Query("SELECT * from cart where quantity > 0 order by id desc")
    fun getAllProductsWithQuantity(): LiveData<List<Cart>>

    @Query("SELECT * from cart order by id desc")
    fun getAllProducts(): LiveData<List<Cart>>

    @Query("SELECT SUM(quantity) from cart")
    fun getCount(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Cart)

    @Query("DELETE FROM cart")
    suspend fun deleteAll()

    @Query("UPDATE cart SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id:Int,quantity: Int)


    @Query("DELETE FROM cart where id == :id")
    suspend fun deleteById(id: Int)


    @Query("SELECT * FROM cart WHERE id == :id")
    fun getProductById(id: Int): LiveData<Cart>

    @Query("SELECT SUM(total) from cart")
    fun getTotal(): LiveData<Float>

/*@Query("UPDATE notes SET colorId = :colorId,color = :color,description=:description,title=:title WHERE id == :id")
suspend fun updateNote(id: Int,title:String,description:String,color: String,colorId:Int)

@Query("UPDATE notes SET notification=:status WHERE id == :id")
suspend fun setNotification(id: Int,status:Boolean)*/
}