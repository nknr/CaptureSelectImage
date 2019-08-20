package com.example.pickfile.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pickfile.R;
import com.example.pickfile.databinding.ItemDocumentBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<File> documentList;
    private DocumentListener listener;

    public DocumentAdapter() {
        documentList = new ArrayList<>();
    }

    public void setListener(DocumentListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDocumentBinding itemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_document, parent, false);
        return new DocumentViewHolder(itemBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        holder.itemBinding.setItem(documentList.get(position));
        holder.itemBinding.executePendingBindings();

    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    public void addDocument(File file) {
        documentList.add(file);
        notifyItemInserted(documentList.size());
    }

    public void removeDocument(int position) {
        File file = getList().get(position);
        documentList.remove(position);
        notifyItemRemoved(position);

        if (file.exists()){
            file.delete();
        }
    }

    public List<File> getList() {
        return documentList;
    }

    public interface DocumentListener {
        void onDocumentClick(File document, int position);
    }

    public class DocumentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemDocumentBinding itemBinding;

        DocumentViewHolder(ItemDocumentBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
            itemBinding.delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onDocumentClick(documentList.get(getAdapterPosition()), getAdapterPosition());
            }
        }
    }
}

