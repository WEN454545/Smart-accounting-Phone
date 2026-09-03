package com.example.myapplication.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.entity.Bill;
import com.example.myapplication.ui.adapter.BillAdapter;
import com.example.myapplication.ui.adapter.SectionedBillAdapter;
import com.example.myapplication.ui.dialog.AddBillDialog;

public class AllBillsFragment extends Fragment implements BillAdapter.OnBillClickListener, BillAdapter.OnBillLongClickListener {

    private RecyclerView recyclerView;
    private SectionedBillAdapter adapter;
    private AllBillsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_bills, container, false);

        recyclerView = view.findViewById(R.id.recycler_all_bills);
        adapter = new SectionedBillAdapter(this);
        adapter.setOnLongClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        view.findViewById(R.id.btn_delete_all).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                    .setTitle("\u5220\u9664\u5168\u90E8\u8D26\u5355")
                    .setMessage("\u786E\u5B9A\u8981\u5220\u9664\u5168\u90E8\u8D26\u5355\u5417\uFF1F\u6B64\u64CD\u4F5C\u4E0D\u53EF\u64A4\u9500\u3002")
                    .setPositiveButton("\u5220\u9664\u5168\u90E8", (d, w) -> {
                        viewModel.deleteAllBills();
                        Toast.makeText(requireContext(), "\u5DF2\u5220\u9664\u5168\u90E8\u8D26\u5355", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("\u53D6\u6D88", null)
                    .show();
        });

        viewModel = new ViewModelProvider(this).get(AllBillsViewModel.class);

        viewModel.getAllBills().observe(getViewLifecycleOwner(), bills ->
                adapter.setBills(bills != null ? bills : java.util.Collections.emptyList()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Rebind bill icons: icon changes in settings only touch SharedPreferences,
        // which does not retrigger the bill LiveData.
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void onBillClick(Bill bill) {
        AddBillDialog dialog = new AddBillDialog();
        dialog.setEditBill(bill);
        dialog.setOnSaveListener(updatedBill -> {
            updatedBill.setId(bill.getId());
            viewModel.updateBill(updatedBill);
            Toast.makeText(requireContext(), "\u8D26\u5355\u5DF2\u66F4\u65B0", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "edit");
    }

    @Override
    public void onBillLongClick(Bill bill) {
        new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setTitle("\u5220\u9664\u8D26\u5355")
                .setMessage("\u786E\u5B9A\u8981\u5220\u9664 " + bill.getType() + " \u00A5" + String.format(java.util.Locale.CHINA, "%.2f", Math.abs(bill.getAmount())) + " \u5417\uFF1F")
                .setPositiveButton("\u5220\u9664", (d, w) -> {
                    viewModel.deleteBill(bill);
                    Toast.makeText(requireContext(), "\u5DF2\u5220\u9664", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("\u53D6\u6D88", null)
                .show();
    }
}