package com.example.budgetbuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactions;
    private OnTransactionDeleteListener deleteListener;

    public interface OnTransactionDeleteListener {
        void onDelete(int position);
    }

    public TransactionAdapter(List<Transaction> transactions, OnTransactionDeleteListener deleteListener) {
        this.transactions = transactions;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.tvTitle.setText(transaction.getCategory());
        holder.tvPrice.setText(String.format("%.2f %s", transaction.getAmount(), transaction.getCurrency()));
        holder.tvCategory.setText(transaction.getCategory());
        holder.tvNotes.setText(transaction.getNote().isEmpty() ? "No notes" : transaction.getNote());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        holder.tvDate.setText(sdf.format(transaction.getTimestamp().toDate()));

        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(position));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvCategory, tvNotes, tvDate;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}