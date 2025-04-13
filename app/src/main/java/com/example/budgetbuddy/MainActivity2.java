package com.example.budgetbuddy;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {
    private ImageButton btnIncome;
    private ImageButton btnTransport, btnFood, btnPurchases, btnEntertainment, btnEatOutside, btnOther;
    private ImageButton btnOptions;
    private TextView tvTransportSum, tvFoodSum, tvPurchasesSum, tvEntertainmentSum, tvEatOutsideSum, tvOtherSum;
    private float initialXIncome, initialYIncome;
    private static final float SNAP_THRESHOLD = 200f;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvTotalIncome, tvTotalExpense, tvBudget;
    private LinearLayout summaryLayout;
    private double firstOperand = 0;
    private String currentOperation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupButtonPositions();
        setTouchListeners();
        loadCategorySums();
        addSummaryLayout();
        btnOptions.setOnClickListener(v -> showOptionsDialog());
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategorySums();
        updateSummaryViews();
    }

    private void loadCategorySums() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", "Expense")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Double> categorySums = new HashMap<>();
                        categorySums.put("Transport", 0.0);
                        categorySums.put("Food", 0.0);
                        categorySums.put("Purchases", 0.0);
                        categorySums.put("Entertainment", 0.0);
                        categorySums.put("Eat Outside", 0.0);
                        categorySums.put("Other", 0.0);
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String category = document.getString("category");
                            double amount = document.getDouble("amount");
                            categorySums.put(category, categorySums.get(category) + amount);
                        }
                        updateSumView(tvTransportSum, categorySums.get("Transport"));
                        updateSumView(tvFoodSum, categorySums.get("Food"));
                        updateSumView(tvPurchasesSum, categorySums.get("Purchases"));
                        updateSumView(tvEntertainmentSum, categorySums.get("Entertainment"));
                        updateSumView(tvEatOutsideSum, categorySums.get("Eat Outside"));
                        updateSumView(tvOtherSum, categorySums.get("Other"));
                    }
                });
    }

    private void updateSumView(TextView textView, double amount) {
        if (amount > 0) {
            textView.setText(String.format(Locale.getDefault(), "%.2f AMD", amount));
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_view_expense) {
            startViewActivity("Expense");
            return true;
        } else if (item.getItemId() == R.id.action_view_income) {
            startViewActivity("Income");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startViewActivity(String type) {
        startActivity(new Intent(this, ExpenseHistoryActivity.class).putExtra("type", type));
    }

    private void initializeViews() {
        btnIncome = findViewById(R.id.btnIncome);
        btnTransport = findViewById(R.id.btnTransport);
        btnFood = findViewById(R.id.btnFood);
        btnPurchases = findViewById(R.id.btnPurchases);
        btnEntertainment = findViewById(R.id.btnEntertainment);
        btnEatOutside = findViewById(R.id.btnEatOutside);
        btnOther = findViewById(R.id.btnOther);
        btnOptions = findViewById(R.id.btnOptions);
        tvTransportSum = findViewById(R.id.tvTransportSum);
        tvFoodSum = findViewById(R.id.tvFoodSum);
        tvPurchasesSum = findViewById(R.id.tvPurchasesSum);
        tvEntertainmentSum = findViewById(R.id.tvEntertainmentSum);
        tvEatOutsideSum = findViewById(R.id.tvEatOutsideSum);
        tvOtherSum = findViewById(R.id.tvOtherSum);
        btnTransport.setOnClickListener(v -> showCategoryTransactions("Transport"));
        btnFood.setOnClickListener(v -> showCategoryTransactions("Food"));
        btnPurchases.setOnClickListener(v -> showCategoryTransactions("Purchases"));
        btnEntertainment.setOnClickListener(v -> showCategoryTransactions("Entertainment"));
        btnEatOutside.setOnClickListener(v -> showCategoryTransactions("Eat Outside"));
        btnOther.setOnClickListener(v -> showCategoryTransactions("Other"));
    }

    private void setupButtonPositions() {
        btnIncome.post(() -> {
            initialXIncome = btnIncome.getX();
            initialYIncome = btnIncome.getY();
        });
    }

    private void setTouchListeners() {
        btnIncome.setOnTouchListener(new DraggableButtonListener());
    }

    private class DraggableButtonListener implements View.OnTouchListener {
        private float dX, dY;
        private boolean isDragged = false;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.bringToFront();
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    isDragged = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    isDragged = true;
                    view.animate().x(event.getRawX() + dX).y(event.getRawY() + dY).setDuration(0).start();
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isDragged) {
                        ImageButton targetCategory = findNearestCategory(view);
                        if (targetCategory != null) showInputDialog(targetCategory, view.getId() == R.id.btnIncome);
                        resetButtonPosition(view);
                    }
                    return true;
                default:
                    return false;
            }
        }

        private ImageButton findNearestCategory(View draggedView) {
            ImageButton[] categories = {btnTransport, btnFood, btnPurchases, btnEntertainment, btnEatOutside, btnOther};
            Point draggedCenter = getViewCenter(draggedView);
            ImageButton nearestCategory = null;
            float minDistance = Float.MAX_VALUE;
            for (ImageButton category : categories) {
                Point categoryCenter = getViewCenter(category);
                float distance = calculateDistance(draggedCenter, categoryCenter);
                if (distance < SNAP_THRESHOLD && distance < minDistance) {
                    minDistance = distance;
                    nearestCategory = category;
                }
            }
            return nearestCategory;
        }

        private Point getViewCenter(View view) {
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            return new Point(location[0] + view.getWidth() / 2, location[1] + view.getHeight() / 2);
        }

        private float calculateDistance(Point p1, Point p2) {
            return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
        }

        private void resetButtonPosition(View view) {
            float targetX = initialXIncome;
            float targetY = initialYIncome;
            view.animate().x(targetX).y(targetY).setDuration(300).setInterpolator(new AccelerateDecelerateInterpolator()).start();
        }
    }

    private void showInputDialog(ImageButton category, boolean isIncome) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_expense, null);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etNote = dialogView.findViewById(R.id.etNote);
        setupNumberPad(dialogView, etAmount);
        builder.setView(dialogView)
                .setTitle("Add Income to " + category.getContentDescription())
                .setPositiveButton("Save", (dialog, which) -> {
                    String amount = etAmount.getText().toString();
                    if (!amount.isEmpty()) saveToFirestore(category.getContentDescription().toString(), amount, etNote.getText().toString(), isIncome);
                    else Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveToFirestore(String category, String amount, String note, boolean isIncome) {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("type", isIncome ? "Income" : "Expense");
        transaction.put("title", category);
        transaction.put("amount", Double.parseDouble(amount));
        transaction.put("category", category);
        transaction.put("note", note.isEmpty() ? "No notes" : note);
        transaction.put("date", new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
        transaction.put("timestamp", Timestamp.now());
        transaction.put("userId", mAuth.getCurrentUser().getUid());
        transaction.put("currency", "AMD");
        db.collection("transactions").add(transaction)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, transaction.get("type") + " saved!", Toast.LENGTH_SHORT).show();
                    loadCategorySums();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupNumberPad(View dialogView, EditText etAmount) {
        for (int i = 0; i <= 9; i++) {
            int resId = getResources().getIdentifier("btn" + i, "id", getPackageName());
            dialogView.findViewById(resId).setOnClickListener(v -> etAmount.append(((Button) v).getText()));
        }
        dialogView.findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (!etAmount.getText().toString().contains(".")) etAmount.append(".");
        });
        dialogView.findViewById(R.id.btnBackspace).setOnClickListener(v -> {
            String current = etAmount.getText().toString();
            if (!current.isEmpty()) etAmount.setText(current.substring(0, current.length() - 1));
        });
        dialogView.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            saveCurrentOperand(etAmount, "+");
            currentOperation = "+";
        });
        dialogView.findViewById(R.id.btnSubtract).setOnClickListener(v -> {
            saveCurrentOperand(etAmount, "-");
            currentOperation = "-";
        });
        dialogView.findViewById(R.id.btnMultiply).setOnClickListener(v -> {
            saveCurrentOperand(etAmount, "*");
            currentOperation = "*";
        });
        dialogView.findViewById(R.id.btnDivide).setOnClickListener(v -> {
            saveCurrentOperand(etAmount, "/");
            currentOperation = "/";
        });
        dialogView.findViewById(R.id.btnEquals).setOnClickListener(v -> {
            if (!currentOperation.isEmpty()) {
                String secondOperandText = etAmount.getText().toString();
                if (!secondOperandText.isEmpty()) {
                    double secondOperand = Double.parseDouble(secondOperandText);
                    double result = calculateResult(firstOperand, secondOperand, currentOperation);
                    etAmount.setText(String.valueOf(result));
                    firstOperand = result;
                    currentOperation = "";
                } else {
                    Toast.makeText(this, "Please enter a second operand", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No operation selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCurrentOperand(EditText etAmount, String operation) {
        String currentText = etAmount.getText().toString();
        if (!currentText.isEmpty()) {
            firstOperand = Double.parseDouble(currentText);
            etAmount.setText("");
        } else {
            Toast.makeText(this, "Please enter a first operand", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateResult(double firstOperand, double secondOperand, String operation) {
        switch (operation) {
            case "+":
                return firstOperand + secondOperand;
            case "-":
                return firstOperand - secondOperand;
            case "*":
                return firstOperand * secondOperand;
            case "/":
                if (secondOperand != 0) {
                    return firstOperand / secondOperand;
                } else {
                    Toast.makeText(this, "Division by zero is not allowed", Toast.LENGTH_SHORT).show();
                    return 0;
                }
            default:
                return 0;
        }
    }

    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 8, 16, 8);
        Button btnViewExpense = createDialogButton("View Expenses", v -> startViewActivity("Expense"));
        Button btnViewIncome = createDialogButton("View Incomes", v -> startViewActivity("Income"));
        Button btnLogout = createDialogButton("Logout", v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity2.this, Login.class));
            finishAffinity();
        });
        layout.addView(btnViewExpense, params);
        layout.addView(btnViewIncome, params);
        layout.addView(btnLogout, params);
        AlertDialog dialog = builder.setView(layout).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.TOP | Gravity.START);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.x = 60;
            layoutParams.y = 120;
            dialog.getWindow().setAttributes(layoutParams);
        }
        layout.setTag(dialog);
        dialog.show();
    }

    private Button createDialogButton(String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setOnClickListener(listener);
        return button;
    }

    private void addSummaryLayout() {
        RelativeLayout rootLayout = findViewById(R.id.rootLayout);
        summaryLayout = new LinearLayout(this);
        summaryLayout.setId(View.generateViewId());
        summaryLayout.setOrientation(LinearLayout.HORIZONTAL);
        summaryLayout.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        params.setMargins(0, 16, 16, 0);
        summaryLayout.addView(createSummarySection("Income", "0.00 AMD"));
        summaryLayout.addView(createSummarySection("Expense", "0.00 AMD"));
        summaryLayout.addView(createSummarySection("Budget", "0.00 AMD"));
        rootLayout.addView(summaryLayout, params);
    }

    private LinearLayout createSummarySection(String label, String value) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams) layout.getLayoutParams()).setMargins(16, 0, 16, 0);
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(getResources().getColor(android.R.color.black));
        labelView.setTextSize(14);
        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(getResources().getColor(android.R.color.black));
        valueView.setTextSize(14);
        if (label.equals("Income")) tvTotalIncome = valueView;
        else if (label.equals("Expense")) tvTotalExpense = valueView;
        else if (label.equals("Budget")) tvBudget = valueView;
        layout.addView(labelView);
        layout.addView(valueView);
        return layout;
    }

    private void updateSummaryViews() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("transactions").whereEqualTo("userId", userId).whereEqualTo("type", "Income").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double totalIncome = 0.0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            totalIncome += document.getDouble("amount");
                        }
                        tvTotalIncome.setText(String.format(Locale.getDefault(), "%.2f AMD", totalIncome));
                        double finalTotalIncome = totalIncome;
                        db.collection("transactions").whereEqualTo("userId", userId).whereEqualTo("type", "Expense").get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        double totalExpense = 0.0;
                                        for (QueryDocumentSnapshot document : task2.getResult()) {
                                            totalExpense += document.getDouble("amount");
                                        }
                                        tvTotalExpense.setText(String.format(Locale.getDefault(), "%.2f AMD", totalExpense));
                                        tvBudget.setText(String.format(Locale.getDefault(), "%.2f AMD", finalTotalIncome - totalExpense));
                                    }
                                });
                    }
                });
    }

    private void showCategoryTransactions(String category) {
        startActivity(new Intent(this, CategoryTransactionActivity.class).putExtra("category", category));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_expense) {
                startActivity(new Intent(MainActivity2.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_income) {
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(MainActivity2.this, HomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(MainActivity2.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_income);
    }
}