package ipvc.estg.cm.db

import androidx.lifecycle.LiveData
import ipvc.estg.cm.dao.CartDao
import ipvc.estg.cm.entities.Cart


class CartRepository(private val cartDao: CartDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allProducts: LiveData<List<Cart>> = cartDao.getAllProducts()

    suspend fun insert(product: Cart) {
        cartDao.insert(product)
    }

    fun getCount(): LiveData<Int> {
        return cartDao.getCount()
    }

    suspend fun deleteAll(){
        cartDao.deleteAll()
    }

     fun getProductById(id: Int): LiveData<Cart> {
        return cartDao.getProductById(id)
    }

    /*  suspend fun updateNote(id: Int,title:String,description:String,color: String,colorId:Int) {
         notesDao.updateNote(id,title,description,color,colorId)
     }

     suspend fun setNotification(id: Int,status:Boolean) {
         notesDao.setNotification(id,status)
     }*/
}