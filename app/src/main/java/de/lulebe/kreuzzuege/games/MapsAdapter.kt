package de.lulebe.kreuzzuege.games

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.lulebe.kreuzzuege.R
import de.lulebe.kreuzzuege.data.Map
import de.lulebe.kreuzzuege.data.Maps


class MapsAdapter : RecyclerView.Adapter<MapsAdapter.ViewHolder>() {

    var clickListener: ((Map) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.listitem_game, parent, false))
    }

    override fun getItemCount() =  Maps.overview.size

    override fun onBindViewHolder(holder: MapsAdapter.ViewHolder, position: Int) {
        holder.tvPlayerName.text = Maps.overview[position].id.toString()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPlayerName: TextView = itemView.findViewById(R.id.tv_enemy)
        init {
            itemView.setOnClickListener {
                clickListener?.invoke(Maps.overview[adapterPosition])
            }
        }
    }

}