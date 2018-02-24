package de.lulebe.kreuzzuege.games

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.beust.klaxon.JsonObject
import de.lulebe.kreuzzuege.R


class UsersAdapter : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    var users = emptyList<JsonObject>()

    var clickListener: ((JsonObject) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.listitem_game, parent, false))
    }

    override fun getItemCount() =  users.size

    override fun onBindViewHolder(holder: UsersAdapter.ViewHolder, position: Int) {
        holder.tvPlayerName.text = users[position].string("name")!!
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPlayerName: TextView = itemView.findViewById(R.id.tv_enemy)
        init {
            itemView.setOnClickListener {
                clickListener?.invoke(users[adapterPosition])
            }
        }
    }

}