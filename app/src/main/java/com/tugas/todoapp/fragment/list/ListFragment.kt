package com.tugas.todoapp.fragment.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tugas.todoapp.R
import com.tugas.todoapp.data.model.ToDoData
import com.tugas.todoapp.data.viewmodel.ToDoViewModel
import com.tugas.todoapp.databinding.FragmentListBinding
import com.tugas.todoapp.fragment.SharedViewModel
import com.tugas.todoapp.fragment.list.adapter.ListAdapter
import com.tugas.todoapp.hideKeyboard
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_list.view.*

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mTodoViewModel: ToDoViewModel by viewModels()
    private val listAdapter: ListAdapter by lazy {ListAdapter()}
    private val mSharedViewModel: SharedViewModel by viewModels()

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.mSharedViewModel = mSharedViewModel

        setupRecyclerView()


        mTodoViewModel.getAllData.observe(viewLifecycleOwner, Observer {data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
            listAdapter.setData(data )
        })
        hideKeyboard(requireActivity())
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setupRecyclerView() {
        val rvTodo = binding.rvTodo

        rvTodo.apply {
            layoutManager = StaggeredGridLayoutManager(2, GridLayoutManager.VERTICAL)
            adapter = listAdapter
            itemAnimator = LandingAnimator().apply {
                addDuration = 300
            }
        }

        swipeToDelete(rvTodo)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = listAdapter.dataList[viewHolder.adapterPosition]

                mTodoViewModel.deleteData(deletedItem)
                listAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                restoredDeletedItem(viewHolder.itemView, deletedItem)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoredDeletedItem(view: View, deletedItem: ToDoData) {
        val snackbar = Snackbar.make(view, "Successfully Remove: '${deletedItem.title}'", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo") {
            mTodoViewModel.insertData(deletedItem)
        }
        snackbar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_delete_all -> confirmDeleteAllData()
            R.id.menu_priority_high -> mTodoViewModel.sortByHighPriority.observe(this, Observer {
                listAdapter.setData(it)
            })
            R.id.menu_priority_low -> mTodoViewModel.sortByLowPriority.observe(this, Observer {
                listAdapter.setData(it)
            })
        }

        return super.onOptionsItemSelected(item)
    }

    private fun confirmDeleteAllData() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete everything?")
            .setMessage("Are you sure want to remove everything?")
            .setPositiveButton("Yes") {_,_ ->
                mTodoViewModel.deleteAllData()
                Toast.makeText(requireContext(), "Successfully remove everything", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .create()
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"

        mTodoViewModel.searchDatabase(searchQuery).observe(this, Observer {list ->
            list?.let {
                listAdapter.setData(it)
            }
        })
    }
}