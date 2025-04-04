package com.example.budgetbuddy;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ActivityHistory extends AppCompatActivity implements TransactionAdapter.OnTransactionDeleteListener {
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactions;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        transactions = new ArrayList<>();
        adapter = new TransactionAdapter(transactions, this);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadExpenses();
    }

    private void loadExpenses() {
        db.collection("transactions")
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .whereEqualTo("type", "Expense")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        transactions.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Transaction transaction = document.toObject(Transaction.class);
                            transaction.setId(document.getId());
                            transactions.add(transaction);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load expenses", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDelete(int position) {
        String docId = transactions.get(position).getId();
        db.collection("transactions").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    transactions.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete expense", Toast.LENGTH_SHORT).show();
                });
    }
}