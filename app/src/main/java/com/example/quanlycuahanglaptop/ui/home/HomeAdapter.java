package com.example.quanlycuahanglaptop.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycuahanglaptop.R;
import com.example.quanlycuahanglaptop.domain.Product;

import java.util.Objects;

/**
 * Adapter đa view-type cho trang Home.
 */
public class HomeAdapter extends ListAdapter<HomeItem, RecyclerView.ViewHolder> {

    public interface Listener {
        void onSearchClick();
        void onFilterClick();
        void onCategoryClick(String category);
        void onProductClick(Product product);
        void onAddToCartClick(Product product);
        void onSeeAllClick(String sectionTitle);
    }

    private static final int VT_HEADER = 1;
    private static final int VT_BANNER = 2;
    private static final int VT_CATEGORY = 3;
    private static final int VT_SECTION_TITLE = 4;
    private static final int VT_PRODUCT_BIG = 5;
    private static final int VT_PRODUCT_SMALL_LIST = 6;

    private final Listener listener;

    public HomeAdapter(@NonNull Listener listener) {
        super(DIFF);
        this.listener = listener;
        setHasStableIds(true);
    }

    private static final DiffUtil.ItemCallback<HomeItem> DIFF = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull HomeItem oldItem, @NonNull HomeItem newItem) {
            if (oldItem.getType() != newItem.getType()) return false;
            if (oldItem.getType() == HomeItem.Type.PRODUCT_BIG_CARD) {
                return oldItem.getProduct() != null && newItem.getProduct() != null
                        && Objects.equals(oldItem.getProduct().getId(), newItem.getProduct().getId());
            }
            return true;
        }

        @Override
        public boolean areContentsTheSame(@NonNull HomeItem oldItem, @NonNull HomeItem newItem) {
            if (oldItem.getType() != newItem.getType()) return false;
            switch (oldItem.getType()) {
                case SECTION_TITLE:
                    return Objects.equals(oldItem.getTitle(), newItem.getTitle());
                case PRODUCT_BIG_CARD:
                    return oldItem.getProduct() != null && newItem.getProduct() != null
                            && Objects.equals(oldItem.getProduct().getName(), newItem.getProduct().getName())
                            && oldItem.getProduct().getPrice() == newItem.getProduct().getPrice()
                            && Objects.equals(oldItem.getProduct().getImage(), newItem.getProduct().getImage());
                default:
                    return true;
            }
        }
    };

    @Override
    public int getItemViewType(int position) {
        HomeItem item = getItem(position);
        switch (item.getType()) {
            case HEADER: return VT_HEADER;
            case BANNER: return VT_BANNER;
            case CATEGORY_CHIPS: return VT_CATEGORY;
            case SECTION_TITLE: return VT_SECTION_TITLE;
            case PRODUCT_BIG_CARD: return VT_PRODUCT_BIG;
            case PRODUCT_SMALL_LIST: return VT_PRODUCT_SMALL_LIST;
        }
        return VT_PRODUCT_BIG;
    }

    @Override
    public long getItemId(int position) {
        HomeItem item = getItem(position);
        if (item.getType() == HomeItem.Type.PRODUCT_BIG_CARD && item.getProduct() != null && item.getProduct().getId() != null) {
            return item.getProduct().getId();
        }
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == VT_HEADER) {
            View v = inf.inflate(R.layout.item_home_header, parent, false);
            return new HeaderVH(v);
        } else if (viewType == VT_BANNER) {
            View v = inf.inflate(R.layout.item_home_banner, parent, false);
            return new BannerVH(v);
        } else if (viewType == VT_CATEGORY) {
            View v = inf.inflate(R.layout.item_home_category_chips, parent, false);
            return new CategoryVH(v);
        } else if (viewType == VT_SECTION_TITLE) {
            View v = inf.inflate(R.layout.item_home_section_title, parent, false);
            return new SectionTitleVH(v);
        } else if (viewType == VT_PRODUCT_SMALL_LIST) {
            View v = inf.inflate(R.layout.item_home_horizontal_list, parent, false);
            return new SmallListVH(v);
        } else {
            View v = inf.inflate(R.layout.item_product_grid, parent, false);
            return new ProductBigVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HomeItem item = getItem(position);
        if (holder instanceof SectionTitleVH) {
            ((SectionTitleVH) holder).bind(item.getTitle());
        } else if (holder instanceof ProductBigVH) {
            ((ProductBigVH) holder).bind(Objects.requireNonNull(item.getProduct()));
        } else if (holder instanceof SmallListVH) {
            ((SmallListVH) holder).bind(item.getProducts());
        } else if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).bind(listener);
        } else if (holder instanceof CategoryVH) {
            ((CategoryVH) holder).bind(item.getCategories(), listener);
        } else if (holder instanceof BannerVH) {
            ((BannerVH) holder).bind(item.getBannerImages());
        }
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        HeaderVH(@NonNull View itemView) { super(itemView); }
        void bind(@NonNull Listener listener) {
            View search = itemView.findViewById(R.id.etSearch);
            View filter = itemView.findViewById(R.id.btnFilter);
            if (search != null) search.setOnClickListener(v -> listener.onSearchClick());
            if (filter != null) filter.setOnClickListener(v -> listener.onFilterClick());
        }
    }

    static class BannerVH extends RecyclerView.ViewHolder {
        BannerVH(@NonNull View itemView) { super(itemView); }
        void bind(java.util.List<Integer> banners) {
            androidx.viewpager2.widget.ViewPager2 vp = itemView.findViewById(R.id.vpBanner);
            if (vp.getAdapter() == null) {
                vp.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        android.widget.ImageView iv = new android.widget.ImageView(parent.getContext());
                        iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        iv.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                        return new RecyclerView.ViewHolder(iv){};
                    }
                    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                        Integer resId = banners != null && !banners.isEmpty() ? banners.get(position % banners.size()) : R.drawable.ic_products;
                        ((android.widget.ImageView) holder.itemView).setImageResource(resId);
                    }
                    @Override public int getItemCount() { return banners != null && !banners.isEmpty() ? banners.size() : 1; }
                });
            }
        }
    }

    static class CategoryVH extends RecyclerView.ViewHolder {
        CategoryVH(@NonNull View itemView) { super(itemView); }
        void bind(java.util.List<String> chips, @NonNull Listener listener) {
            androidx.recyclerview.widget.RecyclerView rv = itemView.findViewById(R.id.rvChips);
            if (rv.getLayoutManager() == null) {
                androidx.recyclerview.widget.LinearLayoutManager lm = new androidx.recyclerview.widget.LinearLayoutManager(itemView.getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false);
                rv.setLayoutManager(lm);
            }
            ChipsAdapter adapter = (ChipsAdapter) rv.getAdapter();
            if (adapter == null) {
                adapter = new ChipsAdapter(listener::onCategoryClick);
                rv.setAdapter(adapter);
            }
            adapter.submit(chips);
        }
    }

    static class SectionTitleVH extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        SectionTitleVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSectionTitle);
            View seeAll = itemView.findViewById(R.id.btnSeeAll);
            if (seeAll != null) {
                seeAll.setOnClickListener(v -> {
                    CharSequence title = tvTitle.getText();
                    if (title != null) {
                        // Truyền title về listener
                        // cast adapter position an toàn
                        RecyclerView rv = (RecyclerView) itemView.getParent();
                        RecyclerView.Adapter a = rv != null ? rv.getAdapter() : null;
                        if (a instanceof HomeAdapter) {
                            ((HomeAdapter) a).listener.onSeeAllClick(title.toString());
                        }
                    }
                });
            }
        }
        void bind(String title) {
            tvTitle.setText(title);
        }
    }

    static class ProductBigVH extends RecyclerView.ViewHolder {
        private final android.widget.ImageView imgProduct;
        private final TextView tvName;
        private final TextView tvPrice;
        
        ProductBigVH(@NonNull View itemView) { 
            super(itemView); 
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
        
        void bind(@NonNull Product p) {
            tvName.setText(p.getName());
            tvPrice.setText(String.format("%,.0f đ", p.getPrice()));
            
            // Hiển thị hình ảnh sản phẩm
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                java.io.File f = new java.io.File(p.getImage());
                if (f.exists()) {
                    android.graphics.Bitmap b = android.graphics.BitmapFactory.decodeFile(f.getAbsolutePath());
                    if (b != null) {
                        imgProduct.setImageBitmap(b);
                    } else {
                        imgProduct.setImageResource(R.drawable.ic_products);
                    }
                } else {
                    imgProduct.setImageResource(R.drawable.ic_products);
                }
            } else {
                imgProduct.setImageResource(R.drawable.ic_products);
            }
            
            itemView.setOnClickListener(v -> {
                RecyclerView rv = (RecyclerView) itemView.getParent();
                RecyclerView.Adapter a = rv != null ? rv.getAdapter() : null;
                if (a instanceof HomeAdapter) {
                    ((HomeAdapter) a).listener.onProductClick(p);
                }
            });
        }
    }

    static class SmallListVH extends RecyclerView.ViewHolder {
        SmallListVH(@NonNull View itemView) { super(itemView); }
        void bind(java.util.List<com.example.quanlycuahanglaptop.domain.Product> products) {
            androidx.recyclerview.widget.RecyclerView rv = itemView.findViewById(R.id.rvHorizontal);
            if (rv.getLayoutManager() == null) {
                androidx.recyclerview.widget.LinearLayoutManager lm = new androidx.recyclerview.widget.LinearLayoutManager(itemView.getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false);
                rv.setLayoutManager(lm);
                androidx.recyclerview.widget.PagerSnapHelper snap = new androidx.recyclerview.widget.PagerSnapHelper();
                snap.attachToRecyclerView(rv);
            }
            ProductSmallAdapter adapter = (ProductSmallAdapter) rv.getAdapter();
            if (adapter == null) {
                adapter = new ProductSmallAdapter(p -> {
                    RecyclerView parent = (RecyclerView) itemView.getParent();
                    RecyclerView.Adapter a = parent != null ? parent.getAdapter() : null;
                    if (a instanceof HomeAdapter) {
                        ((HomeAdapter) a).listener.onProductClick(p);
                    }
                });
                rv.setAdapter(adapter);
            }
            adapter.submitList(products);
        }
    }
}


