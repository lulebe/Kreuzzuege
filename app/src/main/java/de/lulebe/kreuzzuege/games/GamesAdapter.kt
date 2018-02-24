package de.lulebe.kreuzzuege.games

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.beust.klaxon.JsonObject
import de.lulebe.kreuzzuege.R
import de.lulebe.kreuzzuege.data.Game


class GamesAdapter() : RecyclerView.Adapter<GamesAdapter.ViewHolder>() {

    var userId = 0
    var games = emptyList<JsonObject>()

    var clickListener: ((JsonObject) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.listitem_game, parent, false))
    }

    override fun getItemCount() =  games.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val enemy = games[position].array<JsonObject>("players")!!.first { it.int("id")!! != userId }
        holder.tvEnemy.text = enemy.string("name")!!
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEnemy: TextView = itemView.findViewById(R.id.tv_enemy)
        init {
            itemView.setOnClickListener {
                clickListener?.invoke(games[adapterPosition])
            }
        }
    }

}