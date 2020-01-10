package io.mehow.squashit

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import io.mehow.squashit.databinding.CredentialsItemBinding

class CredentialsAdapter(
  private val inflater: LayoutInflater,
  private val onDeleteCredentials: (Credentials) -> Unit
) : ListAdapter<Credentials, CredentialsViewHolder>(CredentialsCallback) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CredentialsViewHolder {
    val binding = CredentialsItemBinding.inflate(inflater, parent, false)
    return CredentialsViewHolder(binding, onDeleteCredentials)
  }

  override fun onBindViewHolder(holder: CredentialsViewHolder, position: Int) {
    holder.bindTo(currentList[position])
  }

  private object CredentialsCallback : ItemCallback<Credentials>() {
    override fun areItemsTheSame(oldItem: Credentials, newItem: Credentials): Boolean {
      return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals") // Handled by SqlDelight.
    override fun areContentsTheSame(oldItem: Credentials, newItem: Credentials): Boolean {
      return oldItem == newItem
    }
  }
}
