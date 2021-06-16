package ipvc.estg.cm.db

import androidx.lifecycle.LiveData
import ipvc.estg.cm.dao.CartDao
import ipvc.estg.cm.entities.Cart


class CartRepository(private val cartDao: CartDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allProducts: LiveData<List<Cart>> = cartDao.getAllProducts()

    val allProductsWithQuantity: LiveData<List<Cart>> = cartDao.getAllProductsWithQuantity()

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

    suspend fun deleteById(id: Int) {
        return cartDao.deleteById(id)
    }

    fun getTotal(): LiveData<Float> {
        return cartDao.getTotal()
    }

    suspend fun clearQuantities() {
        return cartDao.clearQuantities()
    }

    fun getFavorites(): LiveData<List<Cart>>{
        return cartDao.getFavorites()
    }
}