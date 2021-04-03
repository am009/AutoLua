package moe.wjk.autolua.ui.doc;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import moe.wjk.autolua.R;
import moe.wjk.autolua.dummy.DocContent.DocItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DocItem}.
 *
 */
public class MyDocRecyclerViewAdapter extends RecyclerView.Adapter<MyDocRecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

    private final List<DocItem> mValues;
    private RecyclerView mRecyclerView;

    public MyDocRecyclerViewAdapter(List<DocItem> items, RecyclerView v) {
        mValues = items;
        mRecyclerView = v;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_doc, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).title);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public void onClick(View view) {
        int itemPosition = mRecyclerView.getChildLayoutPosition(view);
        DocItem item = mValues.get(itemPosition);
        String url = item.url;
        // Navigate to webview
        Bundle bundle = new Bundle();
        bundle.putString(DocWebViewFragment.ARG_DOC_URL, url);
        Navigation.findNavController(mRecyclerView).navigate(R.id.show_doc_webpage, bundle);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public DocItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}