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
                    .setTitle("删除全部账单")
                    .setMessage("确定要删除全部账单吗？此操作不可撤销。")
                    .setPositiveButton("删除全部", (d, w) -> {
                        viewModel.deleteAllBills();
                        Toast.makeText(requireContext(), "已删除全部账单", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        viewModel = new ViewModelProvider(this).get(AllBillsViewModel.class);

        viewModel.getAllBills().observe(getViewLifecycleOwner(), bills ->
                adapter.setBills(bills != null ? bills : java.util.Collections.emptyList()));

        return view;
    }

    @Override
    public void onBillClick(Bill bill) {
        AddBillDialog dialog = new AddBillDialog();
        dialog.setEditBill(bill);
        dialog.setOnSaveListener(updatedBill -> {
            updatedBill.setId(bill.getId());
            viewModel.updateBill(updatedBill);
            Toast.makeText(requireContext(), "账单已更新", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getParentFragmentManager(), "edit");
    }

    @Override
    public void onBillLongClick(Bill bill) {
        new AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_RoundedDialog)
                .setTitle("删除账单")
                .setMessage("确定要删除 " + bill.getType() + " ¥" + String.format(java.util.Locale.CHINA, "%.2f", Math.abs(bill.getAmount())) + " 吗？")
                .setPositiveButton("删除", (d, w) -> {
                    viewModel.deleteBill(bill);
                    Toast.makeText(requireContext(), "已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
