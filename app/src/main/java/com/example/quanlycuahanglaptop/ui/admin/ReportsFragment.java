package com.example.quanlycuahanglaptop.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.app.AdminActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.example.quanlycuahanglaptop.service.OrderService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportsFragment extends Fragment {

    private Spinner spYear;
    private TextView tvRevenueProjected;
    private TextView tvRevenueReal;
    private TextView tvCountDelivered;
    private TextView tvCountCancelled;
    private TextView tvCountOther;
    private PieChart pieTopProducts;
    private BarChart barRevenueByMonth;
    private OrderService orderService;
    private ImageButton btnOpenDrawer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        spYear = view.findViewById(R.id.spYear);
        tvRevenueProjected = view.findViewById(R.id.tvRevenueProjected);
        tvRevenueReal = view.findViewById(R.id.tvRevenueReal);
        tvCountDelivered = view.findViewById(R.id.tvCountDelivered);
        tvCountCancelled = view.findViewById(R.id.tvCountCancelled);
        tvCountOther = view.findViewById(R.id.tvCountOther);
        pieTopProducts = view.findViewById(R.id.pieTopProducts);
        barRevenueByMonth = view.findViewById(R.id.barRevenueByMonth);
        btnOpenDrawer = view.findViewById(R.id.btnOpenDrawer);

        orderService = new OrderService(requireContext());
        setupYearSpinner();
        // Render ngay theo năm đang chọn (mặc định 2025)
        spYear.post(() -> {
            try {
                Object sel = spYear.getSelectedItem();
                int year = sel != null ? Integer.parseInt(sel.toString()) : 2025;
                renderChartsForYear(year);
            } catch (Exception e) {
                renderChartsForYear(2025);
            }
        });
        // Xử lý mở drawer
        if (btnOpenDrawer != null) {
            btnOpenDrawer.setOnClickListener(v -> {
                if (getActivity() instanceof AdminActivity) {
                    ((AdminActivity) getActivity()).openDrawer();
                }
            });
        }
    }

    private void setupYearSpinner() {
        if (spYear == null) return;
        List<String> years = new ArrayList<>();
        int startYear = 2025;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = startYear; y <= currentYear + 5; y++) {
            years.add(String.valueOf(y));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_spinner_center, years);
        adapter.setDropDownViewResource(R.layout.item_spinner_center_dropdown);
        spYear.setAdapter(adapter);

        int defaultIndex = years.indexOf("2025");
        if (defaultIndex >= 0) {
            spYear.setSelection(defaultIndex);
        }

        spYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.black));
                }
                String selectedYear = years.get(position);
                onYearChanged(Integer.parseInt(selectedYear));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Ép màu đen cho item đang chọn sau khi setSelection mặc định
        spYear.post(() -> {
            View v = spYear.getSelectedView();
            if (v instanceof TextView) {
                ((TextView) v).setTextColor(getResources().getColor(R.color.black));
            }
        });
    }

    private void onYearChanged(int year) {
        // TODO: sẽ load dữ liệu biểu đồ và thống kê theo năm khi bổ sung service
        // Tạm reset về 0 để placeholder hiển thị an toàn
        // Vẫn giữ fallback nếu cần
        setTextSafely(tvRevenueProjected, tvRevenueProjected != null ? tvRevenueProjected.getText().toString() : "");
        setTextSafely(tvRevenueReal, tvRevenueReal != null ? tvRevenueReal.getText().toString() : "");
        setTextSafely(tvCountDelivered, tvCountDelivered != null ? tvCountDelivered.getText().toString() : "");
        setTextSafely(tvCountCancelled, tvCountCancelled != null ? tvCountCancelled.getText().toString() : "");
        setTextSafely(tvCountOther, tvCountOther != null ? tvCountOther.getText().toString() : "");
        renderChartsForYear(year);
    }

    private void setTextSafely(TextView tv, String text) {
        if (tv != null) tv.setText(text);
    }

    private void renderChartsForYear(int year) {
        // Bind số liệu thống kê
        double proj = orderService.getProjectedRevenueByYear(year);
        double real = orderService.getRealRevenueByYear(year);
        OrderService.CountByStatus c = orderService.countOrdersByYearGrouped(year);
        setTextSafely(tvRevenueProjected, formatCurrency(proj));
        setTextSafely(tvRevenueReal, formatCurrency(real));
        setTextSafely(tvCountDelivered, String.valueOf(c.delivered));
        setTextSafely(tvCountCancelled, String.valueOf(c.cancelled));
        setTextSafely(tvCountOther, String.valueOf(c.other));

        // PieChart Top 3
        if (pieTopProducts != null) {
            pieTopProducts.setNoDataText("Chưa có dữ liệu top sản phẩm");
            java.util.List<PieEntry> pieEntries = new java.util.ArrayList<>();
            java.util.List<OrderService.TopProductStat> tops = orderService.getTop3ProductsByYear(year);
            for (OrderService.TopProductStat t : tops) {
                pieEntries.add(new PieEntry(t.totalQuantity, t.productName));
            }
            PieDataSet pieDataSet = new PieDataSet(pieEntries, "Top 3");
            pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            PieData pieData = new PieData(pieDataSet);
            pieData.setValueTextSize(12f);
            pieTopProducts.setData(pieData);
            Description d = new Description();
            d.setText("");
            pieTopProducts.setDescription(d);
            pieTopProducts.getLegend().setEnabled(true);
            pieTopProducts.setUsePercentValues(true);
            pieTopProducts.invalidate();
        }

        if (barRevenueByMonth != null) {
            barRevenueByMonth.setNoDataText("Chưa có dữ liệu doanh thu theo tháng");
            java.util.List<BarEntry> barEntries = new java.util.ArrayList<>();
            float[] months = orderService.getMonthlyRevenueRealByYear(year);
            for (int i = 1; i <= 12; i++) {
                barEntries.add(new BarEntry(i, months[i - 1]));
            }
            BarDataSet dataSet = new BarDataSet(barEntries, "Doanh thu (triệu)");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.8f);
            barRevenueByMonth.setData(barData);
            Description d = new Description();
            d.setText("");
            barRevenueByMonth.setDescription(d);
            barRevenueByMonth.getLegend().setEnabled(true);
            barRevenueByMonth.setFitBars(true);
            barRevenueByMonth.invalidate();
        }
    }

    private String formatCurrency(double value) {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return nf.format(value) + "₫";
    }
}


