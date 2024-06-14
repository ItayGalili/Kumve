package com.example.mykumve

data class Item (val title : String,
                 val place : String,
                 val level : String,
                 val date : String,
                 val photo : String?)

// A temporary object to save the data:
object ItemManager {
    val items : MutableList<Item> = mutableListOf()

    fun add(item: Item){
        items.add(item)
    }

    fun remove(index:Int){
        items.removeAt(index)
    }
}
