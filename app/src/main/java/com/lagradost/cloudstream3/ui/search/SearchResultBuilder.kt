package com.lagradost.cloudstream3.ui.search

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.AppUtils.getNameFull
import com.lagradost.cloudstream3.utils.DataStoreHelper
import com.lagradost.cloudstream3.utils.DataStoreHelper.fixVisual
import com.lagradost.cloudstream3.utils.UIHelper.setImage
import kotlinx.android.synthetic.main.home_result_grid.view.*

object SearchResultBuilder {
    fun bind(
        clickCallback: (SearchClickCallback) -> Unit,
        card: SearchResponse,
        itemView: View
    ) {
        val cardView: ImageView = itemView.imageView
        val cardText: TextView = itemView.imageText

        val textIsDub: TextView? = itemView.text_is_dub
        val textIsSub: TextView? = itemView.text_is_sub
        println(card.name)

        val bg: CardView = itemView.backgroundCard

        val bar: ProgressBar? = itemView.watchProgress
        val playImg: ImageView? = itemView.search_item_download_play

        // Do logic

        bar?.visibility = View.GONE
        playImg?.visibility = View.GONE
        textIsDub?.visibility = View.GONE
        textIsSub?.visibility = View.GONE

        cardText.text = card.name

        //imageTextProvider.text = card.apiName
        cardView.setImage(card.posterUrl)

        bg.setOnClickListener {
            clickCallback.invoke(
                SearchClickCallback(
                    if (card is DataStoreHelper.ResumeWatchingResult) SEARCH_ACTION_PLAY_FILE else SEARCH_ACTION_LOAD,
                    it,
                    card
                )
            )
        }

        bg.setOnLongClickListener {
            clickCallback.invoke(SearchClickCallback(SEARCH_ACTION_SHOW_METADATA, it, card))
            return@setOnLongClickListener true
        }

        when (card) {
            is DataStoreHelper.ResumeWatchingResult -> {
                val pos = card.watchPos?.fixVisual()
                if (pos != null) {
                    bar?.max = (pos.duration / 1000).toInt()
                    bar?.progress = (pos.position / 1000).toInt()
                    bar?.visibility = View.VISIBLE
                }

                playImg?.visibility = View.VISIBLE

                if (!card.type.isMovieType()) {
                    cardText.text = cardText.context.getNameFull(card.name, card.episode, card.season)
                }
            }
            is AnimeSearchResponse -> {
                if (card.dubStatus != null && card.dubStatus.size > 0) {
                    if (card.dubStatus.contains(DubStatus.Dubbed)) {
                        textIsDub?.visibility = View.VISIBLE
                    }
                    if (card.dubStatus.contains(DubStatus.Subbed)) {
                        textIsSub?.visibility = View.VISIBLE
                    }
                }

                textIsDub?.apply {
                    val dubText = context.getString(R.string.app_dubbed_text)
                    text = if (card.dubEpisodes != null && card.dubEpisodes > 0) {
                        context.getString(R.string.app_dub_sub_episode_text_format).format(dubText, card.dubEpisodes)
                    } else {
                        dubText
                    }
                }

                textIsSub?.apply {
                    val subText = context.getString(R.string.app_subbed_text)
                    text = if (card.subEpisodes != null && card.subEpisodes > 0) {
                        context.getString(R.string.app_dub_sub_episode_text_format).format(subText, card.subEpisodes)
                    } else {
                        subText
                    }
                }
            }
        }
    }
}