package io.mehow.squashit

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.mehow.squashit.databinding.CredentialsItemBinding

class CredentialsViewHolder(
  private val binding: CredentialsItemBinding,
  onDeleteCredentials: (Credentials) -> Unit
) : ViewHolder(binding.root) {
  private var credentials: Credentials? = null

  init {
    binding.delete.setOnClickListener {
      onDeleteCredentials(credentials!!)
    }
  }

  fun bindTo(credentials: Credentials) {
    this.credentials = credentials
    binding.credentialsId.text = credentials.id.value
  }
}
