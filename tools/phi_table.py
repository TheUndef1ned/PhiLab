"""Builds phi_table.json in the format of Hald's table (Приложение IV) and checks it against values read from the photo."""
import json, math, sys
from decimal import Decimal, ROUND_HALF_EVEN, getcontext
getcontext().prec = 50

def phi_exact(z: float) -> Decimal:
    # 1 - Q(z), Q via erfc (double precision relative ~1e-16 on the tail)
    q = Decimal(repr(0.5 * math.erfc(z / math.sqrt(2.0))))
    return Decimal(1) - q

def table_string(z: float) -> str:
    v = phi_exact(z)
    s = format(v, 'f')
    frac = s.split('.')[1]
    nines = len(frac) - len(frac.lstrip('9'))
    if v < Decimal('0.9'):
        nines = 0
    digits = nines + 4
    q = v.quantize(Decimal(1).scaleb(-digits), rounding=ROUND_HALF_EVEN)
    return format(q, 'f')

def sup(n):
    return ''.join('⁰¹²³⁴⁵⁶⁷⁸⁹'[int(c)] for c in str(n))

def from_photo(token: str) -> str:
    # "9^3 8215" style: k nines + 4 digits; plain "8849" means 0.8849; "90147" means 0.90147
    if '^' in token:
        k, rest = token.split('^')
        return '0.' + '9' * int(k) + rest
    return '0.' + token

PHOTO = {
    0.0: "5000 5040 5080 5120 5160 5199 5239 5279 5319 5359",
    0.5: "6915 6950 6985 7019 7054 7088 7123 7157 7190 7224",
    0.9: "8159 8186 8212 8238 8264 8289 8315 8340 8365 8389",
    1.2: "8849 8869 8888 8907 8925 8944 8962 8980 8997 90147",
    1.3: "90320 90490 90658 90824 90988 91149 91309 91466 91621 91774",
    1.9: "97128 97193 97257 97320 97381 97441 97500 97558 97615 97670",
    2.3: "98928 98956 98983 2^0097 2^0358 2^0613 2^0863 2^1106 2^1344 2^1576",
    2.4: "2^1802 2^2024 2^2240 2^2451 2^2656 2^2857 2^3053 2^3244 2^3431 2^3613",
    3.0: "2^8650 2^8694 2^8736 2^8777 2^8817 2^8856 2^8893 2^8930 2^8965 2^8999",
    3.1: "3^0324 3^0646 3^0957 3^1260 3^1553 3^1836 3^2112 3^2378 3^2636 3^2886",
    3.5: "3^7674 3^7759 3^7842 3^7922 3^7999 3^8074 3^8146 3^8215 3^8282 3^8347",
    3.7: "3^8922 3^8964 4^0039 4^0426 4^0799 4^1158 4^1504 4^1838 4^2159 4^2468",
    4.0: "4^6833 4^6964 4^7090 4^7211 4^7327 4^7439 4^7546 4^7649 4^7748 4^7843",
    4.5: "5^6602 5^6759 5^6908 5^7051 5^7187 5^7318 5^7442 5^7561 5^7675 5^7784",
    4.9: "6^5208 6^5446 6^5673 6^5889 6^6094 6^6289 6^6475 6^6652 6^6821 6^6981",
}

values = [table_string(i / 100) for i in range(500)]
bad = 0
checked = 0
for row, toks in PHOTO.items():
    for j, tok in enumerate(toks.split()):
        i = round(row * 100) + j
        want = from_photo(tok)
        checked += 1
        if values[i] != want:
            bad += 1
            print(f"MISMATCH z={i/100:.2f}: generated {values[i]} photo {want}")
print(f"checked {checked} photo cells, mismatches: {bad}")

out = {
    "title": "Таблица интеграла вероятности Φ(z)",
    "source": "A. Hald, Statistical tables and formulas, John Wiley, 1952 (Приложение IV)",
    "definition": "Φ(z) = 1/√(2π) ∫_{-∞}^{z} exp(-x²/2) dx,  Φ(-z) = 1 - Φ(z)",
    "zMin": 0.0,
    "zStep": 0.01,
    "count": len(values),
    "values": values,
}
with open(sys.argv[1], 'w', encoding='utf-8') as f:
    json.dump(out, f, ensure_ascii=False, indent=1)
print("example 3.57 ->", values[357])
sys.exit(1 if bad else 0)
