package com.example.budgetbuddy;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ExpenseHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> expenseList;
    private FirebaseFirestore db;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_history);

        recyclerView = findViewById(R.id.recyclerView);
        type = getIntent().getStringExtra("type");

        expenseList = new ArrayList<>();
        adapter = new ExpenseAdapter(expenseList, this::deleteExpense);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        loadTransactions();
    }

    private void loadTransactions() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        expenseList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Expense expense = document.toObject(Expense.class);
                            expense.setId(document.getId());
                            expenseList.add(expense);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void deleteExpense(int position) {
        String docId = expenseList.get(position).getId();
        db.collection("transactions").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    expenseList.remove(position);
                    adapter.notifyItemRemoved(position);
                });
    }
}