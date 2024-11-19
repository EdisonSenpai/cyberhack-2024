import hashlib
from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)

# Numele bazei de date
DATABASE = 'Cyberhack.db'


# Funcție pentru hashing CNP
def hash_cnp(cnp):
    return hashlib.sha256(cnp.encode()).hexdigest()


# Inițializare baza de date
def init_db():
    with sqlite3.connect(DATABASE) as conn:
        cursor = conn.cursor()
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS clients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cnp TEXT UNIQUE NOT NULL,
                total_reduceri REAL DEFAULT 0,
                adresa_mac TEXT,
                data_inregistrare TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status TEXT DEFAULT 'active'
            )
        ''')
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS shops (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                client_id INTEGER,
                shop_name TEXT NOT NULL,
                data_inrolare TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status TEXT DEFAULT 'active',
                FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
            )
        ''')
        conn.commit()


init_db()


@app.route('/enroll_card', methods=['POST'])
def enroll_card():
    try:
        # Extragem datele din cerere
        received_data = request.get_json()
        cnp = received_data.get("cnp")
        shop_name = received_data.get("shop_name")
        adresa_mac = received_data.get("adresa_mac")

        if not cnp or not shop_name or not adresa_mac:
            return jsonify({"error": "Parametri lipsă."}), 400

        # Generăm hash-ul CNP-ului
        hashed_cnp = hash_cnp(cnp)
        print(f"[DEBUG] Hash CNP generat pentru {cnp}: {hashed_cnp}")

        with sqlite3.connect(DATABASE) as conn:
            cursor = conn.cursor()

            # Verificăm dacă clientul există deja
            cursor.execute("SELECT id FROM clients WHERE cnp = ?", (hashed_cnp,))
            client = cursor.fetchone()

            if client is None:
                # Dacă clientul nu există, îl creăm
                cursor.execute(
                    "INSERT INTO clients (cnp, adresa_mac) VALUES (?, ?)",
                    (hashed_cnp, adresa_mac)
                )
                client_id = cursor.lastrowid
                print(f"[DEBUG] Client nou creat cu ID {client_id} și hash CNP {hashed_cnp}.")
            else:
                client_id = client[0]
                print(f"[DEBUG] Client existent găsit cu ID {client_id} pentru hash CNP {hashed_cnp}.")

            # Verificăm dacă cardul pentru acest magazin există deja
            cursor.execute(
                "SELECT id FROM shops WHERE shop_name = ? AND client_id = ?",
                (shop_name, client_id)
            )
            if cursor.fetchone():
                print(f"[DEBUG] Cardul pentru magazinul '{shop_name}' există deja pentru clientul {client_id}.")
                return jsonify({"error": "Cardul pentru acest magazin există deja."}), 400

            # Adăugăm cardul asociat magazinului
            cursor.execute(
                "INSERT INTO shops (client_id, shop_name) VALUES (?, ?)",
                (client_id, shop_name)
            )
            print(f"[DEBUG] Card înrolat pentru magazinul '{shop_name}' și clientul {client_id}.")

            conn.commit()

        return jsonify({"message": "Card înrolat cu succes pentru magazin."}), 200

    except sqlite3.IntegrityError as e:
        print(f"[DEBUG] IntegrityError: {str(e)}")
        return jsonify({"error": "Clientul există deja sau datele magazinului sunt duplicate."}), 400
    except Exception as e:
        print(f"[DEBUG] Exception: {str(e)}")
        return jsonify({"error": f"Eroare internă: {str(e)}"}), 500


@app.route('/get_total_reduceri', methods=['POST'])
def get_total_reduceri():
    try:
        # Extragem datele din cerere
        received_data = request.get_json()
        cnp = received_data.get("cnp")

        if not cnp:
            return jsonify({"error": "CNP-ul este lipsă."}), 400

        # Generăm hash-ul CNP-ului pentru a-l căuta
        hashed_cnp = hash_cnp(cnp)
        print(f"[DEBUG] Hash CNP generat pentru căutare {cnp}: {hashed_cnp}")

        with sqlite3.connect(DATABASE) as conn:
            cursor = conn.cursor()

            # Verificăm dacă clientul există folosind hash-ul CNP-ului
            cursor.execute("SELECT total_reduceri FROM clients WHERE cnp = ?", (hashed_cnp,))
            client = cursor.fetchone()

            if client is None:
                print(f"[DEBUG] Clientul cu hash CNP {hashed_cnp} nu a fost găsit.")
                return jsonify({"error": "Clientul nu a fost găsit."}), 404

            # Obținem reducerile totale
            total_reduceri = client[0]
            print(f"[DEBUG] Reduceri totale pentru clientul cu hash CNP {hashed_cnp}: {total_reduceri}")

            return jsonify({"cnp": cnp, "total_reduceri": total_reduceri}), 200

    except Exception as e:
        print(f"[DEBUG] Exception: {str(e)}")
        return jsonify({"error": f"Eroare internă: {str(e)}"}), 500


