"""
Disaster Responder - Administrative Geography Dataset (India)
Supports State/UT -> District -> Coordinate Centroid mapping and text normalization.
"""

# 28 States and 8 Union Territories
INDIAN_STATES_UTS = [
    "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
    "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
    "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
    "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
    "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
    "Uttar Pradesh", "Uttarakhand", "West Bengal",
    # 8 Union Territories
    "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu",
    "Delhi NCR", "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
]

# District Centroids Dataset (Key Districts across India)
DISTRICT_CENTROIDS = {
    "Delhi NCR": {"lat": 28.6139, "lng": 77.2090, "state": "Delhi NCR"},
    "East Delhi": {"lat": 28.6280, "lng": 77.2950, "state": "Delhi NCR"},
    "North Delhi": {"lat": 28.6800, "lng": 77.2200, "state": "Delhi NCR"},
    "South Delhi": {"lat": 28.5400, "lng": 77.2000, "state": "Delhi NCR"},
    "Yamuna Floodplain": {"lat": 28.6600, "lng": 77.2500, "state": "Delhi NCR"},
    "Geeta Colony": {"lat": 28.6500, "lng": 77.2700, "state": "Delhi NCR"},
    "Mayur Vihar": {"lat": 28.6000, "lng": 77.2900, "state": "Delhi NCR"},
    "Mumbai": {"lat": 19.0760, "lng": 72.8777, "state": "Maharashtra"},
    "Thane": {"lat": 19.2183, "lng": 72.9781, "state": "Maharashtra"},
    "Pune": {"lat": 18.5204, "lng": 73.8567, "state": "Maharashtra"},
    "Chennai": {"lat": 13.0827, "lng": 80.2707, "state": "Tamil Nadu"},
    "Cuddalore": {"lat": 11.7480, "lng": 79.7714, "state": "Tamil Nadu"},
    "Kolkata": {"lat": 22.5726, "lng": 88.3639, "state": "West Bengal"},
    "South 24 Parganas": {"lat": 22.1352, "lng": 88.5000, "state": "West Bengal"},
    "Cuttack": {"lat": 20.4625, "lng": 85.8828, "state": "Odisha"},
    "Puri": {"lat": 19.8135, "lng": 85.8312, "state": "Odisha"},
    "Bhubaneswar": {"lat": 20.2961, "lng": 85.8245, "state": "Odisha"},
    "Guwahati": {"lat": 26.1445, "lng": 91.7362, "state": "Assam"},
    "Kamrup": {"lat": 26.3000, "lng": 91.5000, "state": "Assam"},
    "Patna": {"lat": 25.5941, "lng": 85.1376, "state": "Bihar"},
    "Darbhanga": {"lat": 26.1542, "lng": 85.8918, "state": "Bihar"},
    "Kochi": {"lat": 9.9312, "lng": 76.2673, "state": "Kerala"},
    "Wayanad": {"lat": 11.6854, "lng": 76.1320, "state": "Kerala"},
    "Shimla": {"lat": 31.1048, "lng": 77.1734, "state": "Himachal Pradesh"},
    "Dehradun": {"lat": 30.3165, "lng": 78.0322, "state": "Uttarakhand"},
    "Chamoli": {"lat": 30.4022, "lng": 79.3283, "state": "Uttarakhand"},
    "Srinagar": {"lat": 34.0837, "lng": 74.7973, "state": "Jammu and Kashmir"},
    "Ahmedabad": {"lat": 23.0225, "lng": 72.5714, "state": "Gujarat"},
    "Kutch": {"lat": 23.7337, "lng": 69.8597, "state": "Gujarat"}
}

def resolve_location_coordinates(text: str):
    text_lower = text.lower()

    # Match specific district or locality
    for district, data in DISTRICT_CENTROIDS.items():
        if district.lower() in text_lower:
            return {
                "state": data["state"],
                "district": district,
                "latitude": data["lat"],
                "longitude": data["lng"],
                "precision": "DISTRICT_CENTROID"
            }

    # Match State level if text contains state name
    for state in INDIAN_STATES_UTS:
        if state.lower() in text_lower:
            return {
                "state": state,
                "district": "Statewide Advisory",
                "latitude": None,
                "longitude": None,
                "precision": "STATE_LEVEL"
            }

    # Regional or Pan-India warning without specific point coordinates
    return {
        "state": "National / Multi-State",
        "district": "Pan-India / Regional",
        "latitude": None,
        "longitude": None,
        "precision": "REGIONAL_PAN_INDIA"
    }
