package io.mehow.squashit.view

import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.View.NO_ID
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.adapter.StatefulAdapter
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler

internal abstract class StatefulRecyclerAdapter(
  private val inflater: LayoutInflater,
  capacity: Int
) : RecyclerView.Adapter<ViewHolder>(), StatefulAdapter {
  private val attachedViews = SparseArray<View>(capacity)
  private var detachedState = SparseArray<SparseArray<Parcelable>>()

  @LayoutRes abstract override fun getItemViewType(position: Int): Int

  open fun onViewCreated(view: View) = Unit

  final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = inflater.inflate(viewType, parent, false)
    require(view.id != NO_ID) { "Views in a stateful adapter must have stable IDs." }
    detachedState.get(view.id)?.let {
      view.restoreHierarchyState(it)
    }
    onViewCreated(view)
    return object : ViewHolder(view) {}
  }

  final override fun onBindViewHolder(holder: ViewHolder, position: Int) = Unit

  @CallSuper
  override fun onViewAttachedToWindow(holder: ViewHolder) {
    val view = holder.itemView
    attachedViews.put(view.id, view)
  }

  @CallSuper
  override fun onViewDetachedFromWindow(holder: ViewHolder) {
    val view = holder.itemView
    cacheDetachedState(view)
    attachedViews.remove(view.id)
  }

  final override fun saveState(): Parcelable {
    for (i in 0 until attachedViews.size()) {
      cacheDetachedState(attachedViews.valueAt(i))
    }
    return SavedState(detachedState)
  }

  final override fun restoreState(savedState: Parcelable) {
    detachedState = (savedState as SavedState).states
  }

  private fun cacheDetachedState(view: View) {
    val state = SparseArray<Parcelable>().apply { view.saveHierarchyState(this) }
    detachedState.put(view.id, state)
  }

  @Parcelize
  @TypeParceler<SparseArray<SparseArray<Parcelable>>, NestedSparseArrayParceler>
  private data class SavedState(val states: SparseArray<SparseArray<Parcelable>>) : Parcelable

  private object NestedSparseArrayParceler : Parceler<SparseArray<SparseArray<Parcelable>>> {
    override fun create(parcel: Parcel): SparseArray<SparseArray<Parcelable>> {
      val size = parcel.readInt()
      val array = SparseArray<SparseArray<Parcelable>>(size)
      for (i in 0 until size) {
        val key = parcel.readInt()
        val value = parcel.readSparseArray<Parcelable>(SavedState::class.java.classLoader!!)
        array.append(key, value)
      }
      return array
    }

    override fun SparseArray<SparseArray<Parcelable>>.write(parcel: Parcel, flags: Int) {
      parcel.writeInt(size())
      for (i in 0 until size()) {
        parcel.writeInt(keyAt(i))
        parcel.writeSparseArray(valueAt(i))
      }
    }
  }
}
