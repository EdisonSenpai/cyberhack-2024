package com.example.mywalletproject;

import static com.example.mywalletproject.MainActivity.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CardStorage {

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public CardStorage(Context context) {
        sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        gson = new Gson();
    }

    // Salvează lista de carduri în SharedPreferences
    public void saveCards(String option, String text) {

        List<StoreCard> existingCards = getCards();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (existingCards != null){
            existingCards.add(new StoreCard(option, text));
            String json = gson.toJson(existingCards);  // Serializăm lista într-un JSON
            Log.d(TAG,json);
            editor.putString("card_list", json);  // Salvăm lista serializată
            editor.apply();

        } else{
            List<StoreCard> cardList = new ArrayList<>();
            cardList.add(new StoreCard(option, text));

            String json = gson.toJson(cardList);  // Serializăm lista într-un JSON
            Log.d(TAG,json);

            editor.putString("card_list", json);  // Salvăm lista serializată
            editor.apply();                       // Aplicăm schimbările
        }

    }

    // Citește lista de carduri din SharedPreferences
    public List<StoreCard> getCards() {
        String json = sharedPreferences.getString("card_list", null);
        if (json != null) {
            Type type = new TypeToken<List<StoreCard>>() {}.getType(); // Tipul generic pentru lista de carduri
            return gson.fromJson(json, type);  // Deserializăm JSON-ul într-o listă de StoreCard
        } else {
            return new ArrayList<>(); // Dacă nu există carduri, returnăm o listă goală
        }
    }


    public void removeCardById(String cardId) {

        List<StoreCard> cardList = getCards();

        if (cardList != null) {

            for (int i = 0; i < cardList.size(); i++) {
                if (cardList.get(i).getCardId().equals(cardId)) {
                    cardList.remove(i);
                    break;
                }
            }

            String json = gson.toJson(cardList);  // Serializăm lista într-un JSON
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("card_list", json);  // Salvăm lista serializată
            editor.apply();
        }
    }
}