@app.route('/unroll_card', methods=['POST'])
def unroll_card():
    try:
        # Extragem datele din cerere
        received_data = request.get_json()
        cnp = received_data.get("cnp")
        shop_name = received_data.get("shop_name")

        if not cnp or not shop_name:
            return jsonify({"error": "Parametri lipsă."}), 400

        # Generăm hash-ul CNP-ului
        hashed_cnp = hash_cnp(cnp)
        print(f"[DEBUG] Hash CNP generat pentru {cnp}: {hashed_cnp}")

        with sqlite3.connect(DATABASE) as conn:
            cursor = conn.cursor()

            # Verificăm dacă clientul există deja
            cursor.execute("SELECT id FROM clients WHERE cnp = ?", (hashed_cnp,))
            client = cursor.fetchone()

            if client is None:
                print(f"[DEBUG] Clientul cu hash CNP {hashed_cnp} nu a fost găsit.")
                return jsonify({"error": "Clientul nu a fost găsit."}), 404

            client_id = client[0]

            # Verificăm dacă cardul pentru acest magazin există
            cursor.execute(
                "SELECT id FROM shops WHERE shop_name = ? AND client_id = ?",
                (shop_name, client_id)
            )
            shop = cursor.fetchone()

            if shop is None:
                print(f"[DEBUG] Magazinul '{shop_name}' nu a fost găsit pentru clientul {client_id}.")
                return jsonify({"error": "Cardul pentru acest magazin nu a fost găsit."}), 404

            # Ștergem cardul asociat magazinului
            cursor.execute(
                "DELETE FROM shops WHERE shop_name = ? AND client_id = ?",
                (shop_name, client_id)
            )
            print(f"[DEBUG] Card deinrolat pentru magazinul '{shop_name}' și clientul {client_id}.")

            # Verificăm dacă mai există alte magazine pentru acest client
            cursor.execute(
                "SELECT id FROM shops WHERE client_id = ?",
                (client_id,)
            )
            remaining_shops = cursor.fetchall()

            if not remaining_shops:
                # Dacă nu mai există alte magazine, setăm adresa MAC la NULL
                cursor.execute(
                    "UPDATE clients SET adresa_mac = NULL WHERE id = ?",
                    (client_id,)
                )
                print(f"[DEBUG] Adresa MAC pentru clientul {client_id} a fost setată la NULL.")

            conn.commit()

        return jsonify({"message": "Card deinrolat cu succes."}), 200

    except sqlite3.IntegrityError as e:
        print(f"[DEBUG] IntegrityError: {str(e)}")
        return jsonify({"error": "Eroare de integritate."}), 400
    except Exception as e:
        print(f"[DEBUG] Exception: {str(e)}")
        return jsonify({"error": f"Eroare internă: {str(e)}"}), 500


@app.route('/plata', methods=['POST'])
def plata():
    try:
        # Extragem datele din cerere
        received_data = request.get_json()
        cnp = received_data.get("cnp")
        suma = received_data.get("suma")

        if not cnp or suma is None:
            return jsonify({"error": "CNP-ul și suma sunt necesare."}), 400

        if suma <= 0:
            return jsonify({"error": "Suma trebuie să fie un număr pozitiv."}), 400

        # Generăm hash-ul CNP-ului pentru a-l căuta
        hashed_cnp = hash_cnp(cnp)
        print(f"[DEBUG] Hash CNP generat pentru plata: {hashed_cnp}")

        with sqlite3.connect(DATABASE) as conn:
            cursor = conn.cursor()

            # Verificăm dacă clientul există folosind hash-ul CNP-ului
            cursor.execute("SELECT total_reduceri FROM clients WHERE cnp = ?", (hashed_cnp,))
            client = cursor.fetchone()

            if client is None:
                print(f"[DEBUG] Clientul cu hash CNP {hashed_cnp} nu a fost găsit pentru plata.")
                return jsonify({"error": "Clientul nu a fost găsit."}), 404

            # Obținem reducerile totale
            total_reduceri = client[0]
            print(f"[DEBUG] Reduceri disponibile înainte de plata: {total_reduceri}")

            if total_reduceri < suma:
                return jsonify({"error": "Reducerile disponibile sunt insuficiente."}), 400

            # Calculăm noile reduceri
            nou_total_reduceri = total_reduceri - suma

            # Actualizăm reducerile în baza de date
            cursor.execute("UPDATE clients SET total_reduceri = ? WHERE cnp = ?", (nou_total_reduceri, hashed_cnp))
            conn.commit()

            print(f"[DEBUG] Reduceri disponibile după plata: {nou_total_reduceri}")
            return jsonify({"message": "Plata efectuată cu succes.", "total_reduceri": nou_total_reduceri}), 200

    except Exception as e:
        print(f"[DEBUG] Exception: {str(e)}")
        return jsonify({"error": f"Eroare internă: {str(e)}"}), 500


@app.route('/', methods=['GET'])
def home():
    return "Serverul rulează!"


if __name__ == '__main__':
    app.run(debug=True, host='127.0.0.1', port=5000)
