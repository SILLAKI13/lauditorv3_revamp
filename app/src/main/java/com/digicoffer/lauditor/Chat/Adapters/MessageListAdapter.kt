package com.digicoffer.lauditor.Chat.Adapters

import android.animation.ObjectAnimator
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Chat.Model.MessageDo
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class MessageListAdapter(
    private val mContext: Context,
    private val messageList: List<MessageDo>,
    private val person_name: String,
    private val mtimeList: List<MessageDo>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
        private const val VIEW_TYPE_TYPING_BUBBLE = 3
    }

    private var filteredList: List<MessageDo> = messageList
    private var mMessageList: List<MessageDo> = messageList

    var dateTimeHeader: String = ""
    var formattedDate: String = ""
    private var lastDateHeader: String = ""
    private var typingBubbleVisible = false

    override fun getItemCount(): Int {
        return filteredList.size + if (typingBubbleVisible) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        if (typingBubbleVisible && position == filteredList.size) {
            return VIEW_TYPE_TYPING_BUBBLE
        }

        val message = mMessageList[position]
        return if (message.viewType == Constants.chat_SENT) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.send_msg_design, parent, false)
                SentMessageHolder(view)
            }
            VIEW_TYPE_MESSAGE_RECEIVED -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.received_msg_design, parent, false)
                ReceivedMessageHolder(view)
            }
            VIEW_TYPE_TYPING_BUBBLE -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.typing_bubble_row, parent, false)
                TypingBubbleHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TypingBubbleHolder) {
            return
        }

        val message = filteredList[position]
        val time = mtimeList[position]
        when (holder) {
            is SentMessageHolder -> holder.bind(message, time)
            is ReceivedMessageHolder -> holder.bind(message, time)
        }
    }

    fun setTypingBubbleVisible(visible: Boolean) {
        if (typingBubbleVisible == visible) return

        if (visible) {
            typingBubbleVisible = true
            notifyItemInserted(filteredList.size)
        } else {
            val removedPosition = filteredList.size
            typingBubbleVisible = false
            notifyItemRemoved(removedPosition)
        }
    }

    fun isTypingBubbleVisible(): Boolean {
        return typingBubbleVisible
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                val listTemp: List<MessageDo>

                if (charString.isEmpty()) {
                    listTemp = ArrayList(mMessageList)
                } else {
                    val listFiltered = ArrayList<MessageDo>()
                    for (row in mMessageList) {
                        val name = row.message
                        if (name != null && name.lowercase().contains(charString.lowercase())) {
                            listFiltered.add(row)
                        }
                    }
                    listTemp = listFiltered
                }

                val filterResults = FilterResults()
                filterResults.count = listTemp.size
                filterResults.values = listTemp
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                filteredList = filterResults?.values as List<MessageDo>
                notifyDataSetChanged()
            }
        }
    }

    private inner class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val person_icon: TextView = itemView.findViewById(R.id.person_icon)
        val messageText: TextView = itemView.findViewById(R.id.text_message_body)
        val timeText: TextView = itemView.findViewById(R.id.text_message_time)
        val date: TextView = itemView.findViewById(R.id.date)

        fun bind(message: MessageDo, time: MessageDo) {
            messageText.gravity = Gravity.START
            if (TextUtils.isEmpty(message.message)) return

            messageText.text = message.message

            val createdAt = if (message.iscurrentchat) {
                message.createdAt
            } else {
                time.createdAt
            } ?: ""

            val dateObj: Date
            try {
                val parser = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH)
                dateObj = parser.parse(createdAt) ?: Date()
            } catch (e: ParseException) {
                e.printStackTrace()
                return
            }

            val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
            val formattedDate = dateFormatter.format(dateObj)

            val timeFormatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val formattedTime = timeFormatter.format(dateObj)

            val todayFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
            val today = todayFormat.format(Date())
            val msgDay = todayFormat.format(dateObj)

            val groupKey = if (today == msgDay) "Today" else formattedDate
            val position = bindingAdapterPosition

            if (position == RecyclerView.NO_POSITION) {
                date.visibility = View.GONE
                return
            }

            if (position == 0) {
                date.text = groupKey
                date.visibility = View.VISIBLE
                timeText.text = formattedTime
                return
            }

            val prevMsg = filteredList[position - 1]
            val prevCreatedAt = (if (prevMsg.iscurrentchat) {
                prevMsg.createdAt
            } else {
                mtimeList[position - 1].createdAt
            }) ?: ""

            val prevDate: Date
            try {
                prevDate = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH).parse(prevCreatedAt) ?: Date()
            } catch (e: ParseException) {
                date.visibility = View.GONE
                return
            }

            val prevDay = todayFormat.format(prevDate)
            val prevGroupKey = if (today == prevDay) "Today" else dateFormatter.format(prevDate)

            if (groupKey != prevGroupKey) {
                date.text = groupKey
                date.visibility = View.VISIBLE
            } else {
                date.visibility = View.GONE
            }

            timeText.text = formattedTime

            val constName = Constants.NAME
            if (!constName.isNullOrEmpty()) {
                person_icon.text = constName.substring(0, 1).uppercase()
            } else {
                person_icon.text = "?"
            }
        }
    }

    private inner class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.text_message_body)
        val timeText: TextView = itemView.findViewById(R.id.text_message_time)
        val person_icon: TextView = itemView.findViewById(R.id.person_icon)
        val date: TextView = itemView.findViewById(R.id.date)

        fun bind(message: MessageDo, time: MessageDo) {
            messageText.gravity = Gravity.START
            if (TextUtils.isEmpty(message.message)) return

            messageText.text = message.message

            val createdAt = if (message.iscurrentchat) {
                message.createdAt
            } else {
                time.createdAt
            } ?: ""

            val dateObj: Date
            try {
                val parser = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH)
                dateObj = parser.parse(createdAt) ?: Date()
            } catch (e: ParseException) {
                e.printStackTrace()
                return
            }

            val dateFormatter = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
            val formattedDate = dateFormatter.format(dateObj)

            val timeFormatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val formattedTime = timeFormatter.format(dateObj)

            val todayFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
            val today = todayFormat.format(Date())
            val msgDay = todayFormat.format(dateObj)

            val groupKey = if (today == msgDay) "Today" else formattedDate
            val position = bindingAdapterPosition

            if (position == RecyclerView.NO_POSITION) {
                date.visibility = View.GONE
                return
            }
            timeText.text = formattedTime
            val constName = Constants.NAME
            if (!constName.isNullOrEmpty()) {
                person_icon.text = constName.substring(0, 1).uppercase()
            } else {
                person_icon.text = ""
            }
            if (position == 0) {
                date.text = groupKey
                date.visibility = View.VISIBLE
                return
            }

            val prevMsg = filteredList[position - 1]
            val prevCreatedAt = (if (prevMsg.iscurrentchat) {
                prevMsg.createdAt
            } else {
                mtimeList[position - 1].createdAt
            }) ?: ""

            val prevDate: Date
            try {
                prevDate = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH).parse(prevCreatedAt) ?: Date()
            } catch (e: ParseException) {
                date.visibility = View.GONE
                return
            }

            val prevDay = todayFormat.format(prevDate)
            val prevGroupKey = if (today == prevDay) "Today" else dateFormatter.format(prevDate)

            if (groupKey != prevGroupKey) {
                date.text = groupKey
                date.visibility = View.VISIBLE
            } else {
                date.visibility = View.GONE
            }
        }
    }

    private class TypingBubbleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val animators = ArrayList<ObjectAnimator>()

        init {
            val dot1: View? = itemView.findViewById(R.id.dot1)
            val dot2: View? = itemView.findViewById(R.id.dot2)
            val dot3: View? = itemView.findViewById(R.id.dot3)

            startWaveAnimation(dot1, 0)
            startWaveAnimation(dot2, 150)
            startWaveAnimation(dot3, 300)
        }

        private fun startWaveAnimation(dot: View?, startDelayMs: Long) {
            if (dot == null) return

            val bounceHeightPx = dot.resources.displayMetrics.density * -6f

            val animator = ObjectAnimator.ofFloat(dot, "translationY", 0f, bounceHeightPx, 0f)
            animator.duration = 600
            animator.startDelay = startDelayMs
            animator.repeatCount = ObjectAnimator.INFINITE
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.start()

            animators.add(animator)
        }

        fun stopAnimations() {
            for (animator in animators) {
                animator.cancel()
            }
            animators.clear()
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is TypingBubbleHolder) {
            holder.stopAnimations()
        }
    }

    fun addMessage(message: MessageDo) {
        if (typingBubbleVisible) {
            val bubblePosition = mMessageList.size
            typingBubbleVisible = false
            notifyItemRemoved(bubblePosition)
        }

        (mMessageList as ArrayList<MessageDo>).add(message)
        filteredList = mMessageList
        notifyItemInserted(mMessageList.size - 1)
    }
}
