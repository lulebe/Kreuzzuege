package de.lulebe.kreuzzuege.games

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.beust.klaxon.JsonObject
import de.lulebe.kreuzzuege.R
import de.lulebe.kreuzzuege.data.Maps


class GamesAdapter : RecyclerView.Adapter<GamesAdapter.ViewHolder>() {

    var userId = 0
    var games = emptyList<JsonObject>()

    var clickListener: ((JsonObject) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.listitem_game, parent, false))
    }

    override fun getItemCount() =  games.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val enemy = games[position].array<JsonObject>("players")!!.first { it.int("id")!! != userId }
        val enemyName = enemy.string("name")!!
        val winnerId = games[position].obj("data")!!.int("winner")
        val mapId = games[position].obj("data")!!.int("map")!!
        if (!games[position].boolean("myTurn")!!) {
            holder.v.isClickable = false
            holder.tvWinner.text = "Waiting for $enemyName to play..."
        }
        holder.ivMap.setImageResource(Maps.getMapOverview(mapId))
        holder.tvEnemy.text = enemyName
        if (winnerId != null) {
            val winner = games[position].array<JsonObject>("players")!!.first { it.int("id")!! == winnerId }
            val winnerName = if (winner.int("id")!! == userId) "You" else enemyName
            holder.tvWinner.text = winnerName + " won."
        }
    }

    inner class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        val tvEnemy: TextView = v.findViewById(R.id.tv_enemy)
        val tvWinner: TextView = v.findViewById(R.id.tv_winner)
        val ivMap: ImageView = v.findViewById(R.id.iv_map)
        init {
            itemView.setOnClickListener {
                clickListener?.invoke(games[adapterPosition])
            }
        }
    }

}