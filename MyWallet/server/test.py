import requests

# URL-ul serverului Flask
base_url = "http://127.0.0.1:5000"


# Testăm înrolarea unui card pentru un magazin
def test_enroll_card(cnp, shop_name):
    print(f"Test: /enroll_card pentru CNP {cnp} și magazin {shop_name}")
    url = f"{base_url}/enroll_card"
    data = {
        "cnp": cnp,  # CNP-ul clientului
        "shop_name": shop_name,  # Numele magazinului
        "adresa_mac": "22:33:44:55:66:77"  # Adresa MAC
    }

    try:
        # Trimitem cererea
        response = requests.post(url, json=data)
        # Afișăm răspunsul
        print(f"Status Code: {response.status_code}")
        print("Răspuns:", response.json())
        print("-" * 50)
    except Exception as e:
        print(f"Eroare în timpul testului /enroll_card: {e}\n")


# Testăm obținerea totalului reducerilor
def test_get_total_reduceri(cnp):
    print(f"Test: /get_total_reduceri pentru CNP {cnp}")
    url = f"{base_url}/get_total_reduceri"
    data = {
        "cnp": cnp  # CNP-ul clientului pentru verificare
    }

    try:
        # Trimitem cererea
        response = requests.post(url, json=data)
        # Afișăm răspunsul
        print(f"Status Code: {response.status_code}")
        print("Răspuns:", response.json())
        print("-" * 50)
    except Exception as e:
        print(f"Eroare în timpul testului /get_total_reduceri: {e}\n")


# Testăm plata cu reduceri
def test_plata(cnp, suma):
    print(f"Test: /plata pentru CNP {cnp} și suma {suma}")
    url = f"{base_url}/plata"
    data = {
        "cnp": cnp,
        "suma": suma
    }

    try:
        # Trimitem cererea
        response = requests.post(url, json=data)
        # Afișăm răspunsul
        print(f"Status Code: {response.status_code}")
        print("Răspuns:", response.json())
        print("-" * 50)
    except Exception as e:
        print(f"Eroare în timpul testului /plata: {e}\n")


# Test complet: Înregistrare, obținere reduceri și efectuare plată
def test_full_scenario():
    print("Test complet: Înrolare card, obținere reduceri și efectuare plată")
    print("=" * 50)

    # Setăm datele de testare (valori noi)
    test_cases = [
        {"cnp": "1122334455667", "shop_name": "MagazinUltra1", "suma": 20},
        {"cnp": "2233445566778", "shop_name": "MagazinUltra2", "suma": 30},
        {"cnp": "3344556677889", "shop_name": "MagazinUltra3", "suma": 50}
    ]

    for case in test_cases:
        # Pasul 1: Înrolăm un client și un card asociat unui magazin
        print(f"1. Înrolare card asociat unui magazin pentru CNP {case['cnp']}:")
        test_enroll_card(case["cnp"], case["shop_name"])

        # Pasul 2: Obținem totalul reducerilor
        print(f"2. Obținere total reduceri pentru CNP {case['cnp']}:")
        test_get_total_reduceri(case["cnp"])

        # Pasul 3: Efectuăm o plată cu reduceri
        print(f"3. Plata cu reduceri pentru CNP {case['cnp']} și suma {case['suma']}:")
        test_plata(case["cnp"], case["suma"])


# Rulează toate testele
if __name__ == "__main__":
    print("Începem testele...\n")
    test_full_scenario()
    print("Teste finalizate!")
