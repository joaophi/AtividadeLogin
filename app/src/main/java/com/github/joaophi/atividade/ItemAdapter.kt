package com.github.joaophi.atividade

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.joaophi.atividade.databinding.LayoutItemBinding

class ItemAdapter(
    val onClick: (Item) -> Unit,
    val onDelete: (Item) -> Unit,
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(ItemCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        LayoutItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int): Unit =
        holder.bind(getItem(position))

    inner class ItemViewHolder(
        private val binding: LayoutItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var item: Item

        init {
            binding.root.setOnClickListener { onClick(item) }
            binding.btnExcluir.setOnClickListener { onDelete(item) }
        }

        fun bind(item: Item) {
            this.item = item
            binding.tvId.text = "ID: ${item.id}"
            binding.tvDescricao.text = "Descrição: ${item.descricao}"
            binding.tvQuantidade.text = "Quantidade: ${item.quantidade}"
        }
    }
}

private object ItemCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
        oldItem == newItem
}