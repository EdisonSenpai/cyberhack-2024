package com.example.mywalletproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mywalletproject.StoreCard;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<StoreCard> cardList;
    private OnCardClickListener onCardClickListener;

    // Constructorul care primește lista de carduri
    public CardAdapter(List<StoreCard> cardList, OnCardClickListener listener) {
        this.cardList = cardList;
        this.onCardClickListener = listener;
    }

    // Crearea elementului ViewHolder
    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new CardViewHolder(view, onCardClickListener);
    }

    // Legarea datelor din lista cu view-ul
    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        StoreCard card = cardList.get(position);
        holder.cardNameTextView.setText(card.getName());
        holder.cardIdTextView.setText(card.getCardId());
    }

    // Numărul de elemente din lista
    @Override
    public int getItemCount() {
        return cardList.size();
    }

    // ViewHolder care conține referințe către componentele UI
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardNameTextView;
        TextView cardIdTextView;

        public CardViewHolder(View itemView, final OnCardClickListener listener) {
            super(itemView);
            cardNameTextView = itemView.findViewById(R.id.cardNameTextView);
            cardIdTextView = itemView.findViewById(R.id.cardIdTextView);

            // Setează click-ul pe elementul de card
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCardClick(getAdapterPosition());
                }
            });
        }
    }

    // Interfața pentru click-ul pe card
    public interface OnCardClickListener {
        void onCardClick(int position);
    }
}