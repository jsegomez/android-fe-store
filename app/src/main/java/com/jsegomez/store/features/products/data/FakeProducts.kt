package com.jsegomez.store.features.products.data

import com.jsegomez.store.features.products.domain.model.Product
import com.jsegomez.store.features.products.domain.model.Rating

object FakeProducts {
    val products: List<Product> = listOf(
        Product(
            id = 1,
            title = "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
            price = 109.95,
            description = "Your perfect pack for everyday use and walks in the forest. Stash your laptop (up to 15 inches) in the padded sleeve, your everyday",
            category = "men's clothing",
            image = "https://fakestoreapi.com/img/81fPKd-2AYL._AC_SL1500_t.png",
            rating = Rating(rate = 3.9, count = 120)
        ),
        Product(
            id = 2,
            title = "Mens Casual Premium Slim Fit T-Shirts",
            price = 22.3,
            description = "Slim-fitting style, contrast raglan long sleeve, three-button henley placket, light weight & soft fabric for breathable and comfortable wearing.",
            category = "men's clothing",
            image = "https://fakestoreapi.com/img/71-3HjGNDUL._AC_SY879._SX._UX._SY._UY_t.png",
            rating = Rating(rate = 4.1, count = 259)
        ),
        Product(
            id = 3,
            title = "Mens Cotton Jacket",
            price = 55.99,
            description = "Great outerwear jackets for Spring/Autumn/Winter, suitable for many occasions, such as working, hiking, camping, mountain/rock climbing, cycling, traveling or other outdoors.",
            category = "men's clothing",
            image = "https://fakestoreapi.com/img/71li-ujtlUL._AC_UX679_t.png",
            rating = Rating(rate = 4.7, count = 500)
        ),
        Product(
            id = 4,
            title = "John Hardy Women's Legends Naga Gold & Silver Dragon Station Chain Bracelet",
            price = 695.0,
            description = "From our Legends Collection, the Naga was inspired by the mythical water dragon that protects the ocean's pearl.",
            category = "jewelery",
            image = "https://fakestoreapi.com/img/71pWzhdJNwL._AC_UL640_QL65_ML3_t.png",
            rating = Rating(rate = 4.6, count = 400)
        ),
        Product(
            id = 5,
            title = "WD 2TB Elements Portable External Hard Drive - USB 3.0",
            price = 64.0,
            description = "USB 3.0 and USB 2.0 Compatibility Fast data transfers Improve PC Performance High Capacity.",
            category = "electronics",
            image = "https://fakestoreapi.com/img/61IBBVJvSDL._AC_SY879_t.png",
            rating = Rating(rate = 3.3, count = 203)
        ),
        Product(
            id = 6,
            title = "Women's Short Sleeve Boat Neck V Blouse",
            price = 9.85,
            description = "95% Rayon, 5% Spandex. Lightweight, breathable fabric with a relaxed fit.",
            category = "women's clothing",
            image = "https://fakestoreapi.com/img/51eg55uWmdL._AC_UX679_t.png"
        ),
    )
}
