package ipvc.estg.cm.entities

import android.widget.ImageView


class Product(
    var id: Int,
    var name: String,
    var image: String,
    var images:String,
    var price: Float,
    var subcategory: Subcategory,
    var description: String,
    var favorite: Boolean,
    var quantity: Int,
    var total: Float,
    var entity: EntityProd,
    var holderImage: ImageView? = null,
)

data class EntityProd(
    val id: Int,
    val name: String,
)

data class Subcategory(
    val id: Int,
    val name: String,
)