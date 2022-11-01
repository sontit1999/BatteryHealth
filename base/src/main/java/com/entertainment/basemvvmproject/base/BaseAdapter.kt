package com.entertainment.basemvvmproject.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T, VB : ViewDataBinding> :
    RecyclerView.Adapter<BaseAdapter.BaseViewHolder<T, VB>>() {

    protected var list: ArrayList<T> = arrayListOf()
    protected lateinit var binding: VB

    abstract fun getLayoutId(): Int
    abstract fun getIdVariable(): Int
    abstract fun getIdVariableOnClick(): Int?

    @Nullable
    abstract fun getOnClick(): CBAdapter?

    @JvmName("setList1")
    fun setList(list: ArrayList<T>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T, VB> {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            getLayoutId(),
            parent,
            false
        )
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T, VB>, position: Int) {
        holder.setVariable(getIdVariable(), list[position])
        if (getOnClick() != null) {
            holder.setClickHolder(getIdVariableOnClick()!!, getOnClick()!!)
        }
    }

    override fun getItemCount() = list.size

    class BaseViewHolder<T, VB : ViewDataBinding>(var binding: VB) :
        RecyclerView.ViewHolder(binding.root) {
        fun setVariable(id: Int, t: T) {
            binding.setVariable(id, t)
        }

        fun setClickHolder(id: Int, onClick: CBAdapter) {
            binding.setVariable(id, onClick)
        }
    }
}