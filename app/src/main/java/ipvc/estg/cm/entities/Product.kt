package ipvc.estg.cm.entities


class Product(
    var id: Int,
    var name: String,
    var image: String,
    var price: Float,
    var subcategory: String,
    var favorite: Boolean,
    var quantity: Int,
    var total: Float
) {


}
