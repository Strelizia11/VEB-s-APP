package com.example.veb_app.ui.budget;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.core.content.ContextCompat;
import java.util.Collections;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.veb_app.R;
import com.example.veb_app.databinding.FragmentBudgetBinding;
import com.example.veb_app.data.DatabaseManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetFragment extends Fragment {

    private FragmentBudgetBinding binding;
    private BudgetManager budgetManager;
    private DecimalFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private Calendar calendar;
    private BudgetManager.Transaction.TransactionType selectedTransactionType = BudgetManager.Transaction.TransactionType.EXPENSE;
    private String currentFilter = "all";
    private RecyclerView recentTransactionsRecyclerView;
    private TransactionAdapter transactionAdapter;
    private List<BudgetManager.Transaction> displayedTransactions = new ArrayList<>(); // "all", "income", "expense"

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BudgetViewModel budgetViewModel =
                new ViewModelProvider(this).get(BudgetViewModel.class);

        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textBudget;
        budgetViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Initialize
        initialize();
        
        // Setup UI
        setupUI(root);
        
        // Load data
        loadBudgetData();

        return root;
    }

    private void initialize() {
        budgetManager = BudgetManager.getInstance();
        if (getContext() != null) {
            budgetManager.initialize(getContext());
        }
        
        currencyFormat = new DecimalFormat("₱#,##0.00");
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        calendar = Calendar.getInstance();
    }

    private void setupUI(View root) {
        // Setup FAB
        FloatingActionButton fabAddTransaction = binding.fabAddTransaction;
        if (fabAddTransaction != null) {
            fabAddTransaction.setOnClickListener(v -> showAddTransactionDialog());
        }

        // Setup search functionality
        TextInputEditText etSearch = root.findViewById(R.id.et_search);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterTransactions(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Setup filter chips
        Chip chipAll = root.findViewById(R.id.chip_all);
        Chip chipIncome = root.findViewById(R.id.chip_income);
        Chip chipExpense = root.findViewById(R.id.chip_expense);

        if (chipAll != null) {
            chipAll.setChecked(true);
            chipAll.setOnClickListener(v -> {
                currentFilter = "all";
                updateFilterButtonStyles(chipAll, chipIncome, chipExpense);
                loadBudgetData(); // Reload all data with new filter
            });
        }

        if (chipIncome != null) {
            chipIncome.setOnClickListener(v -> {
                currentFilter = "income";
                updateFilterButtonStyles(chipAll, chipIncome, chipExpense);
                loadBudgetData(); // Reload all data with new filter
            });
        }

        if (chipExpense != null) {
            chipExpense.setOnClickListener(v -> {
                currentFilter = "expense";
                updateFilterButtonStyles(chipAll, chipIncome, chipExpense);
                loadBudgetData(); // Reload all data with new filter
            });
        }

        // Setup Total Budget button
        Chip btnTotalBudget = root.findViewById(R.id.btn_total_budget);
        if (btnTotalBudget != null) {
            btnTotalBudget.setOnClickListener(v -> showTotalBudgetDialog());
        }

        // Setup RecyclerView
        recentTransactionsRecyclerView = binding.getRoot().findViewById(R.id.recent_transactions_recycler_view);
        if (recentTransactionsRecyclerView != null) {
            recentTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            transactionAdapter = new TransactionAdapter(displayedTransactions);
            recentTransactionsRecyclerView.setAdapter(transactionAdapter);
        }


    }

    private void loadBudgetData() {
        updateBudgetOverview();
        updateQuickStats();
        updateRecentTransactions();
        updateEmptyState();
    }

    private void updateBudgetOverview() {
        TextView tvBudgetTitle = binding.getRoot().findViewById(R.id.tv_budget_title);
        TextView tvIncomeAmount = binding.getRoot().findViewById(R.id.tv_income_amount);
        TextView tvExpensesAmount = binding.getRoot().findViewById(R.id.tv_expenses_amount);
        TextView tvRemainingAmount = binding.getRoot().findViewById(R.id.tv_remaining_amount);
        LinearProgressIndicator progressBudget = binding.getRoot().findViewById(R.id.progress_budget);

        // Update month display
        updateMonthDisplay();

        // Update monthly income
        if (tvIncomeAmount != null) {
            tvIncomeAmount.setText("Income " + currencyFormat.format(budgetManager.getTotalIncomeThisMonth()));
        }

        // Update monthly expenses
        if (tvExpensesAmount != null) {
            tvExpensesAmount.setText("Expenses " + currencyFormat.format(budgetManager.getTotalSpentThisMonth()));
        }

        // Update remaining budget (now on top right)
        if (tvRemainingAmount != null) {
            tvRemainingAmount.setText(currencyFormat.format(budgetManager.getRemainingBudget()));
        }

        if (progressBudget != null) {
            double spent = budgetManager.getTotalSpentThisMonth();
            double budget = budgetManager.getMonthlyBudget();
            int progress = budget > 0 ? (int) ((spent / budget) * 100) : 0;
            progressBudget.setProgress(progress);
        }
    }

    private void updateMonthDisplay() {
        // Find the month text view and update it with current month
        TextView tvBudgetTitle = binding.getRoot().findViewById(R.id.tv_budget_title);
        if (tvBudgetTitle != null) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            tvBudgetTitle.setText(monthFormat.format(cal.getTime()));
        }
    }

    private void updateQuickStats() {
        // Quick stats section removed - functionality moved to floating elements
    }

    private void updateFilterButtonStyles(Chip chipAll, Chip chipIncome, Chip chipExpense) {
        // Reset all chips to inactive style
        if (chipAll != null) {
            chipAll.setChipBackgroundColorResource(android.R.color.transparent);
            chipAll.setChipStrokeColorResource(R.color.sage_green);
            chipAll.setChipStrokeWidth(1f);
        }
        
        if (chipIncome != null) {
            chipIncome.setChipBackgroundColorResource(android.R.color.transparent);
            chipIncome.setChipStrokeColorResource(R.color.sage_green);
            chipIncome.setChipStrokeWidth(1f);
        }
        
        if (chipExpense != null) {
            chipExpense.setChipBackgroundColorResource(android.R.color.transparent);
            chipExpense.setChipStrokeColorResource(R.color.sage_green);
            chipExpense.setChipStrokeWidth(1f);
        }

        // Set active style for current filter
        switch (currentFilter) {
            case "all":
                if (chipAll != null) {
                    chipAll.setChipBackgroundColorResource(R.color.sage_green);
                    chipAll.setChipStrokeColorResource(R.color.sage_green);
                    chipAll.setChipStrokeWidth(0f);
                }
                break;
            case "income":
                if (chipIncome != null) {
                    chipIncome.setChipBackgroundColorResource(R.color.sage_green);
                    chipIncome.setChipStrokeColorResource(R.color.sage_green);
                    chipIncome.setChipStrokeWidth(0f);
                }
                break;
            case "expense":
                if (chipExpense != null) {
                    chipExpense.setChipBackgroundColorResource(R.color.sage_green);
                    chipExpense.setChipStrokeColorResource(R.color.sage_green);
                    chipExpense.setChipStrokeWidth(0f);
                }
                break;
        }
    }


    private void updateRecentTransactions() {
        if (transactionAdapter == null) return; // Ensure adapter is initialized

        List<BudgetManager.Transaction> allTransactions = budgetManager.getAllTransactions();
        List<BudgetManager.Transaction> filteredTransactions = new ArrayList<>();

        for (BudgetManager.Transaction transaction : allTransactions) {
            if (currentFilter.equals("all")) {
                filteredTransactions.add(transaction);
            } else if (currentFilter.equals("income") && transaction.getType() == BudgetManager.Transaction.TransactionType.INCOME) {
                filteredTransactions.add(transaction);
            } else if (currentFilter.equals("expense") && transaction.getType() == BudgetManager.Transaction.TransactionType.EXPENSE) {
                filteredTransactions.add(transaction);
            }
        }

        // Sort transactions by date (most recent first)
        Collections.sort(filteredTransactions, (t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        // Update the adapter's data
        displayedTransactions.clear();
        displayedTransactions.addAll(filteredTransactions);
        transactionAdapter.notifyDataSetChanged();

        // Handle empty state
        LinearLayout emptyStateLayout = binding.getRoot().findViewById(R.id.layout_empty_state_transactions);
        if (emptyStateLayout != null) {
            if (filteredTransactions.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                recentTransactionsRecyclerView.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recentTransactionsRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

        private List<BudgetManager.Transaction> transactions;

        public TransactionAdapter(List<BudgetManager.Transaction> transactions) {
            this.transactions = transactions;
        }

        @NonNull
        @Override
        public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
            BudgetManager.Transaction transaction = transactions.get(position);
            holder.bind(transaction);
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        class TransactionViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvDescription;
            private final TextView tvCategory;
            private final TextView tvDate;
            private final TextView tvAmount;
            private final ImageView ivIcon;

            public TransactionViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDescription = itemView.findViewById(R.id.tv_transaction_description);
                tvCategory = itemView.findViewById(R.id.tv_transaction_category);
                tvDate = itemView.findViewById(R.id.tv_transaction_date);
                tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
                ivIcon = itemView.findViewById(R.id.iv_transaction_icon);
            }

            public void bind(BudgetManager.Transaction transaction) {
                tvDescription.setText(transaction.getDescription());
                tvCategory.setText(transaction.getCategory());
                tvDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(transaction.getDate()));

                DecimalFormat currencyFormat = new DecimalFormat("₱#,##0.00");
                String amountText = currencyFormat.format(transaction.getAmount());

                if (transaction.getType() == BudgetManager.Transaction.TransactionType.INCOME) {
                    tvAmount.setText("+" + amountText);
                    tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.sage_green));
                    ivIcon.setImageResource(R.drawable.ic_add_24dp);
                    ivIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.sage_green));
                } else {
                    tvAmount.setText("-" + amountText);
                    tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error));
                    ivIcon.setImageResource(R.drawable.ic_remove_24dp);
                    ivIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.error));
                }
            }
        }
    }

    private void updateEmptyState() {
        LinearLayout emptyState = binding.getRoot().findViewById(R.id.layout_empty_state);
        if (emptyState == null) return;

        boolean hasTransactions = !budgetManager.getAllTransactions().isEmpty();
        emptyState.setVisibility(hasTransactions ? View.GONE : View.VISIBLE);
    }

    private void showTotalBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_total_budget, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Get dialog views
        TextView tvTotalIncome = dialogView.findViewById(R.id.tv_dialog_total_income);
        TextView tvTotalExpenses = dialogView.findViewById(R.id.tv_dialog_total_expenses);
        TextView tvTotalRemaining = dialogView.findViewById(R.id.tv_dialog_total_remaining);
        ImageView ivRemainingIcon = dialogView.findViewById(R.id.iv_dialog_remaining_icon);
        MaterialButton btnClose = dialogView.findViewById(R.id.btn_close_dialog);

        // Get total values across all time
        double totalIncome = budgetManager.getTotalIncome();
        double totalExpenses = budgetManager.getTotalExpenses();
        double totalRemaining = budgetManager.getTotalRemaining();

        // Update income
        if (tvTotalIncome != null) {
            tvTotalIncome.setText(currencyFormat.format(totalIncome));
        }

        // Update expenses
        if (tvTotalExpenses != null) {
            tvTotalExpenses.setText(currencyFormat.format(totalExpenses));
        }

        // Update remaining with dynamic color
        if (tvTotalRemaining != null) {
            tvTotalRemaining.setText(currencyFormat.format(totalRemaining));
            
            // Color code the remaining amount and icon
            int color;
            if (totalRemaining >= 0) {
                color = getResources().getColor(R.color.sage_green_darker);
            } else {
                color = getResources().getColor(R.color.error);
            }
            
            tvTotalRemaining.setTextColor(color);
            
            if (ivRemainingIcon != null) {
                ivRemainingIcon.setColorFilter(color);
            }
        }

        // Setup close button
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }
    }


    private void filterTransactions(String query) {
        List<BudgetManager.Transaction> filteredTransactions;
        
        if (query.trim().isEmpty()) {
            filteredTransactions = budgetManager.getAllTransactions();
        } else {
            filteredTransactions = budgetManager.searchTransactions(query);
        }

        // Apply type filter based on current filter
        if (!currentFilter.equals("all")) {
            List<BudgetManager.Transaction> typeFiltered = new ArrayList<>();
            BudgetManager.Transaction.TransactionType filterType = currentFilter.equals("income") ? 
                BudgetManager.Transaction.TransactionType.INCOME : BudgetManager.Transaction.TransactionType.EXPENSE;
            
            for (BudgetManager.Transaction transaction : filteredTransactions) {
                if (transaction.getType() == filterType) {
                    typeFiltered.add(transaction);
                }
            }
            filteredTransactions = typeFiltered;
        }

    }


    private void showAddTransactionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_transaction, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

         // Get dialog views
         MaterialButton btnIncome = dialogView.findViewById(R.id.btn_income);
         MaterialButton btnExpense = dialogView.findViewById(R.id.btn_expense);
         TextInputEditText etAmount = dialogView.findViewById(R.id.et_amount);
         TextInputEditText etDescription = dialogView.findViewById(R.id.et_description);
         AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actv_category);
         TextInputEditText etDate = dialogView.findViewById(R.id.et_date);
         MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
         MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Setup category dropdown
        setupCategoryDropdown(actvCategory);

        // Setup date picker
        setupDatePicker(etDate);

        // Setup transaction type buttons
        btnIncome.setOnClickListener(v -> {
            selectedTransactionType = BudgetManager.Transaction.TransactionType.INCOME;
            updateTransactionTypeButtons(btnIncome, btnExpense);
            updateCategoryDropdown(actvCategory);
        });

         btnExpense.setOnClickListener(v -> {
             selectedTransactionType = BudgetManager.Transaction.TransactionType.EXPENSE;
             updateTransactionTypeButtons(btnIncome, btnExpense);
             updateCategoryDropdown(actvCategory);
         });

         // Set default date to today
         etDate.setText(dateFormat.format(new Date()));
         calendar.setTime(new Date());

        // Setup save button
        btnSave.setOnClickListener(v -> {
            String amountText = etAmount.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String category = actvCategory.getText().toString().trim();
            String dateText = etDate.getText().toString().trim();

            if (amountText.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a description", Toast.LENGTH_SHORT).show();
                return;
            }

            if (category.isEmpty()) {
                Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);
                BudgetManager.Transaction transaction = new BudgetManager.Transaction(
                    description, amount, category, selectedTransactionType
                );
                
                // Set custom date if selected
                if (!dateText.isEmpty()) {
                    transaction.setDate(calendar.getTime());
                }

                budgetManager.addTransaction(transaction);
                loadBudgetData(); // Refresh all data
                
                // Refresh home page if it's visible
                android.util.Log.d("BudgetFragment", "Calling refreshHomePageIfVisible after adding transaction");
                com.example.veb_app.ui.home.HomeFragment.refreshHomePageIfVisible();
                
                dialog.dismiss();
                
                String typeText = selectedTransactionType == BudgetManager.Transaction.TransactionType.INCOME ? "Income" : "Expense";
                Toast.makeText(getContext(), typeText + " transaction added: " + currencyFormat.format(amount), Toast.LENGTH_SHORT).show();
                
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Initialize with expense selected
        selectedTransactionType = BudgetManager.Transaction.TransactionType.EXPENSE;
        updateTransactionTypeButtons(btnIncome, btnExpense);
    }

    private void setupCategoryDropdown(AutoCompleteTextView actvCategory) {
        updateCategoryDropdown(actvCategory);
    }
    
    private void updateCategoryDropdown(AutoCompleteTextView actvCategory) {
        List<BudgetManager.BudgetCategory> categories;
        if (selectedTransactionType == BudgetManager.Transaction.TransactionType.INCOME) {
            categories = budgetManager.getIncomeCategories();
        } else {
            categories = budgetManager.getExpenseCategories();
        }
        
        List<String> categoryNames = new ArrayList<>();
        for (BudgetManager.BudgetCategory category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_dropdown_item_1line, categoryNames);
        actvCategory.setAdapter(adapter);
        
        // Clear the selected category when switching types
        actvCategory.setText("");
    }

    private void setupDatePicker(TextInputEditText etDate) {
        etDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    etDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Set default date to today
        etDate.setText(dateFormat.format(new Date()));
        calendar.setTime(new Date());
    }

    private void updateTransactionTypeButtons(MaterialButton btnIncome, MaterialButton btnExpense) {
        if (selectedTransactionType == BudgetManager.Transaction.TransactionType.INCOME) {
            btnIncome.setBackgroundColor(getResources().getColor(R.color.sage_green));
            btnIncome.setTextColor(getResources().getColor(R.color.md_theme_light_onPrimary));
            btnExpense.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            btnExpense.setTextColor(getResources().getColor(R.color.sage_green));
        } else {
            btnExpense.setBackgroundColor(getResources().getColor(R.color.sage_green));
            btnExpense.setTextColor(getResources().getColor(R.color.md_theme_light_onPrimary));
            btnIncome.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            btnIncome.setTextColor(getResources().getColor(R.color.sage_green));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh budget data when returning to this fragment
        loadBudgetData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}