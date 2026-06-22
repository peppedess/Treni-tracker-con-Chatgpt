package com.treni.tracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.treni.tracker.R
import com.treni.tracker.data.TrenoMonitorato

class TreniAdapter(
    private var treni: List<TrenoMonitorato>,
    private val onRimuovi: (TrenoMonitorato) -> Unit,
    private val onApriDettaglio: (TrenoMonitorato) -> Unit
) : RecyclerView.Adapter<TreniAdapter.TrenoViewHolder>() {

    fun aggiorna(nuoviTreni: List<TrenoMonitorato>) {
        treni = nuoviTreni
        notifyDataSetChanged()
    }

    fun treniCorrenti(): List<TrenoMonitorato> = treni

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): TrenoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_treno, parent, false)
        return TrenoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrenoViewHolder, position: Int) {
        holder.bind(treni[position])
    }

    override fun getItemCount(): Int = treni.size

    inner class TrenoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardTreno)
        private val contenuto: View = itemView.findViewById(R.id.contenutoCardTreno)
        private val numero: android.widget.TextView = itemView.findViewById(R.id.textNumeroTreno)
        private val tratta: android.widget.TextView = itemView.findViewById(R.id.textTratta)
        private val stato: android.widget.TextView = itemView.findViewById(R.id.textStato)
        private val statoCompatto: android.widget.TextView = itemView.findViewById(R.id.textStatoCompatto)
        private val btnRimuovi: android.widget.ImageButton = itemView.findViewById(R.id.btnRimuovi)
        private val progressPercorso: LinearProgressIndicator = itemView.findViewById(R.id.progressPercorso)

        fun bind(treno: TrenoMonitorato) {
            val context = itemView.context
            numero.text = treno.numeroTreno
            tratta.text = "${treno.stazionePartenzaNome} → ${treno.stazioneDestinazioneNome ?: "?"}"

            val ritardo = treno.ultimoRitardo
            val (gradientRes, onColorRes, testoCompatto) = when {
                ritardo == null -> Triple(R.drawable.gradient_card_pending, R.color.card_pending_on, "In attesa")
                ritardo > 0 -> Triple(R.drawable.gradient_card_late, R.color.card_late_on, "+$ritardo min")
                ritardo < 0 -> Triple(R.drawable.gradient_card_early, R.color.card_early_on, "$ritardo min")
                else -> Triple(R.drawable.gradient_card_ontime, R.color.card_ontime_on, "In orario")
            }

            val onColor = ContextCompat.getColor(context, onColorRes)

            contenuto.setBackgroundResource(gradientRes)
            numero.setTextColor(onColor)
            statoCompatto.setTextColor(onColor)
            tratta.setTextColor(onColor)
            stato.setTextColor(onColor)
            btnRimuovi.imageTintList = android.content.res.ColorStateList.valueOf(onColor)

            // Barra di avanzamento del percorso: visibile solo se abbiamo dati reali
            // sulle fermate (disponibili dopo il primo controllo del worker)
            val totale = treno.numeroFermateTotali
            val indice = treno.indiceFermataCorrente
            if (totale != null && totale > 0 && indice != null) {
                progressPercorso.visibility = View.VISIBLE
                progressPercorso.max = totale
                progressPercorso.setProgressCompat(indice, false)
                progressPercorso.setIndicatorColor(onColor)
                progressPercorso.trackColor = android.graphics.Color.argb(50, android.graphics.Color.red(onColor), android.graphics.Color.green(onColor), android.graphics.Color.blue(onColor))
            } else {
                progressPercorso.visibility = View.GONE
            }

            statoCompatto.text = testoCompatto

            stato.text = treno.ultimaStazioneNotificata?.let { "Ultima fermata rilevata: $it" }
                ?: "In attesa del primo aggiornamento…"

            btnRimuovi.setOnClickListener { onRimuovi(treno) }
            itemView.setOnClickListener { onApriDettaglio(treno) }
        }
    }
}
