package com.example.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CategoryTransactionActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionDeleteListener {
    private RecyclerView recyclerView;
    private TransactionAdapter adapter;
    private List<Transaction> transactions;
    private FirebaseFirestore db;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        transactions = new ArrayList<>();
        adapter = new TransactionAdapter(transactions, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        category = getIntent().getStringExtra("category");

        if (category == null || category.isEmpty()) {
            Toast.makeText(this, "Category not specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTransactions();

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void loadTransactions() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        transactions.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Transaction transaction = document.toObject(Transaction.class);
                                transaction.setId(document.getId());
                                transactions.add(transaction);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load transactions", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete transaction", Toast.LENGTH_SHORT).show();
                });
    }
}