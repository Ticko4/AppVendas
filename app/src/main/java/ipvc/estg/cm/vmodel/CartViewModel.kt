package ipvc.estg.cm.vmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import ipvc.estg.cm.db.CartDB
import ipvc.estg.cm.entities.Cart

import ipvc.estg.cm.db.CartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CartRepository
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allProducts: LiveData<List<Cart>>

    init {
        val cartDao = CartDB.getDatabase(application, viewModelScope).cartDao()
        repository = CartRepository(cartDao)
        allProducts = repository.allProducts
    }

    fun insert(product: Cart) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(product)
    }

    fun getCount(): LiveData<Int> {
        return repository.getCount()
    }


    fun deleteAll() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

    fun getProductById(id: Int): LiveData<Cart> {
        return repository.getProductById(id)
    }

    fun deleteById(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteById(id)
    }

    /*fun deleteById(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteById(id)
    }

    fun getNoteById(id: Int): LiveData<Note> {
        return repository.getNoteById(id)
    }

    fun updateNote(id: Int,title:String,description:String,color: String,colorId:Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateNote(id,title,description,color,colorId)
    }

    fun setNotification(id: Int,status:Boolean) = viewModelScope.launch(Dispatchers.IO) {
        repository.setNotification(id,status)
    }*/
}